package com.example.fortunecookie.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/fortunes")
public class FortuneController {

    private final List<String> fortunes = new ArrayList<>();
    private final Random random = new Random();

    @PostConstruct
    public void loadFortunes() throws IOException {
        var resource = new ClassPathResource("fortunes.txt");
        try (var in = resource.getInputStream()) {
            var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String line : content.split("\\R")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    fortunes.add(trimmed);
                }
            }
        }
    }

    @GetMapping("/random")
    public Fortune random() {
        return new Fortune(fortunes.get(random.nextInt(fortunes.size())));
    }

    public record Fortune(String message) {}
}
