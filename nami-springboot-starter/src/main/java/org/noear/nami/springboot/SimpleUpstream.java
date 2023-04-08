package org.noear.nami.springboot;

import java.util.List;
import java.util.function.Supplier;

/**
 * 一个简单的负载均衡器
 * @author desire
 */
public class SimpleUpstream implements Supplier<String> {
    private final List<String> urls;
    private int index = 0;
    private static final int indexMax = 99999999;

    public SimpleUpstream(List<String> urls) {
        this.urls = urls;
    }

    @Override
    public String get() {
        int size = urls.size();
        if (size == 0) {
            return null;
        }

        //这里不需要原子性，快就好
        if (index > indexMax) {
            index = 0;
        }

        return urls.get(index++ % size);
    }
}
