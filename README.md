# AI Product Passport

<div align="center" style="background-color: white">
  <a href="https://www.ai4hf.com/">
    <img height="60px" src="readme-assets/ai4hf_logo.svg" alt="AI4HF Project"/>
  </a>
</div>

<br/>

<p align="center">
  <a href="https://github.com/AI4HF/passport">
    <img src="https://img.shields.io/github/license/AI4HF/passport" alt="License">
  </a>
  <a href="https://github.com/AI4HF/passport/releases">
    <img src="https://img.shields.io/github/v/release/AI4HF/passport" alt="Releases">
  </a>
</p>

<br/>

This repository contains the source code and deployment scripts of the backend of the AI Product Passport developed under the [AI4HF](https://www.ai4hf.com/) project. 

The AI Product Passport is one of the traceability tools for post-deployment AI monitoring. It aims to enhance transparency, traceability, and comprehensive documentation of AI models used within the production environments. Its validation and testing has been carried out for the healthcare sector where different predictive AI models have been deployed into hospital environments. Each deployment of each AI model has its AI Product Passport instance. 


# Passport 

To start using the REST methods, follow the instructions below:
- Firstly, run the ```docker compose up```  command via console while in the docker folder.
- After the docker containers start running without issue, edit the application.properties in backend/java/resources with the new client secret that can be obtained from Keycloak GUI.
- Then, run the gateway in the backend folder to start using the REST methods as needed, if they are required separately.
# Docker Configuration

- Docker related files are provided in the docker folder to ensure the proper building of the system's requirements in other locals.
- Simply run ```docker compose up``` command in the console, in the /docker folder.
- Should ensure that the Keycloak realm with the provided details is set up properly
- Will also ensure the proper setting up of other outside dependencies of this project, as the required methods for them are implemented.

- When set up, the Keycloak server will be set up with the provided Keycloak environment details, and the provided realm details in the realm-import.json.
- In this step, all the implemented client and its roles are implemented along with the realm.

# Swagger UI
- Once the Passport is run, go to http://localhost:8080/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/ to access the Swagger documentation.
- Authorization token provided by the login task should be set as the Authorization header for non-login requests, along with a ```Bearer``` at the start.

# DSS
- Upon Passport PDF generation on frontend, respective endpoint signs the created PDF file and sends it back via response.
- Upon server setup, for a properly personalized signature, Java Keytool must be utilized to generate a Keystore file and it must be stored in docs folder under the name keystore.p12. To generate, the following command must be run on a path with relation to the Keytool application:
  - ```keytool -genkeypair -alias mykey -keyalg RSA -keysize 2048 -storetype pkcs12 -keystore keystore.p12 -storepass password -keypass password -validity 365 -dname "CN=YourName, OU=YourOrgUnit, O=YourOrg, L=YourCity, ST=YourState, C=TwoCharacterCountryCode"```
