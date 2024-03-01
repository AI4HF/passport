# Passport 

To start using the REST methods and the GUI implementation itself, follow the instructions below:
- Firstly, run the "docker compose up" command via console while in the docker folder.
- After the docker containers start running without issue, edit the application.properties in backend/java/resources with the new client secret that can be obtained from Keycloak GUI.
- Then, run the gateway in the backend folder to start using the REST methods as needed, if they are required separately.
- After both the Keycloak and Gateway are set up, run the Angular project to start using the GUI implementation of the Passport.