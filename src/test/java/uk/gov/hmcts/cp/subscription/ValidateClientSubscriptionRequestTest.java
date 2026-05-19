package uk.gov.hmcts.cp.subscription;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.model.ClientSubscriptionRequest;
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
                .eventTypes(List.of("PRISON_COURT_REGISTER_GENERATED"))
                .notificationEndpoint(NotificationEndpoint.builder().callbackUrl("https://good-url").build())
                .build();
        Set<ConstraintViolation<ClientSubscriptionRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void validate_request_should_reject_empty_event_type() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of())
                .notificationEndpoint(NotificationEndpoint.builder().callbackUrl("https://good-url").build())
                .build();
        validate_request_error(request, "size must be between 1 and 2147483647");
    }

    @Test
    void validate_request_should_accept_multiple_event_types() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of("PRISON_COURT_REGISTER_GENERATED", "WEE_Layout5"))
                .notificationEndpoint(NotificationEndpoint.builder().callbackUrl("https://good-url").build())
                .build();
        Set<ConstraintViolation<ClientSubscriptionRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void validate_request_should_reject_missing_url() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of("PRISON_COURT_REGISTER_GENERATED"))
                .notificationEndpoint(NotificationEndpoint.builder().build())
                .build();
        validate_request_error(request, "must not be null");
    }

    @Test
    void validate_request_should_accept_http_url() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of("PRISON_COURT_REGISTER_GENERATED"))
                .notificationEndpoint(NotificationEndpoint.builder().callbackUrl("http://callback-url").build())
                .build();
        Set<ConstraintViolation<ClientSubscriptionRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void validate_request_should_reject_bad_url() {
        ClientSubscriptionRequest request = ClientSubscriptionRequest.builder()
                .eventTypes(List.of("PRISON_COURT_REGISTER_GENERATED"))
                .notificationEndpoint(NotificationEndpoint.builder().callbackUrl("bad-url").build())
                .build();
        validate_request_error(request, "must match \"^https?://.*$\"");
    }

    private void validate_request_error(ClientSubscriptionRequest request, String expectedError) {
        Set<ConstraintViolation<ClientSubscriptionRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.iterator().next().getMessage()).isEqualTo(expectedError);
    }
}
