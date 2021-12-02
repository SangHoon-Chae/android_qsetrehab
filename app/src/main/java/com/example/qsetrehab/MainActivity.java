package com.example.qsetrehab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.qsetrehab.databinding.ActivityMainBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Completable.fromCallable;

public class MainActivity extends AppCompatActivity {
    Button insert_information;
    Button status;
    String exer;
    String exerDate;
    public int exercise_type; // 1: Q-set, 2: Walk, 3: Side-walk
    public static final String WIFE_STATE = "WIFE";
    public static final String MOBILE_STATE = "MOBILE";
    public static final String NONE_STATE = "NONE";
    private boolean newtwork = true;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private BarChart barChart;
    private String urlPhp = "http://143.248.66.229/getExerCount.php?ID=".concat(String.valueOf(7));
    private String link;
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
        exer_setting(0);
        startActivity(intent);
    }

    public void Exer_walk(View v) {
        Intent intent = new Intent(MainActivity.this, ExerActivity.class);
        exer_setting(1);

        startActivity(intent);
    }

    public void Exer_crab(View v) {
        Intent intent = new Intent(MainActivity.this, ExerActivity.class);
        exer_setting(2);

        startActivity(intent);
    }

    public void exer_setting(int type){
        SharedPreferences exer_type = getSharedPreferences("exer_type", MODE_PRIVATE);
        SharedPreferences.Editor editor = exer_type.edit();

        editor.putString("exer_type",String.valueOf(type));
        editor.apply();
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



    private void loadResultsBackground() {
        fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    URL url = new URL(link);
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    link = urlPhp;
                    request.setURI(new URI(link));
                    HttpResponse response = client.execute(request);
                    BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    // DB 에 Data 가 없는 경우
                    if (line == null) {
                        in.close();
//                        prevCount = 0;
                        return true;
                    }
                    else {
                        in.close();
                        String[] dbExerData = line.split("&");
//                        prevCount = Integer.valueOf(dbExerData[2]);
                        return true;               // String 형태로 반환
                    }
                } catch (Exception e) {
                    return true;
                }

                // RxJava does not accept null return value. Null will be treated as a failure.
                // So just make it return true.

            }
        }) // Execute in IO thread, i.e. background thread.
                .subscribeOn(Schedulers.newThread())
                // report or post the result to main thread.
                .observeOn(AndroidSchedulers.mainThread())
                // execute this RxJava
                .subscribe();
    }

    public void graphInitSetting(){

        SharedPreferences patientData = getSharedPreferences("exer_data", MODE_PRIVATE);
        exer = patientData.getString("exer1", null);
        exerDate = patientData.getString("exerDate", null);
        String exerDate2;
        String exerDate3;

        if(exer == null)
            exer = "0";

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        Date date = new Date();
        Date date2 = new Date();
        Calendar c = Calendar.getInstance();
        exerDate = dateFormat.format(c.getTime());
        c.add(Calendar.DATE, -1);
        exerDate2 = dateFormat.format(c.getTime());
        c.add(Calendar.DATE, -1);
        exerDate3 = dateFormat.format(c.getTime());

        labelList.add(exerDate);
        labelList.add(exerDate2);
        labelList.add(exerDate3);

        jsonList.add(Integer.valueOf(exer));
        jsonList.add(20);
        jsonList.add(30);

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
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawLabels(true);
        barChart.getXAxis().setTextSize(15);
        barChart.getXAxis().setLabelCount(3);
        barChart.getXAxis().setCenterAxisLabels(false);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labelList));

        barChart.setData(data);
        barChart.animateXY(1000, 1000);
        barChart.invalidate();
    }

    public void onBackPressed() {
        finish();
    }
}