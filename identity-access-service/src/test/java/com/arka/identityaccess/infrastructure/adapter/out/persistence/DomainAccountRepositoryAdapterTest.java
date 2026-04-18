package com.arka.identityaccess.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class DomainAccountRepositoryAdapterTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Test
    void shouldReturnEmptyWhenFindByIdDoesNotExist() {
        DomainAccountRepositoryAdapter adapter =
                new DomainAccountRepositoryAdapter(userPersistencePort, "BUYER", "SYSTEM");
        AccountId accountId = AccountId.of("acc-123");

        when(userPersistencePort.loadById(accountId)).thenReturn(Mono.empty());

        assertTrue(adapter.findById(accountId).isEmpty());
    }

    @Test
    void shouldPropagateInfrastructureErrorWhenFindByIdFails() {
        DomainAccountRepositoryAdapter adapter =
                new DomainAccountRepositoryAdapter(userPersistencePort, "BUYER", "SYSTEM");
        AccountId accountId = AccountId.of("acc-123");

        when(userPersistencePort.loadById(accountId)).thenReturn(Mono.error(new RuntimeException("db down")));

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> adapter.findById(accountId));
        assertEquals("Failed to load account by id from persistence. accountId=acc-123", exception.getMessage());
        assertEquals("db down", exception.getCause().getMessage());
    }

    @Test
    void shouldReturnEmptyWhenFindByEmailDoesNotExist() {
        DomainAccountRepositoryAdapter adapter =
                new DomainAccountRepositoryAdapter(userPersistencePort, "BUYER", "SYSTEM");
        EmailAddress emailAddress = EmailAddress.of("user@arka.com");

        when(userPersistencePort.loadForLogin(emailAddress)).thenReturn(Mono.empty());

        assertTrue(adapter.findByEmail(emailAddress).isEmpty());
    }

    @Test
    void shouldPropagateInfrastructureErrorWhenFindByEmailFails() {
        DomainAccountRepositoryAdapter adapter =
                new DomainAccountRepositoryAdapter(userPersistencePort, "BUYER", "SYSTEM");
        EmailAddress emailAddress = EmailAddress.of("user@arka.com");

        when(userPersistencePort.loadForLogin(emailAddress)).thenReturn(Mono.error(new RuntimeException("timeout")));

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> adapter.findByEmail(emailAddress));
        assertEquals("Failed to load account by email from persistence. email=user@arka.com", exception.getMessage());
        assertEquals("timeout", exception.getCause().getMessage());
    }
}
