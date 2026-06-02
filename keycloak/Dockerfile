FROM quay.io/keycloak/keycloak:26.6.2 AS builder

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres

COPY keycloak/social-realm-realm.json /opt/keycloak/data/import/social-realm-realm.json

RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.6.2

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --chmod=0755 keycloak/start.sh /opt/keycloak/bin/start.sh

ENTRYPOINT ["/opt/keycloak/bin/start.sh", "start", "--optimized", "--import-realm"]
