package sjtu.csdi.AndroidIOTool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Yang on 2015/6/9.
 */
public class AppItemAty extends Activity {
    private TextView appNameText;
    private TextView packageNameText;
    private Button startBtn;
    private Button cancelBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_item_aty);

        appNameText = (TextView)findViewById(R.id.app_item_name);
        packageNameText = (TextView)findViewById(R.id.app_item_packagename);
        startBtn = (Button) findViewById(R.id.start_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);

        Intent intent = getIntent();
        String appName = intent.getStringExtra(getResources().getString(R.string.APP_NAME_KEY));
        String packageName = intent.getStringExtra(getResources().getString(R.string.APP_PACKAGENAME_KEY));

        appNameText.setText(getResources().getString(R.string.app_item_name)+appName);
        packageNameText.setText(getResources().getString(R.string.app_item_packagename)+packageName);
    }
}