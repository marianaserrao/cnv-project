package pt.ulisboa.tecnico.cnv.javassist.tools;

public class ThreadMetrics {
  public static ThreadLocal<Integer> threadMetric = new ThreadLocal<>();
  public static boolean computeMetrics = false;

  public static void initialize() {
    computeMetrics = true;
    threadMetric.set(0);
  }

  public static void incrementMetric() {
    if(computeMetrics){
      Integer count = threadMetric.get();
      System.out.println(count);
      threadMetric.set(count + 1);
    }
  }

  public static Integer getMetric() {
    return threadMetric.get();
  }
}