package com.example.qsetrehab;

import static io.reactivex.Completable.fromCallable;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Administrator on 2017-08-08.
 */
public class Note extends AppCompatActivity {
    private RatingBar ratingBar;
    private RatingBar ratingQolBar;
    private Button saveData;
    private Button saveNote;
    private Button clearData;
    private String vasRate;
    private String qolRate;
    private TextView txtRatingValue;
    private int gender;
//    public  MyFileManager myFileManager;
    public String message;
    private String strDate;
    private String strDate2;
    private EditText nameEdit;
    private EditText idEdit;
    private EditText thres_idEdit;
    private EditText birth_dateEdit;
    private EditText etcEdit;
    private EditText groupEdit;

    String string = "Hello world!";
    private URL Url;
    private String strUrl;
    private String name;
    private String birth_date;
    private String group;
    private String sex;
    private String id;

//    ArrayList<Test> patients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        //Initialize
        addListenerOnButton();
        gender = -1;

        setTitle("정보 입력");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        Date date = new Date();
        strDate = dateFormat.format(date);
        strDate2 = dateFormat2.format(date);

//        strDate = "현재시각 : " + strDate;

        TextView dateText = (TextView) findViewById(R.id.date);
        dateText.setText(strDate);

        TextView idText = (TextView) findViewById(R.id.id);
        idText.setText("등록번호");
        TextView nameText = (TextView) findViewById(R.id.name);
        nameText.setText("이름");
        TextView groupText = (TextView) findViewById(R.id.group);
        groupText.setText("소속");
        TextView genderText = (TextView) findViewById(R.id.gender);
        genderText.setText("성별");
        TextView birth = (TextView) findViewById(R.id.birth_date);
        birth.setText("생년월일");

        idEdit = (EditText) findViewById(R.id.id_edit);
        nameEdit = (EditText) findViewById(R.id.name_edit);
        birth_dateEdit = (EditText) findViewById(R.id.birth_date_edit);
        groupEdit = (EditText) findViewById(R.id.group_edit);

        SharedPreferences patientData = getSharedPreferences("subject_information", MODE_PRIVATE);

        idEdit.setText(patientData.getString("id", null));
        nameEdit.setText(patientData.getString("name", null));
        birth_dateEdit.setText(patientData.getString("birth_date", null));
        groupEdit.setText(patientData.getString("group", null));

        id = idEdit.getText().toString();
        name = nameEdit.getText().toString();
        birth_date =  birth_dateEdit.getText().toString();
        group = groupEdit.getText().toString();

        if (shouldAskPermissions()) {
            askPermissions();
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.man:
                if (checked)
                    gender = 1;
                break;
            case R.id.woman:
                if (checked)
                    gender = 0;
                break;
        }
    }


    private void saveResultsBackground(String result) {
        fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    strUrl = "http://203.252.230.222/insert_subj_info.php?subj_id=" + id + "&&name=" + name + "&birthday=" + birth_date + "&sex=" + sex;
                    Url = new URL(strUrl);  // URL화 한다.
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.
                    conn.setRequestMethod("GET"); // get방식 통신

                    //                    InputStream is = conn.getInputStream();        //input스트림 개방
                    conn.getPermission();
                    int resCode = conn.getResponseCode();  // connect, send http reuqest, receive htttp request
                    showToast("데이터 전송 성공.");
                    System.out.println("code = " + resCode);
                }
                catch (MalformedURLException | ProtocolException exception) {
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


    public void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Note.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveResultsBackground() {
        fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                SharedPreferences patientData = getSharedPreferences("subject_information", MODE_PRIVATE);
                SharedPreferences.Editor editor = patientData.edit();

                id = idEdit.getText().toString();
                name = nameEdit.getText().toString();
                birth_date =  birth_dateEdit.getText().toString();
                group = groupEdit.getText().toString();

                editor.putString("id", id);
                editor.putString("name", name);
                editor.putString("birth_date", birth_date);
                editor.putString("group", group);
                editor.apply();

                if (gender != -1) {
                    if (gender == 1) {
                        sex = "M";
                    } else if (gender == 0) {
                        sex = "F";
                    }
                }

                try {
                    strUrl = "http://143.248.66.229/hbr_project_php/insertSubjinfo.php?&id=" + id + "&name=" + name + "&sex=" + sex + "&birth_date=" + birth_date + "&group_name=" + group;

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

    public void addListenerOnButton() {
        saveData = (Button) findViewById(R.id.button_save);
        clearData = (Button) findViewById(R.id.button_clear);
        saveNote = (Button) findViewById(R.id.button_note);

        //if click on me, then display the current rating value.
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 데이터 저장.
                saveResultsBackground();
            }
        });

        //if click on me, then display the current rating value.
        saveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 데이터 저장.
                SharedPreferences patientData = getSharedPreferences("subject_information", MODE_PRIVATE);
                SharedPreferences.Editor editor = patientData.edit();

                id = idEdit.getText().toString();
                name = nameEdit.getText().toString();
                birth_date =  birth_dateEdit.getText().toString();
                group = groupEdit.getText().toString();

                editor.putString("id", id);
                editor.putString("name", name);
                editor.putString("birth_date", birth_date);
                editor.putString("group", group);
                editor.apply();

                try {
                    Toast.makeText(getApplicationContext(), "SAVE_NOTE", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        clearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SharedPreferences test2 = getSharedPreferences("patientData", 0);
//                SharedPreferences sharedPref = getSharedPreferences("patientData", Context.MODE_PRIVATE);
//                String name =sharedPref.getString("name", null);
                idEdit.setText("");
                nameEdit.setText("");
                birth_dateEdit.setText("");
                groupEdit.setText("");
            }
        });
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }
}
