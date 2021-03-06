package com.nameof.zookeeper.tools.queue;

import com.nameof.zookeeper.tools.common.WaitDuration;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 弱一致性的有界阻塞队列
 * @Author: chengpan
 * @Date: 2018/11/8
 */
public class BoundedZkBlockingQueue extends ZkBlockingQueue {

    private int size;

    public BoundedZkBlockingQueue(String queueName, String connectString, Serializer serializer, int size) throws IOException, InterruptedException, KeeperException {
        super(queueName, connectString, serializer);
        this.size = size;
    }

    @Override
    public int remainingCapacity() {
        return size - size();
    }

    @Override
    public void put(Object o) throws InterruptedException {
        while (!offer(o)) {
            zkPrimitiveSupport.waitChildren(queuePath);
        }
    }

    @Override
    public boolean add(Object o) {
        if (offer(o))
            return true;
        throw new IllegalStateException("Queue full");
    }

    @Override
    public boolean offer(Object o) {
        if (size() < size)
            return super.offer(o);
        return false;
    }

    @Override
    public boolean offer(Object o, long timeout, TimeUnit unit) throws InterruptedException {
        WaitDuration duration = WaitDuration.from(unit.toMillis(timeout));
        while (size() >= size) {
            try {
                zkPrimitiveSupport.waitChildren(queuePath, duration);
            } catch (TimeoutException e) {
                return false;
            }
        }
        return super.offer(o);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
