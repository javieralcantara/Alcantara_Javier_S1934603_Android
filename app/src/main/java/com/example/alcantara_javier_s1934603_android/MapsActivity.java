package com.example.alcantara_javier_s1934603_android;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.example.alcantara_javier_s1934603_android.adapter.ItemAdapter;
import com.example.alcantara_javier_s1934603_android.model.Item;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.alcantara_javier_s1934603_android.databinding.ActivityMapsBinding;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.stream.Collectors;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private ViewSwitcher avw;
    private Button s1Button;
    private Button s2Button;

    // My Code
    private ArrayList<Item> items = new ArrayList<Item>();
    private Handler updateUIHandler = null;
    // Message type code.
    private final static int MESSAGE_UPDATE_LIST =0;
    private final static int MESSAGE_UPDATE_PERCENTAGE =1;
    private final static int MESSAGE_UPDATE_DATE =2;

    private String result = "";

    private ListView listView;
    private ItemAdapter iAdapter;
    private TextView tvDate;

    private Button startButton;
    private Button startButton1;
    private Button startButton2;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Handler.
        createUpdateUiHandler();

        avw = (ViewSwitcher) findViewById(R.id.vwSwitch);

        tvDate = findViewById(R.id.tvDate);
        Button btPickDate = findViewById(R.id.btPickDate);
        Button btClearDate = findViewById(R.id.btClearDate);
        btPickDate.setOnClickListener(this);

        btClearDate.setOnClickListener(this);

        startButton = findViewById(R.id.startButton);
        startButton1 = findViewById(R.id.startButton1);
        startButton2 = findViewById(R.id.startButton2);

        startButton.setOnClickListener(this);
        startButton1.setOnClickListener(this);
        startButton2.setOnClickListener(this);

        progressBar = findViewById(R.id.progressBar);

        listView = findViewById(R.id.list);

        s1Button = (Button) findViewById(R.id.screen1Button);
        s2Button = (Button) findViewById(R.id.screen2Button);
        s1Button.setOnClickListener(this);
        s2Button.setOnClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);

        // Add a marker in Sydney and move the camera
        // LatLng sydney = new LatLng(-34, 151);
        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        // Traffic Scotland Planned Roadworks XML link
        String roadWorks = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
        String plannedRoadWorks = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
        String currentIncidents = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";

        switch (view.getId()) {
            case R.id.startButton:
                doATaskAsynchronously(roadWorks);

                startButton.setTextColor(Color.YELLOW);
                startButton1.setTextColor(Color.WHITE);
                startButton2.setTextColor(Color.WHITE);
                break;
            case R.id.startButton1:
                doATaskAsynchronously(plannedRoadWorks);

                startButton.setTextColor(Color.WHITE);
                startButton1.setTextColor(Color.YELLOW);
                startButton2.setTextColor(Color.WHITE);
                break;
            case R.id.startButton2:
                doATaskAsynchronously(currentIncidents);

                startButton.setTextColor(Color.WHITE);
                startButton1.setTextColor(Color.WHITE);
                startButton2.setTextColor(Color.YELLOW);
                break;
            case R.id.screen1Button:
                avw.showNext();

                /* if (items.size() > 0) {
                    LatLng loc = new LatLng(0,0);
                    ArrayList<MarkerOptions> markers = new ArrayList<MarkerOptions>();
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).isDisplay()) {
                            loc = new LatLng(items.get(i).getLatitude(), items.get(i).getLongitude());
                            markers.add(new MarkerOptions().position(loc).title("Marker in " + items.get(i)));
                            mMap.addMarker(markers.get(i));
                            builder.include(markers.get(i).getPosition());
                        }
                    }

                    LatLngBounds bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100, 100, 0);

                    mMap.moveCamera(cu);
                } */
                break;
            case R.id.screen2Button:
                avw.showPrevious();
                break;
            case R.id.btClearDate:
                Message message = new Message();
                message.what = MESSAGE_UPDATE_DATE;
                message.obj = "Date";
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);

                for(int i = 0; i < items.size(); i++) {
                    items.get(i).setDisplay(true);
                }

                // Set message type.
                message = new Message();
                message.what = MESSAGE_UPDATE_LIST;
                message.obj = items;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);
                break;
            case R.id.btPickDate:
                com.example.alcantara_javier_s1934603_android.DatePicker mDatePickerDialogFragment;
                mDatePickerDialogFragment = new com.example.alcantara_javier_s1934603_android.DatePicker();
                mDatePickerDialogFragment.show(getSupportFragmentManager(), "DATE PICK");
                break;
            default:
                break;
        }

    }

    /* Create Handler object in main thread. */
    private void createUpdateUiHandler()
    {
        if(updateUIHandler == null)
        {
            updateUIHandler = new Handler(Looper.getMainLooper())
            {
                @Override
                public void handleMessage(Message msg)
                {
                    // Means the message is sent from child thread.
                    if (msg.what == MESSAGE_UPDATE_LIST){
                        iAdapter = new ItemAdapter(MapsActivity.this, (ArrayList<Item>) msg.obj);
                        listView.setAdapter(iAdapter);
                    } else
                    if (msg.what == MESSAGE_UPDATE_PERCENTAGE) {
                        progressBar.setProgress((int) Math.round((Double) msg.obj));
                    } else {
                        tvDate.setText((String) msg.obj);
                    }
                }
            };
        }
    }

    private void parseData(String dataToParse)
    {
        Item item = new Item();
        try
        {
            String text = "";
            SimpleDateFormat parser = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", Locale.UK);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader(result));
            int eventType = xpp.getEventType();

            // Clean item list first
            if (items.size() > 0) {
                items.clear();
            }

            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                String tagName = xpp.getName();

                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase("item"))
                        {
                            item = new Item();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagName.equalsIgnoreCase("item"))
                        {
                            items.add(item);
                        }
                        else if (tagName.equalsIgnoreCase("title"))
                        {
                            item.setTitle(text);
                        }
                        else if (tagName.equalsIgnoreCase("description"))
                        {
                            item.setDescription(text);
                        }
                        else if (tagName.equalsIgnoreCase("link"))
                        {
                            item.setLink(new URL(text));
                        }
                        else if (tagName.equalsIgnoreCase("point"))
                        {
                            String[] values = text.split(" ");
                            item.setLatitude(Float.parseFloat(values[0]));
                            item.setLongitude(Float.parseFloat(values[1]));

                        }
                        else if (tagName.equalsIgnoreCase("pubDate"))
                        {
                            item.setDate(parser.parse(text));
                        }
                        break;
                    default:
                        break;
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | ParseException | IOException e) {
            e.printStackTrace();
        }

        Log.e("MyTag","End document");

    }

    private void doATaskAsynchronously(String url)
    {
        // Code here will create a separate Thread to do some
        // work that may be time consuming
        Thread workerThread = new Thread()
        {
            @Override
            public void run()
            {
                URL aurl;
                URLConnection yc;
                BufferedReader in = null;
                String inputLine = "";
                double percent = 0.0;
                long readLength = 0;
                Message message = new Message();


                Log.e("MyTag","in run");

                try
                {
                    Log.e("MyTag","in try");
                    aurl = new URL(url);
                    yc = aurl.openConnection();
                    percent = 100.0 / yc.getContentLength();
                    in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                    Log.e("MyTag","after ready");

                    // Now read the data. Make sure that there are no specific hedrs
                    // in the data file that you need to ignore.
                    // The useful data that you need is in each of the item entries
                    result = "";
                    while ((inputLine = in.readLine()) != null)
                    {
                        result = result + inputLine;
                        readLength += inputLine.length();

                        long finalReadLength = readLength;

                        // Set message type.
                        message = new Message();
                        message.what = MESSAGE_UPDATE_PERCENTAGE;
                        message.obj = percent * finalReadLength;
                        // Send message to main thread Handler.
                        updateUIHandler.sendMessage(message);
                    }
                    in.close();

                    parseData(result);

                    message = new Message();
                    message.what = MESSAGE_UPDATE_PERCENTAGE;
                    message.obj = 100.0;
                    // Send message to main thread Handler.
                    updateUIHandler.sendMessage(message);


                }
                catch (IOException io)
                {
                    Log.e("MyTag", "ioexception in run");
                }


                // Set message type.
                message = new Message();
                message.what = MESSAGE_UPDATE_LIST;
                message.obj = items;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);
            }
        };
        workerThread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar mCalendar = Calendar.getInstance();
        Message message = new Message();
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.getTime());

        message.what = MESSAGE_UPDATE_DATE;
        message.obj = selectedDate;
        // Send message to main thread Handler.
        updateUIHandler.sendMessage(message);

        for(int i = 0; i < items.size(); i++) {
            ArrayList<Integer> itemYearMonthDay = items.get(i).getYearMonthDay();
            items.get(i).setDisplay(itemYearMonthDay.get(0) == year && itemYearMonthDay.get(1) == month && itemYearMonthDay.get(2) == dayOfMonth);
        }

        ArrayList<Item> aux = (ArrayList<Item>) items.stream().filter(Item::isDisplay).collect(Collectors.toList());

        message = new Message();
        message.what = MESSAGE_UPDATE_LIST;
        message.obj = aux;
        // Send message to main thread Handler.
        updateUIHandler.sendMessage(message);

    }
}