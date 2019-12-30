package com.rpc.serialization.protostuff;

import com.rpc.common.util.Maps;
import com.rpc.serialization.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;


public class ProtoStuffSerializer implements Serializer {

    private static Logger logger = LoggerFactory.getLogger((ProtoStuffSerializer.class));

    private final ConcurrentMap<Class<?>, Schema<?>> schemaCache = Maps.newConcurrentHashMap();

    @Override
    public <T> byte[] writeObject(T obj) {
        Schema<T> schema = getSchema((Class<T>) obj.getClass());
        LinkedBuffer buf = LinkedBuffer.allocate() ;
        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buf);
        } finally {
            buf.clear();
        }
    }

    @Override
    public <T> T readObject(byte[] bytes, Class<T> clazz) {
        T msg = null;
        try {
             msg = clazz.newInstance();
        } catch (Exception e) {
            logger.warn("protoStuff serializer fail:{},{}",clazz.getName(),e);
            return null;
        }
        Schema<T> schema = getSchema(clazz);
        ProtostuffIOUtil.mergeFrom(bytes, msg, schema);
        return msg;
    }

    private <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (schema == null) {
            Schema<T> newSchema = RuntimeSchema.createFrom(clazz);
            schema = (Schema<T>) schemaCache.putIfAbsent(clazz, newSchema);
            if (schema == null) {
                schema = newSchema;
            }
        }
        return schema;
    }
}
