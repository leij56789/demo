package com.company.demo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
/**
 * @author jiaolei
 * @date 2026/6/4 15:58
 * @description 
 */
public  class AscendingComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer a, Integer b) {
        return Integer.compare(a, b);
    }
}


