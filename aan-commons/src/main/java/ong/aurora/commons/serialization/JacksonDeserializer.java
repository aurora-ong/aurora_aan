package ong.aurora.commons.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JacksonDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private Class<T> destinationClass;
    private Type reflectionTypeToken;

    /**
     * Default constructor needed by Kafka
     */
    public JacksonDeserializer(Class<T> destinationClass) {
        this.destinationClass = destinationClass;
        objectMapper.findAndRegisterModules();
    }

    public JacksonDeserializer(Type reflectionTypeToken) {
        this.reflectionTypeToken = reflectionTypeToken;
        objectMapper.findAndRegisterModules();
    }

    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
    }

    @Override
    public T deserialize(String topic, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        Type type = destinationClass != null ? destinationClass : reflectionTypeToken;

        try {
            return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8), objectMapper.constructType(type));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
    }
}
