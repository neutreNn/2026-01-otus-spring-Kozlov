package ru.otus.homevault.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.otus.homevault.auth.dto.AuthResponse;
import ru.otus.homevault.auth.dto.LoginRequest;
import ru.otus.homevault.auth.dto.RefreshTokenRequest;
import ru.otus.homevault.auth.dto.RegisterRequest;
import ru.otus.homevault.auth.model.RefreshToken;
import ru.otus.homevault.support.IntegrationTestSupport;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.model.UserStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldReturnFieldErrorsForValidationFailure() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("bad-email", "short", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.fields.email").isArray())
                .andExpect(jsonPath("$.details.fields.password").isArray())
                .andExpect(jsonPath("$.details.fields.displayName").isArray());
    }

    @Test
    void shouldRegisterUser() throws Exception {
        AuthResponse response = register("User@Example.com", "Password123", "Ivan Petrov");

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("user@example.com");
        assertThat(response.user().roles()).extracting(Enum::name).containsExactly("USER");

        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo("Password123");
        assertThat(passwordEncoder.matches("Password123", user.getPasswordHash())).isTrue();
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        register("user@example.com", "Password123", "Ivan Petrov");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("USER@example.com", "Password123", "Ivan Petrov")
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        register("user@example.com", "Password123", "Ivan Petrov");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user@example.com", "Password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("user@example.com"));
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {
        register("user@example.com", "Password123", "Ivan Petrov");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user@example.com", "WrongPass123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void shouldRefreshToken() throws Exception {
        AuthResponse registered = register("user@example.com", "Password123", "Ivan Petrov");

        String responseBody = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(registered.refreshToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse refreshed = objectMapper.readValue(responseBody, AuthResponse.class);
        assertThat(refreshed.refreshToken()).isNotEqualTo(registered.refreshToken());
        assertThat(refreshTokenRepository.findAll())
                .extracting(RefreshToken::getRevokedAt)
                .filteredOn(revokedAt -> revokedAt != null)
                .hasSize(1);
    }

    @Test
    void shouldRequireJwtForProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void shouldRejectBlockedUserOnProtectedEndpoint() throws Exception {
        AuthResponse registered = register("user@example.com", "Password123", "Ivan Petrov");
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        user.setStatus(UserStatus.BLOCKED);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearer(registered)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User is blocked"));
    }
}
