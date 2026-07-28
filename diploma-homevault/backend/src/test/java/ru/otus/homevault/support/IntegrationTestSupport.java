package ru.otus.homevault.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ru.otus.homevault.audit.repository.AuditEventRepository;
import ru.otus.homevault.auth.dto.AuthResponse;
import ru.otus.homevault.auth.dto.LoginRequest;
import ru.otus.homevault.auth.dto.RegisterRequest;
import ru.otus.homevault.auth.repository.RefreshTokenRepository;
import ru.otus.homevault.folders.dto.CreateFolderRequest;
import ru.otus.homevault.folders.dto.FolderResponse;
import ru.otus.homevault.folders.repository.FolderRepository;
import ru.otus.homevault.notes.dto.CreateNoteRequest;
import ru.otus.homevault.notes.dto.NoteResponse;
import ru.otus.homevault.notes.repository.NoteRepository;
import ru.otus.homevault.sharing.dto.CreateShareRequest;
import ru.otus.homevault.sharing.dto.ShareResponse;
import ru.otus.homevault.sharing.model.ShareLink;
import ru.otus.homevault.sharing.model.ShareResourceType;
import ru.otus.homevault.sharing.repository.ShareLinkRepository;
import ru.otus.homevault.storage.dto.FileResponse;
import ru.otus.homevault.storage.dto.StoredObject;
import ru.otus.homevault.storage.repository.StoredFileRepository;
import ru.otus.homevault.storage.service.FileStorageService;
import ru.otus.homevault.users.model.Role;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.repository.UserRepository;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected FolderRepository folderRepository;

    @Autowired
    protected StoredFileRepository storedFileRepository;

    @Autowired
    protected AuditEventRepository auditEventRepository;

    @Autowired
    protected NoteRepository noteRepository;

    @Autowired
    protected ShareLinkRepository shareLinkRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @MockBean
    protected FileStorageService fileStorageService;

    @BeforeEach
    void cleanDatabase() {
        auditEventRepository.deleteAll();
        shareLinkRepository.deleteAll();
        storedFileRepository.deleteAll();
        folderRepository.deleteAll();
        noteRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        reset(fileStorageService);
    }

    protected AuthResponse register(String email, String password, String displayName) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, password, displayName))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseBody, AuthResponse.class);
    }

    protected AuthResponse login(String email, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseBody, AuthResponse.class);
    }

    protected AuthResponse createAdminAndLogin(String email, String password, String displayName) throws Exception {
        User admin = new User(email, passwordEncoder.encode(password), displayName);
        admin.setRoles(Set.of(Role.ADMIN));
        userRepository.saveAndFlush(admin);
        return login(email, password);
    }

    protected FolderResponse createFolder(AuthResponse authResponse, String name, UUID parentId) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/folders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authResponse))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateFolderRequest(name, parentId))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseBody, FolderResponse.class);
    }

    protected FileResponse uploadFile(
            AuthResponse authResponse,
            byte[] content,
            String originalName,
            String contentType,
            UUID folderId
    ) throws Exception {
        configureStorageUploadMock();
        MockMultipartFile file = new MockMultipartFile("file", originalName, contentType, content);
        var requestBuilder = multipart("/api/v1/files")
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, bearer(authResponse));
        if (folderId != null) {
            requestBuilder.param("folderId", folderId.toString());
        }

        String responseBody = mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseBody, FileResponse.class);
    }

    protected NoteResponse createNote(
            AuthResponse authResponse,
            String title,
            String content,
            Set<String> tags
    ) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authResponse))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateNoteRequest(title, content, tags))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseBody, NoteResponse.class);
    }

    protected ShareResponse createShare(
            AuthResponse authResponse,
            ShareResourceType resourceType,
            UUID resourceId,
            Instant expiresAt
    ) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/shares")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authResponse))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CreateShareRequest(resourceType, resourceId, expiresAt))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseBody, ShareResponse.class);
    }

    protected ShareLink createStoredShareLink(
            AuthResponse authResponse,
            ShareResourceType resourceType,
            UUID resourceId,
            String token,
            Instant expiresAt
    ) {
        User owner = userRepository.findById(authResponse.user().id()).orElseThrow();
        return shareLinkRepository.saveAndFlush(new ShareLink(owner, resourceType, resourceId, token, expiresAt));
    }

    protected String bearer(AuthResponse authResponse) {
        return "Bearer " + authResponse.accessToken();
    }

    private void configureStorageUploadMock() {
        when(fileStorageService.put(anyString(), any(InputStream.class), anyLong(), anyString()))
                .thenAnswer(invocation -> {
                    String storageKey = invocation.getArgument(0, String.class);
                    InputStream inputStream = invocation.getArgument(1, InputStream.class);
                    Long sizeBytes = invocation.getArgument(2, Long.class);
                    String contentType = invocation.getArgument(3, String.class);
                    inputStream.transferTo(OutputStream.nullOutputStream());
                    return new StoredObject(storageKey, sizeBytes, contentType);
                });
    }
}
