package ru.otus.hw.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.domain.BrewingStation;
import ru.otus.hw.domain.CoffeeOrder;
import ru.otus.hw.domain.CoffeeSize;
import ru.otus.hw.domain.CoffeeType;
import ru.otus.hw.gateway.CoffeeShopGateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CoffeeShopIntegrationFlowTest {

    @Autowired
    private CoffeeShopGateway coffeeShopGateway;

    @Test
    void shouldPrepareHotDrinkViaHotSubflow() {
        var order = new CoffeeOrder(10, " Ivan ", CoffeeType.CAPPUCCINO, CoffeeSize.MEDIUM);

        var coffee = coffeeShopGateway.process(order);

        assertThat(coffee.orderId()).isEqualTo(order.id());
        assertThat(coffee.customerName()).isEqualTo("Ivan");
        assertThat(coffee.title()).isEqualTo("cappuccino");
        assertThat(coffee.size()).isEqualTo(CoffeeSize.MEDIUM);
        assertThat(coffee.station()).isEqualTo(BrewingStation.HOT_BAR);
        assertThat(coffee.steps())
                .containsExactly("grind beans", "extract espresso", "steam milk", "seal cup");
    }

    @Test
    void shouldPrepareColdDrinkViaColdSubflow() {
        var order = new CoffeeOrder(11, "Olga", CoffeeType.ICED_LATTE, CoffeeSize.LARGE);

        var coffee = coffeeShopGateway.process(order);

        assertThat(coffee.orderId()).isEqualTo(order.id());
        assertThat(coffee.customerName()).isEqualTo(order.customerName());
        assertThat(coffee.title()).isEqualTo("iced latte");
        assertThat(coffee.size()).isEqualTo(CoffeeSize.LARGE);
        assertThat(coffee.station()).isEqualTo(BrewingStation.COLD_BAR);
        assertThat(coffee.steps())
                .containsExactly("brew concentrate", "fill cup with ice", "add milk foam", "seal cup");
    }

    @Test
    void shouldRejectInvalidOrderBeforeRouting() {
        var order = new CoffeeOrder(12, " ", CoffeeType.LATTE, CoffeeSize.SMALL);

        assertThatThrownBy(() -> coffeeShopGateway.process(order))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer name must not be blank");
    }
}
