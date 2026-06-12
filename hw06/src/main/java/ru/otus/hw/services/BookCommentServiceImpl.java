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
        return save(0, text, bookId);
    }

    @Override
    @Transactional
    public BookComment update(long id, String text, long bookId) {
        return save(id, text, bookId);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookCommentRepository.deleteById(id);
    }

    private BookComment save(long id, String text, long bookId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        var comment = new BookComment(id, text, book);
        return bookCommentRepository.save(comment);
    }
}
