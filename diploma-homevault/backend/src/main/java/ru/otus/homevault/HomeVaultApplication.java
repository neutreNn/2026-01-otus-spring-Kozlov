package ru.otus.homevault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HomeVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeVaultApplication.class, args);
    }
}
