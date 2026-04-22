# GUIA DE PRUEBAS MANUALES POR HISTORIA DE USUARIO

## Objetivo

Esta guia permite ejecutar manualmente las historias de usuario del backlog original `HU1..HU8` usando las APIs reales actuales de ArkaB2B.

La guia esta pensada para:
- Swagger UI via Gateway
- Postman
- pruebas locales contra `http://localhost:8080`

Si trabajas contra VPS, reemplaza `localhost:8080` por el host remoto.

---

## 1. Prerrequisitos

### Stack local arriba

Desde la raiz del repo:

```bash
cd /Users/jose/Development/Java/arkab2b
./scripts/generate-local-jwt-keys.sh
./scripts/start-integrated-local.sh
```

### Swagger Hub

Usa el hub principal del Gateway:

- `http://localhost:8080/tools/ARKAB2B_SWAGGER_TABS.html`

### Base URL

- `http://localhost:8080`

---

## 2. Baseline local que usa esta guia

Valores reales sembrados en la baseline actual para `Arka Distribuciones SAS`:

- `organizationId`: `6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77`
- `countryCode`: `CO`
- `addressId`: `3c43c7db-bf2c-4f0a-bf63-8e0f7d8a1e9a`
- `warehouseId`: `96c99bf8-7de7-4141-96fc-2d788b771c86`
- `policyId`: `f7c6c218-2bb4-4b4c-a7dc-7d4b9e2ef3f1`

### Catalogo seed util

- `brandId Redragon`: `e1af4db9-7a9a-4c40-b8a0-d9737d5988d8`
- `brandId Kingston`: `7d5dd0e5-7fd4-4c80-9592-df9a6b5d2159`
- `categoryId Teclados`: `0a17f6d8-d7a7-4f22-bb9f-8ae735ef3c95`
- `categoryId Almacenamiento`: `eab40527-9a6c-4e14-93d6-45b2a6ccf08d`

### Productos/variantes seed utiles

- `Redragon Kumara K552 RGB`
- `productId`: `1ecf779c-0baf-4fd4-a2f6-720be51a12f0`
- `variantId`: `f5d14d62-7fbd-4b0b-9c97-872c62c0fd8e`
- `sku`: `ARK-RDG-K552-RGB`
- `stockItemId`: `cb35045a-8f6e-4c3e-a1d5-2e3217a43b9c`

- `Kingston A400 480GB SATA`
- `productId`: `24f9bc61-bbf2-4318-8d6f-09f8cd4e2c51`
- `variantId`: `3b84ec72-acde-4433-a55f-cd5e47d2a8f5`
- `sku`: `ARK-KNG-A400-480`
- `stockItemId`: `464ffdb0-70e2-4f8d-898f-a3ba0d78d597`

### Semana de ejemplo

Para los reportes semanales puedes usar:

- `weekId`: `2026-W17`

---

## 3. Autenticacion comun para todas las historias

## Paso A. Crear founder si aun no tienes actor

**Endpoint**
- `POST /api/v1/auth/register-founder`

**Body**

```json
{
  "email": "qa.hu.manual@arka.test",
  "password": "PlainSecret123"
}
```

Usa un email unico si ya habias creado esa cuenta antes.

## Paso B. Login con contexto organizacional

**Endpoint**
- `POST /api/v1/auth/login`

**Body**

```json
{
  "email": "qa.hu.manual@arka.test",
  "password": "PlainSecret123",
  "organizationId": "6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77",
  "countryCode": "CO"
}
```

**Resultado esperado**

La respuesta trae:
- `accessToken`
- `refreshToken`
- `sessionId`

Guarda `accessToken`.

## Paso C. Header comun de autorizacion

En Swagger usa el boton `Authorize` y pega:

```text
Bearer <accessToken>
```

En Postman usa:

```http
Authorization: Bearer <accessToken>
```

## Paso D. Idempotency-Key

Cuando un endpoint la pida por header, usa un valor unico por intento. Ejemplos:

```text
hu4-order-001
hu4-order-002
stock-adjust-001
```

Si quieres evitar choques, usa UUID.

---

## 4. HU1 - Registrar productos en el sistema

**Objetivo**
Registrar un nuevo producto con caracteristicas, precio y stock inicial.

## Orden de ejecucion

### 1. Consultar marcas activas

**Endpoint**
- `GET /api/v1/catalog/brands`

**Headers**
- `Authorization`

**Resultado esperado**
- Debe incluir las marcas `Redragon` y `Kingston` con sus UUIDs.

### 2. Consultar categorias activas

**Endpoint**
- `GET /api/v1/catalog/categories`

**Headers**
- `Authorization`

**Resultado esperado**
- Debe incluir las categorias `Teclados mecanicos` y `Almacenamiento SSD` con sus UUIDs.

### 3. Registrar producto completo

**Endpoint**
- `POST /api/v1/catalog/product-registrations`

**Headers**
- `Authorization`

**Body**

```json
{
  "productCode": "K617-FIZZ-BLK",
  "name": "Teclado mecanico Redragon Fizz K617 negro",
  "description": "Teclado mecanico compacto 60% para canal B2B, oficinas gamer y venta corporativa.",
  "brandId": "e1af4db9-7a9a-4c40-b8a0-d9737d5988d8",
  "categoryId": "0a17f6d8-d7a7-4f22-bb9f-8ae735ef3c95",
  "tags": ["teclado", "mecanico", "rgb", "redragon"],
  "variant": {
    "sku": "ARK-RDG-K617-BLK",
    "name": "Redragon Fizz K617 negro",
    "description": "Switch rojo, formato 60%, iluminacion RGB.",
    "weightGrams": 680,
    "attributes": [
      {
        "attributeCode": "COLOR",
        "value": "Negro",
        "normalizedValue": "NEGRO"
      },
      {
        "attributeCode": "SWITCH",
        "value": "Red",
        "normalizedValue": "RED"
      },
      {
        "attributeCode": "FORM_FACTOR",
        "value": "60%",
        "normalizedValue": "60"
      }
    ]
  },
  "price": {
    "amount": 249900,
    "currency": "COP",
    "priceType": "BASE",
    "effectiveFrom": null,
    "effectiveUntil": null
  },
  "stock": {
    "warehouseId": "96c99bf8-7de7-4141-96fc-2d788b771c86",
    "initialPhysicalQty": 25,
    "reorderPoint": 5,
    "safetyStock": 3
  },
  "regionalPolicyReference": "f7c6c218-2bb4-4b4c-a7dc-7d4b9e2ef3f1",
  "idempotencyKey": "hu1-k617-001"
}
```

**Resultado esperado**
- La respuesta trae:
  - `message`
  - `product`
  - `variant`
  - `price`
  - `offer`
  - `stock`

Guarda:
- `product.productId`
- `variant.variantId`
- `stock.stockItemId`

### 4. Verificar detalle del producto

**Endpoint**
- `GET /api/v1/catalog/products/{productId}/detail`

**Headers**
- `Authorization`

**Resultado esperado**
- El producto debe aparecer `ACTIVE`.
- La variante debe quedar vendible.
- El precio y la oferta deben quedar publicados.

---

## 5. HU2 - Actualizar stock de productos

**Objetivo**
Modificar stock sin permitir negativos y revisar historial.

## Orden de ejecucion

### 1. Consultar stock actual de una referencia existente

**Endpoint**
- `GET /api/v1/stock-items/cb35045a-8f6e-4c3e-a1d5-2e3217a43b9c`

**Headers**
- `Authorization`

### 2. Ajustar stock

**Endpoint**
- `POST /api/v1/stock-items/cb35045a-8f6e-4c3e-a1d5-2e3217a43b9c/stock-adjustments`

**Headers**
- `Authorization`
- `Idempotency-Key: hu2-stock-adjust-001`

**Body**

```json
{
  "deltaQty": 10,
  "reason": "Ingreso por compra a proveedor nacional"
}
```

### 3. Confirmar stock actualizado

**Endpoint**
- `GET /api/v1/stock-items/cb35045a-8f6e-4c3e-a1d5-2e3217a43b9c`

**Headers**
- `Authorization`

### 4. Revisar historial de movimientos

**Endpoint**
- `GET /api/v1/stock-items/cb35045a-8f6e-4c3e-a1d5-2e3217a43b9c/movements?limit=20`

**Headers**
- `Authorization`

**Resultado esperado**
- Debe aparecer un movimiento con el `reason` enviado.
- El stock disponible no debe quedar negativo.

---

## 6. HU3 - Generar reportes de productos por abastecer

**Objetivo**
Ver productos bajo umbral configurable y generar el reporte semanal exportable.

## Orden de ejecucion

### 1. Consultar productos bajo umbral configurable

**Endpoint**
- `GET /api/v1/stock-items/low-stock-report?warehouseId=96c99bf8-7de7-4141-96fc-2d788b771c86&threshold=50`

**Headers**
- `Authorization`

**Resultado esperado**
- Debe devolver al menos los SKUs con disponibilidad menor o igual a `50`.
- En la baseline actual normalmente aparecera `ARK-RDG-K552-RGB`.

### 2. Generar reporte semanal de abastecimiento

**Endpoint**
- `POST /api/v1/reporting/reports/replenishment/weekly`

**Headers**
- `Authorization`

**Body**

```json
{
  "weekId": "2026-W17",
  "format": "CSV",
  "idempotencyKey": "hu3-replenishment-w17-001"
}
```

**Resultado esperado**
- La respuesta trae:
  - `message`
  - `reportType`
  - `execution`
  - `artifact`

Guarda:
- `execution.executionId`
- `artifact.artifactId` si viene generado

### 3. Consultar la ejecucion semanal

**Endpoint**
- `GET /api/v1/reporting/weekly-executions/{executionId}`

**Headers**
- `Authorization`

### 4. Consultar artefactos de la ejecucion

**Endpoint**
- `GET /api/v1/reporting/weekly-executions/{executionId}/artifacts?page=0&size=20`

**Headers**
- `Authorization`

---

## 7. HU4 - Registrar una orden de compra

**Objetivo**
Crear una compra con multiples productos, validacion de stock y resumen final.

## Orden de ejecucion

### 1. Registrar orden desde flujo de negocio

**Endpoint**
- `POST /api/v1/orders/registrations`

**Headers**
- `Authorization`

**Body**

```json
{
  "userId": null,
  "checkoutCorrelationId": null,
  "addressId": "3c43c7db-bf2c-4f0a-bf63-8e0f7d8a1e9a",
  "countryCode": "CO",
  "items": [
    {
      "variantId": "f5d14d62-7fbd-4b0b-9c97-872c62c0fd8e",
      "sku": "ARK-RDG-K552-RGB",
      "qty": 2,
      "unitPrice": 189900,
      "currency": "COP"
    },
    {
      "variantId": "3b84ec72-acde-4433-a55f-cd5e47d2a8f5",
      "sku": "ARK-KNG-A400-480",
      "qty": 1,
      "unitPrice": 159900,
      "currency": "COP"
    }
  ]
}
```

**Resultado esperado**
- La respuesta trae:
  - `message`
  - `cart`
  - `checkout`
  - `order`

Guarda:
- `cart.cartId`
- `checkout.checkoutCorrelationId`
- `order.orderId`

### 2. Verificar el pedido creado

**Endpoint**
- `GET /api/v1/orders/{orderId}`

**Headers**
- `Authorization`

**Resultado esperado**
- Debe incluir lineas del pedido.
- Cada linea debe traer datos para seguimiento del flujo.

---

## 8. HU5 - Modificar una orden de compra

**Objetivo**
Ajustar un pedido antes del cierre y revalidarlo.

**Precondicion**
- Usa un `orderId` creado en HU4.
- Antes de ejecutar esta HU consulta `GET /api/v1/orders/{orderId}` y copia de una linea:
  - `orderLineId`
  - `variantId`
  - `sku`
  - `reservationId`

## Orden de ejecucion

### 1. Consultar el pedido actual

**Endpoint**
- `GET /api/v1/orders/{orderId}`

**Headers**
- `Authorization`

### 2. Ajustar el pedido

**Endpoint**
- `PUT /api/v1/orders/{orderId}/customer-update`

**Headers**
- `Authorization`

**Body**

```json
{
  "lines": [
    {
      "orderLineId": "REEMPLAZAR_CON_ORDER_LINE_ID",
      "variantId": "f5d14d62-7fbd-4b0b-9c97-872c62c0fd8e",
      "sku": "ARK-RDG-K552-RGB",
      "qty": 1,
      "unitPrice": 189900,
      "currency": "COP",
      "reservationId": "REEMPLAZAR_CON_RESERVATION_ID",
      "reservationConfirmed": true
    }
  ],
  "reason": "Cliente corrige cantidad antes de confirmacion final",
  "revalidateAfterAdjustment": true
}
```

**Resultado esperado**
- La respuesta trae:
  - `message`
  - `revalidated`
  - `order`

### 3. Verificar historial del pedido

**Endpoint**
- `GET /api/v1/orders/{orderId}/history`

**Headers**
- `Authorization`

---

## 9. HU6 - Notificacion de cambio de estado de pedido

**Objetivo**
Actualizar el estado operativo del pedido y confirmar que la notificacion derivada aparece en `notification-service`.

**Precondicion**
- Usa un `orderId` ya existente.

## Orden de ejecucion

### 1. Cambiar el estado y dejar trazabilidad de notificacion

**Endpoint**
- `POST /api/v1/orders/{orderId}/status-notifications`

**Headers**
- `Authorization`

**Body**

```json
{
  "targetStatus": "CONFIRMED",
  "reason": "Pedido aprobado para preparacion logistica"
}
```

**Resultado esperado**
- La respuesta trae:
  - `message`
  - `notificationSourceEventType`
  - `notificationRecipientRefHint`
  - `order`

### 2. Buscar la notificacion generada

**Endpoint**
- `GET /api/v1/notifications?sourceEventType=OrderOperationalStatusUpdated&page=0&size=20`

**Headers**
- `Authorization`

**Resultado esperado**
- Debe aparecer un registro reciente para `6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77`.

### 3. Ver detalle de la notificacion

**Endpoint**
- `GET /api/v1/notifications/{notificationId}/detail`

**Headers**
- `Authorization`

### 4. Ver timeline de la notificacion

**Endpoint**
- `GET /api/v1/notifications/{notificationId}/timeline`

**Headers**
- `Authorization`

---

## 10. HU7 - Generar reportes de ventas semanales

**Objetivo**
Consultar resumen semanal de ventas y generar artefacto exportable.

**Precondicion**
- Idealmente haber ejecutado HU4 al menos una vez en la semana de prueba.

## Orden de ejecucion

### 1. Consultar resumen semanal consolidado

**Endpoint**
- `GET /api/v1/orders/weekly-sales-summary?weekId=2026-W17&limit=5`

**Headers**
- `Authorization`

**Resultado esperado**
- La respuesta trae:
  - `totalSales`
  - `totalOrders`
  - `topProducts`
  - `frequentCustomers`

### 2. Generar reporte semanal de ventas con export

**Endpoint**
- `POST /api/v1/reporting/reports/sales/weekly`

**Headers**
- `Authorization`

**Body**

```json
{
  "weekId": "2026-W17",
  "format": "PDF",
  "idempotencyKey": "hu7-sales-w17-001"
}
```

**Resultado esperado**
- La respuesta trae:
  - `message`
  - `reportType`
  - `execution`
  - `artifact`

### 3. Consultar la ejecucion semanal

**Endpoint**
- `GET /api/v1/reporting/weekly-executions/{executionId}`

**Headers**
- `Authorization`

### 4. Consultar artefactos del reporte

**Endpoint**
- `GET /api/v1/reporting/weekly-executions/{executionId}/artifacts?page=0&size=20`

**Headers**
- `Authorization`

---

## 11. HU8 - Identificar carritos abandonados

**Objetivo**
Listar carritos abandonados y enviar recordatorio.

**Importante**
La API ya esta implementada, pero para probarla de forma real necesitas una de estas dos condiciones:

1. un carrito con `status = ABANDONED`, o
2. un carrito `ACTIVE` o `CHECKOUT_IN_PROGRESS` con mas de 1 hora de inactividad.

Hoy no existe un endpoint publico que fuerce el abandono inmediato. Por eso la prueba 100% por API requiere una precondicion temporal real.

## Flujo recomendado para probarla

### Opcion A. Preparar un carrito y dejarlo inactivo por mas de 1 hora

1. `POST /api/v1/carts`

Body:

```json
{
  "userId": null
}
```

2. `PUT /api/v1/carts/{cartId}/items`

Headers:
- `Authorization`
- `Idempotency-Key: hu8-cart-items-001`

Body:

```json
{
  "items": [
    {
      "operation": "UPSERT",
      "variantId": "f5d14d62-7fbd-4b0b-9c97-872c62c0fd8e",
      "sku": "ARK-RDG-K552-RGB",
      "qty": 1,
      "unitPrice": 189900,
      "currency": "COP",
      "reservationId": null,
      "reservationConfirmed": false
    }
  ]
}
```

3. no completar checkout
4. esperar mas de 1 hora
5. consultar `GET /api/v1/carts/abandoned?inactiveHours=1&limit=20`

### Opcion B. Si ya existe un carrito viejo en el entorno

### 1. Listar carritos abandonados o inferidos

**Endpoint**
- `GET /api/v1/carts/abandoned?inactiveHours=1&limit=20`

**Headers**
- `Authorization`

**Resultado esperado**
- Cada item trae:
  - `cartId`
  - `organizationId`
  - `userId`
  - `status`
  - `inferredAbandoned`
  - `items[]`

### 2. Enviar recordatorio por correo

**Endpoint**
- `POST /api/v1/carts/{cartId}/reminders`

**Headers**
- `Authorization`

**Body**

```json
{
  "channel": "EMAIL",
  "note": "Tu carrito sigue disponible para completar la compra corporativa."
}
```

**Resultado esperado**
- La respuesta trae:
  - `message`
  - `sourceEventType`
  - `channel`
  - `recipientRef`
  - `cartId`

### 3. Validar la notificacion del recordatorio

**Endpoint**
- `GET /api/v1/notifications?sourceEventType=CartAbandonedReminder&page=0&size=20`

**Headers**
- `Authorization`

---

## 12. Checklist corto por HU

- `HU1`: listar marcas -> listar categorias -> registrar producto -> consultar detalle.
- `HU2`: consultar stock item -> ajustar stock -> consultar stock -> revisar movimientos.
- `HU3`: consultar low stock report -> generar reporte semanal de abastecimiento -> consultar ejecucion -> consultar artefactos.
- `HU4`: registrar orden desde flujo integrado -> consultar pedido.
- `HU5`: consultar pedido -> ajustar pedido -> revisar historial.
- `HU6`: actualizar estado con endpoint de notificacion -> buscar notificacion -> consultar detalle/timeline.
- `HU7`: consultar resumen semanal -> generar reporte semanal de ventas -> consultar ejecucion -> consultar artefactos.
- `HU8`: listar carritos abandonados -> enviar recordatorio -> buscar notificacion de recordatorio.

---

## 13. Observaciones practicas

- Los endpoints nuevos fueron pensados para cubrir el backlog sin rehacer el dominio actual.
- `Idempotency-Key` no es `userId`; es una llave unica por operacion mutable.
- Si un Swagger no muestra el boton `Authorize`, abre el Swagger Hub del Gateway, no el puerto interno del microservicio.
- Si una mutacion falla por `401` o `403`, repite el login y asegurate de que el token fue obtenido con:
  - `organizationId = 6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77`
  - `countryCode = CO`
