package uk.gov.hmcts.cp.subscription;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.model.ClientSubscriptionRequest;
import uk.gov.hmcts.cp.openapi.model.EventType;
import uk.gov.hmcts.cp.openapi.model.NotificationEndpoint;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ValidateClientSubscriptionRequestTest {

    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validate_request_should_accept_valid_request() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of(EventType.CUSTODIAL_RESULT, EventType.PCR))
                .notificationEndpoint(NotificationEndpoint.builder().webhookUrl("https://good-url").build())
                .build();
        Set<ConstraintViolation<ClientSubscriptionRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void validate_request_should_reject_missing_event_types() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .notificationEndpoint(NotificationEndpoint.builder().webhookUrl("https://good-url").build())
                .build();
        validate_request_error(request, "size must be between 1 and 2");
    }

    @Test
    void validate_request_should_reject_empty_event_types() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of())
                .notificationEndpoint(NotificationEndpoint.builder().webhookUrl("https://good-url").build())
                .build();
        validate_request_error(request, "size must be between 1 and 2");
    }

    @Test
    void validate_request_should_reject_too_many_event_types() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of(EventType.CUSTODIAL_RESULT, EventType.PCR, EventType.PCR))
                .notificationEndpoint(NotificationEndpoint.builder().webhookUrl("https://good-url").build())
                .build();
        validate_request_error(request, "size must be between 1 and 2");
    }

    @Test
    void validate_request_should_reject_missing_url() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of(EventType.CUSTODIAL_RESULT))
                .notificationEndpoint(NotificationEndpoint.builder().build())
                .build();
        validate_request_error(request, "must not be null");
    }

    @Test
    void validate_request_should_reject_bad_url() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of(EventType.PCR))
                .notificationEndpoint(NotificationEndpoint.builder().webhookUrl("bad-url").build())
                .build();
        validate_request_error(request, "must match \"^https://.*$\"");
    }

    @Test
    void validate_request_should_reject_none_https_url() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of(EventType.PCR))
                .notificationEndpoint(NotificationEndpoint.builder().webhookUrl("http://bad-url").build())
                .build();
        validate_request_error(request, "must match \"^https://.*$\"");
    }

    private void validate_request_error(ClientSubscriptionRequest request, String expectedError) {
        Set<ConstraintViolation<ClientSubscriptionRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.iterator().next().getMessage()).isEqualTo(expectedError);
    }
}
