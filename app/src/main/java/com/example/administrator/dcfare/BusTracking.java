package com.example.administrator.dcfare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.nearby.messages.Message;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


//FIX GPS LOCATIION IN ON CREATE AND ONREQUESTPERMISSIONS

public class BusTracking extends AppCompatActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;

    //used to modify gui within a runnable
    private final Handler mHandler = new Handler();

    //holds are bus info
    public static ArrayList<Bus> buses = new ArrayList<Bus>();
    public static ArrayList<Marker> markers = new ArrayList<Marker>();
    public static LinkedList<Marker> stopMarkers = new LinkedList<Marker>();
    public BusRoute busRoute = new BusRoute();

    //holds a list of all routes
    private HashSet<String> allRoutes = new HashSet<String>();

    //Actionbar
    private Toolbar toolbar;

    //View that allows user to display routes
    private AutoCompleteTextView acTextView;

    private String currentRoute = "10A";
    private Button selectRoute;

    //timertask reset
    private Timer mTimer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_tracking);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        acTextView = (AutoCompleteTextView) findViewById(R.id.acTextView);
        acTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acTextView.showDropDown();
            }
        });


        selectRoute = (Button) findViewById(R.id.selectRoute);

        selectRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashSet <String> set = new HashSet<String>();

                for(Bus b : buses)
                    set.add(b.RouteID);

                if(set.contains(acTextView.getText().toString().toUpperCase())) {
                    Log.d("set","contains");
                    currentRoute = acTextView.getText().toString().toUpperCase();
                    routeUpdateFlag=true;
                    mMap.clear();
                    mTimer.cancel();
                    mTimer.purge();
                    setRepeatingAsyncTask();
                }
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setRepeatingAsyncTask();


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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(38.897676, -77.036483);
        Marker mark = mMap.addMarker(new MarkerOptions().position(sydney).title("The White House"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10));



            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        0);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



final Runnable drawBus = new Runnable(){



        private BitmapDescriptor determineIcon(Bus b)
        {
                if(b.DirectionText.equals("NORTH"))
                    return BitmapDescriptorFactory.fromResource(R.drawable.vehiclenorth);
                else if(b.DirectionText.equals("SOUTH"))
                    return BitmapDescriptorFactory.fromResource(R.drawable.vehiclesouth);
                else if(b.DirectionText.equals("EAST"))
                    return BitmapDescriptorFactory.fromResource(R.drawable.vehicleeast);
                else if(b.DirectionText.equals("WEST"))
                    return BitmapDescriptorFactory.fromResource(R.drawable.vehiclewest);

            return null;
        }

        private void drawBuses()
        {
            for(int i=0;i<markers.size();++i)
                markers.get(i).remove();

            markers.clear();

            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;


            for(Bus b : buses)
            {

                //Determine Icon type
                BitmapDescriptor icon = determineIcon(b);

                allRoutes.add(b.RouteID);

                if(!b.RouteID.equals(currentRoute))
                    continue;

                Log.d("busdirection",b.DirectionNum+ " " + b.DirectionText);
                LatLng latlng = new LatLng(0,0);
                try {
                    latlng = new LatLng(b.Lat, b.Lon);

                }
                catch(NullPointerException err)
                { err.printStackTrace();}
                finally {

                  //  if(bounds.contains(latlng))
                        markers.add(mMap.addMarker(new MarkerOptions().position(latlng).title(b.VehicleID).snippet("Test Snippet").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))));
                }


            }

            if(acTextView.getAdapter()==null ) {
                String[] tmp = allRoutes.toArray(new String[allRoutes.size()]);
                Arrays.sort(tmp);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.acview, tmp);
                acTextView.setAdapter(adapter);
            }
        }

        private void addBusMarkerListener(Marker m)
        {

        }
        public void run(){
         drawBuses();
        }
    };





final Runnable drawRoute = new Runnable(){


    private void drawPoly()
    {
        PolylineOptions option = new PolylineOptions();
        for(int i=0;i<busRoute.direction0.routePoints.size(); ++i)
        {
            option.add(new LatLng(busRoute.direction0.routePoints.get(i).lat,busRoute.direction0.routePoints.get(i).lon));
        }

        PolylineOptions option2 = new PolylineOptions();

        for(int i=0;i<busRoute.direction1.routePoints.size();++i)
        {
            option2.add(new LatLng(busRoute.direction1.routePoints.get(i).lat,busRoute.direction1.routePoints.get(i).lon));

        }
        Log.d("direction1",busRoute.direction1.routePoints.size()+" "+busRoute.direction0.routePoints.size());
        Log.d("stops",busRoute.direction1.stopPoints.size()+" "+busRoute.direction0.stopPoints.size());

        Polyline direction0 =  mMap.addPolyline(option.width(5).color(Color.RED).visible(true));
        Polyline direction1 = mMap.addPolyline(option2.width(5).color(Color.RED).visible(true));



    }

    private void drawStop()
    {
        for(Marker m : stopMarkers)
            m.remove();

       stopMarkers.clear();

        for(StopPoint s : busRoute.direction0.stopPoints)
        {
            LatLng latlng = new LatLng(s.lat,s.lon);
           stopMarkers.add(mMap.addMarker(new MarkerOptions().position(latlng).title(s.name).snippet(s.stopID).icon(BitmapDescriptorFactory.fromResource(R.drawable.stopunknown))));


        }
        for(StopPoint s : busRoute.direction1.stopPoints)
        {
            LatLng latlng = new LatLng(s.lat,s.lon);
            stopMarkers.add(mMap.addMarker(new MarkerOptions().position(latlng).title(s.name).snippet(s.stopID).icon(BitmapDescriptorFactory.fromResource(R.drawable.stopunknown))));

        }

    }



    public void run(){

            drawPoly();
            drawStop();
            busRoute = new BusRoute();



        Log.d("drawRoute","finished");
    }
};



    //Check if route and stops have been drawn yet.
    Boolean routeUpdateFlag=true;

    //Method used to create a repeating asynctask
    private void setRepeatingAsyncTask() {



        final Handler handler = new Handler();
        Timer timer = new Timer("bus",true);

        //allow global timer
        mTimer = timer;
        TimerTask task = new TimerTask() {

            @Override
            public void run() {



                downloadBusInfo();
                handler.post(drawBus);

                if(routeUpdateFlag) {
                    downloadRoutes();
                    handler.post(drawRoute);
                    routeUpdateFlag=false;
                }
            }
        };

        timer.schedule(task, 1000*1, 1000*5);  // modify interval



    }

    private void downloadRoutes()
    {
        try {
            HttpURLConnection httpURLConnection;


            String route = currentRoute;
            URL builder = new URL("https://api.wmata.com/Bus.svc/json/jRouteDetails?RouteID="+route);


            httpURLConnection = (HttpURLConnection) builder.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("api_key", "65e999675a9f4186a08979bf60155dcc");
            httpURLConnection.setRequestProperty("RouteID",route);
            httpURLConnection.connect();

            BufferedReader bis = new BufferedReader( new InputStreamReader(httpURLConnection.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line ="";
            sb.append(bis.readLine());

            JsonReader json = new JsonReader(new StringReader(sb.toString()));

            //Begin document
            json.beginObject();
            while(json.peek()!=JsonToken.END_DOCUMENT) {

                if(json.peek()==JsonToken.NAME)
                {
                    String name = json.nextName();

                    if(name.equals("RouteID"))
                    {
                        busRoute.routeID = json.nextString();

                    }
                    else if(name.equals("Name"))
                    {
                        busRoute.name = json.nextString();


                    }
                    else if(name.equals("Direction0")) {
                        json.beginObject();
                        //Looping through direction for all key value pairs
                        while(json.peek()!=JsonToken.END_OBJECT) {
                            if(json.peek()==JsonToken.NAME) {
                                name = json.nextName();

                                if(name.equals("DirectionNum")){
                                    busRoute.direction0.directionNum = json.nextString();
                                }
                                else if(name.equals("DirectionText")){
                                    busRoute.direction0.directionText = json.nextString();
                                }
                                else if(name.equals("Shape")){
                                    json.beginArray();

                                    while(json.peek()!=JsonToken.END_ARRAY)
                                    {
                                        RoutePoint routePoint = new RoutePoint();
                                        json.beginObject();
                                        while(json.peek()!=JsonToken.END_OBJECT)
                                        {
                                            name = json.nextName();

                                            if(name.equals("Lat")){
                                                routePoint.lat = json.nextDouble();
                                            }
                                            else if(name.equals("Lon")){
                                                routePoint.lon = json.nextDouble();
                                            }
                                            else if(name.equals("SeqNum")){
                                                routePoint.seqNumber = json.nextInt();
                                            }
                                        }
                                        busRoute.direction0.routePoints.add(routePoint);
                                        json.endObject();

                                    }
                                    json.endArray();
                                }
                                else if(name.equals("Stops")){
                                    json.beginArray();
                                    while(json.peek()!=JsonToken.END_ARRAY)
                                    {
                                        StopPoint stopPoint = new StopPoint();
                                        json.beginObject();
                                        while(json.peek()!=JsonToken.END_OBJECT)
                                        {
                                            name = json.nextName();

                                            if(name.equals("Lat")){
                                                stopPoint.lat = json.nextDouble();
                                            }
                                            else if(name.equals("Lon")){
                                                stopPoint.lon = json.nextDouble();
                                            }
                                            else if(name.equals("StopID")){
                                                stopPoint.stopID = json.nextString();
                                            }
                                            else if(name.equals("Name")){
                                                stopPoint.name = json.nextString();
                                            }
                                            else if(name.equals("Routes"))
                                            {
                                                json.beginArray();
                                                while(json.peek()!=JsonToken.END_ARRAY) {
                                                    stopPoint.routes.add(json.nextString());
                                                }
                                                json.endArray();
                                            }
                                        }
                                        busRoute.direction0.stopPoints.add(stopPoint);
                                        json.endObject();

                                    }
                                    json.endArray();
                                }
                                else if(name.equals("TripHeadsign")){
                                    busRoute.direction0.tripHeadSign = json.nextString();
                                }

                            }

                        }
                        json.endObject();

                    }
                    else if(name.equals("Direction1"))
                    {
                        json.beginObject();
                        //Looping through direction for all key value pairs
                        while(json.peek()!=JsonToken.END_OBJECT) {
                            if(json.peek()==JsonToken.NAME) {
                                name = json.nextName();

                                if(name.equals("DirectionNum")){
                                    busRoute.direction1.directionNum = json.nextString();
                                }
                                else if(name.equals("DirectionText")){
                                    busRoute.direction1.directionText = json.nextString();
                                }
                                else if(name.equals("Shape")){
                                    json.beginArray();

                                    while(json.peek()!=JsonToken.END_ARRAY)
                                    {
                                        RoutePoint routePoint = new RoutePoint();
                                        json.beginObject();
                                        while(json.peek()!=JsonToken.END_OBJECT)
                                        {
                                            name = json.nextName();

                                            if(name.equals("Lat")){
                                                routePoint.lat = json.nextDouble();
                                            }
                                            else if(name.equals("Lon")){
                                                routePoint.lon = json.nextDouble();
                                            }
                                            else if(name.equals("SeqNum")){
                                                routePoint.seqNumber = json.nextInt();
                                            }
                                        }
                                        busRoute.direction1.routePoints.add(routePoint);
                                        json.endObject();

                                    }
                                    json.endArray();
                                }
                                else if(name.equals("Stops")){
                                    json.beginArray();

                                    while(json.peek()!=JsonToken.END_ARRAY)
                                    {
                                        StopPoint stopPoint = new StopPoint();
                                        json.beginObject();
                                        while(json.peek()!=JsonToken.END_OBJECT)
                                        {
                                            name = json.nextName();

                                            if(name.equals("Lat")){
                                                stopPoint.lat = json.nextDouble();
                                            }
                                            else if(name.equals("Lon")){
                                                stopPoint.lon = json.nextDouble();
                                            }
                                            else if(name.equals("StopID")){
                                                stopPoint.stopID = json.nextString();
                                            }
                                            else if(name.equals("Name")){
                                                stopPoint.name = json.nextString();
                                            }
                                            else if(name.equals("Routes"))
                                            {
                                                json.beginArray();
                                                while(json.peek()!=JsonToken.END_ARRAY) {
                                                    stopPoint.routes.add(json.nextString());
                                                }
                                                json.endArray();
                                            }
                                        }
                                        busRoute.direction1.stopPoints.add(stopPoint);
                                        json.endObject();

                                    }
                                    json.endArray();
                                }
                                else if(name.equals("TripHeadsign")){
                                    busRoute.direction0.tripHeadSign = json.nextString();
                                }

                            }

                        }
                        json.endObject();

                    }


                }
                //close final object
                if(json.peek()==JsonToken.END_OBJECT)
                    json.endObject();


            }

            bis.close();
            httpURLConnection.disconnect();
            json.close();
        }
        catch(MalformedURLException err){err.printStackTrace();}
        catch(IOException err){err.printStackTrace();}
        Log.d("here","download routes end");
    }

    private void downloadBusInfo(){


        try {
            HttpURLConnection httpURLConnection;
          //  String page ="http://www.brianmerel.me/busPosition.json";
            // URL builder = new URL(page);
            URL builder = new URL("https://api.wmata.com/Bus.svc/json/jBusPositions");

            httpURLConnection = (HttpURLConnection) builder.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("api_key", "65e999675a9f4186a08979bf60155dcc");
            httpURLConnection.connect();

            BufferedReader bis = new BufferedReader( new InputStreamReader(httpURLConnection.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line ="";
            sb.append(bis.readLine());

            JsonReader json = new JsonReader(new StringReader(sb.toString()));
            json.beginObject();
            JsonToken tkn = json.peek();

            int i=0;

            buses.clear();
            while(tkn != JsonToken.END_DOCUMENT)
            {


                if (tkn == JsonToken.NAME)
                    Log.d("json", json.nextName());
                else if(tkn==JsonToken.BEGIN_ARRAY) {
                    json.beginArray();
                    Log.d("json", "began array");

                    while(json.peek()== JsonToken.BEGIN_OBJECT)
                    {
                        Bus bus = new Bus();
                        ++i;
                        json.beginObject();
                        String str = "";
                        while(json.peek()!=JsonToken.END_OBJECT) {
                            // Log.d("here", "Object Loop - Next: " + json.peek());

                            if(json.peek()==JsonToken.NAME) {


                                str = json.nextName();

                                //  Log.d("here", json.peek() + "Key Loop " + str);

                                if(json.peek()==JsonToken.STRING) {

                                    String value = json.nextString();
                                    //  Log.d("here","Value: " + value);

                                    if (str.equals("DateTime")) {
                                        bus.DateTime = value;
                                    }
                                    if (str.equals("DirectionText")) {
                                        bus.DirectionText = value;
                                    }
                                    if (str.equals("RouteID")) {
                                        bus.RouteID = value;
                                    }
                                    if (str.equals("TripEndTime")) {
                                        bus.TripEndTime = value;
                                    }
                                    if (str.equals("TripHeadsign")) {
                                        bus.TripHeadsign = value;
                                    }
                                    if (str.equals("TripID")) {
                                        bus.TripID = value;
                                    }
                                    if (str.equals("TripStartTime")) {
                                        bus.TripStartTime = value;
                                    }
                                    if (str.equals("VehicleID")){
                                        bus.VehicleID = value;

                                    }




                                }
                                else if(json.peek()==JsonToken.NUMBER) {

                                    double value = Double.parseDouble(json.nextString());

                                    if(str.equals("Lat")) {
                                        bus.Lat = value;

                                    }
                                    if(str.equals("Lon")) {
                                        bus.Lon = value;

                                    }
                                    if(str.equals("Deviation")) {
                                        bus.Deviation = value;

                                    }
                                    if (str.equals("DirectionNum")) {
                                        bus.DirectionNum = value;

                                    }

                                    //   Log.d("here","Number Condition");
                                }
                            }



                        }

                        buses.add(bus);
                        json.endObject();
                    }

                }
                else if(json.peek()==JsonToken.END_ARRAY)
                {
                    json.endArray();
                }
                else if(json.peek()==JsonToken.END_OBJECT)
                {
                    json.endObject();
                }
                else
                {
                    break;
                }
                tkn=json.peek();


            }

            Log.d("here","Bus Size: " + buses.size());




            bis.close();
            httpURLConnection.disconnect();
            json.close();



        }
        catch(MalformedURLException err){err.printStackTrace();}
        catch(IOException err){err.printStackTrace();}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



}
