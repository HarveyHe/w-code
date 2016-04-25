package com.harvey.common.core.dao.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.beetl.core.BeetlKit;


public class Test {

    public static void main(String[] args) {
        runParseTest();
        //runNamedParameterTest();
        //runLoadPriceShipOwnerQuery();
    }
    
    private static void runLoadPriceShipOwnerQuery(){
        //String sql = getResource("LoadPriceShipOwnerQuery.txt");
        String sql = getResource("sql2.txt");
        //String sql1 = QueryUtils.parseNameSql(sql);
        //String result = BeetlKit.render(sql1, parameters);
        sql = QueryUtils.trimLine(sql);
        System.out.println(sql);
    }

    private static void runNamedParameterTest(){
        String sql = getResource("runSQL.txt");
        for(String name : QueryUtils.getSqlParameterSets(sql)){
            System.out.println(name);
        }
    }
    
    private static void runParseTest(){
        String sql = getOrderInfoSql();
        String sql1 = QueryUtils.parseNameSql(sql);
        Map<String,Object> parameters = new HashMap<String,Object>();
        //parameters.put("consignNo","123");
        parameters.put("userId","123");
        parameters.put("eexpRequestNo", 123);
        parameters.put("custofsettlement", 123);
        parameters.put("eexpIds", 123);
        Map<String,Object> parameters1 = new HashMap<String,Object>();
        parameters1.put("arg", parameters);
        BeetlKit.gt.setErrorHandler(new com.harvey.common.core.beetl.ParseErrorHandler());
        BeetlKit.gt.getConf().setHtmlTagSupport(false);
        String result = BeetlKit.render(sql1, parameters1);
        System.out.println(sql1);
        System.out.println("##############################################################");
        System.out.println( QueryUtils.trimLine(result));
        System.out.println("##############################################################");        
    }
    
    private static String getOrderInfoSql() {
        return getResource("consignQuery.txt");
    }
    
    private static String getResource(String resName){
        InputStream is = Test.class.getClassLoader().getResourceAsStream("com/harvey/common/core/dao/utils/"+resName);
        Reader reader = new InputStreamReader(is);
        StringWriter writer = new StringWriter();
        int c;
        try {
            while((c = reader.read()) != -1){
                writer.write((char)c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();        
    }
    
}
