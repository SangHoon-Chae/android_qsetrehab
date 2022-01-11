package com.example.qsetrehab;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by kijong on 2017-03-05.
 */

public class MyFileManager {

    public static final String MY_APP_DIR_NAME = "MyApp";
    public String fileName;

    public void initialize() {
        // SD카드 루트/MyAppRoot 폴더가 없으면 생성한다.
        File myAppFolder = new File(getMyAppRootPath());
        if (!myAppFolder.exists()) {
            myAppFolder.mkdir();
        }
        if (!myAppFolder.exists()) {
            myAppFolder.mkdir();
        }
    }

    /**
     * 파일에 써있는 문자열 전체를 읽어서 반환한다.
     */
    public String getFileContents() {
        try {
            FileReader fileReader = new FileReader(getMyFilePath());
            BufferedReader bufferedrReader = new BufferedReader(fileReader);

            String contents = "";
            String line = "";
            while ((line = bufferedrReader.readLine()) != null) {
                contents = contents + line + '\n';
            }

            bufferedrReader.close();
            return contents;
        } catch (Exception e){
            // 에러가 날 경우에는 빈 문자열을 반환한다.
            return "err";
        }
    }

    public String getFileLine() {
        try {
            FileReader fileReader = new FileReader(getMyFilePath());
            BufferedReader bufferedrReader = new BufferedReader(fileReader);

            String contents = "";
            String line = "";
            line = bufferedrReader.readLine();
            bufferedrReader.close();
            return line;
        } catch (Exception e){
            // 에러가 날 경우에는 빈 문자열을 반환한다.
            return "err";
        }
    }
    /**
     * 새 문자열을 파일에 추가한다.
     */
    public void addStringToFile(String s) {
        try {
            // FileWriter 생성 시에 true 인자를 추가하면 새 문자열을 파일에 "추가" 하게 됩니다.
            FileWriter fileWriter = new FileWriter(getMyFilePath(), true);
            fileWriter.write(s);
            fileWriter.flush();
            fileWriter.close();
        } catch(Exception e) {
        }
    }

    /**
     * 파일 내용을 초기화 한다.
     */
    public void clearFile() {
        try {
            // FileWriter 생성 시에 true 인자가 없으면 새로쓰기가 됩니다.
            FileWriter fileWriter = new FileWriter(getMyFilePath());
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
        }
    }

    /**
     * 외부 저장소(보통 SD카드) 루트 경로를 얻는다.
     * @return
     */
    public String getExtStorageRootPath() {
        return  Environment.getExternalStorageDirectory().getAbsolutePath();
        //        return  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    /**
     * 내 앱 폴더 경로를 얻는다.
     * @return
     */
    public String getMyAppRootPath() {
        return getExtStorageRootPath() + "/" + MY_APP_DIR_NAME;
    }

    /**
     * 내가 사용할 파일 경로를 얻는다.
     * @return
     */
    public String getMyFilePath() {
        return getMyAppRootPath() + "/" + fileName;
    }
}
