package com.example.qsetrehab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    List<String> titles;
    List<Integer> images;
    LayoutInflater inflater;

    public Adapter(Context ctx, List<String> titles, List<Integer> images) {
        this.titles = titles;
        this.images = images;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_grid_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(titles.get(position));
        holder.gridIcon.setImageResource(images.get(position));
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView gridIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textView2);
            gridIcon = itemView.findViewById(R.id.imageView2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (title.getText().toString()) {
                        case "Q-set":
                            Toast.makeText(v.getContext(), "Clicked -1"+ title.getText().toString(), Toast.LENGTH_SHORT).show();
                            break;
                        case "Q-Walk":
                            Toast.makeText(v.getContext(), "Clicked -2"+title.getText().toString(), Toast.LENGTH_SHORT).show();
                            break;
                        case "Side-Walk":
                            Toast.makeText(v.getContext(), "Clicked -3"+title.getText().toString(), Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
            });
        }
/*
        public void Exer_qset(View v) {
            //You can change the fragment, something like this, not tested, please correct for your desired output:
            Activity activity = v.getContext();
            Fragment CityName = new Fragment();
            //Create a bundle to pass data, add data, set the bundle to your fragment and:
            activity.getFragmentManager().beginTransaction().replace(R.id.fragment_container, cityName).addToBackStack(null).commit();     //Here m getting error

        }

        public void Exer_walk(View v) {
            Intent intent = new Intent(v.getContext(), ExerActivity.class);
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
    }
    */
    }
}