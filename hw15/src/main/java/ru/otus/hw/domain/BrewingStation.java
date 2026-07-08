package ru.otus.hw.domain;

public enum BrewingStation {
    HOT_BAR("hot bar"),
    COLD_BAR("cold bar");

    private final String title;

    BrewingStation(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
