package com.oduolgeorgina.kebloodbank;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    public static final int CALL_SOMEONE = 2;
    LocationRequest mLocationRequest;
    private HashMap<Marker, BBUser> myMarkerHashMap;
    private List<BBUser> markerArrayList = new ArrayList<>();
    private SessionManager sessionManager;
    int pos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sessionManager = new SessionManager(getApplicationContext());
        myMarkerHashMap = new HashMap<>();


    }


    private void RegisterDevice(final String email, final String token) {

        //Dialog starts when the method is called
        final ProgressDialog progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setMessage("Registering Device....");
        progressDialog.show();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RequestInterface requestInterface = retrofit.create(RequestInterface.class);

            BBUser user = new BBUser();
            user.setEmail(email);
            user.setDevicetoken(token);

            //Makes a request to the server based on the operation set
            ServerRequest request = new ServerRequest();
            request.setOperation(Constants.REGISTER_DEVICE);
            request.setUser(user);

        Gson gson = new Gson();
        String device = gson.toJson(request);
        Log.i(Constants.TAG, device);

            //Response from the server side, requestInterface is the response received based on the request made
            Call<ServerResponse> response = requestInterface.operation(request);

            response.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                    try {
                        ServerResponse resp = response.body();

                        if (resp.getResult().equals(Constants.SUCCESS)){
                            Toast.makeText(getApplicationContext(), "Device registered to receive notifications. Carry on", Toast.LENGTH_SHORT).show();
                            Log.i(Constants.TAG, email + " " + token);
                        } else {
                            Toast.makeText(getApplicationContext(),resp.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e){
                        Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {


                    Log.d(Constants.TAG,t.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        CreateMarkers();
    }

    private void CreateMarkers() {

        HashMap<String, String> u = sessionManager.getUserDetails();
        String email = u.get(SessionManager.KEY_EMAIL);

        final ProgressDialog progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setMessage("Loading....");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        BBUser user = new BBUser();
        user.setEmail(email);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.GET_LOCATIONS);
        request.setUser(user);

        Call<ServerResponse> response = requestInterface.operation(request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                    ServerResponse serverResponse = response.body();

                    if (serverResponse.getResult().equals(Constants.SUCCESS)){


                        markerArrayList = new ArrayList<>(serverResponse.getUsers());
                        Toast.makeText(getApplicationContext(), "Showing users around you", Toast.LENGTH_SHORT).show();

                        Gson gson = new Gson();
                        String watu = gson.toJson(markerArrayList);
                        Log.i(Constants.TAG, watu);

                        PlotMarkers(markerArrayList, pos);

                    } else {

                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.d("NiHapa", t.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("You are Here");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));


        HashMap<String, String> user = sessionManager.getUserDetails();
        String email = user.get(SessionManager.KEY_EMAIL);
        String phone = user.get(sessionManager.KEY_PHONE);
        String name = user.get(sessionManager.KEY_NAME);
        String capacity = user.get(sessionManager.KEY_CAPACITY);


        markerArrayList.add(new BBUser(name, phone, capacity, location.getLatitude(), location.getLongitude()));
        myMarkerHashMap.put(mCurrLocationMarker, new BBUser(name, phone, capacity, location.getLatitude(), location.getLongitude()));

        if (!isOnline(MapsActivity.this)){
            Toast.makeText(getApplicationContext(), "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
        } else {
            try {
                SaveLocation(email, location.getLatitude(), location.getLongitude());
            } catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void PlotMarkers(List<BBUser> markerArrayList, int pos) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();


            for (int i = 0; i < markerArrayList.size(); i++){

                BBUser user = markerArrayList.get(pos);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(user.getLatitude(), user.getLongitude()));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker());

                Marker marker = mMap.addMarker(markerOptions);
                builder.include(markerOptions.getPosition());
                myMarkerHashMap.put(marker, markerArrayList.get(i));

                mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
            }

    }


    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

        public MarkerInfoWindowAdapter() {
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
            final BBUser myMarker = myMarkerHashMap.get(marker);

            TextView markerlabel = (TextView) v.findViewById(R.id.tvusername);
            TextView markercapacity = (TextView) v.findViewById(R.id.tvtype);
            TextView markercall = (TextView) v.findViewById(R.id.calluser);

            markerlabel.setText(myMarker.getName());
            markercapacity.setText(myMarker.getCapacity());
            markercall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + myMarker.getPhone()));
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                            PermissionRequest();
                            return;
                        }
                        startActivity(intent);
                }
            });
            return v;
        }
    }

    private void PermissionRequest() {
        if (isCallaNiggaAllowed()){

        }
        else {
            requestCallPermission();
        }
    }

    private void requestCallPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, android.Manifest.permission.CALL_PHONE)){

        }
        else {
            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{android.Manifest.permission.CALL_PHONE},CALL_SOMEONE);
        }
    }

    private boolean isCallaNiggaAllowed() {
        int result = ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.CALL_PHONE);

        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else {
            return false;
        }
    }

    //Checks if there is a working connection to the internet
    private boolean isOnline(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void SaveLocation(String email, double latitude, double longitude) {

        final ProgressDialog progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setTitle("Just a moment");
        progressDialog.setMessage("Loading....");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        BBUser user = new BBUser();
        user.setEmail(email);
        user.setLatitude(latitude);
        user.setLongitude(longitude);

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.LOCATION_OPERATION);
        request.setUser(user);

        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();

                //Fetch result from db
                try {
                    if(resp.getResult().equals(Constants.SUCCESS)) {

                        Toast.makeText(getApplicationContext(), "Your location has been saved", Toast.LENGTH_SHORT).show();
                        Checker();

                    } else {
                        Toast.makeText(getApplicationContext(),resp.getMessage(), Toast.LENGTH_SHORT ).show();
                    }
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                Log.d(Constants.TAG,t.getLocalizedMessage());
                Toast.makeText(getApplicationContext(),t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void Checker() {

        HashMap<String, String> user = sessionManager.getUserDetails();
        String capacity = user.get(SessionManager.KEY_CAPACITY);

        if (capacity.isEmpty()){
            InitiateUpdateCapacity();
        }
    }

    private void InitiateUpdateCapacity() {

        HashMap<String, String> user = sessionManager.getUserDetails();
        final String email = user.get(SessionManager.KEY_EMAIL);

        new MaterialDialog.Builder(this)
                .title("Choose your capacity")
                .items(R.array.capacities)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                        String choice = text.toString();
                        UpdateCapacity(email, choice);
                        return true; // allow selection
                    }
                })
                .positiveText(R.string.md_choose_label)
                .show();
    }

    private void UpdateCapacity(final String email, final String choice) {

        final ProgressDialog progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setTitle("Just a moment");
        progressDialog.setMessage("Loading....");
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        BBUser user = new BBUser();
        user.setEmail(email);
        user.setCapacity(choice);

        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.CAPACITY_OPERATION);
        request.setUser(user);

        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {


                ServerResponse resp = response.body();

                //Fetch result from db and save to Shared Preferences
                if(resp.getResult().equals(Constants.SUCCESS)) {

                    Toast.makeText(getApplicationContext(), "Your preference has been updated", Toast.LENGTH_SHORT).show();
                    sessionManager.updatecapacity(choice);

                } else {
                    Toast.makeText(getApplicationContext(),resp.getMessage(), Toast.LENGTH_SHORT ).show();
                }
                progressDialog.dismiss();

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                Log.d(Constants.TAG,t.getLocalizedMessage());
                Toast.makeText(getApplicationContext(),t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case  CALL_SOMEONE: {


                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MapsActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MapsActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                }

                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);


        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        HashMap<String, String> user = sessionManager.getUserDetails();
        String capacity = user.get(SessionManager.KEY_CAPACITY);
        String email = user.get(sessionManager.KEY_EMAIL);

        int id = item.getItemId();

        if (id == R.id.action_logout){


            //Creates an alert dialog that prompts a user to logout
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SignOut();
                        }
                    })
                    .setNegativeButton("No",null).show();

        }

        if (id == R.id.action_sendmessage){

            if (!capacity.equals("Health Officer")){
                Toast.makeText(getApplicationContext(), "Action not authorised", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(MapsActivity.this, SendMessage.class));
            }
        }

        if (id ==  R.id.action_push){

            if (capacity.equals("Health Officer")){
                Toast.makeText(getApplicationContext(), "Action not authorised", Toast.LENGTH_SHORT).show();
            } else {

                HashMap<String, String> deviceToken = sessionManager.getDeviceToken();
                String token = deviceToken.get(SessionManager.TAG_TOKEN);

                if (!isOnline(MapsActivity.this)){

                    Toast.makeText(getApplicationContext() ,"NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                } else {
                    RegisterDevice(email, token);
                }


            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void SignOut() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(getApplicationContext(),"See you next time....", Toast.LENGTH_LONG).show();
                sessionManager.logoutUser();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }, 500);

    }
}

