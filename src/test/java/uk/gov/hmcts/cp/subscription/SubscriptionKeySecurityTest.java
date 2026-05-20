package uk.gov.hmcts.cp.subscription;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionKeySecurityTest {

    private static Map<String, Object> spec;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void loadSpec() {
        Yaml yaml = new Yaml();
        InputStream stream = SubscriptionKeySecurityTest.class
                .getResourceAsStream("/openapi/openapi-spec.yml");
        assertThat(stream).as("openapi-spec.yml must be on the classpath").isNotNull();
        spec = yaml.load(stream);
    }

    @Test
    @SuppressWarnings("unchecked")
    void spec_should_define_subscription_key_security_scheme() {
        Map<String, Object> components = (Map<String, Object>) spec.get("components");
        Map<String, Object> securitySchemes = (Map<String, Object>) components.get("securitySchemes");
        Map<String, Object> subscriptionKey = (Map<String, Object>) securitySchemes.get("subscriptionKey");

        assertThat(subscriptionKey)
                .as("subscriptionKey security scheme must be defined in components/securitySchemes")
                .isNotNull();
        assertThat(subscriptionKey.get("type"))
                .as("subscriptionKey must be of type apiKey")
                .isEqualTo("apiKey");
        assertThat(subscriptionKey.get("in"))
                .as("subscriptionKey must be passed in header")
                .isEqualTo("header");
        assertThat(subscriptionKey.get("name"))
                .as("subscriptionKey header name must be Ocp-Apim-Subscription-Key")
                .isEqualTo("Ocp-Apim-Subscription-Key");
    }

    @Test
    @SuppressWarnings("unchecked")
    void global_security_should_require_subscription_key() {
        List<Map<String, Object>> security = (List<Map<String, Object>>) spec.get("security");

        assertThat(security)
                .as("Global security must be defined")
                .isNotNull()
                .isNotEmpty();

        boolean hasSubscriptionKey = security.stream()
                .anyMatch(entry -> entry.containsKey("subscriptionKey"));
        assertThat(hasSubscriptionKey)
                .as("Global security must include subscriptionKey (Ocp-Apim-Subscription-Key)")
                .isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void global_security_should_require_bearer_auth() {
        List<Map<String, Object>> security = (List<Map<String, Object>>) spec.get("security");

        boolean hasBearerAuth = security.stream()
                .anyMatch(entry -> entry.containsKey("bearerAuth"));
        assertThat(hasBearerAuth)
                .as("Global security must include bearerAuth (JWT)")
                .isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void subscription_endpoints_should_return_401_when_credentials_missing_or_invalid() {
        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");

        assertEndpointHasResponseCode(paths, "/event-types", "get", "401");
        assertEndpointHasResponseCode(paths, "/client-subscriptions", "post", "401");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}", "get", "401");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}", "put", "401");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}", "delete", "401");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}/documents/{documentId}", "get", "401");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}/hearing-events/{hearingEventId}", "get", "401");
    }

    @Test
    @SuppressWarnings("unchecked")
    void subscription_endpoints_should_return_403_when_not_subscribed_to_api_product() {
        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");

        assertEndpointHasResponseCode(paths, "/event-types", "get", "403");
        assertEndpointHasResponseCode(paths, "/client-subscriptions", "post", "403");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}", "get", "403");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}", "put", "403");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}", "delete", "403");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}/documents/{documentId}", "get", "403");
        assertEndpointHasResponseCode(paths, "/client-subscriptions/{clientSubscriptionId}/hearing-events/{hearingEventId}", "get", "403");
    }

    @Test
    @SuppressWarnings("unchecked")
    void event_notification_payload_hearingEventId_should_be_optional() {
        Map<String, Object> components = (Map<String, Object>) spec.get("components");
        Map<String, Object> schemas = (Map<String, Object>) components.get("schemas");
        Map<String, Object> schema = (Map<String, Object>) schemas.get("EventNotificationPayload");
        List<String> required = (List<String>) schema.get("required");

        assertThat(required)
                .as("hearingEventId must not be in EventNotificationPayload required array — service omits it when toggle is off")
                .doesNotContain("hearingEventId");
    }

    @SuppressWarnings("unchecked")
    private void assertEndpointHasResponseCode(
            Map<String, Object> paths, String path, String method, String responseCode) {
        Map<String, Object> pathItem = (Map<String, Object>) paths.get(path);
        assertThat(pathItem).as("Path %s not found in spec", path).isNotNull();
        Map<String, Object> operation = (Map<String, Object>) pathItem.get(method);
        assertThat(operation).as("Method %s %s not found in spec", method.toUpperCase(), path).isNotNull();
        Map<String, Object> responses = (Map<String, Object>) operation.get("responses");
        assertThat(responses).as("Responses for %s %s not defined", method.toUpperCase(), path).isNotNull();
        assertThat(responses)
                .as("%s %s must define a %s response", method.toUpperCase(), path, responseCode)
                .containsKey(responseCode);
    }
}