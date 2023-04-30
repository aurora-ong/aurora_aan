package ong.aurora.aan.serialization;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.lang.reflect.Type;

public class JsonSerdes {

    public static <T> Serde<T> getJSONSerde(Class<T> type) {
        JacksonSerializer<T> serializer = new JacksonSerializer<>();
        JacksonDeserializer<T> deserializer = new JacksonDeserializer<>(type);
        return Serdes.serdeFrom(serializer, deserializer);

    }

    public static <T> Serde<T> getJSONSerde(Type type) {
        JacksonSerializer<T> serializer = new JacksonSerializer<>();
        JacksonDeserializer<T> deserializer = new JacksonDeserializer<>(type);
        return Serdes.serdeFrom(serializer, deserializer);

    }
}
