# Snapshot de tablas con datos (Postgres ArkaB2B)

Generado: 2026-04-22 17:47:13 -0500

## Resumen ejecutivo

| Base de datos | Tablas con datos | Filas totales |
|---|---:|---:|
| `identity_access` | 5 | 72 |
| `directory` | 5 | 12 |
| `arkab2b_catalog` | 5 | 20 |
| `inventory` | 3 | 8 |
| `order` | 0 | 0 |
| `arkab2b_notification` | 2 | 60 |
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
  "created_at": "2026-04-22T22:42:53.829447",
  "updated_at": "2026-04-22T22:42:53.829447"
}
```

</details>

<details>
<summary><strong>`public.role_assignment_policy`</strong> — 12 filas</summary>

```json
{
  "assigner_role_id": "3f4b2c76-8be7-4ea2-b620-4a8c6d29d30f",
  "assignable_role_id": "9de6fc0e-13d2-42a1-9232-4df6cdbf6f31",
  "created_at": "2026-04-22T22:42:53.843076"
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
  "created_at": "2026-04-22T22:42:53.834548"
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
  "created_at": "2026-04-22T22:42:53.845885",
  "updated_at": "2026-04-22T22:42:53.845885"
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
  "assigned_at": "2026-04-22T22:42:53.849733",
  "created_at": "2026-04-22T22:42:53.849733",
  "updated_at": "2026-04-22T22:42:53.849733"
}
```

</details>


## directory

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.address` | 2 |
| 2 | `public.organization` | 2 |
| 3 | `public.organization_contact` | 4 |
| 4 | `public.organization_country_policy` | 2 |
| 5 | `public.organization_legal_profile` | 2 |

### Muestras por tabla

<details>
<summary><strong>`public.address`</strong> — 2 filas</summary>

```json
{
  "address_id": "1d3f5c0a-7d1c-4305-96b4-3cde13d8ac8f",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "address_type": "SHIPPING",
  "alias": "Bodega metropolitana",
  "line1": "Calle 10 Sur # 50FF-84",
  "line2": "Bodega 7",
  "city": "Medellin",
  "state_region": "Antioquia",
  "postal_code": "050022",
  "country_code": "CO",
  "reference": "Centro de despacho para integradores y resellers de perifericos.",
  "latitude": null,
  "longitude": null,
  "is_default": true,
  "status": "ACTIVE",
  "validation_status": "VERIFIED",
  "validated_at": "2026-04-22T22:43:15.461212",
  "created_at": "2026-04-22T22:43:15.461212",
  "updated_at": "2026-04-22T22:43:15.461212"
}
```

</details>

<details>
<summary><strong>`public.organization`</strong> — 2 filas</summary>

```json
{
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "legal_name": "NovaCore Integraciones SAS",
  "trade_name": "NovaCore TI",
  "country_code": "CO",
  "currency_code": "COP",
  "timezone": "America/Bogota",
  "segment_tier": "FOUNDATION",
  "status": "ACTIVE",
  "created_at": "2026-04-22T22:43:15.454039",
  "updated_at": "2026-04-22T22:43:15.454039"
}
```

</details>

<details>
<summary><strong>`public.organization_contact`</strong> — 4 filas</summary>

```json
{
  "contact_id": "7b10db82-291f-49a3-a8fa-d2221872b33e",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "contact_type": "EMAIL",
  "label": "Operaciones Medellin",
  "value_normalized": "operaciones@novacoreti.co",
  "value_masked": "o***@novacoreti.co",
  "is_primary": true,
  "status": "ACTIVE",
  "created_at": "2026-04-22T22:43:15.476672",
  "updated_at": "2026-04-22T22:43:15.476672"
}
```

</details>

<details>
<summary><strong>`public.organization_country_policy`</strong> — 2 filas</summary>

```json
{
  "policy_id": "c5ebf957-6df6-432c-8e60-e9ee3c33ae45",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "country_code": "CO",
  "policy_version": 1,
  "currency_code": "COP",
  "week_starts_on": "MONDAY",
  "weekly_cutoff_local_time": "18:00:00",
  "timezone": "America/Bogota",
  "reporting_retention_days": 90,
  "requires_verified_address": true,
  "effective_from": "2026-04-21T22:43:15.470929",
  "effective_to": null,
  "status": "ACTIVE",
  "created_at": "2026-04-22T22:43:15.470929",
  "updated_at": "2026-04-22T22:43:15.470929"
}
```

</details>

<details>
<summary><strong>`public.organization_legal_profile`</strong> — 2 filas</summary>

```json
{
  "legal_profile_id": "a844ef93-e31f-4f3c-9c4f-1a2ffdb058d1",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "tax_id_type": "NIT",
  "tax_id": "901815274-1",
  "fiscal_regime": "RESPONSABLE_IVA",
  "legal_representative": "Paula Andrea Vargas",
  "country_code": "CO",
  "verification_status": "VERIFIED",
  "verified_at": "2026-04-22T22:43:15.456907",
  "created_at": "2026-04-22T22:43:15.456907",
  "updated_at": "2026-04-22T22:43:15.456907"
}
```

</details>


## arkab2b_catalog

### Tablas con datos

| # | Tabla | Filas |
|---:|---|---:|
| 1 | `public.brands` | 4 |
| 2 | `public.categories` | 4 |
| 3 | `public.prices` | 4 |
| 4 | `public.products` | 4 |
| 5 | `public.variants` | 4 |

### Muestras por tabla

<details>
<summary><strong>`public.brands`</strong> — 4 filas</summary>

```json
{
  "brand_id": "4d01aa38-baf1-4300-ad88-d1d97c4cb447",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "brand_code": "LOGITECH",
  "brand_name": "Logitech",
  "status": "ACTIVE",
  "created_at": "2026-04-22T22:43:44.630955+00:00",
  "updated_at": "2026-04-22T22:43:44.630955+00:00"
}
```

</details>

<details>
<summary><strong>`public.categories`</strong> — 4 filas</summary>

```json
{
  "category_id": "2be89df2-2f04-4ba3-8d13-6b0c4dff0c5e",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "category_code": "MICE",
  "category_name": "Mice y apuntadores",
  "status": "ACTIVE",
  "created_at": "2026-04-22T22:43:44.632621+00:00",
  "updated_at": "2026-04-22T22:43:44.632621+00:00"
}
```

</details>

<details>
<summary><strong>`public.prices`</strong> — 4 filas</summary>

```json
{
  "price_id": "de8e1c86-23a5-49e0-bf56-48e0887a97d3",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "variant_id": "9ebaa7c4-cd85-4cfa-b916-86a390412534",
  "price_type": "BASE",
  "currency": "COP",
  "amount": 64900.0000,
  "effective_from": "2026-04-21T22:43:44.635913+00:00",
  "effective_until": null,
  "status": "ACTIVE",
  "created_at": "2026-04-22T22:43:44.635913+00:00",
  "updated_at": "2026-04-22T22:43:44.635913+00:00"
}
```

</details>

<details>
<summary><strong>`public.products`</strong> — 4 filas</summary>

```json
{
  "product_id": "0b5bf628-e499-4ba9-a352-ae77f722c650",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "product_code": "M185-WL",
  "product_name": "Mouse inalambrico Logitech M185 gris",
  "description": "Mouse inalambrico de entrada para oficinas, puntos de venta y estaciones administrativas.",
  "brand_id": "4d01aa38-baf1-4300-ad88-d1d97c4cb447",
  "category_id": "2be89df2-2f04-4ba3-8d13-6b0c4dff0c5e",
  "status": "ACTIVE",
  "created_at": "2026-04-22T22:43:44.633597+00:00",
  "updated_at": "2026-04-22T22:43:44.633597+00:00"
}
```

</details>

<details>
<summary><strong>`public.variants`</strong> — 4 filas</summary>

```json
{
  "variant_id": "9ebaa7c4-cd85-4cfa-b916-86a390412534",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "product_id": "0b5bf628-e499-4ba9-a352-ae77f722c650",
  "sku": "ARK-LGT-M185-GRY",
  "variant_name": "Mouse Logitech M185 gris",
  "description": "Variante vendible orientada a reposicion de estaciones de trabajo.",
  "status": "SELLABLE",
  "sellable_from": "2026-03-23T22:43:44.63496+00:00",
  "sellable_until": null,
  "weight_grams": 75,
  "created_at": "2026-04-22T22:43:44.63496+00:00",
  "updated_at": "2026-04-22T22:43:44.63496+00:00"
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
  "stock_item_id": "8ca61d8f-0fb7-428d-b7a7-9a44d20f978a",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "warehouse_id": "b0d0d293-3f63-4a43-b0c1-8d9c2ac9ce0b",
  "sku": "ARK-LGT-M185-GRY",
  "physical_qty": 120,
  "reserved_qty": 2,
  "reorder_point": 25,
  "safety_stock": 12,
  "status": "ACTIVE",
  "version": 0,
  "created_at": "2026-04-22T22:44:18.035423",
  "updated_at": "2026-04-22T22:44:18.035423"
}
```

</details>

<details>
<summary><strong>`public.stock_reservations`</strong> — 2 filas</summary>

```json
{
  "reservation_id": "1243c2fa-6f02-4baf-98e5-c98f7cc90c43",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "stock_item_id": "8ca61d8f-0fb7-428d-b7a7-9a44d20f978a",
  "warehouse_id": "b0d0d293-3f63-4a43-b0c1-8d9c2ac9ce0b",
  "sku": "ARK-LGT-M185-GRY",
  "cart_id": "43d5ec97-89e3-46a2-a5aa-df69013ef8e0",
  "order_id": "67fc526d-b87e-42c9-a420-163243833fcf",
  "qty": 2,
  "status": "CONFIRMED",
  "expires_at": "2026-04-29T22:44:18.040931",
  "confirmed_at": "2026-04-22T22:43:18.040931",
  "released_at": null,
  "created_at": "2026-04-22T22:44:18.040931",
  "updated_at": "2026-04-22T22:44:18.040931"
}
```

</details>

<details>
<summary><strong>`public.warehouses`</strong> — 2 filas</summary>

```json
{
  "warehouse_id": "b0d0d293-3f63-4a43-b0c1-8d9c2ac9ce0b",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "warehouse_code": "MED-CEN",
  "warehouse_name": "Centro logistico NovaCore Medellin",
  "country_code": "CO",
  "status": "ACTIVE",
  "created_at": "2026-04-22T22:44:18.032843",
  "updated_at": "2026-04-22T22:44:18.032843"
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
| 1 | `public.channel_policies` | 30 |
| 2 | `public.notification_templates` | 30 |

### Muestras por tabla

<details>
<summary><strong>`public.channel_policies`</strong> — 30 filas</summary>

```json
{
  "policy_id": "5b9b50d1-99e8-42f4-b47e-48071e98e5d4",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "source_event_type": "order.confirmed",
  "primary_channel": "EMAIL",
  "fallback_channel": "SMS",
  "max_attempts": 3,
  "retry_interval_seconds": 300,
  "active": true,
  "created_at": "2026-04-22T22:45:15.845531+00:00",
  "updated_at": "2026-04-22T22:45:15.845531+00:00"
}
```

</details>

<details>
<summary><strong>`public.notification_templates`</strong> — 30 filas</summary>

```json
{
  "template_id": "83ea8c94-fbda-42e2-873f-e1630352e5b8",
  "organization_id": "8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1",
  "source_event_type": "order.confirmed",
  "channel": "EMAIL",
  "template_version": 1,
  "subject_template": "Pedido de accesorios confirmado",
  "body_template": "Tu pedido B2B de accesorios para PC fue confirmado correctamente.",
  "active": true,
  "created_at": "2026-04-22T22:45:15.853266+00:00",
  "updated_at": "2026-04-22T22:45:15.853266+00:00"
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
  "projection_id": "0395f80c-9ea0-4ddd-a588-badd3d661601",
  "organization_id": "6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77",
  "period": "2026-W17",
  "kpi_name": "order_fill_rate",
  "kpi_value": 97.50,
  "version": 0,
  "created_at": "2026-04-22T22:45:52.156194+00:00",
  "updated_at": "2026-04-22T22:45:52.156194+00:00"
}
```

</details>


