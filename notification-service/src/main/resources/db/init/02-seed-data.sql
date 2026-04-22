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
    ('5b9b50d1-99e8-42f4-b47e-48071e98e5d4', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'order.confirmed', 'EMAIL', 'SMS', 3, 300, TRUE, NOW(), NOW()),
    ('7adf91c3-9556-471a-a394-759c9a78adfe', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'order.status-changed', 'EMAIL', 'WHATSAPP', 3, 180, TRUE, NOW(), NOW()),
    ('cd8ff698-5a5b-4a37-ab39-1225668ecf74', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'inventory.low-stock-detected', 'IN_APP', 'EMAIL', 2, 120, TRUE, NOW(), NOW())
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
    ('a2f94047-08f4-46c3-ad6f-d6d15a6e0f27', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'order.confirmed', 'EMAIL', 'SMS', 3, 300, TRUE, NOW(), NOW()),
    ('b1929eca-009a-490b-977d-67cd8b36553b', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'order.status-changed', 'EMAIL', 'WHATSAPP', 3, 180, TRUE, NOW(), NOW()),
    ('02ae0a2e-7f7f-4519-9b94-ee654a00f9de', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'inventory.low-stock-detected', 'IN_APP', 'EMAIL', 2, 120, TRUE, NOW(), NOW())
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
    ('1942fd07-1196-4096-81f8-a16bc407dde5', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderCreatedFromValidatedCart', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('50afb4db-712a-4e1e-abf7-e2e515cba165', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderOperationalStatusUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('f52b8e21-76d7-421e-97f3-e966c8b0d967', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderFinancialStatusUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('deb17fa9-a462-4d4c-b9b0-a63b2283799b', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'ManualPaymentRegistered', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('4f7f861f-c6e9-456a-a908-84d5dbc23e4b', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderAdjustedBeforeClose', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('08ddca89-650f-4d82-9ff8-b7052cef9423', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderConsistencyRevalidated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('97942129-e554-47e1-88b9-f4e083582356', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CartCreated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('d514bc85-dfc9-450c-8c75-eee7c799a17c', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CartItemsAdjusted', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('85aea26c-232a-4fa5-9991-eed5e95de691', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CartAbandonedReminder', 'EMAIL', 'WHATSAPP', 3, 120, TRUE, NOW(), NOW()),
    ('c568bb5e-13c4-4bb6-8413-815a880878a1', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CheckoutAvailabilityValidated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('1263f3c2-44ca-436d-8d25-43eb31754413', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'StockUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('317dc000-26c0-4cf5-bd60-ce18a40a6aec', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CommitableAvailabilityRecalculated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW())
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
    ('2796988b-1cc6-4347-84ba-eced4f8605ba', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderCreatedFromValidatedCart', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('c62d4c53-b66a-4680-8186-c1499009af96', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderOperationalStatusUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('a06773c4-cae8-4c89-9384-54347458c3f7', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderFinancialStatusUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('49214d9e-19cb-40c6-9fea-533a95168918', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'ManualPaymentRegistered', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('6eab6b39-a9e0-441e-8138-1ab9fcf0bf15', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderAdjustedBeforeClose', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('dc585955-16a8-4779-a3eb-0332cae0dc35', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderConsistencyRevalidated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('7cf08cfb-f436-4ff8-8c03-47fb21a17f1a', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CartCreated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('4544d9d2-64cd-449c-957b-ab798f1dd0ed', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CartItemsAdjusted', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('76bd7a20-3775-4477-ac0f-06254dcecfdd', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CartAbandonedReminder', 'EMAIL', 'WHATSAPP', 3, 120, TRUE, NOW(), NOW()),
    ('bc9ca841-98d2-472e-b828-71600a4a7c37', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CheckoutAvailabilityValidated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('a4e15468-257a-499b-91af-5c464a25c98a', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'StockUpdated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW()),
    ('aa9f779c-c04e-4c17-888d-e43763856f3d', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CommitableAvailabilityRecalculated', 'EMAIL', 'SMS', 3, 120, TRUE, NOW(), NOW())
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
    ('83ea8c94-fbda-42e2-873f-e1630352e5b8', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'order.confirmed', 'EMAIL', 1, 'Pedido de accesorios confirmado', 'Tu pedido B2B de accesorios para PC fue confirmado correctamente.', TRUE, NOW(), NOW()),
    ('d9b62cd0-911a-45f2-a8cc-996ccadbc916', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'order.status-changed', 'EMAIL', 1, 'Actualizacion de estado del pedido', 'Tu pedido de perifericos tiene un nuevo estado operativo.', TRUE, NOW(), NOW()),
    ('6d5c4e79-aa08-4319-8905-1b62239da3ab', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'inventory.low-stock-detected', 'IN_APP', 1, 'Alerta de bajo stock', 'Se detecto bajo stock en un SKU de accesorios para PC priorizado.', TRUE, NOW(), NOW())
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
    ('873ba1e9-8f78-4521-89f2-f7cd51063a38', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'order.confirmed', 'EMAIL', 1, 'Pedido de accesorios confirmado', 'El pedido Arka B2B fue confirmado y puede pasar a preparacion logistica.', TRUE, NOW(), NOW()),
    ('f7929ffa-a6b8-4d25-ba63-d33830f58d95', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'order.status-changed', 'EMAIL', 1, 'Cambio de estado operativo', 'El pedido de accesorios y componentes cambio de estado operativo.', TRUE, NOW(), NOW()),
    ('8823ce94-90a1-4698-bbbb-c10e50f134e4', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'inventory.low-stock-detected', 'IN_APP', 1, 'Alerta de bajo stock', 'Un SKU clave del portafolio Arka quedo por debajo del stock de seguridad.', TRUE, NOW(), NOW())
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
    ('6fc8e804-a250-46a1-8bd6-1232895b05f5', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderCreatedFromValidatedCart', 'EMAIL', 1, 'Pedido B2B registrado', 'Se registro un pedido de accesorios para PC y quedo pendiente de aprobacion.', TRUE, NOW(), NOW()),
    ('66120b3b-f592-4dda-9750-ebe65c65cd5b', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderOperationalStatusUpdated', 'EMAIL', 1, 'Estado operativo actualizado', 'El pedido de accesorios cambio de etapa en la operacion.', TRUE, NOW(), NOW()),
    ('d3cc002d-57a1-4b99-862e-0357f9d9771c', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderFinancialStatusUpdated', 'EMAIL', 1, 'Estado financiero actualizado', 'El estado financiero del pedido B2B fue actualizado.', TRUE, NOW(), NOW()),
    ('ca7bab80-716f-4ae8-b716-d0de7979dd74', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'ManualPaymentRegistered', 'EMAIL', 1, 'Pago manual registrado', 'Se registro un pago manual asociado a un pedido de Arka.', TRUE, NOW(), NOW()),
    ('db49bb58-081b-4b78-a95b-73e35b39facc', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderAdjustedBeforeClose', 'EMAIL', 1, 'Pedido ajustado', 'El pedido de accesorios fue ajustado antes del cierre operativo.', TRUE, NOW(), NOW()),
    ('94a291e9-cb27-4e24-ab29-de010f2088b8', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'OrderConsistencyRevalidated', 'EMAIL', 1, 'Pedido revalidado', 'Se revalido la consistencia comercial y logistica del pedido.', TRUE, NOW(), NOW()),
    ('fe61820d-1e46-490f-9584-2374f0939410', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CartCreated', 'EMAIL', 1, 'Carrito B2B creado', 'Se creo un carrito con referencias de perifericos para una nueva compra.', TRUE, NOW(), NOW()),
    ('e2517161-f4d6-48a4-8c89-bfb788d2343b', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CartItemsAdjusted', 'EMAIL', 1, 'Carrito actualizado', 'Los items del carrito de accesorios fueron ajustados.', TRUE, NOW(), NOW()),
    ('78e1e871-8e93-457a-8142-1c5adef64a2a', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CartAbandonedReminder', 'EMAIL', 1, 'Recupera tu carrito Arka B2B', 'Detectamos un carrito pendiente con accesorios para PC listo para continuar la compra.', TRUE, NOW(), NOW()),
    ('4f3ee1f0-d0fc-40f8-948e-a7918d19a09f', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CheckoutAvailabilityValidated', 'EMAIL', 1, 'Checkout validado', 'La validacion de disponibilidad y direccion fue completada.', TRUE, NOW(), NOW()),
    ('e9f8c00d-d5e8-43a2-870f-1229da9b8ba5', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'StockUpdated', 'EMAIL', 1, 'Stock actualizado', 'Se actualizo el stock operativo del portafolio de accesorios.', TRUE, NOW(), NOW()),
    ('0bd604a1-582b-4609-bdff-29e64980332a', '8b88f0d3-7f1e-4f5e-b2d0-3dfb2ac6f3a1', 'CommitableAvailabilityRecalculated', 'EMAIL', 1, 'Disponibilidad recalculada', 'Se recalculo la disponibilidad comprometible de un SKU del catalogo.', TRUE, NOW(), NOW())
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
    ('41980a09-0ffb-4e72-ab40-34603383b0da', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderCreatedFromValidatedCart', 'EMAIL', 1, 'Pedido B2B registrado', 'Se registro un pedido de accesorios para PC y quedo listo para aprobacion comercial.', TRUE, NOW(), NOW()),
    ('43564ebc-2cfb-476e-84cd-abb11d758796', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderOperationalStatusUpdated', 'EMAIL', 1, 'Estado operativo actualizado', 'El pedido de accesorios cambio de etapa en la operacion Arka.', TRUE, NOW(), NOW()),
    ('99254f05-3a4e-4be6-9216-b9bc1abae4cc', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderFinancialStatusUpdated', 'EMAIL', 1, 'Estado financiero actualizado', 'El estado financiero del pedido B2B de Arka fue actualizado.', TRUE, NOW(), NOW()),
    ('9ec2e874-9ed1-4579-b102-f6ab82b94f0d', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'ManualPaymentRegistered', 'EMAIL', 1, 'Pago manual registrado', 'Se registro un pago manual para un pedido de hardware y accesorios.', TRUE, NOW(), NOW()),
    ('53df2f36-4767-45da-a912-03a5a70dfb95', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderAdjustedBeforeClose', 'EMAIL', 1, 'Pedido ajustado', 'El pedido fue ajustado antes del cierre logistico.', TRUE, NOW(), NOW()),
    ('097552bc-f065-4274-9d5d-bb46873644fd', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'OrderConsistencyRevalidated', 'EMAIL', 1, 'Pedido revalidado', 'Se revalido la consistencia de precio, stock y direccion del pedido.', TRUE, NOW(), NOW()),
    ('905d6eb3-fa68-409e-b9d5-02e4da3a12e6', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CartCreated', 'EMAIL', 1, 'Carrito B2B creado', 'Se creo un carrito con referencias de teclados, SSD y accesorios para PC.', TRUE, NOW(), NOW()),
    ('29eecf55-c7ee-4355-8dae-e8dbcb17ceb6', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CartItemsAdjusted', 'EMAIL', 1, 'Carrito actualizado', 'Los items del carrito de accesorios fueron ajustados por el operador.', TRUE, NOW(), NOW()),
    ('7ad7140a-bf56-41f0-ac8d-d2aa825b8546', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CartAbandonedReminder', 'EMAIL', 1, 'Tu carrito Arka sigue disponible', 'Tu carrito con accesorios para PC sigue disponible para reactivar la compra corporativa.', TRUE, NOW(), NOW()),
    ('f8986e33-eafd-40f9-9925-bc3013061bf3', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CheckoutAvailabilityValidated', 'EMAIL', 1, 'Checkout validado', 'La validacion de disponibilidad y cobertura logistica fue completada.', TRUE, NOW(), NOW()),
    ('7b71f4af-9dd3-41d4-a505-2302298dfcac', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'StockUpdated', 'EMAIL', 1, 'Stock actualizado', 'Se actualizo el stock operativo del catalogo de accesorios y componentes.', TRUE, NOW(), NOW()),
    ('424d4b54-e8af-4fc2-aab3-fe5a44fec050', '6f6d04b6-4c9d-4b6a-8e13-5b2f9c0a1d77', 'CommitableAvailabilityRecalculated', 'EMAIL', 1, 'Disponibilidad recalculada', 'Se recalculo la disponibilidad comprometible para un SKU del portafolio Arka.', TRUE, NOW(), NOW())
ON CONFLICT (organization_id, source_event_type, channel, template_version) DO UPDATE
SET subject_template = EXCLUDED.subject_template,
    body_template = EXCLUDED.body_template,
    active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at;
