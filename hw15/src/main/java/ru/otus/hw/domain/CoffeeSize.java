package ru.otus.hw.domain;

public enum CoffeeSize {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    private final String title;

    CoffeeSize(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
