## Notifications

The PCR notification flow ensures that Prison Court Register (PCR) documents are reliably generated, published, and delivered to all subscribed downstream clients.

The Progression Service generates the PCR, stores the document, and publishes a PCR-generated event. After internal validation, it triggers the Results Subscription Service via http command.

The Results Subscription Service:
1.	Retrieves the PCR document securely using a time-limited SAS URL
1.	Resolves all registered subscribers
1.	Fans out delivery messages via Artemis
1.	Delivers notifications to client webhooks through API Management

The design is resilient and failure-aware:
1.	Material availability is checked before fan-out
1.	Each subscriber is handled independently
1.	Webhook failures are retried using exponential backoff
1.	Messages that exceed retry limits are sent to a Dead Letter Queue (DLQ)

This approach provides secure document access, isolated delivery per subscriber, and robust failure handling, while keeping services loosely coupled and scalable.

```mermaid
sequenceDiagram
    autonumber

    participant PS as Progression Service
    participant AMB as Artemis Message Broker
    participant SUB as Results Subscription Service
    participant MS as Material Service
    participant APIM as API Management
    participant RASS as RaSS Service (Client Webhook)
    participant DLQ as Dead Letter Queue

    %% ===== PCR GENERATION =====
    rect rgb(235,235,235)
        Note over PS: PCR generation & material creation
        PS->>PS: Generate PCR PDF
        PS->>PS: Generate materialId
        PS->>PS: Set pcrGeneratedTimestamp
        PS-->>MS: Upload PCR document (materialId, pdfPayload)
    end

    %% ===== EVENT PUBLICATION =====
    rect rgb(230,245,255)
        Note over PS,AMB: Event publication & internal processing
        PS->>AMB: Publish event\nprison-court-register-generated-v2
        AMB-->>PS: Deliver event
        PS->>PS: PrisonCourtRegisterEventProcessor\nprocess event (validate + enrich)
    end

    %% ===== COMMAND TO SUBSCRIPTION SERVICE =====
    rect rgb(245,245,220)
        Note over PS,SUB: Command subscription service
        PS->>SUB: POST /pcr-notifications\n(materialId, pcrGeneratedTimestamp, case metadata)
    end

    %% ===== MATERIAL RESOLUTION =====
    rect rgb(255,230,230)
        Note over SUB,MS: Material resolution (failure-first)
        SUB->>MS: GET /material/{materialId}?requestPdf=true

        alt Material NOT Found
            MS-->>SUB: 404 Not Found
            alt retryCount < maxMaterialRetries
                SUB->>SUB: Retry material lookup after delay
            else retryCount >= maxMaterialRetries
                SUB->>DLQ: Move message to DLQ\n(material not available)
            end
        end
    end

    %% ===== MATERIAL FOUND & FAN-OUT =====
    rect rgb(220,240,255)
        Note over SUB,AMB: Subscriber fan-out & message publication

        alt Material Found
            MS-->>SUB: 200 OK (SAS URL)

            SUB->>SUB: Fetch all subscribers for PCR
            SUB->>SUB: Build request per subscriber\n(JSON metadata + PDF link via APIM)
            SUB->>AMB: Publish delivery message\n(per subscriber)
        end
    end

    %% ===== WEBHOOK DELIVERY & RETRY =====
    rect rgb(235,255,235)
        Note over SUB,RASS: Webhook delivery, retry & DLQ handling

        AMB-->>SUB: Deliver subscriber message
        SUB->>APIM: Send webhook request
        APIM->>RASS: Forward request to RaSS webhook

        alt RaSS responds
            RASS-->>APIM: 200 OK
            APIM-->>SUB: 200 OK
        else RaSS does not respond
            rect rgb(255,235,200)
                Note over SUB: Exponential retry policy
                alt retryCount < maxWebhookRetries
                    SUB->>SUB: Exponential backoff retry
                    SUB->>APIM: Retry webhook request
                else retryCount >= maxWebhookRetries
                    SUB->>DLQ: Move message to DLQ\n(webhook failed after retries)
                end
            end
        end
    end
```