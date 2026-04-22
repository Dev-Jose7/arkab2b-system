(function () {
  const p = (service, method, path, note) => ({ service, method, path, note });

  window.ARKAB2B_E2E_EXECUTION_PLAN = {
    "E2E-CUF01": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("iam", "POST", "/api/v1/auth/introspect", "Validar token activo"),
      p("catalog", "GET", "/api/v1/catalog/search", "Consultar catálogo vendible"),
      p("catalog", "GET", "/api/v1/catalog/products/{productId}/variants", "Revisar variantes del producto"),
      p("catalog", "GET", "/api/v1/catalog/variants/{variantId}/prices/current", "Validar precio vigente")
    ],
    "E2E-CUF02": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "POST", "/api/v1/carts", "Crear carrito de validación"),
      p("order", "PUT", "/api/v1/carts/{cartId}/items", "Agregar ítems"),
      p("order", "POST", "/api/v1/carts/{cartId}/checkout-validation", "Disparar validación de checkout"),
      p("directory", "GET", "/api/v1/internal/organizations/{organizationId}/addresses/{addressId}/checkout-resolution", "Resolver dirección/política"),
      p("catalog", "GET", "/api/v1/internal/catalog/checkout/variant-resolution", "Resolver variante/precio"),
      p("inventory", "GET", "/api/v1/checkout/availability", "Comprobar disponibilidad comprometible")
    ],
    "E2E-CUF03": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "POST", "/api/v1/carts", "Crear carrito"),
      p("order", "PUT", "/api/v1/carts/{cartId}/items", "Agregar/ajustar ítems"),
      p("order", "GET", "/api/v1/carts/{cartId}", "Consultar carrito actualizado"),
      p("order", "GET", "/api/v1/carts/active", "Consultar carrito activo")
    ],
    "E2E-CUF04": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "POST", "/api/v1/carts", "Crear carrito"),
      p("order", "PUT", "/api/v1/carts/{cartId}/items", "Agregar ítems"),
      p("order", "POST", "/api/v1/carts/{cartId}/checkout-validation", "Validar checkout"),
      p("order", "POST", "/api/v1/orders", "Crear pedido"),
      p("order", "GET", "/api/v1/orders/{orderId}", "Consultar pedido creado"),
      p("notification", "GET", "/api/v1/notifications", "Verificar derivación de notificación"),
      p("reporting", "GET", "/api/v1/reporting/facts", "Verificar ingesta en reporting")
    ],
    "E2E-CUF05": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "POST", "/api/v1/orders/{orderId}/adjustments", "Ajustar pedido"),
      p("order", "POST", "/api/v1/orders/{orderId}/revalidate", "Revalidar consistencia"),
      p("inventory", "GET", "/api/v1/checkout/availability", "Revalidar disponibilidad"),
      p("order", "GET", "/api/v1/orders/{orderId}", "Consultar pedido ajustado")
    ],
    "E2E-CUF06": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "GET", "/api/v1/orders/{orderId}", "Consultar estado vigente")
    ],
    "E2E-CUF07": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "POST", "/api/v1/orders/{orderId}/status", "Actualizar estado operativo"),
      p("order", "GET", "/api/v1/orders/{orderId}/history", "Verificar historial"),
      p("notification", "GET", "/api/v1/notifications/{notificationId}/timeline", "Verificar timeline notificación")
    ],
    "E2E-CUF08": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "POST", "/api/v1/orders/{orderId}/payments/manual", "Registrar pago manual"),
      p("order", "GET", "/api/v1/orders/{orderId}/financial-status", "Consultar estado financiero"),
      p("order", "GET", "/api/v1/orders/{orderId}/payments/manual", "Consultar pagos manuales"),
      p("notification", "GET", "/api/v1/notifications", "Verificar evento derivado")
    ],
    "E2E-CUF09": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "GET", "/api/v1/orders/{orderId}/financial-status", "Consultar estado financiero")
    ],
    "E2E-CUF10": [
      p("order", "POST", "/api/v1/orders/{orderId}/status", "Generar cambio relevante"),
      p("notification", "GET", "/api/v1/notifications", "Localizar solicitud derivada"),
      p("notification", "POST", "/api/v1/notifications/{notificationId}/dispatch", "Despachar notificación"),
      p("notification", "POST", "/api/v1/notifications/provider-callbacks", "Registrar callback del provider")
    ],
    "E2E-CUF11": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "GET", "/api/v1/orders/{orderId}/history", "Consultar historial de pedido"),
      p("notification", "GET", "/api/v1/notifications/{notificationId}/timeline", "Consultar timeline de comunicación"),
      p("notification", "GET", "/api/v1/notifications/{notificationId}/attempts", "Consultar intentos de entrega")
    ],
    "E2E-CUF12": [
      p("iam", "POST", "/api/v1/internal/auth/service-token", "Obtener token técnico"),
      p("inventory", "POST", "/api/v1/stock-items/{stockItemId}/stock-adjustments", "Ajustar stock operativo"),
      p("inventory", "GET", "/api/v1/stock-items/{stockItemId}", "Consultar estado del stock item"),
      p("inventory", "GET", "/api/v1/stock-items/{stockItemId}/movements", "Consultar ledger de movimientos")
    ],
    "E2E-CUF13": [
      p("inventory", "POST", "/api/v1/stock-items/{stockItemId}/availability/recalculate", "Recalcular disponibilidad"),
      p("inventory", "GET", "/api/v1/availability", "Consultar disponibilidad comprometible"),
      p("order", "POST", "/api/v1/carts/{cartId}/checkout-validation", "Validar impacto en checkout")
    ],
    "E2E-CUF14": [
      p("reporting", "POST", "/api/v1/reporting/weekly-executions/sales", "Generar corte semanal de ventas"),
      p("reporting", "GET", "/api/v1/reporting/weekly-executions/{executionId}", "Consultar ejecución semanal"),
      p("reporting", "POST", "/api/v1/reporting/weekly-executions/{executionId}/artifacts", "Generar artifact"),
      p("reporting", "GET", "/api/v1/reporting/projections/sales", "Consultar proyección de ventas")
    ],
    "E2E-CUF15": [
      p("reporting", "POST", "/api/v1/reporting/weekly-executions/replenishment", "Generar corte de reposición"),
      p("reporting", "GET", "/api/v1/reporting/weekly-executions/{executionId}", "Consultar ejecución"),
      p("reporting", "GET", "/api/v1/reporting/projections/replenishment", "Consultar proyección de reposición")
    ],
    "E2E-CUF16": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor de operación"),
      p("directory", "POST", "/api/v1/organizations/{organizationId}/country-policies", "Crear política regional"),
      p("directory", "POST", "/api/v1/organizations/{organizationId}/country-policies/{countryCode}/apply", "Aplicar política regional"),
      p("directory", "GET", "/api/v1/organizations/{organizationId}/country-policies/{countryCode}", "Consultar política vigente")
    ],
    "E2E-CUF17": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("directory", "GET", "/api/v1/organizations/{organizationId}/regional-context/{countryCode}", "Resolver contexto regional"),
      p("order", "POST", "/api/v1/carts/{cartId}/checkout-validation", "Aplicar regla regional en checkout"),
      p("reporting", "POST", "/api/v1/reporting/weekly-executions/sales", "Aplicar regla regional en reporte")
    ],

    "E2E-SEC-01": [
      p("iam", "POST", "/api/v1/auth/login", "Obtener JWT"),
      p("iam", "POST", "/api/v1/auth/introspect", "Introspección de token"),
      p("iam", "GET", "/.well-known/jwks.json", "Validar JWKS"),
      p("order", "POST", "/api/v1/carts", "Probar endpoint protegido")
    ],
    "E2E-SEC-02": [
      p("iam", "POST", "/api/v1/internal/auth/service-token", "Emitir token técnico S2S"),
      p("directory", "GET", "/api/v1/internal/organizations/{organizationId}/regional-context/{countryCode}", "Validar scope interno en directory"),
      p("catalog", "GET", "/api/v1/internal/catalog/checkout/variant-resolution", "Validar scope interno en catalog"),
      p("inventory", "GET", "/api/v1/internal/reservations/{reservationId}/validation", "Validar scope interno en inventory"),
      p("reporting", "POST", "/api/v1/reporting/rebuild", "Validar scope interno en reporting ops")
    ],
    "E2E-ORG-01": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "POST", "/api/v1/orders/{orderId}/adjustments", "Intentar mutación cross-organization"),
      p("inventory", "POST", "/api/v1/stock-items/{stockItemId}/stock-adjustments", "Intentar ajuste cross-organization"),
      p("catalog", "PUT", "/api/v1/catalog/products/{productId}", "Intentar edición cross-organization"),
      p("directory", "PUT", "/api/v1/organizations/{organizationId}/legal-profile", "Intentar actualización cross-organization")
    ],
    "E2E-ORG-02": [
      p("iam", "POST", "/api/v1/auth/login", "Autenticar actor"),
      p("order", "GET", "/api/v1/orders/{orderId}", "Intentar consulta cross-organization"),
      p("reporting", "GET", "/api/v1/reporting/projections/sales", "Intentar lectura cross-organization"),
      p("directory", "GET", "/api/v1/organizations/{organizationId}/profile", "Intentar lectura de perfil ajeno")
    ],
    "E2E-REG-01": [
      p("directory", "GET", "/api/v1/internal/organizations/{organizationId}/country-policies/{countryCode}", "Verificar política regional"),
      p("order", "POST", "/api/v1/carts/{cartId}/checkout-validation", "Bloqueo por ausencia de política"),
      p("reporting", "POST", "/api/v1/reporting/weekly-executions/sales", "Bloqueo en reporting por política")
    ],
    "E2E-SYNC-01": [
      p("order", "POST", "/api/v1/carts/{cartId}/checkout-validation", "Iniciar validación sync"),
      p("directory", "GET", "/api/v1/internal/organizations/{organizationId}/addresses/{addressId}/checkout-resolution", "Resolver contexto en directory"),
      p("catalog", "GET", "/api/v1/internal/catalog/checkout/variant-resolution", "Resolver variante/precio en catalog"),
      p("inventory", "GET", "/api/v1/checkout/availability", "Validar disponibilidad en inventory"),
      p("order", "POST", "/api/v1/orders", "Crear pedido solo si todo valida")
    ],
    "E2E-ASYNC-01": [
      p("order", "POST", "/api/v1/orders/{orderId}/status", "Publicar evento de pedido"),
      p("inventory", "POST", "/api/v1/reservations/expire", "Publicar evento de inventario"),
      p("notification", "GET", "/api/v1/notifications", "Consumir y materializar solicitud"),
      p("notification", "GET", "/api/v1/notifications/{notificationId}/attempts", "Verificar intentos derivados")
    ],
    "E2E-ASYNC-02": [
      p("order", "POST", "/api/v1/orders/{orderId}/status", "Generar hecho de pedido"),
      p("catalog", "PUT", "/api/v1/catalog/prices/{priceId}", "Generar hecho comercial"),
      p("inventory", "POST", "/api/v1/stock-items/{stockItemId}/stock-adjustments", "Generar hecho de stock"),
      p("directory", "POST", "/api/v1/organizations/{organizationId}/country-policies/{countryCode}/apply", "Generar hecho regional"),
      p("notification", "POST", "/api/v1/notifications/provider-callbacks", "Generar hecho de entrega"),
      p("reporting", "POST", "/api/v1/reporting/facts", "Ingestar hecho"),
      p("reporting", "POST", "/api/v1/reporting/facts/{factId}/apply", "Aplicar hecho"),
      p("reporting", "GET", "/api/v1/reporting/projections/kpis", "Verificar proyección consolidada")
    ],
    "E2E-ASYNC-03": [
      p("order", "GET", "/api/v1/orders/{orderId}/audit", "Verificar trazas de evento en core"),
      p("notification", "POST", "/api/v1/notifications/{notificationId}/reprocess-dlq", "Reprocesar DLQ de notificación"),
      p("notification", "POST", "/api/v1/notifications/{notificationId}/retry", "Reintentar entrega"),
      p("reporting", "POST", "/api/v1/reporting/reprocess-dlq", "Reprocesar DLQ de reporting"),
      p("reporting", "POST", "/api/v1/reporting/rebuild", "Reconstruir proyecciones")
    ],
    "E2E-IDEMP-01": [
      p("order", "POST", "/api/v1/carts", "Reintentar creación de carrito con misma idempotency key"),
      p("order", "POST", "/api/v1/orders", "Reintentar creación de pedido"),
      p("inventory", "POST", "/api/v1/stock-items/{stockItemId}/stock-adjustments", "Reintentar ajuste de stock"),
      p("notification", "POST", "/api/v1/notifications/{notificationId}/dispatch", "Reintentar dispatch controlado"),
      p("reporting", "POST", "/api/v1/reporting/weekly-executions/sales", "Reintentar generación semanal")
    ],
    "E2E-IDEMP-02": [
      p("notification", "POST", "/api/v1/notifications/{notificationId}/reprocess-dlq", "Replay controlado en notification"),
      p("reporting", "POST", "/api/v1/reporting/reprocess-dlq", "Replay controlado en reporting"),
      p("inventory", "POST", "/api/v1/reservations/expire", "Replay de expiración"),
      p("directory", "GET", "/api/v1/internal/organizations/{organizationId}/regional-context/{countryCode}", "Verificar dedupe sin side effects")
    ],
    "E2E-TRACE-01": [
      p("order", "POST", "/api/v1/orders", "Mutación core con trazabilidad"),
      p("order", "GET", "/api/v1/orders/{orderId}/audit", "Verificar auditoría core"),
      p("notification", "GET", "/api/v1/notifications/audits", "Verificar auditoría de notificación"),
      p("reporting", "GET", "/api/v1/reporting/audits", "Verificar auditoría de reporting")
    ],
    "E2E-ERR-01": [
      p("iam", "POST", "/api/v1/auth/introspect", "Semántica de error de auth"),
      p("directory", "GET", "/api/v1/organizations/{organizationId}/country-policies/{countryCode}", "Semántica de error regional"),
      p("inventory", "GET", "/api/v1/checkout/availability", "Semántica de error de disponibilidad"),
      p("order", "POST", "/api/v1/carts/{cartId}/checkout-validation", "Semántica de error de checkout")
    ],
    "E2E-PERF-01": [
      p("catalog", "GET", "/api/v1/catalog/search", "Punto de medición de catálogo"),
      p("directory", "GET", "/api/v1/internal/organizations/{organizationId}/regional-context/{countryCode}", "Punto de medición de contexto regional"),
      p("inventory", "GET", "/api/v1/checkout/availability", "Punto de medición de disponibilidad"),
      p("order", "POST", "/api/v1/carts/{cartId}/checkout-validation", "Punto de medición de checkout"),
      p("order", "POST", "/api/v1/orders", "Punto de medición de creación de pedido")
    ],
    "E2E-AVAIL-01": [
      p("iam", "POST", "/api/v1/auth/login", "Comprobar operación de IAM"),
      p("directory", "GET", "/api/v1/internal/organizations/{organizationId}/regional-context/{countryCode}", "Comprobar operación de directory"),
      p("catalog", "GET", "/api/v1/internal/catalog/checkout/variant-resolution", "Comprobar operación de catalog"),
      p("inventory", "GET", "/api/v1/availability", "Comprobar operación de inventory"),
      p("order", "GET", "/api/v1/carts/active", "Comprobar operación de order"),
      p("notification", "GET", "/api/v1/notifications/metrics", "Comprobar operación de notification"),
      p("reporting", "GET", "/api/v1/reporting/metrics", "Comprobar operación de reporting")
    ]
  };
})();
