package com.hladunoleksander.semanticcache.persistence.redis;

import com.google.gson.Gson;
import com.hladunoleksander.semanticcache.core.VectorDocument;
import com.hladunoleksander.semanticcache.core.VectorStore;
import lombok.experimental.FieldDefaults;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;
import static redis.clients.jedis.search.IndexDefinition.Type.JSON;
import static redis.clients.jedis.search.RediSearchUtil.toByteArray;

@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class RedisVectorStore implements VectorStore {

    private static final Gson GSON = new Gson();

    JedisPooled client;
    RedisSchema redisSchema;
    Supplier<UUID> idGenerator;
    RedisVectorStoreProperties properties;

    public RedisVectorStore(RedisVectorStoreProperties properties,
                            Supplier<UUID> idGenerator) {
        this.redisSchema = RedisSchema.builder()
                .vectorDimensionality(properties.getVectorDimensionality())
                .vectorAlgorithm(properties.getVectorAlgorithm())
                .indexName(properties.getIndexName())
                .metricType(properties.getMetricType())
                .build();
        this.client = properties.getUser() == null
                ? new JedisPooled(properties.getHost(), properties.getPort())
                : new JedisPooled(properties.getHost(), properties.getPort(), properties.getUser(), properties.getPassword());
        this.idGenerator = idGenerator;
        this.properties = properties;

        if (!isIndexExist(properties.getIndexName())) {
            createIndex(properties.getIndexName());
        }
    }

    public RedisVectorStore(RedisVectorStoreProperties properties) {
        this(properties, UUID::randomUUID);
    }

    @Override
    public void save(VectorDocument document) {
        var key = RedisSchema.PREFIX + idGenerator.get();
        saveInternal(key, document);
    }

    @Override
    public void save(VectorDocument document, Duration ttl) {
        var key = RedisSchema.PREFIX + idGenerator.get();
        saveInternal(key, document);
        client.expire(key, (int) ttl.getSeconds());
    }

    @Override
    public Optional<VectorDocument> similaritySearch(float[] queryVector) {
        String queryTemplate = "*=>[ KNN 1 @%s $BLOB AS %s ]";
        var returnFields = List.of(
                RedisSchema.VECTOR_FIELD_NAME,
                RedisSchema.KEY_FIELD_NAME,
                RedisSchema.VALUE_FIELD_NAME,
                RedisSchema.SCORE_FIELD_NAME
        );

        Query query = new Query(format(queryTemplate, RedisSchema.VECTOR_FIELD_NAME, RedisSchema.SCORE_FIELD_NAME))
                .addParam("BLOB", toByteArray(queryVector))
                .returnFields(returnFields.toArray(new String[0]))
                .setSortBy(RedisSchema.SCORE_FIELD_NAME, true)
                .dialect(2);

        SearchResult result = client.ftSearch(redisSchema.getIndexName(), query);
        var documents = result.getDocuments();

        return documents.stream()
                .filter(this::notExpired)
                .filter(this::matchesSimilarityThreshold)
                .map(this::toVectorDocument)
                .findFirst();
    }

    private VectorDocument toVectorDocument(Document doc) {
        return VectorDocument.builder()
                .vector(GSON.fromJson(doc.getString(RedisSchema.VECTOR_FIELD_NAME), float[].class))
                .key(doc.getString(RedisSchema.KEY_FIELD_NAME))
                .value(doc.getString(RedisSchema.VALUE_FIELD_NAME))
                .build();
    }

    /**
     * Redis can temporarily return expired document. The expired document have not fields just id.
     */
    private boolean notExpired(Document document) {
        return document.hasProperty(RedisSchema.VECTOR_FIELD_NAME);
    }

    private boolean matchesSimilarityThreshold(Document doc) {
        double score = (2 - Double.parseDouble(doc.getString(RedisSchema.SCORE_FIELD_NAME))) / 2;
        return score >= properties.getSimilarityThreshold();
    }

    private void saveInternal(String key, VectorDocument document) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(RedisSchema.VECTOR_FIELD_NAME, document.vector());
        fields.put(RedisSchema.KEY_FIELD_NAME, document.key());
        fields.put(RedisSchema.VALUE_FIELD_NAME, document.value());

        client.jsonSetWithEscape(key, Path2.of("$"), fields);
    }

    private boolean isIndexExist(String indexName) {
        var indexes = client.ftList();
        return indexes.contains(indexName);
    }

    private void createIndex(String indexName) {
        var indexDefinition = new IndexDefinition(JSON);
        indexDefinition.setPrefixes(RedisSchema.PREFIX);
        String res = client.ftCreate(indexName, FTCreateParams.createParams()
                .on(IndexDataType.JSON)
                .addPrefix(RedisSchema.PREFIX), redisSchema.toSchemaFields());
        if (!"OK".equals(res)) {
            throw new IllegalStateException("create index error, msg=" + res);
        }
    }
}
