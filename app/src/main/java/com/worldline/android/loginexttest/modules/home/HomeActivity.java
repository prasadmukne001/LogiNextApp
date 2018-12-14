package com.worldline.android.loginexttest.modules.home;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.worldline.android.loginexttest.R;
import com.worldline.android.loginexttest.commons.database.SQLiteDatabaseManager;
import com.worldline.android.loginexttest.commons.services.LocationService;
import com.worldline.android.loginexttest.commons.view.BottomNavigationViewHelper;
import com.worldline.android.loginexttest.commons.view.ImageViewerActivity;
import com.worldline.android.loginexttest.databinding.PhotoDialogBinding;
import com.worldline.android.loginexttest.modules.camera2.CaptureCamera2Activity;
import com.worldline.android.loginexttest.utility.LogiNextConstants;
import com.worldline.android.loginexttest.viewmodels.HomeScreenViewModel;
import es.dmoral.toasty.Toasty;
import java.util.ArrayList;
import java.util.HashMap;


public class HomeActivity extends FragmentActivity implements OnMapReadyCallback
{

	@InjectView(R.id.navigationView)
	BottomNavigationView bottomNavigationView;
	private GoogleMap mMap;
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private boolean isServiceStarted = false;
	private static AlertDialog photoDialog;

	public static final int PICK_IMAGE = 1;
	private static FragmentActivity context;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps_test);
		context = this;
		ButterKnife.inject(this);
		try
		{
			initialiseMap();

			setUpBottomNavigation();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void initialiseMap()
	{
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}


	private void setUpBottomNavigation()
	{
		BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
		bottomNavigationView.setOnNavigationItemSelectedListener(new OnNavigationItemSelectedListener()
		{
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item)
			{

				switch (item.getItemId())
				{
					case R.id.navigationPhoto:
						showPhotoDialog();
						break;
					case R.id.navigationRealTimeTracking:
						if (!isServiceStarted)
						{

							isServiceStarted = true;
							startService(new Intent(HomeActivity.this, LocationService.class));
							item.setTitle("Stop Real-time Tracking");
							Toasty.info(HomeActivity.this,"Real-time Tracking Service Started").show();
						}
						else
						{
							isServiceStarted = false;
							stopService(new Intent(HomeActivity.this, LocationService.class));
							item.setTitle("Real-time Tracking");
							Toasty.info(HomeActivity.this,"Real-time Tracking Service Stopped").show();
						}

						break;
					case R.id.navigationShowRoute:
						new ShowRouteAsynTask().execute();
						break;

				}

				return false;
			}
		});
	}

	private HashMap<String, ArrayList<String>> getDataFromDB()
	{
		HashMap<String, ArrayList<String>> hashMap = new HashMap<String, ArrayList<String>>();
		SQLiteDatabaseManager sqLiteDatabaseManager = SQLiteDatabaseManager.getInstance(HomeActivity.this);
		Cursor cursor = sqLiteDatabaseManager.search("Select * from " + SQLiteDatabaseManager.LOCATION_TABLE + ";");
		ArrayList<String> latitudeArrayList = new ArrayList();
		ArrayList<String> longitudeArrayList = new ArrayList();
		while (cursor.moveToNext())
		{
			try
			{

				latitudeArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteDatabaseManager.LATITUDE)));
				longitudeArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteDatabaseManager.LONGITUDE)));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		cursor.close();
		hashMap.put("lat", latitudeArrayList);
		hashMap.put("log", longitudeArrayList);
		return hashMap;
	}

	private class ShowRouteAsynTask extends AsyncTask<String, Integer, HashMap<String, ArrayList<String>>>
	{

		@Override
		protected HashMap<String, ArrayList<String>> doInBackground(String... jsonData)
		{
			HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
			try
			{
				hashMap = getDataFromDB();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return hashMap;
		}

		@Override
		protected void onPostExecute(HashMap<String, ArrayList<String>> result)
		{

			mMap.clear();
			ArrayList points = null;
			PolylineOptions lineOptions = null;
			ArrayList<String> latitudeArrayList = result.get("lat");
			ArrayList<String> longitudeArrayList = result.get("log");
			points = new ArrayList();
			lineOptions = new PolylineOptions();

			for (int j = 0; j < latitudeArrayList.size(); j++)
			{

				double lat = Double.parseDouble(latitudeArrayList.get(j));
				double lng = Double.parseDouble(longitudeArrayList.get(j));
				LatLng position = new LatLng(lat, lng);

				points.add(position);
			}

			lineOptions.addAll(points);
			lineOptions.width(12);
			lineOptions.color(getResources().getColor(R.color.colorPrimary));
			lineOptions.geodesic(true);

			if (null != lineOptions)
			{
				Polyline polyline = mMap.addPolyline(lineOptions);

			}

			try
			{

				LatLng latLong1 = new LatLng(Double.parseDouble(latitudeArrayList.get(0)), Double.parseDouble(longitudeArrayList.get(0)));
				LatLng latLong = new LatLng(Double.parseDouble(latitudeArrayList.get(latitudeArrayList.size() - 1)), Double.parseDouble(longitudeArrayList.get(latitudeArrayList.size() - 1)));
				MarkerOptions markerOptions1 = new MarkerOptions().position(latLong).title("Previous Location");
				Marker locationMarker = mMap.addMarker(markerOptions1);
				MarkerOptions markerOptions2 = new MarkerOptions().position(latLong1).title("Current Location");
				Marker locationMarker1 = mMap.addMarker(markerOptions2);
				LatLngBounds.Builder builder = new LatLngBounds.Builder();

				Marker[] markers = new Marker[2];
				markers[0] = locationMarker;
				markers[1] = locationMarker1;
				for (Marker marker : markers)
				{
					builder.include(marker.getPosition());
				}
				LatLngBounds bounds = builder.build();
				int padding = 100; // offset from edges of the map in pixels
				CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
				mMap.moveCamera(cu);
				mMap.animateCamera(cu);

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == PICK_IMAGE)
		{
			try
			{
				Uri selectedImage = data.getData();
				String path = getPath(this, selectedImage);
				Intent intent = new Intent(HomeActivity.this, ImageViewerActivity.class);
				intent.putExtra(LogiNextConstants.FILE_PATH, path);
				startActivity(intent);

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void showPhotoDialog()
	{
		photoDialog = new AlertDialog.Builder(this).create();
		PhotoDialogBinding photoDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.photo_dialog, null, false);
		photoDialog.setView(photoDialogBinding.getRoot());
		photoDialogBinding.setViewModel(new HomeScreenViewModel());
		photoDialog.show();
	}

	@BindingAdapter({"action"})
	public static void doAction(View view, String message)
	{
		if (message != null && message.equals(HomeScreenViewModel.galleryMessage))
		{
			photoDialog.dismiss();
			Toasty.info(view.getContext(), message, Toast.LENGTH_SHORT).show();
			Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
			getIntent.setType("image/*");
			Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
			context.startActivityForResult(chooserIntent, PICK_IMAGE);
		}
		else if (message != null && message.equals(HomeScreenViewModel.cameraMessage))
		{
			photoDialog.dismiss();
			Toasty.info(view.getContext(), message, Toast.LENGTH_SHORT).show();
			context.startActivity(new Intent(context, CaptureCamera2Activity.class));
		}

	}


	@Override
	protected void onResume()
	{
		super.onResume();

		if (!checkPlayServices())
		{
			Toasty.warning(this, "You need to install Google Play Services to use the App properly", Toast.LENGTH_SHORT).show();

		}
	}


	private boolean checkPlayServices()
	{
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

		if (resultCode != ConnectionResult.SUCCESS)
		{
			if (apiAvailability.isUserResolvableError(resultCode))
			{
				apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
			}
			else
			{
				finish();
			}

			return false;
		}

		return true;
	}

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		mMap = googleMap;
		if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			return;
		}
		mMap.setMyLocationEnabled(true);

		Location locationCt;
		LocationManager locationManagerCt = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationCt = locationManagerCt.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		LatLng latLng = new LatLng(locationCt.getLatitude(),
			locationCt.getLongitude());
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.addMarker(new MarkerOptions().position(latLng)
											   .title("Current Location"));

		googleMap.setMyLocationEnabled(true);

		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Zoom in the Google Map
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

	}

	public static String getPath(final Context context, final Uri uri)
	{

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri))
		{
			if (isExternalStorageDocument(uri))
			{
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type))
				{
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

			}
			else if (isDownloadsDocument(uri))
			{

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			else if (isMediaDocument(uri))
			{
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type))
				{
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				}
				else if ("video".equals(type))
				{
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				}
				else if ("audio".equals(type))
				{
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		else if ("content".equalsIgnoreCase(uri.getScheme()))
		{
			return getDataColumn(context, uri, null, null);
		}
		else if ("file".equalsIgnoreCase(uri.getScheme()))
		{
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
	{

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {column};

		try
		{
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst())
			{
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
		return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri)
	{
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri)
	{
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri)
	{
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	@Override
	public void onBackPressed()
	{
		logoutDialog();
	}

	private void logoutDialog()
	{
		new AlertDialog.Builder(this).setIcon(null).setTitle("Confirm").setCancelable(false).setMessage("Do you want to logout of FasTag app ?").setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				finish();
			}

		}).setNegativeButton("No", null).show();

	}

}
