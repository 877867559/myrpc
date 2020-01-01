package com.rpc.transport;

public class ProtocolHeader {

    /** 协议头长度 */
    public static final int HEAD_LENGTH = 12;
    /** Magic */
    public static final short MAGIC = (short) 0xbabe;

    /** Request */
    public static final byte REQUEST = 1;
    /** Response */
    public static final byte RESPONSE = 2;


    /** Acknowledge */
    public static final byte ACK = 126;
    /** Heartbeat */
    public static final byte HEARTBEAT = 127;

    public static byte SUCSTATUS = 1;


    private byte sign;
    private byte status;
    private int bodyLength;
    private int  attrBodyLength;

    public byte sign() {
        return sign;
    }

    public void sign(byte sign) {
        this.sign = sign;
    }

    public byte status() {
        return status;
    }

    public void status(byte status) {
        this.status = status;
    }


    public int bodyLength() {
        return bodyLength;
    }

    public void bodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public int attrBodyLength() {
        return attrBodyLength;
    }

    public void attrBodyLength(int attrBodyLength) {
        this.attrBodyLength = attrBodyLength;
    }

    @Override
    public String toString() {
        return "JProtocolHeader{" +
                "sign=" + sign +
                ", status=" + status +
                ", attrBodyLength=" + attrBodyLength +
                ", bodyLength=" + bodyLength +
                '}';
    }
}
