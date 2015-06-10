package sjtu.csdi.AndroidIOTool;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import sjtu.csdi.AndroidIOTool.Tool.Commander;
import sjtu.csdi.AndroidIOTool.analyze.Analyzer;
import sjtu.csdi.AndroidIOTool.analyze.AnalyzerInterface;

import java.util.Map;

/**
 * Created by Yang on 2015/6/10.
 */
public class AnalyzerAty extends Activity {

    private Button analyseBtn;
    private Button cancenBtn;

    private AnalyzerInterface analyzer = new Analyzer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyzer_aty);

        analyseBtn = (Button) findViewById(R.id.btn_analyse);
        cancenBtn = (Button) findViewById(R.id.btn_cancel_analyse);

        BtnListener listener = new BtnListener();
        analyseBtn.setOnClickListener(listener);
        cancenBtn.setOnClickListener(listener);
    }

    private class BtnListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.btn_analyse:
                    //TODO
                    analyzer.analyzeAll();
                    Map<String, Long> res =  analyzer.getFileSizes();
                    break;
                case R.id.btn_cancel_analyse:
                    Commander.clean();
                    finish();
                    break;
            }
        }
    }
}