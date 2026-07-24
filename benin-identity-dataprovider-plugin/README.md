# Benin Identity Data Provider Plugin

An inji-certify `DataProviderPlugin` that issues Verifiable Credentials from
Benin birth records (*actes de naissance*) held by ANIP.

The plugin resolves the citizen's NPI from the eSignet session, fetches the
birth record from the ANIP API, and projects it onto the credential subject
using an externalised field mapping.

---

## Flow

```
Wallet ──/credential──► Certify ──► BeninIdentityDataProviderPlugin
                                          │
                    1. accessTokenHash ───┤
                                          ▼
                                    CacheService
                                    reads Redis  "userinfo::{accessTokenHash}"
                                    → OIDCTransaction.individualId  ("8820267980@npi")
                                    → strips "@npi"                 → "8820267980"
                                          │
                    2. NPI ───────────────┤
                                          ▼
                                 ActeNaissanceApiClient
                                    POST {base-url}/auth      → access_token (15 min)
                                    GET  {base-url}/{npi}     → birth record
                                          │
                    3. record ────────────┤
                                          ▼
                              BeninFieldMappingConfig
                                    JSON-Pointer expressions from properties
                                          │
                                          ▼
                                    credentialSubject → VC template → signed VC
```

---

## Modules

| File | Responsibility |
|---|---|
| `BeninIdentityDataProviderPlugin` | Entry point (`fetchData`); orchestrates the flow and applies the mapping |
| `CacheService` | Resolves the NPI from the eSignet `userinfo` transaction in Redis |
| `ActeNaissanceApiClient` | ANIP auth + record lookup, with token caching and 401 retry |
| `ActeNaissanceApiException` | Transport / auth / parse failures |
| `BeninFieldMappingConfig` | `@ConfigurationProperties` holding the output-key → expression map |
| `AuthRequest` / `AuthResponse` | Auth endpoint DTOs |
| `ActeNaissanceResponse` | Birth-record DTO (nested, with per-block catch-all) |

---

## Configuration

### Plugin activation

```properties
mosip.certify.integration.data-provider-plugin=BeninIdentityDataProviderPlugin
mosip.certify.integration.scan-base-package=io.mosip.certify.beniniddataprovider
```

The scan package must cover `...integration.config`, or
`BeninFieldMappingConfig` never registers and the mapping comes back empty.

### ANIP API

```properties
mosip.certify.benin-id.data-provider-plugin.base-url=https://testussd.anip.bj/test/acte-naissance
mosip.certify.benin-id.data-provider-plugin.login=<login>
mosip.certify.benin-id.data-provider-plugin.password=<password>
mosip.certify.benin-id.data-provider-plugin.connect-timeout-ms=10000
mosip.certify.benin-id.data-provider-plugin.read-timeout-ms=15000
```

`/auth` and `/{npi}` are appended to `base-url`.

> **Credentials must not be committed.** Supply `login` / `password` from a
> secret, vault, or an externally mounted properties file — not from
> `application.properties` in the repo.

### NPI resolution

```properties
mosip.certify.cache.store.individual-id=true
mosip.certify.cache.secure.individual-id=false
```

`secure.individual-id=true` is **not supported** — decryption is not
implemented. If the deployment encrypts the stored individualId, this needs
building before the plugin will work there.

### Field mapping

One property per credential-subject field:

```properties
mosip.certify.benin-id.data-provider-plugin.mapping.<outputKey>=<expression>
```

Expression grammar — JSON Pointers (RFC 6901) rooted at the response `data`
object:

| Form | Meaning |
|---|---|
| `/enfant/nom` | single value |
| `X \|\| Y \|\| Z` | fallback — first non-blank wins |
| `X + Y + Z` | join non-blank parts with a single space |

Do not mix `+` and `||` in one expression; the evaluator checks `||` first.
There is no value transformation (no `MASCULIN`→`M`, no date reformatting).

Full working set: see `benin-mapping.properties`.

---

## ANIP API contract

**Auth** — `POST {base-url}/auth`

```json
{ "login": "...", "password": "..." }
```
```json
{ "access_token": "eyJ...", "token_type": "Bearer", "expires_in": 900 }
```

The token is cached in memory and reused until 30s before its `exp` claim.
A 401/403 on the lookup triggers one forced re-auth and retry.

**Lookup** — `GET {base-url}/{npi}` with `Authorization: Bearer <token>`

```json
{
  "success": true,
  "data": {
    "npi": "...", "numeroFormulaire": "...", "referenceActe": "...",
    "enfant":  { "nom", "prenoms", "sexe", "dateNaissance", "lieuNaissance",
                 "nationalite", "profession", "domicile", "npi" },
    "pere":    { "nom", "prenoms", "dateNaissance", "lieuNaissance",
                 "profession", "domicile", "npi" },
    "mere":    { ...same as pere... },
    "naissance":      { "date", "lieu", "departementCode", "communeCode",
                        "arrondissementCode", "villageCode", "paysCode" },
    "enregistrement": { "centreDeclarant", "departementCode", "communeCode",
                        "arrondissementCode", "villageCode" },
    "sources": [ "..." ]
  }
}
```

Fields ANIP adds later are captured by each block's `additionalProperties`
catch-all rather than dropped.

**Fields observed null in real records:** parents' `profession`, `domicile`,
`npi`; `enregistrement.centreDeclarant`. The credential template must tolerate
their absence — blank values are skipped, so those keys are simply not emitted.

---

## TLS — required setup

`testussd.anip.bj` serves a Let's Encrypt **Generation Y** chain
(leaf → YR1 → Root YR). ANIP does not serve the cross-signed Root YR, and no
JDK trusts Root YR natively yet, so Java fails with:

```
PKIX path building failed: unable to find valid certification path
```

Postman does not show this because it disables certificate verification by
default.

**Workaround** — trust Root YR explicitly:

```bash
curl -o root-yr.der https://letsencrypt.org/certs/gen-y/root-yr.der
cp "$JAVA_HOME/lib/security/cacerts" certify-truststore.jks
keytool -importcert -alias isrg-root-yr -file root-yr.der \
        -keystore certify-truststore.jks -storepass changeit -noprompt
```

Run Certify with:

```
-Djavax.net.ssl.trustStore=/path/certify-truststore.jks
-Djavax.net.ssl.trustStorePassword=changeit
```

Copy `cacerts` rather than creating an empty store — `trustStore` *replaces*
the default, so a bare store breaks every other TLS connection.

In Kubernetes, mount the store as a secret and set `JAVA_TOOL_OPTIONS`.

> **This is scaffolding.** The correct fix is ANIP serving the complete chain,
> after which no client needs the workaround. Remove the alias once that
> happens or once JDKs ship Root YR.

---

## VC artefacts

| File | Purpose |
|---|---|
| `benin-credential-config-fr.json` | Credential config (French locale) with the base64 VC template |
| `vc-template.vm` | Velocity VC template, readable form |
| `mosip-identity-context.json` | JSON-LD context, extended with the birth-record terms |
| `acte-naissance-template.html` | HTML render template for the wallet |

Every credential-subject term must be defined in the JSON-LD context or
`ldp_vc` canonicalization drops it. The context is served via jsDelivr, which
caches aggressively — purge after publishing:

```
https://purge.jsdelivr.net/gh/<org>/mosip-identity-context@main/mosip-identity-context.json
```

Always verify an issued credential, not just that one was produced. A dropped
term shows up as a missing field in the signed VC, not as an error.

---

## Build & deploy

```bash
mvn clean install -DskipTests=true
```

`clean` is not optional — a plain `install` reuses stale `.class` files.

For a containerised Certify, `mvn install` on a workstation changes nothing
until the image is rebuilt or the mounted jar is replaced. Check for old
plugin jars alongside the new one; a stale copy can win on the classpath.

`CacheService` logs its `CacheManager` at startup — use that line to confirm
the build actually deployed.

---

## Troubleshooting

| Symptom | Cause |
|---|---|
| `INDIVIDUAL_ID_NOT_FOUND` | No `userinfo::{hash}` in Redis, or Certify and eSignet are on different Redis instances / db indexes. Compare `spring.data.redis.*` on both. |
| `CacheManager=ConcurrentMapCacheManager` at startup | Certify fell back to an in-memory cache; set `spring.cache.type=redis` |
| `PKIX path building failed` | Truststore not reaching the JVM. Verify `System.getProperty("javax.net.ssl.trustStore")` is non-null in the running process. |
| ANIP call returns 404 | NPI still carries the `@npi` suffix — `stripIdTypeSuffix` should remove it |
| `success=null, data=null` after a 200 | Response parsed into the wrong shape — confirm `parseActeNaissance` calls `readValue(body, ...)` on the **whole** body, not a sub-node |
| Credential subject nearly empty | Mapping config not loaded; check `mapping entries loaded` and the scan package |
| Log wording doesn't match the source | Stale build — rebuild and redeploy |

Two long-running failures in this plugin's history were stale artefacts and a
sub-node parse; both look like data problems and are not. Confirm the running
build first.

---

## Known gaps

- `mosip.certify.cache.secure.individual-id=true` is unimplemented.
- The mapping DSL cannot transform values, only relocate them.
- `CacheService` hardcodes the `userinfo::` key layout.
- `declarationTrouvee` / `referenceActe` semantics are unconfirmed: whether a
  credential should be issued when no formal declaration exists is a policy
  question for ANIP, not a code default.