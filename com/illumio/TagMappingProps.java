package com.illumio;

import java.util.Map;
import java.util.Objects;

public record TagMappingProps(String dstport, String protocol) {
    private static final String UNKNOWN_PROTOCOL = "unknown";

    public static TagMappingProps fromLogFields(String[] logFields, Map<String, String> decimalToProtocol) {
        // In default format of flow logs fields `dstport` and `protocol` have 6 and 7 indexes (0-indexed)
        String dstport = logFields[6];
        String protocol = decimalToProtocol.getOrDefault(logFields[7], UNKNOWN_PROTOCOL);
        return new TagMappingProps(dstport, protocol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagMappingProps that = (TagMappingProps) o;
        return Objects.equals(dstport, that.dstport) && Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dstport, protocol);
    }
}
