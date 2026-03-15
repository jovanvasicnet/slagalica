package com.securevault;

import com.securevault.security.JwtFilter;
import com.securevault.service.WordService;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCrypt;

@SpringBootApplication
@RestController
public class ServerApp {
    @PostConstruct
    public void init(){
        WordService.loadWords();
    }
    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilter() {

        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(new JwtFilter());


        return registration;
    }

}
