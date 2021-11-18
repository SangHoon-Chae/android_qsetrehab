package com.example.qsetrehab;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpSend extends Thread {
    public URL Url;

    @Override
    public void run() {
        while( ! Thread.interrupted() )
        {
            try {
                HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.
                conn.setRequestMethod("GET"); // get방식 통신

                InputStream is = conn.getInputStream();        //input스트림 개방
                int resCode = conn.getResponseCode();  // connect, send http reuqest, receive htttp request
                System.out.println("code = " + resCode);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public void sendUrl(String url){
        try {
            Url = new URL(url);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}