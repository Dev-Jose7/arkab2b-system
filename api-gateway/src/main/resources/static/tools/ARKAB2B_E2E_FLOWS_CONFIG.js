window.ARKAB2B_E2E_CONFIG = {
  serviceSpecs: {
    iam: "/swagger/iam/v1/api-docs",
    directory: "/swagger/directory/v1/api-docs",
    catalog: "/swagger/catalog/v1/api-docs",
    inventory: "/swagger/inventory/v1/api-docs",
    order: "/swagger/order/v1/api-docs",
    notification: "/swagger/notification/v1/api-docs",
    reporting: "/swagger/reporting/v1/api-docs"
  },
  serviceLabels: {
    iam: "Identity Access",
    directory: "Directory",
    catalog: "Catalog",
    inventory: "Inventory",
    order: "Order",
    notification: "Notification",
    reporting: "Reporting"
  },
  categories: [
    {
      id: "functional",
      label: "Funcionales de negocio",
      description: "Flujos CUF de Producto, modelados por Dominio y ejecutados en arquitectura actual"
    },
    {
      id: "transversal",
      label: "Transversales de arquitectura",
      description: "Seguridad, aislamiento, consistencia, idempotencia, trazabilidad y operación"
    }
  ],
  flows: [
    {
      id: "E2E-CUF01",
      category: "functional",
      title: "Consultar catálogo vendible",
      objective: "Permitir explorar oferta comercial vendible por organization.",
      services: [
        { service: "iam", reason: "Autenticación y token de actor.", keywords: ["auth", "login", "token", "introspect"] },
        { service: "catalog", reason: "Consulta de oferta/producto/variante vendible.", keywords: ["catalog", "product", "variant", "search", "list", "price", "sellable"] }
      ]
    },
    {
      id: "E2E-CUF02",
      category: "functional",
      title: "Consultar disponibilidad antes de confirmar compra",
      objective: "Validar promesa comercial antes de formalizar pedido.",
      services: [
        { service: "order", reason: "Entrada de validación de checkout.", keywords: ["checkout", "validate", "cart", "order"] },
        { service: "catalog", reason: "Resolución de variante/precio vigente.", keywords: ["resolve", "variant", "price", "sellable"] },
        { service: "inventory", reason: "Disponibilidad/reserva comprometible.", keywords: ["availability", "reservation", "stock", "validate"] },
        { service: "directory", reason: "Contexto organization/country policy.", keywords: ["organization", "policy", "country", "address", "checkout"] }
      ]
    },
    {
      id: "E2E-CUF03",
      category: "functional",
      title: "Gestionar carrito de compra",
      objective: "Crear y ajustar intención de compra antes del pedido.",
      services: [
        { service: "order", reason: "CRUD funcional del carrito.", keywords: ["cart", "item", "create", "adjust", "update", "remove"] }
      ]
    },
    {
      id: "E2E-CUF04",
      category: "functional",
      title: "Crear pedido desde carrito validado",
      objective: "Formalizar pedido con validaciones sync obligatorias.",
      services: [
        { service: "order", reason: "Creación de pedido y transición inicial.", keywords: ["order", "create", "checkout", "cart", "confirm"] },
        { service: "directory", reason: "Resolución de política/contexto regional.", keywords: ["policy", "country", "organization", "address"] },
        { service: "catalog", reason: "Validación de vendibilidad/precio.", keywords: ["resolve", "variant", "price", "sellable"] },
        { service: "inventory", reason: "Validación/reserva para compromiso comercial.", keywords: ["reservation", "availability", "stock", "validate"] },
        { service: "notification", reason: "Derivación no bloqueante post pedido.", keywords: ["notification", "request", "dispatch", "event"] },
        { service: "reporting", reason: "Ingesta derivada del hecho de pedido.", keywords: ["event", "fact", "projection", "checkpoint"] }
      ]
    },
    {
      id: "E2E-CUF05",
      category: "functional",
      title: "Ajustar pedido antes de cierre",
      objective: "Permitir ajustes en ventana válida y bloquear transiciones inválidas.",
      services: [
        { service: "order", reason: "Mutación de pedido antes de cierre.", keywords: ["order", "adjust", "update", "status", "revalidate"] },
        { service: "inventory", reason: "Revalidación de disponibilidad post ajuste.", keywords: ["validate", "reservation", "availability", "stock"] },
        { service: "catalog", reason: "Revalidación comercial cuando cambian ítems.", keywords: ["resolve", "variant", "price"] },
        { service: "directory", reason: "Reglas contextuales/regionales del ajuste.", keywords: ["policy", "country", "organization"] }
      ]
    },
    {
      id: "E2E-CUF06",
      category: "functional",
      title: "Consultar estado del pedido",
      objective: "Exponer estado vigente con contexto operativo.",
      services: [
        { service: "order", reason: "Consulta de estado y detalle vigente.", keywords: ["order", "status", "get", "detail", "query"] }
      ]
    },
    {
      id: "E2E-CUF07",
      category: "functional",
      title: "Actualizar estado operativo del pedido",
      objective: "Registrar transiciones operativas trazables.",
      services: [
        { service: "order", reason: "Mutación de estado operativo.", keywords: ["order", "status", "transition", "update", "operational"] },
        { service: "notification", reason: "Notificación derivada por cambio relevante.", keywords: ["notification", "status", "event", "dispatch"] },
        { service: "reporting", reason: "Proyección de cambios operativos.", keywords: ["event", "fact", "projection", "kpi"] }
      ]
    },
    {
      id: "E2E-CUF08",
      category: "functional",
      title: "Registrar pago manual",
      objective: "Actualizar estado financiero con evidencia verificable.",
      services: [
        { service: "order", reason: "Registro de pago y estado financiero.", keywords: ["payment", "manual", "financial", "order"] },
        { service: "notification", reason: "Comunicación derivada por cambio financiero.", keywords: ["payment", "notification", "event"] },
        { service: "reporting", reason: "Ingesta de evento financiero en proyecciones.", keywords: ["payment", "event", "projection", "sales"] }
      ]
    },
    {
      id: "E2E-CUF09",
      category: "functional",
      title: "Consultar estado financiero del pedido",
      objective: "Permitir visibilidad financiera consistente del pedido.",
      services: [
        { service: "order", reason: "Consulta del estado de pago/financiero.", keywords: ["financial", "payment", "order", "status"] }
      ]
    },
    {
      id: "E2E-CUF10",
      category: "functional",
      title: "Emitir notificación por cambio relevante",
      objective: "Disparar comunicación derivada sin mutar el core.",
      services: [
        { service: "order", reason: "Publica hechos relevantes de pedido.", keywords: ["order", "event", "status", "confirmed"] },
        { service: "inventory", reason: "Publica hechos de stock/reserva relevantes.", keywords: ["stock", "reservation", "event", "expired", "low"] },
        { service: "notification", reason: "Consume, aplica policy/template y dispatch.", keywords: ["notification", "dispatch", "request", "attempt", "callback"] }
      ]
    },
    {
      id: "E2E-CUF11",
      category: "functional",
      title: "Consultar historial de cambios relevantes",
      objective: "Exponer timeline verificable de cambios confirmados.",
      services: [
        { service: "order", reason: "Historial de cambios del pedido.", keywords: ["history", "order", "status", "change", "timeline"] },
        { service: "notification", reason: "Entrega de comunicación asociada a cambios.", keywords: ["notification", "delivery", "audit", "attempt"] }
      ]
    },
    {
      id: "E2E-CUF12",
      category: "functional",
      title: "Actualizar stock operativo",
      objective: "Registrar cambios de stock alineados con operación real.",
      services: [
        { service: "inventory", reason: "Operaciones de ajuste/ledger de stock.", keywords: ["stock", "adjust", "increase", "decrease", "initialize", "ledger"] },
        { service: "reporting", reason: "Impacto derivado en abastecimiento/KPI.", keywords: ["stock", "replenishment", "event", "projection"] }
      ]
    },
    {
      id: "E2E-CUF13",
      category: "functional",
      title: "Recalcular disponibilidad comprometible",
      objective: "Mantener promesa comercial coherente tras cambios de stock/reserva.",
      services: [
        { service: "inventory", reason: "Recalcula disponibilidad comprometible.", keywords: ["availability", "commitable", "reservation", "recalculate", "stock"] },
        { service: "order", reason: "Consume disponibilidad en checkout/pedido.", keywords: ["checkout", "validate", "reservation", "availability"] }
      ]
    },
    {
      id: "E2E-CUF14",
      category: "functional",
      title: "Generar reporte semanal de ventas",
      objective: "Consolidar snapshot semanal comercial para decisión.",
      services: [
        { service: "reporting", reason: "Generación y consulta de reporte semanal de ventas.", keywords: ["weekly", "sales", "report", "generate", "artifact", "kpi"] },
        { service: "order", reason: "Hechos de pedidos/pagos para consolidación.", keywords: ["order", "payment", "event", "status"] },
        { service: "catalog", reason: "Contexto comercial de oferta/precio.", keywords: ["price", "product", "variant", "event"] }
      ]
    },
    {
      id: "E2E-CUF15",
      category: "functional",
      title: "Generar reporte semanal de reposición",
      objective: "Consolidar necesidad de abastecimiento semanal.",
      services: [
        { service: "reporting", reason: "Generación y consulta de reporte de reposición.", keywords: ["weekly", "replenishment", "report", "generate", "artifact"] },
        { service: "inventory", reason: "Hechos de stock/reserva para riesgo de abastecimiento.", keywords: ["stock", "reservation", "low", "event", "availability"] }
      ]
    },
    {
      id: "E2E-CUF16",
      category: "functional",
      title: "Configurar política regional aplicable",
      objective: "Definir reglas por país/organización sin romper core.",
      services: [
        { service: "directory", reason: "Alta/actualización de política por país.", keywords: ["policy", "country", "configure", "organization", "regional"] },
        { service: "reporting", reason: "Consumo de contexto regional en consolidación.", keywords: ["country", "policy", "weekly", "report"] }
      ]
    },
    {
      id: "E2E-CUF17",
      category: "functional",
      title: "Aplicar reglas regionales en operación",
      objective: "Ejecutar operaciones sensibles usando country policy vigente.",
      services: [
        { service: "directory", reason: "Resolución de política vigente por countryCode.", keywords: ["policy", "resolve", "country", "organization"] },
        { service: "order", reason: "Bloquea/permite checkout según policy.", keywords: ["checkout", "policy", "country", "order", "validation"] },
        { service: "reporting", reason: "Bloquea/permite generación/consulta según policy.", keywords: ["policy", "country", "weekly", "report", "generate"] }
      ]
    },

    {
      id: "E2E-SEC-01",
      category: "transversal",
      title: "Seguridad de borde JWT",
      objective: "Gateway debe rechazar requests sin token, inválidos o sin permisos.",
      services: [
        { service: "iam", reason: "Emisión/introspección/JWKS para validación JWT.", keywords: ["auth", "login", "refresh", "jwks", "introspect", "token"] },
        { service: "order", reason: "Endpoint protegido para verificar 401/403 en borde.", keywords: ["cart", "order", "checkout"] }
      ]
    },
    {
      id: "E2E-SEC-02",
      category: "transversal",
      title: "Seguridad interna S2S con scopes",
      objective: "EndPoints internos deben requerir token técnico y scope correcto.",
      services: [
        { service: "iam", reason: "Emisión de service token para m2m.", keywords: ["service", "token", "internal", "scope"] },
        { service: "directory", reason: "Endpoints internos de lookup/contexto.", keywords: ["internal", "organization", "policy", "lookup"] },
        { service: "catalog", reason: "Endpoints internos de resolve de variante.", keywords: ["internal", "resolve", "variant", "price"] },
        { service: "inventory", reason: "Endpoints internos de validate/reservation.", keywords: ["internal", "validate", "reservation", "availability"] },
        { service: "reporting", reason: "Ops internas (rebuild/generate) con scopes.", keywords: ["internal", "ops", "rebuild", "generate", "report"] }
      ]
    },
    {
      id: "E2E-ORG-01",
      category: "transversal",
      title: "Aislamiento organization en mutaciones",
      objective: "Actor de org A no puede mutar recursos de org B.",
      services: [
        { service: "order", reason: "Mutaciones de carrito/pedido protegidas por organization.", keywords: ["cart", "order", "create", "update", "organization"] },
        { service: "inventory", reason: "Mutaciones de stock/reserva aisladas por organization.", keywords: ["stock", "reservation", "organization", "adjust"] },
        { service: "catalog", reason: "Mutaciones admin aisladas por organization.", keywords: ["product", "variant", "price", "organization"] },
        { service: "directory", reason: "Contexto/ownership organizacional.", keywords: ["organization", "profile", "address", "contact"] }
      ]
    },
    {
      id: "E2E-ORG-02",
      category: "transversal",
      title: "Aislamiento organization en consultas",
      objective: "Actor de org A no puede leer datos de org B.",
      services: [
        { service: "order", reason: "Consultas de pedidos e historial por organization.", keywords: ["order", "history", "status", "organization"] },
        { service: "reporting", reason: "Consultas de reportes por organization.", keywords: ["report", "weekly", "artifact", "organization"] },
        { service: "directory", reason: "Consultas de perfil/contacto/dirección por organization.", keywords: ["organization", "profile", "contact", "address"] }
      ]
    },
    {
      id: "E2E-REG-01",
      category: "transversal",
      title: "Bloqueo por ausencia de política regional",
      objective: "Sin policy vigente se bloquea operación crítica (sin fallback implícito).",
      services: [
        { service: "directory", reason: "Owner de country policy y su resolución.", keywords: ["policy", "country", "resolve", "organization"] },
        { service: "order", reason: "Checkout/pedido bloqueado por falta de policy.", keywords: ["checkout", "policy", "country", "validation"] },
        { service: "reporting", reason: "Consulta/generación semanal condicionada por policy.", keywords: ["weekly", "report", "country", "policy", "generate"] }
      ]
    },
    {
      id: "E2E-SYNC-01",
      category: "transversal",
      title: "Precondiciones sync obligatorias antes de crear pedido",
      objective: "Order no confirma si falla Directory/Catalog/Inventory.",
      services: [
        { service: "order", reason: "Orquesta validaciones sync del camino crítico.", keywords: ["checkout", "validate", "order", "create"] },
        { service: "directory", reason: "Contexto/policy previa obligatoria.", keywords: ["policy", "address", "checkout", "internal"] },
        { service: "catalog", reason: "Variante/precio vigentes.", keywords: ["resolve", "variant", "price", "internal"] },
        { service: "inventory", reason: "Disponibilidad/reserva válidas.", keywords: ["reservation", "availability", "validate", "internal"] }
      ]
    },
    {
      id: "E2E-ASYNC-01",
      category: "transversal",
      title: "Derivación Core -> Notification no bloqueante",
      objective: "Eventos de core generan notificación sin revertir transacción core.",
      services: [
        { service: "order", reason: "Publica hechos de pedido/pago.", keywords: ["event", "order", "status", "payment", "outbox"] },
        { service: "inventory", reason: "Publica hechos de reserva/stock.", keywords: ["event", "reservation", "stock", "outbox"] },
        { service: "notification", reason: "Consume hechos, crea request e intenta dispatch.", keywords: ["kafka", "event", "request", "dispatch", "attempt", "callback"] }
      ]
    },
    {
      id: "E2E-ASYNC-02",
      category: "transversal",
      title: "Derivación Core -> Reporting idempotente",
      objective: "Reporting consolida hechos de múltiples productores sin duplicar.",
      services: [
        { service: "order", reason: "Fuente de eventos comerciales.", keywords: ["event", "order", "payment", "outbox"] },
        { service: "catalog", reason: "Fuente de eventos de oferta/precio.", keywords: ["event", "price", "product", "variant", "outbox"] },
        { service: "inventory", reason: "Fuente de eventos de stock/reserva.", keywords: ["event", "stock", "reservation", "outbox"] },
        { service: "directory", reason: "Fuente de eventos de contexto regional/organizacional.", keywords: ["event", "organization", "policy", "outbox"] },
        { service: "notification", reason: "Fuente de efectividad de comunicación.", keywords: ["event", "notification", "delivery", "outbox"] },
        { service: "reporting", reason: "Consume/aplica/checkpoint de forma idempotente.", keywords: ["kafka", "fact", "projection", "checkpoint", "processed", "rebuild"] }
      ]
    },
    {
      id: "E2E-ASYNC-03",
      category: "transversal",
      title: "Outbox + retry + DLQ",
      objective: "Fallo de broker/consumo no debe perder decisiones de negocio.",
      services: [
        { service: "order", reason: "Outbox transaccional en comandos de pedido/pago.", keywords: ["outbox", "relay", "retry", "dlq", "event"] },
        { service: "inventory", reason: "Outbox transaccional en cambios de stock/reserva.", keywords: ["outbox", "relay", "retry", "dlq", "event"] },
        { service: "notification", reason: "Retry y reproceso de mensajes fallidos.", keywords: ["retry", "dlq", "reprocess", "dispatch", "event"] },
        { service: "reporting", reason: "Rebuild/reprocess para reconciliación.", keywords: ["rebuild", "reprocess", "checkpoint", "dlq", "event"] }
      ]
    },
    {
      id: "E2E-IDEMP-01",
      category: "transversal",
      title: "Idempotencia de comandos mutantes",
      objective: "Misma Idempotency-Key no duplica efectos de negocio.",
      services: [
        { service: "order", reason: "Comandos de carrito/pedido/pago idempotentes.", keywords: ["idempotency", "cart", "order", "payment", "create", "update"] },
        { service: "inventory", reason: "Ajustes/reservas idempotentes.", keywords: ["idempotency", "stock", "reservation", "adjust"] },
        { service: "notification", reason: "Dispatch/reintentos sin duplicar entrega.", keywords: ["idempotency", "dispatch", "attempt", "retry"] },
        { service: "reporting", reason: "Generación/rebuild sin duplicar artifacts.", keywords: ["idempotency", "generate", "weekly", "report", "rebuild"] }
      ]
    },
    {
      id: "E2E-IDEMP-02",
      category: "transversal",
      title: "Idempotencia de consumo de eventos",
      objective: "Replay de eventId ya procesado debe ser noop.",
      services: [
        { service: "notification", reason: "Dedupe de eventos upstream.", keywords: ["processed", "event", "dedupe", "replay", "consumer"] },
        { service: "reporting", reason: "Dedupe en facts/proyecciones/checkpoints.", keywords: ["processed", "event", "dedupe", "checkpoint", "fact"] },
        { service: "inventory", reason: "Consumo idempotente de eventos de catálogo.", keywords: ["event", "dedupe", "catalog", "consumer"] },
        { service: "directory", reason: "Consumo idempotente de eventos IAM.", keywords: ["event", "processed", "consumer", "iam"] }
      ]
    },
    {
      id: "E2E-TRACE-01",
      category: "transversal",
      title: "Trazabilidad técnica completa",
      objective: "Cadena request->db->outbox->evento->consumidor con metadata obligatoria.",
      services: [
        { service: "order", reason: "Mutaciones core con correlación completa.", keywords: ["trace", "correlation", "audit", "outbox", "event"] },
        { service: "notification", reason: "Trazabilidad de request/attempt/callback.", keywords: ["trace", "correlation", "audit", "attempt", "callback"] },
        { service: "reporting", reason: "Trazabilidad de consumo/apply/checkpoint.", keywords: ["trace", "correlation", "fact", "checkpoint", "event"] }
      ]
    },
    {
      id: "E2E-ERR-01",
      category: "transversal",
      title: "Error canónico estable",
      objective: "Errores de negocio/seguridad con semántica HTTP consistente.",
      services: [
        { service: "order", reason: "Errores de checkout/pedido/pago.", keywords: ["error", "conflict", "checkout", "order", "status"] },
        { service: "inventory", reason: "Errores de stock/reserva/disponibilidad.", keywords: ["error", "stock", "reservation", "availability", "conflict"] },
        { service: "directory", reason: "Errores de policy/contexto regional.", keywords: ["error", "policy", "country", "organization"] },
        { service: "iam", reason: "Errores de authn/authz/introspect.", keywords: ["auth", "forbidden", "unauthorized", "token", "introspect"] }
      ]
    },
    {
      id: "E2E-PERF-01",
      category: "transversal",
      title: "Baseline de latencia core",
      objective: "Medir p95 en flujos críticos de catálogo/checkout/pedido.",
      services: [
        { service: "catalog", reason: "Latencia de consulta/resolución comercial.", keywords: ["search", "catalog", "resolve", "variant", "price"] },
        { service: "directory", reason: "Latencia de resolución policy/contexto.", keywords: ["policy", "country", "organization", "lookup"] },
        { service: "inventory", reason: "Latencia de disponibilidad/reserva.", keywords: ["availability", "reservation", "validate", "stock"] },
        { service: "order", reason: "Latencia de checkout/creación de pedido.", keywords: ["checkout", "cart", "order", "create", "validate"] }
      ]
    },
    {
      id: "E2E-AVAIL-01",
      category: "transversal",
      title: "Disponibilidad operativa del stack",
      objective: "Verificar health/readiness y smoke de integración multi-servicio.",
      services: [
        { service: "iam", reason: "Autenticación y tokens como precondición de flujo.", keywords: ["health", "readiness", "auth", "token"] },
        { service: "directory", reason: "Contexto organizacional/política disponible.", keywords: ["health", "readiness", "policy", "organization"] },
        { service: "catalog", reason: "Oferta vendible disponible.", keywords: ["health", "readiness", "catalog", "variant"] },
        { service: "inventory", reason: "Disponibilidad y reservas operativas.", keywords: ["health", "readiness", "stock", "reservation"] },
        { service: "order", reason: "Checkout/pedido operativos.", keywords: ["health", "readiness", "checkout", "order"] },
        { service: "notification", reason: "Pipeline derivado de notificación disponible.", keywords: ["health", "readiness", "notification", "dispatch"] },
        { service: "reporting", reason: "Pipeline derivado de reporting disponible.", keywords: ["health", "readiness", "report", "checkpoint"] }
      ]
    }
  ]
};
