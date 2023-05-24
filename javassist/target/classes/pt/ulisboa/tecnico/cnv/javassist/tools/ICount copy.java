package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.io.IOException;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;

import com.sun.net.httpserver.HttpExchange;

public class ICount extends CodeDumper {

    private static long nblocks = 0;
    private static long nmethods = 0;
    private static long ninsts = 0;

    public ICount(List<String> packageNameList, String writeDestination) {
        super(packageNameList, writeDestination);
    }

    public static void incBasicBlock(int position, int length) {
        nblocks++;
        ninsts += length;
    }

    public static void incBehavior(String name) {
        nmethods++;
    }

    public static void printStatistics() {
        System.out.println(String.format("[%s] Number of executed methods: %s", ICount.class.getSimpleName(), nmethods));
        System.out.println(String.format("[%s] Number of executed basic blocks: %s", ICount.class.getSimpleName(), nblocks));
        System.out.println(String.format("[%s] Number of executed instructions: %s", ICount.class.getSimpleName(), ninsts));
    }

    public static void getRequestMetrics(HttpExchange he){
        System.out.println(he.toString());
        System.out.println(he.getAttribute("Request-ID"));
    }

    public static void seeBeahavior(String behavior) {
        System.out.println(behavior);
    }

    @Override
    protected void transform(CtBehavior behavior) throws Exception {
        super.transform(behavior);
        behavior.insertAfter(String.format("%s.incBehavior(\"%s\");", ICount.class.getName(), behavior.getLongName()));
        // behavior.insertAfter(String.format("%s.seeBeahavior(\"%s\");", ICount.class.getName(), behavior.getName()));

        if (behavior.getName().equals("main")) {
            behavior.insertAfter(String.format("%s.printStatistics();", ICount.class.getName()));
        }

        if(behavior.getName().equals("handle")){
            behavior.insertAfter(String.format("%s.getRequestMetrics($1);", ICount.class.getName()));
        }
    }

    @Override
    protected void transform(BasicBlock block) throws CannotCompileException {
        super.transform(block);
        block.behavior.insertAt(block.line, String.format("%s.incBasicBlock(%s, %s);", ICount.class.getName(), block.getPosition(), block.getLength()));
    }
}
