package com.company.demo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public  class AscendingComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer a, Integer b) {
        return Integer.compare(a, b);
    }
}


