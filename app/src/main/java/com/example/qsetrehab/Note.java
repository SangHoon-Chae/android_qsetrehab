package com.example.qsetrehab;

import static android.os.Environment.DIRECTORY_PICTURES;
import static io.reactivex.Completable.fromCallable;
import static java.security.AccessController.getContext;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.http.RequestQueue;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
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

    // ????????? ??????
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilePath;
    private String prevImagePath;
    private Uri photoUri;
    String image = "";
    String imageURL=  "";
    String subj_id;

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

        // ?????? ?????? ??? ????????? ???????????? ???????????? ???????????? ?????????.
        mMediaScanner = MediaScanner.getInstance(getApplicationContext());

        // ?????? ??????
        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("?????????????????????.")
                .setRationaleMessage("????????? ????????? ???????????????.")
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

        findViewById(R.id.save_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences photoPath = getSharedPreferences("subject_information", MODE_PRIVATE);
                SharedPreferences.Editor editor = photoPath.edit();

                editor.putString("photoPath", imageFilePath);
                editor.apply();

                httpPostData();
            }
        });

        addListenerOnButton();
        gender = -1;

        setTitle("?????? ??????");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        Date date = new Date();
        strDate = dateFormat.format(date);
        strDate2 = dateFormat2.format(date);

//        strDate = "???????????? : " + strDate;

        TextView dateText = (TextView) findViewById(R.id.date);
        dateText.setText(strDate);

        TextView idText = (TextView) findViewById(R.id.id);
        idText.setText("????????????");
        TextView nameText = (TextView) findViewById(R.id.name);
        nameText.setText("??????");
        TextView groupText = (TextView) findViewById(R.id.group);
        groupText.setText("??????");
        TextView genderText = (TextView) findViewById(R.id.gender);
        genderText.setText("??????");
        TextView birth = (TextView) findViewById(R.id.birth_date);
        birth.setText("????????????");

        idEdit = (EditText) findViewById(R.id.id_edit);
        nameEdit = (EditText) findViewById(R.id.name_edit);
        birth_dateEdit = (EditText) findViewById(R.id.birth_date_edit);
        groupEdit = (EditText) findViewById(R.id.group_edit);

        SharedPreferences patientData = getSharedPreferences("subject_information", MODE_PRIVATE);

        subj_id = patientData.getString("id", null);
        idEdit.setText(patientData.getString("id", null));
        nameEdit.setText(patientData.getString("name", null));
        birth_dateEdit.setText(patientData.getString("birth_date", null));
        groupEdit.setText(patientData.getString("group", null));

        id = idEdit.getText().toString();
        name = nameEdit.getText().toString();
        birth_date =  birth_dateEdit.getText().toString();
        group = groupEdit.getText().toString();

        //PhotoView ????????????
        prevImagePath = patientData.getString("photoPath", null);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(prevImagePath);
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

        File imgFile = new File(prevImagePath);

        if(imgFile.exists()) {
            Bitmap myBitmap= BitmapFactory.decodeFile(prevImagePath);
            ((ImageView) findViewById(R.id.cv_picture)).setImageBitmap(rotate(myBitmap, exifDegree));
        }

        if (shouldAskPermissions()) {
            askPermissions();
        }
    }

    private void httpPostData()  {
        new Thread ()
        {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://203.252.230.222/setPhoto.php");
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();   // ??????
                    //--------------------------
                    //   ?????? ?????? ?????? - ???????????? ????????????
                    //--------------------------
                    http.setDefaultUseCaches(false);
                    http.setDoInput(true);                         // ???????????? ?????? ?????? ??????
                    http.setDoOutput(true);                       // ????????? ?????? ?????? ??????
                    http.setRequestMethod("POST");         // ?????? ????????? POST
                    http.getPermission();

                    // ???????????? ????????? <Form>?????? ?????? ????????? ?????? ?????? ???????????? ??????????????? ??? ????????????
                    http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                    //--------------------------
                    //   ????????? ??? ??????
                    //--------------------------
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("id").append("=").append(subj_id).append("&");                 // php ????????? ??? ??????
                    buffer.append("BLOB").append("=").append(imageURL);   // php ?????? ?????? '$' ????????? ?????????

                    OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                    PrintWriter writer = new PrintWriter(outStream);
                    writer.write(buffer.toString());
                    int resCode = http.getResponseCode();
                    writer.flush();

                    /*
                    //--------------------------
                    //   ???????????? ????????????
                    //--------------------------
                    InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "EUC-KR");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuilder builder = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {       // ???????????? ??????????????? ????????? ???????????? ??????????????? ?????????
                        builder.append(str + "\n");                     // View??? ???????????? ?????? ?????? ????????? ??????
                    }
                    myResult = builder.toString();                       // ??????????????? ?????? ????????? ??????
                    ((TextView)(findViewById(R.id.text_result))).setText(myResult);
                    Toast.makeText(MainActivity.this, "?????? ??? ?????? ??????", 0).show();
                    */
                } catch (MalformedURLException e) {
                    //
                } catch (IOException e) {
                    //
                } // try
            }
        }.start();
    }


    private Bitmap resize(Bitmap bm) {
        Configuration config = getResources().getConfiguration();

        if(config.smallestScreenWidthDp > 800)
            bm = Bitmap.createScaledBitmap(bm, 400, 240, true);
        else if(config.smallestScreenWidthDp >= 600)
            bm = Bitmap.createScaledBitmap(bm, 400, 240, true);
//            bm = Bitmap.createScaledBitmap(bm, 300, 180, true);
        else if(config.smallestScreenWidthDp >= 400)
            bm = Bitmap.createScaledBitmap(bm, 400, 240, true);
//            bm = Bitmap.createScaledBitmap(bm, 200, 120, true);
        else if(config.smallestScreenWidthDp >= 360)
            bm = Bitmap.createScaledBitmap(bm, 400, 240, true);
//            bm = Bitmap.createScaledBitmap(bm, 180, 108, true);
        else
            bm = Bitmap.createScaledBitmap(bm, 400, 240, true);
//            bm = Bitmap.createScaledBitmap(bm, 160, 96, true);

        return bm;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        ImageView imageView = findViewById(R.id.cv_picture);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
/*
            try {
//                Uri uri = data.getData();
                Glide.with(getApplicationContext()).load(photoUri).into(imageView);

                Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
                bitmap = resize(bitmap);

                String image = bitmapToByteArray(bitmap);
                changeProfileImageToDB(image);

            } catch (Exception e) {

            }
        } else if (resultCode== RESULT_CANCELED);

            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);

            bitmap = resize(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            image = byteArrayToBinaryString(bytes);

//            image = byteArrayToBinaryString(bytes);
/*
            fromCallable(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        strUrl = "http://203.252.230.222/insertPhoto.php?subj_id="+subj_id+image;

                            Url = new URL(strUrl);  // URL??? ??????.
                            HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL??? ????????? ?????? ??????.
                            conn.setRequestMethod("GET"); // get?????? ??????

                            //                    InputStream is = conn.getInputStream();        //input????????? ??????
                            conn.getPermission();
                            int resCode = conn.getResponseCode();  // connect, send http reuqest, receive htttp request
                            showToast("????????? ?????? ??????.");
                            System.out.println("code = " + resCode);
                    } catch (MalformedURLException | ProtocolException exception) {
                        exception.printStackTrace();
                        showToast("URL error(Get).");
                        return false;
                    } catch (IOException io) {
                        io.printStackTrace();
                        showToast("????????? ?????? ??????. ?????????????????? ???????????????.");
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
*/
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

            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            bitmap = resize(rotate(bitmap,exifDegree));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);


            // ????????? ?????? ?????? ????????? ??????
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, fOut);

            byte[] bytes = baos.toByteArray();
            image = Base64.encodeToString(bytes, Base64.DEFAULT);
            try {
                imageURL = URLEncoder.encode(image,"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
                // ?????? ????????? ????????? ????????? ?????? ?????? ??? ?????????
                mMediaScanner.mediaScanning(strFolderName + "/" + filename + ".png");
            } catch (IOException e) {
                e.printStackTrace();
                result = "File close Error";
            }

            // ????????? ?????? ???????????? set?????? ????????? ??????
            ((ImageView) findViewById(R.id.cv_picture)).setImageBitmap(bitmap);
        }
    }

    public static String bitmapToByteArray(Bitmap bitmap) {
        String image = "";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        image = "&image=" + byteArrayToBinaryString(byteArray);
        return image;
    }

    public static String byteArrayToBinaryString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {
            sb.append(byteToBinaryString(b[i]));
        }
        return sb.toString();
    }

    public static String byteToBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        } return sb.toString();
    }

    /*
    public void changeProfileImageToDB(String image) {
//        Response.Listener<String> responseListener = new Response.Listener<String>(){

        Profile_Img_Check profile_img_check = new Profile_Img_Check(id, image, responseListener);
        RequestQueue queue = Volley.newRequestQueue(getContext());


    }
*/
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
                ".png",
                storageDir
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    PermissionListener permissionListener =new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(getApplicationContext(), "????????? ?????????.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "????????? ?????????.", Toast.LENGTH_SHORT).show();
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
                    showToast("????????? ??????????????????. ????????? ?????? ??????");
                }

                try {
                    strUrl = "http://203.252.230.222/insert_subj_info.php?subj_id=" + id + "&name=" + name + "&birthday=" + birth_date + "&sex=" + sex;

                    Url = new URL(strUrl);  // URL??? ??????.
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL??? ????????? ?????? ??????.
                    conn.setRequestMethod("GET"); // get?????? ??????

//                    InputStream is = conn.getInputStream();        //input????????? ??????
                    conn.getPermission();
                    int resCode = conn.getResponseCode();  // connect, send http reuqest, receive htttp request
                    showToast("????????? ?????? ??????.");
                    System.out.println("code = " + resCode);
                } catch (MalformedURLException | ProtocolException exception) {
                    exception.printStackTrace();
                    showToast("URL error(Get).");
                    return false;
                } catch (IOException io) {
                    io.printStackTrace();
                    showToast("????????? ?????? ??????. ?????????????????? ???????????????.");
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
                // ????????? ??????.
                saveResultsBackground();
            }
        });

        //if click on me, then display the current rating value.
        saveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ????????? ??????.
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
