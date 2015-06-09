import java.util.ArrayList;
import java.util.Map;

/**
 * Created by yzmizeyu on 15/6/9.
 */
public interface AnalyzerInterface {
    public void analyzeAll();
    public Map<String, Long> getFileSizes();
    public ArrayList<Integer> getAccessNum();
    public ArrayList<Long> getAccessSize();
    public Map<String, Long> getSequentialSizes();
    public Map<String, Long> getNonSequentialSize();
    public ArrayList<Long> getTotalSequentialResult();
    public Map<String, Long> getPreallocationSizes();
    public Long getTotalPreallocationSize();
    public Long getTotalFsyncNum();
    public ArrayList<Integer> getFsyncTable();
    public Long getTotalAtomicNum();
    public Map<String, Long> getAtomicSizes();
    public Map<Integer, Long> getAccessSizeOfThreads();
    public ArrayList<Integer> getAccessPatternsOfThreads();
}
