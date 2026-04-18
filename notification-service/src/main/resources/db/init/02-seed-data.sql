INSERT INTO channel_policies (
    policy_id,
    organization_id,
    source_event_type,
    primary_channel,
    fallback_channel,
    max_attempts,
    retry_interval_seconds,
    active,
    created_at,
    updated_at
)
VALUES
    ('policy-order-confirmed', 'organization-demo', 'order.confirmed', 'EMAIL', 'SMS', 3, 300, TRUE, NOW(), NOW()),
    ('policy-order-status-changed', 'organization-demo', 'order.status-changed', 'EMAIL', 'WHATSAPP', 3, 180, TRUE, NOW(), NOW()),
    ('policy-low-stock-detected', 'organization-demo', 'inventory.low-stock-detected', 'IN_APP', 'EMAIL', 2, 120, TRUE, NOW(), NOW())
ON CONFLICT (organization_id, source_event_type) DO UPDATE
SET primary_channel = EXCLUDED.primary_channel,
    fallback_channel = EXCLUDED.fallback_channel,
    max_attempts = EXCLUDED.max_attempts,
    retry_interval_seconds = EXCLUDED.retry_interval_seconds,
    active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at;

INSERT INTO channel_policies (
    policy_id,
    organization_id,
    source_event_type,
    primary_channel,
    fallback_channel,
    max_attempts,
    retry_interval_seconds,
    active,
    created_at,
    updated_at
)
VALUES
    ('policy-order-confirmed-ph6', 'organization-phase6', 'order.confirmed', 'EMAIL', 'SMS', 3, 300, TRUE, NOW(), NOW()),
    ('policy-order-status-changed-ph6', 'organization-phase6', 'order.status-changed', 'EMAIL', 'WHATSAPP', 3, 180, TRUE, NOW(), NOW()),
    ('policy-low-stock-detected-ph6', 'organization-phase6', 'inventory.low-stock-detected', 'IN_APP', 'EMAIL', 2, 120, TRUE, NOW(), NOW())
ON CONFLICT (organization_id, source_event_type) DO UPDATE
SET primary_channel = EXCLUDED.primary_channel,
    fallback_channel = EXCLUDED.fallback_channel,
    max_attempts = EXCLUDED.max_attempts,
    retry_interval_seconds = EXCLUDED.retry_interval_seconds,
    active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at;

INSERT INTO channel_policies (
    policy_id,
    organization_id,
    source_event_type,
    primary_channel,
    fallback_channel,
    max_attempts,
    retry_interval_seconds,
    active,
    created_at,
    updated_at
)
VALUES
    ('policy-order-created-v1', 'organization-demo', 'OrderCreatedFromValidatedCart', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-order-status-updated-v1', 'organization-demo', 'OrderOperationalStatusUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-order-financial-updated-v1', 'organization-demo', 'OrderFinancialStatusUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-manual-payment-v1', 'organization-demo', 'ManualPaymentRegistered', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-order-adjusted-v1', 'organization-demo', 'OrderAdjustedBeforeClose', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-order-revalidated-v1', 'organization-demo', 'OrderConsistencyRevalidated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-cart-created-v1', 'organization-demo', 'CartCreated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-cart-adjusted-v1', 'organization-demo', 'CartItemsAdjusted', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-checkout-validated-v1', 'organization-demo', 'CheckoutAvailabilityValidated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-stock-updated-v1', 'organization-demo', 'StockUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-availability-recalculated-v1', 'organization-demo', 'CommitableAvailabilityRecalculated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW())
ON CONFLICT (organization_id, source_event_type) DO UPDATE
SET primary_channel = EXCLUDED.primary_channel,
    fallback_channel = EXCLUDED.fallback_channel,
    max_attempts = EXCLUDED.max_attempts,
    retry_interval_seconds = EXCLUDED.retry_interval_seconds,
    active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at;

INSERT INTO channel_policies (
    policy_id,
    organization_id,
    source_event_type,
    primary_channel,
    fallback_channel,
    max_attempts,
    retry_interval_seconds,
    active,
    created_at,
    updated_at
)
VALUES
    ('policy-order-created-v1-ph6', 'organization-phase6', 'OrderCreatedFromValidatedCart', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-order-status-v1-ph6', 'organization-phase6', 'OrderOperationalStatusUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-order-fin-v1-ph6', 'organization-phase6', 'OrderFinancialStatusUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-manual-pay-v1-ph6', 'organization-phase6', 'ManualPaymentRegistered', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-order-adjust-v1-ph6', 'organization-phase6', 'OrderAdjustedBeforeClose', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-order-revalid-v1-ph6', 'organization-phase6', 'OrderConsistencyRevalidated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-cart-created-v1-ph6', 'organization-phase6', 'CartCreated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-cart-adjusted-v1-ph6', 'organization-phase6', 'CartItemsAdjusted', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-checkout-val-v1-ph6', 'organization-phase6', 'CheckoutAvailabilityValidated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-stock-updated-v1-ph6', 'organization-phase6', 'StockUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('policy-avail-recalc-v1-ph6', 'organization-phase6', 'CommitableAvailabilityRecalculated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW())
ON CONFLICT (organization_id, source_event_type) DO UPDATE
SET primary_channel = EXCLUDED.primary_channel,
    fallback_channel = EXCLUDED.fallback_channel,
    max_attempts = EXCLUDED.max_attempts,
    retry_interval_seconds = EXCLUDED.retry_interval_seconds,
    active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at;

INSERT INTO notification_templates (
    template_id,
    organization_id,
    source_event_type,
    channel,
    template_version,
    subject_template,
    body_template,
    active,
    created_at,
    updated_at
)
VALUES
    (
        'tpl-order-confirmed-email-v1',
        'organization-demo',
        'order.confirmed',
        'EMAIL',
        1,
        'Pedido confirmado',
        'Tu pedido fue confirmado correctamente.',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'tpl-order-status-changed-email-v1',
        'organization-demo',
        'order.status-changed',
        'EMAIL',
        1,
        'Actualizacion de pedido',
        'Tu pedido tiene un nuevo estado.',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'tpl-low-stock-inapp-v1',
        'organization-demo',
        'inventory.low-stock-detected',
        'IN_APP',
        1,
        'Alerta de bajo stock',
        'Se detecto bajo stock para un SKU monitoreado.',
        TRUE,
        NOW(),
        NOW()
    )
ON CONFLICT (organization_id, source_event_type, channel, template_version) DO UPDATE
SET subject_template = EXCLUDED.subject_template,
    body_template = EXCLUDED.body_template,
    active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at;

INSERT INTO notification_templates (
    template_id,
    organization_id,
    source_event_type,
    channel,
    template_version,
    subject_template,
    body_template,
    active,
    created_at,
    updated_at
)
VALUES
    (
        'tpl-order-confirmed-email-ph6',
        'organization-phase6',
        'order.confirmed',
        'EMAIL',
        1,
        'Pedido confirmado',
        'Tu pedido fue confirmado correctamente.',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'tpl-order-status-email-ph6',
        'organization-phase6',
        'order.status-changed',
        'EMAIL',
        1,
        'Actualizacion de pedido',
        'Tu pedido tiene un nuevo estado.',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'tpl-low-stock-inapp-ph6',
        'organization-phase6',
        'inventory.low-stock-detected',
        'IN_APP',
        1,
        'Alerta de bajo stock',
        'Se detecto bajo stock para un SKU monitoreado.',
        TRUE,
        NOW(),
        NOW()
    )
ON CONFLICT (organization_id, source_event_type, channel, template_version) DO UPDATE
SET subject_template = EXCLUDED.subject_template,
    body_template = EXCLUDED.body_template,
    active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at;

INSERT INTO notification_templates (
    template_id,
    organization_id,
    source_event_type,
    channel,
    template_version,
    subject_template,
    body_template,
    active,
    created_at,
    updated_at
)
VALUES
    ('tpl-order-created-v1', 'organization-demo', 'OrderCreatedFromValidatedCart', 'EMAIL', 1, 'Pedido creado', 'Se formalizo un pedido para tu organizacion.', TRUE, NOW(), NOW()),
    ('tpl-order-status-v1', 'organization-demo', 'OrderOperationalStatusUpdated', 'EMAIL', 1, 'Estado operativo actualizado', 'El pedido tuvo una transicion operativa.', TRUE, NOW(), NOW()),
    ('tpl-order-financial-v1', 'organization-demo', 'OrderFinancialStatusUpdated', 'EMAIL', 1, 'Estado financiero actualizado', 'El estado financiero del pedido cambio.', TRUE, NOW(), NOW()),
    ('tpl-manual-payment-v1', 'organization-demo', 'ManualPaymentRegistered', 'EMAIL', 1, 'Pago manual registrado', 'Se registro un pago manual en un pedido.', TRUE, NOW(), NOW()),
    ('tpl-order-adjusted-v1', 'organization-demo', 'OrderAdjustedBeforeClose', 'EMAIL', 1, 'Pedido ajustado', 'El pedido fue ajustado antes del cierre.', TRUE, NOW(), NOW()),
    ('tpl-order-revalidated-v1', 'organization-demo', 'OrderConsistencyRevalidated', 'EMAIL', 1, 'Pedido revalidado', 'Se revalido la consistencia del pedido.', TRUE, NOW(), NOW()),
    ('tpl-cart-created-v1', 'organization-demo', 'CartCreated', 'EMAIL', 1, 'Carrito creado', 'Se creo un carrito para una nueva compra.', TRUE, NOW(), NOW()),
    ('tpl-cart-adjusted-v1', 'organization-demo', 'CartItemsAdjusted', 'EMAIL', 1, 'Carrito actualizado', 'Los items del carrito fueron ajustados.', TRUE, NOW(), NOW()),
    ('tpl-checkout-validated-v1', 'organization-demo', 'CheckoutAvailabilityValidated', 'EMAIL', 1, 'Checkout validado', 'La validacion de checkout fue ejecutada.', TRUE, NOW(), NOW()),
    ('tpl-stock-updated-v1', 'organization-demo', 'StockUpdated', 'EMAIL', 1, 'Stock actualizado', 'Se actualizo el stock operativo de inventario.', TRUE, NOW(), NOW()),
    ('tpl-availability-recalc-v1', 'organization-demo', 'CommitableAvailabilityRecalculated', 'EMAIL', 1, 'Disponibilidad recalculada', 'Se recalculo la disponibilidad comprometible.', TRUE, NOW(), NOW())
ON CONFLICT (organization_id, source_event_type, channel, template_version) DO UPDATE
SET subject_template = EXCLUDED.subject_template,
    body_template = EXCLUDED.body_template,
    active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at;

INSERT INTO notification_templates (
    template_id,
    organization_id,
    source_event_type,
    channel,
    template_version,
    subject_template,
    body_template,
    active,
    created_at,
    updated_at
)
VALUES
    ('tpl-order-created-v1-ph6', 'organization-phase6', 'OrderCreatedFromValidatedCart', 'EMAIL', 1, 'Pedido creado', 'Se formalizo un pedido para tu organizacion.', TRUE, NOW(), NOW()),
    ('tpl-order-status-v1-ph6', 'organization-phase6', 'OrderOperationalStatusUpdated', 'EMAIL', 1, 'Estado operativo actualizado', 'El pedido tuvo una transicion operativa.', TRUE, NOW(), NOW()),
    ('tpl-order-fin-v1-ph6', 'organization-phase6', 'OrderFinancialStatusUpdated', 'EMAIL', 1, 'Estado financiero actualizado', 'El estado financiero del pedido cambio.', TRUE, NOW(), NOW()),
    ('tpl-manual-pay-v1-ph6', 'organization-phase6', 'ManualPaymentRegistered', 'EMAIL', 1, 'Pago manual registrado', 'Se registro un pago manual en un pedido.', TRUE, NOW(), NOW()),
    ('tpl-order-adjust-v1-ph6', 'organization-phase6', 'OrderAdjustedBeforeClose', 'EMAIL', 1, 'Pedido ajustado', 'El pedido fue ajustado antes del cierre.', TRUE, NOW(), NOW()),
    ('tpl-order-revalid-v1-ph6', 'organization-phase6', 'OrderConsistencyRevalidated', 'EMAIL', 1, 'Pedido revalidado', 'Se revalido la consistencia del pedido.', TRUE, NOW(), NOW()),
    ('tpl-cart-created-v1-ph6', 'organization-phase6', 'CartCreated', 'EMAIL', 1, 'Carrito creado', 'Se creo un carrito para una nueva compra.', TRUE, NOW(), NOW()),
    ('tpl-cart-adjusted-v1-ph6', 'organization-phase6', 'CartItemsAdjusted', 'EMAIL', 1, 'Carrito actualizado', 'Los items del carrito fueron ajustados.', TRUE, NOW(), NOW()),
    ('tpl-checkout-val-v1-ph6', 'organization-phase6', 'CheckoutAvailabilityValidated', 'EMAIL', 1, 'Checkout validado', 'La validacion de checkout fue ejecutada.', TRUE, NOW(), NOW()),
    ('tpl-stock-updated-v1-ph6', 'organization-phase6', 'StockUpdated', 'EMAIL', 1, 'Stock actualizado', 'Se actualizo el stock operativo de inventario.', TRUE, NOW(), NOW()),
    ('tpl-avail-recalc-v1-ph6', 'organization-phase6', 'CommitableAvailabilityRecalculated', 'EMAIL', 1, 'Disponibilidad recalculada', 'Se recalculo la disponibilidad comprometible.', TRUE, NOW(), NOW())
ON CONFLICT (organization_id, source_event_type, channel, template_version) DO UPDATE
SET subject_template = EXCLUDED.subject_template,
    body_template = EXCLUDED.body_template,
    active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at;
