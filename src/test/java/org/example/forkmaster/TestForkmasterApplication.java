package org.example.forkmaster;

import org.springframework.boot.SpringApplication;

public class TestForkmasterApplication {

    public static void main(String[] args) {
        SpringApplication.from(ForkmasterApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
