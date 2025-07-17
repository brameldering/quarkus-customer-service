package com.packt.quarkus;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

public class KeycloakTestResource implements QuarkusTestResourceLifecycleManager {

    private KeycloakContainer keycloak;

    @Override
    public Map<String, String> start() {
        keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.1")
                .withRealmImportFile("quarkus-realm-realm.json");
        keycloak.start();

        return Map.of(
                "keycloak.url", keycloak.getAuthServerUrl(),
                "quarkus.oidc.auth-server-url", keycloak.getAuthServerUrl() + "/realms/quarkus-realm"
        );
    }

    @Override
    public void stop() {
        if (keycloak != null) {
            keycloak.stop();
        }
    }
}
