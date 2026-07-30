package ru.otus.homevault.folders;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ru.otus.homevault.auth.dto.AuthResponse;
import ru.otus.homevault.folders.dto.CreateFolderRequest;
import ru.otus.homevault.folders.dto.FolderResponse;
import ru.otus.homevault.support.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FoldersApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldCreateFolder() throws Exception {
        AuthResponse registered = register("folder-owner@example.com", "Password123", "Folder Owner");

        FolderResponse folder = createFolder(registered, "Documents", null);

        assertThat(folder.id()).isNotNull();
        assertThat(folder.parentId()).isNull();
        assertThat(folder.name()).isEqualTo("Documents");
        assertThat(folderRepository.findAll()).hasSize(1);
        assertThat(auditEventRepository.findAll())
                .extracting("action")
                .contains("FOLDER_CREATED");
    }

    @Test
    void shouldRejectDuplicateFolderNameInSameParent() throws Exception {
        AuthResponse registered = register("folder-owner@example.com", "Password123", "Folder Owner");
        createFolder(registered, "Documents", null);

        mockMvc.perform(post("/api/v1/folders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateFolderRequest("documents", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Folder name already exists in this location"));
    }
}
