package com.arka.order.domain.cart.valueobject;

import com.arka.order.domain.cart.enumtype.CheckoutValidationStatus;
import com.arka.order.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.List;

public record ValidatedCheckout(
        String checkoutCorrelationId,
        String cartId,
        CheckoutValidationStatus validationStatus,
        String organizationId,
        String addressId,
        String countryCode,
        Long regionalPolicyVersion,
        String policyCurrency,
        List<String> rejectionReasons,
        Instant validatedAt) {

    public ValidatedCheckout {
        requireNotBlank(checkoutCorrelationId, "checkoutCorrelationId");
        requireNotBlank(cartId, "cartId");
        requireNotBlank(organizationId, "organizationId");
        requireNotBlank(countryCode, "countryCode");
        if (validationStatus == null) {
            throw new DomainInvariantViolationException("validationStatus is required");
        }
        if (regionalPolicyVersion == null || regionalPolicyVersion <= 0) {
            throw new DomainInvariantViolationException("regionalPolicyVersion must be positive");
        }
        if (validationStatus == CheckoutValidationStatus.VALID && (policyCurrency == null || policyCurrency.isBlank())) {
            throw new DomainInvariantViolationException("policyCurrency is required for valid checkout");
        }
        if (validationStatus == CheckoutValidationStatus.INVALID && (rejectionReasons == null || rejectionReasons.isEmpty())) {
            throw new DomainInvariantViolationException("rejectionReasons are required for invalid checkout");
        }
        rejectionReasons = rejectionReasons == null ? List.of() : List.copyOf(rejectionReasons);
        validatedAt = validatedAt == null ? Instant.now() : validatedAt;
    }

    public boolean isValid() {
        return validationStatus == CheckoutValidationStatus.VALID;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}
