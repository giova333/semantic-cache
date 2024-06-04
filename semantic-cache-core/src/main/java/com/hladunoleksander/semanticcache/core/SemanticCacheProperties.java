package com.hladunoleksander.semanticcache.core;

public interface SemanticCacheProperties {

    /**
     * The similarityThreshold parameter ranges from 0 to 1. It lets you define the minimum relevance score to determine a cache hit.
     * The higher this number, the more similar your user input must be to the cached content to be a hit.
     * In practice, a score of 0.95 indicates a very high similarity, while a score of 0.75 already indicates a low similarity.
     * For example, a value of 1.00, the highest possible, would only accept an exact match of your user query and cache content as a cache hit.
     *
     * @return the similarityThreshold
     */
    double getSimilarityThreshold();
}
