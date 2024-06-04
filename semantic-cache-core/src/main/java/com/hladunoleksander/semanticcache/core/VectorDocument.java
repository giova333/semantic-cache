package com.hladunoleksander.semanticcache.core;

import lombok.Builder;

import java.util.Arrays;
import java.util.Objects;

@Builder
public record VectorDocument(float[] vector, String key, String value) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorDocument that = (VectorDocument) o;
        return Arrays.equals(vector, that.vector) && Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(key, value);
        result = 31 * result + Arrays.hashCode(vector);
        return result;
    }
}
