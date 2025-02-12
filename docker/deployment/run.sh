#!/bin/bash


docker compose -f passport/docker/deployment/docker-compose.yaml --project-directory ./ -p ai4hf-passport up -d
