package com.example.qsetrehab;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import static io.reactivex.Completable.fromCallable;
import static java.util.Observable.*;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

public class Plot extends Activity {
    Date date = new Date();
    String strDate;
    String strDate2;
    String[] phpReturnData;
    Button button_time;
    Button button_number;

    BarChart barChart1;

    private String[] exerDate;
    private String[] exerDay;
    private String id;
    private String exer1;
    private int data1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        SharedPreferences patientData = getSharedPreferences("exer_data", MODE_PRIVATE);
        exer1 = patientData.getString("exer1", null);

        data1 = Integer.valueOf(exer1);

        barChart1 = (BarChart) findViewById(R.id.chart1);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();  // combined all dataset into an arraylist
        ArrayList<BarEntry> bargroup1 = new ArrayList<>();
        bargroup1.add(new BarEntry(1,data1));
        BarDataSet barDataSet1 = new BarDataSet(bargroup1, "Q-Set Count");
        barDataSet1.setColor(Color.rgb(0,155,0));

        ArrayList<String> labels = new ArrayList<String>();
        labels.add(strDate+ "일");
        labels.add(strDate+ "일");
        labels.add(strDate+ "일");

        Legend legend = barChart1.getLegend();
//        legend.setTextSize(60);

        YAxis leftAxis = barChart1.getAxisLeft();
        leftAxis.setTextSize(60);
        LimitLine ll = new LimitLine(150, "운동 목표");
        ll.setTextColor(Color.RED);
        ll.setTextSize(18f);

        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll);
//        leftAxis.setValueFormatter(new MyYAxisValueFormatter());


        barChart1.animateY(500);
        dataSets.add(barDataSet1);
        BarData data = new BarData(dataSets);
        barChart1.setData(data);
    }
}
