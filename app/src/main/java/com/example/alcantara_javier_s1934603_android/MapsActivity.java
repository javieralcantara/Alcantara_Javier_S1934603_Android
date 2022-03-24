package com.example.alcantara_javier_s1934603_android;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;

import com.example.alcantara_javier_s1934603_android.adapter.ItemAdapter;
import com.example.alcantara_javier_s1934603_android.adapter.ItemAdapter2;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    // Array of Items
    private ArrayList<Item> incidents = new ArrayList<Item>();
    private ArrayList<Item> roadWorks = new ArrayList<Item>();

    private String roadWorksFeed = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    private String plannedRoadWorks = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
    private String currentIncidents = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";

    private Spinner viewSpinner;


    // Google Maps
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    // Fetch type code.
    private final static int ROAD_WORKS =0;
    private final static int INCIDENTS =1;

    // Message type code.
    private final static int MESSAGE_UPDATE_RW_LIST =0;
    private final static int MESSAGE_UPDATE_INC_LIST =1;
    private final static int MESSAGE_UPDATE_PERCENTAGE =2;
    private final static int MESSAGE_UPDATE_DATE =3;
    private final static int MESSAGE_UPDATE_DATE_PLANNER =4;
    private final static int MESSAGE_ADD_MARKER =5;
    private final static int MESSAGE_CLEAR_MARKERS = 6;

    // View Switcher
    private ViewSwitcher avw;
    private ViewSwitcher avw2;

    // Buttons
    private Button btShowMap;
    private Button btHideMap;
    private Button btPickDate;
    private Button btClearDate;
    private Button btPickDatePlanner;

    // Date Picker
    DatePicker mDatePickerDialogFragment;

    // UI Handler
    private Handler updateUIHandler = null;


    // List View and Custom Array Adapter
    private ListView listView;
    private ListView listView2;
    private ItemAdapter iAdapter;
    private ItemAdapter2 iAdapter2;



    // Progress Bar
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Handler.
        createUpdateUiHandler();

        // Find ViewSwitcher
        avw = (ViewSwitcher) findViewById(R.id.vwSwitch);
        avw2 = (ViewSwitcher) findViewById(R.id.vwSwitch2);


        fetchFeed(roadWorksFeed, ROAD_WORKS, false);
        fetchFeed(plannedRoadWorks, ROAD_WORKS, true);
        fetchFeed(currentIncidents, INCIDENTS, false);

        viewSpinner = (Spinner) findViewById(R.id.vSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dataSources, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewSpinner.setAdapter(adapter);
        viewSpinner.setOnItemSelectedListener(this);



        // Find Buttons
        btShowMap = (Button) findViewById(R.id.showMapButton);
        btHideMap = (Button) findViewById(R.id.hideMapButton);
        btPickDate = findViewById(R.id.pickDButton);
        btClearDate = findViewById(R.id.clearDateButton);
        btPickDatePlanner = findViewById(R.id.pickDPlannerButton);

        // Click listeners
        btShowMap.setOnClickListener(this);
        btHideMap.setOnClickListener(this);
        btPickDate.setOnClickListener(this);
        btClearDate.setOnClickListener(this);
        btPickDatePlanner.setOnClickListener(this);

        // Find Progress Bar
        progressBar = findViewById(R.id.progressBar);

        // Find ListView
        listView = findViewById(R.id.listRoadWorks);
        listView2 = findViewById(R.id.listIncidents);

        iAdapter = new ItemAdapter(MapsActivity.this, roadWorks);
        iAdapter2 = new ItemAdapter2(MapsActivity.this, incidents);

        listView.setAdapter(iAdapter);
        listView2.setAdapter(iAdapter2);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just move the camera towards Glasgow.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng glasgow = new LatLng(55.860916, -4.251433);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(glasgow, 6));
    }

    /**
     * Handles different actions taken on all the buttons.
     * A switch statement separates the use cases in a clean and organized way.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        // Traffic Scotland Planned Roadworks XML link
        Message message = new Message();

        switch (view.getId()) {
            case R.id.showMapButton:
                avw.showNext();
                break;
            case R.id.hideMapButton:
                avw.showPrevious();
                message.what = MESSAGE_CLEAR_MARKERS;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);

                message = new Message();
                message.what = MESSAGE_UPDATE_DATE_PLANNER;
                message.obj = "Pick Date";
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);
                break;
            case R.id.clearDateButton:
                message.what = MESSAGE_UPDATE_DATE;
                message.obj = "Pick Date";
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);

                for(int i = 0; i < roadWorks.size(); i++) {
                    roadWorks.get(i).setDisplay(true);
                }

                // Set message type.
                message = new Message();
                message.what = MESSAGE_UPDATE_RW_LIST;
                message.obj = roadWorks;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);

                for(int i = 0; i < incidents.size(); i++) {
                    incidents.get(i).setDisplay(true);
                }

                // Set message type.
                message = new Message();
                message.what = MESSAGE_UPDATE_INC_LIST;
                message.obj = incidents;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);
                break;
            case R.id.pickDButton:
                mDatePickerDialogFragment = new DatePicker();
                mDatePickerDialogFragment.show(getSupportFragmentManager(), "DATE PICK");
                break;
            case R.id.pickDPlannerButton:
                mDatePickerDialogFragment = new DatePicker();
                mDatePickerDialogFragment.show(getSupportFragmentManager(), "JOURNEY PICK");
                break;
            default:
                break;
        }

    }

    /**
     * Create Handler object in main thread.
     * The handleMessage method listens for incoming updates that should change the UI.
     */
    private void createUpdateUiHandler()
    {
        if(updateUIHandler == null)
        {
            updateUIHandler = new Handler(Looper.getMainLooper())
            {
                @Override
                public void handleMessage(Message msg)
                {
                    if (msg.what == MESSAGE_UPDATE_RW_LIST){
                        iAdapter.addAll((ArrayList<Item>) msg.obj);
                        iAdapter.notifyDataSetChanged();
                    } else if (msg.what == MESSAGE_UPDATE_INC_LIST){
                        iAdapter2.addAll((ArrayList<Item>) msg.obj);
                        iAdapter2.notifyDataSetChanged();
                    } else if (msg.what == MESSAGE_UPDATE_PERCENTAGE) {
                        progressBar.setProgress((int) Math.round((Double) msg.obj));
                    } else if (msg.what == MESSAGE_UPDATE_DATE) {
                        btPickDate.setText((String) msg.obj);
                    } else if (msg.what == MESSAGE_UPDATE_DATE_PLANNER) {
                        btPickDatePlanner.setText((String) msg.obj);
                    } else if (msg.what == MESSAGE_ADD_MARKER) {
                        mMap.addMarker((MarkerOptions) msg.obj);
                    } else if (msg.what == MESSAGE_CLEAR_MARKERS) {
                        mMap.clear();
                    }
                }
            };
        }
    }

    /**
     * Parses the data from a XML feed into a Java Class object.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void parseData(String dataToParse, int type)
    {
        Item item = new Item();
        try
        {
            String text = "";
            SimpleDateFormat parser = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", Locale.UK);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader(dataToParse));
            int eventType = xpp.getEventType();

            // Clean item list first
            /* if (type == ROAD_WORKS) {
                roadWorks.clear();
            } else if (type == INCIDENTS) {
                // items.add(item);
                incidents.clear();
            }*/

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
                            if (type == ROAD_WORKS) {
                                // items.add(item);
                                roadWorks.add(item);
                            } else if (type == INCIDENTS) {
                                // items.add(item);
                                incidents.add(item);
                            }
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
            // roadWorks.sort((r1, r2) -> r1.getDate().before(r2.getDate())  ? 1 : 0);
            // incidents.sort((r1, r2) -> r1.getDate().before(r2.getDate())  ? 1 : 0);
        } catch (XmlPullParserException | ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a separate Thread to do some work that may be time consuming.
     */
    private void fetchFeed(String url, int type, boolean showProgress)
    {
        Thread workerThread = new Thread()
        {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run()
            {
                String result = "";
                URL aurl;
                URLConnection yc;
                BufferedReader in = null;
                String inputLine = "";
                double percent = 0.0;
                long readLength = 0;
                Message message = new Message();

                try
                {
                    aurl = new URL(url);
                    yc = aurl.openConnection();
                    percent = 100.0 / yc.getContentLength();
                    in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                    // Now read the data. Make sure that there are no specific hedrs
                    // in the data file that you need to ignore.
                    // The useful data that you need is in each of the item entries
                    result = "";
                    while ((inputLine = in.readLine()) != null)
                    {
                        result = result + inputLine;
                        readLength += inputLine.length();

                        long finalReadLength = readLength;

                        if (showProgress) {
                            // Set message type.
                            message = new Message();
                            message.what = MESSAGE_UPDATE_PERCENTAGE;
                            message.obj = percent * finalReadLength;
                            // Send message to main thread Handler.
                            updateUIHandler.sendMessage(message);
                        }
                    }
                    in.close();

                    parseData(result, type);

                    if (showProgress) {
                        message = new Message();
                        message.what = MESSAGE_UPDATE_PERCENTAGE;
                        message.obj = 100.0;
                        // Send message to main thread Handler.
                        updateUIHandler.sendMessage(message);
                    }


                }
                catch (IOException io)
                {
                    Log.e("MyTag", "ioexception in run");
                }


                // Set message type.
                message = new Message();
                if (type == ROAD_WORKS) {
                    message.what = MESSAGE_UPDATE_RW_LIST;
                    message.obj = roadWorks;
                } else if (type == INCIDENTS) {
                    message.what = MESSAGE_UPDATE_INC_LIST;
                    message.obj = incidents;
                }
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);
            }
        };
        workerThread.start();
    }

    /**
     * When a date is choosing in the DatePicker, this method triggers.
     * Since there are 2 DatePickers, each one has a different tag - it makes it easy
     * to recognize which action to do depending on the tag.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar mCalendar = Calendar.getInstance();
        Message message = new Message();
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String selectedDate = DateFormat.getDateInstance(DateFormat.DEFAULT).format(mCalendar.getTime());

        switch (Objects.requireNonNull(mDatePickerDialogFragment.getTag())) {
            case "DATE PICK":
                ArrayList<Item> aux = new ArrayList<>();
                message.what = MESSAGE_UPDATE_DATE;
                message.obj = selectedDate;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);

                message = new Message();
                if (avw2.getCurrentView() == listView) {
                    aux = roadWorks;
                    message.what = MESSAGE_UPDATE_RW_LIST;

                } else {
                    aux = incidents;
                    message.what = MESSAGE_UPDATE_INC_LIST;
                }

                for(int i = 0; i < aux.size(); i++) {
                    ArrayList<Integer> itemYearMonthDay = aux.get(i).getYearMonthDay();
                    aux.get(i).setDisplay(itemYearMonthDay.get(0) == year && itemYearMonthDay.get(1) == month && itemYearMonthDay.get(2) == dayOfMonth);
                }

                aux = (ArrayList<Item>) aux.stream().filter(Item::isDisplay).collect(Collectors.toList());


                message.obj = aux;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);
                break;
            case "JOURNEY PICK":

                message.what = MESSAGE_CLEAR_MARKERS;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);

                message = new Message();
                message.what = MESSAGE_UPDATE_DATE_PLANNER;
                message.obj = selectedDate;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);

                LatLng loc = new LatLng(0,0);
                ArrayList<MarkerOptions> markers = new ArrayList<MarkerOptions>();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for(int i = 0; i < roadWorks.size(); i++) {
                    ArrayList<Integer> itemYearMonthDay = roadWorks.get(i).getYearMonthDay();
                    if(itemYearMonthDay.get(0) == year && itemYearMonthDay.get(1) == month && itemYearMonthDay.get(2) == dayOfMonth)
                    {
                        loc = new LatLng(roadWorks.get(i).getLatitude(), roadWorks.get(i).getLongitude());
                        markers.add(new MarkerOptions().position(loc).title("Marker in " + roadWorks.get(i)));

                        message = new Message();
                        message.what = MESSAGE_ADD_MARKER;
                        message.obj = markers.get(markers.size()-1);
                        // Send message to main thread Handler.
                        updateUIHandler.sendMessage(message);

                        builder.include(markers.get(markers.size()-1).getPosition());
                    }
                }

                if (markers.size() > 0) {
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150);

                    mMap.animateCamera(cu);
                }


            default:
                break;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String text = (String)viewSpinner.getSelectedItem();
        View currentView = avw2.getCurrentView();

        if (currentView == listView && text.equalsIgnoreCase("incidents")) {
            avw2.showNext();
        } else if (currentView == listView2 && text.equalsIgnoreCase("road works")) {
            avw2.showPrevious();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}