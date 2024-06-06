package io.github.giova333.semanticcache.persistence.redis;

import io.github.giova333.semanticcache.core.VectorDocument;
import io.github.giova333.semanticcache.core.VectorStore;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Durations;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
class RedisVectorStoreTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis/redis-stack:latest"))
                    .withExposedPorts(6379);

    VectorStore vectorStore = redisVectorStore();

    @Test
    void shouldSaveVectorDocument() {
        var vector = new float[]{1.0f, 2.0f, 3.0f};
        var vectorDocument = new VectorDocument(vector, "Some query1", "Some answer1");

        vectorStore.save(vectorDocument);

        var savedDocument = vectorStore.similaritySearch(vector);
        assertThat(savedDocument).hasValue(vectorDocument);
    }

    @Test
    void shouldSaveVectorDocumentWithTtl() {
        var vector = new float[]{15.3f, 24.7f, 45.2f};
        var vectorDocument = new VectorDocument(vector, "Some query2", "Some answer2");

        vectorStore.save(vectorDocument, Duration.ofSeconds(3));

        var savedDocument = vectorStore.similaritySearch(vector);
        assertThat(savedDocument).hasValue(vectorDocument);

        await().atMost(Durations.FIVE_MINUTES).until(() -> {
            var document = vectorStore.similaritySearch(vector);
            return document.isEmpty();
        });
    }

    @Test
    void shouldFindSimilarDocument() {
        var vector1 = new float[]{150.3f, 240.7f, 450.2f};
        var vectorDocument1 = new VectorDocument(vector1, "Some query3", "Some answer3");

        var vector2 = new float[]{1500.3f, 2400.7f, 4500.2f};
        var vectorDocument2 = new VectorDocument(vector2, "Some query4", "Some answer4");

        vectorStore.save(vectorDocument1);
        vectorStore.save(vectorDocument2);

        var actualResult = vectorStore.similaritySearch(vector2);

        assertThat(actualResult).hasValue(vectorDocument2);
    }

    private VectorStore redisVectorStore() {
        var properties = RedisVectorStoreProperties.builder()
                .host(REDIS.getHost())
                .port(REDIS.getFirstMappedPort())
                .indexName("test")
                .vectorDimensionality(3)
                .similarityThreshold(0.99)
                .build();
        return new RedisVectorStore(properties);
    }
}