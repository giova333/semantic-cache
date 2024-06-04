package com.hladunoleksander.semanticcache.example.java;

import com.hladunoleksander.semanticcache.core.DefaultSemanticCache;
import com.hladunoleksander.semanticcache.persistence.redis.RedisVectorStore;
import com.hladunoleksander.semanticcache.persistence.redis.RedisVectorStoreProperties;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

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
