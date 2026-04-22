# Runbook: Internal JWT propagation or actor context failure

## Symptom
Internal HTTP calls fail with `401/403`, or async processors/schedulers reject business operations due to missing actor or organization context.

## Possible causes
- caller did not propagate the authenticated bearer JWT
- receiver rejected JWT due to issuer/audience/JWKS mismatch
- token is valid but missing required organization or country claims
- async consumer/scheduler built an incomplete internal actor context

## First checks
- inspect caller logs for outbound `401/403`
- inspect receiver logs for JWT validation or authorization errors
- verify `app.security.jwt.*` issuer/audience/JWKS settings
- verify the triggering JWT contains `organizationId` / `countryCode` when the flow requires them

## Confirm problem
A valid internal flow consistently fails with `401/403` while target service is reachable and business data exists.

## Mitigation / recovery
1. Confirm the original user JWT is present at the entrypoint and reaches the caller service.
2. Confirm the caller `WebClient` propagated the bearer token instead of sending an anonymous request.
3. Validate receiver JWKS, issuer and audience configuration.
4. For async/scheduler paths, verify the service built an internal authenticated actor context with the required organization data.
5. Restart the affected service only after config drift or key mismatch is corrected.

## Escalation
Escalate if JWT validation fails system-wide or multiple services lose actor context simultaneously.
