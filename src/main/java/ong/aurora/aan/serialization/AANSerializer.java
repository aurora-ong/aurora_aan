package ong.aurora.aan.serialization;

import java.lang.reflect.Type;
public interface AANSerializer {

    String toJSON(Object object );

    <T> T fromJSON(String json, Type tClass);

    byte[] toBytes(Object object);

    <T> T fromBytes(byte[] bytes, Type tClass);
}

