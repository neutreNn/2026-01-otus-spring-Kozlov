package ru.otus.homevault.storage;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ru.otus.homevault.auth.dto.AuthResponse;
import ru.otus.homevault.storage.dto.FileResponse;
import ru.otus.homevault.storage.model.StoredFile;
import ru.otus.homevault.support.IntegrationTestSupport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilesApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldReturnReadableErrorForMissingMultipartFile() throws Exception {
        AuthResponse registered = register("multipart-owner@example.com", "Password123", "Multipart Owner");

        mockMvc.perform(multipart("/api/v1/files")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing multipart request part"))
                .andExpect(jsonPath("$.details.part").value("file"));
    }

    @Test
    void shouldUploadFileAndCreateMetadata() throws Exception {
        AuthResponse registered = register("file-owner@example.com", "Password123", "File Owner");
        byte[] content = "hello storage".getBytes(StandardCharsets.UTF_8);

        FileResponse uploaded = uploadFile(registered, content, "hello.txt", "text/plain", null);

        assertThat(uploaded.id()).isNotNull();
        assertThat(uploaded.folderId()).isNull();
        assertThat(uploaded.originalName()).isEqualTo("hello.txt");
        assertThat(uploaded.contentType()).isEqualTo("text/plain");
        assertThat(uploaded.sizeBytes()).isEqualTo(content.length);
        assertThat(uploaded.checksumSha256()).isEqualTo(sha256(content));

        StoredFile storedFile = storedFileRepository.findById(uploaded.id()).orElseThrow();
        assertThat(storedFile.getStorageKey()).startsWith(registered.user().id() + "/");
        verify(fileStorageService).put(
                startsWith(registered.user().id() + "/"),
                any(InputStream.class),
                eq((long) content.length),
                eq("text/plain")
        );
    }

    @Test
    void shouldDownloadOwnFile() throws Exception {
        AuthResponse registered = register("file-owner@example.com", "Password123", "File Owner");
        byte[] content = "download me".getBytes(StandardCharsets.UTF_8);
        FileResponse uploaded = uploadFile(registered, content, "download.txt", "text/plain", null);

        reset(fileStorageService);
        when(fileStorageService.get(anyString())).thenReturn(new ByteArrayInputStream(content));

        mockMvc.perform(get("/api/v1/files/{fileId}/download", uploaded.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("text/plain")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("download.txt")))
                .andExpect(content().bytes(content));
    }

    @Test
    void shouldHideForeignFileOnDownload() throws Exception {
        AuthResponse owner = register("owner@example.com", "Password123", "Owner");
        AuthResponse anotherUser = register("another@example.com", "Password123", "Another User");
        FileResponse uploaded = uploadFile(
                owner,
                "private".getBytes(StandardCharsets.UTF_8),
                "private.txt",
                "text/plain",
                null
        );

        reset(fileStorageService);

        mockMvc.perform(get("/api/v1/files/{fileId}/download", uploaded.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File not found"));

        verify(fileStorageService, never()).get(anyString());
    }

    @Test
    void shouldDeleteFileMetadataAndStorageObject() throws Exception {
        AuthResponse registered = register("file-owner@example.com", "Password123", "File Owner");
        FileResponse uploaded = uploadFile(
                registered,
                "delete me".getBytes(StandardCharsets.UTF_8),
                "delete.txt",
                "text/plain",
                null
        );
        StoredFile storedFile = storedFileRepository.findById(uploaded.id()).orElseThrow();

        reset(fileStorageService);

        mockMvc.perform(delete("/api/v1/files/{fileId}", uploaded.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered)))
                .andExpect(status().isNoContent());

        assertThat(storedFileRepository.findById(uploaded.id())).isEmpty();
        verify(fileStorageService).delete(storedFile.getStorageKey());
    }

    private String sha256(byte[] content) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    }
}
