package com.example.alcantara_javier_s1934603_android.adapter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.alcantara_javier_s1934603_android.R;
import com.example.alcantara_javier_s1934603_android.model.Item;

import java.util.ArrayList;


public class IncidentsAdapter extends ArrayAdapter<Item> {

    private final Context iContext;
    private static ArrayList<Item> itemsList = new ArrayList<>();

    public IncidentsAdapter(Context context, ArrayList<Item> list) {
        super(context, 0 , list);
        iContext = context;
        itemsList = list;
    }

    public void updateItems(ArrayList<Item> newList) {
        itemsList.clear();
        itemsList.addAll(newList);
        this.notifyDataSetChanged();
    }

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

        return listItem;
    }
}
