#!/bin/bash
echo "Pulling keycloak, passport, passport-web, and postgres images from public Docker Hub..."
docker compose -f passport/docker/deployment/docker-compose.yaml pull keycloak passport passport-web backend_postgres

