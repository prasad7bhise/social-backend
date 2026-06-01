#!/bin/bash
export KC_DB_URL="jdbc:${KC_DB_URL}"
exec /opt/keycloak/bin/kc.sh "$@"
