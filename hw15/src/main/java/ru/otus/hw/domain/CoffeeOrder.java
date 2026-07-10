package ru.otus.hw.domain;

public record CoffeeOrder(long id, String customerName, CoffeeType type, CoffeeSize size) {

    public boolean isCold() {
        return type.isCold();
    }
}
