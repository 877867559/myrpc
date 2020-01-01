package com.rpc.common.util;

public class ConstantsUtils {

    /** 服务默认组别 */
    public static final String DEFAULT_GROUP = "rpc";
    /** 服务默认版本号 */
    public static final String DEFAULT_VERSION = "1";

    /** 链路read空闲检测 默认60秒, 60秒没读到任何数据会强制关闭连接 */
    public static final int READER_IDLE_TIME_SECONDS = SystemPropertyUtil.getInt("rpc.reader.idle.time.seconds", 60);
    /** 链路write空闲检测 默认30秒, 30秒没有向链路中写入任何数据时客户端会主动向对端发送心跳 */
    public static final int WRITER_IDLE_TIME_SECONDS = SystemPropertyUtil.getInt("rpc.writer.idle.time.seconds", 30);

}
