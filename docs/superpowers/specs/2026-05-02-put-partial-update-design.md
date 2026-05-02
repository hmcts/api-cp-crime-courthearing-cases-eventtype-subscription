# Design: Partial Update for PUT /client-subscriptions/{id}

**Date:** 2026-05-02
**Status:** Approved

## Problem

The `PUT /client-subscriptions/{clientSubscriptionId}` endpoint currently uses `ClientSubscriptionRequest`, which marks both `notificationEndpoint` and `eventTypes` as required. Callers must supply both fields even when they only want to update one. The desired behaviour is a partial update: supply one or both fields; omitted fields are left unchanged on the server.

## Decision

Introduce a new `ClientSubscriptionUpdateRequest` schema used exclusively by the PUT operation. The POST (create) operation continues to use the existing `ClientSubscriptionRequest` (both fields required).

## Schema: `ClientSubscriptionUpdateRequest`

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

- No top-level `required` array — both fields are individually optional.
- `anyOf` enforces at least one field must be present (spec-level validation).
- Field-level constraints (`minItems`, `maxItems`, HTTPS pattern) are inherited unchanged from the shared sub-schemas.

## PUT Operation Changes

- **Schema ref:** `ClientSubscriptionRequest` → `ClientSubscriptionUpdateRequest`
- **Summary:** `"Strict update of a client subscription"` → `"Partial update of a client subscription"`
- **Description:** `"Caller must supply *all* updatable fields."` → `"Updates one or more fields of an existing subscription. Omitted fields are left unchanged."`
- **`updateSubscription` example:** updated to show a single-field request (notificationEndpoint only) to illustrate partial update.

## Test Changes

### `OpenAPISpecTest`
Add a test verifying `ClientSubscriptionUpdateRequest` has both fields declared with correct types (mirrors the existing `subscription_request_should_have_expected_fields` test).

### New `ValidateClientSubscriptionUpdateRequestTest`

| Test | Input | Expected |
|------|-------|----------|
| `valid_with_notification_endpoint_only` | `notificationEndpoint` set, `eventTypes` null | 0 violations |
| `valid_with_event_types_only` | `eventTypes` set, `notificationEndpoint` null | 0 violations |
| `valid_with_both_fields` | both set | 0 violations |
| `invalid_with_neither_field` | both null | validation error |
| `invalid_with_bad_url` | bad `callbackUrl`, no `eventTypes` | validation error |
| `invalid_with_empty_event_types_list` | empty `eventTypes`, no `notificationEndpoint` | validation error |

The existing `ValidateClientSubscriptionRequestTest` (covering POST) is untouched.

## Out of Scope

- HTTP method change to PATCH — the PUT endpoint retains its method.
- Server-side merge logic — handled by the implementing service, not this spec.