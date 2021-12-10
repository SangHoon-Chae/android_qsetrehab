package com.example.qsetrehab;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Completable.fromCallable;

public class IntroActivity extends AppCompatActivity {
    String link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String urlPhp = "http://203.252.230.222/getExerCount.php?subj_id=1000";
        link = urlPhp;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        loadResultsBackground();

        IntroThread introThread = new IntroThread(handler);
        introThread.start();
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
    };

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

}