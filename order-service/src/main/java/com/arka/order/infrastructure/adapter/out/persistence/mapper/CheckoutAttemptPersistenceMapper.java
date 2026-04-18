package com.arka.order.infrastructure.adapter.out.persistence.mapper;

import com.arka.order.domain.cart.entity.CheckoutAttempt;
import com.arka.order.domain.cart.enumtype.CheckoutValidationStatus;
import com.arka.order.infrastructure.adapter.out.persistence.entity.CheckoutAttemptEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CheckoutAttemptPersistenceMapper {

    private final ObjectMapper objectMapper;

    public CheckoutAttemptPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CheckoutAttemptEntity toEntity(CheckoutAttempt attempt) {
        return new CheckoutAttemptEntity(
                attempt.checkoutAttemptId(),
                attempt.tenantId(),
                attempt.organizationId(),
                attempt.userId(),
                attempt.cartId(),
                attempt.checkoutCorrelationId(),
                attempt.validationStatus().name(),
                attempt.addressId(),
                attempt.countryCode(),
                attempt.regionalPolicyVersion(),
                attempt.policyCurrency(),
                writeReasons(attempt.rejectionReasons()),
                attempt.createdAt(),
                attempt.updatedAt());
    }

    public CheckoutAttempt toDomain(CheckoutAttemptEntity entity) {
        return new CheckoutAttempt(
                entity.checkoutAttemptId(),
                entity.tenantId(),
                entity.organizationId(),
                entity.userId(),
                entity.cartId(),
                entity.checkoutCorrelationId(),
                CheckoutValidationStatus.valueOf(entity.validationStatus()),
                entity.addressId(),
                entity.countryCode(),
                entity.regionalPolicyVersion(),
                entity.policyCurrency(),
                readReasons(entity.rejectionReasons()),
                entity.createdAt(),
                entity.updatedAt());
    }

    private String writeReasons(List<String> reasons) {
        try {
            return objectMapper.writeValueAsString(reasons == null ? List.of() : reasons);
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }

    private List<String> readReasons(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException exception) {
            return List.of(raw);
        }
    }
}
