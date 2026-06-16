package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Override
    @Transactional(readOnly = true)
    public Optional<BookComment> findById(long id) {
        return bookCommentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookComment> findByBookId(long bookId) {
        return bookCommentRepository.findByBookId(bookId);
    }

    @Override
    @Transactional
    public BookComment insert(String text, long bookId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        var comment = new BookComment(0, text, book);
        return bookCommentRepository.save(comment);
    }

    @Override
    @Transactional
    public BookComment update(long id, String text) {
        var comment = bookCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book comment with id %d not found".formatted(id)));
        comment.setText(text);
        return bookCommentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookCommentRepository.deleteById(id);
    }
}
