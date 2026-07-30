package ru.otus.homevault.notes.service;

import org.springframework.stereotype.Component;
import ru.otus.homevault.notes.dto.NoteResponse;
import ru.otus.homevault.notes.model.Note;

import java.util.TreeSet;

@Component
public class NoteMapper {

    public NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                new TreeSet<>(note.getTags()),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
