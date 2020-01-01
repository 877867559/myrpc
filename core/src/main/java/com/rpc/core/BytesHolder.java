package com.rpc.core;

public abstract class BytesHolder {

    private transient byte[] bytes;

    private transient byte[] attributesBytes;

    public byte[] bytes() {
        return bytes;
    }

    public void bytes(byte[] bytes) {
        this.bytes = bytes;
    }


    public byte[] attributesBytes() {
        return attributesBytes;
    }

    public void attributesBytes(byte[] attributesBytes) {
        this.attributesBytes = attributesBytes;
    }
}