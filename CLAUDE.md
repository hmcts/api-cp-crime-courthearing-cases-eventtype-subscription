## Repo: api-cp-crime-hearing-results-document-subscription

OpenAPI-first API spec library that defines the subscription and notification contract for Crime Hearing Results document events, including webhook callbacks with HMAC-SHA256 signatures.

**Pattern**: Pure spec-only
**OpenAPI spec version**: 3.1.0
**OpenAPI Generator version**: 7.21.0 (target 7.22.0 per upgrade cycle)
**Spring Boot version**: 4.0.5 (target 4.0.6+ per upgrade cycle)

## API Endpoint(s)

```
GET  /event-types
  → 200 (list of event types)
  → 400 ErrorResponse
  → 401 Unauthorized
  → 403 Forbidden

POST /client-subscriptions
  → 201 (subscription created)
  → 400 ErrorResponse
  → 401 Unauthorized
  → 403 Forbidden
```

Callbacks: The spec defines an HMAC-SHA256-signed webhook payload delivered to subscriber-registered URLs on hearing result events.

## Generated Interfaces & Schema

- Schema files: schemas defined inline in `components/schemas` (no separate `.schema.json` files)
- Generated API interfaces:
  - `uk.gov.hmcts.cp.openapi.api.SubscriptionApi` — manage client subscriptions
  - `uk.gov.hmcts.cp.openapi.api.NotificationApi` — notification delivery endpoints
  - `uk.gov.hmcts.cp.openapi.api.InternalApi` — internal event processing
- Security: Bearer JWT + `Ocp-Apim-Subscription-Key` (header)

## Domain Models

| Model | Purpose |
|---|---|
| `ClientSubscriptionRequest` | Payload for registering a new webhook subscription |
| `EventType` | Classification of hearing result events |
| `ErrorResponse` | Machine-readable error with traceId |

## Test Structure

| Class | What it validates |
|---|---|
| `SubscriptionKeySecurityTest` | YAML spec parsing; verifies subscription key security scheme is present and correct |
| `OpenAPISpecTest` | OpenAPI model validation; verifies generated models conform to spec schemas |
| `ValidateClientSubscriptionRequestTest` | Jakarta Bean Validation constraints on `ClientSubscriptionRequest` |

## Generator Config Notes

- `@JsonInclude(NON_NULL)` is absent from `additionalModelTypeAnnotations` — add to align with standard.
- Spring Boot deps are declared `compileOnly` (not `implementation`) — the published JAR does not bundle Spring transitive dependencies; downstream services must supply them.
- `inputSpec` uses modern `.set()` syntax.

## CI/CD Deviations

- `auto-merge-dependabot.yml` — present in this repo only; auto-merges Dependabot PRs on minor/patch version bumps.
- Standard set otherwise: `ci-draft.yml`, `ci-released.yml`, `lint-openapi.yml`, `code-analysis.yml`, `codeql.yml`, `secrets-scanner.yml`, `publish-openapi-spec.yml`.

## Repo-Specific Notes

- **openspec workflow active**: `openspec/` directory is present — spec changes follow the openspec change-management workflow before being committed to main.
- **HMAC-SHA256 callbacks**: The spec includes callback definitions for signed webhook delivery. Subscribers receive an `X-Hmac-Signature` header; validate with the shared secret stored in Key Vault.
- **docs/ present**: Contains additional integration and callback documentation.
- Run `/openapi-spec-reviewer` when authoring or reviewing the OpenAPI spec.
