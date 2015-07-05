package sjtu.csdi.AndroidIOTool.control;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import sjtu.csdi.AndroidIOTool.AnalyzerAty;
import sjtu.csdi.AndroidIOTool.R;
import sjtu.csdi.AndroidIOTool.Tool.Commander;

import java.util.List;

/**
 * Created by Yang on 2015/6/9.
 */

public class CtrlServ extends Service {

    //定义浮动窗口布局
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;

    private Button mMoveBtn;
    private Button mStartBtn;
    private Button mStopBtn;
    private Button mCancelBtn;

    private String packageName;

    private static final String TAG = "CtrlServ";

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.i(TAG, "oncreat");
        createFloatView();
        //Toast.makeText(FxService.this, "create FxService", Toast.LENGTH_LONG);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        try {
            packageName = intent.getStringExtra("packageName");
            Log.i(TAG, "current running app: " + packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
////        return super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "[onStartCommand]Received start id " + startId + ": " + intent);
//        return START_STICKY;
        Notification notification = new Notification(R.drawable.ic_launcher,
                getString(R.string.app_name), System.currentTimeMillis());
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        PendingIntent pendingintent = PendingIntent.getActivity(this, 0,
                new Intent(this, AnalyzerAty.class), 0);
        notification.setLatestEventInfo(this, "recording now...", "请保持程序在后台运行",
                pendingintent);
        startForeground(0x111, notification);

        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        //获取WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        //设置window type
        wmParams.type = LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags =
//          LayoutParams.FLAG_NOT_TOUCH_MODAL |
                LayoutParams.FLAG_NOT_FOCUSABLE
//          LayoutParams.FLAG_NOT_TOUCHABLE
        ;

        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;

        // 以屏幕左上角为原点，设置x、y初始值
        wmParams.x = 0;
        wmParams.y = 0;

        /*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.ctrl_serv, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);

        Log.i(TAG, "mFloatLayout-->left" + mFloatLayout.getLeft());
        Log.i(TAG, "mFloatLayout-->right" + mFloatLayout.getRight());
        Log.i(TAG, "mFloatLayout-->top" + mFloatLayout.getTop());
        Log.i(TAG, "mFloatLayout-->bottom" + mFloatLayout.getBottom());

        //浮动窗口按钮
        mMoveBtn = (Button) mFloatLayout.findViewById(R.id.ctrl_move);
        mStartBtn = (Button) mFloatLayout.findViewById(R.id.start_rcd);
        mStopBtn = (Button) mFloatLayout.findViewById(R.id.stop_rcd);
        mCancelBtn = (Button) mFloatLayout.findViewById(R.id.cancel_rcd);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        Log.i(TAG, "Width/2--->" + mMoveBtn.getMeasuredWidth() / 2);
        Log.i(TAG, "Height/2--->" + mMoveBtn.getMeasuredHeight() / 2);

        //设置监听浮动窗口的触摸移动
        mMoveBtn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                wmParams.x = (int) event.getRawX() - mMoveBtn.getMeasuredWidth() / 2;

                Log.i(TAG, "RawX" + event.getRawX());
                Log.i(TAG, "X" + event.getX());
                //25为状态栏的高度
                wmParams.y = (int) event.getRawY() - mMoveBtn.getMeasuredHeight() / 2 - 25;

                Log.i(TAG, "RawY" + event.getRawY());
                Log.i(TAG, "Y" + event.getY());
                //刷新
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;
            }
        });

        BtnClickListener listener = new BtnClickListener();
        mStartBtn.setOnClickListener(listener);
        mStopBtn.setOnClickListener(listener);
        mCancelBtn.setOnClickListener(listener);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
        Log.i(TAG, TAG + " is closed");
    }

    private class BtnClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.start_rcd:
                    startRecord();
                    break;

                case R.id.stop_rcd:
                    stopRecord();
                    break;

                case R.id.cancel_rcd:
                    Toast.makeText(getApplicationContext(), "cancel record", Toast.LENGTH_SHORT).show();
                    stopSelf();
                    break;
            }
        }
    }

    private void startRecord() {
        String[] pkgList;
        int pid = 0;
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : runningApps) {
            pkgList = app.pkgList;
            for (int i = 0; i < pkgList.length; i++) {
                if (pkgList[i].equals(packageName)) {
                    pid = app.pid;
                    Log.i(TAG, "current pid:" + pid);
                    break;
                }
            }
        }
        Commander.strace(pid);
        Toast.makeText(getApplicationContext(), "Start recording...", Toast.LENGTH_SHORT);
    }

    public void stopRecord() {
        //1.kill strace : pkill -f strace and chmod auth
        Commander.stopStrace();
        Toast.makeText(getApplicationContext(), "Stop recording...", Toast.LENGTH_SHORT);
        this.stopSelf();
    }
}