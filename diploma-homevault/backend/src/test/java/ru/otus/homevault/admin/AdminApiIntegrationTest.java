package ru.otus.homevault.admin;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ru.otus.homevault.admin.dto.UpdateUserStatusRequest;
import ru.otus.homevault.auth.dto.AuthResponse;
import ru.otus.homevault.support.IntegrationTestSupport;
import ru.otus.homevault.users.model.UserStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldRejectAdminEndpointForNonAdmin() throws Exception {
        AuthResponse registered = register("regular@example.com", "Password123", "Regular User");

        mockMvc.perform(get("/api/v1/admin/stats")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access is denied"));
    }

    @Test
    void shouldAllowAdminToBlockUser() throws Exception {
        AuthResponse admin = createAdminAndLogin("admin@example.com", "Password123", "Admin User");
        AuthResponse regularUser = register("regular@example.com", "Password123", "Regular User");

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/status", regularUser.user().id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserStatusRequest(UserStatus.BLOCKED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        assertThat(userRepository.findById(regularUser.user().id()).orElseThrow().getStatus())
                .isEqualTo(UserStatus.BLOCKED);
        assertThat(auditEventRepository.findAll())
                .extracting("action")
                .contains("USER_STATUS_UPDATED");
    }
}
