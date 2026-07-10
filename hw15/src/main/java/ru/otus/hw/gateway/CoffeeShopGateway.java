package ru.otus.hw.gateway;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.domain.CoffeeOrder;
import ru.otus.hw.domain.PreparedCoffee;

@MessagingGateway
public interface CoffeeShopGateway {

    @Gateway(requestChannel = "coffeeOrdersInput")
    PreparedCoffee process(CoffeeOrder order);
}
