package ong.aurora.aan.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JacksonSerializer<T> implements Serializer<T> {
  private final ObjectMapper objectMapper =
      new ObjectMapper();

  /** Default constructor needed by Kafka */
  public JacksonSerializer() {
    objectMapper.findAndRegisterModules();
  }

  @Override
  public void configure(Map<String, ?> props, boolean isKey) {}

  @Override
  public byte[] serialize(String topic, T type) {
    try {
      return objectMapper.writeValueAsBytes(type);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {}

  public static Map<String, Object> toMap(Object obj) {
    ObjectMapper oMapper = new ObjectMapper();
    Map<String, Object> map = oMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});

    return map;
  }
}
