package io.github.giova333.semanticcache.redis.starter.autoconfiguration;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.github.giova333.semanticcache.core.DefaultSemanticCache;
import io.github.giova333.semanticcache.core.SemanticCache;
import io.github.giova333.semanticcache.core.VectorStore;
import io.github.giova333.semanticcache.persistence.redis.RedisVectorStore;
import io.github.giova333.semanticcache.persistence.redis.RedisVectorStoreProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class RedisSemanticCacheAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "semantic-cache.redis")
    public RedisVectorStoreProperties redisVectorStoreProperties() {
        return new RedisVectorStoreProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public VectorStore vectorStore(RedisVectorStoreProperties properties) {
        return new RedisVectorStore(properties);
    }

    @Bean
    public SemanticCache semanticCache(EmbeddingModel embeddingModel,
                                       VectorStore vectorStore) {
        return new DefaultSemanticCache(embeddingModel, vectorStore);
    }
}
