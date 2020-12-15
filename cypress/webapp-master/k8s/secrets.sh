#!/usr/bin/env bash
set -euo pipefail

SECRET_NAME="webapp-secrets"
ENVIRONMENT="${1?usage: secrets.sh <environment>}"
VAULT="${ENVIRONMENT}-${SECRET_NAME}"
KUBE_CMD="kubectl --context matchi-${ENVIRONMENT}.k8s.local --namespace webapp"

checkLogin() {
  if op list items > /dev/null 2>&1
  then
    echo "1Password session OK"
  else
    echo "You need to login to 1Password, try:"
    echo "eval \$(op signin matchi <username>)"
    exit 1
  fi
}

checkVault() {
  if op get vault ${VAULT} > /dev/null 2>&1
  then
    echo "Fetching secrets from vault ${VAULT}"
  else
    echo "No vault with name ${VAULT} exist"
    exit 1
  fi
}

getFrom1Password() {
    op get item $1 --vault "${VAULT}" | jq -r '.details.fields[] | select(.designation=="password").value'
    if [[ ! $? ]]
    then
      echo "Failed to find secret named {$1} in vault ${VAULT}"
      exit 1
    fi
}

checkLogin
checkVault

./db-secrets.sh ${ENVIRONMENT}

$KUBE_CMD delete secret $(tr '[:upper:]' '[:lower:]' <<< $SECRET_NAME)  &> /dev/null || true
$KUBE_CMD create secret generic $(tr '[:upper:]' '[:lower:]' <<< $SECRET_NAME)  \
  --from-literal=ADYEN_PASSWORD="$(getFrom1Password ADYEN_PASSWORD)" \
  --from-literal=ADYEN_CONFIRM_PASSWORD="$(getFrom1Password ADYEN_CONFIRM_PASSWORD)" \
  --from-literal=BOXNET_KEY="$(getFrom1Password BOXNET_KEY)" \
  --from-literal=FACEBOOK_CLIENT_SECRET="$(getFrom1Password FACEBOOK_CLIENT_SECRET)" \
  --from-literal=FORTNOX_API_V3_ACCESSTOKEN="$(getFrom1Password FORTNOX_API_V3_ACCESSTOKEN)" \
  --from-literal=FORTNOX_API_V3_CLIENTSECRET="$(getFrom1Password FORTNOX_API_V3_CLIENTSECRET)" \
  --from-literal=GRAILS_MAIL_PASSWORD="$(getFrom1Password GRAILS_MAIL_PASSWORD)" \
  --from-literal=INTERCOM_SECRET_KEY="$(getFrom1Password INTERCOM_SECRET_KEY)" \
  --from-literal=IDROTT_ONLINE_PASSWORD="$(getFrom1Password IDROTT_ONLINE_PASSWORD)" \
  --from-literal=KAFKA_API_SECRET="$(getFrom1Password KAFKA_API_SECRET)" \
  --from-literal=MATEX_PASSWORD="$(getFrom1Password MATEX_PASSWORD)" \
  --from-literal=MPC_PASSWORD="$(getFrom1Password MPC_PASSWORD)" \
  --from-literal=newrelic.config.license_key="$(getFrom1Password NEWRELIC_CONFIG_LICENSEKEY)" \
  --from-literal=NEWRELIC_BROWSER_LICENSEKEY="$(getFrom1Password NEWRELIC_BROWSER_LICENSEKEY)" \
  --from-literal=ELEVIO_CLIENT_SECRET="$(getFrom1Password ELEVIO_CLIENT_SECRET)" \
  --from-literal=GOOGLE_SHORTENER_API_KEY="$(getFrom1Password GOOGLE_SHORTENER_API_KEY)" \
  --from-literal=GOOGLE_MAPS_API_KEY="$(getFrom1Password GOOGLE_MAPS_API_KEY)" \
  --from-literal=APPLE_SIGN_IN_KEY_ID="$(getFrom1Password APPLE_SIGN_IN_KEY_ID)" \
  --from-literal=APPLE_SIGN_IN_TEAM_ID="$(getFrom1Password APPLE_SIGN_IN_TEAM_ID)" \
  --from-literal=APPLE_SIGN_IN_AUTH_KEY="$(getFrom1Password APPLE_SIGN_IN_AUTH_KEY)" \
  --from-literal=RECAPTCHA_PRIVATE_KEY="$(getFrom1Password RECAPTCHA_PRIVATE_KEY)" 