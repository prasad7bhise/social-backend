FROM quay.io/keycloak/keycloak:latest AS builder

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

COPY keycloak/social-realm.json /opt/keycloak/data/import/social-realm.json

FROM quay.io/keycloak/keycloak:latest
COPY --from=builder /opt/keycloak/data/import/social-realm.json /opt/keycloak/data/import/social-realm.json
COPY keycloak/start.sh /opt/keycloak/bin/start.sh
RUN chmod +x /opt/keycloak/bin/start.sh

ENTRYPOINT ["/opt/keycloak/bin/start.sh", "start-dev", "--import-realm"]
