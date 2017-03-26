package com.whe.test;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by trustme on 2017/3/11.
 * TreeMapTest
 */
public class TreeMapTest {

    @Test
    public void test3(){
        Map map =Collections.synchronizedMap(new LinkedHashMap());
        map.put("s","w");
        map.put("飒爽的","说说");
        map.put("飒爽的","说说");
        map.put("飒爽的1","说说");
        map.put("飒爽的2","说说");
        map.put("飒爽的23","说说");
        System.out.println(map);
    }
    @Test
    public void test() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 0; i < 50; i += 5) {
            map.put(i, "val" + i);
            map.put(i + 1, "val" + i);
        }
        Map.Entry<Integer, String> integerStringEntry = map.lowerEntry(1);
        System.out.println(map);
        System.out.println(integerStringEntry);
    }

    @Test
    public void test2(){
        System.out.println(1123131311%(5<<1)>>1);
        System.out.println(1123131311&(5-1<<1)>>1);
        //11 <<2 1100   12   左移运算符
        int a=-3;
        System.out.println(a=a<<2);
        //1100 >>1 110 6
        System.out.println(a=a>>1);
        a=-6;
        System.out.println(a>>>1);
    }
}
