package ru.otus.homevault.notes;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ru.otus.homevault.auth.dto.AuthResponse;
import ru.otus.homevault.notes.dto.NoteResponse;
import ru.otus.homevault.notes.dto.UpdateNoteRequest;
import ru.otus.homevault.support.IntegrationTestSupport;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotesApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldCreateNoteWithNormalizedTags() throws Exception {
        AuthResponse registered = register("note-owner@example.com", "Password123", "Note Owner");

        NoteResponse note = createNote(
                registered,
                "  Deployment checklist  ",
                "Remember to check health endpoint",
                Set.of(" Work ", "DEVOPS", "", "work")
        );

        assertThat(note.id()).isNotNull();
        assertThat(note.title()).isEqualTo("Deployment checklist");
        assertThat(note.content()).isEqualTo("Remember to check health endpoint");
        assertThat(note.tags()).containsExactlyInAnyOrder("devops", "work");
        assertThat(noteRepository.findAll()).hasSize(1);
        assertThat(auditEventRepository.findAll())
                .extracting("action")
                .contains("NOTE_CREATED");
    }

    @Test
    void shouldUpdateNote() throws Exception {
        AuthResponse registered = register("note-owner@example.com", "Password123", "Note Owner");
        NoteResponse created = createNote(registered, "Draft", "Initial content", Set.of("draft"));

        String responseBody = mockMvc.perform(put("/api/v1/notes/{noteId}", created.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateNoteRequest("Updated", "Updated content", Set.of(" Done ", "WORK"))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.content").value("Updated content"))
                .andExpect(jsonPath("$.tags[0]").value("done"))
                .andExpect(jsonPath("$.tags[1]").value("work"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        NoteResponse updated = objectMapper.readValue(responseBody, NoteResponse.class);
        assertThat(updated.tags()).containsExactlyInAnyOrder("done", "work");
        assertThat(noteRepository.findById(created.id()).orElseThrow().getTitle()).isEqualTo("Updated");
        assertThat(auditEventRepository.findAll())
                .extracting("action")
                .contains("NOTE_UPDATED");
    }

    @Test
    void shouldSearchNotesByQuery() throws Exception {
        AuthResponse registered = register("note-owner@example.com", "Password123", "Note Owner");
        NoteResponse expected = createNote(registered, "Docker compose", "MinIO bucket smoke plan", Set.of("devops"));
        createNote(registered, "Groceries", "Milk and apples", Set.of("personal"));

        mockMvc.perform(get("/api/v1/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered))
                        .param("query", "bucket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(expected.id().toString()))
                .andExpect(jsonPath("$.content[0].title").value("Docker compose"));
    }

    @Test
    void shouldSearchNotesByTag() throws Exception {
        AuthResponse registered = register("note-owner@example.com", "Password123", "Note Owner");
        NoteResponse expected = createNote(registered, "Operations", "Deploy backend", Set.of("Ops", "backend"));
        createNote(registered, "Weekend", "Buy coffee", Set.of("personal"));

        mockMvc.perform(get("/api/v1/notes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered))
                        .param("tag", " OPS "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(expected.id().toString()))
                .andExpect(jsonPath("$.content[0].tags[0]").value("backend"))
                .andExpect(jsonPath("$.content[0].tags[1]").value("ops"));
    }

    @Test
    void shouldHideForeignNote() throws Exception {
        AuthResponse owner = register("owner@example.com", "Password123", "Owner");
        AuthResponse anotherUser = register("another@example.com", "Password123", "Another User");
        NoteResponse note = createNote(owner, "Private note", "Secret content", Set.of("private"));

        mockMvc.perform(get("/api/v1/notes/{noteId}", note.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Note not found"));
    }

    @Test
    void shouldDeleteNote() throws Exception {
        AuthResponse registered = register("note-owner@example.com", "Password123", "Note Owner");
        NoteResponse note = createNote(registered, "Trash", "Delete this note", Set.of("cleanup"));

        mockMvc.perform(delete("/api/v1/notes/{noteId}", note.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(registered)))
                .andExpect(status().isNoContent());

        assertThat(noteRepository.findById(note.id())).isEmpty();
        assertThat(auditEventRepository.findAll())
                .extracting("action")
                .contains("NOTE_DELETED");
    }
}
