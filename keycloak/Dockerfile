FROM quay.io/keycloak/keycloak:latest AS builder

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

COPY keycloak/social-realm.json /opt/keycloak/data/import/social-realm.json

FROM quay.io/keycloak/keycloak:latest
COPY --from=builder /opt/keycloak/data/import/social-realm.json /opt/keycloak/data/import/social-realm.json

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm"]
