package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.image.BufferedImage;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import pt.ulisboa.tecnico.cnv.javassist.dynamodb.DynamoDB;

import com.sun.net.httpserver.HttpExchange;

public class RequestAnalyser extends CodeDumper {
    private static DynamoDB dynamodb;
    public static ThreadLocal<Map<String, String>> metricsThread = new ThreadLocal<>();

    public RequestAnalyser(List<String> packageNameList, String writeDestination) throws Exception{
        super(packageNameList, writeDestination);
        dynamodb = new DynamoDB();
    }

    public static void initializeThread(HttpExchange he) {
        Map<String, String> initialThread = new HashMap<String, String>(){{
            put("blocks","0");
            put("methods","0");
            put("instructions","0");           
        }};
        metricsThread.set(initialThread);
    }
    
    public static void appendMetrics(HttpExchange he) throws Exception{
        Map<String, String> metrics = metricsThread.get();
        if (metrics.containsKey("app")) {
            dynamodb.addMetrics(metrics);
        }
    }

    public static void incMapValue(Map<String, String> metrics, String fieldName, int increment) {
        String fieldValue  = metrics.get(fieldName);
        long value = Long.parseLong(fieldValue);
        value+=increment;
        metrics.put(fieldName, Long.toString(value));
    }

    public static void incBehavior() {
        Map<String, String> metrics = metricsThread.get();
        if (metrics != null){
            incMapValue(metrics, "methods", 1);
        }
    }
    
    public static void incBasicBlock(int position, int length) {
        Map<String, String> metrics = metricsThread.get();
        if (metrics != null){
            incMapValue(metrics, "blocks", 1);
            incMapValue(metrics, "instructions", length);
        }
    }

    public static Map<String, String> getRequestMap(String uri) {
        Map<String, String> requestMap = new HashMap<>();

        String[] uriSegments = uri.split("[/?]");
        requestMap.put("app", uriSegments[1]);
        for(String param : uriSegments[2].split("&")) {
            String[] entry = param.split("=");
            if(entry.length > 1) {
                requestMap.put(entry[0], entry[1]);
            }else{
                requestMap.put(entry[0], "");
            }
        }
        return requestMap;
    }
    
    // for GET requests
    public static void getRequestParams(HttpExchange he){
        if(he.getRequestMethod().equals("GET")){
            String uri = he.getRequestURI().toString();
            Map<String,String> requestMap = getRequestMap(uri);  
            Map<String, String> metrics = metricsThread.get();
            metrics.putAll(requestMap);  
        }
    }
    // For POST requests (image compression)
    public static void getRequestParams(BufferedImage bi, String targetFormat, float compressionQuality){
        Map<String, String> metrics = metricsThread.get();
        metrics.put("app", "compressimage");
        metrics.put("imageWidth",Integer.toString(bi.getWidth()));
        metrics.put("imageHeight",Integer.toString(bi.getHeight()));
        metrics.put("targetFormat", targetFormat);
        metrics.put("compressionQuality", Float.toString(compressionQuality));
    }
    public static void seeBehavior(String behavior) {
        System.out.println(behavior);
    }

    @Override
    protected void transform(CtBehavior behavior) throws Exception {
        super.transform(behavior);
        
        if(behavior.getName().equals("handle")){
            behavior.insertBefore(String.format("%s.initializeThread($1);", RequestAnalyser.class.getName()));
            behavior.insertAfter(String.format("%s.getRequestParams($1);", RequestAnalyser.class.getName()));
            behavior.insertAfter(String.format("%s.appendMetrics($1);", RequestAnalyser.class.getName()));
        }else{
            behavior.insertAfter(String.format("%s.incBehavior();", RequestAnalyser.class.getName()));
        }
        if(behavior.getName().equals("process")){
            behavior.insertAfter(String.format("%s.getRequestParams($1, $2, $3);", RequestAnalyser.class.getName()));
        }
    }

    @Override
    protected void transform(BasicBlock block) throws CannotCompileException {
        super.transform(block);
        block.behavior.insertAt(block.line, String.format("%s.incBasicBlock(%s, %s);", RequestAnalyser.class.getName(), block.getPosition(), block.getLength()));
    }
}
