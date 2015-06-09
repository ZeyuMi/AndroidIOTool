package sjtu.csdi.AndroidIOTool.Tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Yang on 2015/6/9.
 */
public class Commander {
    public static void mkdir() throws IOException{
        String cmd = "mkdir /data/strace";
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmd);
    }

    public static void su() throws IOException{
        String cmd = "su";
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmd);
    }

    public static void remount() throws IOException{
        String cmd = "su -c 'mount -o remount,rw /data'";
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmd);
    }

    public static void strace(String cmd) throws IOException{
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmd);
        //如果有参数的话可以用另外一个被重载的exec方法
        //实际上这样执行时启动了一个子进程,它没有父进程的控制台
        //也就看不到输出,所以我们需要用输出流来得到shell执行后的输出
        InputStream inputstream = proc.getInputStream();
        InputStream errorstream = proc.getErrorStream();

        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        InputStreamReader errorstreamreader = new InputStreamReader(errorstream);

        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        BufferedReader errorbf = new BufferedReader(errorstreamreader);

        // read the ls output
        String line = "";
        StringBuilder sb = new StringBuilder(line);

        //
        String error = "";
        StringBuilder err = new StringBuilder(error);

        while ((line = bufferedreader.readLine()) != null) {
            //System.out.println(line);
            sb.append(line);
            sb.append('\n');
        }


        while ((error = errorbf.readLine()) != null){
            err.append(error);
            err.append('\n');
        }


        //tv.setText(sb.toString());
        String res = sb.toString();
        //使用exec执行不会等执行成功以后才返回,它会立即返回
        //所以在某些情况下是很要命的(比如复制文件的时候)
        //使用wairFor()可以等待命令执行完成以后才返回
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
        }
        catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    public static void strace(int pid) throws IOException{
        String cmd = "su -c 'strace -o /data/strace/output -tt -ff -e trace=open -p " + pid + "'";
        strace(cmd);
    }
}