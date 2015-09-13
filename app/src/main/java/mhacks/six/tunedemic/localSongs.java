package mhacks.six.tunedemic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.*;


import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;


public class localSongs extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

    String TAG = "TAG";

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Boolean makingRequests;
    String mLastTime;

    final static String U_KEY = "requesting-location-updates-key";
    final static String L_KEY = "location-key";
    final static String T_KEY = "last-updated-time-string-key";

    private double myLat, myLong;
    MobileServiceClient mClient;


    private MapView mapView;
    private GoogleMap mymap;
    LatLng mylocation;
    View rootview;

    ArrayList<Nodes> localNodes;
    //ArrayList<Marker> nearMarkers;
    HashMap<Marker, Nodes> clickNodes;

    public static localSongs newInstance(int position) {
        localSongs fragment = new localSongs();

        return fragment;
    }

    public localSongs() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // mLatitudeText = (TextView) getActivity().findViewById(R.id.lat);
       // mLongitudeText = (TextView) getActivity().findViewById(R.id.lon);

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        makeLocationRequest();
    }

    protected void makeLocationRequest(){
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void startHandler(){
        if (!makingRequests){
            makingRequests = true;
            startLocRequests();
        }
    }

    public void stopHandler(){
        if (makingRequests){
            makingRequests = false;
            stopLocRequests();
        }
    }
    protected void startLocRequests(){
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest,  this);
    }

    private void updateUI(){
        if(mLastLocation != null){
            myLat = mLastLocation.getLatitude();
            myLong = mLastLocation.getLongitude();

            mylocation = new LatLng(myLat, myLong);
            MainActivity.globalPos = mylocation;

           // Toast.makeText(getActivity().getApplicationContext(), String.valueOf(myLat) + " " + String.valueOf(myLong), Toast.LENGTH_LONG).show();
            setMap();
        }
    }

    private void setMap(){
        try{
            mClient = new MobileServiceClient (
                    "https://tunedemic.azure-mobile.net/",
                    "ceLnwHzMkiIjIHQqCTZSLHjGyfVxPJ90",
                    this.getActivity()
            );
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        MobileServiceTable<Nodes> mNodesTable = mClient.getTable(Nodes.class);
                        MobileServiceList<Nodes> result = mNodesTable.execute().get();
                        if(!result.isEmpty()) {
                            for (Nodes n : result) {
                                float[] resultarray = new float[1];
                                Location.distanceBetween(mylocation.latitude, mylocation.longitude, n.latitude, n.longitude, resultarray);

                                if (resultarray[0] <= n.radius){
                                    localNodes.add(n);
                                }
                                Log.e("MATH", "ERROR: " + resultarray[0] + " " + n.latitude + " " + n.longitude);
                                Log.e("MATH", "MINE: " + mylocation.latitude + " " + mylocation.longitude);
                            }
                        }
                        else{}


                    } catch (Exception exception) {
                        //   Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                  //Toast.makeText(getActivity().getApplicationContext(), Integer.toString(localNodes.size()),Toast.LENGTH_LONG ).show();
                    for (Nodes oneNode : localNodes){
                        LatLng temp = new LatLng(oneNode.latitude, oneNode.longitude);
                        Marker tempM;
                        mymap.addCircle(new CircleOptions()
                                        .center(temp)
                                        .radius(oneNode.radius)
                                        .fillColor(Color.parseColor("#107F3F97"))
                                        .strokeColor(Color.parseColor("#7F3F97"))
                        );
                        tempM = mymap.addMarker(new MarkerOptions()
                                        .position(temp)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin32))
                        );

                        clickNodes.put(tempM, oneNode);
                        //Toast.makeText(getActivity().getApplicationContext(), Float.toString(oneNode.radius), Toast.LENGTH_LONG).show();
                    }


                    mymap.setMyLocationEnabled(true);
                    mymap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            reactMarker(marker);
                            return false;
                        }
                    });


                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mylocation, 17);
                    mymap.animateCamera(cameraUpdate);

                }
            }.execute();

        }
        catch(MalformedURLException murl){
            Log.e("TAG", "Malformed URL");
        }
    }

//    @Override
//    public boolean onMarkerClick(final Marker marker){
//        reactMarker(marker);
//        return true;
//    }

    public void reactMarker(final Marker marker){

        //Toast.makeText(getActivity().getApplicationContext(), "Welp", Toast.LENGTH_LONG).show();

        new AsyncTask<Void, Void, Void>() {
            String title = "", artist = "";
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceTable<Songs> mSongsTable;
                    mSongsTable = mClient.getTable(Songs.class);
                    Nodes str = clickNodes.get(marker);
                    MobileServiceList<Songs> result = mSongsTable.where().field("url").eq(str.url).execute().get();
                    for (Songs s : result){
                        title = s.name;
                        artist = s.artist;
                    }
                } catch (Exception exception) {
                    Log.e("NODES", "THERE WAS A PROBLEM");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage("Go to song \"" + title + "\" by " + artist + "?");
                builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Fragment myFrag = new songFragment();

                        Bundle args = new Bundle();
                        args.putString("nodeID", clickNodes.get(marker).id);
                        myFrag.setArguments(args);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, myFrag)
                                .commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                //Toast.makeText(getActivity().getApplicationContext(), "Hellerrrr", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    protected void stopLocRequests(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        mapView.onResume();
       // mLatitudeText = (TextView) getActivity().findViewById(R.id.lat);
       // mLongitudeText = (TextView) getActivity().findViewById(R.id.lon);
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && makingRequests){
            startLocRequests();
        }
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocRequests();
        }
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
        mapView.onDestroy();
    }

//    @Override
//    public void onLowMemory(){
//        super.onLowMemory();
//        mapView.onLowMemory();
//    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if (mLastLocation == null) {
//            //Toast.makeText(getActivity().getApplicationContext(), String.valueOf(mLastLocation.getLatitude()), Toast.LENGTH_LONG).show();
//            //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
//            //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
//            myLat = mLastLocation.getLatitude();
//            myLong = mLastLocation.getLongitude();
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //lastTime
            updateUI();
        }

        if (makingRequests){
            startLocRequests();
        }
//  else {
//            Toast.makeText(this.getActivity(), "OH NO", Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public void onLocationChanged(Location location){
        mLastLocation = location;
        if (location.getAccuracy() <= 50){
            updateUI();
            stopHandler();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean(U_KEY, makingRequests);
        savedInstanceState.putParcelable(L_KEY, mLastLocation);
        savedInstanceState.putString(T_KEY, mLastTime);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_local_songs, container, false);
        makingRequests = false;
        mLastTime = "";

        updateVals(savedInstanceState);

        buildGoogleApiClient();
/*----------------------------------------------------------------------------------------------*/
        mapView = (MapView) rootview.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mymap = mapView.getMap();

        mymap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        MapsInitializer.initialize(getActivity());


        localNodes = new ArrayList<>();
        //nearMarkers = new ArrayList<>();
        clickNodes = new HashMap<>();


        return rootview;
    }

    private void updateVals(Bundle savedInstanceState){
        if (savedInstanceState != null){
            if (savedInstanceState.keySet().contains(U_KEY)){
                makingRequests = savedInstanceState.getBoolean(U_KEY);
            }

            if (savedInstanceState.keySet().contains(L_KEY)){
                mLastLocation = savedInstanceState.getParcelable(L_KEY);
            }

            if(savedInstanceState.keySet().contains(T_KEY)){
                mLastTime = savedInstanceState.getString(T_KEY);
            }
            //updateUI();
        }
    }
}
