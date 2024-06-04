package com.hladunoleksander.semanticcache.core;

import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DefaultSemanticCache implements SemanticCache {

    EmbeddingModel embeddingModel;
    VectorStore vectorStore;

    @Override
    public void set(String key, String value) {
        var embedding = embeddingModel.embed(key);
        vectorStore.save(new VectorDocument(embedding.content().vector(), key, value));
    }

    @Override
    public void set(String key, String value, Duration ttl) {
        var embedding = embeddingModel.embed(key);
        vectorStore.save(new VectorDocument(embedding.content().vector(), key, value), ttl);
    }

    @Override
    public Optional<String> get(String key) {
        var embedding = embeddingModel.embed(key);

        return vectorStore.similaritySearch(embedding.content().vector())
                .map(VectorDocument::value);
    }

}
