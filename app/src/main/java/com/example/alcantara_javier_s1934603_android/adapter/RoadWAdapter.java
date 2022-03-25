package com.example.alcantara_javier_s1934603_android.adapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.alcantara_javier_s1934603_android.R;
import com.example.alcantara_javier_s1934603_android.model.Item;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class RoadWAdapter extends ArrayAdapter<Item> {

    private final Context iContext;
    private static ArrayList<Item> itemsList = new ArrayList<>();

    public RoadWAdapter(Context context, ArrayList<Item> list) {
        super(context, 0 , list);
        iContext = context;
        itemsList = list;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(iContext).inflate(R.layout.item,parent,false);

        Item currentItem = itemsList.get(position);

        TextView title = (TextView) listItem.findViewById(R.id.title);
        title.setText(currentItem.getTitle());

        TextView description = (TextView) listItem.findViewById(R.id.description);
        description.setText(currentItem.getDescription());

        TextView date = (TextView) listItem.findViewById(R.id.date);
        date.setText(currentItem.getParsedDate());

        Instant now = Instant.now();

        Instant sixMonthAgo = now.minus(180, ChronoUnit.DAYS);
        Instant oneMonthAgo = now.minus(30, ChronoUnit.DAYS);


        if(currentItem.getDate().before(Date.from(sixMonthAgo))) {
            date.setTextColor(Color.RED);
        } else if (currentItem.getDate().before(Date.from(oneMonthAgo))) {
            date.setTextColor(Color.rgb(255,140,0));
        } else {
            date.setTextColor(Color.rgb(0,100,0));
        }

        return listItem;
    }
}
