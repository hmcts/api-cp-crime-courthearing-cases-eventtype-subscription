# PUT Partial Update — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow `PUT /client-subscriptions/{id}` to accept either `notificationEndpoint` or `eventTypes` (or both) instead of requiring both fields.

**Architecture:** Introduce a new `ClientSubscriptionUpdateRequest` schema in the OpenAPI spec. The PUT operation references this schema; POST keeps `ClientSubscriptionRequest` (both fields required). The `anyOf` constraint in the new schema enforces at least one field at spec-tooling level. Jakarta Bean Validation enforces field-level rules (HTTPS pattern, list size) when individual fields are present.

**Tech Stack:** OpenAPI 3.1, OpenAPI Generator 7.21.0, Spring (generated interfaces), Jakarta Bean Validation, JUnit 5, AssertJ, Lombok

---

### Task 1: Update openapi-spec.yml

**Files:**
- Modify: `src/main/resources/openapi/openapi-spec.yml`

- [ ] **Step 1: Add `ClientSubscriptionUpdateRequest` schema**

In `components/schemas`, after the `ClientSubscriptionRequest` block (around line 603), add:

```yaml
    ClientSubscriptionUpdateRequest:
      type: object
      properties:
        notificationEndpoint:
          $ref: '#/components/schemas/NotificationEndpoint'
        eventTypes:
          type: array
          minItems: 1
          maxItems: 1
          items:
            $ref: '#/components/schemas/EventType'
      anyOf:
        - required: [notificationEndpoint]
        - required: [eventTypes]
```

- [ ] **Step 2: Update PUT operation summary and description**

Find the `put:` block at `/client-subscriptions/{clientSubscriptionId}` (around line 214). Change:

```yaml
      summary: Strict update of a client subscription
      description: Caller must supply *all* updatable fields.
```

to:

```yaml
      summary: Partial update of a client subscription
      description: Updates one or more fields of an existing subscription. Omitted fields are left unchanged.
```

- [ ] **Step 3: Update PUT operation schema ref**

In the same PUT block, under `requestBody.content.application/json`, change:

```yaml
            schema:
              $ref: '#/components/schemas/ClientSubscriptionRequest'
```

to:

```yaml
            schema:
              $ref: '#/components/schemas/ClientSubscriptionUpdateRequest'
```

- [ ] **Step 4: Update `updateSubscription` example**

Find the `updateSubscription` example under `components/examples` (around line 845). Change:

```yaml
    updateSubscription:
      summary: Example update request
      value:
        notificationEndpoint:
          callbackUrl: "https://client.example.com/new-hook"
        eventTypes: [ "PRISON_COURT_REGISTER_GENERATED" ]
```

to:

```yaml
    updateSubscription:
      summary: Example partial update request (endpoint only)
      value:
        notificationEndpoint:
          callbackUrl: "https://client.example.com/new-hook"
```

- [ ] **Step 5: Verify spec compiles by running a build (tests skipped)**

```bash
./gradlew build -x test -DAPI_SPEC_VERSION=local
```

Expected: `BUILD SUCCESSFUL`. If it fails, check the YAML indentation around the new schema block.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/openapi/openapi-spec.yml
git commit -m "feat: add ClientSubscriptionUpdateRequest schema for partial PUT updates"
```

---

### Task 2: Add OpenAPISpecTest tests for the new schema and updated method signature

**Files:**
- Modify: `src/test/java/uk/gov/hmcts/cp/subscription/OpenAPISpecTest.java`

- [ ] **Step 1: Add import for `ClientSubscriptionUpdateRequest`**

At the top of `OpenAPISpecTest.java`, add this import alongside the existing model imports:

```java
import uk.gov.hmcts.cp.openapi.model.ClientSubscriptionUpdateRequest;
```

- [ ] **Step 2: Write two failing tests**

Add these two methods to `OpenAPISpecTest` (after the existing `client_subscription_request_fields_should_have_correct_types` test):

```java
@Test
void subscription_update_request_should_have_expected_fields() {
    assertThat(ClientSubscriptionUpdateRequest.class).hasDeclaredFields("notificationEndpoint");
    assertThat(ClientSubscriptionUpdateRequest.class).hasDeclaredFields("eventTypes");
}

@Test
void client_subscription_update_request_fields_should_have_correct_types() throws NoSuchFieldException {
    Field notificationEndpointField = ClientSubscriptionUpdateRequest.class.getDeclaredField("notificationEndpoint");
    Field eventTypesField = ClientSubscriptionUpdateRequest.class.getDeclaredField("eventTypes");

    assertThat(notificationEndpointField.getType()).isEqualTo(NotificationEndpoint.class);
    assertThat(eventTypesField.getType()).isAssignableFrom(List.class);
}
```

- [ ] **Step 3: Write a failing test verifying the updated method signature**

Add this method to `OpenAPISpecTest` (after the existing `subscription_api_should_have_expected_methods` test):

```java
@Test
void update_subscription_method_should_accept_client_subscription_update_request() throws NoSuchMethodException {
    Method method = SubscriptionApi.class.getMethod(
            "updateClientSubscription", UUID.class, ClientSubscriptionUpdateRequest.class, UUID.class);
    assertThat(method).isNotNull();
}
```

- [ ] **Step 4: Run the new tests to confirm they pass**

```bash
./gradlew test --tests "uk.gov.hmcts.cp.subscription.OpenAPISpecTest.subscription_update_request_should_have_expected_fields" --tests "uk.gov.hmcts.cp.subscription.OpenAPISpecTest.client_subscription_update_request_fields_should_have_correct_types" --tests "uk.gov.hmcts.cp.subscription.OpenAPISpecTest.update_subscription_method_should_accept_client_subscription_update_request" -DAPI_SPEC_VERSION=local
```

Expected: all 3 PASS. If any fail, the most likely cause is a YAML indentation error in the spec — re-check the `ClientSubscriptionUpdateRequest` block.

- [ ] **Step 5: Run the full test suite to ensure nothing regressed**

```bash
./gradlew check -DAPI_SPEC_VERSION=local
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add src/test/java/uk/gov/hmcts/cp/subscription/OpenAPISpecTest.java
git commit -m "test: verify ClientSubscriptionUpdateRequest schema and updated PUT method signature"
```

---

### Task 3: Add ValidateClientSubscriptionUpdateRequestTest

**Files:**
- Create: `src/test/java/uk/gov/hmcts/cp/subscription/ValidateClientSubscriptionUpdateRequestTest.java`

Note: The `anyOf` constraint (at least one field required) is enforced at the spec-tooling level (e.g. Spectral linting), **not** by Jakarta Bean Validation. Jakarta only enforces per-field annotations (`@Pattern`, `@Size`). Tests below reflect this — "neither field provided" produces 0 Bean Validation violations.

- [ ] **Step 1: Write the test class with all cases**

Create `src/test/java/uk/gov/hmcts/cp/subscription/ValidateClientSubscriptionUpdateRequestTest.java`:

```java
package uk.gov.hmcts.cp.subscription;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.model.ClientSubscriptionUpdateRequest;
import uk.gov.hmcts.cp.openapi.model.NotificationEndpoint;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ValidateClientSubscriptionUpdateRequestTest {

    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void valid_with_notification_endpoint_only() {
        ClientSubscriptionUpdateRequest request = ClientSubscriptionUpdateRequest.builder()
                .notificationEndpoint(NotificationEndpoint.builder().callbackUrl("https://good-url").build())
                .build();
        Set<ConstraintViolation<ClientSubscriptionUpdateRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void valid_with_event_types_only() {
        ClientSubscriptionUpdateRequest request = ClientSubscriptionUpdateRequest.builder()
                .eventTypes(List.of("PRISON_COURT_REGISTER_GENERATED"))
                .build();
        Set<ConstraintViolation<ClientSubscriptionUpdateRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void valid_with_both_fields() {
        ClientSubscriptionUpdateRequest request = ClientSubscriptionUpdateRequest.builder()
                .notificationEndpoint(NotificationEndpoint.builder().callbackUrl("https://good-url").build())
                .eventTypes(List.of("PRISON_COURT_REGISTER_GENERATED"))
                .build();
        Set<ConstraintViolation<ClientSubscriptionUpdateRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(0);
    }

    @Test
    void invalid_with_bad_url() {
        ClientSubscriptionUpdateRequest request = ClientSubscriptionUpdateRequest.builder()
                .notificationEndpoint(NotificationEndpoint.builder().callbackUrl("bad-url").build())
                .build();
        Set<ConstraintViolation<ClientSubscriptionUpdateRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.iterator().next().getMessage()).isEqualTo("must match \"^https://.*$\"");
    }

    @Test
    void invalid_with_none_https_url() {
        ClientSubscriptionUpdateRequest request = ClientSubscriptionUpdateRequest.builder()
                .notificationEndpoint(NotificationEndpoint.builder().callbackUrl("http://bad-url").build())
                .build();
        Set<ConstraintViolation<ClientSubscriptionUpdateRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.iterator().next().getMessage()).isEqualTo("must match \"^https://.*$\"");
    }

    @Test
    void invalid_with_empty_event_types_list() {
        ClientSubscriptionUpdateRequest request = ClientSubscriptionUpdateRequest.builder()
                .eventTypes(List.of())
                .build();
        Set<ConstraintViolation<ClientSubscriptionUpdateRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.iterator().next().getMessage()).isEqualTo("size must be between 1 and 1");
    }

    @Test
    void invalid_with_too_many_event_types() {
        ClientSubscriptionUpdateRequest request = ClientSubscriptionUpdateRequest.builder()
                .eventTypes(List.of("PRISON_COURT_REGISTER_GENERATED", "PRISON_COURT_REGISTER_GENERATED"))
                .build();
        Set<ConstraintViolation<ClientSubscriptionUpdateRequest>> errors = validator.validate(request);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.iterator().next().getMessage()).isEqualTo("size must be between 1 and 1");
    }
}
```

- [ ] **Step 2: Run the new tests**

```bash
./gradlew test --tests "uk.gov.hmcts.cp.subscription.ValidateClientSubscriptionUpdateRequestTest" -DAPI_SPEC_VERSION=local
```

Expected: all 7 PASS. If `valid_with_notification_endpoint_only` or `valid_with_event_types_only` fail with a violation, check whether the generated model picked up an unexpected `@NotNull` — re-verify the spec has no `required` array on `ClientSubscriptionUpdateRequest`.

- [ ] **Step 3: Run the full test suite**

```bash
./gradlew check -DAPI_SPEC_VERSION=local
```

Expected: `BUILD SUCCESSFUL` with coverage report generated.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/uk/gov/hmcts/cp/subscription/ValidateClientSubscriptionUpdateRequestTest.java
git commit -m "test: validate ClientSubscriptionUpdateRequest field-level constraints"
```