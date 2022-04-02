package com.example.alcantara_javier_s1934603_android.model;

import android.graphics.PointF;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

///////////////////////////////////////////////////////////////////////////////
//
// Author:           Javier Alcántara García
// Email:            jalcan200@caledonian.ac.uk
// Student ID:       S1934603
//
///////////////////////////////////////////////////////////////////////////////
public class Item {
    private String title;
    private String description;
    private URL link;
    private Float latitude;
    private Float longitude;
    private Date date;
    private Date startDate;
    private Date endDate;
    // When filter by date, if not found set to false
    private boolean display;

    public Item(){
        this.display = true;
    }

    public Item(String tit, String desc, URL l, Float lat, Float lon, Date d) {
        this.title = tit;
        this.description = desc;
        this.link = l;
        this.latitude = lat;
        this.longitude = lon;
        this.date = d;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URL getLink() {
        return link;
    }

    public void setLink(URL link) {
        this.link = link;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public PointF getCoords() {
        return new PointF(this.latitude, this.longitude);
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<Integer> getYearMonthDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        ArrayList<Integer> result = new ArrayList<Integer>();
        result.add(cal.get(Calendar.YEAR));
        result.add(cal.get(Calendar.MONTH));
        result.add(cal.get(Calendar.DAY_OF_MONTH));
        return result;
    }

    public String getParsedDate() {
        DateFormat df = new SimpleDateFormat("EEEE, d MMMM yyyy");

        return df.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getParsedStartDate() {
        DateFormat df = new SimpleDateFormat("EEEE, d MMMM yyyy");

        return df.format(startDate);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getParsedEndDate() {
        DateFormat df = new SimpleDateFormat("EEEE, d MMMM yyyy");

        return df.format(endDate);
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
