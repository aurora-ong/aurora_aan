package ong.aurora.commons.serialization.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ong.aurora.commons.serialization.ANNSerializer;

public class ANNJacksonSerializer implements ANNSerializer {

    private ObjectMapper objectMapper;

    public ANNJacksonSerializer() {
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
    public <T> T fromJSON(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
