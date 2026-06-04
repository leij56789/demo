package com.company.demo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LearnStream {
    public static void main(String[] args) {
//        List<String> list = Arrays.asList("a", "b", "c");
//        list.stream().forEach(System.out::println);
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> collect = list.stream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());
    }
}
