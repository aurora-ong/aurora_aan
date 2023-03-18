package ong.aurora.commons.serialization;

import java.lang.reflect.Type;

public interface ANNSerializer {

    String toJSON(Object object );

    <T> T fromJSON(String json, Type tClass);

    byte[] toBytes(Object object);

    <T> T fromBytes(byte[] bytes, Type tClass);
}

