package io.github.giova333.semanticcache.example.java;

import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import io.github.giova333.semanticcache.core.DefaultSemanticCache;
import io.github.giova333.semanticcache.persistence.redis.RedisVectorStore;
import io.github.giova333.semanticcache.persistence.redis.RedisVectorStoreProperties;

import java.time.Duration;

public class Main {

    public static void main(String[] args) {
        var redisVectorStoreProperties = RedisVectorStoreProperties.builder()
                .host("localhost")
                .port(6379)
                .vectorDimensionality(1536)
                .build();

        var redisVectorStore = new RedisVectorStore(redisVectorStoreProperties);

        var embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey("YOUR_OPEN_AI_API_KEY")
                .modelName("text-embedding-ada-002")
                .build();

        var semanticCache = new DefaultSemanticCache(embeddingModel, redisVectorStore);

        semanticCache.set("year in which the Berlin wall fell", "1989", Duration.ofSeconds(3));

        semanticCache.get("what's the year the Berlin wall destroyed?")
                .ifPresentOrElse(
                        System.out::println,
                        () -> System.out.println("No answer found"));
    }
}
