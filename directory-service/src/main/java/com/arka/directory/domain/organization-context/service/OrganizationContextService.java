package com.arka.directory.domain.organizationcontext.service;

import com.arka.directory.domain.countrypolicy.aggregate.CountryPolicy;
import com.arka.directory.domain.organizationcontext.aggregate.Organization;
import com.arka.directory.domain.organizationcontext.entity.Address;
import com.arka.directory.domain.organizationcontext.exception.OrganizationIsolationViolationException;
import com.arka.directory.domain.organizationcontext.valueobject.OrganizationContext;
import com.arka.directory.domain.shared.exception.DomainInvariantViolationException;
import com.arka.directory.domain.shared.exception.OperationNotPermittedException;
import org.springframework.stereotype.Component;

@Component
public class OrganizationContextService {

    public void validateSensitiveOperationContext(Organization organization, OrganizationContext context) {
        if (organization == null) {
            throw new DomainInvariantViolationException("Organization context cannot be resolved");
        }
        if (context == null) {
            throw new DomainInvariantViolationException("Operation context is required");
        }
        if (!context.actorLegitimate()) {
            throw new OperationNotPermittedException("Actor legitimacy validation failed");
        }

        organization.assertOwnership(context.organizationId());
        if (context.hasScopedOrganization()
                && !context.actorOrganizationId().equals(organization.id().value())
                && !context.directoryAdmin()) {
            throw new OrganizationIsolationViolationException("Actor organization does not match requested organization");
        }
        organization.ensureActiveForSensitiveOperation();
    }

    public void validateCheckoutAddress(
            Organization organization,
            CountryPolicy activeCountryPolicy,
            Address address) {
        if (organization == null || activeCountryPolicy == null || address == null) {
            throw new DomainInvariantViolationException("Checkout context cannot be resolved");
        }
        organization.ensureActiveForSensitiveOperation();
        if (!activeCountryPolicy.status().isActive()) {
            throw new DomainInvariantViolationException("Country policy must be active for checkout");
        }
        address.ensureUsableForCheckout(
                organization.id().value(),
                activeCountryPolicy.requiresVerifiedAddress());
    }
}
