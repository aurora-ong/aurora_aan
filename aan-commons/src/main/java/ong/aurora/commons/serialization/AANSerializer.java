package ong.aurora.commons.serialization;

public interface AANSerializer {

    String toJSON(Object object );

    <T> T fromJSON(String json, Class<T> tClass);

}
