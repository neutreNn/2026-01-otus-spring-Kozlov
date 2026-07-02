package ru.otus.hw.models;

public enum UserRole {
    USER,
    EDITOR,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
