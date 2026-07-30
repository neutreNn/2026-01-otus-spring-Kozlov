package ru.otus.homevault.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.homevault.users.model.Role;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.repository.UserRepository;

import java.util.EnumSet;

@Component
@EnableConfigurationProperties(AdminSeedProperties.class)
public class AdminSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeedRunner.class);

    private final AdminSeedProperties properties;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public AdminSeedRunner(
            AdminSeedProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.enabled()) {
            return;
        }

        String email = properties.normalizedEmail();
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = new User(
                email,
                passwordEncoder.encode(properties.password()),
                properties.normalizedDisplayName()
        );
        admin.setRoles(EnumSet.of(Role.USER, Role.ADMIN));
        userRepository.save(admin);
        log.info("Seed admin user created: {}", email);
    }
}
