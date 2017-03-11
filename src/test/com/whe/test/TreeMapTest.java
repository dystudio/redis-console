package com.whe.test;

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by trustme on 2017/3/11.
 * TreeMapTest
 */
public class TreeMapTest {
    @Test
    public void test() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 0; i < 50; i += 5) {
            map.put(i, "val" + i);
            map.put(i+1, "val" + i);
        }
        Map.Entry<Integer, String> integerStringEntry = map.lowerEntry(1);
        System.out.println(map);
        System.out.println(integerStringEntry);
    }
}
