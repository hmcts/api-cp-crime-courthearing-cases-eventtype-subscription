package uk.gov.hmcts.cp.subscription;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.api.NotificationApi;
import uk.gov.hmcts.cp.openapi.api.SubscriptionApi;
import uk.gov.hmcts.cp.openapi.model.ClientSubscription;
import uk.gov.hmcts.cp.openapi.model.ClientSubscriptionRequest;
import uk.gov.hmcts.cp.openapi.model.EventNotificationPayload;
import uk.gov.hmcts.cp.openapi.model.EventNotificationPayloadCasesInner;
import uk.gov.hmcts.cp.openapi.model.EventTypePayload;
import uk.gov.hmcts.cp.openapi.model.EventTypeResponse;
import uk.gov.hmcts.cp.openapi.model.HearingEventResponse;
import uk.gov.hmcts.cp.openapi.model.HmacCredentials;
import uk.gov.hmcts.cp.openapi.model.NotificationEndpoint;
import uk.gov.hmcts.cp.openapi.model.EventPayload;
import uk.gov.hmcts.cp.openapi.model.EventPayloadDefendant;
import uk.gov.hmcts.cp.openapi.model.RotateSecretRequest;

import java.util.Map;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OpenAPISpecTest {

    @Test
    void notification_endpoint_should_have_expected_fields() {
        assertThat(NotificationEndpoint.class)
                .hasDeclaredFields("callbackUrl");
    }

    @Test
    void notification_endpoint_should_have_correct_type() throws NoSuchFieldException {
        Field callbackUrlField = NotificationEndpoint.class.getDeclaredField("callbackUrl");
        assertThat(callbackUrlField.getType()).isEqualTo(String.class);
    }

    @SneakyThrows
    @Test
    void get_event_types_response_should_have_expected_fields() {
        Field eventTypesField = EventTypeResponse.class.getDeclaredField("events");
        assertThat(eventTypesField.getType()).isEqualTo(List.class);
        assertThat(EventTypePayload.class).hasDeclaredFields("eventName");
        assertThat(EventTypePayload.class).hasDeclaredFields("displayName");
        assertThat(EventTypePayload.class).hasDeclaredFields("category");
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
        assertThat(ClientSubscription.class).hasDeclaredFields("hmac");
        assertThat(HmacCredentials.class).hasDeclaredFields("keyId");
        assertThat(HmacCredentials.class).hasDeclaredFields("secret");
        assertThat(ClientSubscription.class).hasDeclaredFields("createdAt");
        assertThat(ClientSubscription.class).hasDeclaredFields("updatedAt");
    }

    @Test
    void client_subscription_fields_should_have_correct_types() throws NoSuchFieldException {
        Field clientSubscriptionIdField = ClientSubscription.class.getDeclaredField("clientSubscriptionId");
        Field notificationEndpointField = ClientSubscription.class.getDeclaredField("notificationEndpoint");
        Field eventTypesField = ClientSubscription.class.getDeclaredField("eventTypes");
        Field hmacField = ClientSubscription.class.getDeclaredField("hmac");
        Field keyIdField = HmacCredentials.class.getDeclaredField("keyId");
        Field secretField = HmacCredentials.class.getDeclaredField("secret");
        Field createdAtField = ClientSubscription.class.getDeclaredField("createdAt");
        Field updatedAtField = ClientSubscription.class.getDeclaredField("updatedAt");

        assertThat(clientSubscriptionIdField.getType()).isEqualTo(UUID.class);
        assertThat(notificationEndpointField.getType()).isEqualTo(NotificationEndpoint.class);
        assertThat(eventTypesField.getType()).isAssignableFrom(List.class);
        assertThat(hmacField.getType()).isEqualTo(HmacCredentials.class);
        assertThat(keyIdField.getType()).isEqualTo(String.class);
        assertThat(secretField.getType()).isEqualTo(String.class);
        assertThat(createdAtField.getType()).isEqualTo(Instant.class);
        assertThat(updatedAtField.getType()).isEqualTo(Instant.class);
    }

    @Test
    void subscription_api_should_have_expected_methods() {
        assertThat(SubscriptionApi.class.getMethods())
                .extracting(Method::getName)
                .containsAll(List.of("createClientSubscription", "getClientSubscription", "updateClientSubscription", "deleteClientSubscription", "rotateClientSubscriptionSecret"));
    }

    @Test
    void rotate_secret_request_should_have_key_id_field() throws NoSuchFieldException {
        Field keyIdField = RotateSecretRequest.class.getDeclaredField("keyId");
        assertThat(keyIdField.getType()).isEqualTo(String.class);
    }

    @Test
    void rotate_secret_method_should_return_hmac_credentials() throws NoSuchMethodException {
        Method method = SubscriptionApi.class.getMethod(
                "rotateClientSubscriptionSecret", UUID.class, RotateSecretRequest.class, UUID.class);
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        assertThat(returnType.getActualTypeArguments()[0]).isEqualTo(HmacCredentials.class);
    }

    @Test
    void pcr_event_payload_should_have_all_expected_fields() throws NoSuchFieldException {
        assertThat(EventPayload.class.getDeclaredField("eventId").getType()).isEqualTo(UUID.class);
        assertThat(EventPayload.class.getDeclaredField("hearingId").getType()).isEqualTo(UUID.class);
        assertThat(EventPayload.class.getDeclaredField("eventType").getType()).isEqualTo(String.class);
        assertThat(EventPayload.class.getDeclaredField("timestamp").getType()).isEqualTo(Instant.class);
        assertThat(EventPayload.class.getDeclaredField("payload").getType()).isEqualTo(Map.class);

        assertThat(EventPayloadDefendant.class.getDeclaredField("masterDefendantId").getType()).isEqualTo(UUID.class);
        assertThat(EventPayloadDefendant.class.getDeclaredField("name").getType()).isEqualTo(String.class);
        assertThat(EventPayloadDefendant.class.getDeclaredField("dateOfBirth").getType()).isEqualTo(LocalDate.class);
        assertThat(EventPayloadDefendant.class.getDeclaredField("cases").getType()).isEqualTo(List.class);
    }

    @Test
    void event_notification_payload_should_have_all_expected_fields() throws NoSuchFieldException {
        assertThat(EventNotificationPayload.class.getDeclaredField("hearingEventId").getType()).isEqualTo(UUID.class);
        assertThat(EventNotificationPayload.class.getDeclaredField("eventType").getType()).isEqualTo(String.class);
        assertThat(EventNotificationPayload.class.getDeclaredField("cases").getType()).isAssignableFrom(List.class);
        assertThat(EventNotificationPayloadCasesInner.class.getDeclaredField("urn").getType()).isEqualTo(String.class);
        assertThat(EventNotificationPayload.class.getDeclaredField("masterDefendantId").getType()).isEqualTo(UUID.class);
        assertThat(EventNotificationPayload.class.getDeclaredField("documentId").getType()).isEqualTo(UUID.class);
        assertThat(EventNotificationPayload.class.getDeclaredField("documentGeneratedTimestamp").getType()).isEqualTo(Instant.class);
        assertThat(EventNotificationPayload.class.getDeclaredField("prisonEmailAddress").getType()).isEqualTo(String.class);
        assertThat(EventNotificationPayload.class.getDeclaredField("hearingId").getType()).isEqualTo(UUID.class);
    }

    @Test
    void hearing_event_response_should_have_all_expected_fields() throws NoSuchFieldException {
        assertThat(HearingEventResponse.class.getDeclaredField("hearingEventId").getType()).isEqualTo(UUID.class);
        assertThat(HearingEventResponse.class.getDeclaredField("eventType").getType()).isEqualTo(String.class);
        assertThat(HearingEventResponse.class.getDeclaredField("createdAt").getType()).isEqualTo(Instant.class);
        assertThat(HearingEventResponse.class.getDeclaredField("payload").getType()).isEqualTo(String.class);
    }

    @Test
    void notification_api_should_have_get_hearing_event_method() throws NoSuchMethodException {
        Method method = NotificationApi.class.getMethod("getHearingEvent", UUID.class, UUID.class, UUID.class);
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        assertThat(returnType.getActualTypeArguments()[0]).isEqualTo(HearingEventResponse.class);
    }
}