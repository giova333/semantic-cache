package io.github.giova333.semanticcachespringbootexample;

import io.github.giova333.semanticcache.core.SemanticCache;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
public class SemanticCacheSpringBootExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemanticCacheSpringBootExampleApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(SemanticCache semanticCache) {
        return args -> {
            semanticCache.set("year in which the Berlin wall fell", "1989", Duration.ofSeconds(3));

            semanticCache.get("what's the year the Berlin wall destroyed?")
                    .ifPresentOrElse(
                            System.out::println,
                            () -> System.out.println("No answer found"));
        };
    }

}
