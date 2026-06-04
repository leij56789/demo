package com.company.demo;

import java.util.Arrays;
import java.util.List;
/**
 * @author jiaolei
 * @date 2026/6/4 18:03
 * @description 
 */
public class TestJava {
    public static void main(String[] args) {
        List<String> list = Arrays.asList("a", "b", "c");
// 替代 for-each
        list.forEach(item -> System.out.println(item));
// 或使用方法引用
//        list.forEach(System.out::println);
//        List<Integer> numbers = Arrays.asList(3, 1, 4, 2);
//// 升序
//        numbers.sort((a, b) -> a - b);
//// 降序
//        numbers.sort((a, b) -> b - a);
//        List<Integer> numberss = Arrays.asList(3, 1, 4, 2);
//        numbers.sort(new AscendingComparator());
    }
}
