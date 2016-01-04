package com.platypus.android.tablet;
//code load waypoitns from file
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.CoordinateRegion;
import com.mapbox.mapboxsdk.geometry.CoordinateSpan;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineMapDatabase;
import com.mapbox.mapboxsdk.offline.OfflineMapDownloader;
import com.mapbox.mapboxsdk.offline.OfflineMapDownloaderListener;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.TilesOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.*;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

import android.app.AlertDialog;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;

import android.view.MotionEvent;
import android.view.View;
import android.graphics.Matrix;


import com.platypus.crw.CrwNetworkUtils;
import com.platypus.crw.SensorListener;
import com.platypus.crw.VehicleServer;
import com.platypus.crw.data.SensorData;
import robotutils.Pose3D;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ProgressBar;

import java.util.Map;

import com.platypus.crw.FunctionObserver;
import com.platypus.crw.ImageListener;
import com.platypus.crw.PoseListener;
import com.platypus.crw.VehicleServer.WaypointState;
import com.platypus.crw.VelocityListener;
import com.platypus.crw.WaypointListener;
import com.platypus.crw.data.Twist;
import com.platypus.crw.data.Utm;
import com.platypus.crw.data.UtmPose;

import android.app.Dialog;

import android.view.View.OnClickListener;
import com.platypus.android.tablet.Joystick.*;


public class TeleOpPanel extends Activity implements SensorEventListener {
    final Context context = this;
    SeekBar thrust = null;
    SeekBar rudder = null;
    TextView ipAddressBox = null;
    TextView thrustProgress = null;
    TextView rudderProgress = null;
    RelativeLayout linlay = null;
    CheckBox autonomous = null;
    Button mapButton = null;
    static TextView testIP = null;
    AsyncTask networkThread;
    TextView test = null;
    ToggleButton tiltButton = null;
    ToggleButton waypointButton = null;


    Button deleteWaypoint = null;
    Button connectButton = null;
    Button saveMap = null;
    Button loadMap = null;
    Button removeMap = null;
    Button refreshMap = null;
    Button route = null;
	Button saveWaypoints;
    Button loadWaypoints;
	
    //TextView log = null;
    Handler network = new Handler();
    ImageView cameraStream = null;
    Button loadWPFile = null;

    TextView sensorData1 = null;
    TextView sensorData2 = null;
    TextView sensorData3 = null;

    TextView sensorType1 = null;
    TextView sensorType2 = null;
    TextView sensorType3 = null;
    TextView battery = null;

    ToggleButton sensorvalueButton = null;
    JoystickView joystick;
    ProgressBar progressBar;



    boolean checktest;
    int a = 0;

    double xValue;
    double yValue;
    double zValue;
    LatLong latlongloc;
    LatLng boatLocation;


    MapView mv;
    String zone;
    String rotation;

    TextView loca = null;
    //Marker boat;
    Marker boat2;
    Marker compass;

    int currentselected = -1; //which element selected
	String saveName; //shouldnt be here?
    LatLng pHollowStartingPoint = new LatLng((float) 40.436871,
            (float) -79.948825);
    LatLng UCMerced = new LatLng((float)37.400732,(float) -120.487372);
    LatLng Mapcenter ;
    long lastTime = -1;
    double lat = 10;
    double lon = 10;
    String waypointStatus = "";
    Handler handlerRudder = new Handler();
    int thrustCurrent;
    int rudderCurrent;
    double heading = Math.PI / 2.;
    double rudderTemp = 0;
    double thrustTemp = 0;
    double old_rudder=0;
    double old_thrust=0;
    double temp;
    double rot;
    String boatwaypoint;
    double tempThrustValue = 0; //used for abs value of thrust
    Twist twist = new Twist();

    float tempX = 0;
    float tempY = 0;

    Bitmap currentImage = null;
    boolean isAutonomous;
    boolean isCurrentWaypointDone = true;

    SensorManager senSensorManager;
    Sensor senAccelerometer;
    public boolean stopWaypoints = true;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    public static final double THRUST_MIN = -1.0;
    public static final double THRUST_MAX = 1.0;
    public static final double RUDDER_MIN = 1.0;
    public static final double RUDDER_MAX = -1.0;

    public EditText ipAddress = null;
    public EditText color = null;
    public RadioButton actualBoat = null;
    public RadioButton simulation = null;
    public Button startWaypoints = null;

    public RadioButton direct = null;
    public RadioButton reg = null;

    public Button submitButton = null;
    public static RadioGroup simvsact = null;
    public static String textIpAddress;
    public static boolean simul = false;
    public static boolean actual;
    public static Boat currentBoat;
    public static InetSocketAddress address;
    public CheckBox autoBox;
    private final Object _waypointLock = new Object(); //deadlock?!??
    boolean failedwp = true;

    public int wpcount = 0;
    public String wpstirng = "";
    public int channel =0;
    public double[] data;
    SensorData Data;
    public String sensorV = "Loading...";
    public static int counter = 0;
    public TextView sensorValueBox;
    boolean dialogClosed = false;
    boolean sensorReady =false;
    public static TextView log;
    public boolean Auto = false;
    private String MapID = "shantanuv.nkob79p0";
    Dialog connectDialog;
    private PoseListener pl;
    private SensorListener sl;
    private int WPnum = 0;
    private boolean longClick = false;
    private long startTime, endTime;




    List<LatLng> waypointList = new ArrayList<LatLng>();
    List<Marker> markerList = new ArrayList(); //List of all the
    List<Float>touchList = new ArrayList<Float>();
    //markers on the map
    //corresponding to the
    //given way

    OfflineMapDownloader offlineMapDownloader;
    TilesOverlay offlineMapOverlay;
    LatLng OfflineCenter = null ;
    Projection MapProj;

    private static final String logTag = TeleOpPanel.class.getName();

    protected void onCreate(Bundle savedInstanceState)   {
        super.onCreate(savedInstanceState);

       //this.setContentView(R.layout.tabletlayout_lg8);  // layout for LG GpadF 8
       this.setContentView(R.layout.tabletlayout); // layout for Nexus 10

        ipAddressBox = (TextView) this.findViewById(R.id.printIpAddress);
        //thrust = (SeekBar) this.findViewById(R.id.thrustBar);
        //rudder = (SeekBar) this.findViewById(R.id.rudderBar);
        linlay = (RelativeLayout) this.findViewById(R.id.linlay);
        thrustProgress = (TextView) this.findViewById(R.id.getThrustProgress);
        rudderProgress = (TextView) this.findViewById(R.id.getRudderProgress);
        // test = (TextView) this.findViewById(R.id.test12);
        //tiltButton = (ToggleButton) this.findViewById(R.id.tiltButton);
        waypointButton = (ToggleButton) this.findViewById(R.id.waypointButton);
        deleteWaypoint = (Button) this.findViewById(R.id.waypointDeleteButton);
        connectButton = (Button) this.findViewById(R.id.connectButton);
        log = (TextView) this.findViewById(R.id.log);
       // loadWPFile = (Button)this.findViewById(R.id.loadFileButton);
        autoBox = (CheckBox) this.findViewById(R.id.autonomousBox);
        startWaypoints = (Button) this.findViewById(R.id.waypointStartButton);

        saveWaypoints = (Button) this.findViewById(R.id.savewpbutton);
        loadWaypoints = (Button) this.findViewById(R.id.loadwpbutton);
        sensorData1 = (TextView) this.findViewById(R.id.SValue1);
        sensorData2 = (TextView) this.findViewById(R.id.SValue2);
        sensorData3 = (TextView) this.findViewById(R.id.SValue3);
        sensorType1 = (TextView) this.findViewById(R.id.sensortype1);
        sensorType2 = (TextView) this.findViewById(R.id.sensortype2);
        sensorType3 = (TextView) this.findViewById(R.id.sensortype3);
        sensorvalueButton = (ToggleButton) this.findViewById(R.id.SensorStart);
        sensorvalueButton.setClickable(sensorReady);
        sensorvalueButton.setTextColor(Color.GRAY);
        battery = (TextView)this.findViewById(R.id.batteryVoltage);
		
        saveMap = (Button) this.findViewById(R.id.saveMap);
        loadMap = (Button) this.findViewById(R.id.loadMap);
        removeMap = (Button) this.findViewById(R.id.removeMap);
        refreshMap = (Button) this.findViewById(R.id.refreshMap);
        progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
		
        route = (Button) this.findViewById(R.id.Route);


//**********************************************************************
//  faked sensor data
//***********************************************************************
//        sensorData1.setText("6.56");
//        sensorType1.setText("ATLAS_PH \n pH");
//        sensorData2.setText("9.56");
//        sensorType2.setText("ATLAS_DO \n mg/L");
//        sensorData3.setText("305\n19.0");
//        sensorType3.setText("ES2 \nEC(µS/cm)\nTE(°C)");
//        battery.setText("14.566");
     // *****************//
     //      Joystick   //
     // ****************//
        joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setYAxisInverted(false);

//*****************************************************************************
//  Initialize Poselistener
//*****************************************************************************
        pl = new PoseListener() { //gets the location of the boat
            public void receivedPose(UtmPose upwcs) {

                UtmPose _pose = upwcs.clone();
                {
                    xValue = _pose.pose.getX();
                    yValue = _pose.pose.getY();
                    zValue = _pose.pose.getZ();
                    rotation = String.valueOf(Math.PI / 2
                            - _pose.pose.getRotation().toYaw());
                    rot =  Math.PI/2 - _pose.pose.getRotation().toYaw();

                    zone = String.valueOf(_pose.origin.zone);

                    latlongloc = UTM.utmToLatLong(UTM.valueOf(
                                    _pose.origin.zone, 'T', _pose.pose.getX(),
                                    _pose.pose.getY(), SI.METER),
                            ReferenceEllipsoid.WGS84);

                    //Log.i(logTag, "rot:" + rot);
                }
            }
        };

//*******************************************************************************
//  Initialize Sensorlistener
//*******************************************************************************
         sl = new SensorListener() {
            @Override
            public void receivedSensor(SensorData sensorData) {
                Data = sensorData;

                sensorV = Arrays.toString(Data.data);
                sensorV = sensorV.substring(1, sensorV.length()-1);
                sensorReady = true;
                //Log.i("Platypus","Get sensor Data");
            }
        };

//***********************************************************************
// Initialize save and load waypoint buttons
// **********************************************************************

        saveWaypoints.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SaveWaypointsToFile();
                }
                catch(Exception e)
                {}
            }
        });

        loadWaypoints.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LoadWaypointsFromFile();
                }
                catch (Exception e)
                {}
            }
        });


		 
//****************************************************************************
//  Initialize the Boat
// ****************************************************************************
        currentBoat = new Boat(pl, sl);


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
//
//        //cameraStream.setImageResource(R.drawable.streamnotfound);
        //File file = new File("/Android/data/com.example.platypuscontrolapp/cache/mapbox_tiles_cache");
        //final MBTilesLayer mbTilesLayer = new MBTilesLayer(file);
        mv = (MapView) findViewById(R.id.mapview);
        offlineMapDownloader = OfflineMapDownloader.getOfflineMapDownloader(this);
        mv.setDiskCacheEnabled(false);
        //mv.setMapRotationEnabled(true);
        //mv.setMapOrientation(90f);
        mv.setUserLocationEnabled(true);
        compass = new Marker("Compass","",pHollowStartingPoint);
        mv.addMarker(compass);
        MapProj = new Projection(mv);

        // If has Internet connection, using online map, else using offline map

            Thread thread = new Thread() {
                public void run() {
                    if (isInternetAvailable()) {
                        try {

                            mv.setAccessToken("pk.eyJ1Ijoic2hhbnRhbnV2IiwiYSI6ImNpZmZ0Zzd5Mjh4NW9zeG03NGMzNDI4ZGUifQ.QJgnm41kA9Wo3CJU-xZLTA");
                            // AccessToken "pk.eyJ1Ijoic2hhbnRhbnV2IiwiYSI6ImNpZmZ0NTMzZzh4YTJyeWx4azhiZ2toMzUifQ.juDMFHzZybMwwO1C4DUN1A"
                            //mv.setTileSource(new MapboxTileLayer("mapbox.streets"));
                            mv.setTileSource(new MapboxTileLayer(MapID));
                           // mv.loadFromGeoJSONURL("https://www.mapbox.com/studio/styles/zeshengx/cihxtezs800nxaikon74h4v4l/");
                            // mv.setTileSource(mbTilesLayer);

                            mv.setCenter(new ILatLng() {
                                @Override
                                public double getLatitude() {
                                    return pHollowStartingPoint.getLatitude();
                                }

                                @Override
                                public double getLongitude() {

                                    return pHollowStartingPoint.getLongitude();
                                }

                                @Override
                                public double getAltitude() {
                                    return 0;
                                }
                            });

                            mv.setZoom(16);


//

                        }catch(Exception e){
                            System.err.println(e);
                        }
                    }
                    else{
                        if(offlineMapDownloader.getMutableOfflineMapDatabases() != null & offlineMapDownloader.getMutableOfflineMapDatabases().size()>0){

                            loadOfflineMap();

                        }
                        else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "No OfflineMap available, Connect to Internet", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                }
            };
            thread.start();




        connectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentBoat = new Boat(pl, sl);
                connectBox();
            }
        });



        connectBox();

//        loadWPFile.setOnClickListener(
//                new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        try {
//
//                            if(setWaypointsFromFile()==false) {
//                                failedwp = true;
////
//                            }
//                            else
//                            {
//                                failedwp = false;
//                            }
//                        }
//                        catch(Exception e)
//                        {
//
//                        }
//                    }
//                });

//        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//        alertDialog.setTitle("Add Waypoints from File");
//        if (failedwp == true)
//        {
//            alertDialog.setMessage("Waypoint File was in the incorrect formatting. \n No Current Waypoints");
//            waypointList.clear();
//            for (Marker i : markerList) {
//                i.remove();
//            }
//        }
//        else {
//            alertDialog.setMessage("Waypoints Added and Started");
//        }

//        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                alertDialog.dismiss();
//            }
//        });
        //alertDialog.show();
        //actual = true;

        /*
         * This gets called when a boat is connected
         * Note it has to draw the boat somewhere initially until it gets a gps loc so it draws it
         * on PantherHollow lake until it gets a new gps loc and will then update to the current
         * position
         */

        startWaypoints.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(){
                    public void run(){
                        if (currentBoat.isConnected() == true) {
                            System.out.println("called");
                            // System.out.println("waypointList > 0");
                            checktest = true;
                            if (waypointList.size() > 0) {
                                UtmPose tempUtm = convertLatLngUtm(waypointList.get(waypointList.size() - 1));

                                waypointStatus = tempUtm.toString();

                                //System.out.println("wps" + waypointStatus);
                                currentBoat.addWaypoint(tempUtm.pose, tempUtm.origin);
                                UtmPose[] wpPose = new UtmPose[waypointList.size()];
                                synchronized (_waypointLock) {
                                    //wpPose[0] = new UtmPose(tempUtm.pose, tempUtm.origin);
                                    for (int i = 0; i < waypointList.size(); i++) {
                                        wpPose[i] = convertLatLngUtm(waypointList.get(i));
                                    }
                                }

                                checkAndSleepForCmd();
                                currentBoat.returnServer().startWaypoints(wpPose, "POINT_AND_SHOOT", new FunctionObserver<Void>() {
                                    @Override
                                    public void completed(Void aVoid) {
                                        System.out.println("completed");
                                    }

                                    @Override
                                    public void failed(FunctionError functionError) {
                                        isCurrentWaypointDone = false;
                                        System.out.println("asdf");
                                        // = waypointStatus + "\n" + functionError.toString();
                                        // System.out.println(waypointStatus);
                                    }
                                });
                                currentBoat.returnServer().getWaypoints(new FunctionObserver<UtmPose[]>() {
                                    @Override
                                    public void completed(UtmPose[] wps) {
                                        for (UtmPose i : wps) {
                                            System.out.println("wp");
                                            System.out.println(i.toString());
                                        }
                                    }

                                    @Override
                                    public void failed(FunctionError functionError) {
                                        System.out.println("shit");
                                    }
                                });
                            }
                            else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Please Select Waypoints", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }


                    }
                };
                thread.start();
            }
        });

        waypointButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Thread thread = new Thread() {
                    public void run() {
                        if (waypointButton.isChecked()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Use long press to add waypoints", Toast.LENGTH_LONG).show();
                                }
                            });
                            Auto = true;
                        } else {
                            Auto = false;
                        }
                        if (Auto) {
                            currentBoat.returnServer().setAutonomous(true, null);
                        } else {
                            currentBoat.returnServer().setAutonomous(false, null);
                        }

                        currentBoat.returnServer().isAutonomous(new FunctionObserver<Boolean>() {
                            @Override
                            public void completed(Boolean aBoolean) {
                                isAutonomous = aBoolean;
                                Log.i(logTag, "isAutonomous: " + isAutonomous);
                            }

                            @Override
                            public void failed(FunctionError functionError) {

                            }
                        });
                    }
                };
                thread.start();
            }
        });
        // download offline map
        saveMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(offlineMapDownloader.isMapIdAlreadyAnOfflineMapDatabase(MapID)){
                    Toast.makeText(getApplicationContext(), "MapID has already been downloaded.\n" +
                            "Please remove it before trying to download again", Toast.LENGTH_SHORT).show();
                }
               // mv.setDiskCacheEnabled(true);
                Mapcenter = mv.getCenter();
                writeToFile(Mapcenter.toString());
                Thread thread = new Thread(){
                    public void run(){
                        saveOfflineMap(Mapcenter);
                    }
                };
                thread.start();
            }
        });
        // display offline map
        loadMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //loadOfflineMap();

                Thread thread = new Thread(){
                    public void run(){
                        loadOfflineMap();
                    }
                };
                thread.start();
            }
        });
        // switch to online map
        refreshMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                        if(isInternetAvailable()){
                            mv.setAccessToken("pk.eyJ1Ijoic2hhbnRhbnV2IiwiYSI6ImNpZmZ0Zzd5Mjh4NW9zeG03NGMzNDI4ZGUifQ.QJgnm41kA9Wo3CJU-xZLTA");
                            //mv.setTileSource(new MapboxTileLayer("mapbox.streets"));
                            mv.setTileSource(new MapboxTileLayer(MapID));
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Need Internet Connection", Toast.LENGTH_LONG).show();
                        }


            }
        });
        // remove offline map database
        removeMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                offlineMapDownloader.removeOfflineMapDatabaseWithID(MapID);
                Toast.makeText(getApplicationContext(), "Removed OfflineMap", Toast.LENGTH_SHORT).show();
                progressBar.setProgress(0);
                String dir = getFilesDir().getAbsolutePath();
                File file = new File(dir, "Mapcenter.txt");
                file.delete();


            }
        });


        route.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng wp1 = new LatLng(40.43683327334392,-79.94899168610573);
                LatLng wp2 = new LatLng(40.43681285847137,-79.94882136583328);
                LatLng wp3 = new LatLng(40.436801630288834,-79.94869530200958);
                LatLng wp4 = new LatLng(40.436943513548584,-79.94863897562027);
                LatLng wp5 = new LatLng(40.437061919492976,-79.94865104556084);
                LatLng wp6 = new LatLng(40.437094583165084,-79.9487717449665);
                LatLng wp7 = new LatLng(40.43703640098811,-79.94891256093979);
                LatLng wp8 = new LatLng(40.436958645252844,-79.94903887999624);
                LatLng wp9 = new LatLng(40.43691838324968,-79.94905889558552);

                Collections.addAll(waypointList, wp1, wp2, wp3, wp4, wp5, wp6, wp7, wp8, wp9);

                for(WPnum = 1 ; WPnum <= waypointList.size(); WPnum ++){
                    mv.addMarker(new Marker(Integer.toString(WPnum), "", waypointList.get(WPnum-1)));
                }

            }
        });

    }

    private void saveOfflineMap (LatLng Mapcenter){
        mv.setDiskCacheEnabled(true);
        offlineMapDownloader = OfflineMapDownloader.getOfflineMapDownloader(this);
       // mv.setCenter(pHollowStartingPoint);
       // mv.setZoom(17);

        //BoundingBox boundingBox = new BoundingBox(new LatLng(40.435203, -79.951636), new LatLng(40.439345, -79.944796));

        BoundingBox boundingBox = new BoundingBox(new LatLng(Mapcenter.getLatitude() - 0.0015,Mapcenter.getLongitude() - 0.0015), new LatLng(Mapcenter.getLatitude() +0.003,Mapcenter.getLongitude()+0.003));
        CoordinateSpan span = new CoordinateSpan(boundingBox.getLatitudeSpan(), boundingBox.getLongitudeSpan());
        CoordinateRegion coordinateRegion = new CoordinateRegion(Mapcenter, span);
        offlineMapDownloader.beginDownloadingMapID(MapID, coordinateRegion, 17, 20);

        OfflineMapDownloaderListener listener = new OfflineMapDownloaderListener() {
            @Override
            public void stateChanged(OfflineMapDownloader.MBXOfflineMapDownloaderState newState) {
                Log.i(logTag, String.format(MapboxConstants.MAPBOX_LOCALE, "stateChanged to %s", newState));
            }

            @Override
            public void initialCountOfFiles(Integer numberOfFiles) {
                Log.i(logTag, String.format(MapboxConstants.MAPBOX_LOCALE, "File number = %d", numberOfFiles));
            }

            @Override
            public void progressUpdate(final Integer numberOfFilesWritten, final Integer numberOfFilesExcepted) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(progressBar.getVisibility() == View.GONE){
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        progressBar.setMax(numberOfFilesExcepted);
                        progressBar.setProgress(numberOfFilesWritten);

                        if(numberOfFilesExcepted == numberOfFilesWritten){
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }

            @Override
            public void networkConnectivityError(Throwable error) {

            }

            @Override
            public void sqlLiteError(Throwable error) {

            }

            @Override
            public void httpStatusError(Throwable error) {

            }

            @Override
            public void completionOfOfflineDatabaseMap(OfflineMapDatabase offlineMapDatabase) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Finish Saving Map", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });


            }
        };
        offlineMapDownloader.addOfflineMapDownloaderListener(listener);
    }

    private void loadOfflineMap(){
        mv.setDiskCacheEnabled(true);
        ArrayList<OfflineMapDatabase> offlineMapDatabases = offlineMapDownloader.getMutableOfflineMapDatabases();
        if(offlineMapDatabases != null & offlineMapDatabases.size()>0) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Marker marker = new Marker("Test", "", pHollowStartingPoint);
                    String mapcenter = readFromFile();
                    String[] mc  = mapcenter.split(",", 3);
                    double lat = Double.parseDouble(mc[0]);
                    double lon = Double.parseDouble(mc[1]);
                    mv.setCenter(new LatLng(lat, lon));
                    mv.setZoom(17);
                    mv.setTileSource(new MBTilesLayer(getApplicationContext(), "shantanuv.nkob79p0.mblite"));

                    mv.animate();
                    Toast.makeText(getApplicationContext(), "Loading OfflineMap", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "OfflineMap is Unavailable", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("Mapcenter.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    private String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput("Mapcenter.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                bufferedReader.close();

                inputStream.close();

                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
    // This method checks the wifi connection but not Internet access
    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    // This method need to run in another thread except UI thread(main thread)
    public static boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable(context)) {
            try {

                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(logTag, "Error checking internet connection", e);
            }
        } else {
            Log.d(logTag, "No network available!");
        }
        return false;
    }
    // Really Check Internet access
    public Boolean isInternetAvailable() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1    www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            if(reachable){
                Log.i(logTag, "Internet access");
                return reachable;
            }
            else{
                Log.i(logTag, "No Internet access");
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        return false;
    }




    // *******************************
    //  JoystickView listener
    // *******************************
    private JoystickMovedListener _listener = new JoystickMovedListener() {
        @Override
        public void OnMoved(int x, int y) {
            thrustTemp = fromProgressToRange(y, THRUST_MIN, THRUST_MAX);
            rudderTemp = fromProgressToRange(x, RUDDER_MIN,RUDDER_MAX);
            Log.i(logTag,"Y:" + y + "\tX:" + x);
            Log.i(logTag, "Thrust" + thrustTemp + "\t Rudder" + rudderTemp);

        }

        @Override
        public void OnReleased() {

        }

        @Override
        public void OnReturnedToCenter() {

        }
    };

    public void dialogClose()
    {
        if (getBoatType() == true) {
//
//
            //log.append("asdf");

            //waypoint on click listener
            /*
             * if the add waypoint button is pressed and new marker where ever they click
             */


            mv.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        startTime = event.getEventTime();
                        Log.d(logTag,"IN DOWN");
                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP){
                        endTime = event.getEventTime();
                        Log.d(logTag, "IN UP");
                    }


                    if (waypointButton.isChecked() && (endTime - startTime >800)) {
                        longClick = false;
                        //Projection proj = ((MapView) v).getProjection();
                        //Projection proj = new Projection((MapView)v);
                       MapProj = mv.getProjection();
                        Rect RotatedScreen = GeometryMath.getBoundingBoxForRotatedRectangle(MapProj.getIntrinsicScreenRect(),MapProj.getCenterX(),
                                MapProj.getCenterY(), MapProj.getMapOrientation(), null);


//                        LatLng WorldCenter = ((MapView) v). getCenter();
//                        double scale = 1; //Math.pow(2, ((MapView) v).getZoomLevel());
//
//                        PointF ScreenCenter = MapProj.toPixels(WorldCenter, null);
//
//                        //PointF Offset = new PointF((float)(event.getX() / scale), (float)(event.getY() / scale));
//                        //PointF NewScreenCenter = new PointF (ScreenCenter.x - Offset.x, ScreenCenter.y + Offset.y);
//
//                        //ILatLng NewWorldCenter = MapProj.fromPixels(NewScreenCenter.x, NewScreenCenter.y);
//                        ILatLng NewWorldCenter = MapProj.fromPixels(event.getX(), event.getY());
//                        LatLng wpLoc = new LatLng(NewWorldCenter.getLatitude(), NewWorldCenter.getLongitude());


                       // Matrix mRotate = MapProj.getRotationMatrix();
                        if(!touchList.isEmpty() && touchList.get(touchList.size()-2) == event.getX() &&
                                touchList.get(touchList.size()-1) == event.getY()){
                            Log.i(logTag,"multi-touch");
                        }
                        else
                        {
                                touchList.add(event.getX());
                                touchList.add(event.getY());
                                Matrix mRotate = new Matrix();
                           // mRotate = MapProj.getRotationMatrix();
                                mRotate.postRotate(((MapView) v).getMapOrientation());
                                //mRotate.setTranslate(v.getTranslationX(),v.getTranslationY());
                                Matrix invRotate = new Matrix();
                                mRotate.invert(invRotate);
                                float rX, rY, w;
                                float[] array = new float[9];
                                mRotate.getValues(array);
//                                rX = event.getX() * array[0] + event.getY() * array[1] + 1 * array[2];
//                                rY = event.getX() * array[3] + event.getY() * array[4] + 1 * array[5];
//                                w = event.getX() * array[6] + event.getY() * array[7] + 1 * array[8];
                                rX = event.getX();
                                rY = event.getY();
                                //event.transform(invRotate);
                           // ILatLng touchedloc = MapProj.pixelXYToLatLong(RotatedScreen.left + (int)rX + MapProj.getHalfWorldSize(),
                           //         RotatedScreen.top + (int)rY + MapProj.getHalfWorldSize(), MapProj.getZoomLevel());
                                ILatLng touchedloc = MapProj.fromPixels(rX,rY);

//                        LatLng wpLoc = new LatLng(rX, rY);
                                LatLng wpLoc = new LatLng(touchedloc.getLatitude(), touchedloc.getLongitude());
//                                double Lat, Lng;
//                            Lat = wpLoc.getLatitude() * array[0] + wpLoc.getLongitude() * array[1] + 1 * array[2];
//                            Lng = wpLoc.getLatitude() * array[3] + wpLoc.getLongitude() * array[4] + 1 * array[5];
//                            wpLoc = new LatLng(Lat, Lng);
                                WPnum += 1;
                                mv.addMarker(new Marker(Integer.toString(WPnum), "", wpLoc));
                                waypointList.add(wpLoc);
                                wpstirng = wpLoc.toString();
                                //SendEmail();
                                markerList.add(new Marker("", "", wpLoc));

                                Log.i(logTag, "Waypoint " + wpLoc);

                        }

                    }
                    return false;
                }

            });

            /*
             * If they press delete wayponts delete all markers off the map and delete waypoints
             */
            deleteWaypoint.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    // ConnectScreen.boat.cancelWaypoint)
                    stopWaypoints = true;
                    //mv.clear();
                    //mv.removeMarkers(markerList);
                    waypointList.clear();
                    isCurrentWaypointDone = true;

                    try {
                        mv.clear();
                        WPnum = 0;
                        LatLng curLoc = new LatLng(latlongloc.latitudeValue(SI.RADIAN) * 180 / Math.PI, latlongloc.longitudeValue(SI.RADIAN) * 180 / Math.PI);
                        boat2 = new Marker(currentBoat.getIpAddress().toString(), "Boat", curLoc);
                        boat2.setIcon(new Icon(getResources().getDrawable(R.drawable.pointarrow)));
                        boat2.setPoint(curLoc);
                        mv.animate();
                        mv.addMarker(boat2);
                    } catch (Exception e) {
                        //boat2 = new Marker(currentBoat.getIpAddress().toString(), "Boat", new LatLng(pHollowStartingPoint.getLatitude(), pHollowStartingPoint.getLongitude()));
                    }
                    System.out.println("called delete");
                }
            });


            networkThread = new NetworkAsync().execute(); //launch networking asnyc task

        }
        else if (getBoatType() == false) {
            log.append("Simulated Boat");
            ipAddressBox.setText("Simulated Phone");
            simulatedBoat();
        }
        else
        {
            log.append("fail");
        }

        try {
            //boat2 = new Marker(currentBoat.getIpAddress().toString(), "Boat", new LatLng(pHollowStartingPoint.getLatitude(), pHollowStartingPoint.getLongitude()));
            boat2 = new Marker("Boat", "Boat", new LatLng(pHollowStartingPoint.getLatitude(), pHollowStartingPoint.getLongitude()));
            mv.addMarker(boat2);
            mv.setCenter(new ILatLng() {
                @Override
                public double getLatitude() {
                    return pHollowStartingPoint.getLatitude();
                }

                @Override
                public double getLongitude() {
                    return pHollowStartingPoint.getLongitude();
                }

                @Override
                public double getAltitude() {
                    return 0;
                }
            });
        }
        catch(Exception e)
        {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // turns the thrust and rudder off when you pause the activity
       // thrust.setProgress(0);
        //rudder.setProgress(50);
        //networkThread.cancel(true);
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        //Intent intent = new Intent(this, TeleOpPanel.class);
//        //startActivity(intent);
//        if (networkThread.isCancelled()) //figure out how to resume asnyc task?
//        {
//            //    networkThread.execute();
//        }
//    }

    public static boolean validIP(String ip) {
        if (ip == null || ip == "")
            return false;
        ip = ip.trim();
        if ((ip.length() < 6) & (ip.length() > 15))
            return false;

        try {
            Pattern pattern = Pattern
                    .compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

    public void updateVelocity(Boat a) { //taken right from desktop client for updating velocity
        // ConnectScreen.boat.setVelocity(thrust.getProgress(),
        // rudder.getProgress());
        if (a.returnServer() != null) {
            //Twist twist = new Twist();
            twist.dx(thrustTemp >= -1 & thrustTemp <= 1 ? thrustTemp : 0);
            if (Math.abs(rudderTemp - 0) < .05) {
                tempThrustValue = 0;
                twist.drz(fromProgressToRange((int) tempThrustValue, RUDDER_MIN,
                        RUDDER_MAX));

            } else {
                twist.drz(rudderTemp >= -1 & rudderTemp <= 1 ? rudderTemp : 0);
            }
            a.returnServer().setVelocity(twist, null);
        }
    }

    /*
     * Rotate the bitmap
     */
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /*
     * this async task handles all of the networking on the boat since networking has to be done on
     * a different thread and since gui updates have to be updated on the main thread ....
     */

    private class NetworkAsync extends AsyncTask<String, Integer, String> {
        long oldTime = 0;
        long oldTime1 = 0;
        String tester = "done";
        boolean connected = false;
        boolean firstTime = true;


        @Override
        protected void onPreExecute()
        {

        }
        @Override
        protected String doInBackground(String... arg0) {
//
            testWaypointListener();




//
            while (true) { //constantly looping
                if (currentBoat != null) {
                    if (System.currentTimeMillis() % 100 == 0
                            && oldTime != System.currentTimeMillis()) {

                        counter++; // if counter == 10 (1000ms), update sensor value
                        if (currentBoat.isConnected() == true) {
                            connected = true;
                            //sensorReady = true;
                        }
                        if (currentBoat.isConnected() == false) {
                            connected = false;
                        }

                        if (old_thrust != thrustTemp) { //update velocity
                            updateVelocity(currentBoat);
                        }

                        if (old_rudder != rudderTemp) { //update rudder
                            updateVelocity(currentBoat);
                        }

//                    }
                        //make this a method
                        if (stopWaypoints == true) {
                            currentBoat.returnServer().stopWaypoints(null);
                            stopWaypoints = false;
                        }



                        old_thrust = thrustTemp;
                        old_rudder = rudderTemp;
                        oldTime = System.currentTimeMillis();



                        publishProgress();

                    }
//
                }
            }


        }

        @Override
        protected void onProgressUpdate(Integer... result) {

            //cameraStream.setImageBitmap(currentImage);
            try
            {
//                Projection mvproj = mv.getProjection();
//                int size = mvproj.getHalfWorldSize();
//                int right = mvproj.getCenterX();
//                int top = mvproj.getCenterY();
//                ILatLng Icomp = mvproj.fromPixels(2084, 112);
//                LatLng comp = new LatLng(Icomp.getLatitude(),Icomp.getLongitude());
                //compass.setPoint(comp);

                LatLng curLoc = new LatLng(latlongloc.latitudeValue(SI.RADIAN) * 180 / Math.PI, latlongloc.longitudeValue(SI.RADIAN) * 180 / Math.PI);
                float degree = (float)(rot * 180 /Math.PI);  // degree is -90 to 270
                degree = (degree < 0 ? 360 + degree : degree); // degree is 0 to 360

                float bias = mv.getMapOrientation(); // bias is the map orirentation
                // Using bitmap to make marker rotatable.
                Bitmap boatarrow = RotateBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pointarrow), degree + bias);
                Drawable d = new BitmapDrawable(getResources(),boatarrow);
                boat2.setDescription(curLoc.toString());
                boat2.setIcon(new Icon(d));
                boat2.setPoint(curLoc);

                //mvproj.toMapPixels(curLoc,null);

               // boat2.setRelatedObject(mv);
               //Log.i(logTag, "Compass " + comp);

                mv.animate();


            if (firstTime == true) {
                try {
                    mv.getController().setCenter(new ILatLng() {
                        @Override
                        public double getLatitude() {
                            return latlongloc.latitudeValue(SI.RADIAN) * 180 / Math.PI;

                        }

                        @Override
                        public double getLongitude() {
                            return latlongloc.longitudeValue(SI.RADIAN) * 180 / Math.PI;

                        }

                        @Override
                        public double getAltitude() {
                            return 0;
                        }
                    });
                    //mv.addMarker(compass);
                    mv.animate();
                    firstTime = false;

                } catch (Exception e) {
                    firstTime = true; //for false/fake lat long values until the phone gets real location values
                }
            }



        }
            catch(Exception e) {

            }



        if (connected == true) {
            ipAddressBox.setBackgroundColor(Color.GREEN);
        }
        if (connected == false) {
            ipAddressBox.setBackgroundColor(Color.RED);
        }



        if(sensorReady == true) {
            sensorvalueButton.setClickable(sensorReady);
            sensorvalueButton.setTextColor(Color.BLACK);
            sensorvalueButton.setText("Show SensorData");



            if(Data.channel == 4){
                String[] batteries = sensorV.split(",");
                battery.setText(batteries[0]);

            }

            if (sensorvalueButton.isChecked()) {
              //  sensorValueBox.setBackgroundColor(Color.GREEN);

                switch (Data.channel) {
                    case 4:
//                        String[] batteries = sensorV.split(",");
//                        battery.setText(batteries[0]);
                        break;
                    case 1:
                        sensorData1.setText(sensorV);
                        sensorType1.setText(Data.type+"\n"+unit(Data.type));

                        break;
                    case 2:
                        sensorData2.setText(sensorV);
                        sensorType2.setText(Data.type+ "\n"+unit(Data.type));

                        break;
                    case 3:
                        sensorData3.setText(sensorV);
                        sensorType3.setText(Data.type+ "\n"+unit(Data.type));

                        break;
                    case 9:
                        break;
                    default:
                        sensorData1.setText("Waiting");
                        sensorData2.setText("Waiting");
                        sensorData3.setText("Waiting");
                }

            }
            if (!sensorvalueButton.isChecked()) {
                //sensorV = "";
                sensorData1.setText("----");
                sensorData2.setText("----");
                sensorData3.setText("----");
                //sensorValueBox.setBackgroundColor(Color.DKGRAY);
            }
        }
        else{
            sensorvalueButton.setText("Sensor Unavailable");
            sensorData1.setText("----");
            sensorData2.setText("----");
            sensorData3.setText("----");
        }
//********************************//
// Adding Joystick move listener//
// ******************************//
        joystick.setOnJostickMovedListener(_listener);

        DecimalFormat velFormatter = new DecimalFormat("####.###");

        //thrustTemp = fromProgressToRange(thrust.getProgress(), THRUST_MIN, THRUST_MAX);
       // rudderTemp = fromProgressToRange(rudder.getProgress(), RUDDER_MIN, RUDDER_MAX);
        thrustProgress.setText(velFormatter.format(thrustTemp * 100.0) + "%");
        rudderProgress.setText(velFormatter.format(rudderTemp * -100.0) + "%");

        log.setText("Waypoint Status: \n" + boatwaypoint);
        autoBox.setChecked(isAutonomous);



    }
}

    public void simulatedBoat() {
//        boat2 = map.addMarker(new MarkerOptions().anchor(.5f, .5f) //add boat to panther hollow
//                .rotation(270).title("Boat 1")
//                .snippet("IP Address: 192.168.1.1")
//                .position(pHollowStartingPoint).title("Boat 1")
//                .snippet("127.0.0.1 (localhost)")
//                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.airboat))
//                .flat(true));
//
//        lat = pHollowStartingPoint.latitude;
//        lon = pHollowStartingPoint.longitude;
//        map.setMyLocationEnabled(true);
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pHollowStartingPoint,
//                15));
//        map.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
//
//        boat2.setRotation((float) (heading * (180 / Math.PI)));
//        handlerRudder.post(new Runnable() { //control the boat
//            @Override
//            public void run() {
//                if (thrust.getProgress() > 0) {
//                    lat += Math.cos(heading) * (thrust.getProgress() - 50)
//                            * .0000001;
//                    lon += Math.sin(heading) * (thrust.getProgress())
//                            * .0000001;
//                    heading -= (rudder.getProgress() - 50) * .001;
//                    boat2.setRotation((float) (heading * (180 / Math.PI)));
//                }
//                boat2.setPosition(new LatLng(lat, lon));
//                handlerRudder.postDelayed(this, 200);
//            }
//        });
    }
    private String unit(VehicleServer.SensorType Type){
        String unit="";

        if(Type.toString().equalsIgnoreCase("ATLAS_PH")){
            unit = "pH";
        }
        else if(Type.toString().equalsIgnoreCase("ATLAS_DO")){
            unit = "mg/L";
        }
        else if(Type.toString().equalsIgnoreCase("ES2")){
            unit = "EC(µS/cm)\n" +
                    "TE(°C)";
        }
        else if(Type.toString().equalsIgnoreCase("HDS_DEPTH")){
            unit = "m";
        }
        else{
            unit = "";
        }


        return unit;
    }

    public void setVelListener() {
        currentBoat.returnServer().addVelocityListener(
                new VelocityListener() {
                    public void receivedVelocity(Twist twist) {
                        thrust.setProgress(fromRangeToProgress(twist.dx(),
                                THRUST_MIN, THRUST_MAX));
                        rudder.setProgress(fromRangeToProgress(twist.drz(),
                                RUDDER_MIN, RUDDER_MAX));
                    }
                }, null);

    }

    // Converts from progress bar value to linear scaling between min and
// max
    private double fromProgressToRange(int progress, double min, double max) {
        return ((max - min) * ((double) progress) / 20.0);
    }

    // Converts from progress bar value to linear scaling between min and
// max
    private int fromRangeToProgress(double value, double min, double max) {
        return (int) (20.0 * (value ) / (max - min));
    }

    /* accelerometer controls */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

//            if (tiltButton.isChecked()) {
//                if ((curTime - lastUpdate) > 100) {
//                    long diffTime = (curTime - lastUpdate);
//                    lastUpdate = curTime;
//                    float speed = Math
//                            .abs(x + y + z - last_x - last_y - last_z)
//                            / diffTime * 10000;
//
//                    if (speed > SHAKE_THRESHOLD) {
//                    }
//
//                    last_x = y; // rudder switching x and z for tesing orientation
//                    last_y = x;
//                    last_z = z; // thrust
//                    // test.setText("x: " + last_x + "y: " + last_y + "z: "
//                    // + last_z);
//
//                    //updateViaAcceleration(last_x, last_y, last_z);
//                }
//            }
        }
    }

    public void updateViaAcceleration(float xval, float yval, float zval) { //update the thrust via accelerometers
        if (Math.abs(tempX - last_x) > 2.5) {

            if (last_x > 2) {
                thrust.setProgress(thrust.getProgress() - 3);
            }
            if (last_x < 2) {
                thrust.setProgress(thrust.getProgress() + 3);
            }
        }
        if (Math.abs(tempY - last_y) > 1) {
            if (last_y > 2) {
                rudder.setProgress(rudder.getProgress() - 3);
            }
            if (last_y < -2) {
                rudder.setProgress(rudder.getProgress() + 3);
            }
        }
    }

    public void addWayPointFromMap() {
        // when you click you make utm pose... below is fake values
        Pose3D pose = new Pose3D(1, 1, 0, 0.0, 0.0, 10);
        Utm origin = new Utm(17, true);
        // ConnectScreen.boat.addWaypoint(pose, origin);
        UtmPose[] wpPose = new UtmPose[1];
        wpPose[0] = new UtmPose(pose, origin);
        currentBoat.returnServer().startWaypoints(wpPose,
                "POINT_AND_SHOOT", new FunctionObserver<Void>() {
                    public void completed(Void v) {
                        //log.setText("completed"); UNCOMMENT THESE
                    }

                    public void failed(FunctionError fe) {
                        ///log.setText("failed");
                    }
                });

    }

    public LatLng convertUtmLatLng(Pose3D _pose, Utm _origin) {
        LatLong temp = UTM
                .utmToLatLong(
                        UTM.valueOf(_origin.zone, 'T', _pose.getX(),
                                _pose.getY(), SI.METER),
                        ReferenceEllipsoid.WGS84);
        return new LatLng(temp.latitudeValue(SI.RADIAN),
                temp.longitudeValue(SI.RADIAN));
    }

    public UtmPose convertLatLngUtm(ILatLng point) {

        UTM utmLoc = UTM.latLongToUtm(LatLong.valueOf(point.getLatitude(),
                point.getLongitude(), NonSI.DEGREE_ANGLE), ReferenceEllipsoid.WGS84);

        // Convert to UTM data structure
        Pose3D pose = new Pose3D(utmLoc.eastingValue(SI.METER), utmLoc.northingValue(SI.METER), 0.0, 0, 0, 0);
        Utm origin = new Utm(utmLoc.longitudeZone(), utmLoc.latitudeZone() > 'O');
        UtmPose utm = new UtmPose(pose, origin);
        return utm;
    }


    public void testCamera() {
        //log.setText("test camera");
        currentBoat.returnServer().addImageListener(new ImageListener() {
            public void receivedImage(byte[] imageData) {
                // Take a picture, and put the resulting image into the panel
                //log.setText("image taken");

                try {
                    Bitmap image1 = BitmapFactory.decodeByteArray(imageData, 0, 15);
                    if (image1 != null) {
                        // a++;
                        //System.out.println("image made");
                        currentImage = image1;

                    }
                } catch (Exception e) {
                    //log.setText(e.toString()); uncomment this
                    e.printStackTrace();
                }
            }
        }, null);
    }

    public void connectBox()
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.connectdialog);
        ipAddress = (EditText) dialog.findViewById(R.id.ipAddress1);

        Button submitButton = (Button) dialog.findViewById(R.id.submit);
        simvsact = (RadioGroup) dialog.findViewById(R.id.simvsactual);
        actualBoat = (RadioButton) dialog.findViewById(R.id.actualBoatRadio);
        simulation = (RadioButton) dialog.findViewById(R.id.simulationRadio);

        direct = (RadioButton) dialog.findViewById(R.id.wifi);
        reg = (RadioButton) dialog.findViewById(R.id.reg);
        ipAddress.setText("192.168.1.20");

        direct.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(direct.isChecked()){
                    ipAddress.setText("192.168.1.20");
                }
            }
        });
        reg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reg.isChecked())
                {
                    ipAddress.setText("tunnel.senseplatypus.com");
                }
                else
                {
                    ipAddress.setText("192.168.1.20");
                }
            }
        });
        if (ipAddress.getText() == null || ipAddress.getText().equals("") || ipAddress.getText().length()==0)
        {
            ipAddressBox.setText("IP Address: 127.0.0.1 (localhost)");
        }
        else {
            ipAddressBox.setText("IP Address: " + ipAddress.getText());
        }


        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // int selectedId = simvsact.getCheckedRadioButtonId();
                //int selectedOption = actvsim.getCheckedRadioButtonId();
                //log.append("asdf" + selectedOption);
//                if (boat2 != null) {
                    //boat2.remove();
//                }
                markerList = new ArrayList<Marker>();
                actual = actualBoat.isChecked();

                textIpAddress = ipAddress.getText().toString();
                System.out.println("asdfasdfasdf");
                System.out.println("IP Address entered is: " + textIpAddress);
                if (direct.isChecked()) {
                    if (ipAddress.getText() == null || ipAddress.getText().equals("")) {
                        address = CrwNetworkUtils.toInetSocketAddress("127.0.0.1" + ":11411");
                    }
                    address = CrwNetworkUtils.toInetSocketAddress(textIpAddress + ":11411");
                   // address = CrwNetworkUtils.toInetSocketAddress(textIpAddress + ":6077");
//                    log.append("\n" + address.toString());
                    //currentBoat = new Boat(address);
                    currentBoat.setAddress(address);
                }
                else if(reg.isChecked())
                {
                    Log.i(logTag,"finding ip");
                    FindIP();
                }
                mv.clear();
                dialog.dismiss();
                dialogClose();
            }
        });

        dialog.show();

    }

    public static InetSocketAddress getAddress()
    {
        return address;
    }
    public static String getIpAddress() {
        return textIpAddress;
    }

    public static boolean getBoatType() {
        return actual;
    }
    public void waypointListenerTest()
    {
        currentBoat.returnServer().addWaypointListener(new WaypointListener() {
            @Override
            public void waypointUpdate(WaypointState waypointState) {
                System.out.println("waypontstate: " + waypointState.toString());
            }
        },null);
    }
    public void testWaypointListener()
    {
        //this gets called on doInBackground() in the async task
        currentBoat.returnServer().addWaypointListener(new WaypointListener() {
            public void waypointUpdate(WaypointState ws) {
                boatwaypoint = ws.toString();
//                currentBoat.returnServer().isAutonomous(new FunctionObserver<Boolean>() {
//                    @Override
//                    public void completed(Boolean aBoolean) {
//                        isAutonomous = aBoolean;
//                        Log.i(logTag, "isAutonomous: "+ isAutonomous);
//                    }
//
//                    @Override
//                    public void failed(FunctionError functionError) {
//
//                    }
                //});
                //System.out.println(boatwaypoint);
            }
        }, null);
    }
    private void checkAndSleepForCmd() {
        if (lastTime >= 0) {
            long timeGap = 1000 - (System.currentTimeMillis() - lastTime);
            if (timeGap > 0) {
                try {
                    Thread.sleep(timeGap);
                } catch (InterruptedException ex) {
                }
            }
        }
        lastTime = System.currentTimeMillis();
    }
    public void fromFiletoWPList() throws IOException
    {
        //code for opening window for meantime have tmep folder with one file it accepts for wp list
        File readFile = new File("");
        Scanner fileReader = new Scanner(readFile);
        //set delimeter
        //parse text into latlong
        //waypointList.add(fileReader.next());
    }

    /* at the moment does not validate files! make sure your waypoint file is correctly matched this will be implemented later..*/
    public boolean setWaypointsFromFile() throws IOException {
        File wpFile = null;
        try {
            wpFile = new File("./waypoints.txt");
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
        Scanner fileScanner;
        int valueCounter = 0;
        //first make sure even number of elements

        if (wpFile.exists()) {
            fileScanner = new Scanner(wpFile);
            //first make sure even number of element
            while(fileScanner.hasNext())
            {
                try
                {
                    LatLng temp = new LatLng(Double.parseDouble(fileScanner.next()), Double.parseDouble(fileScanner.next()));
                    waypointList.add(temp);
                    Marker tempMarker = mv.addMarker(new Marker("","",temp));
                    markerList.add(tempMarker);
                }
                catch(Exception e)
                {
                    System.out.println("Invalid LAT/LNG in file");
                }
                System.out.println(fileScanner.next() + " " + fileScanner.next());
                valueCounter+=2;
            }
            System.out.println("amount of elements: " + valueCounter);
            if ((valueCounter % 2) != 0)
            {
                System.out.println("Mismatching lat long vals");
                return false;
            }
            else
            {
                System.out.println("Valid");
            }
        } else
        {
            System.out.println("File not found");
        }
        return true;
    }

//    public void FindIP() {
//
//

//        address = CrwNetworkUtils.toInetSocketAddress(textIpAddress + ":6077");
//
//        Thread thread = new Thread() {
//            public void run() {
//
//                currentBoat = new Boat();
//                UdpVehicleServer tempserver = new UdpVehicleServer();
//                currentBoat.returnServer().setRegistryService(address);
//                currentBoat.returnServer().getVehicleServices(new FunctionObserver<Map<SocketAddress, String>>() {
//                    @Override
//                    public void completed(Map<SocketAddress, String> socketAddressStringMap) {
//                        System.out.println("Completed");
//                        for (Map.Entry<SocketAddress, String> entry : socketAddressStringMap.entrySet()) {
//
//
//                            //newaddressstring = entry.getKey().toString();
//                            //System.out.println(newaddressstring);
//                            currentBoat.returnServer().setVehicleService(entry.getKey());
//
//                            System.out.println(entry.getKey().toString());
//                            System.out.println(entry.getValue().toString());
//
//                        }
//                    }
//
//                    @Override
//                    public void failed(FunctionError functionError) {
//                        System.out.println("No Response");
//                    }
//                });
//                //currentBoat = new Boat(CrwNetworkUtils.toInetSocketAddress(newaddressstring));
//                //System.out.println("Boat address" + currentBoat.getIpAddress());
//                // regcheck.show();
//                //}
//            }
//        };
//
//        thread.start();
//        //System.out.println("print here: " + newaddressstring);
//        //currentBoat = new Boat(CrwNetworkUtils.toInetSocketAddress(newaddressstring));
//
//    }
public void FindIP() {




    Thread thread = new Thread() {

        public void run() {
            address = CrwNetworkUtils.toInetSocketAddress(textIpAddress + ":6077");
            //address = CrwNetworkUtils.toInetSocketAddress(textIpAddress);
            //currentBoat = new Boat(pl,null);
           // currentBoat = new Boat();
           // UdpVehicleServer tempserver = new UdpVehicleServer();
            currentBoat.returnServer().setRegistryService(address);
            currentBoat.returnServer().getVehicleServices(new FunctionObserver<Map<SocketAddress, String>>() {
                @Override
                public void completed(Map<SocketAddress, String> socketAddressStringMap) {
                    Log.i(logTag, "Completed");
                    for (Map.Entry<SocketAddress, String> entry : socketAddressStringMap.entrySet()) {
                        //newaddressstring = entry.getKey().toString();
                        //System.out.println(newaddressstring);
                        // currentBoat.returnServer().setVehicleService(entry.getKey());
//                        adapter.add(entry);
//                        adapter.notifyDataSetChanged();

                        Log.i(logTag, entry.toString());
                        currentBoat.returnServer().setVehicleService(entry.getKey());


//

                    }
//                    if(currentBoat.isConnected() == true){
//                        Log.i(logTag, "Connected");
//                    }
//                    else{
//                        Log.i(logTag, "Disconnected");
//                    }
                }

                @Override
                public void failed(FunctionError functionError) {
                    Log.i(logTag,"No Response");
                }
            });

        }
    };
    thread.start();

}

    public void SendEmail()
    {
        Thread thread = new Thread() {
            public void run() {
                Email mail = new Email("platypuslocation@gmail.com", "airboats");
                try {
                    //   mail.sendMail("jeffboat", wpstirng, "shantanu@gmail.com", "platypuslocation@gmail.com");
                }
                catch(Exception e)
                {
                    System.out.println(e.toString());
                    //System.out.println("fucked up");
                }

            }
        };
        thread.start();
    }

    public void InitSensorData() {
        while (currentBoat == null) {
        }

        final SensorListener sensorListener = new SensorListener() {
            @Override
            public void receivedSensor(SensorData sensorData) {

                Data = sensorData;
                //data = Data.data;
                //channel = Data.channel;
                sensorV = Arrays.toString(Data.data);
                //sensorV = Integer.toString(Data.channel);
//                if(Data.toString()==null){
//                    sensorV = "No sensor value";
//                }
//                else {
//                    sensorV = Data.toString();
//                }
            }
        };

        //currentBoat.returnServer().getNumSensors(new FunctionObserver<Integer>() {
        // @Override
        //  public void completed(Integer numSensors) {
        //    System.out.println("Sensor num:" + numSensors);
        //  for (int channel = 0; channel < numSensors; ++channel) {
        currentBoat.returnServer().addSensorListener(3, sensorListener, new FunctionObserver<Void>() {
            @Override
            public void completed(Void aVoid) {
                System.out.println("Add Sensorlistener");
            }

            @Override
            public void failed(FunctionError functionError) {
                sensorV = "Failed to get sensor value";
            }
        });
    }

    //  Make return button same as home button
        @Override
    public void onBackPressed() {
        //Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

	/*
    * format of waypoint file
    * x x x x x x (first save)
    * x x x x x x (second save) ..etc
    * */
    public void SaveWaypointsToFile() throws IOException
    {
        //nothing to
        // save if no waypoints
        if (waypointList.isEmpty() == true)
        {
            //return;
        }


        final BufferedWriter writer;
        try {
            File waypointFile = new File(getFilesDir() + "/waypoints.txt");
            writer= new BufferedWriter(new FileWriter(waypointFile, true));
        }
        catch(Exception e)
        {
            return;
        }

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.wpsavedialog);
        dialog.setTitle("Save Waypoint Set");
        final EditText input = (EditText) dialog.findViewById(R.id.newname);
        Button submit = (Button) dialog.findViewById(R.id.savebutton);
        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                //NO NUMBERS OR SPACES LOL
                saveName = input.getText().toString();

                if (!(saveName.contains(" ") || saveName.matches(".*\\d+.*"))) {
                    try {
                        writer.append("\n" + input.getText());
                        writer.flush();
                        //writer.append(input.getText());
                        for (ILatLng i : waypointList) {
                            writer.append(" " + i.getLatitude() + " " + i.getLongitude());
                            writer.flush();
                        }
                        //writer.write("\n");

                        writer.close();
                    } catch (Exception e) {
                    }
                    dialog.dismiss();
                }
                else
                {
                    final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("No Numbers or Spaces in Title");
                    alertDialog.show();
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);
                }

            }
        });
        dialog.show();
        File waypointFile = new File(getFilesDir() + "/waypoints.txt");

        //if (waypointFile.exists())
        //{
            //System.out.println("made new file");
    //}

    }
    public void LoadWaypointsFromFile() throws IOException {
        final File waypointFile = new File(getFilesDir() + "/waypoints.txt");
        Scanner fileScanner = new Scanner(waypointFile); //Scans each
        //line of the file
        final ArrayList<ArrayList<ILatLng>> waypointsaves = new ArrayList<ArrayList<ILatLng>>();
        final ArrayList<String> saveName = new ArrayList<String>();
		/* scans each line of the file as a waypoint save
		 * then scans each line every two elements makes a latlng
		 * adds all saves to arraylist
		 * chose between arraylist later on
		 */

        if (waypointFile.exists()) {
            while (fileScanner.hasNext()) {
                final ArrayList<ILatLng> currentSave = new ArrayList<ILatLng>();
                String s = fileScanner.nextLine();
                System.out.println(s);
                final Scanner stringScanner = new Scanner(s);

                //get save name
                if (stringScanner.hasNext()) {
                    String name = stringScanner.next();
                    saveName.add(name);
                }
                while (stringScanner.hasNext()) {
                    // System.out.println(stringScanner.next());
//                    System.out.println(Double.parseDouble(stringScanner.next()) + " " + Double.parseDouble(stringScanner.next()));

                    final double templat = Double.parseDouble(stringScanner.next());
                    final double templon = Double.parseDouble(stringScanner.next());
                    ILatLng temp = new ILatLng() {
                        @Override
                        public double getLatitude() {
                            return templat;

                        }

                        @Override
                        public double getLongitude() {
                            return templon;
                        }

                        @Override
                        public double getAltitude() {
                            return 0;
                        }
                    };

                    currentSave.add(temp);

                }
                if (currentSave.size() > 0) { //make sure no empty arrays (throws offset of wpsaves also why this?!?!)
                    waypointsaves.add(currentSave);
                }
                stringScanner.close();
            }
            fileScanner.close();

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.waypointsavelistview);
            dialog.setTitle("List of Waypoint Saves");
            final ListView wpsaves = (ListView) dialog.findViewById(R.id.waypointlistview);
            Button submitButton = (Button) dialog.findViewById(R.id.submitsave);


            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    TeleOpPanel.this,
                    android.R.layout.select_dialog_singlechoice);
            wpsaves.setAdapter(adapter);
            for (String s : saveName) {
                adapter.add(s);
                adapter.notifyDataSetChanged();
            }
            final int chosensave;
            wpsaves.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    System.out.println("on long click");
                    final Dialog confirmdialog = new Dialog(context);
                    confirmdialog.setContentView(R.layout.confirmdeletewaypoints);
                    confirmdialog.setTitle("Delete This Waypoint Path?");
                    Button deletebutton = (Button) confirmdialog.findViewById(R.id.yesbutton);
                    Button cancel = (Button) confirmdialog.findViewById(R.id.nobutton);
                    deletebutton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //delete line from file

                            //delete object from list since update wont occur until you press load wp again
                            adapter.remove(adapter.getItem(position));
                            try
                            {
                                File inputFile = new File(getFilesDir() + "/waypoints.txt");
                                File tempFile = new File(getFilesDir() + "/tempwaypoints.txt");

                                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                                String lineToRemove = saveName.get(position);
                                String currentLine;

                                while((currentLine = reader.readLine()) != null)
                                {
                                    int index = currentLine.indexOf(' ');
                                    System.out.println(index);
                                    String tempasdf = currentLine;

                                    String trimmedLine = currentLine.trim();
                                    if(trimmedLine.contains(lineToRemove))
                                    {
                                        System.out.println(lineToRemove);
                                        System.out.println("found");
                                        continue;
                                    }
                                    writer.write(currentLine + System.getProperty("line.separator"));
                                }
                                writer.close();
                                reader.close();
                                tempFile.renameTo(inputFile);
                            }
                            catch(Exception e)
                            {
                            }
                            confirmdialog.dismiss();
                        }
                    });
                    cancel.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmdialog.dismiss();
                        }
                    });
                    confirmdialog.show();

                    return false;
                }
            });
            wpsaves.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    currentselected = position;
                }
            });
            submitButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Object checkedItem = wpsaves.getAdapter().getItem(wpsaves.getCheckedItemPosition());
                    //System.out.println(chosensave);

                    if (currentselected == -1) {
                        dialog.dismiss();
                        //write no selected box
                    }
                    waypointList.clear();
                    markerList.clear();
                    //System.out.println(currentselected);
                    for (ArrayList<ILatLng> i : waypointsaves)
                    {
                        System.out.println(i.size());
                    }
                        for (ILatLng i : waypointsaves.get(currentselected)) //tbh not sure why there is a 1 offset but there is
                        {
                            //System.out.println(i.getLatitude() + " " + i.getLongitude());
                            mv.addMarker(new Marker("marker", "Waypoint", new LatLng(i.getLatitude(), i.getLongitude())));
                        }
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

}
//
//class