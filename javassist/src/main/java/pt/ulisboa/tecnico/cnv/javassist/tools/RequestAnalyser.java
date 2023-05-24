package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.image.BufferedImage;

import javassist.CtBehavior;

import com.sun.net.httpserver.HttpExchange;

public class RequestAnalyser extends CodeDumper {

    public RequestAnalyser(List<String> packageNameList, String writeDestination) {
        super(packageNameList, writeDestination);
    }

    public static Map<String, String> getRequestMap(String uri) {
        Map<String, String> requesMap = new HashMap<>();
        requesMap.put("app", uri.split("[/?]")[1]);
        for(String param : uri.split("&")) {
            String[] entry = param.split("=");
            if(entry.length > 1) {
                requesMap.put(entry[0], entry[1]);
            }else{
                requesMap.put(entry[0], "");
            }
        }
        return requesMap;
    }

    // for GET requests
    public static Map<String,String> getRequestMetrics(HttpExchange he){
        if(he.getRequestMethod().equals("GET")){
            String uri = he.getRequestURI().toString();
            Map<String,String> requesMap = getRequestMap(uri);    
            System.out.println(requesMap);
            return  requesMap;
        }
        return null;
    }
    // For POST requests (image compression)
    public static Map<String,String> getRequestMetrics(BufferedImage bi, String targetFormat, float compressionQuality){
        Map<String, String> requesMap = new HashMap<String, String>(){{
            put("app", "compressimage");
            put("imageWidth",Integer.toString(bi.getWidth()));
            put("imageHeight",Integer.toString(bi.getHeight()));
            put("targetFormat", targetFormat);
            put("compressionQuality", Float.toString(compressionQuality));
        }};
        System.out.println(requesMap);
        return requesMap;
    }

    public static void seeBeahavior(String behavior) {
        System.out.println(behavior);
    }

    @Override
    protected void transform(CtBehavior behavior) throws Exception {
        super.transform(behavior);
        // if(behavior.getName().equals("handleRequest")){
        //     behavior.insertAfter(String.format("%s.seeBeahavior(\"%s\");", RequestAnalyser.class.getName(), behavior.getName()));
        // }
        if(behavior.getName().equals("handle")){
            behavior.insertAfter(String.format("%s.getRequestMetrics($1);", RequestAnalyser.class.getName()));
        }
        if(behavior.getName().equals("process")){
            behavior.insertAfter(String.format("%s.getRequestMetrics($1, $2, $3);", RequestAnalyser.class.getName()));
        }
    }
}
