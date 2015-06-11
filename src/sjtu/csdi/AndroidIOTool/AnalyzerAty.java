package sjtu.csdi.AndroidIOTool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import sjtu.csdi.AndroidIOTool.Tool.Commander;
import sjtu.csdi.AndroidIOTool.analyze.Analyzer;
import sjtu.csdi.AndroidIOTool.analyze.AnalyzerInterface;
import sjtu.csdi.AndroidIOTool.chart.PieChartAty;

import java.util.List;

/**
 * Created by Yang on 2015/6/10.
 */
public class AnalyzerAty extends Activity {
    private final String TAG = "AnalyzerAty";

    private Button analyseBtn;
    private Button cancenBtn;
    private Button anlsFileTypeBtn;
    private AnalyzerInterface analyzer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyzer_aty);

        analyseBtn = (Button) findViewById(R.id.btn_analyse);
        cancenBtn = (Button) findViewById(R.id.btn_cancel_analyse);
        anlsFileTypeBtn = (Button) findViewById(R.id.anls_file_type);

        BtnListener listener = new BtnListener();
        analyseBtn.setOnClickListener(listener);
        cancenBtn.setOnClickListener(listener);
        anlsFileTypeBtn.setOnClickListener(listener);

        analyzer = new Analyzer();
    }

    private class BtnListener implements View.OnClickListener {
        private List<Integer> fileTypes;


        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.btn_analyse:
                    //TODO
                    analyzer.analyzeAll();
                    Log.i(TAG, "analyzeAll() is done");
                    break;

                case R.id.btn_cancel_analyse:
                    Commander.clean();
                    finish();
                    break;

                case R.id.anls_file_type:
                    fileTypes = analyzer.getFileTypeNums();
                    Log.i(TAG, "getFileTypeNums() is done");

                    String[] fileTypeTag = {"multimedia", "productivity", "executable", "sqlite", "resources", "other"};
                    int[] fileTypeNum = new int[fileTypeTag.length];
                    for (int i = 0; i < fileTypeTag.length; i++) {
                        fileTypeNum[i] = fileTypes.get(i);
                    }
                    Intent intent = new Intent(AnalyzerAty.this, PieChartAty.class);
                    intent.putExtra(getResources().getString(R.string.typeTag), fileTypeTag);
                    intent.putExtra(getResources().getString(R.string.typeNum),fileTypeNum);
                    intent.putExtra(getResources().getString(R.string.chartDescription), "File Type Pie Chart");
                    intent.putExtra(getResources().getString(R.string.typeIntro),"File Type");
                    startActivity(intent);
                    break;
            }
        }
    }
}