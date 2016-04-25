package com.harvey.common.core.utils;

public class CommonUtils {

    /**
     * like oracle decode function
     * 
     * @param value
     * @param args
     * @return
     */
    public static Object decode(Object value, Object... args) {
        int i = 0;
        while (true) {
            if (value.equals(args[i]) || (value == null && args[i] == null)) {
                return args[i + 1];
            } else {
                if (args.length == i + 3) {
                    // default
                    return args[i + 2];
                } else {
                    i = i + 2;
                    continue;
                }
            }
        }
    }

    /**
     * like mysql ifnull function
     * @param args
     * @return
     */
    public static Object ifnull(Object... args) {
        for (Object arg : args) {
            if (arg != null) {
                return arg;
            }
        }
        return null;
    }
}
