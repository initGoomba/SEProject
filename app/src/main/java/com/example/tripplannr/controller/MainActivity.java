package com.example.tripplannr.controller;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.tripplannr.model.AddressResultReceiver;
import com.example.tripplannr.model.FetchAddressConstants;
import com.example.tripplannr.model.FetchAddressIntentService;
import com.example.tripplannr.R;
import com.example.tripplannr.model.TripLocation;
import com.example.tripplannr.model.TripViewModel;
import com.example.tripplannr.model.TripViewModel.LocationField;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import static com.example.tripplannr.model.TripViewModel.LocationField.DESTINATION;
import static com.example.tripplannr.model.TripViewModel.LocationField.ORIGIN;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    // Chalmers Lindholmen location :)
    public static final LatLng DEF_LAT_LNG = new LatLng(57.707202, 11.940108);

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    Location mLastLocation;
    Location clickedLocation;
    Marker originMarker;
    Marker destinationMarker;
    LocationRequest mLocationRequest;
    TripViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        model = ViewModelProviders.of(this).get(TripViewModel.class);
        model.getDestination().observe(this, new Observer<TripLocation>() {
            @Override
            public void onChanged(TripLocation tripLocation) {
                locationChanged(tripLocation, DESTINATION);
            }
        });
        model.getOrigin().observe(this, new Observer<TripLocation>() {
            @Override
            public void onChanged(TripLocation tripLocation) {
                locationChanged(tripLocation, ORIGIN);
            }
        });
        model.getAddressQuery().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged (Boolean addressQuery) {
                if (addressQuery) simulateMapClick(mLastLocation);
            }
        });
    }

    private void locationChanged(TripLocation tripLocation, LocationField destination) {
        if (tripLocation != null) {
            LatLng latLng = tripLocationToLatLng(tripLocation.getLocation());
            updateMarker(latLng);
        }
        else removeMarker(destination);
        if (model.getAddressQuery().getValue() != null &&
                model.getAddressQuery().getValue()) {
            model.setAddressQuery(false);
            model.flattenFocLocStack();
        }
    }

    private void removeMarker(LocationField field) {
        if (field == DESTINATION && destinationMarker != null) {
            destinationMarker.remove();
        }
        else if (field == ORIGIN && originMarker != null) {
            originMarker.remove();
        }
    }

    private LatLng tripLocationToLatLng(Location location) {
        return new LatLng(location.getLatitude(),
                location.getLongitude());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEF_LAT_LNG, 13));
        // updateMarker(DEF_LAT_LNG);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000); // two minute interval
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //Location Permission already granted
            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        } else {
            //Request Location Permission
            checkLocationPermission();
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " +
                        location.getLongitude());
                mLastLocation = location;
                //model.autoSetCurrLocation(mLastLocation);
                if (model.isInitOriginField()) {
                    simulateMapClick(location);
                }
            }
        }
    };

    private void simulateMapClick(Location location) {
        LatLng latLng = tripLocationToLatLng(location);
        clickedLocation = mLastLocation;
        onMapClick(latLng);
    }

    private void updateMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if (model.isInitOriginField() || model.getFocusedLocationField() == ORIGIN) {
            removeMarker(ORIGIN);
            markerOptions.icon(BitmapDescriptorFactory.
                    defaultMarker(BitmapDescriptorFactory.HUE_RED));
            originMarker = mMap.addMarker(markerOptions);
        }
        else {
            removeMarker(DESTINATION);
            markerOptions.icon(BitmapDescriptorFactory.
                    defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            destinationMarker = mMap.addMarker(markerOptions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {// If request is cancelled,
            // the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    fusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location permission Needed")
                        .setMessage("This app needs the location permission, " +
                                "please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create().show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        updateMarker(latLng);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

        clickedLocation = new Location("");
        clickedLocation.setLatitude(latLng.latitude);
        clickedLocation.setLongitude(latLng.longitude);
        startIntentService();
    }

    protected void startIntentService() {
        AddressResultReceiver resultReceiver = new AddressResultReceiver(model, new Handler(),
                clickedLocation);
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressConstants.RECEIVER, resultReceiver);
        intent.putExtra(FetchAddressConstants.LOCATION_DATA_EXTRA, clickedLocation);
        startService(intent);
    }

}