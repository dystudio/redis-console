package com.whe.test;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by wang hongen on 2017/2/27.
 * DataTimeTest
 */
public class DataTimeTest {

    @Test
    public void test1(){
        LocalDate date=LocalDate.now();
        System.out.println(date);
        LocalDateTime dateTime=LocalDateTime.now();
        String format = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd  hh:mm:ss"));
        System.out.println(format);
    }
}
