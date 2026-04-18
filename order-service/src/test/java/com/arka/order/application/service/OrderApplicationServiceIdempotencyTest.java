package com.arka.order.application.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.arka.order.application.command.CreateCartCommand;
import com.arka.order.application.command.HandleReservationExpiredCommand;
import com.arka.order.application.exception.IdempotencyConflictException;
import com.arka.order.application.mapper.result.OrderResultMapper;
import com.arka.order.application.port.out.audit.OrderAuditPort;
import com.arka.order.application.port.out.cache.CheckoutAttemptCachePort;
import com.arka.order.application.port.out.directory.DirectoryCheckoutPort;
import com.arka.order.application.port.out.external.ActorLegitimacyPort;
import com.arka.order.application.port.out.external.CatalogVariantPort;
import com.arka.order.application.port.out.external.ClockPort;
import com.arka.order.application.port.out.external.InventoryReservationPort;
import com.arka.order.application.port.out.persistence.CartPersistencePort;
import com.arka.order.application.port.out.persistence.CheckoutAttemptPersistencePort;
import com.arka.order.application.port.out.persistence.IdempotencyRecordPersistencePort;
import com.arka.order.application.port.out.persistence.OrderStatusHistoryPersistencePort;
import com.arka.order.application.port.out.persistence.OutboxPersistencePort;
import com.arka.order.application.port.out.persistence.ProcessedEventPersistencePort;
import com.arka.order.application.port.out.persistence.PurchaseOrderPersistencePort;
import com.arka.order.application.port.out.security.ActorContext;
import com.arka.order.application.port.out.security.ActorContextProviderPort;
import com.arka.order.domain.cart.service.CartPolicyService;
import com.arka.order.domain.cart.aggregate.Cart;
import com.arka.order.domain.cart.enumtype.CartStatus;
import com.arka.order.domain.order.entity.IdempotencyRecord;
import com.arka.order.domain.order.service.OrderPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceIdempotencyTest {

    @Mock
    private CartPersistencePort cartPersistencePort;
    @Mock
    private CheckoutAttemptPersistencePort checkoutAttemptPersistencePort;
    @Mock
    private PurchaseOrderPersistencePort purchaseOrderPersistencePort;
    @Mock
    private OrderStatusHistoryPersistencePort orderStatusHistoryPersistencePort;
    @Mock
    private IdempotencyRecordPersistencePort idempotencyRecordPersistencePort;
    @Mock
    private OutboxPersistencePort outboxPersistencePort;
    @Mock
    private ProcessedEventPersistencePort processedEventPersistencePort;
    @Mock
    private OrderAuditPort orderAuditPort;
    @Mock
    private CheckoutAttemptCachePort checkoutAttemptCachePort;
    @Mock
    private ClockPort clockPort;
    @Mock
    private ActorLegitimacyPort actorLegitimacyPort;
    @Mock
    private CatalogVariantPort catalogVariantPort;
    @Mock
    private InventoryReservationPort inventoryReservationPort;
    @Mock
    private DirectoryCheckoutPort directoryCheckoutPort;
    @Mock
    private ActorContextProviderPort actorContextProviderPort;

    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(
                cartPersistencePort,
                checkoutAttemptPersistencePort,
                purchaseOrderPersistencePort,
                orderStatusHistoryPersistencePort,
                idempotencyRecordPersistencePort,
                outboxPersistencePort,
                processedEventPersistencePort,
                orderAuditPort,
                checkoutAttemptCachePort,
                clockPort,
                actorLegitimacyPort,
                catalogVariantPort,
                inventoryReservationPort,
                directoryCheckoutPort,
                actorContextProviderPort,
                new CartPolicyService(),
                new OrderPolicyService(),
                new OrderResultMapper(),
                new ObjectMapper());
    }

    @Test
    void shouldRejectIdempotencyKeyReuseWithDifferentPayload() {
        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext("user-1", "organization-1", false, false)));
        when(actorLegitimacyPort.isLegitimate(anyString())).thenReturn(Mono.just(true));
        when(idempotencyRecordPersistencePort.findByOrganizationOperationAndKey("organization-1", "CreateCart", "idem-key-1"))
                .thenReturn(Mono.just(new IdempotencyRecord(
                        UUID.randomUUID().toString(),
                        "organization-1",
                        "CreateCart",
                        "idem-key-1",
                        "different-hash",
                        "Cart",
                        "cart-1",
                        200,
                        Instant.now(),
                        Instant.now())));

        CreateCartCommand command = new CreateCartCommand(
                "organization-1", "user-1",
                "user-1",
                "idem-key-1");

        StepVerifier.create(service.handle(command))
                .expectError(IdempotencyConflictException.class)
                .verify();
    }

    @Test
    void shouldTreatReservationExpiredEventAsIdempotentWhenAlreadyProcessed() {
        Instant now = Instant.parse("2026-04-15T10:00:00Z");
        Cart cart = Cart.rehydrate(
                "cart-1", "organization-1", "user-1",
                CartStatus.ACTIVE,
                2L,
                now.minusSeconds(30),
                now.minusSeconds(5),
                List.of());

        when(actorContextProviderPort.currentActor())
                .thenReturn(Mono.just(new ActorContext("user-1", "organization-1", true, false)));
        when(actorLegitimacyPort.isLegitimate("user-1")).thenReturn(Mono.just(true));
        when(processedEventPersistencePort.existsByEventAndConsumer("evt-1", "order.reservation-expired-handler"))
                .thenReturn(Mono.just(true));
        when(cartPersistencePort.findById("organization-1", "cart-1")).thenReturn(Mono.just(cart));

        HandleReservationExpiredCommand command = new HandleReservationExpiredCommand(
                "organization-1", "cart-1",
                "res-1",
                "evt-1",
                "user-1");

        StepVerifier.create(service.handle(command))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("cart-1", result.cartId()))
                .verifyComplete();
    }
}
