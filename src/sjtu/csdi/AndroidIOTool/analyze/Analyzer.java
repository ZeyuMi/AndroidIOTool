package sjtu.csdi.AndroidIOTool.analyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yzmizeyu on 15/6/9.
 */
public class Analyzer implements AnalyzerInterface{
    private ArrayList<String> traceFileList;
    private Map<String, Integer> pidsOfEachTraceLog;

    private Map<String, Long> fileSizes;
    private ArrayList<Integer> accessNum;
    private ArrayList<Long> accessSize;

    private Map<String, Long> sequentialSizes;
    private Map<String, Long> nonsequentialSizes;
    private ArrayList<Long> totalSequentialResult;

    private Map<String, Long> preallocationSizes;
    private long totalPreallocationSize;

    private long totalFsyncNum;
    private ArrayList<Integer> fsyncTable;

    private long totalAtomicNum;
    private Map<String, Long> atomicSizes;

    private Map<Integer, Long> accessSizesOfThreads;
    private ArrayList<Integer> accessPatternsOfThreads;


    private Pattern tracefileNamePattern = Pattern.compile("\\w+\\.\\d+");
    private Pattern openPattern = Pattern.compile("(.*)(open)\\(\\\"(.*)\\\",.*\\) += (\\d+)");
    private Pattern closePattern = Pattern.compile("(.*)(close)\\((\\d*)\\).*");
    private Pattern rwPattern = Pattern.compile("(.*)(read|write)\\((\\d*), (.*), (.*)\\) += (\\d+)");
    private Pattern lseekPattern = Pattern.compile("(.*)(lseek)\\((\\d*), (.*), (.*)\\) += (.*)");
    private Pattern pwritePattern = Pattern.compile("(.*)(pwrite)\\((\\d*), (.*), (\\d*), (\\d*)\\) += (.*)");
    private Pattern ftruncatePattern = Pattern.compile("(.*)(ftruncate)\\((\\d*), (\\d*)\\) += (.*)");
    private Pattern fsyncPattern = Pattern.compile("(.*)(fsync)\\((\\d*)\\) += (.*)");
    private Pattern renamePattern = Pattern.compile("(.*)(rename)\\(\\\"(.*)\\\", \\\"(.*)\\\"\\) += (.*)");

    private void clearResults(){
        traceFileList = new ArrayList<String>();
        pidsOfEachTraceLog = new HashMap<String, Integer>();

        fileSizes = new HashMap<String, Long>();
        accessNum = new ArrayList<Integer>();
        accessSize = new ArrayList<Long>();

        sequentialSizes = new HashMap<String, Long>();
        nonsequentialSizes = new HashMap<String, Long>();
        totalSequentialResult = new ArrayList<Long>();

        preallocationSizes = new HashMap<String, Long>();
        totalPreallocationSize = 0;

        totalFsyncNum = 0;
        fsyncTable = new ArrayList<Integer>();

        totalAtomicNum = 0;
        atomicSizes = new HashMap<String, Long>();

        accessSizesOfThreads = new HashMap<Integer, Long>();
        accessPatternsOfThreads = new ArrayList<Integer>();
    }

    private void collectTraceFileList(){
        //String path = Environment.getExternalStorageDirectory().toString()+"/strace";
        String path = "/data/strace";
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i=0; i < file.length; i++){
            Matcher matcher = tracefileNamePattern.matcher(file[i].getName());
            if(matcher.matches()) {
                String fileName = file[i].getName();
                int pid = Integer.parseInt(file[i].getName().split("\\.")[1]);
                traceFileList.add(fileName);
                pidsOfEachTraceLog.put(fileName, pid);
            }
        }
    }

    private Map<Integer, String> prereadAllOpenedFileNamesInAllProcesses(){
        Map<Integer, String> fileNames = new HashMap<Integer, String>();

        for(int i = 0; i < traceFileList.size(); i++) {
            String currentLog = traceFileList.get(i);
            try {
                //if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    //获取指定文件对应的输入流
                    //FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().toString()+"/strace/" + currentLog);
                    FileInputStream fis = new FileInputStream("/data/strace/" + currentLog);
                    //将指定输入流包装成BufferReader
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String line = null;
                    //循环读取文件内容
                    while((line = br.readLine()) != null){
                        Matcher openResult = openPattern.matcher(line);
                        if(openResult.matches()){
                            String filename = openResult.group(3);
                            int fd = Integer.parseInt(openResult.group(4));
                            if(!fileNames.containsKey(fd)){
                                fileNames.put(fd, filename);
                            }
                        }
                    }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileNames;
    }

    private String getFileNameByfd(int fd, int pid, Map<Integer, String> fileNamesOfCurrentProcess, Map<Integer, String> fileNamesInEnv){
        if(fileNamesOfCurrentProcess.containsKey(fd))
            return fileNamesOfCurrentProcess.get(fd);
        else if(fileNamesInEnv.containsKey(fd))
            return fileNamesInEnv.get(fd);
        else {
            //String cmd = "readlink /proc/" + pid + "/fd/" + fd;
            //String filename = Commander.executeWithReturnValue(cmd);
            //if (!filename.equals("None"))
            //    fileNamesInEnv.put(fd, filename);
            return "None";
        }
    }

    private void analyzeAccessPatterns(Map<Integer, String> fileNames){
        Set<String> readFNs = new HashSet<String>();
        Set<String> writeFNs = new HashSet<String>();
        Set<String> bothFNs = new HashSet<String>();
        Map<String, Long> readSizes = new HashMap<String, Long>();
        Map<String, Long> writeSizes = new HashMap<String, Long>();
        Map<String, Long> bothSizes = new HashMap<String, Long>();
        for(int i = 0; i < traceFileList.size(); i++) {
            String currentLog = traceFileList.get(i);
            Map<Integer, String> fileNamesOpened = new HashMap<Integer, String>();
            try {
                //if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    //获取指定文件对应的输入流
                    //FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().toString()+"/strace/" + currentLog);
                    FileInputStream fis = new FileInputStream("/data/strace/" + currentLog);
                    //将指定输入流包装成BufferReader
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String line = null;
                    //循环读取文件内容
                    while((line = br.readLine()) != null){
                        Matcher openResult = openPattern.matcher(line);
                        if(openResult.matches()){
                            String filename = openResult.group(3);
                            int fd = Integer.parseInt(openResult.group(4));
                            fileNamesOpened.put(fd, filename);
                            continue;
                        }
                        Matcher closeResult = closePattern.matcher(line);
                        if(closeResult.matches()){
                            int fd = Integer.parseInt(closeResult.group(3));
                            if(fileNamesOpened.containsKey(fd)){
                                fileNamesOpened.remove(fd);
                            }
                            continue;
                        }
                        Matcher rwResult = rwPattern.matcher(line);
                        if(rwResult.matches()){
                            String operation = rwResult.group(2);
                            int fd = Integer.parseInt(rwResult.group(3));
                            String fileName = getFileNameByfd(fd, pidsOfEachTraceLog.get(currentLog), fileNamesOpened, fileNames);

                            long size = Long.parseLong(rwResult.group(6).trim());
                            if(operation.equals("read")){
                                if(bothFNs.contains(fileName)){
                                    bothSizes.put(fileName, bothSizes.get(fileName) + size);
                                    continue;
                                }
                                if(writeFNs.contains(fileName)){
                                    bothFNs.add(fileName);
                                    writeFNs.remove(fileName);
                                    bothSizes.put(fileName, writeSizes.get(fileName) + size);
                                    writeSizes.remove(fileName);
                                }else if(!readFNs.contains(fileName)){
                                    readFNs.add(fileName);
                                    readSizes.put(fileName, size);
                                }else{
                                    readSizes.put(fileName, readSizes.get(fileName)+size);
                                }
                            }else{
                                if(bothFNs.contains(fileName)){
                                    bothSizes.put(fileName, bothSizes.get(fileName) + size);
                                    continue;
                                }
                                if(readFNs.contains(fileName)){
                                    bothFNs.add(fileName);
                                    readFNs.remove(fileName);
                                    bothSizes.put(fileName, readSizes.get(fileName) + size);
                                    readSizes.remove(fileName);
                                }else if(!writeFNs.contains(fileName)){
                                    writeFNs.add(fileName);
                                    writeSizes.put(fileName, size);
                                }else{
                                    writeSizes.put(fileName, writeSizes.get(fileName)+size);
                                }
                            }
                        }
                    }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //统计结果，保存到相应的数据结构中
        long readTotalSize = 0;
        long writeTotalSize = 0;
        long bothTotalSize = 0;
        Iterator<String> iterator = readFNs.iterator();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            fileSizes.put(fileName, readSizes.get(fileName));
            readTotalSize += readSizes.get(fileName);
        }
        iterator = writeFNs.iterator();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            fileSizes.put(fileName, writeSizes.get(fileName));
            writeTotalSize += writeSizes.get(fileName);
        }
        iterator = bothFNs.iterator();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            fileSizes.put(fileName, bothSizes.get(fileName));
            bothTotalSize += bothSizes.get(fileName);
        }
        accessNum.add(readFNs.size());
        accessNum.add(writeFNs.size());
        accessNum.add(bothFNs.size());
        accessSize.add(readTotalSize);
        accessSize.add(writeTotalSize);
        accessSize.add(bothTotalSize);
    }

    private void analyzeSequentiality(Map<Integer, String> fileNames){
        Map<String, Integer> currentPositions = new HashMap<String, Integer>();
        Map<String, Integer> backSizes = new HashMap<String, Integer>();

        for(int i = 0; i < traceFileList.size(); i++) {
            String currentLog = traceFileList.get(i);
            Map<Integer, String> fileNamesOpened = new HashMap<Integer, String>();
            try {
                //if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //获取指定文件对应的输入流
                //FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().toString()+"/strace/" + currentLog);
                FileInputStream fis = new FileInputStream("/data/strace/" + currentLog);
                //将指定输入流包装成BufferReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                //循环读取文件内容
                while((line = br.readLine()) != null){
                    Matcher openResult = openPattern.matcher(line);
                    if(openResult.matches()){
                        String filename = openResult.group(3);
                        int fd = Integer.parseInt(openResult.group(4));
                        fileNamesOpened.put(fd, filename);
                        continue;
                    }
                    Matcher closeResult = closePattern.matcher(line);
                    if(closeResult.matches()){
                        int fd = Integer.parseInt(closeResult.group(3));
                        if(fileNamesOpened.containsKey(fd)){
                            fileNamesOpened.remove(fd);
                        }
                        continue;
                    }
                    Matcher lseekResult = lseekPattern.matcher(line);
                    if (lseekResult.matches()){
                        int fd = Integer.parseInt(lseekResult.group(3));
                        int offset = Integer.parseInt(lseekResult.group(4));
                        String whence = lseekResult.group(5);
                        String rtvalueStr = lseekResult.group(6);
                        int rtvalue = 0;
                        if(rtvalueStr.length() > 1 && rtvalueStr.charAt(1) == 'x'){
                            rtvalue = Integer.parseInt(rtvalueStr.substring(2), 16);
                        }else{
                            rtvalue = Integer.parseInt(rtvalueStr);
                        }
                        String fileName = getFileNameByfd(fd, pidsOfEachTraceLog.get(currentLog), fileNamesOpened, fileNames);
                        if(!backSizes.containsKey(fileName)){
                            backSizes.put(fileName, 0);
                        }
                        if(!currentPositions.containsKey(fileName)){
                            currentPositions.put(fileName, 0);
                        }

                        if(whence.equals("SEEK_SET")){
                            if(currentPositions.get(fileName) > offset){
                                backSizes.put(fileName, backSizes.get(fileName) + currentPositions.get(fileName) - offset);
                            }else{
                                int t = offset - currentPositions.get(fileName) - backSizes.get(fileName);
                                if (t < 0){
                                    backSizes.put(fileName, -1 * t);
                                }else{
                                    backSizes.put(fileName, 0);
                                }
                            }
                            currentPositions.put(fileName, offset);
                        }else if (whence.equals("SEEK_CUR")){
                            currentPositions.put(fileName, currentPositions.get(fileName) + offset);
                            if(offset < 0){
                                backSizes.put(fileName, -1 * offset + backSizes.get(fileName));
                            }else{
                                int t = backSizes.get(fileName) - offset;
                                if (t > 0){
                                    backSizes.put(fileName, t);
                                }else{
                                    backSizes.put(fileName, 0);
                                }
                            }
                        }else if(whence.equals("SEEK_END")){
                            if(rtvalue < currentPositions.get(fileName)){
                                backSizes.put(fileName, currentPositions.get(fileName) - rtvalue);
                            }else{
                                int t = rtvalue - currentPositions.get(fileName) - backSizes.get(fileName);
                                if (t < 0){
                                    backSizes.put(fileName, -1 * t);
                                }else{
                                    backSizes.put(fileName, 0);
                                }
                            }
                            currentPositions.put(fileName, rtvalue);
                        }
                    }

                    Matcher rwResult = rwPattern.matcher(line);
                    if (rwResult.matches()){
                        String operation = rwResult.group(2);
                        int fd = Integer.parseInt(rwResult.group(3));
                        String fileName = getFileNameByfd(fd, pidsOfEachTraceLog.get(currentLog), fileNamesOpened, fileNames);
                        int size = Integer.parseInt(rwResult.group(6).trim());

                        if(!currentPositions.containsKey(fileName)){
                            currentPositions.put(fileName, size);
                        }else{
                            currentPositions.put(fileName, currentPositions.get(fileName) + size);
                        }

                        if(!backSizes.containsKey(fileName)){
                            backSizes.put(fileName, 0);
                        }

                        if(backSizes.get(fileName) == 0){
                            if(!sequentialSizes.containsKey(fileName)){
                                sequentialSizes.put(fileName, (long)size);
                            }else{
                                sequentialSizes.put(fileName, sequentialSizes.get(fileName) + size);
                            }
                        }else{
                            if(!nonsequentialSizes.containsKey(fileName)){
                                nonsequentialSizes.put(fileName, (long)size);
                            }else{
                                nonsequentialSizes.put(fileName, nonsequentialSizes.get(fileName) + size);
                            }
                            backSizes.put(fileName, backSizes.get(fileName) - size);
                            if(backSizes.get(fileName) < 0){
                                backSizes.put(fileName, 0);
                            }
                        }
                    }
                }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long totalSequentialSize = 0;
        long totalNonSequentialSize = 0;
        Iterator<String> iterator = sequentialSizes.keySet().iterator();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            totalSequentialSize += sequentialSizes.get(fileName);
        }
        iterator = nonsequentialSizes.keySet().iterator();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            totalNonSequentialSize += nonsequentialSizes.get(fileName);
        }
        totalSequentialResult.add(totalSequentialSize);
        totalSequentialResult.add(totalNonSequentialSize);
    }

    private void analyzePreallocation(Map<Integer, String> fileNames){
        for(int i = 0; i < traceFileList.size(); i++) {
            String currentLog = traceFileList.get(i);
            Map<Integer, String> fileNamesOpened = new HashMap<Integer, String>();
            try {
                //if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //获取指定文件对应的输入流
                //FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().toString()+"/strace/" + currentLog);
                FileInputStream fis = new FileInputStream("/data/strace/" + currentLog);
                //将指定输入流包装成BufferReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                //循环读取文件内容
                while((line = br.readLine()) != null){
                    Matcher openResult = openPattern.matcher(line);
                    if(openResult.matches()){
                        String filename = openResult.group(3);
                        int fd = Integer.parseInt(openResult.group(4));
                        fileNamesOpened.put(fd, filename);
                        continue;
                    }
                    Matcher closeResult = closePattern.matcher(line);
                    if(closeResult.matches()){
                        int fd = Integer.parseInt(closeResult.group(3));
                        if(fileNamesOpened.containsKey(fd)){
                            fileNamesOpened.remove(fd);
                        }
                        continue;
                    }
                    Matcher pwriteResult = pwritePattern.matcher(line);
                    if(pwriteResult.matches()){
                        int fd = Integer.parseInt(pwriteResult.group(3));
                        String fileName = getFileNameByfd(fd, pidsOfEachTraceLog.get(currentLog), fileNamesOpened, fileNames);
                        long size = Long.parseLong(pwriteResult.group(6).trim());
                        preallocationSizes.put(fileName, size);
                        totalPreallocationSize += size;
                    }
                    Matcher ftruncateResult = ftruncatePattern.matcher(line);
                    if(ftruncateResult.matches()){
                        int fd = Integer.parseInt(ftruncateResult.group(3));
                        String fileName = getFileNameByfd(fd, pidsOfEachTraceLog.get(currentLog), fileNamesOpened, fileNames);
                        long size = Long.parseLong(ftruncateResult.group(4).trim());
                        preallocationSizes.put(fileName, size);
                        totalPreallocationSize += size;
                    }
                }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void analyzeFsyncProperty(Map<Integer, String> fileNames){
        Map<String, Long> currentWriteSizes = new HashMap<String, Long>();
        Map<String, Integer> fsyncNum = new HashMap<String, Integer>();
        Map<String, ArrayList<Integer>> fsyncTables = new HashMap<String, ArrayList<Integer>>();

        for(int i = 0; i < traceFileList.size(); i++) {
            String currentLog = traceFileList.get(i);
            Map<Integer, String> fileNamesOpened = new HashMap<Integer, String>();
            try {
                //if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //获取指定文件对应的输入流
                //FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().toString()+"/strace/" + currentLog);
                FileInputStream fis = new FileInputStream("/data/strace/" + currentLog);
                //将指定输入流包装成BufferReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                //循环读取文件内容
                while((line = br.readLine()) != null){
                    Matcher openResult = openPattern.matcher(line);
                    if(openResult.matches()){
                        String filename = openResult.group(3);
                        int fd = Integer.parseInt(openResult.group(4));
                        fileNamesOpened.put(fd, filename);
                        continue;
                    }
                    Matcher closeResult = closePattern.matcher(line);
                    if(closeResult.matches()){
                        int fd = Integer.parseInt(closeResult.group(3));
                        if(fileNamesOpened.containsKey(fd)){
                            fileNamesOpened.remove(fd);
                        }
                        continue;
                    }
                    Matcher fsyncResult = fsyncPattern.matcher(line);
                    if(fsyncResult.matches()){
                        int fd = Integer.parseInt(fsyncResult.group(3));
                        String fileName = getFileNameByfd(fd, pidsOfEachTraceLog.get(currentLog), fileNamesOpened, fileNames);
                        if(!fsyncNum.containsKey(fileName)){
                            fsyncNum.put(fileName, 1);
                        }else{
                            fsyncNum.put(fileName, fsyncNum.get(fileName)+1);
                        }

                        if(!currentWriteSizes.containsKey(fileName)){
                            currentWriteSizes.put(fileName, (long)0);
                        }

                        long size = currentWriteSizes.get(fileName);
                        ArrayList<Integer> table;
                        if(!fsyncTables.containsKey(fileName)){
                            table = new ArrayList<Integer>();
                            table.add(0);
                            table.add(0);
                            table.add(0);
                            table.add(0);
                            table.add(0);
                            table.add(0);
                        }else{
                            table = fsyncTables.get(fileName);
                        }
                        if (size == 0){
                            table.set(0, table.get(0)+1);
                        }else if(size < 4096){
                            table.set(1, table.get(1)+1);
                        }else if(size < (64 * 1024)){
                            table.set(2, table.get(2)+1);
                        }else if(size < (1024 * 1024)){
                            table.set(3, table.get(3)+1);
                        }else if(size < (10 * 1024 * 1024)){
                            table.set(4, table.get(4)+1);
                        }else{
                            table.set(5, table.get(5)+1);
                        }
                        fsyncTables.put(fileName, table);
                        currentWriteSizes.put(fileName, (long)0);
                    }

                    Matcher rwResult = rwPattern.matcher(line);
                    if(rwResult.matches()){
                        String operation = rwResult.group(2);
                        int fd = Integer.parseInt(rwResult.group(3));
                        String fileName = getFileNameByfd(fd, pidsOfEachTraceLog.get(currentLog), fileNamesOpened, fileNames);
                        long size = Long.parseLong(rwResult.group(6).trim());

                        if(operation.equals("write")) {
                            if (!currentWriteSizes.containsKey(fileName)) {
                                currentWriteSizes.put(fileName, size);
                            }else{
                                currentWriteSizes.put(fileName, currentWriteSizes.get(fileName) + size);
                            }
                        }
                    }

                }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fsyncTable.add(0);
        fsyncTable.add(0);
        fsyncTable.add(0);
        fsyncTable.add(0);
        fsyncTable.add(0);
        fsyncTable.add(0);

        Iterator<String> iterator = fsyncTables.keySet().iterator();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            ArrayList<Integer> table = fsyncTables.get(fileName);
            fsyncTable.set(0, fsyncTable.get(0) + table.get(0));
            fsyncTable.set(1, fsyncTable.get(1) + table.get(1));
            fsyncTable.set(2, fsyncTable.get(2) + table.get(2));
            fsyncTable.set(3, fsyncTable.get(3) + table.get(3));
            fsyncTable.set(4, fsyncTable.get(4) + table.get(4));
            fsyncTable.set(5, fsyncTable.get(5) + table.get(5));
            totalFsyncNum += fsyncNum.get(fileName);
        }
    }

    private void analyzeAtomicProperty(Map<Integer, String> fileNames){
        Map<String, Long> currentWriteSizes = new HashMap<String, Long>();

        for(int i = 0; i < traceFileList.size(); i++) {
            String currentLog = traceFileList.get(i);
            Map<Integer, String> fileNamesOpened = new HashMap<Integer, String>();
            try {
                //if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //获取指定文件对应的输入流
                //FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().toString()+"/strace/" + currentLog);
                FileInputStream fis = new FileInputStream("/data/strace/" + currentLog);
                //将指定输入流包装成BufferReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                //循环读取文件内容
                while((line = br.readLine()) != null){
                    Matcher openResult = openPattern.matcher(line);
                    if(openResult.matches()){
                        String filename = openResult.group(3);
                        int fd = Integer.parseInt(openResult.group(4));
                        fileNamesOpened.put(fd, filename);
                        continue;
                    }
                    Matcher closeResult = closePattern.matcher(line);
                    if(closeResult.matches()){
                        int fd = Integer.parseInt(closeResult.group(3));
                        if(fileNamesOpened.containsKey(fd)){
                            fileNamesOpened.remove(fd);
                        }
                        continue;
                    }
                    Matcher renameResult = renamePattern.matcher(line);
                    if(renameResult.matches()) {
                        String fileName = renameResult.group(3);
                        totalAtomicNum++;
                        if (!currentWriteSizes.containsKey(fileName)) {
                            currentWriteSizes.put(fileName, (long) 0);
                        }
                        long size = currentWriteSizes.get(fileName);
                        if (!atomicSizes.containsKey(fileName)) {
                            atomicSizes.put(fileName, size);
                        } else {
                            atomicSizes.put(fileName, atomicSizes.get(fileName) + size);
                        }

                        currentWriteSizes.put(fileName, (long) 0);
                    }

                    Matcher rwResult = rwPattern.matcher(line);
                    if(rwResult.matches()){
                        String operation = rwResult.group(2);
                        int fd = Integer.parseInt(rwResult.group(3));
                        String fileName = getFileNameByfd(fd, pidsOfEachTraceLog.get(currentLog), fileNamesOpened, fileNames);
                        long size = Long.parseLong(rwResult.group(6).trim());

                        if(operation.equals("write")) {
                            if (!currentWriteSizes.containsKey(fileName)) {
                                currentWriteSizes.put(fileName, size);
                            }else{
                                currentWriteSizes.put(fileName, currentWriteSizes.get(fileName) + size);
                            }
                        }
                    }

                }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void analyzeThreads(){
        Set<Integer> readOnlyPids = new HashSet<Integer>();
        Set<Integer> writeOnlyPids = new HashSet<Integer>();
        Set<Integer> bothPids = new HashSet<Integer>();

        for(int i = 0; i < traceFileList.size(); i++) {
            String currentLog = traceFileList.get(i);
            int pid = pidsOfEachTraceLog.get(currentLog);
            try {
                //if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //获取指定文件对应的输入流
                //FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().toString()+"/strace/" + currentLog);
                FileInputStream fis = new FileInputStream("/data/strace/" + currentLog);
                //将指定输入流包装成BufferReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                //循环读取文件内容
                while((line = br.readLine()) != null){
                    Matcher rwResult = rwPattern.matcher(line);
                    if(rwResult.matches()) {
                        String operation = rwResult.group(2);
                        long size = Long.parseLong(rwResult.group(6).trim());
                        if(!accessSizesOfThreads.containsKey(pid)){
                            accessSizesOfThreads.put(pid, (long)0);
                        }
                        accessSizesOfThreads.put(pid, accessSizesOfThreads.get(pid)+size);
                        if (operation.equals("read")) {
                            if(writeOnlyPids.contains(pid)){
                                bothPids.add(pid);
                                writeOnlyPids.remove(pid);
                            }else if (!readOnlyPids.contains(pid)){
                                readOnlyPids.add(pid);
                            }
                        } else {
                            if(readOnlyPids.contains(pid)){
                                bothPids.add(pid);
                                readOnlyPids.remove(pid);
                            }else if (!writeOnlyPids.contains(pid)){
                                writeOnlyPids.add(pid);
                            }
                        }
                    }
                }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        accessPatternsOfThreads.add(readOnlyPids.size());
        accessPatternsOfThreads.add(writeOnlyPids.size());
        accessPatternsOfThreads.add(bothPids.size());
    }

    @Override
    public void analyzeAll() {
        clearResults();
        collectTraceFileList();
        Map<Integer, String> fileNamesOpenedInAllProcesses = prereadAllOpenedFileNamesInAllProcesses();
        analyzeAccessPatterns(fileNamesOpenedInAllProcesses);
        analyzeSequentiality(fileNamesOpenedInAllProcesses);
        analyzePreallocation(fileNamesOpenedInAllProcesses);
        analyzeFsyncProperty(fileNamesOpenedInAllProcesses);
        analyzeAtomicProperty(fileNamesOpenedInAllProcesses);
        analyzeThreads();
    }

    @Override
    public Map<String, Long> getFileSizes() {
        return fileSizes;
    }

    @Override
    public ArrayList<Integer> getAccessNum() {
        return accessNum;
    }

    @Override
    public ArrayList<Long> getAccessSize(){
        return accessSize;
    }

    @Override
    public Map<String, Long> getSequentialSizes() {
        return sequentialSizes;
    }

    @Override
    public Map<String, Long> getNonSequentialSize() {
        return nonsequentialSizes;
    }

    @Override
    public ArrayList<Long> getTotalSequentialResult() {
        return totalSequentialResult;
    }

    @Override
    public Map<String, Long> getPreallocationSizes() {
        return preallocationSizes;
    }

    @Override
    public Long getTotalPreallocationSize() {
        return totalPreallocationSize;
    }

    @Override
    public Long getTotalFsyncNum() {
        return totalFsyncNum;
    }

    @Override
    public ArrayList<Integer> getFsyncTable() {
        return fsyncTable;
    }

    @Override
    public Long getTotalAtomicNum() {
        return totalAtomicNum;
    }

    @Override
    public Map<String, Long> getAtomicSizes() {
        return atomicSizes;
    }

    @Override
    public Map<Integer, Long> getAccessSizeOfThreads() {
        return accessSizesOfThreads;
    }

    @Override
    public ArrayList<Integer> getAccessPatternsOfThreads() {
        return accessPatternsOfThreads;
    }
}
