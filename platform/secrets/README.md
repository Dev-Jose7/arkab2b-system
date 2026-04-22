# ArkaB2B Secrets Strategy (Phase 5 Baseline)

## Goal
Keep runtime-sensitive material outside source-controlled defaults while preserving local developer ergonomics.

## Environment strategy

- `local`:
  - secrets are loaded from `platform/secrets/local` (developer-generated, ignored by VCS)
  - intended only for local integrated stack and tests/manual smoke
- `integration`:
  - secrets must be injected through environment variables or CI/CD secret store
- `production`:
  - secrets must be injected by managed secret systems (no plaintext in repo)

## JWT keys for identity-access-service

`identity-access-service` no longer uses an embedded private key from `src/main/resources` as runtime baseline.

Expected local files:

- `platform/secrets/local/identity-access-private.pem`
- `platform/secrets/local/identity-access-public.pem`

Generate them with:

```bash
./scripts/generate-local-jwt-keys.sh
```

## Runtime variables

- `APP_SECURITY_JWT_PRIVATE_KEY_PATH`
- `APP_SECURITY_JWT_PUBLIC_KEY_PATH`
- DB/Kafka/Redis credentials in target environment

## Guardrails

- Do not commit real secrets.
- Do not use development keypair as production fallback.
- Keep per-environment secret injection explicit.
