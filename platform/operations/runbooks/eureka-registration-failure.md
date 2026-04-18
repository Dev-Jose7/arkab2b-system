# Runbook: Eureka not registering instances

## Symptom
Service-to-service discovery fails; eureka UI lacks expected service entries.

## Possible causes
- `eureka-server` down
- `APP_EUREKA_DEFAULT_ZONE` wrong
- client registration disabled by config

## First checks
- `curl -sS http://localhost:8761/actuator/health`
- `curl -sS http://localhost:8761/eureka/apps`
- inspect service logs for discovery client errors

## Confirm problem
Target service does not appear under `<applications>` in `/eureka/apps`.

## Mitigation / recovery
1. Restart `eureka-server`.
2. Verify `eureka.client.service-url.defaultZone` in config.
3. Ensure `register-with-eureka=true` for clients.
4. Restart non-registered services.

## Escalation
Escalate if registry is unstable (instances flapping) after reconnect.
