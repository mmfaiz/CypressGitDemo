#!/bin/bash

SECRET_NAME="db-secrets"
ENVIRONMENT="${1?usage: secrets.sh <environment>}"
VAULT="${ENVIRONMENT}-${SECRET_NAME}"
KUBE_CMD="kubectl --context matchi-${ENVIRONMENT}.k8s.local --namespace webapp"

json=$(op get item "AWS ${ENVIRONMENT}-webapp" --vault ${ENVIRONMENT}-webapp-secrets)

$KUBE_CMD delete secret $(tr '[:upper:]' '[:lower:]' <<< $SECRET_NAME) &> /dev/null || true
$KUBE_CMD create secret generic $(tr '[:upper:]' '[:lower:]' <<< $SECRET_NAME) \
    --from-literal=DB_USER=$(echo ${json} | jq -r '.details.sections[0].fields[] | select(.n=="username").v') \
    --from-literal=DB_PASSWORD=$(echo ${json} | jq -r '.details.sections[0].fields[] | select(.n=="password").v') \
    --from-literal=DB_HOST=$(echo ${json} | jq -r '.details.sections[0].fields[] | select(.n=="hostname").v') \
    --from-literal=DB_NAME=$(echo ${json} | jq -r '.details.sections[0].fields[] | select(.n=="database").v')
