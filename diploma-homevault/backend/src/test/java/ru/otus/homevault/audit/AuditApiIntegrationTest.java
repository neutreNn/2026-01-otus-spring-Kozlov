package ru.otus.homevault.audit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import ru.otus.homevault.auth.dto.AuthResponse;
import ru.otus.homevault.support.IntegrationTestSupport;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldListOwnAuditEvents() throws Exception {
        AuthResponse registered = register("audit-owner@example.com", "Password123", "Audit Owner");
        createNote(registered, "Audit note", "Audit content", Set.of("audit"));

        mockMvc.perform(get("/api/v1/audit/events")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].action").value("NOTE_CREATED"));
    }
}
