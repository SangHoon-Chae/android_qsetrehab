package com.example.qsetrehab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.qsetrehab.databinding.ActivityMainBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button insert_information;
    Button status;
    String exer;
    String exerDate;
    Button rom;
    public static final String WIFE_STATE = "WIFE";
    public static final String MOBILE_STATE = "MOBILE";
    public static final String NONE_STATE = "NONE";
    private boolean newtwork = true;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private BarChart barChart;
    private TextView minuteTextview;
    ArrayList<Integer> jsonList = new ArrayList<>(); // ArrayList 선언
    ArrayList<String> labelList = new ArrayList<>(); // ArrayList 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exer);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        barChart = (BarChart) findViewById(R.id.chartBar);
        graphInitSetting();       //그래프 기본 세팅


        insert_information= (Button) findViewById(R.id.button_user);
        status= (Button) findViewById(R.id.button_status);

        String getNetwork =  getWhatKindOfNetwork(getApplication());

        if(getNetwork.equals("NONE")){
            newtwork = false;
            Toast.makeText(getApplicationContext(), "인터넷연결을 확인하세요.", Toast.LENGTH_SHORT).show();
        }

        insert_information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExerActivity.class);
                startActivity(intent);
            }
        });
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExerActivity.class);
                startActivity(intent);
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Note.class);
                startActivity(intent);

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void Exer_qset(View v) {
        Intent intent = new Intent(MainActivity.this, ExerActivity.class);
        startActivity(intent);
    }

    /*
        public void pressAlarm(View v) {
            Intent intent = new Intent(MainActivity.this, AlarmSet2.class);
            startActivity(intent);
        }
    */

    public void Exer_walk(View v) {
        Intent intent = new Intent(MainActivity.this, ExerActivity.class);
        startActivity(intent);
    }

    public void Exer_crab(View v) {
        Intent intent = new Intent(MainActivity.this, ExerActivity.class);
        startActivity(intent);
    }

    public static String getWhatKindOfNetwork(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return WIFE_STATE;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return MOBILE_STATE;
            }
        }
        return NONE_STATE;
    }


    public void graphInitSetting(){

        SharedPreferences patientData = getSharedPreferences("exer_data", MODE_PRIVATE);
        exer = patientData.getString("exer1", null);
        exerDate = patientData.getString("exerDate", null);

        labelList.add(exerDate);
        labelList.add("월");
        labelList.add("화");
        labelList.add("수");
        labelList.add("목");
        labelList.add("금");
        labelList.add("토");

        jsonList.add(Integer.valueOf(exer));
        jsonList.add(20);
        jsonList.add(30);
        jsonList.add(40);
        jsonList.add(50);
        jsonList.add(60);
        jsonList.add(60);


        BarChartGraph(labelList, jsonList);
        barChart.setTouchEnabled(false); //확대하지못하게 막아버림! 별로 안좋은 기능인 것 같아~
        //barChart.setRendererLeftYAxis();
        barChart.setMaxVisibleValueCount(50);
        barChart.setTop(50);
        barChart.setBottom(0);
        barChart.setAutoScaleMinMaxEnabled(true);
//        barChart.getAxisLeft().setAxisMaxValue(80);
//        barChart.getXAxis().setAxisMaximum((float) 30);

    }
    /**
     * 그래프함수
     */
    private void BarChartGraph(ArrayList<String> labelList, ArrayList<Integer> valList) {
        // BarChart 메소드

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < valList.size(); i++) {
            entries.add(new BarEntry(i, (Integer) valList.get(i)));
        }

        BarDataSet depenses = new BarDataSet(entries, "일일 사용시간"); // 변수로 받아서 넣어줘도 됨
        depenses.setAxisDependency(YAxis.AxisDependency.LEFT);
//        barChart.setDescription(" ");
//        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
//        dataSets.add((IBarDataSet) depenses);
        BarData data = new BarData (depenses);

        barChart.getAxisRight().setEnabled(false);

        depenses.setColors(ColorTemplate.LIBERTY_COLORS); //
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labelList));

        barChart.setData(data);
        barChart.animateXY(1000, 1000);
        barChart.invalidate();
    }

    public void onBackPressed() {
        finish();
    }
}