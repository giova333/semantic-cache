package io.github.giova333.semanticcache.persistence.redis;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.search.schemafields.TextField;
import redis.clients.jedis.search.schemafields.VectorField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
class RedisSchema {

    static final String SCORE_FIELD_NAME = "vector_score";
    static final String PREFIX = "embedding:";
    static final String JSON_PATH_PREFIX = "$.";
    static final String VECTOR_FIELD_NAME = "vector";
    static final String KEY_FIELD_NAME = "key";
    static final String VALUE_FIELD_NAME = "value";

    String indexName;
    int vectorDimensionality;
    MetricType metricType;
    VectorField.VectorAlgorithm vectorAlgorithm;

    SchemaField[] toSchemaFields() {
        Map<String, Object> vectorAttrs = new HashMap<>();
        vectorAttrs.put("DIM", vectorDimensionality);
        vectorAttrs.put("DISTANCE_METRIC", metricType.name());
        vectorAttrs.put("TYPE", "FLOAT32");
        vectorAttrs.put("INITIAL_CAP", 5);
        List<SchemaField> fields = new ArrayList<>();
        fields.add(TextField.of(JSON_PATH_PREFIX + VALUE_FIELD_NAME).as(VALUE_FIELD_NAME).weight(1.0));
        fields.add(TextField.of(JSON_PATH_PREFIX + KEY_FIELD_NAME).as(KEY_FIELD_NAME).weight(1.0));
        fields.add(VectorField.builder()
                .fieldName(JSON_PATH_PREFIX + VECTOR_FIELD_NAME)
                .algorithm(vectorAlgorithm)
                .attributes(vectorAttrs)
                .as(VECTOR_FIELD_NAME)
                .build());
        return fields.toArray(new SchemaField[0]);
    }
}
