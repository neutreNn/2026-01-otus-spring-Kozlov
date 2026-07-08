package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import ru.otus.hw.domain.CoffeeOrder;
import ru.otus.hw.services.CoffeeKitchenService;

@Configuration
public class CoffeeShopIntegrationConfig {

    private static final String COFFEE_ORDERS_INPUT_CHANNEL = "coffeeOrdersInput";

    private static final String HOT_ORDERS_CHANNEL = "hotCoffeeOrders";

    private static final String COLD_ORDERS_CHANNEL = "coldCoffeeOrders";

    @Bean
    MessageChannel coffeeOrdersInput() {
        return new DirectChannel();
    }

    @Bean
    MessageChannel hotCoffeeOrders() {
        return new DirectChannel();
    }

    @Bean
    MessageChannel coldCoffeeOrders() {
        return new DirectChannel();
    }

    @Bean
    IntegrationFlow coffeeOrderFlow(CoffeeKitchenService coffeeKitchenService) {
        return IntegrationFlow.from(COFFEE_ORDERS_INPUT_CHANNEL)
                .handle(coffeeKitchenService, "validate")
                .route(CoffeeOrder.class, CoffeeOrder::isCold, mapping -> mapping
                        .subFlowMapping(false, hotDrinkFlow(coffeeKitchenService))
                        .subFlowMapping(true, coldDrinkFlow(coffeeKitchenService)))
                .get();
    }

    private IntegrationFlow hotDrinkFlow(CoffeeKitchenService coffeeKitchenService) {
        return flow -> flow
                .channel(HOT_ORDERS_CHANNEL)
                .handle(coffeeKitchenService, "prepareHotDrink");
    }

    private IntegrationFlow coldDrinkFlow(CoffeeKitchenService coffeeKitchenService) {
        return flow -> flow
                .channel(COLD_ORDERS_CHANNEL)
                .handle(coffeeKitchenService, "prepareColdDrink");
    }
}
