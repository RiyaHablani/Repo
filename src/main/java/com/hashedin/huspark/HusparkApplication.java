package com.hashedin.huspark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HusparkApplication {

    public static void main(String[] args) {
        SpringApplication.run(HusparkApplication.class, args);
    }

}

