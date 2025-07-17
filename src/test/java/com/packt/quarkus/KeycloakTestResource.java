package com.packt.quarkus;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class KeycloakTestResource implements QuarkusTestResourceLifecycleManager {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakTestResource.class);

    private KeycloakContainer keycloak;

    @Override
    public Map<String, String> start() {
        keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.1")
                .withRealmImportFile("quarkus-realm-realm.json");

        keycloak.start();

        String keyCloakURL = keycloak.getAuthServerUrl();
        String quarkusOidcAuthServerURL = keyCloakURL + "realms/quarkus-realm";
        LOG.info("+++++===========================++++++++");
        LOG.info("+++++===> Keycloak URL: {}", keyCloakURL);
        LOG.info("+++++===> quarkusOidcAuthServerURL: {}", quarkusOidcAuthServerURL);
        LOG.info("+++++===========================++++++++");

        return Map.of(
                "keycloak.url", keyCloakURL,
                "quarkus.oidc.auth-server-url", quarkusOidcAuthServerURL);
    }

    @Override
    public void stop() {
        if (keycloak != null) {
            keycloak.stop();
        }
    }
}
