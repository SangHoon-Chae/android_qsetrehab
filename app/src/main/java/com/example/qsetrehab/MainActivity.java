package com.example.qsetrehab;

import static java.time.temporal.ChronoUnit.DAYS;

import android.bluetooth.le.AdvertisingSetParameters;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qsetrehab.databinding.ActivityMainBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    String exer1;  //Q-set
    String exer2;  //Walk
    String exer3;  //Crab-walk
    String exerDate;   //오늘
    String exerDate2;  //어제
    String exerDate3;  //그저께


    RecyclerView exerList;
    List<String> titles;
    List<Integer> images;
    Adapter adapter;
    private String prevExerTotal;
    private String prevExerTotal2;
    private String prevExerTotal3;

    public int exercise_type; // 1: Q-set, 2: Walk, 3: Side-walk
    public static final String WIFE_STATE = "WIFE";
    public static final String MOBILE_STATE = "MOBILE";
    public static final String NONE_STATE = "NONE";
    private boolean newtwork = true;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private BarChart barChart;
//    private String urlPhp = "http://143.248.66.229/getExerCount.php?ID=".concat(String.valueOf(7));
//    private String link;
    private TextView minuteTextview;
    ArrayList<Integer> jsonList = new ArrayList<>(); // ArrayList 선언
    ArrayList<String> labelList = new ArrayList<>(); // ArrayList 선언

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        exerList = findViewById(R.id.exList);
        exerList.setHasFixedSize(false);

//        String urlPhp = "http://203.252.230.222/getExerCount.php?subj_id=1000";
//        link = urlPhp;

        //ToolBar
        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        //BarChart
        barChart = (BarChart) findViewById(R.id.chartBar);
        graphInitSetting();       //그래프 기본 세팅

        //ExerChoice
        titles = new ArrayList<>();
        images = new ArrayList<>();

        titles.add("Q-set");
        titles.add("Q-Walk");
        titles.add("Side-Walk");

        images.add(R.drawable.exer1);
        images.add(R.drawable.exer2);
        images.add(R.drawable.exer3);

        adapter = new Adapter(this, titles, images);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        exerList.setLayoutManager(gridLayoutManager);
        exerList.setAdapter(adapter);

        initBottom_menu();

        String getNetwork =  getWhatKindOfNetwork(getApplication());

        if(getNetwork.equals("NONE")){
            newtwork = false;
            Toast.makeText(getApplicationContext(), "인터넷연결을 확인하세요.", Toast.LENGTH_SHORT).show();
        }

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

        editor.putString("exer",String.valueOf(type));
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

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        jsonList.clear();

        SharedPreferences patientData = getSharedPreferences("exer_data", MODE_PRIVATE);
        prevExerTotal2 = patientData.getString("-1_total", null);
        prevExerTotal3 = patientData.getString("-2_total", null);

        SharedPreferences exerData = getSharedPreferences("exer_data", MODE_PRIVATE);
        exer1 = exerData.getString("exer1", null);
        exer2 = exerData.getString("exer2", null);
        exer3 = exerData.getString("exer3", null);

        jsonList.add(Integer.valueOf(exer1) + Integer.valueOf(exer2) + Integer.valueOf(exer3));
        jsonList.add(Integer.valueOf(prevExerTotal2));
        jsonList.add(Integer.valueOf(prevExerTotal3));
        BarChartGraph(labelList, jsonList);
    }

    /*
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
    */

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void graphInitSetting(){
        ArrayList<Integer> exer_count = new ArrayList<>();
        String exDate;

        // 최근 3일 데이터를 sharedpreference 저장소에 저장
        // 오늘 날짜 - exerDate 해서 1 or 2 일 경우 exerDate2, exerDate3 에 exerData 저장, 그 이상은 버림.

        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.getDefault());
        SharedPreferences patientData = getSharedPreferences("exer_data", MODE_PRIVATE);
        prevExerTotal = patientData.getString("0_total", null);
        prevExerTotal2 = patientData.getString("-1_total", null);
        prevExerTotal3 = patientData.getString("-2_total", null);
        exDate = patientData.getString("exerDate", null);

        Calendar c = Calendar.getInstance();
        exerDate = dateFormat.format(c.getTime());
        c.add(Calendar.DATE, -1);
        exerDate2 = dateFormat.format(c.getTime());
        c.add(Calendar.DATE, -1);
        exerDate3 = dateFormat.format(c.getTime());

        ParsePosition pos = new ParsePosition(0);
        Date date = new Date();

        if(exDate == null)
            exDate = exerDate;
        if(exer1 == null){
            exer1 = "0";
        }
        if(exer2 == null){
            exer2 = "0";
        }
        if(exer3 == null){
            exer3 = "0";
        }

/*
        date = dateFormat.parse(exDate, pos);
        Date date2 = new Date();

        long dayDiff = date.getTime() - date2.getTime();
        int hours = (int)(dayDiff/(60*60*1000));
        int days = hours/24;

        if(days == 0) {
            prevExerTotal = Integer.valueOf(exer1) + Integer.valueOf(exer2) + Integer.valueOf(exer3);
        }
        else if (days == 1){
            exer1=null;
            exer2=null;
            exer3=null;
            prevExerTotal3 = prevExerTotal2;
            prevExerTotal2 = prevExerTotal;
        }
        else if (days == 2){
            exer1=null;
            exer2=null;
            exer3=null;
            prevExerTotal3 = prevExerTotal;
            prevExerTotal2 = 0;
        }
        else {
            exer1=null;
            exer2=null;
            exer3=null;
            prevExerTotal = 0;
            prevExerTotal2 = 0;
            prevExerTotal3 = 0;
        }

        // Exercise data 가 비어있을 경우 0 을 입력
        if(exer1 == null)
            exer_count.add(0,0);
        else
            exer_count.add(0,Integer.valueOf(exer1));

        if(exer2 == null)
            exer_count.add(1,0);
        else
            exer_count.add(1,Integer.valueOf(exer2));

        if(exer3 == null)
            exer_count.add(2,0);
        else
            exer_count.add(2,Integer.valueOf(exer3));
*/

        labelList.add("오 늘");
        labelList.add("하루전");
        labelList.add("이틀전");

        jsonList.add(Integer.valueOf(prevExerTotal));
        jsonList.add(Integer.valueOf(prevExerTotal2));
        jsonList.add(Integer.valueOf(prevExerTotal3));

        BarChartGraph(labelList, jsonList);
        barChart.setTouchEnabled(false); //확대하지못하게 막아버림! 별로 안좋은 기능인 것 같아~
        //barChart.setRendererLeftYAxis();
        barChart.setMaxVisibleValueCount(50);
        barChart.setAutoScaleMinMaxEnabled(false);
        barChart.setTouchEnabled(false); //확대하지못하게 막아버림! 별로 안좋은 기능인 것 같아~
        barChart.getAxisLeft().setAxisMaxValue(300);

        LimitLine ll1 = new LimitLine(150f, "목표 수치");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(20f);
//        ll1.setTypeface(tf);

        barChart.getAxisLeft().addLimitLine(ll1);
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
        barChart.getXAxis().setTextSize(17);
        barChart.getXAxis().setTextColor(Color.GRAY);
        barChart.getXAxis().setLabelCount(3);
        barChart.getXAxis().setCenterAxisLabels(false);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labelList));

        barChart.setData(data);
        barChart.animateXY(500, 500);
        barChart.invalidate();
    }

    public void initBottom_menu (){
//        getSupportFragmentManager().beginTransaction().replace(R.id.container, pinkFragment).commit();
        BottomNavigationView bottom_menu = findViewById(R.id.bottom_menu);
        bottom_menu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.first_tab:
//                        getSupportFragmentManager().beginTransaction().replace(R.id.container, pinkFragment).commit();
                        Toast.makeText(getApplicationContext(), "Bottom_menu_Clicked - >1", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.second_tab:

                        return true;
                    case R.id.third_tab:
                        Intent intent = new Intent(MainActivity.this, Note.class);
                        startActivity(intent);
                        return true;
                    case R.id.fourth_tab:
                        //                      getSupportFragmentManager().beginTransaction().replace(R.id.container, purpleFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }
/*
    private void loadResultsBackground() {
        fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    URL url = new URL(link);
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
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
                        if(dbExerData[3] == null){
                            dbExerData[3] = "0";
                        }
                        if(dbExerData[4] == null){
                            dbExerData[4] = "0";
                        }
                        if(dbExerData[5] == null){
                            dbExerData[5] = "0";
                        }
                        if(dbExerData[6] == null){
                            dbExerData[6] = "0";
                        }
                        if(dbExerData[7] == null){
                            dbExerData[7] = "0";
                        }
                        if(dbExerData[8] == null){
                            dbExerData[8] = "0";
                        }
                        SharedPreferences exerData = getSharedPreferences("exer_data", MODE_PRIVATE);
                        SharedPreferences.Editor editor = exerData.edit();

                        editor.putString("0_total",dbExerData[0]+ dbExerData[1] + dbExerData[2]); // total
                        editor.putString("-1_total",dbExerData[3]+ dbExerData[4] + dbExerData[5]); // -1 day
                        editor.putString("-2_total",dbExerData[6]+ dbExerData[7] + dbExerData[8]); // -2 day
                        editor.apply();
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
    */

    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}