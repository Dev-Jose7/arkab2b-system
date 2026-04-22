# Snapshot de tablas con datos (Postgres ArkaB2B)

Generado: 2026-04-22 15:57:16 -0500

## Resumen ejecutivo

| Base de datos | Tablas con datos | Filas totales |
|---|---:|---:|
| `identity_access` | 5 | 72 |
| `directory` | 5 | 11 |
| `arkab2b_catalog` | 5 | 22 |
| `inventory` | 3 | 8 |
| `order` | 0 | 0 |
| `arkab2b_notification` | 2 | 56 |
| `arkab2b_reporting` | 1 | 1 |

## identity_access

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.role` | 6 |
| 2 | `public.role_assignment_policy` | 12 |
| 3 | `public.role_permission` | 52 |
| 4 | `public.user_account` | 1 |
| 5 | `public.user_role_assignment` | 1 |

### Muestras por tabla

<details>
<summary><strong>`public.role`</strong> — 6 filas</summary>

```json
{
  "role_id": "9de6fc0e-13d2-42a1-9232-4df6cdbf6f31",
  "role_code": "ORG_OWNER",
  "description": "Organization owner role",
  "created_at": "2026-04-22T20:42:18.190946",
  "updated_at": "2026-04-22T20:42:18.190946"
}
```

</details>

<details>
<summary><strong>`public.role_assignment_policy`</strong> — 12 filas</summary>

```json
{
  "assigner_role_id": "3f4b2c76-8be7-4ea2-b620-4a8c6d29d30f",
  "assignable_role_id": "9de6fc0e-13d2-42a1-9232-4df6cdbf6f31",
  "created_at": "2026-04-22T20:42:18.196362"
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
  "created_at": "2026-04-22T20:42:18.192395"
}
```

</details>

<details>
<summary><strong>`public.user_account`</strong> — 1 filas</summary>

```json
{
  "user_id": "svc-arka-system",
  "email": "svc-arka-system@service.local",
  "status": "ACTIVE",
  "failed_login_count": 0,
  "created_at": "2026-04-22T20:42:18.197824",
  "updated_at": "2026-04-22T20:42:18.197824"
}
```

</details>

<details>
<summary><strong>`public.user_role_assignment`</strong> — 1 filas</summary>

```json
{
  "assignment_id": "svc-arka-system-arka-admin",
  "user_id": "svc-arka-system",
  "role_id": "3f4b2c76-8be7-4ea2-b620-4a8c6d29d30f",
  "status": "ACTIVE",
  "assigned_by": "svc-arka-system",
  "assigned_at": "2026-04-22T20:42:18.198452",
  "created_at": "2026-04-22T20:42:18.198452",
  "updated_at": "2026-04-22T20:42:18.198452"
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
| 5 | `public.organization_legal_profile` | 2 |

### Muestras por tabla

<details>
<summary><strong>`public.address`</strong> — 2 filas</summary>

```json
{
  "address_id": "addr-organization-demo-hq",
  "organization_id": "organization-demo",
  "address_type": "SHIPPING",
  "alias": "Centro logistico demo",
  "line1": "Calle 72 # 20-37",
  "line2": "Bodega 4",
  "city": "Bogota",
  "state_region": "Cundinamarca",
  "postal_code": "110231",
  "country_code": "CO",
  "reference": "Bodega local de pruebas para integracion de accesorios PC",
  "latitude": null,
  "longitude": null,
  "is_default": true,
  "status": "ACTIVE",
  "validation_status": "VERIFIED",
  "validated_at": "2026-04-22T20:57:01.184067",
  "created_at": "2026-04-22T20:42:44.071893",
  "updated_at": "2026-04-22T20:57:01.184067"
}
```

</details>

<details>
<summary><strong>`public.organization`</strong> — 2 filas</summary>

```json
{
  "organization_id": "organization-demo",
  "legal_name": "Arka Demo SAS",
  "trade_name": "Arka Demo",
  "country_code": "CO",
  "currency_code": "COP",
  "timezone": "America/Bogota",
  "segment_tier": "FOUNDATION",
  "status": "ACTIVE",
  "created_at": "2026-04-22T20:42:44.070499",
  "updated_at": "2026-04-22T20:57:00.988761"
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
  "label": "Operaciones demo",
  "value_normalized": "operaciones.demo@arka-b2b.test",
  "value_masked": "o***@arka-b2b.test",
  "is_primary": true,
  "status": "ACTIVE",
  "created_at": "2026-04-22T20:42:44.075592",
  "updated_at": "2026-04-22T20:57:01.213846"
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
  "effective_from": "2026-04-21T20:57:01.196668",
  "effective_to": null,
  "status": "ACTIVE",
  "created_at": "2026-04-22T20:42:44.07381",
  "updated_at": "2026-04-22T20:57:01.196668"
}
```

</details>

<details>
<summary><strong>`public.organization_legal_profile`</strong> — 2 filas</summary>

```json
{
  "legal_profile_id": "legal-profile-organization-demo",
  "organization_id": "organization-demo",
  "tax_id_type": "NIT",
  "tax_id": "901654321-7",
  "fiscal_regime": "RESPONSABLE_IVA",
  "legal_representative": "Laura Mendoza Rios",
  "country_code": "CO",
  "verification_status": "VERIFIED",
  "verified_at": "2026-04-22T20:57:01.135054",
  "created_at": "2026-04-22T20:57:01.135054",
  "updated_at": "2026-04-22T20:57:01.135054"
}
```

</details>


## arkab2b_catalog

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.brands` | 5 |
| 2 | `public.categories` | 5 |
| 3 | `public.prices` | 4 |
| 4 | `public.products` | 4 |
| 5 | `public.variants` | 4 |

### Muestras por tabla

<details>
<summary><strong>`public.brands`</strong> — 5 filas</summary>

```json
{
  "brand_id": "brand-logitech",
  "organization_id": "organization-demo",
  "brand_code": "LOGITECH",
  "brand_name": "Logitech",
  "status": "ACTIVE",
  "created_at": "2026-04-22T20:43:13.384247+00:00",
  "updated_at": "2026-04-22T20:43:13.384247+00:00"
}
```

</details>

<details>
<summary><strong>`public.categories`</strong> — 5 filas</summary>

```json
{
  "category_id": "category-mice",
  "organization_id": "organization-demo",
  "category_code": "MICE",
  "category_name": "Mice y apuntadores",
  "status": "ACTIVE",
  "created_at": "2026-04-22T20:43:13.385616+00:00",
  "updated_at": "2026-04-22T20:43:13.385616+00:00"
}
```

</details>

<details>
<summary><strong>`public.prices`</strong> — 4 filas</summary>

```json
{
  "price_id": "price-demo-m185-base",
  "organization_id": "organization-demo",
  "variant_id": "variant-demo-m185",
  "price_type": "BASE",
  "currency": "COP",
  "amount": 64900.0000,
  "effective_from": "2026-04-21T20:43:13.389678+00:00",
  "effective_until": null,
  "status": "ACTIVE",
  "created_at": "2026-04-22T20:43:13.389678+00:00",
  "updated_at": "2026-04-22T20:43:13.389678+00:00"
}
```

</details>

<details>
<summary><strong>`public.products`</strong> — 4 filas</summary>

```json
{
  "product_id": "product-demo-m185",
  "organization_id": "organization-demo",
  "product_code": "M185-WL",
  "product_name": "Mouse inalambrico Logitech M185 gris",
  "description": "Mouse inalambrico de entrada para oficinas, puntos de venta y estaciones administrativas.",
  "brand_id": "brand-logitech",
  "category_id": "category-mice",
  "status": "ACTIVE",
  "created_at": "2026-04-22T20:43:13.386711+00:00",
  "updated_at": "2026-04-22T20:43:13.386711+00:00"
}
```

</details>

<details>
<summary><strong>`public.variants`</strong> — 4 filas</summary>

```json
{
  "variant_id": "variant-demo-m185",
  "organization_id": "organization-demo",
  "product_id": "product-demo-m185",
  "sku": "SKU-DEMO-M185-GRY",
  "variant_name": "Mouse Logitech M185 gris",
  "description": "Variante vendible de mouse inalambrico para baseline demo.",
  "status": "SELLABLE",
  "sellable_from": "2026-03-23T20:43:13.388515+00:00",
  "sellable_until": null,
  "weight_grams": 75,
  "created_at": "2026-04-22T20:43:13.388515+00:00",
  "updated_at": "2026-04-22T20:43:13.388515+00:00"
}
```

</details>


## inventory

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.stock_items` | 4 |
| 2 | `public.stock_reservations` | 2 |
| 3 | `public.warehouses` | 2 |

### Muestras por tabla

<details>
<summary><strong>`public.stock_items`</strong> — 4 filas</summary>

```json
{
  "stock_item_id": "stock-item-demo-m185",
  "organization_id": "organization-demo",
  "warehouse_id": "warehouse-demo-main",
  "sku": "SKU-DEMO-M185-GRY",
  "physical_qty": 120,
  "reserved_qty": 2,
  "reorder_point": 25,
  "safety_stock": 12,
  "status": "ACTIVE",
  "version": 0,
  "created_at": "2026-04-22T20:43:43.83286",
  "updated_at": "2026-04-22T20:43:43.83286"
}
```

</details>

<details>
<summary><strong>`public.stock_reservations`</strong> — 2 filas</summary>

```json
{
  "reservation_id": "reservation-demo-m185-1",
  "organization_id": "organization-demo",
  "stock_item_id": "stock-item-demo-m185",
  "warehouse_id": "warehouse-demo-main",
  "sku": "SKU-DEMO-M185-GRY",
  "cart_id": "cart-demo-m185-seeded",
  "order_id": "order-demo-m185-seeded",
  "qty": 2,
  "status": "CONFIRMED",
  "expires_at": "2026-04-29T20:43:43.834011",
  "confirmed_at": "2026-04-22T20:42:43.834011",
  "released_at": null,
  "created_at": "2026-04-22T20:43:43.834011",
  "updated_at": "2026-04-22T20:43:43.834011"
}
```

</details>

<details>
<summary><strong>`public.warehouses`</strong> — 2 filas</summary>

```json
{
  "warehouse_id": "warehouse-demo-main",
  "organization_id": "organization-demo",
  "warehouse_code": "BOG-MAIN",
  "warehouse_name": "Centro logistico demo Bogota",
  "country_code": "CO",
  "status": "ACTIVE",
  "created_at": "2026-04-22T20:43:43.83163",
  "updated_at": "2026-04-22T20:43:43.83163"
}
```

</details>


## order

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | _sin tablas con datos_ | 0 |

### Muestras por tabla

_No hay muestras porque no hay tablas con datos._


## arkab2b_notification

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.channel_policies` | 28 |
| 2 | `public.notification_templates` | 28 |

### Muestras por tabla

<details>
<summary><strong>`public.channel_policies`</strong> — 28 filas</summary>

```json
{
  "policy_id": "policy-order-confirmed",
  "organization_id": "organization-demo",
  "source_event_type": "order.confirmed",
  "primary_channel": "EMAIL",
  "fallback_channel": "SMS",
  "max_attempts": 3,
  "retry_interval_seconds": 300,
  "active": true,
  "created_at": "2026-04-22T20:44:46.912242+00:00",
  "updated_at": "2026-04-22T20:44:46.912242+00:00"
}
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
  "subject_template": "Pedido de accesorios confirmado",
  "body_template": "Tu pedido B2B de accesorios para PC fue confirmado correctamente.",
  "active": true,
  "created_at": "2026-04-22T20:44:46.921044+00:00",
  "updated_at": "2026-04-22T20:44:46.921044+00:00"
}
```

</details>


## arkab2b_reporting

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.operations_kpi_projections` | 1 |

### Muestras por tabla

<details>
<summary><strong>`public.operations_kpi_projections`</strong> — 1 filas</summary>

```json
{
  "projection_id": "kpi-order-fill-rate-orgdemo-2026w01",
  "organization_id": "organization-demo",
  "period": "2026-W01",
  "kpi_name": "order_fill_rate",
  "kpi_value": 97.50,
  "version": 0,
  "created_at": "2026-04-22T20:45:20.777928+00:00",
  "updated_at": "2026-04-22T20:45:20.777928+00:00"
}
```

</details>


