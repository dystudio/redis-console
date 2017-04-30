package com.whe.test;


import org.junit.jupiter.api.Test;

import java.util.Iterator;

public class TestList {
    @Test
    public void test1() {
        MyList<String> list = new MyList<>();
        for (int i = 0; i < 10; i++) {
            list.add("list" + i);
        }
        Iterator<String> iterator = list.iterator();

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        iterator.next();

       /* for (String str : list) {
            System.out.println(str);
        }*/
        for (int i = 0; i < 5; i++) {
            System.out.println(list.remove(0));
        }
        list.forEach(System.out::println);
        list.forEach(System.out::println);
        list.forEach(System.out::println);
    }
}
