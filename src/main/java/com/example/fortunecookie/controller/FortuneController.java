package com.example.fortunecookie.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/fortunes")
@ImportRuntimeHints(FortuneController.FortuneHints.class)
public class FortuneController {

    private final List<Fortune> fortunes = new ArrayList<>();
    private final Random random = new Random();

    @PostConstruct
    public void loadFortunes() throws IOException {
        var resource = new ClassPathResource("fortunes.txt");
        try (var in = resource.getInputStream()) {
            var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String line : content.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int sep = trimmed.indexOf('|');
                if (sep >= 0) {
                    String message = trimmed.substring(0, sep).trim();
                    String url = trimmed.substring(sep + 1).trim();
                    fortunes.add(new Fortune(message, url.isEmpty() ? null : url));
                } else {
                    fortunes.add(new Fortune(trimmed, null));
                }
            }
        }
    }

    @GetMapping("/random")
    public Fortune random() {
        return fortunes.get(random.nextInt(fortunes.size()));
    }

    public record Fortune(String message, String url) {}

    static class FortuneHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources().registerPattern("fortunes.txt");
        }
    }
}

