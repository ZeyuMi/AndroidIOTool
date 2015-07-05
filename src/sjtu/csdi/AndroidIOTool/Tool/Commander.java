package sjtu.csdi.AndroidIOTool.Tool;

import java.io.*;

/**
 * Created by Yang on 2015/6/9.
 */
public class Commander {
    public static void strace(int pid){
        String clnCmd = "rm /data/strace/*";
        String mkdirCmd = "mkdir /data/strace";
        String mountCmd = "mount -o remount, rw /data";
        String straceCmd = "strace -o /data/strace/output -tt -ff -e trace=open,read,write,pwrite,truncate,ftruncate,rename,fsync,close,dup,dup2,dup3,socket,mknod,lseek -p " + pid;
        String cmd  = clnCmd + ";" + mkdirCmd + ";" + mountCmd + ";" + straceCmd;
        execute(cmd);
    }

//    public static void exec(String cmd) throws IOException {
//        Process proc = rt.exec(cmd);
//        //如果有参数的话可以用另外一个被重载的exec方法
//        //实际上这样执行时启动了一个子进程,它没有父进程的控制台
//        //也就看不到输出,所以我们需要用输出流来得到shell执行后的输出
//        InputStream inputstream = proc.getInputStream();
//        InputStream errorstream = proc.getErrorStream();
//
//        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
//        InputStreamReader errorstreamreader = new InputStreamReader(errorstream);
//
//        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
//        BufferedReader errorBf = new BufferedReader(errorstreamreader);
//
//        // read the ls output
//        String line = "";
//        StringBuilder sb = new StringBuilder(line);
//
//        //
//        String error = "";
//        StringBuilder errSb = new StringBuilder(error);
//
//        while ((line = bufferedreader.readLine()) != null) {
//            //System.out.println(line);
//            sb.append(line);
//            sb.append('\n');
//        }
//
//
//        while ((error = errorBf.readLine()) != null) {
//            errSb.append(error);
//            errSb.append('\n');
//        }
//
//        //tv.setText(sb.toString());
//        String res = sb.toString();
//        //使用exec执行不会等执行成功以后才返回,它会立即返回
//        //所以在某些情况下是很要命的(比如复制文件的时候)
//        //使用wairFor()可以等待命令执行完成以后才返回
//        try {
//            if (proc.waitFor() != 0) {
//                System.err.println("exit value = " + proc.exitValue());
//            }
//        } catch (InterruptedException e) {
//            System.err.println(e);
//        }
//    }

    public static void execute(String cmd) {
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));

            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String executeWithReturnValue(String cmd){
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));

            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            bf.readLine();
            String result;
            while((result = bf.readLine()) != null)
            {
                break;
            }
            os.flush();
            if (result != null)
                return result;
            else
                return "None";
        } catch (IOException e) {
            e.printStackTrace();
            return "None";
        }
    }

    public static void stopStrace(){
        String killCmd = "pkill -f strace";
        String chmdCmd = "chmod 777 /data/strace /data/strace/*";
        String cmd = killCmd + ";" + chmdCmd;
        execute(cmd);
    }

    public static void clean(){
        String cmd = "rm /data/strace/*";
        execute(cmd);
    }
}