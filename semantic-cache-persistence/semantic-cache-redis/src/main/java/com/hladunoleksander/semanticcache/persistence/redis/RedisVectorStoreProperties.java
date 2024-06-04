package com.hladunoleksander.semanticcache.persistence.redis;

import com.hladunoleksander.semanticcache.core.SemanticCacheProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import redis.clients.jedis.search.schemafields.VectorField;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedisVectorStoreProperties implements SemanticCacheProperties {
    String host;
    @Builder.Default
    int port = 6379;
    String user;
    String password;
    @Builder.Default
    String indexName = "semantic-cache-index";
    int vectorDimensionality;
    @Builder.Default
    VectorField.VectorAlgorithm vectorAlgorithm = VectorField.VectorAlgorithm.HNSW;
    @Builder.Default
    MetricType metricType = MetricType.COSINE;
    @Builder.Default
    double similarityThreshold = 0.95;
}
