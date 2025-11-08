package com.roadwarnings.narino.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // 👈 tus rutas comienzan con /api según tu mensaje en consola
@CrossOrigin(origins = "http://localhost:5173")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}