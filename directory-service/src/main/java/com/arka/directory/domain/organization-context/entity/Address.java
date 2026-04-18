package com.arka.directory.domain.organizationcontext.entity;

import com.arka.directory.domain.organizationcontext.enumtype.AddressStatus;
import com.arka.directory.domain.organizationcontext.enumtype.AddressType;
import com.arka.directory.domain.organizationcontext.enumtype.AddressValidationStatus;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record Address(
        String addressId,
        String organizationId,
        AddressType addressType,
        String alias,
        String line1,
        String line2,
        String city,
        String stateRegion,
        String postalCode,
        String countryCode,
        String reference,
        Double latitude,
        Double longitude,
        boolean isDefault,
        AddressStatus status,
        AddressValidationStatus validationStatus,
        Instant validatedAt,
        Instant createdAt,
        Instant updatedAt) {

    public Address {
        requireNotBlank(addressId, "addressId");
        requireNotBlank(organizationId, "organizationId");
        if (addressType == null) {
            throw new DomainInvariantViolationException("addressType is required");
        }
        requireNotBlank(line1, "line1");
        requireNotBlank(city, "city");
        requireNotBlank(countryCode, "countryCode");
        if (status == null) {
            throw new DomainInvariantViolationException("address status is required");
        }
        if (validationStatus == null) {
            throw new DomainInvariantViolationException("address validation status is required");
        }
        if (isDefault && !status.isActive()) {
            throw new DomainInvariantViolationException("Default address must be active");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    public Address deactivate(Instant updatedAt) {
        return new Address(
                addressId,
                organizationId,
                addressType,
                alias,
                line1,
                line2,
                city,
                stateRegion,
                postalCode,
                countryCode,
                reference,
                latitude,
                longitude,
                false,
                AddressStatus.INACTIVE,
                validationStatus,
                validatedAt,
                createdAt,
                updatedAt);
    }

    public Address markDefault(boolean defaultValue, Instant updatedAt) {
        if (defaultValue && !status.isActive()) {
            throw new DomainInvariantViolationException("Inactive address cannot be default");
        }
        return new Address(
                addressId,
                organizationId,
                addressType,
                alias,
                line1,
                line2,
                city,
                stateRegion,
                postalCode,
                countryCode,
                reference,
                latitude,
                longitude,
                defaultValue,
                status,
                validationStatus,
                validatedAt,
                createdAt,
                updatedAt);
    }

    public void ensureUsableForCheckout(String expectedOrganizationId, boolean requiresVerifiedAddress) {
        if (expectedOrganizationId == null || !organizationId.equals(expectedOrganizationId)) {
            throw new DomainInvariantViolationException("Address does not belong to operation organization");
        }
        if (!status.isActive()) {
            throw new DomainInvariantViolationException("Address is not active");
        }
        if (requiresVerifiedAddress && !validationStatus.isVerified()) {
            throw new DomainInvariantViolationException("Address must be verified to be used in checkout");
        }
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException(fieldName + " is required");
        }
    }
}
