import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by yzmizeyu on 15/6/9.
 */
public class Tester {
    public static void main(String[] args){
        AnalyzerInterface analyzer = new Analyzer();
        analyzer.analyzeAll();
        Iterator<Integer> iterator = analyzer.getAccessSizeOfThreads().keySet().iterator();
        while (iterator.hasNext()) {
            int pid = iterator.next();
            System.out.println(pid + ":" + analyzer.getAccessSizeOfThreads().get(pid));
        }

        System.out.println(analyzer.getAccessPatternsOfThreads().get(0));
        System.out.println(analyzer.getAccessPatternsOfThreads().get(1));
        System.out.println(analyzer.getAccessPatternsOfThreads().get(2));
    }
}
