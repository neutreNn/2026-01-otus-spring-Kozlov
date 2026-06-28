package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookCommentServiceImpl implements BookCommentService {
    private final BookRepository bookRepository;

    private final BookCommentRepository bookCommentRepository;

    private final SequenceService sequenceService;

    @Override
    public Optional<BookComment> findById(long id) {
        return bookCommentRepository.findById(id);
    }

    @Override
    public List<BookComment> findByBookId(long bookId) {
        return bookCommentRepository.findByBookId(bookId, Sort.by("id"));
    }

    @Override
    public BookComment insert(String text, long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        var comment = new BookComment(sequenceService.getNextSequence("book_comments"), text, bookId);
        return bookCommentRepository.save(comment);
    }

    @Override
    public BookComment update(long id, String text) {
        var comment = bookCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book comment with id %d not found".formatted(id)));
        comment.setText(text);
        return bookCommentRepository.save(comment);
    }

    @Override
    public void deleteById(long id) {
        bookCommentRepository.deleteById(id);
    }
}
