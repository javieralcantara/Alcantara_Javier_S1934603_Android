package com.example.alcantara_javier_s1934603_android.adapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.alcantara_javier_s1934603_android.R;
import com.example.alcantara_javier_s1934603_android.model.Item;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

///////////////////////////////////////////////////////////////////////////////
//
// Author:           Javier Alcántara García
// Email:            jalcan200@caledonian.ac.uk
// Student ID:       S1934603
//
///////////////////////////////////////////////////////////////////////////////
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
            listItem = LayoutInflater.from(iContext).inflate(R.layout.roadwork,parent,false);

        Item currentItem = itemsList.get(position);

        TextView title = (TextView) listItem.findViewById(R.id.title);
        title.setText(currentItem.getTitle());

        // TextView description = (TextView) listItem.findViewById(R.id.description);
        // description.setText(currentItem.getDescription());

        TextView date = (TextView) listItem.findViewById(R.id.date);
        date.setText(currentItem.getParsedDate());

        TextView startDate = (TextView) listItem.findViewById(R.id.startDate);
        startDate.setText(String.format("Start Date: %s", currentItem.getParsedStartDate()));

        TextView endDate = (TextView) listItem.findViewById(R.id.endDate);
        endDate.setText(String.format("End Date: %s", currentItem.getParsedEndDate()));

        LocalDate localStart = currentItem.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localEnd = currentItem.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();


        long daysBetween = ChronoUnit.DAYS.between(localStart, localEnd);

        TextView duration = (TextView) listItem.findViewById(R.id.duration);
        duration.setText(String.format("Duration: %s days", daysBetween));

        if (daysBetween > 180) {
            duration.setTextColor(Color.RED);
        } else if (daysBetween > 30) {
            duration.setTextColor(Color.rgb(255,140,0));
        } else if (daysBetween > 10) {
            duration.setTextColor(Color.rgb(0,100,0));
        } else {
            duration.setTextColor(Color.BLUE);
        }

        Instant now = Instant.now();

        Instant sixMonthAgo = now.minus(180, ChronoUnit.DAYS);
        Instant oneMonthAgo = now.minus(30, ChronoUnit.DAYS);


        if(currentItem.getDate().before(Date.from(sixMonthAgo))) {
            date.setTextColor(Color.RED);
        } else if (currentItem.getDate().before(Date.from(oneMonthAgo))) {
            date.setTextColor(Color.rgb(255,140,0));
        } else if (currentItem.getStartDate().after(Date.from(now))) {
            date.setTextColor(Color.BLUE);
        } else {
            date.setTextColor(Color.rgb(0,100,0));
        }

        return listItem;
    }
}
