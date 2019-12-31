package com.rpc.core.channel;

import com.rpc.common.util.Maps;
import com.rpc.core.model.ServiceMetadata;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MetadataChannelGroup {

    // key: 服务标识(接口名称); value: 提供服务的节点列表(group list)
    private final ConcurrentMap<String, CopyOnWriteArrayList> groups = Maps.newConcurrentHashMap();
    // 对应服务节点(group)的引用计数
    private final GroupRefCounterMap groupRefCounter = new GroupRefCounterMap();

    public CopyOnWriteArrayList find(ServiceMetadata metadata) {
        String _directory = metadata.metadata();
        CopyOnWriteArrayList groupList = groups.get(_directory);
        if (groupList == null) {
            CopyOnWriteArrayList newGroupList = new CopyOnWriteArrayList();
            groupList = groups.putIfAbsent(_directory, newGroupList);
            if (groupList == null) {
                groupList = newGroupList;
            }
        }
        return groupList;
    }

    /**
     * 获取指定group的引用计数
     */
    public int getRefCount(ChannelGroup group) {
        AtomicInteger counter = groupRefCounter.get(group);
        if (counter == null) {
            return 0;
        }
        return counter.get();
    }


    /**
     * 指定group的引用计数 +1
     */
    public int incrementRefCount(ChannelGroup group) {
        return groupRefCounter.getOrCreate(group).incrementAndGet();
    }

    /**
     * 指定group的引用计数 -1, 如果引用计数为 0 则remove
     */
    public int decrementRefCount(ChannelGroup group) {
        AtomicInteger counter = groupRefCounter.get(group);
        if (counter == null) {
            return 0;
        }
        int count = counter.decrementAndGet();
        if (count == 0) {
            // get与remove并不是原子操作, 但在当前场景是可接受的
            groupRefCounter.remove(group);
        }
        return count;
    }


    static class GroupRefCounterMap extends ConcurrentHashMap<ChannelGroup, AtomicInteger> {

        private static final long serialVersionUID = 6590976614405397299L;

        public AtomicInteger getOrCreate(ChannelGroup key) {
            AtomicInteger counter = super.get(key);
            if (counter == null) {
                AtomicInteger newCounter = new AtomicInteger(0);
                counter = super.putIfAbsent(key, newCounter);
                if (counter == null) {
                    counter = newCounter;
                }
            }
            return counter;
        }
    }
}
