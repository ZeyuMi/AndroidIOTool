package sjtu.csdi.AndroidIOTool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import sjtu.csdi.AndroidIOTool.Tool.Commander;
import sjtu.csdi.AndroidIOTool.analyze.Analyzer;
import sjtu.csdi.AndroidIOTool.analyze.AnalyzerInterface;
import sjtu.csdi.AndroidIOTool.chart.BarChartAty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yang on 2015/6/10.
 */
public class AnalyzerAty extends Activity {
    private final String TAG = "AnalyzerAty";

    private Button analyseBtn;
    private Button cancenBtn;
    private Button fileTypeNumBtn;
    private Button accSizeBtn;
    private Button totalSeqBtn;
    private Button fsyncTableBtn;
    private Button accPatternsOfThreadsBtn;

    private AnalyzerInterface analyzer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyzer_aty);

        analyseBtn = (Button) findViewById(R.id.btn_analyse);
        cancenBtn = (Button) findViewById(R.id.btn_cancel_analyse);
        fileTypeNumBtn = (Button) findViewById(R.id.file_type_num);
        accSizeBtn = (Button) findViewById(R.id.acc_size);
        totalSeqBtn = (Button) findViewById(R.id.total_seq_res);
        fsyncTableBtn = (Button) findViewById(R.id.fsync_table);
        accPatternsOfThreadsBtn = (Button) findViewById(R.id.acc_patternsOfThreads);

        BtnListener listener = new BtnListener();
        analyseBtn.setOnClickListener(listener);
        cancenBtn.setOnClickListener(listener);
        fileTypeNumBtn.setOnClickListener(listener);
        accSizeBtn.setOnClickListener(listener);
        totalSeqBtn.setOnClickListener(listener);
        fsyncTableBtn.setOnClickListener(listener);
        accPatternsOfThreadsBtn.setOnClickListener(listener);

        analyzer = new Analyzer();
    }

    private class BtnListener implements View.OnClickListener {
        private List<Integer> fileTypeNum;
        private ArrayList<Long> accessSize;
        private ArrayList<Long> totalSequentialResult;
        private ArrayList<Integer> fsyncTable;
        private ArrayList<Integer> accessPatternsOfThreads;


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

                case R.id.file_type_num:
                    fileTypeNum = analyzer.getFileTypeNums();
                    Log.i(TAG, "getFileTypeNums() is done");

                    String[] fileTypeTag = {"multimedia", "productivity", "executable", "sqlite", "resources", "other"};
                    long[] fileTypeNum = new long[fileTypeTag.length];

                    for (int i = 0; i < fileTypeTag.length; i++) {
                        fileTypeNum[i] = this.fileTypeNum.get(i);
                    }

                    Intent intent = buildIntent(AnalyzerAty.this, BarChartAty.class, fileTypeTag, fileTypeNum, "", "");
                    startActivity(intent);
                    break;

                case R.id.acc_size:
                    accessSize = analyzer.getAccessSize();
                    Log.i(TAG, "getAccessSize is done");
                    String[] accessMod = {"only read", "only written", "read and written"};
                    long[] accessNum = new long[accessMod.length];
                    for (int i = 0; i < accessMod.length; i++) {
                        accessNum[i] = this.accessSize.get(i);
                    }

                    Intent accSizeIntent = buildIntent(AnalyzerAty.this, BarChartAty.class, accessMod, accessNum,"","");
                    startActivity(accSizeIntent);
                    break;

                case R.id.total_seq_res:
                    totalSequentialResult = analyzer.getTotalSequentialResult();
                    Log.i(TAG, "getTotalSequentialResult() is done");
                    String[] seqTypeTag = {"sequentially", "nonsequentially"};
                    long[] seqTypeNum = new long[seqTypeTag.length];
                    for (int i = 0; i < seqTypeTag.length; i++) {
                        seqTypeNum[i] = this.totalSequentialResult.get(i);
                    }

                    Intent seqResIntent = buildIntent(AnalyzerAty.this,BarChartAty.class,seqTypeTag,seqTypeNum,"","");
                    startActivity(seqResIntent);
                    break;

                case R.id.fsync_table:
                    fsyncTable = analyzer.getFsyncTable();
                    Log.i(TAG, "getFsyncTable() is done");
                    String[] fsyncTypeTag = {"0", "(0,4K)", "[4k,64k)", "{64K,1M)", "[1M,10M)", "[10M,+)"};
                    long[] fsyncTypeNum = new long[fsyncTypeTag.length];
                    for (int i = 0; i < fsyncTypeTag.length; i++) {
                        fsyncTypeNum[i] = this.fsyncTable.get(i);
                    }
                    Intent fsyncTableIntent = buildIntent(AnalyzerAty.this, BarChartAty.class,fsyncTypeTag,fsyncTypeNum,"","");
                    startActivity(fsyncTableIntent);
                    break;

                case R.id.acc_patternsOfThreads:
                    accessPatternsOfThreads = analyzer.getAccessPatternsOfThreads();
                    Log.i(TAG,"getAccessPatternsOfThreads()");
                    String[] threadTypeTag = {"only read", "only written", "read and written"};
                    long[] threadTypeNum = new long[threadTypeTag.length];
                    for (int i=0; i<threadTypeTag.length;i++){
                        threadTypeNum[i] = this.accessPatternsOfThreads.get(i);
                    }
                    Intent threadIntent = buildIntent(AnalyzerAty.this, BarChartAty.class,threadTypeTag, threadTypeNum,"","");
                    startActivity(threadIntent);
                    break;
            }
        }
    }

    private Intent buildIntent(Context c, Class<?> cls,String[] typeTag, long[] typeNum, String description, String typeIntro){
        Intent intent = new Intent(c, cls);
        intent.putExtra(getResources().getString(R.string.typeTag), typeTag);
        intent.putExtra(getResources().getString(R.string.typeNum), typeNum);
        intent.putExtra(getResources().getString(R.string.chartDescription), description);
        intent.putExtra(getResources().getString(R.string.typeIntro), typeIntro);
        return intent;
    }
}