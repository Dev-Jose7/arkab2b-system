package com.arka.identityaccess.infrastructure.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.s2s")
public class ServiceClientRegistryProperties {

    private boolean enabled = true;
    private long defaultTokenTtlSeconds = 300L;
    private Map<String, Client> clients = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getDefaultTokenTtlSeconds() {
        return defaultTokenTtlSeconds;
    }

    public void setDefaultTokenTtlSeconds(long defaultTokenTtlSeconds) {
        this.defaultTokenTtlSeconds = defaultTokenTtlSeconds;
    }

    public Map<String, Client> getClients() {
        return clients;
    }

    public void setClients(Map<String, Client> clients) {
        this.clients = clients == null ? new LinkedHashMap<>() : new LinkedHashMap<>(clients);
    }

    public Client resolvedClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        Client client = clients.get(clientId);
        if (client == null) {
            return null;
        }
        client.setClientId(clientId);
        return client;
    }

    public static class Client {

        private String clientId;
        private String clientSecret = "";
        private List<String> scopes = new ArrayList<>();
        private List<String> audiences = new ArrayList<>();
        private List<String> roles = new ArrayList<>();
        private Long tokenTtlSeconds;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes == null ? new ArrayList<>() : new ArrayList<>(scopes);
        }

        public List<String> getAudiences() {
            return audiences;
        }

        public void setAudiences(List<String> audiences) {
            this.audiences = audiences == null ? new ArrayList<>() : new ArrayList<>(audiences);
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
        }

        public Long getTokenTtlSeconds() {
            return tokenTtlSeconds;
        }

        public void setTokenTtlSeconds(Long tokenTtlSeconds) {
            this.tokenTtlSeconds = tokenTtlSeconds;
        }

        public Set<String> normalizedScopes() {
            return normalize(scopes);
        }

        public Set<String> normalizedAudiences() {
            return normalize(audiences);
        }

        public Set<String> normalizedRoles() {
            Set<String> normalized = normalize(roles).stream()
                    .map(value -> value.toUpperCase(Locale.ROOT))
                    .map(value -> value.startsWith("ROLE_") ? value.substring("ROLE_".length()) : value)
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
            normalized.add("TRUSTED_SERVICE");
            return Set.copyOf(normalized);
        }

        private Set<String> normalize(List<String> values) {
            if (values == null || values.isEmpty()) {
                return Set.of();
            }
            return values.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        }
    }
}
