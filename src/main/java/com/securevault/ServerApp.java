package com.securevault;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RestController
public class ServerApp {

    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }
}
