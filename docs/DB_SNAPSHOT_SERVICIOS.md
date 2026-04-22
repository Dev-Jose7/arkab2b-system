# Snapshot de tablas con datos (Postgres ArkaB2B)

Generado: 2026-04-22 09:51:40 -05

## Resumen ejecutivo

| Base de datos | Tablas con datos | Filas totales |
|---|---:|---:|
| `identity_access` | 11 | 94 |
| `directory` | 4 | 9 |
| `arkab2b_catalog` | 5 | 16 |
| `inventory` | 3 | 6 |
| `order` | 9 | 36 |
| `arkab2b_notification` | 7 | 112 |
| `arkab2b_reporting` | 7 | 71 |

## identity_access

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.auth_audit` | 4 |
| 2 | `public.credential` | 2 |
| 3 | `public.credential_password` | 2 |
| 4 | `public.outbox_event` | 6 |
| 5 | `public.role` | 6 |
| 6 | `public.role_assignment_policy` | 12 |
| 7 | `public.role_permission` | 52 |
| 8 | `public.user_account` | 3 |
| 9 | `public.user_login_attempt` | 2 |
| 10 | `public.user_role_assignment` | 3 |
| 11 | `public.user_session` | 2 |

### Muestras por tabla

<details>
<summary><strong>`public.auth_audit`</strong> — 4 filas</summary>

```json
{
  "audit_id": "d51a8175-d68c-4724-9dc1-ca2cc5821cf9",
  "event_type": "USER_REGISTERED",
  "user_id": "38169310-1b0b-4e0e-b999-2d5ab1b1f94f",
  "session_id": null,
  "ip_address": null,
  "device_id": null,
  "result": "SUCCESS",
  "payload": {
    "email": "founder.audit.1776668417@arka.test"
  },
  "created_at": "2026-04-20T07:00:21.064079"
}
```

</details>

<details>
<summary><strong>`public.credential`</strong> — 2 filas</summary>

```json
{
  "credential_id": "e717900d-6878-44e0-8bc5-35fd698c48b5",
  "user_id": "38169310-1b0b-4e0e-b999-2d5ab1b1f94f",
  "credential_type": "PASSWORD",
  "credential_purpose": "PRIMARY",
  "provider": "LOCAL",
  "status": "ACTIVE",
  "last_used_at": null,
  "created_at": "2026-04-20T07:00:20.944681",
  "updated_at": "2026-04-20T07:00:20.944681"
}
```

</details>

<details>
<summary><strong>`public.credential_password`</strong> — 2 filas</summary>

```json
{
  "credential_id": "e717900d-6878-44e0-8bc5-35fd698c48b5",
  "password_hash": "$2a$10$5He1BArHAnYRzWLVcQLLdOlkdCy1D.4P9BP6Uh9j1/bhTvXGOpuDm",
  "hash_algorithm": "BCRYPT",
  "password_changed_at": "2026-04-20T07:00:20.944681",
  "created_at": "2026-04-20T07:00:20.944681",
  "updated_at": "2026-04-20T07:00:20.944681"
}
```

</details>

<details>
<summary><strong>`public.outbox_event`</strong> — 6 filas</summary>

```json
{
  "event_id": "b62ff09a-b975-4edd-ac68-52a629a7a71f",
  "aggregate_type": "Account",
  "aggregate_id": "38169310-1b0b-4e0e-b999-2d5ab1b1f94f",
  "event_type": "AccountRegistered",
  "payload": {
    "email": {
      "value": "founder.audit.1776668417@arka.test"
    },
    "eventId": "b62ff09a-b975-4edd-ac68-52a629a7a71f",
    "accountId": {
      "value": "38169310-1b0b-4e0e-b999-2d5ab1b1f94f"
    },
    "occurredAt": "2026-04-20T07:00:20.866442750Z"
  },
  "status": "PUBLISHED",
  "occurred_at": "2026-04-20T07:00:20.866443",
  "published_at": "2026-04-20T07:00:23.737982",
  "retry_count": 0,
  "last_error": null,
  "created_at": "2026-04-20T07:00:21.080178",
  "updated_at": "2026-04-20T07:00:23.737982"
}
```

</details>

<details>
<summary><strong>`public.role`</strong> — 6 filas</summary>

```json
{
  "role_id": "9de6fc0e-13d2-42a1-9232-4df6cdbf6f31",
  "role_code": "ORG_OWNER",
  "description": "Organization owner role",
  "created_at": "2026-04-20T06:54:40.883281",
  "updated_at": "2026-04-20T06:54:40.883281"
}
```

</details>

<details>
<summary><strong>`public.role_assignment_policy`</strong> — 12 filas</summary>

```json
{
  "assigner_role_id": "3f4b2c76-8be7-4ea2-b620-4a8c6d29d30f",
  "assignable_role_id": "9de6fc0e-13d2-42a1-9232-4df6cdbf6f31",
  "created_at": "2026-04-20T06:54:40.900027"
}
```

</details>

<details>
<summary><strong>`public.role_permission`</strong> — 52 filas</summary>

```json
{
  "role_id": "9de6fc0e-13d2-42a1-9232-4df6cdbf6f31",
  "permission_code": "iam.user.create",
  "resource": "iam.user",
  "action": "create",
  "scope": "ORGANIZATION",
  "created_at": "2026-04-20T06:54:40.888291"
}
```

</details>

<details>
<summary><strong>`public.user_account`</strong> — 3 filas</summary>

```json
{
  "user_id": "38169310-1b0b-4e0e-b999-2d5ab1b1f94f",
  "email": "founder.audit.1776668417@arka.test",
  "status": "ACTIVE",
  "failed_login_count": 0,
  "created_at": "2026-04-20T07:00:20.944681",
  "updated_at": "2026-04-20T07:00:21.573845"
}
```

</details>

<details>
<summary><strong>`public.user_login_attempt`</strong> — 2 filas</summary>

```json
{
  "attempt_id": "b020a0fc-7565-4ff1-bc99-2d9e14f717f0",
  "user_id": "38169310-1b0b-4e0e-b999-2d5ab1b1f94f",
  "ip_address": "172.20.0.14",
  "success": true,
  "attempt_at": "2026-04-20T07:00:21.573845",
  "created_at": "2026-04-20T07:00:21.573845"
}
```

</details>

<details>
<summary><strong>`public.user_role_assignment`</strong> — 3 filas</summary>

```json
{
  "assignment_id": "0701b397-102e-49a5-8bc6-0c94c8d9034b",
  "user_id": "38169310-1b0b-4e0e-b999-2d5ab1b1f94f",
  "role_id": "9de6fc0e-13d2-42a1-9232-4df6cdbf6f31",
  "status": "ACTIVE",
  "assigned_by": "SYSTEM_ONBOARDING",
  "assigned_at": "2026-04-20T07:00:20.944681",
  "created_at": "2026-04-20T07:00:20.944681",
  "updated_at": "2026-04-20T07:00:20.944681"
}
```

</details>

<details>
<summary><strong>`public.user_session`</strong> — 2 filas</summary>

```json
{
  "session_id": "223c7d74-447b-4453-aa61-eeb34c5bff33",
  "user_id": "38169310-1b0b-4e0e-b999-2d5ab1b1f94f",
  "device_id": "2774de87-d693-3612-b491-d6da71625c41",
  "device_name": "curl/8.7.1",
  "device_type": "desktop",
  "ip_address": "172.20.0.14",
  "access_jti": "073ed52f-7b48-4226-9423-99f012a88098",
  "refresh_jti": "0c369c9c-eb3d-4021-9e5f-9e3fdc9b55a9",
  "issued_at": "2026-04-20T07:00:21.573845",
  "access_token_expires_at": "2026-04-20T07:15:21.573845",
  "refresh_token_expires_at": "2026-04-27T07:00:21.573845",
  "last_seen_at": "2026-04-20T07:00:21.681584",
  "status": "ACTIVE",
  "revoked_at": null,
  "revocation_reason": null,
  "created_at": "2026-04-20T07:00:21.681584",
  "updated_at": "2026-04-20T07:00:21.681584"
}
```

</details>


## directory

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.address` | 2 |
| 2 | `public.organization` | 2 |
| 3 | `public.organization_contact` | 3 |
| 4 | `public.organization_country_policy` | 2 |

### Muestras por tabla

<details>
<summary><strong>`public.address`</strong> — 2 filas</summary>

```json
{
  "address_id": "addr-organization-demo-hq",
  "organization_id": "organization-demo",
  "address_type": "SHIPPING",
  "alias": "Main HQ",
  "line1": "Cra 7 # 32-16",
  "line2": null,
  "city": "Bogota",
  "state_region": "Cundinamarca",
  "postal_code": "110311",
  "country_code": "CO",
  "reference": "Seeded default address",
  "latitude": null,
  "longitude": null,
  "is_default": true,
  "status": "ACTIVE",
  "validation_status": "VERIFIED",
  "validated_at": "2026-04-20T06:55:13.796574",
  "created_at": "2026-04-20T06:55:13.796574",
  "updated_at": "2026-04-22T13:44:41.841111"
}
```

</details>

<details>
<summary><strong>`public.organization`</strong> — 2 filas</summary>

```json
{
  "organization_id": "organization-demo",
  "legal_name": "Organization Demo Organization",
  "trade_name": "Organization Demo",
  "country_code": "CO",
  "currency_code": "COP",
  "timezone": "America/Bogota",
  "segment_tier": "FOUNDATION",
  "status": "ACTIVE",
  "created_at": "2026-04-20T06:55:13.795356",
  "updated_at": "2026-04-22T13:44:41.833489"
}
```

</details>

<details>
<summary><strong>`public.organization_contact`</strong> — 3 filas</summary>

```json
{
  "contact_id": "contact-organization-demo-email",
  "organization_id": "organization-demo",
  "contact_type": "EMAIL",
  "label": "Operations",
  "value_normalized": "ops+organization-demo@arka.test",
  "value_masked": "o***@arka.test",
  "is_primary": true,
  "status": "ACTIVE",
  "created_at": "2026-04-20T06:55:13.802987",
  "updated_at": "2026-04-22T13:44:41.855213"
}
```

</details>

<details>
<summary><strong>`public.organization_country_policy`</strong> — 2 filas</summary>

```json
{
  "policy_id": "policy-organization-demo-co-v1",
  "organization_id": "organization-demo",
  "country_code": "CO",
  "policy_version": 1,
  "currency_code": "COP",
  "week_starts_on": "MONDAY",
  "weekly_cutoff_local_time": "18:00:00",
  "timezone": "America/Bogota",
  "reporting_retention_days": 90,
  "requires_verified_address": true,
  "effective_from": "2026-04-19T06:55:13.800115",
  "effective_to": null,
  "status": "ACTIVE",
  "created_at": "2026-04-20T06:55:13.800115",
  "updated_at": "2026-04-22T13:44:41.850558"
}
```

</details>


## arkab2b_catalog

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.brands` | 5 |
| 2 | `public.categories` | 5 |
| 3 | `public.prices` | 2 |
| 4 | `public.products` | 2 |
| 5 | `public.variants` | 2 |

### Muestras por tabla

<details>
<summary><strong>`public.brands`</strong> — 5 filas</summary>

```json
{
  "brand_id": "brand-acme",
  "organization_id": "organization-demo",
  "brand_code": "ACME",
  "brand_name": "ACME",
  "status": "ACTIVE",
  "created_at": "2026-04-20T06:55:44.36956+00:00",
  "updated_at": "2026-04-20T06:55:44.36956+00:00"
}
```

</details>

<details>
<summary><strong>`public.categories`</strong> — 5 filas</summary>

```json
{
  "category_id": "category-beverages",
  "organization_id": "organization-demo",
  "category_code": "BEVERAGES",
  "category_name": "Beverages",
  "status": "ACTIVE",
  "created_at": "2026-04-20T06:55:44.37314+00:00",
  "updated_at": "2026-04-20T06:55:44.37314+00:00"
}
```

</details>

<details>
<summary><strong>`public.prices`</strong> — 2 filas</summary>

```json
{
  "price_id": "price-demo-coffee-base",
  "organization_id": "organization-demo",
  "variant_id": "variant-demo-coffee-500",
  "price_type": "BASE",
  "currency": "COP",
  "amount": 18500.0000,
  "effective_from": "2026-04-19T06:55:44.380057+00:00",
  "effective_until": null,
  "status": "ACTIVE",
  "created_at": "2026-04-20T06:55:44.380057+00:00",
  "updated_at": "2026-04-20T06:55:44.380057+00:00"
}
```

</details>

<details>
<summary><strong>`public.products`</strong> — 2 filas</summary>

```json
{
  "product_id": "product-demo-coffee-500",
  "organization_id": "organization-demo",
  "product_code": "COFFEE-500",
  "product_name": "Cafe Molido 500g",
  "description": "Cafe molido para baseline integrada",
  "brand_id": "brand-acme",
  "category_id": "category-beverages",
  "status": "ACTIVE",
  "created_at": "2026-04-20T06:55:44.375165+00:00",
  "updated_at": "2026-04-20T06:55:44.375165+00:00"
}
```

</details>

<details>
<summary><strong>`public.variants`</strong> — 2 filas</summary>

```json
{
  "variant_id": "variant-demo-coffee-500",
  "organization_id": "organization-demo",
  "product_id": "product-demo-coffee-500",
  "sku": "SKU-DEMO-COFFEE-500",
  "variant_name": "Cafe Molido 500g",
  "description": "Variante vendible baseline demo",
  "status": "SELLABLE",
  "sellable_from": "2026-03-21T06:55:44.377765+00:00",
  "sellable_until": null,
  "weight_grams": 500,
  "created_at": "2026-04-20T06:55:44.377765+00:00",
  "updated_at": "2026-04-20T06:55:44.377765+00:00"
}
```

</details>


## inventory

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.stock_items` | 2 |
| 2 | `public.stock_reservations` | 2 |
| 3 | `public.warehouses` | 2 |

### Muestras por tabla

<details>
<summary><strong>`public.stock_items`</strong> — 2 filas</summary>

```json
{
  "stock_item_id": "stock-item-demo-coffee-500",
  "organization_id": "organization-demo",
  "warehouse_id": "warehouse-demo-main",
  "sku": "SKU-DEMO-COFFEE-500",
  "physical_qty": 100,
  "reserved_qty": 2,
  "reorder_point": 20,
  "safety_stock": 10,
  "status": "ACTIVE",
  "version": 0,
  "created_at": "2026-04-20T06:56:13.374961",
  "updated_at": "2026-04-22T13:45:43.477473"
}
```

</details>

<details>
<summary><strong>`public.stock_reservations`</strong> — 2 filas</summary>

```json
{
  "reservation_id": "reservation-demo-checkout-1",
  "organization_id": "organization-demo",
  "stock_item_id": "stock-item-demo-coffee-500",
  "warehouse_id": "warehouse-demo-main",
  "sku": "SKU-DEMO-COFFEE-500",
  "cart_id": "cart-demo-seeded",
  "order_id": "order-demo-seeded",
  "qty": 2,
  "status": "CONFIRMED",
  "expires_at": "2026-04-29T13:45:43.479381",
  "confirmed_at": "2026-04-22T13:44:43.479381",
  "released_at": null,
  "created_at": "2026-04-20T06:56:13.381136",
  "updated_at": "2026-04-22T13:45:43.479381"
}
```

</details>

<details>
<summary><strong>`public.warehouses`</strong> — 2 filas</summary>

```json
{
  "warehouse_id": "warehouse-demo-main",
  "organization_id": "organization-demo",
  "warehouse_code": "MAIN",
  "warehouse_name": "Warehouse Demo Main",
  "country_code": "CO",
  "status": "ACTIVE",
  "created_at": "2026-04-20T06:56:13.37068",
  "updated_at": "2026-04-22T13:45:43.471415"
}
```

</details>


## order

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.cart_items` | 1 |
| 2 | `public.carts` | 4 |
| 3 | `public.checkout_attempts` | 1 |
| 4 | `public.idempotency_records` | 10 |
| 5 | `public.order_audits` | 10 |
| 6 | `public.order_lines` | 1 |
| 7 | `public.order_status_histories` | 1 |
| 8 | `public.outbox_events` | 7 |
| 9 | `public.purchase_orders` | 1 |

### Muestras por tabla

<details>
<summary><strong>`public.cart_items`</strong> — 1 filas</summary>

```json
{
  "cart_item_id": "f822301e-d682-4b74-9572-b70d55c54809",
  "cart_id": "66900a4c-2d62-4a0a-a094-e62e92b0c402",
  "organization_id": "organization-phase6",
  "variant_id": "variant-phase6-coffee-500",
  "sku": "SKU-PHASE6-COFFEE-500",
  "qty": 2,
  "unit_price": 18500.0000,
  "currency": "COP",
  "reservation_id": "reservation-phase6-checkout-1",
  "reservation_confirmed": true,
  "created_at": "2026-04-20T07:04:06.166729",
  "updated_at": "2026-04-20T07:04:06.166729"
}
```

</details>

<details>
<summary><strong>`public.carts`</strong> — 4 filas</summary>

```json
{
  "cart_id": "04ad1949-191f-4065-bfdb-85ebfb42b210",
  "organization_id": "organization-phase6",
  "user_id": "u1",
  "status": "ACTIVE",
  "version": 0,
  "created_at": "2026-04-20T07:04:04.493398",
  "updated_at": "2026-04-20T07:04:04.493398"
}
```

</details>

<details>
<summary><strong>`public.checkout_attempts`</strong> — 1 filas</summary>

```json
{
  "checkout_attempt_id": "0f0aa87f-5b76-44a9-97c8-52f91817ae53",
  "organization_id": "organization-phase6",
  "user_id": "svc:order-service",
  "cart_id": "66900a4c-2d62-4a0a-a094-e62e92b0c402",
  "checkout_correlation_id": "checkout-audit-final-001",
  "validation_status": "VALID",
  "address_id": "addr-organization-phase6-hq",
  "country_code": "CO",
  "regional_policy_version": 1,
  "policy_currency": "COP",
  "rejection_reasons": "[]",
  "created_at": "2026-04-20T07:04:22.364025",
  "updated_at": "2026-04-20T07:04:22.364025"
}
```

</details>

<details>
<summary><strong>`public.idempotency_records`</strong> — 10 filas</summary>

```json
{
  "idempotency_id": "fdd1e608-82ef-478d-bb34-4e0938884f1f",
  "organization_id": "organization-phase6",
  "operation_name": "CreateCart",
  "idempotency_key": "idem-audit-1776668533",
  "request_hash": "5f1f283f730c6b525ff7aff3e6f8ef891406b2b1dcb825defdf9289605b6730d",
  "resource_type": "Cart",
  "resource_id": "66900a4c-2d62-4a0a-a094-e62e92b0c402",
  "response_status": 200,
  "created_at": "2026-04-20T07:02:14.49496",
  "updated_at": "2026-04-20T07:02:14.494961"
}
```

</details>

<details>
<summary><strong>`public.order_audits`</strong> — 10 filas</summary>

```json
{
  "audit_id": "1ab0f01b-7112-482d-aa1b-b45d8b14ad41",
  "organization_id": "organization-phase6",
  "actor_user_id": "svc:order-service",
  "action_type": "CreateCart",
  "target_type": "Cart",
  "target_id": "66900a4c-2d62-4a0a-a094-e62e92b0c402",
  "outcome": "SUCCESS",
  "payload": "{\"userId\":\"svc:order-service\"}",
  "created_at": "2026-04-20T07:02:14.472096"
}
```

</details>

<details>
<summary><strong>`public.order_lines`</strong> — 1 filas</summary>

```json
{
  "order_line_id": "6a161659-29c3-4165-b160-303e6ab499f7",
  "order_id": "2e79ad71-e09d-4de0-9f95-7c498b4dac88",
  "organization_id": "organization-phase6",
  "variant_id": "variant-phase6-coffee-500",
  "sku": "SKU-PHASE6-COFFEE-500",
  "qty": 2,
  "unit_price": 19900.0000,
  "currency": "COP",
  "reservation_id": "reservation-phase6-checkout-1",
  "reservation_confirmed": true,
  "line_total": 39800.0000,
  "created_at": "2026-04-20T07:04:24.812185",
  "updated_at": "2026-04-20T07:04:24.812185"
}
```

</details>

<details>
<summary><strong>`public.order_status_histories`</strong> — 1 filas</summary>

```json
{
  "status_history_id": "0043ef41-e5bd-4cc0-8f11-d2f81cc08821",
  "order_id": "2e79ad71-e09d-4de0-9f95-7c498b4dac88",
  "organization_id": "organization-phase6",
  "actor_user_id": "svc:order-service",
  "from_status": null,
  "to_status": "PENDING_APPROVAL",
  "reason": "created-from-validated-cart",
  "occurred_at": "2026-04-20T07:04:26.419565"
}
```

</details>

<details>
<summary><strong>`public.outbox_events`</strong> — 7 filas</summary>

```json
{
  "event_id": "b1c0403d-cf8f-47c0-8901-9a1528f7543c",
  "aggregate_type": "Cart",
  "aggregate_id": "66900a4c-2d62-4a0a-a094-e62e92b0c402",
  "event_type": "CartCreated",
  "payload": "{\"eventId\":\"b1c0403d-cf8f-47c0-8901-9a1528f7543c\",\"eventType\":\"CartCreated\",\"occurredAt\":\"2026-04-20T07:02:14.326171636Z\",\"aggregateId\":\"66900a4c-2d62-4a0a-a094-e62e92b0c402\",\"aggregateType\":\"Cart\",\"data\":{}}",
  "status": "PUBLISHED",
  "occurred_at": "2026-04-20T07:02:14.326172",
  "published_at": "2026-04-20T07:02:16.076221",
  "retry_count": 0,
  "last_error": null,
  "created_at": "2026-04-20T07:02:14.479235",
  "updated_at": "2026-04-20T07:02:16.076221"
}
```

</details>

<details>
<summary><strong>`public.purchase_orders`</strong> — 1 filas</summary>

```json
{
  "order_id": "2e79ad71-e09d-4de0-9f95-7c498b4dac88",
  "order_number": "ORD-1776668665754-2E79AD71",
  "organization_id": "organization-phase6",
  "user_id": "svc:order-service",
  "cart_id": "66900a4c-2d62-4a0a-a094-e62e92b0c402",
  "checkout_correlation_id": "checkout-audit-final-001",
  "address_id": "addr-organization-phase6-hq",
  "country_code": "CO",
  "regional_policy_version": 1,
  "policy_currency": "COP",
  "status": "PENDING_APPROVAL",
  "payment_status": "PENDING",
  "subtotal": 39800.0000,
  "total_amount": 39800.0000,
  "version": 0,
  "created_at": "2026-04-20T07:04:25.754012",
  "updated_at": "2026-04-20T07:04:25.754012"
}
```

</details>


## arkab2b_notification

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.channel_policies` | 28 |
| 2 | `public.notification_attempts` | 21 |
| 3 | `public.notification_audits` | 7 |
| 4 | `public.notification_requests` | 7 |
| 5 | `public.notification_templates` | 28 |
| 6 | `public.outbox_events` | 14 |
| 7 | `public.processed_events` | 7 |

### Muestras por tabla

<details>
<summary><strong>`public.channel_policies`</strong> — 28 filas</summary>

```json
{
  "policy_id": "policy-low-stock-detected",
  "organization_id": "organization-demo",
  "source_event_type": "inventory.low-stock-detected",
  "primary_channel": "IN_APP",
  "fallback_channel": "EMAIL",
  "max_attempts": 2,
  "retry_interval_seconds": 120,
  "active": true,
  "created_at": "2026-04-20T06:57:15.173974+00:00",
  "updated_at": "2026-04-22T13:46:47.602823+00:00"
}
```

</details>

<details>
<summary><strong>`public.notification_attempts`</strong> — 21 filas</summary>

```json
{
  "attempt_id": "93fc10d9-5af4-439b-8af4-6154e1f2365c",
  "organization_id": "organization-phase6",
  "notification_id": "47032457-06f3-466f-81c6-dc6d7cdacdd7",
  "attempt_number": 1,
  "result_status": "FAILED",
  "provider_code": "EMAIL",
  "provider_ref": null,
  "error_code": "HTTP_503",
  "error_message": "LoadBalancer does not contain an instance for the service notification-provider",
  "retryable": true,
  "latency_ms": 91,
  "request_snapshot": "{\"subject\":\"Carrito creado\",\"body\":\"Se creo un carrito para una nueva compra.\",\"data\":{}}",
  "response_snapshot": "LoadBalancer does not contain an instance for the service notification-provider",
  "created_at": "2026-04-20T07:02:22.780646+00:00"
}
```

</details>

<details>
<summary><strong>`public.notification_audits`</strong> — 7 filas</summary>

```json
{
  "audit_id": "1f1d84f7-2cc4-430f-a48c-57453f6735fe",
  "organization_id": "organization-phase6",
  "actor_id": "svc:order-service",
  "action_type": "NOTIFICATION_EMITTED",
  "target_type": "NotificationRequest",
  "target_id": "47032457-06f3-466f-81c6-dc6d7cdacdd7",
  "outcome": "SUCCESS",
  "payload": "{\"notificationId\":\"47032457-06f3-466f-81c6-dc6d7cdacdd7\"}",
  "idempotency_key": "kafka-emit-b1c0403d-cf8f-47c0-8901-9a1528f7543c",
  "payload_hash": "64fbe364c0b39398a69836108884922ad86d2a052b79625bd6cd10242c9e0956",
  "created_at": "2026-04-20T07:02:20.279936+00:00"
}
```

</details>

<details>
<summary><strong>`public.notification_requests`</strong> — 7 filas</summary>

```json
{"notification_id":"47032457-06f3-466f-81c6-dc6d7cdacdd7","organization_id":"organization-phase6","source_event_id":"b1c0403d-cf8f-47c0-8901-9a1528f7543c","source_event_type":"CartCreated","recipient_ref":"organization-phase6","channel":"EMAIL","template_id":"tpl-cart-created-v1-ph6","channel_policy_id":"policy-cart-created-v1-ph6","notification_key":"b1c0403d-cf8f-47c0-8901-9a1528f7543c::organization-phase6::EMAIL","payload_json":"{\"subject\":\"Carrito creado\",\"body\":\"Se creo un carrito para una nueva compra.\",\"data\":{}}","status":"DISCARDED","retryable":false,"next_retry_at":null,"max_attempts":3,"attempt_count":3,"trace_id":"","correlation_id":"","version":3,"created_at":"2026-04-
```

</details>

<details>
<summary><strong>`public.notification_templates`</strong> — 28 filas</summary>

```json
{
  "template_id": "tpl-order-confirmed-email-v1",
  "organization_id": "organization-demo",
  "source_event_type": "order.confirmed",
  "channel": "EMAIL",
  "template_version": 1,
  "subject_template": "Pedido confirmado",
  "body_template": "Tu pedido fue confirmado correctamente.",
  "active": true,
  "created_at": "2026-04-20T06:57:15.180203+00:00",
  "updated_at": "2026-04-22T13:46:47.622256+00:00"
}
```

</details>

<details>
<summary><strong>`public.outbox_events`</strong> — 14 filas</summary>

```json
{"event_id":"bc32bcf1-7e9a-41c8-851f-f79fe1f1a2f2","aggregate_type":"NotificationDispatch","aggregate_id":"47032457-06f3-466f-81c6-dc6d7cdacdd7","event_type":"RelevantChangeNotificationEmitted","payload":"{\"eventId\":\"bc32bcf1-7e9a-41c8-851f-f79fe1f1a2f2\",\"eventType\":\"RelevantChangeNotificationEmitted\",\"topic\":\"notification.relevant-change-notification-emitted.v1\",\"aggregateType\":\"NotificationDispatch\",\"aggregateId\":\"47032457-06f3-466f-81c6-dc6d7cdacdd7\",\"occurredAt\":\"2026-04-20T07:02:19.613065389Z\"}","status":"PUBLISHED","occurred_at":"2026-04-20T07:02:19.613065+00:00","published_at":"2026-04-20T07:02:21.633492+00:00","retry_count":0,"last_error":null,"created_at":"20
```

</details>

<details>
<summary><strong>`public.processed_events`</strong> — 7 filas</summary>

```json
{
  "processed_event_id": "114524f9-d74f-4ddd-8464-1aad6b05bc9a",
  "event_id": "b1c0403d-cf8f-47c0-8901-9a1528f7543c",
  "consumer_name": "notification-service",
  "processed_at": "2026-04-20T07:02:19.613065+00:00"
}
```

</details>


## arkab2b_reporting

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.analytic_facts` | 7 |
| 2 | `public.consumer_checkpoints` | 4 |
| 3 | `public.operations_kpi_projections` | 1 |
| 4 | `public.outbox_events` | 29 |
| 5 | `public.processed_events` | 7 |
| 6 | `public.reporting_audits` | 22 |
| 7 | `public.sales_projections` | 1 |

### Muestras por tabla

<details>
<summary><strong>`public.analytic_facts`</strong> — 7 filas</summary>

```json
{
  "fact_id": "6636a0fe-6ee6-4365-9623-2dfb86b00f5e",
  "organization_id": "organization-phase6",
  "source_event_id": "b1c0403d-cf8f-47c0-8901-9a1528f7543c",
  "event_type": "CartCreated",
  "fact_type": "SALES",
  "raw_payload": "{}",
  "normalized_payload": "{}",
  "fact_status": "APPLIED",
  "rejection_reason": null,
  "period": "2026-W17",
  "occurred_at": "2026-04-20T07:02:14.326172+00:00",
  "created_at": "2026-04-20T07:02:19.606804+00:00",
  "updated_at": "2026-04-20T07:02:20.350908+00:00"
}
```

</details>

<details>
<summary><strong>`public.consumer_checkpoints`</strong> — 4 filas</summary>

```json
{
  "checkpoint_id": "c2213401-189e-4b75-b216-875b9ab9f24b",
  "organization_id": "organization-phase6",
  "consumer_name": "reporting-service",
  "topic": "order.cart.events.v1",
  "partition": 1,
  "current_offset": 1,
  "latest_offset": 1,
  "lag": 0,
  "updated_at": "2026-04-20T07:04:21.924969+00:00"
}
```

</details>

<details>
<summary><strong>`public.operations_kpi_projections`</strong> — 1 filas</summary>

```json
{
  "projection_id": "kpi-notif-eff-orgdemo-2026w01",
  "organization_id": "organization-demo",
  "period": "2026-W01",
  "kpi_name": "notification_effectiveness",
  "kpi_value": 95.00,
  "version": 0,
  "created_at": "2026-04-20T06:57:45.891576+00:00",
  "updated_at": "2026-04-20T06:57:45.891576+00:00"
}
```

</details>

<details>
<summary><strong>`public.outbox_events`</strong> — 29 filas</summary>

```json
{"event_id":"0f74458b-1248-44cb-844c-dfce0ca2073d","aggregate_type":"AnalyticFact","aggregate_id":"6636a0fe-6ee6-4365-9623-2dfb86b00f5e","event_type":"AnalyticFactRegistered","payload":"{\"eventId\":\"0f74458b-1248-44cb-844c-dfce0ca2073d\",\"eventType\":\"AnalyticFactRegistered\",\"aggregateType\":\"AnalyticFact\",\"aggregateId\":\"6636a0fe-6ee6-4365-9623-2dfb86b00f5e\",\"occurredAt\":\"2026-04-20T07:02:19.606803514Z\",\"topic\":\"reporting.mutation.v1\",\"data\":{}}","status":"PUBLISHED","occurred_at":"2026-04-20T07:02:19.606804+00:00","published_at":"2026-04-20T07:02:20.556734+00:00","retry_count":0,"last_error":null,"created_at":"2026-04-20T07:02:20.304404+00:00","updated_at":"2026-04-20T
```

</details>

<details>
<summary><strong>`public.processed_events`</strong> — 7 filas</summary>

```json
{
  "processed_event_id": "a979c1a0-f8eb-4f96-aa79-352166b862e0",
  "event_id": "b1c0403d-cf8f-47c0-8901-9a1528f7543c",
  "consumer_name": "reporting-service",
  "processed_at": "2026-04-20T07:02:19.606804+00:00"
}
```

</details>

<details>
<summary><strong>`public.reporting_audits`</strong> — 22 filas</summary>

```json
{
  "audit_id": "52764212-a4b2-457d-a6f8-93e571d5263a",
  "organization_id": "organization-phase6",
  "actor_id": "reporting-kafka-consumer",
  "action_type": "REGISTER_ANALYTIC_FACT",
  "target_type": "AnalyticFact",
  "target_id": "6636a0fe-6ee6-4365-9623-2dfb86b00f5e",
  "outcome": "SUCCESS",
  "payload": "{\"sourceEventId\":\"b1c0403d-cf8f-47c0-8901-9a1528f7543c\",\"factId\":\"6636a0fe-6ee6-4365-9623-2dfb86b00f5e\"}",
  "idempotency_key": "kafka-register-b1c0403d-cf8f-47c0-8901-9a1528f7543c",
  "payload_hash": "f225f4c98988c66ce79b73235a9370e67962dda496cb14126b9d919b6cbb6804",
  "created_at": "2026-04-20T07:02:20.286243+00:00"
}
```

</details>

<details>
<summary><strong>`public.sales_projections`</strong> — 1 filas</summary>

```json
{
  "projection_id": "4be9d372-9302-4634-8e30-b043f76bbeb2",
  "organization_id": "organization-phase6",
  "period": "2026-W17",
  "total_sales": 0.00,
  "paid_amount": 0.00,
  "pending_amount": 0.00,
  "confirmed_orders": 0,
  "average_ticket": 0.00,
  "version": 6,
  "created_at": "2026-04-20T07:02:20.417033+00:00",
  "updated_at": "2026-04-20T07:15:37.955133+00:00"
}
```

</details>

