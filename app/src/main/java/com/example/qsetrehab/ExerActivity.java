package com.example.qsetrehab;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.qsetrehab.databinding.ActivityMainBinding;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.Callable;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Completable.fromCallable;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;


public class ExerActivity extends AppCompatActivity {

    // properties

    private final static String LOG_TAG = ExerActivity.class.getName();
    private SimpleSocket socket;

    private String msg;
    private String link;
    private int subj_id;

    private URL Url;
    private String strUrl;
    private String strDate;
    private final static int REQUEST_CODE_ANDROID = 1001;

    private IMUConfig mConfig = new IMUConfig();
    private IMUSession mIMUSession;
    private WifiSession mWifiSession;
    private BatterySession mBatterySession;
    private Handler mHandler = new Handler();
//    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
    private PowerManager.WakeLock mWakeLock;
    private TextView mLabelAccelDataX, mLabelAccelDataY, mLabelAccelDataZ;
    private TextView exerCountData;
    private TextView exerPercentData;
/*
    private TextView mLabelAccelBiasX, mLabelAccelBiasY, mLabelAccelBiasZ;
    private TextView mLabelGyroDataX, mLabelGyroDataY, mLabelGyroDataZ;
    private TextView mLabelGyroBiasX, mLabelGyroBiasY, mLabelGyroBiasZ;
    private TextView mLabelMagnetDataX, mLabelMagnetDataY, mLabelMagnetDataZ;
    private TextView mLabelMagnetBiasX, mLabelMagnetBiasY, mLabelMagnetBiasZ;
*/
    private TextView mLabelWifiAPNums, mLabelWifiScanInterval;
    private TextView mLabelWifiNameSSID, mLabelWifiRSSI;

    private Button endButton;
    private TextView mLabelInterfaceTime;
    private Timer mInterfaceTimer = new Timer();
    private int mSecondCounter = 0;

    private int exerCount = 0;
    private int prevCount;
    private int trigger = -1;
    private boolean upCount;
    private PieChart pieChart;
    private String exer1;
    private String exeDate;


    // Android activity lifecycle states
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exer);

        // Initialize screen labels and buttons
        initializeViews();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        subj_id = 7; //김외숙

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        strDate = dateFormat.format(date);

        //php에서 저장값 가져오는 주소 설정
        String urlPhp = "http://143.248.66.229/getExerCount.php?ID=".concat(String.valueOf(subj_id));
        link = urlPhp;

        // Initial data loading of the day
//        loadResultsBackground();
//        showGraph();
        // setup sessions
        mIMUSession = new IMUSession(this);
//        mWifiSession = new WifiSession(this);
//        mBatterySession = new BatterySession(this);
        pieChart = findViewById(R.id.chart1);

        // battery power setting
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sensors_data_logger:wakelocktag");
        mWakeLock.acquire();

        SharedPreferences patientData = getSharedPreferences("exer_data", MODE_PRIVATE);
        exer1 = patientData.getString("exer1", null);
        exeDate = patientData.getString("exerDate", null);

        if (exeDate == null || !exeDate.equals(strDate))
            prevCount = 0;
        else
            prevCount = Integer.valueOf(exer1);

        // monitor various sensor measurements
        displayIMUSensorMeasurements();

        showPiechart(prevCount, exerCount);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResultsBackground(String.valueOf(exerCount+prevCount));

                SharedPreferences patientData = getSharedPreferences("exer_data", MODE_PRIVATE);
                SharedPreferences.Editor editor = patientData.edit();

                editor.putString("exer1",String.valueOf(exerCount+prevCount));
                editor.putString("exerDate",strDate);
                editor.apply();

                finish();
            }
        });

/*
        editor.putString("exer1",String.valueOf(prevCount));
        editor.apply();

        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Plot.class);
                startActivity(intent);
            }
        });
*/
//        mLabelInterfaceTime.setText(R.string.ready_title);
    }

    private void showPiechart(int pre, int now) {

        pieChart.setUsePercentValues(true);

        List<PieEntry> value = new ArrayList<>();

        if (pre <= 1000) {
            value.add(new PieEntry((float) pre, "Performed(%)"));
            value.add(new PieEntry((float) 1000 - pre, "Remaining(%)"));
        }
        else
        {
            value.add(new PieEntry((float) 1000, "Performed(%)"));
            value.add(new PieEntry((float) 0, "Remaining(%)"));
        }

        PieDataSet pieDataSet = new PieDataSet(value, " ");
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
        Legend l = pieChart.getLegend();
        l.setTextSize(18f);


        pieData.setValueFormatter(new PercentFormatter());
        pieChart.setUsePercentValues(true);

        pieChart.getDescription().setText(" ");
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        pieData.setValueTextSize(30f);
        pieData.setValueTextColor(Color.WHITE);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mIMUSession.unregisterSensors();
        super.onDestroy();
    }

    private void updateConfig() {
        final int MICRO_TO_SEC = 1000;
    }

    public void showAlertAndStop(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(ExerActivity.this)
                        .setTitle(text)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
            }
        });
    }


    public void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ExerActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void resetUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mStartStopButton.setEnabled(true);
//                mStartStopButton.setText(R.string.start_title);
            }
        });
    }

    private void initializeViews() {

        mLabelAccelDataX = (TextView) findViewById(R.id.label_accel_X);
        mLabelAccelDataY = (TextView) findViewById(R.id.label_accel_Y);
        mLabelAccelDataZ = (TextView) findViewById(R.id.label_accel_Z);
//        exerPercentData = (TextView) findViewById(R.id.percentData);
        exerCountData = (TextView) findViewById(R.id.exerCount);
        endButton = (Button) findViewById(R.id.end_button);
/*
        mLabelAccelBiasX = (TextView) findViewById(R.id.label_accel_bias_X);
        mLabelAccelBiasY = (TextView) findViewById(R.id.label_accel_bias_Y);
        mLabelAccelBiasZ = (TextView) findViewById(R.id.label_accel_bias_Z);

        mLabelGyroDataX = (TextView) findViewById(R.id.label_gyro_X);
        mLabelGyroDataY = (TextView) findViewById(R.id.label_gyro_Y);
        mLabelGyroDataZ = (TextView) findViewById(R.id.label_gyro_Z);

        mLabelGyroBiasX = (TextView) findViewById(R.id.label_gyro_bias_X);
        mLabelGyroBiasY = (TextView) findViewById(R.id.label_gyro_bias_Y);
        mLabelGyroBiasZ = (TextView) findViewById(R.id.label_gyro_bias_Z);

        mLabelMagnetDataX = (TextView) findViewById(R.id.label_magnet_X);
        mLabelMagnetDataY = (TextView) findViewById(R.id.label_magnet_Y);
        mLabelMagnetDataZ = (TextView) findViewById(R.id.label_magnet_Z);

        mLabelMagnetBiasX = (TextView) findViewById(R.id.label_magnet_bias_X);
        mLabelMagnetBiasY = (TextView) findViewById(R.id.label_magnet_bias_Y);
        mLabelMagnetBiasZ = (TextView) findViewById(R.id.label_magnet_bias_Z);

        mLabelWifiAPNums = (TextView) findViewById(R.id.label_wifi_number_ap);
        mLabelWifiScanInterval = (TextView) findViewById(R.id.label_wifi_scan_interval);
        mLabelWifiNameSSID = (TextView) findViewById(R.id.label_wifi_SSID_name);
        mLabelWifiRSSI = (TextView) findViewById(R.id.label_wifi_RSSI);
*/
//        mStartStopButton = (Button) findViewById(R.id.button_start_stop);
//        mLabelInterfaceTime = (TextView) findViewById(R.id.label_interface_time);
    }


    private void displayIMUSensorMeasurements() {

        // get IMU sensor measurements from IMUSession
        final float[] acce_data = mIMUSession.getAcceMeasure();

/*        final float[] gyro_data = mIMUSession.getGyroMeasure();
        final float[] gyro_bias = mIMUSession.getGyroBias();

        final float[] magnet_data = mIMUSession.getMagnetMeasure();
        final float[] magnet_bias = mIMUSession.getMagnetBias();*/

        // update current screen (activity)
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLabelAccelDataX.setText(String.format(Locale.US, "%.3f", acce_data[0]));
                mLabelAccelDataY.setText(String.format(Locale.US, "%.3f", acce_data[1]));
                mLabelAccelDataZ.setText(String.format(Locale.US, "%.3f", acce_data[2]));

                // 완전히 펴고
                if(Math.ceil(acce_data[2]) < 3) {
                    if(trigger == 1) {
                        exerCount = exerCount + 1;
                        trigger = -1;
                    }
                }
                // 완전히 구부리고
                else if(Math.ceil(acce_data[2]) >= 5){
                    if(trigger == -1) {
                        trigger = 1;
                    }
                }
                exerCountData.setText(String.format(Locale.US, "%d",exerCount + prevCount) + "\n / 1000 counts");
            }
        });

        // determine display update rate (100 ms)
        final long displayInterval = 100;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayIMUSensorMeasurements();
            }
        }, displayInterval);
    }

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
                        prevCount = 0;
                        return true;
                    }
                    else {
                        in.close();
                        String[] dbExerData = line.split("&");
                        prevCount = Integer.valueOf(dbExerData[2]);
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

    private void saveResultsBackground(String result) {
        fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    strUrl = "http://143.248.66.229/insertTest.php?ID=".concat(String.valueOf(subj_id)).concat("&countTest=").concat(result);

                    Url = new URL(strUrl);  // URL화 한다.
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.
                    conn.setRequestMethod("GET"); // get방식 통신

//                    InputStream is = conn.getInputStream();        //input스트림 개방
                    conn.getPermission();
                    int resCode = conn.getResponseCode();  // connect, send http reuqest, receive htttp request
                    showToast("데이터 전송 성공.");
                    System.out.println("code = " + resCode);
                } catch (MalformedURLException | ProtocolException exception) {
                    exception.printStackTrace();
                    showToast("URL error(Get).");
                    return false;
                } catch (IOException io) {
                    io.printStackTrace();
                    showToast("데이터 전송 실패. 인터넷연결을 확인하세요.");
                    return false;
                }
                // RxJava does not accept null return value. Null will be treated as a failure.
                // So just make it return true.
                return true;
            }
        })
                .subscribeOn(Schedulers.io())
                // report or post the result to main thread.
                .observeOn(AndroidSchedulers.mainThread())
                // execute this RxJava
                .subscribe();
    }

    public void onBackPressed() {
        saveResultsBackground(String.valueOf(exerCount+prevCount));

        SharedPreferences patientData = getSharedPreferences("exer_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = patientData.edit();

        editor.putString("exer1",String.valueOf(exerCount+prevCount));
        editor.putString("exerDate",strDate);
        editor.apply();

        finish();
    }
}