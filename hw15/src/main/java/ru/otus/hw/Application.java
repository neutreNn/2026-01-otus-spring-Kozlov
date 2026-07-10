package ru.otus.hw;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import ru.otus.hw.domain.CoffeeOrder;
import ru.otus.hw.domain.CoffeeSize;
import ru.otus.hw.domain.CoffeeType;
import ru.otus.hw.gateway.CoffeeShopGateway;

import java.util.List;

@EnableIntegration
@IntegrationComponentScan
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner demo(CoffeeShopGateway coffeeShopGateway) {
        return args -> List.of(
                        new CoffeeOrder(1, "Ivan", CoffeeType.CAPPUCCINO, CoffeeSize.MEDIUM),
                        new CoffeeOrder(2, "Olga", CoffeeType.ICED_LATTE, CoffeeSize.LARGE),
                        new CoffeeOrder(3, "Maria", CoffeeType.ESPRESSO, CoffeeSize.SMALL)
                )
                .stream()
                .map(coffeeShopGateway::process)
                .forEach(coffee -> System.out.printf(
                        "Order %d for %s: %s %s prepared at %s%n",
                        coffee.orderId(),
                        coffee.customerName(),
                        coffee.size().getTitle(),
                        coffee.title(),
                        coffee.station().getTitle()
                ));
    }
}
