#!/usr/bin/env bash

JAVA_CMD="java -Xms256m -Xmx3g -jar "

# Configure application.conf path
if [ ! -z "$APP_CONF_FILE" ]; then
  JAVA_CMD+="-Dconfig.file=$APP_CONF_FILE "
fi

# Configure keycloak client secret
if [ ! -z "$KEYCLOAK_CLIENT_SECRET" ]; then
  JAVA_CMD+="-Dkeycloak.credentials.secret=$KEYCLOAK_CLIENT_SECRET "
fi

# Configure database connection url
if [ ! -z "$DB_URL" ]; then
  JAVA_CMD+="-Dspring.datasource.url=$DB_URL "
fi

# Configure keycloak url
if [ ! -z "$KEYCLOAK_URL" ]; then
  JAVA_CMD+="-Dkeycloak.auth-server-url=$KEYCLOAK_URL "
fi

# Configure keystore path
if [ ! -z "$KEYSTORE_PATH" ]; then
  JAVA_CMD+="-Ddss.keystore.path=$KEYSTORE_PATH "
fi

# Configure keystore password
if [ ! -z "$KEYSTORE_PASSWORD" ]; then
  JAVA_CMD+="-Ddss.keystore.password=$KEYSTORE_PASSWORD "
fi

# Delay the execution for this amount of seconds
if [ ! -z "$DELAY_EXECUTION" ]; then
  sleep $DELAY_EXECUTION
fi

# Finally, tell which jar to run
JAVA_CMD+="passport-2.3.1.jar"

eval $JAVA_CMD "$@"
