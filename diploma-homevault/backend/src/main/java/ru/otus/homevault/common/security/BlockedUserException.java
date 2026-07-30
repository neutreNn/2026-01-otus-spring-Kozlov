package ru.otus.homevault.common.security;

import org.springframework.security.access.AccessDeniedException;

public class BlockedUserException extends AccessDeniedException {

    public BlockedUserException() {
        super("User is blocked");
    }
}

