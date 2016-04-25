package com.harvey.common.core.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public class SortOrderUtils {

    public static <T extends SortOrder> Collection<T> sort(Collection<T> source) {
        Collection<T> result = new TreeSet<T>(new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                if (o1.getOrder() != o2.getOrder()) {
                    return o1.getOrder() - o2.getOrder();
                } else {
                    return o1.hashCode() - o2.hashCode();
                }
            }
        });
        result.addAll(source);
        return result;
    }
}
