package com.harvey.common.core.reflectasm.test;

import com.harvey.common.core.reflectasm.MethodAccess;

public class ReflectasmTest {

    public void out(Integer value){
        System.out.println(value);
    }
    
    public static void main(String[] args) {
        MethodAccess.get(ReflectasmTest.class).invoke(new ReflectasmTest() , "out", "1000");
    }

}
