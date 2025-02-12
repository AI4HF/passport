#!/bin/bash
echo "Pulling tofhir-server, elasticsearch, kibana, and fluentd images from public Docker Hub..."
docker compose -f passport/docker/deployment/docker-compose.yaml pull keycloak passport passport-web backend_postgres

