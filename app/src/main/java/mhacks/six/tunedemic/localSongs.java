package mhacks.six.tunedemic;

import android.app.Activity;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;



public class localSongs extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener{

    String TAG = "TAG";

    GoogleApiClient mGoogleApiClient;

    Location mLastLocation;
   // TextView mLatitudeText;
  //  TextView mLongitudeText;
    private double myLat, myLong;

    private MapView mapView;
    private GoogleMap mymap;
    LatLng mylocation;
    View rootview;

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
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onResume();
       // mLatitudeText = (TextView) getActivity().findViewById(R.id.lat);
       // mLongitudeText = (TextView) getActivity().findViewById(R.id.lon);
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume(){
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //Toast.makeText(getActivity().getApplicationContext(), String.valueOf(mLastLocation.getLatitude()), Toast.LENGTH_LONG).show();
            //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            myLat = mLastLocation.getLatitude();
            myLong = mLastLocation.getLongitude();
        } else {
            Toast.makeText(this.getActivity(), "OH NO", Toast.LENGTH_LONG).show();
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_local_songs, container, false);
        buildGoogleApiClient();

        mapView = (MapView) rootview.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mymap = mapView.getMap();

        mymap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        MapsInitializer.initialize(this.getActivity());

        mymap.setMyLocationEnabled(true);

        mylocation = new LatLng(20, 30);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mylocation, 17);
        mymap.animateCamera(cameraUpdate);

        return rootview;
    }
}
