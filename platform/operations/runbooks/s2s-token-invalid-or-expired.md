# Runbook: S2S token invalid or expired

## Symptom
Internal HTTP calls fail with `401/403`, especially between core services.

## Possible causes
- wrong S2S secret/client-id
- token expired and not refreshed
- issuer/audience/scope mismatch

## First checks
- inspect caller logs for `service-to-service credentials are not configured`
- inspect receiver logs for JWT validation errors
- verify `app.security.s2s.*` and JWT audience/issuer config

## Confirm problem
A valid internal call path consistently fails with `401/403` while service is reachable.

## Mitigation / recovery
1. Ensure caller has correct `client-id/client-secret`.
2. Ensure identity-access has matching S2S client registration.
3. Check audience and required scopes on receiver.
4. Restart caller if stale cached token persists.

## Escalation
Escalate if issuer/JWKS validation fails system-wide (possible key rotation/config drift incident).
