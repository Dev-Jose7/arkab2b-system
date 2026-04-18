# ArkaB2B Platform Config Repository (Local Backend)

## Naming convention
- `<application>.yml`: cross-environment base config for one service.
- `<application>-<profile>.yml`: profile-specific overrides.
- `application.yml`: shared defaults for all services.
- `application-<profile>.yml`: shared profile overrides.

## Active profile flow
For local integrated runtime, services use `SPRING_PROFILES_ACTIVE=local` and consume:
1. `application.yml`
2. `application-local.yml`
3. `<service>.yml`
4. `<service>-local.yml`

## Secrets policy
Only non-sensitive defaults and placeholders are versioned here.
Sensitive values must be injected at runtime by env vars/secret managers in higher environments.

### Identity JWT keys
- `identity-access-service.yml` requires explicit JWT key paths.
- `identity-access-service-local.yml` provides local fallback paths under `platform/secrets/local`.
- Generate local keys with `scripts/generate-local-jwt-keys.sh`.

## Integrated stack baseline (Phase 4)
- Local integrated startup is orchestrated by:
  - `scripts/start-integrated-local.sh`
  - `scripts/stop-integrated-local.sh`
  - `scripts/smoke-integrated-local.sh`
- Default local stack ports and startup/readiness policy are documented in:
  - `platform/stack/README.md`
