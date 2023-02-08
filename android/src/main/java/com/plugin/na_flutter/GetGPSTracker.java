package com.plugin.na_flutter;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GetGPSTracker implements LocationListener {

    private final Context mContext;
    boolean isGPSEnabled = false;

    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;

    String providerVal = "cannot get provider";
    private static final String TAG = "MyLocationPluginStatus";
    public GetGPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            boolean hasPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (hasPermission) {
                locationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (!isGPSEnabled && !isNetworkEnabled) {
                    Log.e(TAG, "getLocation: enter is Not Enabled");
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    if (isGPSEnabled) {

                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                Log.e(TAG, "getLocation: newLoc is is " + location);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }
            } else {
                //	showLocationAlert();
            }


        } catch (Exception e) {
            Log.e(TAG, "getLocation: e.PrintTrace");
            e.printStackTrace();
        }
        return location;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GetGPSTracker.this);
        }
    }


    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }


    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    public String getProvider() {
        if (providerVal != null) {
            providerVal = location.getProvider();
        }

        return providerVal;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("Enhance");

        alertDialog
                .setMessage("GPS is not enabled. Do you want to enable in settings?");

        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                        dialog.cancel();
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    public void showLocationAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("Enhance");

        alertDialog
                .setMessage("Location permission is not granted please enable to continue!");

        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + mContext.getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        mContext.startActivity(i);
                        dialog.cancel();
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public String getAnyProviderLocation() {
        latitude = 0.0;
        longitude = 0.0;
        providerVal = "cannot get provider";
        try {
            boolean hasPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (hasPermission) {

                locationManager.requestLocationUpdates(getBestProvider(locationManager), 0, 0, this);

                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(getBestProvider(locationManager));
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        providerVal = location.getProvider();

                    } else {
                        latitude = 0.0;
                        longitude = 0.0;
                        providerVal = "cannot get provider";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            latitude = 0.0;
            longitude = 0.0;
            providerVal = "cannot get provider";

        }
        return latitude + "," + longitude + "," + providerVal;
    }

    protected String getBestProvider(LocationManager locationManager) {
        String provider = LocationManager.PASSIVE_PROVIDER;
       /* if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else*/
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        }
        return provider;
    }

    public boolean getLocationMode() {
        int locationMode = 9;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (locationMode == Settings.Secure.LOCATION_MODE_OFF && locationMode != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY && locationMode != Settings.Secure.LOCATION_MODE_SENSORS_ONLY) {
                return false;  //location not enabled
            } else {
                if (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
                    return true;//Mode=3  //High Accuracy Enabled
                } else {
                    return false;//High Accuracy Not Enabled
                }
            }
        }
        return false;
    }


    public String getTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String mLastUpdateTimee = sdf.format(d);

        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        String mLastUpdateTime1 = sdf1.format(d);
        String mLastUpdateTime = mLastUpdateTimee + "T" + mLastUpdateTime1;
        return mLastUpdateTime;
    }

}
