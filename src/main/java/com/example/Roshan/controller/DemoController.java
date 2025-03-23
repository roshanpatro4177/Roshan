package com.example.Roshan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/")
    public String home() {
        return "Webhook is running!";
    }

    @GetMapping("/hello")
    public String helloWorld(){
        return "Welcome Roshan !!!";
    }

}
