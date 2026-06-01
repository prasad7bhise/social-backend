#!/bin/bash
export KC_DB_URL="jdbc:postgresql://${KC_DB_URL_HOST}:${KC_DB_URL_PORT}/${KC_DB_URL_DATABASE}"
exec /opt/keycloak/bin/kc.sh "$@"
