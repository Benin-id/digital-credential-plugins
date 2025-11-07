## Peru Identity Data Provider Plugin
    - This plugin is specifically built to connect with the Reniec APIs.
    - It fetches identity details of the Peru Citizens subject to the Consulta Dni details provided.


# Configuration for plugin
- Pre-requisites
  - Authorisation Provider should expose the unique identifier in the `sub` field of the JWT token.
      Eg. If one is using eSignet with mock-identity-system:0.10.0 and above it can be achieved by setting:
      ```mosip.mock.ida.kyc.psut.field=individualId```
      where individualId will be the identifier to locate the identity in the expected identity registry.
  - Refer to the sample soap request and response to create request and response DTOs for serialization and deserialization

1. The schema context containing all the required fields should be hosted in a public url.
    - Refer this link for an existing context: [Reniec Context Schema](https://peru-id.github.io/inji-config/Reniec-Peru/reniec-context.json)
      Eg: https://<username>.github.io/<project_name>/<directory_name>/<file_name>.json
    - Also change the respective credential name:
      ```
         {
             "@context": {
                 "@version": 1.1,
                 "@protected": true,
                 "type": "@type",
                 "schema": "https://schema.org/",
                 "<credential_name>": {
                     "@id": "https://<username>.github.io/<project_name>/<file_name>.json#<credential_name>"
                 },
                 <field1>: "schema:<type>"
                 <field2>: "schema:<type>"
                 ...
             }
         }
      ```
    - When the authentication is completed through KBI, then the `sub` field of the claim contains the value of `nuConsultaDni` which is used to fetch the identity data from the resource url.

2. For referring the table creation and template insertion, see the sql scripts under db_scripts/mosip_certify/ddl folder of inji_certify: [db_scripts](https://github.com/peru-id/inji-certify/tree/peru-0.10.x/db_scripts/mosip_certify/ddl)

3. inji-config changes:
    - Refer to the properties file in [inji-config](https://github.com/peru-id/inji-config) that corresponds to the peru-id data provider plugin implementation.
      [Certify Reniec Properties](https://github.com/peru-id/inji-config/blob/peru-0.5.x/certify-reniec-identity.properties)
    - The value for the property `mosip.certify.integration.data-provider-plugin` must be set to `PeruIdentityDataProviderPlugin`
    - Refer to the below properties for setting the `nuDniUsuario`, `nuRucUsuario`, `password` and `endpointUri` values:
      ```
      mosip.certify.peru-id.data-provider-plugin.nu-dni-usuario
      mosip.certify.peru-id.data-provider-plugin.nu-ruc-usuario
      mosip.certify.peru-id.data-provider-plugin.password
      mosip.certify.peru-id.data-provider-plugin.endpoint-uri
      ```
    - Add the specific scope and type of credential in the well-known config of the properties file. Refer to the property `mosip.certify.key-values` for the same.
    - Add the identity fields in the well-known config.

4. Authorization Server config changes:
    - Add the scope of the credential to the supported credential scopes which is addeed to the well-known in the 3rd point.
    - Also add the above scope wherever necessary so that authentication can be carried out with the particular scopes.


# Functioning of the Data Provider Plugin
1. Serialization of requests:
   - Refer to the `request` directory.
   - Root class is `RequestEnvelope`. 
   - Build an object with required values.
   - `ConsultaArg` class contains the parameters required to make a soap request.
   - Pass the `ConsultaArg` object in the `buildSoapRequest` method of `SoapUtil` class.
   - The request object is serialized into a soap request string using JAXBContext Marshalling.

2. Sending request:
   - The soap request obtained in above step is passed in the method `sendSoapRequest` method of `SoapUtilClass`.
   - This method makes a connection request to the `endpointUri` parameter passed to the above method.
   - The response is obtained as a xml string which is then deserialized further.

3. Deserialization of SOAP response:
   - Refer to the `response` directory.
   - Root class is `ResponseEnvelope`.
   - The response xml is then deserialized into `ResponseEnvelope` object using a JAXBContext unmarshalling.
   - The `ResponseReturn` class contains the response parameters as response code, response message and the identity details as the `DatosPersona` object.

4. PeruIdentityDataProviderPlugin
   - This class receives the `ResponseReturn` object.
   - It checks the response status based on the `coResultado` parameter.
   - In case of success, it converts `DatosPersona` object to a `JSONObject` and returns the JSONObject.
   - In case of failure, it throws the `DataProviderExchangeException` with appropriate error message.


## Postman Collections
- Refer to the postman collections [Consulta Datos](postman-collections/consulta-datos.postman_collection_mosip.json)
   