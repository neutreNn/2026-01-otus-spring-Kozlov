package ru.otus.hw.services;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.otus.hw.domain.BrewingStation;
import ru.otus.hw.domain.CoffeeOrder;
import ru.otus.hw.domain.PreparedCoffee;

import java.util.List;

@Service
public class CoffeeKitchenService {

    public CoffeeOrder validate(CoffeeOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Coffee order must not be null");
        }
        if (order.id() <= 0) {
            throw new IllegalArgumentException("Coffee order id must be positive");
        }
        if (!StringUtils.hasText(order.customerName())) {
            throw new IllegalArgumentException("Customer name must not be blank");
        }
        if (order.type() == null) {
            throw new IllegalArgumentException("Coffee type must not be null");
        }
        if (order.size() == null) {
            throw new IllegalArgumentException("Coffee size must not be null");
        }
        return order;
    }

    public PreparedCoffee prepareHotDrink(CoffeeOrder order) {
        return prepare(
                order,
                BrewingStation.HOT_BAR,
                List.of("grind beans", "extract espresso", "steam milk", "seal cup")
        );
    }

    public PreparedCoffee prepareColdDrink(CoffeeOrder order) {
        return prepare(
                order,
                BrewingStation.COLD_BAR,
                List.of("brew concentrate", "fill cup with ice", "add milk foam", "seal cup")
        );
    }

    private PreparedCoffee prepare(CoffeeOrder order, BrewingStation station, List<String> steps) {
        return new PreparedCoffee(
                order.id(),
                order.customerName().trim(),
                order.type().getTitle(),
                order.size(),
                station,
                steps
        );
    }
}
