# ArkaB2B Observability Baseline (Phase 5)

## Logging baseline

- Console log pattern includes `traceId` and `correlationId` placeholders.
- Inbound HTTP filter propagates and normalizes:
  - `X-Trace-Id`
  - `X-Correlation-Id`
- Outbound `WebClient` calls propagate the same headers.
- Error logs for outbound HTTP calls include method, target and trace context.

## Tracing baseline

- Micrometer tracing bridge (`brave`) enabled across services.
- Default sampling: `management.tracing.sampling.probability=1.0`.
- Sync trace continuity is supported through propagated headers.
- Async events keep trace/correlation metadata in payload where event contracts already include them.

## Metrics baseline

- Actuator metrics + Prometheus endpoint (`/actuator/prometheus`) enabled.
- HTTP metrics:
  - `http.server.requests`
  - `http.client.requests`
  - `arka.http.client.requests` (custom downstream observation)
- JVM/Process metrics: standard Micrometer JVM + process set.
- Async operational counters:
  - `arka.notification.kafka.order_events`
  - `arka.reporting.kafka.upstream_events`

## Suggested minimum dashboards

1. **Edge and control plane**
- gateway request rate, 4xx/5xx, latency p95
- config-server/eureka health and availability

2. **Core services**
- per-service HTTP error rate and latency
- downstream call errors by target (`arka.http.client.requests{status}`)

3. **Async services**
- notification/reporting consumer processed vs failed counters
- outbox backlog and publish failures (from service logs + DB checks)

## Suggested minimum alerts

- gateway 5xx rate above threshold
- any core service readiness `DOWN` > N minutes
- repeated S2S auth failures (401/403 internal)
- Kafka consumer failed outcomes growing continuously
- outbox pending backlog growth sustained for N intervals
