package io.github.giova333.semanticcache.core;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultSemanticCacheTest {

    EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
    VectorStore vectorStore = mock(VectorStore.class);
    SemanticCache semanticCache = new DefaultSemanticCache(embeddingModel, vectorStore);

    @Test
    void shouldSetValueInSemanticCache() {
        var key = "largest city in USA by population";
        var value = "New York";

        var embedding = new Embedding(new float[]{1.0f, 2.0f, 3.0f});
        given(embeddingModel.embed(key)).willReturn(Response.from(embedding));

        semanticCache.set(key, value);

        verify(vectorStore).save(new VectorDocument(embedding.vector(), key, value));
    }

    @Test
    void shouldSetValueInSemanticCacheWithTtl() {
        var key = "largest city in USA by population";
        var value = "New York";

        var embedding = new Embedding(new float[]{1.0f, 2.0f, 3.0f});
        given(embeddingModel.embed(key)).willReturn(Response.from(embedding));

        var ttl = Duration.ofMinutes(5);
        semanticCache.set(key, value, ttl);

        verify(vectorStore).save(new VectorDocument(embedding.vector(), key, value), ttl);
    }

    @Test
    void shouldGetValueWhenFound() {
        var key = "largest city in USA by population";
        var value = "New York";

        var embedding = new Embedding(new float[]{1.0f, 2.0f, 3.0f});
        given(embeddingModel.embed(key)).willReturn(Response.from(embedding));

        var vectorDocument = new VectorDocument(embedding.vector(), key, value);
        given(vectorStore.similaritySearch(embedding.vector())).willReturn(Optional.of(vectorDocument));

        var actualResult = semanticCache.get(key);

        assertThat(actualResult).contains(value);
    }

    @Test
    void shouldReturnEmptyOptionalWhenNotFound() {
        var key = "largest city in USA by population";

        var embedding = new Embedding(new float[]{1.0f, 2.0f, 3.0f});
        given(embeddingModel.embed(key)).willReturn(Response.from(embedding));

        given(vectorStore.similaritySearch(embedding.vector())).willReturn(Optional.empty());

        var actualResult = semanticCache.get(key);

        assertThat(actualResult).isEmpty();
    }
}