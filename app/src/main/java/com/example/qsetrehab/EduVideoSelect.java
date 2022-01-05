package com.example.qsetrehab;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EduVideoSelect extends AppCompatActivity {

    RecyclerView exerList;
    List<String> titles;
    List<Integer> images;
    Adapter2 adapter;
    String exer1;  //Q-set
    String exer2;  //Walk
    String exer3;  //Crab-walk

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edu_video_select);
        exerList = findViewById(R.id.exerList);

        titles = new ArrayList<>();
        images = new ArrayList<>();

        titles.add("Q-set");
        titles.add("Q-Walk");
        titles.add("Side-Walk");
        titles.add("Squat");

        images.add(R.drawable.exer1);
        images.add(R.drawable.exer2);
        images.add(R.drawable.exer3);
        images.add(R.drawable.exer4);

        adapter = new Adapter2(this, titles, images);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        exerList.setLayoutManager(gridLayoutManager);
        exerList.setAdapter(adapter);
    }
}