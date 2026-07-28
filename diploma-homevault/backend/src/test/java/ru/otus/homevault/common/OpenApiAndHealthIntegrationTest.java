package ru.otus.homevault.common;

import org.junit.jupiter.api.Test;
import ru.otus.homevault.support.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiAndHealthIntegrationTest extends IntegrationTestSupport {

    @Test
    void contextLoads() {
    }

    @Test
    void shouldExposeActuatorAndOpenApiDocs() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("HomeVault Backend"))
                .andExpect(jsonPath("$.app.version").value("0.1.0"))
                .andExpect(jsonPath("$.app['storage-provider']").value("minio"));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes['bearer-jwt'].type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes['bearer-jwt'].scheme").value("bearer"))
                .andExpect(jsonPath("$.security[0]['bearer-jwt']").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/auth/register'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/public/shares/{token}'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/files'].post.requestBody.content['multipart/form-data'].schema['$ref']")
                        .value("#/components/schemas/FileUploadForm"))
                .andExpect(jsonPath("$.components.schemas.FileUploadForm.properties.file.format").value("binary"))
                .andExpect(jsonPath("$.paths['/api/v1/files/{fileId}/download'].get.responses['200']"
                        + ".content['application/octet-stream'].schema.format").value("binary"));
    }
}
