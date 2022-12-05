package ong.aurora.commons.serialization;

public interface ANNSerializer {

    String toJSON(Object object );

    <T> T fromJSON(String json, Class<T> tClass);

}
