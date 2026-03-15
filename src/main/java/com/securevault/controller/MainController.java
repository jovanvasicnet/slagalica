package com.securevault.controller;

import com.securevault.model.LoginRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class MainController {

    @GetMapping("/")
    public String home() {
        return "Server radi";
    }

    @GetMapping("/ping")
    public String ping() {
        return "ok";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestBody LoginRequest request) {

        if(request.getUsername().equals("admin") &&
                request.getPassword().equals("1234")) {
            return "login success";
        }

        return "login failed";
    }

}