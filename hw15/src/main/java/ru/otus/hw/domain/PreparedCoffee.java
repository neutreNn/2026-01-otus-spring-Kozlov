package ru.otus.hw.domain;

import java.util.List;

public record PreparedCoffee(long orderId, String customerName, String title, CoffeeSize size,
                             BrewingStation station, List<String> steps) {

    public PreparedCoffee {
        steps = List.copyOf(steps);
    }
}
