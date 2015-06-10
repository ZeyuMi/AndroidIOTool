<<<<<<< HEAD
import java.lang.reflect.Array;
=======
package sjtu.csdi.AndroidIOTool.analyze;

>>>>>>> 8a6fc1128188691deeb7ac7bd148bd8c107b745c
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by yzmizeyu on 15/6/9.
 */
public interface AnalyzerInterface {
    /**
     * call this function before all following ones.
     */
    public void analyzeAll();

    /**
     * Element 0 is the number of files that are related to multimedia, such as jpg, png, mp3, avi.
     * Element 1 is the number of files that are related to productivity, such as doc, docx, xls, ppt, pptx, pdf.
     * Element 2 is the number of files that are executable, such as apk, dex, odex, so
     * Element 3 is the number of files that are related to sqlite, such as db, db-journal.
     * Element 4 is the number of files that are resources, such as dat, xml, cache.
     * Element 5 is the number of other files.
     * @return
     */
    public ArrayList<Integer> getFileTypeNums();

    /**
     * Element 0 is the size of files that are related to multimedia, such as jpg, png, mp3, avi.
     * Element 1 is the size of files that are related to productivity, such as doc, docx, xls, ppt, pptx, pdf.
     * Element 2 is the size of files that are executable, such as apk, dex, odex, so
     * Element 3 is the size of files that are related to sqlite, such as db, db-journal.
     * Element 4 is the size of files that are resources, such as dat, xml, cache.
     * Element 5 is the size of other files.
     * @return
     * @return
     */
    public ArrayList<Long> getFileTypeSizes();

    /**
     * Key is a file name, and value is the size of data that accessed in this file.
     * @return
     */
    public Map<String, Long> getFileSizes();

    /**
     * Element 0 is the number of files that only read data.
     * Element 1 is the number of files that only write data.
     * Element 2 is the number of files that both read and write data.
     * @return
     */
    public ArrayList<Integer> getAccessNum();

    /**
     * Element 0 is the size of file that is only read
     * Element 1 is the size of file that is only written
     * Element 2 is the size of file that is both read and written
     * @return
     */
    public ArrayList<Long> getAccessSize();

    /**
     * Key is a file name, and value is the size of data that is sequentially accessed in this file.
     * @return
     */
    public Map<String, Long> getSequentialSizes();

    /**
     * Key is a file name, and value is the size of data that is nonsequentially accessed in this file.
     * @return
     */
    public Map<String, Long> getNonSequentialSize();

    /**
     * Element 0 is the total size of data that is sequentially accessed.
     * Element 1 is the total size of data that is nonsequentially accessed.
     * @return
     */
    public ArrayList<Long> getTotalSequentialResult();

    /**
     * Key is a file name, and value is the size of data that is preallocated in this file
     * @return
     */
    public Map<String, Long> getPreallocationSizes();

    /**
     *
     * @return the total size of data that is preallocated
     */
    public Long getTotalPreallocationSize();

    /**
     *
     * @return the total number of fsycn system call
     */
    public Long getTotalFsyncNum();

    /**
     * Element 0 is the number of fsync syscall that is called for data size is 0 bytes
     * Element 1 is the number of fsync syscall that is called for data size among (0, 4096) bytes
     * Element 2 is the number of fsync syscall that is called for data size among [4096, 64 * 1024) bytes
     * Element 3 is the number of fsync syscall that is called for data size between [64 * 1024, 1024 * 1024) bytes
     * Element 4 is the number of fsync syscall that is called for data size between [1024 * 1024, 10 * 1024 * 1024) bytes
     * Element 5 is the number of fsync syscall that is called for data size between [10 * 1024 * 1024, +limit) bytes
     * @return
     */
    public ArrayList<Integer> getFsyncTable();

    /**
     *
     * @return the total number of rename system call
     */
    public Long getTotalAtomicNum();

    /**
     * Value is a file name, and value is the size of the file that is related to rename system call
     * @return
     */
    public Map<String, Long> getAtomicSizes();

    /**
     * Value is pid of a thread, and value is the size of data that accessed in this thread
     * @return
     */
    public Map<Integer, Long> getAccessSizeOfThreads();

    /**
     * Element 0 is the number of thread that only read data
     * Element 1 is the number of thread that only write data
     * Element 2 is the number of thread that both read and write data
     * @return
     */
    public ArrayList<Integer> getAccessPatternsOfThreads();
}
