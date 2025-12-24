package uk.gov.hmcts.cp.subscription;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.api.SubscriptionApi;
import uk.gov.hmcts.cp.openapi.model.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OpenAPISpecTest {

    @Test
    void event_type_enum_should_have_expected_values() {
        assertThat(EventType.class.isEnum()).isTrue();
        assertThat(EventType.class.getEnumConstants())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("PCR", "CUSTODIAL_RESULT");
    }

    @Test
    void notification_endpoint_should_have_expected_fields() {
        assertThat(NotificationEndpoint.class)
                .hasDeclaredFields("webhookUrl");
    }

    @Test
    void notification_endpoint_should_have_correct_type() throws NoSuchFieldException {
        Field webhookUrlField = NotificationEndpoint.class.getDeclaredField("webhookUrl");
        assertThat(webhookUrlField.getType()).isEqualTo(String.class);
    }

    @Test
    void subscription_request_should_have_expected_fields() {
        assertThat(ClientSubscriptionRequest.class).hasDeclaredFields("notificationEndpoint");
        assertThat(ClientSubscriptionRequest.class).hasDeclaredFields("eventTypes");
    }

    @Test
    void client_subscription_request_fields_should_have_correct_types() throws NoSuchFieldException {
        Field notificationEndpointField = ClientSubscriptionRequest.class.getDeclaredField("notificationEndpoint");
        Field eventTypesField = ClientSubscriptionRequest.class.getDeclaredField("eventTypes");

        assertThat(notificationEndpointField.getType()).isEqualTo(NotificationEndpoint.class);
        assertThat(eventTypesField.getType()).isAssignableFrom(List.class);
    }

    @Test
    void subscription_response_should_have_expected_fields() {
        assertThat(ClientSubscription.class).hasDeclaredFields("clientSubscriptionId");
        assertThat(ClientSubscription.class).hasDeclaredFields("notificationEndpoint");
        assertThat(ClientSubscription.class).hasDeclaredFields("eventTypes");
        assertThat(ClientSubscription.class).hasDeclaredFields("createdAt");
        assertThat(ClientSubscription.class).hasDeclaredFields("updatedAt");
    }

    @Test
    void client_subscription_fields_should_have_correct_types() throws NoSuchFieldException {
        Field clientSubscriptionIdField = ClientSubscription.class.getDeclaredField("clientSubscriptionId");
        Field notificationEndpointField = ClientSubscription.class.getDeclaredField("notificationEndpoint");
        Field eventTypesField = ClientSubscription.class.getDeclaredField("eventTypes");
        Field createdAtField = ClientSubscription.class.getDeclaredField("createdAt");
        Field updatedAtField = ClientSubscription.class.getDeclaredField("updatedAt");

        assertThat(clientSubscriptionIdField.getType()).isEqualTo(UUID.class);
        assertThat(notificationEndpointField.getType()).isEqualTo(NotificationEndpoint.class);
        assertThat(eventTypesField.getType()).isAssignableFrom(List.class);
        assertThat(createdAtField.getType()).isEqualTo(OffsetDateTime.class);
        assertThat(updatedAtField.getType()).isEqualTo(OffsetDateTime.class);
    }

    @Test
    void subscription_api_should_have_expected_methods() {
        assertThat(SubscriptionApi.class.getMethods())
                .extracting(Method::getName)
                .containsAll(List.of("createClientSubscription", "getClientSubscription", "updateClientSubscription", "deleteClientSubscription"));
    }

    @Test
    void pcr_event_payload_should_have_all_expected_top_level_fields() throws NoSuchFieldException {
        assertThat(PcrEventPayload.class.getDeclaredField("eventId").getType())
                .isEqualTo(UUID.class);

        assertThat(PcrEventPayload.class.getDeclaredField("eventType").getType())
                .isEqualTo(String.class);

        assertThat(PcrEventPayload.class.getDeclaredField("timestamp").getType())
                .isEqualTo(OffsetDateTime.class);

        assertThat(List.class.isAssignableFrom(
                PcrEventPayload.class.getDeclaredField("defendants").getType()))
                .as("defendants field should be a List")
                .isTrue();
    }

}