package com.example.alcantara_javier_s1934603_android;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;

import com.example.alcantara_javier_s1934603_android.adapter.RoadWAdapter;
import com.example.alcantara_javier_s1934603_android.adapter.IncidentsAdapter;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

///////////////////////////////////////////////////////////////////////////////
//
// Author:           Javier Alcántara García
// Email:            jalcan200@caledonian.ac.uk
// Student ID:       S1934603
//
///////////////////////////////////////////////////////////////////////////////
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, TextWatcher {

    // Array of roadworks and incidents
    private final ArrayList<Item> incidents = new ArrayList<Item>();
    private final ArrayList<Item> roadWorks = new ArrayList<Item>();

    private final String roadWorksFeed = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    private final String plannedRoadWorks = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
    private final String currentIncidents = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";

    // Spinner
    private Spinner viewSpinner;

    // Google Maps
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    // Fetch type code.
    private final static int ROAD_WORKS =0;
    private final static int INCIDENTS =1;

    // Message type code
    private final static int MESSAGE_INIT_RW_LIST =0;
    private final static int MESSAGE_INIT_INC_LIST =1;
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

    // Edit Text
    private EditText filterList;

    // List View and Custom Array Adapter
    private ListView listView;
    private ListView listView2;
    private RoadWAdapter iAdapter;
    private IncidentsAdapter iAdapter2;

    // Progress Bar
    private ProgressBar progressBar;

    // Timer
    Timer t = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Handler.
        createUpdateUiHandler();

        // Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                roadWorks.clear();
                incidents.clear();

                fetchFeed(roadWorksFeed, ROAD_WORKS, false);
                fetchFeed(plannedRoadWorks, ROAD_WORKS, true);
                fetchFeed(currentIncidents, INCIDENTS, false);
            }

        }, 0, 300000);

        // Find ViewSwitchers
        avw = findViewById(R.id.vwSwitch);
        avw2 = findViewById(R.id.vwSwitch2);

        // Find and Configure Spinner
        viewSpinner = findViewById(R.id.vSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dataSources, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewSpinner.setAdapter(adapter);
        viewSpinner.setOnItemSelectedListener(this);

        // Find and Configure EditText
        filterList = findViewById(R.id.filter);
        filterList.addTextChangedListener(this);

        // Find Buttons
        btShowMap = findViewById(R.id.showMapButton);
        btHideMap = findViewById(R.id.hideMapButton);
        btPickDate = findViewById(R.id.pickDButton);
        btClearDate = findViewById(R.id.clearDateButton);
        btPickDatePlanner = findViewById(R.id.pickDPlannerButton);

        // Configure Buttons
        btShowMap.setOnClickListener(this);
        btHideMap.setOnClickListener(this);
        btPickDate.setOnClickListener(this);
        btClearDate.setOnClickListener(this);
        btPickDatePlanner.setOnClickListener(this);

        // Find Progress Bar
        progressBar = findViewById(R.id.progressBar);

        // Find ListViews
        listView = findViewById(R.id.listRoadWorks);
        listView2 = findViewById(R.id.listIncidents);

        // Configure ListViews
        listView.setOnItemClickListener(this);
        listView2.setOnItemClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
                    if (msg.what == MESSAGE_INIT_RW_LIST){
                        iAdapter = new RoadWAdapter(MapsActivity.this, (ArrayList<Item>) msg.obj);
                        listView.setAdapter(iAdapter);
                    } else if (msg.what == MESSAGE_INIT_INC_LIST){
                        iAdapter2 = new IncidentsAdapter(MapsActivity.this, (ArrayList<Item>) msg.obj);
                        listView2.setAdapter(iAdapter2);
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
     * Parses the data from an XML feed into a Java Class object.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void parseData(String dataToParse, int type)
    {
        Item item = new Item();
        try
        {
            String text = "";
            SimpleDateFormat parser = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", Locale.UK);
            SimpleDateFormat descParser = new SimpleDateFormat("EEEE, d MMMM yyyy - HH:mm", Locale.UK);


            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader(dataToParse));
            int eventType = xpp.getEventType();

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
                                roadWorks.add(item);
                            } else if (type == INCIDENTS) {
                                incidents.add(item);
                            }
                        }
                        else if (tagName.equalsIgnoreCase("title"))
                        {
                            item.setTitle(text);
                        }
                        else if (tagName.equalsIgnoreCase("description"))
                        {
                            String[] split = text.split("<br />");
                            StringBuilder builder = new StringBuilder();

                            for (int i = 0; i < split.length; i++) {
                                builder.append(split[i]).append(i < split.length - 1 ? ", " : " ");
                            }

                            item.setDescription(builder.toString());

                            // Add start and end date if the description has such information
                            if (text.contains("Start Date:") && text.contains("End Date:")) {
                                item.setStartDate(descParser.parse(split[0].substring(12)));
                                item.setEndDate(descParser.parse(split[1].substring(10)));
                            }
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
    }

    /**
     * Creates a separate Thread to do some work that may be time consuming. It fetches an XML feed
     * from a given URL, it then parses the data into a Java Class object and then updates the UI using
     * a Handler.
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
                    message.what = MESSAGE_INIT_RW_LIST;
                    message.obj = roadWorks;
                } else if (type == INCIDENTS) {
                    message.what = MESSAGE_INIT_INC_LIST;
                    message.obj = incidents;
                }

                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);
            }
        };
        workerThread.start();
    }

    /**
     * Helper function that filers roadWorks by Date
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    ArrayList<Item> filterRoadWorksByDate(Date date) {
        for(int i = 0; i < roadWorks.size(); i++) {
            Item currentItem = roadWorks.get(i);
            roadWorks.get(i).setDisplay(date.after(currentItem.getStartDate()) && date.before(currentItem.getEndDate()) && currentItem.getTitle().contains(filterList.getText()));
        }

        return (ArrayList<Item>) roadWorks.stream().filter(Item::isDisplay).collect(Collectors.toList());
    }

    /**
     * Helper function that filers incidents by Date. Since an incident does not
     * have a start and end date, it looks to filter if the selected date matches the
     * publication date of the incident
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    ArrayList<Item> filterIncidentsByDate(int year, int month, int dayOfMonth) {
        for(int i = 0; i < incidents.size(); i++) {
            Item currentItem = incidents.get(i);
            ArrayList<Integer> itemYearMonthDay = currentItem.getYearMonthDay();
            incidents.get(i).setDisplay(itemYearMonthDay.get(0) == year && itemYearMonthDay.get(1) == month && itemYearMonthDay.get(2) == dayOfMonth && currentItem.getTitle().contains(filterList.getText()));
        }

        return (ArrayList<Item>) incidents.stream().filter(Item::isDisplay).collect(Collectors.toList());
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
                message.what = MESSAGE_INIT_RW_LIST;
                message.obj = roadWorks;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);

                for(int i = 0; i < incidents.size(); i++) {
                    incidents.get(i).setDisplay(true);
                }

                // Set message type.
                message = new Message();
                message.what = MESSAGE_INIT_INC_LIST;
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
     * When a date is chosen in the DatePicker, this method triggers.
     * Since there are 2 DatePickers, each one has a different tag - it makes it easy
     * to recognize which action to do depending on the tag.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
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
                message.what = MESSAGE_UPDATE_DATE;
                message.obj = selectedDate;
                // Send message to main thread Handler.
                updateUIHandler.sendMessage(message);

                message = new Message();

                message.obj = filterRoadWorksByDate(mCalendar.getTime());
                message.what = MESSAGE_INIT_RW_LIST;
                updateUIHandler.sendMessage(message);

                message = new Message();

                message.obj = filterIncidentsByDate(year, month, dayOfMonth);
                message.what = MESSAGE_INIT_INC_LIST;
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
                    Item currentItem = roadWorks.get(i);
                    if(mCalendar.getTime().after(currentItem.getStartDate()) && mCalendar.getTime().before(currentItem.getEndDate()))
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

    /**
     * When an item is selected from the spinner, this method is called.
     * In conjunction with the secondary ViewSwitcher, it toggles between both
     * listviews (roadWorks and incidents)
     */
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

    /**
     * This method opens up a dialog that displays information for a roadwork
     */
    private void showRoadWorkDialog(Item item)
    {
        // Custom dialog setup
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.roadwork_dialog);
        dialog.setTitle("Traffic Scotland Data");

        // Set the custom dialog components as a TextView and Button component
        TextView title = dialog.findViewById(R.id.title);
        TextView description = dialog.findViewById(R.id.description);
        TextView additionalInfo = dialog.findViewById(R.id.additionalInfo);

        Button dialogButton = dialog.findViewById(R.id.dialogButtonOK);

        title.setText(item.getTitle());
        description.setText(item.getDescription());
        additionalInfo.setText(String.format("Additional info: %s", item.getLink()));

        dialogButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();

            }
        });

        dialog.show();
    }

    /**
     * This method opens up a dialog that displays information for an incident
     */
    private void showIncidentDialog(Item item)
    {
        // Custom dialog setup
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.incident_dialog);
        dialog.setTitle("Traffic Scotland Data");

        // Set the custom dialog components as a TextView and Button component
        TextView title = dialog.findViewById(R.id.title);
        TextView description = dialog.findViewById(R.id.description);
        TextView date = dialog.findViewById(R.id.date);
        TextView additionalInfo = dialog.findViewById(R.id.additionalInfo);

        Button dialogButton = dialog.findViewById(R.id.dialogButtonOK);

        title.setText(item.getTitle());
        description.setText(item.getDescription());
        date.setText(String.format("Start date: %s", item.getParsedDate()));
        additionalInfo.setText(String.format("Additional info: %s", item.getLink()));

        dialogButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();

            }
        });

        dialog.show();
    }

    /**
     * When an item is selected from a listview, this method is trigger.
     * Since there are 2 ListViews, each one is identified separately (roadWorks and incidents)
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.listRoadWorks:
                showRoadWorkDialog(roadWorks.get(i));
                break;
            case R.id.listIncidents:
                showIncidentDialog(incidents.get(i));
                break;
            default:
                break;
        }
    }

    /**
     * Whenever text is typed into the EditText field, this method is called.
     * It filters both roadWorks and incidents by the inputted text
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Message message = new Message();

        for(int j = 0; j < roadWorks.size(); j++) {
            Item currentItem = roadWorks.get(j);
            roadWorks.get(j).setDisplay(currentItem.getTitle().contains(charSequence));
        }

        message.obj = roadWorks.stream().filter(Item::isDisplay).collect(Collectors.toList());
        message.what = MESSAGE_INIT_RW_LIST;
        updateUIHandler.sendMessage(message);

        message = new Message();

        for(int j = 0; j < incidents.size(); j++) {
            Item currentItem = incidents.get(j);
            incidents.get(j).setDisplay(currentItem.getTitle().contains(charSequence));
        }

        message.obj = incidents.stream().filter(Item::isDisplay).collect(Collectors.toList());
        message.what = MESSAGE_INIT_INC_LIST;
        updateUIHandler.sendMessage(message);
    }



    /**
     * Unused default methods from various implementations
     */

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}