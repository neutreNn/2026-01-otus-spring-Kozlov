package ru.otus.homevault.sharing;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import ru.otus.homevault.auth.dto.AuthResponse;
import ru.otus.homevault.sharing.dto.ShareResponse;
import ru.otus.homevault.sharing.model.ShareLink;
import ru.otus.homevault.sharing.model.ShareResourceType;
import ru.otus.homevault.storage.dto.FileResponse;
import ru.otus.homevault.support.IntegrationTestSupport;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SharingApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldCreateFileShare() throws Exception {
        AuthResponse registered = register("file-owner@example.com", "Password123", "File Owner");
        FileResponse uploaded = uploadFile(
                registered,
                "share me".getBytes(StandardCharsets.UTF_8),
                "shared.txt",
                "text/plain",
                null
        );

        ShareResponse share = createShare(
                registered,
                ShareResourceType.FILE,
                uploaded.id(),
                Instant.now().plusSeconds(3600)
        );

        assertThat(share.id()).isNotNull();
        assertThat(share.token()).isNotBlank();
        assertThat(share.resourceType()).isEqualTo(ShareResourceType.FILE);
        assertThat(share.resourceId()).isEqualTo(uploaded.id());
        assertThat(shareLinkRepository.findById(share.id())).isPresent();
        assertThat(auditEventRepository.findAll())
                .extracting("action")
                .contains("SHARE_CREATED");
    }

    @Test
    void shouldDownloadPublicFileWithoutJwt() throws Exception {
        AuthResponse registered = register("file-owner@example.com", "Password123", "File Owner");
        byte[] content = "public content".getBytes(StandardCharsets.UTF_8);
        FileResponse uploaded = uploadFile(registered, content, "public.txt", "text/plain", null);
        ShareResponse share = createShare(
                registered,
                ShareResourceType.FILE,
                uploaded.id(),
                Instant.now().plusSeconds(3600)
        );

        reset(fileStorageService);
        when(fileStorageService.get(anyString())).thenReturn(new ByteArrayInputStream(content));

        mockMvc.perform(get("/api/v1/public/shares/{token}/download", share.token()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("text/plain")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("public.txt")))
                .andExpect(content().bytes(content));

        assertThat(shareLinkRepository.findById(share.id()).orElseThrow().getAccessCount()).isEqualTo(1);
    }

    @Test
    void shouldReturnGoneForExpiredShare() throws Exception {
        AuthResponse registered = register("file-owner@example.com", "Password123", "File Owner");
        FileResponse uploaded = uploadFile(
                registered,
                "expired content".getBytes(StandardCharsets.UTF_8),
                "expired.txt",
                "text/plain",
                null
        );
        ShareLink expiredShare = createStoredShareLink(
                registered,
                ShareResourceType.FILE,
                uploaded.id(),
                "expired-" + UUID.randomUUID(),
                Instant.now().minusSeconds(60)
        );

        mockMvc.perform(get("/api/v1/public/shares/{token}", expiredShare.getToken()))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("Share link is no longer available"));
    }

    @Test
    void shouldReturnGoneForRevokedShare() throws Exception {
        AuthResponse registered = register("file-owner@example.com", "Password123", "File Owner");
        FileResponse uploaded = uploadFile(
                registered,
                "revoked content".getBytes(StandardCharsets.UTF_8),
                "revoked.txt",
                "text/plain",
                null
        );
        ShareLink revokedShare = createStoredShareLink(
                registered,
                ShareResourceType.FILE,
                uploaded.id(),
                "revoked-" + UUID.randomUUID(),
                Instant.now().plusSeconds(3600)
        );
        revokedShare.setRevokedAt(Instant.now());
        shareLinkRepository.saveAndFlush(revokedShare);

        mockMvc.perform(get("/api/v1/public/shares/{token}", revokedShare.getToken()))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("Share link is no longer available"));
    }

    @Test
    void shouldPreventRevokingAnotherUsersShare() throws Exception {
        AuthResponse owner = register("owner@example.com", "Password123", "Owner");
        AuthResponse anotherUser = register("another@example.com", "Password123", "Another User");
        FileResponse uploaded = uploadFile(
                owner,
                "private share".getBytes(StandardCharsets.UTF_8),
                "private.txt",
                "text/plain",
                null
        );
        ShareResponse share = createShare(owner, ShareResourceType.FILE, uploaded.id(), Instant.now().plusSeconds(3600));

        mockMvc.perform(delete("/api/v1/shares/{shareId}", share.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Share link not found"));

        assertThat(shareLinkRepository.findById(share.id()).orElseThrow().getRevokedAt()).isNull();
    }
}
