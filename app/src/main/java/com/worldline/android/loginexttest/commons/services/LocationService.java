package com.worldline.android.loginexttest.commons.services;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.worldline.android.loginexttest.commons.database.SQLiteDatabaseManager;
import es.dmoral.toasty.Toasty;
import java.util.Calendar;

public class LocationService extends Service implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
	private Location location;
	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;
	private static final long UPDATE_INTERVAL = 120000, FASTEST_INTERVAL = 120000; // = 5 seconds

	public LocationService()
	{

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		initialiseGoogleAPI();
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (googleApiClient != null && googleApiClient.isConnected())
		{
			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
			googleApiClient.disconnect();
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void initialiseGoogleAPI()
	{
		googleApiClient = new GoogleApiClient.Builder(this).
															   addApi(LocationServices.API).
															   addConnectionCallbacks(this).
															   addOnConnectionFailedListener(this).build();

		if (googleApiClient != null)
		{
			googleApiClient.connect();
		}

		(new AsyncTask<Void,Void,Void>(){

			@Override
			protected Void doInBackground(Void... voids)
			{
				SQLiteDatabaseManager.getInstance(LocationService.this).rawQuery("Delete from "+SQLiteDatabaseManager.LOCATION_TABLE+" where 0=0;",null);
				return null;
			}
		}).execute();

	}

	@Override
	public void onConnected(@Nullable Bundle bundle)
	{
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			return;
		}
		location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

		if (location != null)
		{

		}

		startLocationUpdates();
	}

	private void startLocationUpdates()
	{
		locationRequest = new LocationRequest();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(UPDATE_INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			Toasty.info(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
		}

		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int i)
	{
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{
	}

	@Override
	public void onLocationChanged(final Location location)
	{

		(new AsyncTask<Void,Void,Void>(){

			@Override
			protected Void doInBackground(Void... voids)
			{
				if (location != null)
				{
					//Toast.makeText(this, "LATITUDE : " + location.getLatitude() + "\nLONGITUDE : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
					ContentValues contentValues=new ContentValues();
					contentValues.put(SQLiteDatabaseManager.TIMESTAMP, Calendar.getInstance().getTimeInMillis());
					contentValues.put(SQLiteDatabaseManager.LATITUDE, location.getLatitude());
					contentValues.put(SQLiteDatabaseManager.LONGITUDE, location.getLongitude());
					SQLiteDatabaseManager.getInstance(LocationService.this).insert(SQLiteDatabaseManager.LOCATION_TABLE,contentValues);

				}
				return null;
			}
		}).execute();

	}
}
