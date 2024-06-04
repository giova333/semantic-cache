package com.hladunoleksander.semanticcache.persistence.redis;

/**
 * Similarity metric used by Redis
 */
enum MetricType {

    /**
     * cosine similarity
     */
    COSINE,

    /**
     * inner product
     */
    IP,

    /**
     * euclidean distance
     */
    L2
}

