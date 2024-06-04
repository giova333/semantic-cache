package com.hladunoleksander.semanticcache.core;

import java.time.Duration;
import java.util.Optional;

public interface VectorStore {

    void save(VectorDocument vectorDocument);

    void save(VectorDocument vectorDocument, Duration ttl);

    Optional<VectorDocument> similaritySearch(float[] vector);
}
