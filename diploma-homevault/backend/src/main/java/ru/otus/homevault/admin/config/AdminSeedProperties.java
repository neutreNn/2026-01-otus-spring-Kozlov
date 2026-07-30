package ru.otus.homevault.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "homevault.admin.seed")
public record AdminSeedProperties(
        String email,
        String password,
        String displayName
) {

    public boolean enabled() {
        return hasText(email) && hasText(password);
    }

    public String normalizedEmail() {
        return email.trim().toLowerCase();
    }

    public String normalizedDisplayName() {
        return hasText(displayName) ? displayName.trim() : "HomeVault Admin";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
