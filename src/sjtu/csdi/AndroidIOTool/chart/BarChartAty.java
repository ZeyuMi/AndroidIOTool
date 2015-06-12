package sjtu.csdi.AndroidIOTool.chart;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import sjtu.csdi.AndroidIOTool.R;

import java.util.ArrayList;

/**
 * Created by Yang on 2015/6/10.
 */
public class BarChartAty extends Activity {
    private String[] typeTag;   //数据类型说明，及xValue
    private int[] typeNum;      //每种数据类型所对应的数据量，及yValue
    private String typeIntro;   //数据类型的说明
    private String description; //pie chart的描述信息

    private BarChart mBarChart;
    private BarData mBarData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barchart_aty);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        typeTag = bundle.getStringArray(getResources().getString(R.string.typeTag));
        typeNum = bundle.getIntArray(getResources().getString(R.string.typeNum));
        description = bundle.getString(getResources().getString(R.string.chartDescription));
        typeIntro = bundle.getString(getResources().getString(R.string.typeIntro));

        mBarChart = (BarChart) findViewById(R.id.bar_chart);
        mBarData = getBarData();
        showBarChart(mBarChart, mBarData);
    }

    private void showBarChart(BarChart barChart, BarData barData) {
        barChart.setDrawBorders(false);  ////是否在折线图上添加边框

        barChart.setDescription(typeIntro);// 数据描述

        // 如果没有数据的时候，会显示这个，类似ListView的EmptyView
        barChart.setNoDataTextDescription("You need to provide data for the chart.");

        barChart.setDrawGridBackground(false); // 是否显示表格颜色
        barChart.setGridBackgroundColor(Color.WHITE & 0x70FFFFFF); // 表格的的颜色，在这里是是给颜色设置一个透明度

        barChart.setTouchEnabled(true); // 设置是否可以触摸

        barChart.setDragEnabled(true);// 是否可以拖拽
        barChart.setScaleEnabled(true);// 是否可以缩放

        barChart.setPinchZoom(false);//

//      barChart.setBackgroundColor();// 设置背景

        barChart.setDrawBarShadow(true);

        barChart.setData(barData); // 设置数据

        Legend mLegend = barChart.getLegend(); // 设置比例图标示

        mLegend.setForm(LegendForm.CIRCLE);// 样式
        mLegend.setFormSize(6f);// 字体
        mLegend.setTextColor(Color.BLACK);// 颜色

//      X轴设定
//      XAxis xAxis = barChart.getXAxis();
//      xAxis.setPosition(XAxisPosition.BOTTOM);

        barChart.animateX(2500); // 立即执行的动画,x轴
    }

    private BarData getBarData() {
        ArrayList<String> xValues = new ArrayList<String>();
        ArrayList<BarEntry> yValues = new ArrayList<BarEntry>();

        int count = typeTag.length;
        int order = 0;

        for (int i = 0; i < count; i++) {
            if (typeNum[i] != 0) {
                xValues.add(typeTag[i]);
                yValues.add(new BarEntry(typeNum[i], order++));
            }
        }

        // y轴的数据集合
        BarDataSet barDataSet = new BarDataSet(yValues, description);

        barDataSet.setColor(Color.rgb(114, 188, 223));

        ArrayList<BarDataSet> barDataSets = new ArrayList<BarDataSet>();
        barDataSets.add(barDataSet); // add the datasets

        BarData barData = new BarData(xValues, barDataSets);

        return barData;
    }
}