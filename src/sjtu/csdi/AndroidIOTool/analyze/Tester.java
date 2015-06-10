<<<<<<< HEAD
import java.util.ArrayList;
import java.util.InputMismatchException;
=======
package sjtu.csdi.AndroidIOTool.analyze;

>>>>>>> 8a6fc1128188691deeb7ac7bd148bd8c107b745c
import java.util.Iterator;

/**
 * Created by yzmizeyu on 15/6/9.
 */
public class Tester {
    public static void main(String[] args){
        AnalyzerInterface analyzer = new Analyzer();
        analyzer.analyzeAll();
        ArrayList<Integer> filetypeNum = analyzer.getFileTypeNums();
        ArrayList<Long> filetypeSizes = analyzer.getFileTypeSizes();

        System.out.println(filetypeNum.get(0));
        System.out.println(filetypeNum.get(1));
        System.out.println(filetypeNum.get(2));
        System.out.println(filetypeNum.get(3));
        System.out.println(filetypeNum.get(4));
        System.out.println(filetypeNum.get(5));
        System.out.println();
        System.out.println(filetypeSizes.get(0));
        System.out.println(filetypeSizes.get(1));
        System.out.println(filetypeSizes.get(2));
        System.out.println(filetypeSizes.get(3));
        System.out.println(filetypeSizes.get(4));
        System.out.println(filetypeSizes.get(5));

    }
}
