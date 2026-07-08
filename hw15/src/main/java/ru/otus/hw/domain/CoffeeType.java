package ru.otus.hw.domain;

public enum CoffeeType {
    ESPRESSO("espresso", false),
    CAPPUCCINO("cappuccino", false),
    LATTE("latte", false),
    COLD_BREW("cold brew", true),
    ICED_LATTE("iced latte", true);

    private final String title;

    private final boolean cold;

    CoffeeType(String title, boolean cold) {
        this.title = title;
        this.cold = cold;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCold() {
        return cold;
    }
}
