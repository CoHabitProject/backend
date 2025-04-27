package com.esgi.cohabit.adapters.rest.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
}
