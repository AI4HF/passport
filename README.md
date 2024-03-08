# Passport 

To start using the REST methods and the GUI implementation itself, follow the instructions below:
- Firstly, run the "docker compose up" command via console while in the docker folder.
- After the docker containers start running without issue, edit the application.properties in backend/java/resources with the new client secret that can be obtained from Keycloak GUI.
- Then, run the gateway in the backend folder to start using the REST methods as needed, if they are required separately.
- After both the Keycloak and Gateway are set up, run the Angular project to start using the GUI implementation of the Passport.
# Docker Configuration

- Docker related files are provided in the docker folder to ensure the proper building of the system's requirements in other locals.
- Simply run "docker compose up" command in the console, in the /docker folder.
- Should ensure that the Keycloak realm with the provided details is set up properly
- Will also ensure the proper setting up of other outside dependencies of this project, as the required methods for them are implemented.

- When set up, the Keycloak server will be set up with the provided Keycloak environment details, and the provided realm details in the realm-import.json.
- In this step, all the implemented client and its roles are implemented along with the realm.