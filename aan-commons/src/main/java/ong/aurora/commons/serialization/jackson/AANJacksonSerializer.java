package ong.aurora.commons.serialization.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ong.aurora.commons.serialization.AANSerializer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class AANJacksonSerializer implements AANSerializer {

    private ObjectMapper objectMapper;

    public AANJacksonSerializer() {
        this.objectMapper =
                new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public String toJSON(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T fromJSON(String json, Type tClass) {
        try {
            return objectMapper.readValue(json, objectMapper.constructType(tClass));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] toBytes(Object object) {
        return this.toJSON(object).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T fromBytes(byte[] bytes, Type tClass) {
        return this.fromJSON(new String(bytes, StandardCharsets.UTF_8), tClass);
    }


}
