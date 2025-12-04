# 📘 Client Subscriptions API – README

The **Client Subscriptions API** enables external systems to register interest in specific justice-related events (e.g., *PCR* or *CUSTODIAL_RESULT*) via webhook callbacks.  
It supports full **CRUD operations** for:

- **Client Subscriptions** (top-level resource)  
- **Subscriptions** (nested child resource)

All endpoints require **JWT Bearer authentication**.

---

# 🔐 Authentication

All API requests must include a valid JWT:

```
Authorization: Bearer <JWT>
```

---

# 🚀 API Endpoints Overview

Below is a full list of endpoints grouped by resource type.

---

# 📦 Client Subscription Endpoints

## **Create Client Subscription**
### `POST /clientSubscriptions`

Creates a new client subscription.

**Request Body**
```json
{
  "eventType": "PCR",
  "webhookUrl": "https://example.com/webhook"
}
```

**Response 201**
```json
{
  "clientSubscriptionId": "uuid",
  "eventType": "PCR",
  "webhookUrl": "https://example.com/webhook",
  "createdAt": "2025-01-01T12:00:00Z",
  "subscriptions": []
}
```

---

## **Get All Client Subscriptions**
### `GET /clientSubscriptions`

Returns a list of all client subscriptions.

---

## **Get a Client Subscription (with nested subscriptions)**
### `GET /clientSubscriptions/{clientSubscriptionId}`

Returns:
- event type  
- webhook URL  
- created timestamp  
- all child subscriptions  

---

## **Update Client Subscription**
### `PUT /clientSubscriptions/{clientSubscriptionId}`

Updates callback endpoint or event type.

**Request Body**
```json
{
  "eventType": "CUSTODIAL_RESULT",
  "webhookUrl": "https://example.com/new-webhook"
}
```

---

## **Delete Client Subscription**
### `DELETE /clientSubscriptions/{clientSubscriptionId}`

Deletes the client subscription (and optionally all nested subscriptions, depending on implementation).

---

# 📑 Subscription Endpoints (Nested Resource)

All subscription endpoints belong to a **parent client subscription** identified by `{clientSubscriptionId}`.

---

## **Create Subscription**
### `POST /clientSubscriptions/{clientSubscriptionId}/subscriptions`

Creates a child subscription.

**Request Body**
```json
{
  "eventType": "PCR",
  "webhookUrl": "https://example.com/sub-hook"
}
```

---

## **Get All Subscriptions for a Client Subscription**
### `GET /clientSubscriptions/{clientSubscriptionId}/subscriptions`

Returns all subscriptions under the specified client subscription.

---

## **Get a Specific Subscription**
### `GET /clientSubscriptions/{clientSubscriptionId}/subscriptions/{subscriptionId}`

Returns details of a specific child subscription.

---

## **Update Subscription**
### `PUT /clientSubscriptions/{clientSubscriptionId}/subscriptions/{subscriptionId}`

Updates event type or webhook URL.

**Request Body**
```json
{
  "eventType": "CUSTODIAL_RESULT",
  "webhookUrl": "https://example.com/updated-sub-hook"
}
```

---

## **Delete Subscription**
### `DELETE /clientSubscriptions/{clientSubscriptionId}/subscriptions/{subscriptionId}`

Deletes one nested subscription.

---

# 📊 Summary Table

| Resource | Method | Endpoint | Description |
|---------|--------|----------|-------------|
| ClientSubscription | POST | `/clientSubscriptions` | Create client subscription |
| ClientSubscription | GET | `/clientSubscriptions` | List all client subscriptions |
| ClientSubscription | GET | `/clientSubscriptions/{clientSubscriptionId}` | Get one (with nested subscriptions) |
| ClientSubscription | PUT | `/clientSubscriptions/{clientSubscriptionId}` | Update |
| ClientSubscription | DELETE | `/clientSubscriptions/{clientSubscriptionId}` | Delete |
| Subscription | POST | `/clientSubscriptions/{clientSubscriptionId}/subscriptions` | Create child subscription |
| Subscription | GET | `/clientSubscriptions/{clientSubscriptionId}/subscriptions` | List all subscriptions under a parent |
| Subscription | GET | `/clientSubscriptions/{clientSubscriptionId}/subscriptions/{subscriptionId}` | Get subscription |
| Subscription | PUT | `/clientSubscriptions/{clientSubscriptionId}/subscriptions/{subscriptionId}` | Update |
| Subscription | DELETE | `/clientSubscriptions/{clientSubscriptionId}/subscriptions/{subscriptionId}` | Delete |

---

# 📄 OpenAPI Specification

The complete OpenAPI 3.1.0 spec is available under:

```
/openapi.yaml
```

