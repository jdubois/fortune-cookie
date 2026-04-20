package com.example.fortunecookie.controller;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/roasts")
@ImportRuntimeHints(RoastController.RoastHints.class)
public class RoastController {

    private static final String RESOURCE = "roasts.json";

    private List<Roast> roasts = List.of();
    private final Random random = new Random();

    @PostConstruct
    public void loadRoasts() throws IOException {
        var mapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        var resource = new ClassPathResource(RESOURCE);
        try (var in = resource.getInputStream()) {
            this.roasts = List.copyOf(mapper.readerForListOf(Roast.class).readValue(in));
        }
    }

    @GetMapping("/random")
    public Roast random() {
        return roasts.get(random.nextInt(roasts.size()));
    }

    @GetMapping("/count")
    public Count count() {
        return new Count(roasts.size());
    }

    public record Roast(long id, String title, String url, String lang, String roast) {}

    public record Count(int total) {}

    static class RoastHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources().registerPattern(RESOURCE);
            hints.reflection().registerType(Roast.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);
            hints.reflection().registerType(Count.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);
        }
    }
}
