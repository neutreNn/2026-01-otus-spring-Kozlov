package ru.otus.hw.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.models.AppUser;
import ru.otus.hw.repositories.AppUserRepository;
import ru.otus.hw.repositories.BookRepository;

@RequiredArgsConstructor
@Service("bookAuthorizationService")
public class BookAuthorizationService {
    private final AppUserRepository appUserRepository;

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public boolean canCreate(Authentication authentication, BookCreateDto bookCreateDto) {
        if (bookCreateDto == null || bookCreateDto.getGenreId() == null) {
            return false;
        }
        return hasAuthority(authentication, "ROLE_ADMIN")
                || hasAuthority(authentication, "ROLE_EDITOR")
                && canManageGenre(authentication, bookCreateDto.getGenreId());
    }

    @Transactional(readOnly = true)
    public boolean canUpdate(Authentication authentication, BookUpdateDto bookUpdateDto) {
        if (bookUpdateDto == null || bookUpdateDto.getId() == null || bookUpdateDto.getGenreId() == null) {
            return false;
        }
        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            return true;
        }
        if (!hasAuthority(authentication, "ROLE_EDITOR")) {
            return false;
        }
        return canManageGenre(authentication, bookUpdateDto.getGenreId())
                && bookRepository.findById(bookUpdateDto.getId())
                .map(book -> canManageGenre(authentication, book.getGenre().getId()))
                .orElse(false);
    }

    public boolean canDelete(Authentication authentication, BookIdDto bookIdDto) {
        return bookIdDto != null && hasAuthority(authentication, "ROLE_ADMIN");
    }

    private boolean canManageGenre(Authentication authentication, long genreId) {
        return appUserRepository.findByUsername(authentication.getName())
                .filter(AppUser::isEnabled)
                .map(AppUser::getManagedGenre)
                .map(genre -> genre.getId() == genreId)
                .orElse(false);
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
