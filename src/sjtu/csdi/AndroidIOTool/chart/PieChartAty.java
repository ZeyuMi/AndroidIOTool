package sjtu.csdi.AndroidIOTool.chart;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import sjtu.csdi.AndroidIOTool.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Yang on 2015/6/10.
 */

public class PieChartAty extends Activity {
    private String[] typeTag;
    private int[] typeNum;
    private String description;
    private PieChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.piechart_aty);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        typeTag = bundle.getStringArray(getResources().getString(R.string.typeTag));
        typeNum = bundle.getIntArray(getResources().getString(R.string.typeNum));
        description = bundle.getString(getResources().getString(R.string.chartDescription));

        mChart = (PieChart) findViewById(R.id.pie_chart);
        PieData mPieData = getPieData();

        showChart(mChart, mPieData);
    }

    private void showChart(PieChart pieChart, PieData pieData) {
        pieChart.setHoleColorTransparent(true);

        pieChart.setHoleRadius(60f);  //半径
        pieChart.setTransparentCircleRadius(64f); // 半透明圈
        //pieChart.setHoleRadius(0)  //实心圆

        pieChart.setDescription(description);

        // mChart.setDrawYValues(true);
        pieChart.setDrawCenterText(true);  //饼状图中间可以添加文字

        pieChart.setDrawHoleEnabled(true);

        pieChart.setRotationAngle(90); // 初始旋转角度

        // draws the corresponding description value into the slice
        // mChart.setDrawXValues(true);

        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true); // 可以手动旋转

        // display percentage values
        pieChart.setUsePercentValues(true);  //显示成百分比
        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
//      mChart.setOnChartValueSelectedListener(this);
        // mChart.setTouchEnabled(false);

//      mChart.setOnAnimationListener(this);

//        pieChart.setCenterText("Quarterly Revenue");  //饼状图中间的文字

        //设置数据
        pieChart.setData(pieData);

        // undo all highlights
//      pieChart.highlightValues(null);
//      pieChart.invalidate();

        Legend mLegend = pieChart.getLegend();  //设置比例图
        mLegend.setPosition(LegendPosition.RIGHT_OF_CHART);  //最右边显示
//      mLegend.setForm(LegendForm.LINE);  //设置比例图的形状，默认是方形
        mLegend.setXEntrySpace(7f);
        mLegend.setYEntrySpace(5f);

        pieChart.animateXY(1000, 1000);  //设置动画
        // mChart.spin(2000, 0, 360);
    }

    private PieData getPieData() {

        int count = typeTag.length;
        int totalNum = sum(typeNum);

        ArrayList<String> xValues = new ArrayList<String>();  //xVals用来表示每个饼块上的内容
        ArrayList<Entry> yValues = new ArrayList<Entry>();  //yVals用来表示封装每个饼块的实际数据
        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int i = 0; i < count; i++) {
            if (typeNum[i] != 0) {
                xValues.add(typeTag[i]);
                yValues.add(new Entry(100 * typeNum[i] / totalNum, i)); //该type所占比例
                colors.add(genColor());
            }
        }

//
//        // 饼图数据
//        /**
//         * 将一个饼形图分成四部分， 四部分的数值比例为14:14:34:38
//         * 所以 14代表的百分比就是14%
//         */
//        float quarterly1 = 14;
//        float quarterly2 = 14;
//        float quarterly3 = 34;
//        float quarterly4 = 38;
//
//        yValues.add(new Entry(quarterly1, 0));
//        yValues.add(new Entry(quarterly2, 1));
//        yValues.add(new Entry(quarterly3, 2));
//        yValues.add(new Entry(quarterly4, 3));

        //y轴的集合
        PieDataSet pieDataSet = new PieDataSet(yValues, "Quarterly Revenue 2014"/*显示在比例图上*/);
        pieDataSet.setSliceSpace(0f); //设置个饼状图之间的距离


        // 饼图颜色
//        colors.add(Color.rgb(205, 205, 205));
//        colors.add(Color.rgb(114, 188, 223));
//        colors.add(Color.rgb(255, 123, 124));
//        colors.add(Color.rgb(57, 135, 200));

        pieDataSet.setColors(colors);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = 5 * (metrics.densityDpi / 160f);
        pieDataSet.setSelectionShift(px); // 选中态多出的长度

        PieData pieData = new PieData(xValues, pieDataSet);

        return pieData;
    }

    private int sum(int[] arr) {
        int sum = 1;
        if (arr.length != 0) {
            for (int i = 0; i < arr.length; i++) {
                sum += arr[i];
            }
            sum -= 1;
        }
        return sum;
    }

    private int genColor() {
        final int MAX = 255;
        final int MIN = 0;
        Random random = new Random();
        int r = random.nextInt(MAX) % (MAX - MIN + 1) + MIN;
        int g = random.nextInt(MAX) % (MAX - MIN + 1) + MIN;
        int b = random.nextInt(MAX) % (MAX - MIN + 1) + MIN;
        return Color.rgb(r, g, b);
    }
}