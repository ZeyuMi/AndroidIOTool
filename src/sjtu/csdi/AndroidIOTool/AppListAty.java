package sjtu.csdi.AndroidIOTool;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import sjtu.csdi.AndroidIOTool.control.CtrlServ;
import sjtu.csdi.AndroidIOTool.control.ListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yang on 2015/6/9.
 */
public class AppListAty extends Activity {
    /**
     * Called when the activity is first created.
     */
    ListView lv;
    ListAdapter adapter;
    PackageManager pm;
    ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);
        lv = (ListView) findViewById(R.id.lv);
        pm = getPackageManager();
        //得到PackageManager对象
        List<PackageInfo> packs = pm.getInstalledPackages(0);
        //得到系统 安装的所有程序包的PackageInfo对象

        for (PackageInfo pi : packs) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("icon", pi.applicationInfo.loadIcon(pm));
            //图标
            map.put("appName", pi.applicationInfo.loadLabel(pm));
            //应用名
            map.put("packageName", pi.packageName);
            //包名
            items.add(map);
            //循环读取存到HashMap,再增加到ArrayList.一个HashMap就是一项
        }

        adapter = new ListAdapter(this, items, R.layout.app_item, new String[]{
                getResources().getString(R.string.icon),
                getResources().getString(R.string.appName),
                getResources().getString(R.string.packageName)},
                new int[]{R.id.icon,
                        R.id.app_name, R.id.package_name});
        //参数:Context,ArrayList(item的集合),item的layout,包含ArrayList中Hashmap的key的数组,key所对应的值相对应的控件id
//        lv.setAdapter(adapter);
        lv.setAdapter(adapter);

        ItemclickListener listener = new ItemclickListener();
        lv.setOnItemClickListener(listener);
    }

    private class ItemclickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            HashMap<String, Object> packageInfo = items.get(i);
//            Intent intent = new Intent(AppListAty.this, AppItemAty.class);
//            intent.putExtra(getResources().getString(R.string.APP_NAME_KEY), packageInfo.get("appName").toString());
//            intent.putExtra(getResources().getString(R.string.APP_PACKAGENAME_KEY), packageInfo.get("packageName").toString());
//            startActivity(intent);
            Intent startIntent = pm.getLaunchIntentForPackage(packageInfo.get("packageName").toString());
            //TODO 这里存在问题，有些app是不能调用startActivity的
            startActivity(startIntent);
            Intent ctrlIntent = new Intent(AppListAty.this, CtrlServ.class);
            startService(ctrlIntent);
        }
    }
}