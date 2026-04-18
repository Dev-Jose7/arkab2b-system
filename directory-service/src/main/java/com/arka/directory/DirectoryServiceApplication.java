package com.arka.directory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DirectoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DirectoryServiceApplication.class, args);
    }
}
