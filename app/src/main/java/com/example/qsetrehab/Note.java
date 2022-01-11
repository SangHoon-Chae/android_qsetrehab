package com.example.qsetrehab;

import static android.os.Environment.DIRECTORY_PICTURES;
import static io.reactivex.Completable.fromCallable;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.gun0912.tedpermission.PermissionBuilder;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    // 카메라 변수
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilePath;
    private Uri photoUri;


    private URL Url;
    private String strUrl;
    private String name;
    private String birth_date;
    private String group;
    private String sex;
    private String id;
    private MediaScanner mMediaScanner;

//    ArrayList<Test> patients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);


        // 사진 저장 후 미디어 스캐닝을 돌려줘야 갤러리에 반영됨.
        mMediaScanner = MediaScanner.getInstance(getApplicationContext());

        // 권한 체크
        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("거부하셨습니다.")
                .setRationaleMessage("카메라 권한이 필요합니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

        //Initialize
        findViewById(R.id.take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try{
                        photoFile = creatImageFile();
                    } catch (IOException e) {

                    }

                    if(photoFile != null) {
                        photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    }
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

                }

            }
        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegree(exifOrientation);
            } else {
                exifDegree = 0;
            }

            String result = "";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
            Date             curDate   = new Date(System.currentTimeMillis());
            String           filename  = formatter.format(curDate);

            String           strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "Rehab_Picture" + File.separator;
            File file = new File(strFolderName);
            if( !file.exists() )
                file.mkdirs();

            File f = new File(strFolderName + "/" + filename + ".png");
            result = f.getPath();

            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = "Save Error fOut";
            }

            // 비트맵 사진 폴더 경로에 저장
            rotate(bitmap,exifDegree).compress(Bitmap.CompressFormat.PNG, 70, fOut);

            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
                // 방금 저장된 사진을 갤러리 폴더 반영 및 최신화
                mMediaScanner.mediaScanning(strFolderName + "/" + filename + ".png");
            } catch (IOException e) {
                e.printStackTrace();
                result = "File close Error";
            }

            // 이미지 뷰에 비트맵을 set하여 이미지 표현
            ((ImageView) findViewById(R.id.cv_picture)).setImageBitmap(rotate(bitmap, exifDegree));
        }


    }
    private int exifOrientationToDegree(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private File creatImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Test_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    PermissionListener permissionListener =new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(getApplicationContext(), "권한이 허용됨.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한이 거부됨.", Toast.LENGTH_SHORT).show();
        }
    };

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

                editor.putString("id", id);
                editor.apply();

                if (gender != -1) {
                    if (gender == 1) {
                        sex = "M";
                    } else if (gender == 0) {
                        sex = "F";
                    }
                } else {
                    sex = null;
                    showToast("성별을 지정해주세요. 데이터 전송 실패");
                }

                try {
                    strUrl = "http://203.252.230.222/insert_subj_info.php?subj_id=" + id + "&name=" + name + "&birthday=" + birth_date + "&sex=" + sex;

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
                idEdit.setText(null);
                nameEdit.setText(null);
                birth_dateEdit.setText(null);
                groupEdit.setText(null);
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
