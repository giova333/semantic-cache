package io.github.giova333.semanticcache;

import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import io.github.giova333.semanticcache.core.DefaultSemanticCache;
import io.github.giova333.semanticcache.core.SemanticCache;
import io.github.giova333.semanticcache.persistence.redis.RedisVectorStore;
import io.github.giova333.semanticcache.persistence.redis.RedisVectorStoreProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisSemanticCacheFunctionalTest extends BaseFunctionalTest {

    static final String EMBEDDING_MODEL_NAME = "all-minilm";

    @Container
    static OllamaContainer OLLAMA = new OllamaContainer("ollama/ollama");

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis/redis-stack:latest"))
                    .withExposedPorts(6379);

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        OLLAMA.execInContainer("ollama", "pull", EMBEDDING_MODEL_NAME);
    }

    @Test
    void shouldGetAnswerFromCache() {
        var semanticCache = redisSemanticCache();

        semanticCache.set("largest city in USA by population", "New York");

        var result = semanticCache.get("most populated city in the USA?");

        assertThat(result).contains("New York");
    }

    @Test
    void shouldReturnEmptyResponse() {
        var semanticCache = redisSemanticCache();

        semanticCache.set("Capital of France", "Paris");

        var result = semanticCache.get("year in which the Berlin wall fell");

        assertThat(result).isEmpty();
    }

    private SemanticCache redisSemanticCache() {
        var redisVectorStoreProperties = RedisVectorStoreProperties.builder()
                .host(REDIS.getHost())
                .port(REDIS.getFirstMappedPort())
                .vectorDimensionality(384)
                .similarityThreshold(0.9)
                .build();

        var redisVectorStore = new RedisVectorStore(redisVectorStoreProperties);

        var embeddingModel = OllamaEmbeddingModel.builder()
                .modelName(EMBEDDING_MODEL_NAME)
                .baseUrl(String.format("http://%s:%d", OLLAMA.getHost(), OLLAMA.getFirstMappedPort()))
                .build();

        return new DefaultSemanticCache(embeddingModel, redisVectorStore);
    }
}
