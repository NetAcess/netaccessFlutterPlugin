package com.plugin.na_flutter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableField;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class Offline_Area extends AppCompatActivity implements OnMapReadyCallback {
	MeasureView measureView;
	LinearLayout ll_button, ll_close, deleteLayout;
	Button startStopButton, saveButton, reset_button, closeButton;
	Button fetchButton;
	DBHandler db;
	Context c;
	SharedPreferences pref;
	Editor editor;
	RadioGroup type;
	Dialog dialog;
	//AreaPlugin area = new AreaPlugin();
	private GoogleMap map;
	RelativeLayout rl_maplayout;
	Location currentLocation;
	FusedLocationProviderClient fusedLocationProviderClient;
	private static final int REQUEST_CODE = 101;
	Polyline finpolyline;
	LocationManager locationManager;

	View mapView;
	TextView valueTv;
	ArrayList<MarkerOptions> markerList;
	MarkerOptions helperMarker = null;
	Marker tempMarker = null;

	Polygon polygon1;
	ImageView deleteBtn;
	MyFab fab;
	boolean isDragging = false;
	boolean isRunning = false;
	CountDownTimer mTimer;

	public static ObservableField<String> screenPixel = new ObservableField<>();

	private OnClickListener reset_button_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				ArrayList<Double> temparraylist = new ArrayList<Double>();
				if (measureView.getType().equalsIgnoreCase("Manual")) {
					temparraylist = measureView.get_fetch_LatList();
				}else if (measureView.getType().equalsIgnoreCase("Auto")){
					temparraylist = measureView.getLatList();
				}else{
					temparraylist = measureView.getMapLatList();
				}
				int len = temparraylist.size();
				if (len > 0) {
					showAlertDialog(c, "Saved coordinates will be deleted. Do you want to continue?", "Reset");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private OnClickListener start_button_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			startMeasureArea();
		}
	};

	private OnClickListener save_button_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			/*
			 * String starttime = pref.getString("STARTTIME","");
			 * area.testArea(xmlvall
			 * ,"3.0","2017/07/11 10:41","2017/07/11 10:51"); finish();
			 */

			/*Intent inn = new Intent(Offline_Area.this,MapActivity.class);
			  startActivity(inn); finish();*/

			int plotstatus = pref.getInt("ANOTHERPLOTSTATUS", 0);

			plotstatus++;
			editor.putInt("ANOTHERPLOTSTATUS", plotstatus);
			editor.commit();

			if (measureView.getType().equalsIgnoreCase("Manual")) {

				ArrayList<Double> fetch_Latlist = measureView
						.get_fetch_LatList();
				ArrayList<Double> fetch_Lnglist = measureView
						.get_fetch_LngList();
				ArrayList<Integer> fetch_accylist = measureView
						.get_fetch_AccyList();
				int fetch_latlength = fetch_Latlist.size();
				int fetch_lnglength = fetch_Lnglist.size();
				int fetch_accylength = fetch_accylist.size();

				if (fetch_latlength > 0 && fetch_lnglength > 0
						&& fetch_accylength > 0) {

					createXMLAndShowDialog(fetch_accylist, fetch_Latlist,
							fetch_Lnglist);

				} else {
					showAlertDialog(c, "Not enough points to plot area", "Save");
				}
			} else if (measureView.getType().equalsIgnoreCase("Auto")) {
				ArrayList<Integer> fAcclist = measureView.getAccList();
				ArrayList<Double> fLatlist = measureView.getLatList();
				ArrayList<Double> fLnglist = measureView.getLngList();
				int latlength = fLatlist.size();
				int lnglength = fLnglist.size();
				int accLength = fAcclist.size();

				if (latlength > 0 && lnglength > 0 && accLength > 0) {

					createXMLAndShowDialog(fAcclist, fLatlist, fLnglist);

				} else {
					showAlertDialog(c, "Not enough points to plot area", "Save");
				}
			} else {
				//Toast.makeText(Offline_Area.this,"Map Save",Toast.LENGTH_SHORT).show();
				//markerList.get(0).getPosition().longitude
				ArrayList<Integer> mAcclist = measureView.get_Map_AccyList();
				ArrayList<Double> mLatlist = measureView.getMapLatList();
				ArrayList<Double> mLnglist = measureView.getMapLngList();
				int latlength = mLatlist.size();
				int lnglength = mLnglist.size();
				int accLength = mAcclist.size();

				if (latlength > 0 && lnglength > 0 && accLength > 0) {
					long timeInMillis = System.currentTimeMillis();
					Calendar cal1 = Calendar.getInstance();
					cal1.setTimeInMillis(timeInMillis);

					SimpleDateFormat dateFormat1 = new SimpleDateFormat(
							"yyyy/MM/dd HH:mm");
					String currentDateandTime1 = dateFormat1.format(cal1
							.getTime());

					System.out.println(currentDateandTime1);

					editor.putString("ENDTIMEVALUE", currentDateandTime1);
					editor.commit();

					createXMLAndShowDialog(mAcclist, mLatlist, mLnglist);

				} else {
					showAlertDialog(c, "Not enough points to plot area", "Save");
				}
			}

		}
	};

	private OnClickListener log_button_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			measureView.importFromGPS();
			measureView.postInvalidate();
		}
	};

	/** Called when the activity is first created. */

	void detectDragging() {
			if(isRunning) {
				mTimer.cancel();
			}
			mTimer = new CountDownTimer(50, 50) {

				public void onTick(long millisUntilFinished) {
					isRunning = true;
				}

				public void onFinish() {
					isRunning = false;
					if(!isDragging) {
						drawPolygon();
						/*fab.setVisibility(View.GONE);
						helperMarker = null;
						map.getUiSettings().setScrollGesturesEnabled(true);
						map.getUiSettings().setZoomGesturesEnabled(true);
						map.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);*/
					}
				}
			}.start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);
		markerList = new ArrayList();

		fab = findViewById(R.id.myFab);

		measureView = (MeasureView) findViewById(R.id.measureView);
		ll_button = (LinearLayout) findViewById(R.id.ll_button);
		ll_close = (LinearLayout) findViewById(R.id.ll_close);
		rl_maplayout = (RelativeLayout) findViewById(R.id.rl_maplayout);
		deleteLayout = (LinearLayout) findViewById(R.id.deleteOverlay);
		deleteBtn = (ImageView) findViewById(R.id.deleteMarker);
		fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
		screenPixel.addOnPropertyChangedCallback(new androidx.databinding.Observable.OnPropertyChangedCallback() {
			@Override
			public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {
				isDragging = true;
				Projection projection = map.getProjection();
				// Returns the geographic location that corresponds to a screen location
				int x, y;
				x = Math.round(Float.parseFloat(screenPixel.get().split("\\|")[0]));
				y = Math.round(Float.parseFloat(screenPixel.get().split("\\|")[1]));
				LatLng geographicalPosition = projection.fromScreenLocation(new Point(x, y));
				tempMarker.setPosition(geographicalPosition);
				if(helperMarker.getTitle().split("\\|")[0].equals("Real")) {
					for (int i = 0; i < markerList.size(); i++) {
						if(helperMarker.getTitle().equals(markerList.get(i).getTitle())) {
							String title = markerList.get(i).getTitle();
							markerList.remove(i);

							MarkerOptions marker = new MarkerOptions();
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.square));
							marker.alpha(0.8f);
							marker.draggable(true);
							marker.position(geographicalPosition);
							marker.title(title);
							markerList.add(i, marker);
							measureView.importFromMap(marker.getPosition().latitude, marker.getPosition().longitude, (int)Math.round(currentLocation.getAccuracy()), i, true);
						}
					}
				} else {
					MarkerOptions marker = new MarkerOptions();
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.square));
					//marker.position(new LatLng(helperMarker.getPosition().latitude, helperMarker.getPosition().longitude));
					marker.position(geographicalPosition);
					marker.draggable(true);
					marker.title("Real|" + geographicalPosition);
					int pos = Integer.parseInt(helperMarker.getTitle().split("\\|")[1]);
					helperMarker = marker;
					markerList.add(pos + 1, marker);
					measureView.importFromMap(geographicalPosition.latitude, geographicalPosition.longitude, (int)Math.round(currentLocation.getAccuracy()),pos + 1, false);
				}
				isDragging = false;
				detectDragging();
			}
		});

		deleteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				for(int i = 0 ; i < markerList.size() ; i++) {
					if(markerList.get(i).getTitle().equals(helperMarker.getTitle())) {
						markerList.remove(i);
						measureView.removeMarker(i);
					}
				}
				helperMarker = null;
				fab.setVisibility(View.GONE);
				drawPolygon();
				map.getUiSettings().setScrollGesturesEnabled(true);
				map.getUiSettings().setZoomGesturesEnabled(true);
				map.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
			}
		});

		MeasureView.measure = false;
		MeasureView.stop = false;

		db = new DBHandler(getApplicationContext(), null, null, 1);
		c = this;

		db.delete_table("getlocation");

		pref = getApplicationContext().getSharedPreferences("MyPref",
				Activity.MODE_PRIVATE);
		editor = pref.edit();
		// editor.putBoolean("TIMEACCURACYCHECK", false);
		editor.putBoolean("TIMEACCURACYCHECK", false); // replaced for not
		// checking 30secs
		editor.putString("XMLVALUE", "");
		editor.putInt("ANOTHERPLOTSTATUS", 0);
		editor.putString("AREAVALUE", "");
		editor.putString("STARTTIMEVALUE", "");
		editor.putString("ENDTIMEVALUE", "");
		editor.putString("ACCURACYVALUE", "");

		editor.commit();

		measureView.setType("Auto");
		measureView.setTime(0);
		measureView.setaccuracy(30);

		type = (RadioGroup) findViewById(R.id.type);

		measureView.setLengthUnits(MeasureView.LENGTH_UNITS_METER);
		measureView.setAreaUnits(MeasureView.AREA_UNITS_ACRE);

		boolean gpsEnabled = measureView.checkGps();
		if (!gpsEnabled) {

			showAlertDialog(c, "GPS is not enabled. Do you want to go to settings menu?", "GPS");

		}

		WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()) {
			// wifi is enabled
			// showAlertDialog(c, "Please disable wifi for better accuracy.",
			// "Wifi");
			wifi.setWifiEnabled(false);
		}

		reset_button = (Button) findViewById(R.id.resetButton);
		reset_button.setOnClickListener(reset_button_listener);

		fetchButton = (Button) findViewById(R.id.fetchButton);
		fetchButton.setOnClickListener(log_button_listener);

		startStopButton = (Button) findViewById(R.id.startButton);
		startStopButton.setOnClickListener(start_button_listener);

		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(save_button_listener);

		closeButton = (Button) findViewById(R.id.closeButton);
		closeButton.setOnClickListener(close_button_listener);

		dialog = new Dialog(this);
		dialog.setOwnerActivity(this);
		measureView.init(); 

		valueTv = (TextView) findViewById(R.id.acerValue);

		reset_button.setEnabled(false);
		reset_button.setBackgroundResource(R.drawable.button_disabled);

		saveButton.setEnabled(false);
		saveButton.setBackgroundResource(R.drawable.button_disabled);


		type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub

				if (checkedId == R.id.auto) {
					measureView.setVisibility(View.VISIBLE);
					ll_button.setVisibility(View.VISIBLE);
					ll_close.setVisibility(View.GONE);
					rl_maplayout.setVisibility(View.GONE);
					measureView.setType("Auto");
					fetchButton.setVisibility(View.GONE);

					reset_button.setEnabled(false);
					reset_button.setBackgroundResource(R.drawable.button_disabled);
					saveButton.setEnabled(false);
					saveButton.setBackgroundResource(R.drawable.button_disabled);
					startStopButton.setEnabled(true);
					startStopButton.setBackgroundResource(R.drawable.button_background);
					if (measureView.isRunning()) {
						measureView.stop();
						startStopButton.setText(c.getString(R.string.start));
						startStopButton.postInvalidate();
						startStopButton.setEnabled(true);
						startStopButton.setBackgroundResource(R.drawable.button_background);
						measureView.reset();
						measureView.clearArraylist();
						markerList.clear();
						helperMarker=null;
					}

				} else if (checkedId == R.id.manual) {
					measureView.setVisibility(View.VISIBLE);
					ll_button.setVisibility(View.VISIBLE);
					ll_close.setVisibility(View.GONE);
					rl_maplayout.setVisibility(View.GONE);
					measureView.setType("Manual");
					reset_button.setEnabled(false);
					reset_button.setBackgroundResource(R.drawable.button_disabled);

					saveButton.setEnabled(false);
					saveButton.setBackgroundResource(R.drawable.button_disabled);
					startStopButton.setEnabled(true);
					startStopButton.setBackgroundResource(R.drawable.button_background);
					if (measureView.isRunning()) {
						measureView.stop();
						startStopButton.setText(c.getString(R.string.start));
						startStopButton.postInvalidate();
						startStopButton.setEnabled(true);
						startStopButton.setBackgroundResource(R.drawable.button_background);
						measureView.reset();
						measureView.clearArraylist();
						markerList.clear();
						helperMarker=null;
					}
				} else {
					measureView.setVisibility(View.GONE);
					ll_button.setVisibility(View.VISIBLE);
					//ll_close.setVisibility(View.VISIBLE);
					fetchButton.setVisibility(View.GONE);
					rl_maplayout.setVisibility(View.VISIBLE);
					measureView.setType("Map");
					startStopButton.setEnabled(false);
					startStopButton.setBackgroundResource(R.drawable.button_disabled);

					reset_button.setEnabled(true);
					reset_button.setBackgroundResource(R.drawable.button_background);

					saveButton.setEnabled(true);
					saveButton.setBackgroundResource(R.drawable.button_background);
					saveButton.setEnabled(false);
					saveButton.setBackgroundResource(R.drawable.button_disabled);
					if (measureView.isRunning()) {
						measureView.stop();
						startStopButton.setText(c.getString(R.string.start));
						startStopButton.postInvalidate();
						startStopButton.setEnabled(false);
						startStopButton.setBackgroundResource(R.drawable.button_disabled);
						measureView.reset();
						measureView.clearArraylist();

					}
					valueTv.setText("0.00");
					markerList.clear();
					helperMarker=null;
					fetchLocation();
				}
			}
		});
	}

	private OnClickListener close_button_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			ArrayList<Integer> mAcclist = measureView.get_Map_AccyList();
			ArrayList<Double> mLatlist = measureView.getMapLatList();
			ArrayList<Double> mLnglist = measureView.getMapLngList();
			int latlength = mLatlist.size();
			int lnglength = mLnglist.size();
			int accLength = mAcclist.size();

			if (latlength > 0 && lnglength > 0 && accLength > 0) {
				if (latlength > 1) {
					measureView.importFromMap(mLatlist.get(0), mLnglist.get(0), mAcclist.get(0), -1, false);
					ArrayList<Double> mLatlist1 = measureView.getMapLatList();
					ArrayList<Double> mLnglist1 = measureView.getMapLngList();
					ArrayList<LatLng> coordList1 = new ArrayList<LatLng>();
					if (mLatlist1.size() > 0) {
						//finpolyline.remove();
						for (int i = 0; i < mLatlist1.size(); i++) {
							coordList1.add(new LatLng(mLatlist1.get(i), mLnglist1.get(i)));
						}
						PolylineOptions polylineOptions = new PolylineOptions();
						polylineOptions.addAll(coordList1);

						polylineOptions.width(5).color(Color.RED);
						finpolyline = map.addPolyline(polylineOptions);
					}
					saveButton.setEnabled(true);
					saveButton.setBackgroundResource(R.drawable.button_background);

				} else {
					saveButton.setEnabled(true);
					saveButton.setBackgroundResource(R.drawable.button_background);
				}
			} else {
				showAlertDialog(c, "Not enough points to plot area", "Save");
			}
		}
	};

	/**
	 * Author : Prabhakaran Created Date : Jul 6, 2017-2017 Description :
	 *
	 * @param fLnglist
	 * @param fLatlist
	 * @param fAcclist
	 */
	protected void createXMLAndShowDialog(ArrayList<Integer> fAcclist,
										  ArrayList<Double> fLatlist, ArrayList<Double> fLnglist) {

		ArrayList<String> fTimelist = measureView.getTimeList();
		ArrayList<String> fFetchTimelist = measureView.getFetchTimeList();
		ArrayList<String> mFetchTimelist = measureView.getMapTimeList();
		ArrayList<Integer> fFetchAccuracylist = measureView
				.get_fetch_AccyList();

//		int plotstatus = pref.getInt("ANOTHERPLOTSTATUS", 0);
		int latlength = fLatlist.size();

		GETLOCATION_POJO locpojo = null;
		for (int i = 0; i < latlength; i++) {
			//plot status is used to plot another area if anpther plot is enabled uncomment this below part
			/*locpojo = new GETLOCATION_POJO(String.valueOf(plotstatus),
					String.valueOf(fAcclist.get(i)), fTimelist.get(i),
					String.valueOf(fLatlist.get(i)), String.valueOf(fLnglist
							.get(i)));*/
			if (measureView.getType().equalsIgnoreCase("Manual")) {
				locpojo = new GETLOCATION_POJO("1",
						String.valueOf(fAcclist.get(i)), fFetchTimelist.get(i),
						String.valueOf(fLatlist.get(i)), String.valueOf(fLnglist
						.get(i)));
			} else if (measureView.getType().equalsIgnoreCase("Auto")) {
				locpojo = new GETLOCATION_POJO("1",
						String.valueOf(fAcclist.get(i)), fTimelist.get(i),
						String.valueOf(fLatlist.get(i)), String.valueOf(fLnglist
						.get(i)));
			} else {
				locpojo = new GETLOCATION_POJO("1",
						String.valueOf(fAcclist.get(i)), mFetchTimelist.get(i),
						String.valueOf(fLatlist.get(i)), String.valueOf(fLnglist
						.get(i)));
			}

			db.addgetlocation(locpojo);
		}

		String kml = "";
		String xmlvalue = pref.getString("XMLVALUE", "");

		if (measureView.getType().equalsIgnoreCase("Manual")) {

			if (xmlvalue.equalsIgnoreCase("")) {// first
				for (int i = 0; i < fLatlist.size(); i++) {
					kml += "<point><time>" + fFetchTimelist.get(i)
							+ "</time><accy>" + fFetchAccuracylist.get(i)
							+ "</accy><lng>" + fLnglist.get(i) + "</lng><lat>"
							+ fLatlist.get(i) + "</lat></point>";
				}
			} else {
				kml = xmlvalue;
				for (int i = 0; i < fLatlist.size(); i++) {
					kml += "<point><time>" + fFetchTimelist.get(i)
							+ "</time><accy>" + fFetchAccuracylist.get(i)
							+ "</accy><lng>" + fLnglist.get(i) + "</lng><lat>"
							+ fLatlist.get(i) + "</lat></point>";
				}

			}

		} else if (measureView.getType().equalsIgnoreCase("Auto")) {

			if (xmlvalue.equalsIgnoreCase("")) {// first
				for (int i = 0; i < fLatlist.size(); i++) {
					kml += "<point><time>" + fTimelist.get(i) + "</time><accy>"
							+ fAcclist.get(i) + "</accy><lng>"
							+ fLnglist.get(i) + "</lng><lat>" + fLatlist.get(i)
							+ "</lat></point>";
				}
			} else {
				kml = xmlvalue;
				for (int i = 0; i < fLatlist.size(); i++) {
					kml += "<point><time>" + fTimelist.get(i) + "</time><accy>"
							+ fAcclist.get(i) + "</accy><lng>"
							+ fLnglist.get(i) + "</lng><lat>" + fLatlist.get(i)
							+ "</lat></point>";
				}

			}
		} else {
			if (xmlvalue.equalsIgnoreCase("")) {// first
				for (int i = 0; i < fLatlist.size(); i++) {
					kml += "<point><time>" + mFetchTimelist.get(i) + "</time><accy>"
							+ fAcclist.get(i) + "</accy><lng>"
							+ fLnglist.get(i) + "</lng><lat>" + fLatlist.get(i)
							+ "</lat></point>";
				}
			} else {
				kml = xmlvalue;
				for (int i = 0; i < fLatlist.size(); i++) {
					kml += "<point><time>" + mFetchTimelist.get(i) + "</time><accy>"
							+ fAcclist.get(i) + "</accy><lng>"
							+ fLnglist.get(i) + "</lng><lat>" + fLatlist.get(i)
							+ "</lat></point>";
				}

			}
		}
		editor.putString("XMLVALUE", kml);
		editor.commit();
		if (measureView.getType().equalsIgnoreCase("Manual")) {
			GETXML_POJO pojo = new GETXML_POJO("Manual", kml);
			db.addXML(pojo);
		} else if (measureView.getType().equalsIgnoreCase("Auto")) {
			GETXML_POJO pojo = new GETXML_POJO("Auto", kml);
			db.addXML(pojo);
		} else {
			GETXML_POJO pojo = new GETXML_POJO("Map", kml);
			db.addXML(pojo);
		}

		// after built the xml calling dialog
		// showAlertDialog(Offline_Area.this,
		// "Do you want to measure another plot?", "AnotherPlot");
		sendCoordinatesToServer(fLatlist, fLnglist);// only one plot is going to
		// save in server. if
		// anotherplot is needed
		// uncomment the above line

	}

	@SuppressLint("SimpleDateFormat")
	protected void startMeasureArea() {

		WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()) {
			// wifi is enabled
			showAlertDialog(c, "Please disable wifi for better accuracy.",
					"Wifi");
		}

		boolean gpsEnabled = measureView.checkGps();
		if (!gpsEnabled) {

			showAlertDialog(c,
					"GPS is not enabled. Do you want to go settings menu?",
					"GPS");

		} else {
			if (measureView.getType().equalsIgnoreCase("Manual")) {
				try {
					MeasureView.measure = false;
					MeasureView.stop = false;
					reset_button.setEnabled(false);
					reset_button.setBackgroundResource(R.drawable.button_disabled);

					saveButton.setEnabled(false);
					saveButton.setBackgroundResource(R.drawable.button_disabled);

					fetchButton.setVisibility(View.GONE);
					editor.putBoolean("TIMEACCURACYCHECK", false);
					// editor.putBoolean("TIMEACCURACYCHECK", true);//replaced for
					// not checking 30sec
					editor.commit();

					if (measureView.isReadyToStart()) {
						long timeInMillis = System.currentTimeMillis();
						Calendar cal1 = Calendar.getInstance();
						cal1.setTimeInMillis(timeInMillis);

						SimpleDateFormat dateFormat1 = new SimpleDateFormat(
								"yyyy/MM/dd HH:mm");
						String currentDateandTime = dateFormat1.format(cal1.getTime());

						System.out.println(currentDateandTime);

						editor.putString("STARTTIMEVALUE", currentDateandTime);
						editor.commit();

						measureView.reset();

						measureView.clearArraylist();
						measureView.init();
						measureView.start();

						fetchButton.setVisibility(View.VISIBLE);

						reset_button.setEnabled(true);
						reset_button.setBackgroundResource(R.drawable.button_background);

						saveButton.setEnabled(true);
						saveButton.setBackgroundResource(R.drawable.button_background);

						startStopButton.setEnabled(false);
						startStopButton.setBackgroundResource(R.drawable.button_disabled);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (measureView.getType().equalsIgnoreCase("Auto")) {

				try {
					MeasureView.measure = false;
					MeasureView.stop = false;
					reset_button.setEnabled(false);
					reset_button.setBackgroundResource(R.drawable.button_disabled);

					saveButton.setEnabled(false);
					saveButton.setBackgroundResource(R.drawable.button_disabled);


					editor.putBoolean("TIMEACCURACYCHECK", false);
					// editor.putBoolean("TIMEACCURACYCHECK", true);//replaced for
					// not checking 30sec
					editor.commit();
					if (measureView.isReadyToStart()) {

						long timeInMillis1 = System.currentTimeMillis();
						Calendar cal1 = Calendar.getInstance();
						cal1.setTimeInMillis(timeInMillis1);

						SimpleDateFormat dateFormat1 = new SimpleDateFormat(
								"yyyy/MM/dd HH:mm");
						String currentDateandTime11 = dateFormat1.format(cal1
								.getTime());

						System.out.println(currentDateandTime11);

						editor.putString("STARTTIMEVALUE", currentDateandTime11);
						editor.commit();

						measureView.reset();

						measureView.clearArraylist();
						measureView.init();
						measureView.start();
						startStopButton.setText(c.getString(R.string.stop));
						measureView
								.showAlertDialog(
										c,
										"Please wait 30 seconds for getting better Accuracy.",
										"Start");
						startStopButton.postInvalidate();
						// String starttime = pref.getString("STARTTIMEVALUE","");

					} else if (measureView.isRunning()) {
						if (measureView.getType().equalsIgnoreCase("Manual")) {

							long timeInMillis = System.currentTimeMillis();
							Calendar cal1 = Calendar.getInstance();
							cal1.setTimeInMillis(timeInMillis);

							SimpleDateFormat dateFormat1 = new SimpleDateFormat(
									"yyyy/MM/dd HH:mm");
							String currentDateandTime1 = dateFormat1.format(cal1
									.getTime());

							System.out.println(currentDateandTime1);

							editor.putString("ENDTIMEVALUE", currentDateandTime1);
							editor.commit();

						} else if (measureView.getType().equalsIgnoreCase("Auto")) {

							reset_button.setEnabled(true);
							reset_button.setBackgroundResource(R.drawable.button_background);

							saveButton.setEnabled(true);
							saveButton.setBackgroundResource(R.drawable.button_background);

							long timeInMillis = System.currentTimeMillis();
							Calendar cal1 = Calendar.getInstance();
							cal1.setTimeInMillis(timeInMillis);

							SimpleDateFormat dateFormat1 = new SimpleDateFormat(
									"yyyy/MM/dd HH:mm");
							String currentDateandTime = dateFormat1.format(cal1
									.getTime());

							System.out.println(currentDateandTime);
							editor.putString("ENDTIMEVALUE", currentDateandTime);
							editor.commit();

							measureView.stop();
							startStopButton.setText(c.getString(R.string.start));
							startStopButton.postInvalidate();
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		measureView.destroy();
		super.onDestroy();
	}

	public void addAnotherPlot() {

		// Toast.makeText(c, "addAnotherPlot", Toast.LENGTH_SHORT).show();
		measureView.setTime(0);
		measureView.setaccuracy(30);
		startMeasureArea();

	}

	@SuppressLint("DefaultLocale")
	public void sendCoordinatesToServer(ArrayList<Double> fLatlist,
										ArrayList<Double> fLnglist) {

		try {
			/*WifiManager wifiManager = (WifiManager) this
					.getSystemService(Context.WIFI_SERVICE);

			if (wifiManager.isWifiEnabled()) {
				// WIFI ALREADY ENABLED.
			} else {
				// ENABLE THE WIFI
				wifiManager.setWifiEnabled(true);
			}*/

			/*Toast.makeText(c, "Coordinates Length ==> " + fLatlist.size(),
					Toast.LENGTH_LONG).show();*/

			double area = measureView.area(fLatlist, fLnglist);
			double finarea = area / 4046.85642;

			String farea = String.format("%.2f", finarea);

			editor.putString("AREAVALUE", farea);
			editor.commit();

			saveDialog(c);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	private void checkConnectionAndPlotArea() {
		try {
			if (checkInternetConnection()) {
				if (!isGooglePlayServicesAvailable()) {
					Toast.makeText(c, "Google PlayServices Not Available!", Toast.LENGTH_LONG).show();
					sendValuesToGap();

				} else {
					/*Intent inn = new Intent(Offline_Area.this,
							MapActivity.class);
					startActivity(inn);
					finish();*/
					/*showAlertDialog(c,
							"Do you want to view the measured area in map?",
							"Final");*/
				}
			} else {
				showAlertDialog(c, "No Internet! Data saved in local ",
						"Internetconnection");
				// sendValuesToGap();

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void sendValuesToGap() {

		try {
			String xmlvalue = pref.getString("XMLVALUE", "");
			String areavalue = pref.getString("AREAVALUE", "");
			String starttimevalue = pref.getString("STARTTIMEVALUE", "");
			String endtimevalue = pref.getString("ENDTIMEVALUE", "");
			String accuracyvalue = pref.getString("ACCURACYVALUE", "");
			String mType = "";

			String savelat = "0";
			String savelng = "0";
			/*locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (ActivityCompat.checkSelfPermission(
					this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
					this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
				return;
			} else {
				Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (locationGPS != null) {
					double lat = locationGPS.getLatitude();
					double longi = locationGPS.getLongitude();
					savelat = String.valueOf(lat);
					savelng = String.valueOf(longi);

				}
			}*/
			GetGPSTracker gps = new GetGPSTracker(c);
			savelat = gps.getLatitude() + "";
			savelng = gps.getLongitude() + "";
			if (savelng.equals("0") || savelng.equals("0")) {
				String data = gps.getAnyProviderLocation();
				if (data != null) {
					savelat = data.split(",")[0];
					savelng = data.split(",")[1];
				}
			}

			if (measureView.getType().equalsIgnoreCase("Manual")) {
				mType = "M";
			} else if (measureView.getType().equalsIgnoreCase("Auto")) {
				mType = "A";
			} else {
				mType = "R";
			}

			// Toast.makeText(c,
			// "areavalue"+areavalue+"starttimevalue"+starttimevalue+"endtimevalue"+endtimevalue+
			// "accuracyvalue " + accuracyvalue, Toast.LENGTH_LONG).show();
			if (xmlvalue.equalsIgnoreCase("") || areavalue.equalsIgnoreCase("")) {
				Toast.makeText(c, "No point are Plotted. Please try again",
						Toast.LENGTH_LONG).show();
				finish();
			} else {
				// todo setResult()
				Bundle bundle = new Bundle();
				bundle.putString("xmlValue", xmlvalue);
				bundle.putString("areavalue", areavalue);
				bundle.putString("starttimevalue", starttimevalue);
				bundle.putString("endtimevalue", endtimevalue);
				bundle.putString("accuracyvalue", accuracyvalue);
				bundle.putString("mType", mType);
				bundle.putString("savelat", savelat);
				bundle.putString("savelng", savelng);

				setResult(RESULT_OK, new Intent().putExtra("resultData", bundle));
				finish();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void showAlertDialog(Context context, String msg,
								final String purpose) {
		try {
			String message = msg;

			final Dialog dialog = new Dialog(context);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

			dialog.setCancelable(false);
			dialog.setContentView(R.layout.custom_dialog);

			RelativeLayout rl_action_bar = (RelativeLayout) dialog.findViewById(R.id.rl_action_bar);

			TextView txt_app_name = (TextView) dialog.findViewById(R.id.txt_app_name);
			String headText = "Alert";
			if (purpose.equalsIgnoreCase("Reset")
					|| purpose.equalsIgnoreCase("Save")) {
				headText = "Alert";
			} else {
				headText = "ICane";
			}
			txt_app_name.setText(headText);
			TextView text = (TextView) dialog.findViewById(R.id.txt_alert);
			text.setText(message);
			Button btn_Yes = (Button) dialog.findViewById(R.id.btn_yes);
			Button btn_No = (Button) dialog.findViewById(R.id.btn_no);

			if (purpose.equalsIgnoreCase("Reset")
					|| purpose.equalsIgnoreCase("GPS")
					|| purpose.equalsIgnoreCase("Final")
					|| purpose.equalsIgnoreCase("back")) {

				btn_No.setVisibility(View.VISIBLE);
				btn_Yes.setText("Yes");
				btn_No.setText("No");

			} else if (purpose.equalsIgnoreCase("AnotherPlot")) {
				btn_No.setVisibility(View.VISIBLE);
				btn_Yes.setText("Yes");
				btn_No.setText("No & Save");
			} else {
				btn_No.setVisibility(View.GONE);
				btn_Yes.setText("OK");
			}

			btn_Yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					if (purpose.equalsIgnoreCase("Reset")) {
						measureView.reset();
						// db.delete_table("getlocation");
						measureView.clearArraylist();
						if (measureView.getType().equalsIgnoreCase("Map")) {
							map.clear();
							saveButton.setEnabled(false);
							saveButton.setBackgroundResource(R.drawable.button_disabled);
							polygon1.remove();
							markerList.clear();
							map.clear();
							helperMarker = null;
							fab.setVisibility(View.GONE);
							deleteLayout.setVisibility(View.GONE);
							valueTv.setText("0.00");
							LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
							MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(latLng.latitude + " : " + latLng.longitude).icon(BitmapDescriptorFactory.fromResource(R.drawable.person_standing));
							map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
							map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
							//googleMap.getUiSettings().setScrollGesturesEnabled(false);
							map.addMarker(markerOptions);
							map.getUiSettings().setScrollGesturesEnabled(true);
							map.getUiSettings().setZoomGesturesEnabled(true);
							map.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
						}
						dialog.dismiss();
					} else if (purpose.equalsIgnoreCase("GPS")) {
						dialog.dismiss();
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);

					} else if (purpose.equalsIgnoreCase("AnotherPlot")) {
						dialog.dismiss();
						addAnotherPlot();

					} else if (purpose.equalsIgnoreCase("Final")) {


					} else if (purpose.equalsIgnoreCase("Internetconnection")) {
						dialog.dismiss();
						sendValuesToGap();

					} else if (purpose.equalsIgnoreCase("back")) {
						dialog.dismiss();
						Bundle bundle = new Bundle();
						bundle.putString("xmlValue", "aborted");
						setResult(RESULT_OK, new Intent().putExtra("resultData", bundle));
						finish();
					} else {
						dialog.dismiss();
					}
				}
			});

			btn_No.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					if (purpose.equalsIgnoreCase("AnotherPlot")) {
						// dialog.dismiss();
						// sendCoordinatesToServer();

					} else if (purpose.equalsIgnoreCase("Final")) {
						dialog.dismiss();
						sendValuesToGap();

					} else {
						dialog.dismiss();
					}

				}
			});

			dialog.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void saveDialog(Context context) {
		try {
			//String message = msg;

			final Dialog dialog = new Dialog(context);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

			dialog.setCancelable(false);
			dialog.setContentView(R.layout.custom_dialog);

			/*RelativeLayout rl_action_bar = (RelativeLayout) dialog
					.findViewById(R.id.rl_action_bar);

			TextView txt_app_name = (TextView) dialog
					.findViewById(R.id.txt_app_name);*/

			TextView text = (TextView) dialog.findViewById(R.id.txt_alert);
			String areavalue = pref.getString("AREAVALUE", "");
			text.setText("Are you sure? Do you want to save the area? - " + areavalue + " acre");
			Button btn_Yes = (Button) dialog.findViewById(R.id.btn_yes);
			Button btn_No = (Button) dialog.findViewById(R.id.btn_no);
			btn_No.setVisibility(View.VISIBLE);
			//		txt_app_name.setText(headText);
			/*TextView text = (TextView) dialog.findViewById(R.id.txt_alert);
			text.setText(message);*/
			/*Button btn_ViewMap = (Button) dialog.findViewById(R.id.btn_viewmap);
			Button btn_Save = (Button) dialog.findViewById(R.id.btn_save);
			Button btn_close = (Button) dialog.findViewById(R.id.btn_close);*/
			//btn_ViewMap.setVisibility(View.GONE);

			btn_No.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					db.delete_table("getlocation");
					dialog.dismiss();
				}
			});
			/*btn_ViewMap.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					checkConnectionAndPlotArea();
				}
			});*/

			btn_Yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					sendValuesToGap();

				}
			});

			dialog.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public boolean checkInternetConnection() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// test for connection
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
			return true;
		} else {
			return false;
		}

	}

	private boolean isGooglePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == status) {
			return true;
		} else {
			GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
			return false;
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		showAlertDialog(c, "Are you sure? Do you want to go back?", "back");

	}

	@Override
	protected void onResume() {
		measureView.setTime(0);
		measureView.setaccuracy(80);

		super.onResume();
	}

	private void fetchLocation() {
		if (ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
			return;
		}
		Task<Location> task = fusedLocationProviderClient.getLastLocation();
		task.addOnSuccessListener(new OnSuccessListener<Location>() {
			@Override
			public void onSuccess(Location location) {
				if (location != null) {
					currentLocation = location;
					measureView.reset();
					measureView.clearArraylist();
					Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
					MapFragment supportMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.myMap);
					assert supportMapFragment != null;
					mapView = supportMapFragment.getView();
					supportMapFragment.getMapAsync(Offline_Area.this);
				}
			}
		});
	}

	void updateValueText() {
		ArrayList<Double> fLatlist = new ArrayList();
		ArrayList<Double> fLnglist = new ArrayList();

		for(MarkerOptions pos : markerList) {
			fLatlist.add(pos.getPosition().latitude);
			fLnglist.add(pos.getPosition().longitude);
		}

		double area = measureView.area(fLatlist, fLnglist);
		double finarea = area / 4046.85642;

		String farea = String.format("%.2f", finarea);

		//double val = SphericalUtil.computeArea(markerList);
		valueTv.setText(farea);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		map.setMyLocationEnabled(true);
		if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }

		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		long timeInMillis = System.currentTimeMillis();
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(timeInMillis);

		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String currentDateandTime = dateFormat1.format(cal1.getTime());

		System.out.println(currentDateandTime);

		editor.putString("STARTTIMEVALUE", currentDateandTime);
		editor.commit();
		int accyval = (int)Math.round(currentLocation.getAccuracy());
		editor.putString("ACCURACYVALUE",String.valueOf(accyval));
		editor.commit();

		setInitialMap();
		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 18));

		map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				if(helperMarker == null) {
					MarkerOptions marker = new MarkerOptions();
					marker.position(latLng);
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.square));
					marker.draggable(true);
					marker.title("Real|"+latLng.latitude+":"+ latLng.longitude);
					markerList.add(marker);
					reset_button.setBackgroundResource(R.drawable.button_background);
					if(markerList.size() >= 3) {
						saveButton.setEnabled(true);
						saveButton.setBackgroundResource(R.drawable.button_background);
					} else {
						saveButton.setEnabled(false);
						saveButton.setBackgroundResource(R.drawable.button_disabled);
					}
					measureView.importFromMap(latLng.latitude, latLng.longitude, (int)Math.round(currentLocation.getAccuracy()),-1, false);
				} else {
					helperMarker = null;
					fab.setVisibility(View.GONE);
					deleteLayout.setVisibility(View.GONE);
					map.getUiSettings().setScrollGesturesEnabled(true);
					map.getUiSettings().setZoomGesturesEnabled(true);
					map.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
					//deleteBtn.performClick();
			  }

				drawPolygon();

				// ArrayList<Double> mLatlist = measureView.getMapLatList();
				// ArrayList<Double> mLnglist = measureView.getMapLngList();
				// ArrayList<LatLng> coordList1 = new ArrayList<LatLng>();

				// MarkerOptions markerOptions = new MarkerOptions();
				// markerOptions.position(latLng);
				// markerOptions.title(String.valueOf(mLatlist.size()-1));
				// markerOptions.draggable(true);
				// //markerOptions.title(latLng.latitude + " : " + latLng.longitude+ " : " +currentLocation.getAccuracy());
				// map.addMarker(markerOptions);

				// if(mLatlist.size() > 0) {
				// 	//finpolyline.remove();
				// 	for (int i = 0; i < mLatlist.size(); i++) {
				// 		coordList1.add(new LatLng(mLatlist.get(i), mLnglist.get(i)));
				// 	}
				// 	PolylineOptions polylineOptions = new PolylineOptions();
				// 	polylineOptions.addAll(coordList1);

				// 	polylineOptions.width(5).color(Color.RED);
				// 	finpolyline = map.addPolyline(polylineOptions);
				// } 
				// if(mLatlist.size() >= 3) {
				// 	closeButton.performClick();
				// }
			}
		});

		map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker arg0) {
				if(!((currentLocation.getLatitude()+":"+currentLocation.getLongitude()).equals(arg0.getPosition().latitude+":"+ arg0.getPosition().longitude))) {
					Projection projection = map.getProjection();
					LatLng markerLocation = arg0.getPosition();
					Point screenPosition = projection.toScreenLocation(markerLocation);
					/*int xVal = screenPosition.x - 30;
					int yVal = screenPosition.y - 50;*/
                    int xVal = screenPosition.x;
                    int yVal = screenPosition.y;
					fab.animate().x(xVal).y(yVal).setDuration(0).start();
					helperMarker = new MarkerOptions();
					helperMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.square));
					helperMarker.alpha(0.8f);
					helperMarker.draggable(true);
					helperMarker.position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude));
					helperMarker.title(arg0.getTitle());
					if(tempMarker != null) {
						drawPolygon();
					}
					tempMarker = map.addMarker(helperMarker);
					deleteLayout.setVisibility(View.VISIBLE);

					map.getUiSettings().setScrollGesturesEnabled(false);
					map.getUiSettings().setZoomGesturesEnabled(false);
					map.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);

					fab.setVisibility(View.VISIBLE);
					//drawPolygon();
					/*	helperMarker = new MarkerOptions();
						helperMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
						helperMarker.alpha(0.8f);
						//helperMarker.anchor(0.5f, 0.0f);
						helperMarker.draggable(true);
						helperMarker.position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude));
						helperMarker.title(arg0.getTitle());
						map.addMarker(helperMarker);
						deleteLayout.setVisibility(View.VISIBLE);
						map.getUiSettings().setScrollGesturesEnabled(false);
						map.getUiSettings().setZoomGesturesEnabled(false);
						map.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);*/
					}
				return true;
			}
		});
		fab.setOnDragListener(new View.OnDragListener() {
			@Override
			public boolean onDrag(View view, DragEvent dragEvent) {
				return false;
			}
		});

		map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
			@Override
			public void onMarkerDragStart(Marker arg0) {
				// TODO Auto-generated method stub
				map.getUiSettings().setScrollGesturesEnabled(false);
				map.getUiSettings().setZoomGesturesEnabled(false);
				map.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onMarkerDragEnd(Marker arg0) {
				//LatLng latLng = new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude);
				if(arg0.getTitle().split("\\|")[0].equals("Real")) {
					for(int i = 0 ; i < markerList.size() ; i++) {
						if(markerList.get(i).getTitle().equals(arg0.getTitle())) {
							markerList.remove(i);
							MarkerOptions marker = new MarkerOptions();
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.square));
							marker.position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude));
							marker.draggable(true);
							marker.title("Real|" + arg0.getPosition().latitude+":"+ arg0.getPosition().longitude);
							markerList.add(i, marker);
							measureView.importFromMap(arg0.getPosition().latitude, arg0.getPosition().longitude, (int)Math.round(currentLocation.getAccuracy()), i, true);
						}
					}
				} else {
					MarkerOptions marker = new MarkerOptions();
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.square));
					marker.position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude));
					marker.draggable(true);
					marker.title("Real|" + arg0.getPosition().latitude+":"+ arg0.getPosition().longitude);
					int pos = Integer.parseInt(arg0.getTitle().split("\\|")[1]);
					markerList.add(pos + 1, marker);
					measureView.importFromMap(arg0.getPosition().latitude, arg0.getPosition().longitude, (int)Math.round(currentLocation.getAccuracy()),pos + 1, false);

				}

				//map.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
				drawPolygon();
				map.getUiSettings().setScrollGesturesEnabled(true);
				map.getUiSettings().setZoomGesturesEnabled(true);
				map.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
				/*// TODO Auto-generated method stub
				Log.d("System out", "onMarkerDragEnd..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
				Toast.makeText(Offline_Area.this,"onMarkerDragEnd = "+arg0.getTitle()+" = "+arg0.getPosition().latitude,Toast.LENGTH_SHORT).show();
				map.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));

				measureView.importFromMap(arg0.getPosition().latitude,arg0.getPosition().longitude,0,Integer.parseInt(arg0.getTitle()));
				ArrayList<Double> mLatlist = measureView.getMapLatList();
				ArrayList<Double> mLnglist = measureView.getMapLngList();
				ArrayList<LatLng> coordList1 = new ArrayList<LatLng>();
				if(mLatlist.size()>0){
					//finpolyline.remove();
					map.clear();
					LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
					MarkerOptions markerval = new MarkerOptions().position(latLng).title(latLng.latitude + " : " + latLng.longitude).icon(BitmapDescriptorFactory.fromResource(R.drawable.person));
					map.addMarker(markerval);
					for (int i = 0; i < mLatlist.size(); i++) {
						MarkerOptions markerOptions = new MarkerOptions();
						markerOptions.position(new LatLng(mLatlist.get(i), mLnglist.get(i)));
						markerOptions.title(String.valueOf(i));
						markerOptions.draggable(true);
						map.addMarker(markerOptions);
						coordList1.add(new LatLng(mLatlist.get(i), mLnglist.get(i)));
					}
					PolylineOptions polylineOptions = new PolylineOptions();
					polylineOptions.addAll(coordList1);

					polylineOptions.width(5).color(Color.RED);
					finpolyline = map.addPolyline(polylineOptions);
				}*/
			}

			@Override
			public void onMarkerDrag(Marker arg0) {
				// TODO Auto-generated method stub
				Log.d("System out", "onMarkerDrag..."+arg0.getPosition());
			}
		});
	}

	public LatLng midPoint(double lat1,double lon1,double lat2,double lon2){

		double dLon = Math.toRadians(lon2 - lon1);

		//convert to radians
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		lon1 = Math.toRadians(lon1);

		double Bx = Math.cos(lat2) * Math.cos(dLon);
		double By = Math.cos(lat2) * Math.sin(dLon);
		double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
		double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

		//print out in degrees
		System.out.println(Math.toDegrees(lat3) + " " + Math.toDegrees(lon3));
		return new LatLng(Math.toDegrees(lat3), Math.toDegrees(lon3));
	}

	private void setInitialMap() {
		map.clear();

		LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
		MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(latLng.latitude + " : " + latLng.longitude).icon(BitmapDescriptorFactory.fromResource(R.drawable.person_standing));
		//googleMap.getUiSettings().setScrollGesturesEnabled(false);
		map.addMarker(markerOptions);
		deleteLayout.setVisibility(View.GONE);
	}

	private void drawPolygon() {
		setInitialMap();

		if(markerList.size() > 0) {
			if(polygon1 != null) {
				polygon1.remove();
			}
			ArrayList<MarkerOptions> tempList1 = new ArrayList<>();
			tempList1.addAll(markerList);
			if(markerList.size() > 2) {
				ArrayList<MarkerOptions> tempList = new ArrayList<>();
				int i = 0, j = 0;

				while(i < markerList.size()) {
					LatLng latLng = null;
					MarkerOptions newMarker = new MarkerOptions();
					newMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.helper_marker));
					newMarker.draggable(true);

					tempList.add(markerList.get(i));
					//	j = (i == 0 ? 0 : i + 1);
					if(markerList.get(i).getTitle().split("\\|")[0].equals("Real")) {
						if(i == markerList.size() - 1) {
							double newLat = (markerList.get(i).getPosition().latitude + markerList.get(0).getPosition().latitude) / 2;
							double newLong = (markerList.get(i).getPosition().longitude + markerList.get(0).getPosition().longitude) / 2;
							latLng = new LatLng(newLat, newLong);
							newMarker.position(latLng);
							newMarker.title("helper|" + i +"|"+ latLng.latitude+":"+ latLng.longitude);
							tempList.add(newMarker);
						} else {
							double newLat = (markerList.get(i).getPosition().latitude + markerList.get(i + 1).getPosition().latitude) / 2;
							double newLong = (markerList.get(i).getPosition().longitude + markerList.get(i + 1).getPosition().longitude) / 2;
							latLng = new LatLng(newLat, newLong);
							newMarker.position(latLng);
							newMarker.title("helper|" + i + "|" + latLng.latitude+":"+ latLng.longitude);
							tempList.add(newMarker);
						}
					}

					System.out.println();
					i++;
				}
				tempList1.clear();
				tempList1.addAll(tempList);
				System.out.println();
			}

			//map.clear();
			ArrayList<LatLng> latLngs = new ArrayList<>();
			for(MarkerOptions marker : tempList1) {
				map.addMarker(marker);
				latLngs.add(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
			}


			polygon1 = map.addPolygon(new PolygonOptions()
					.clickable(true)
					.addAll(latLngs));

			polygon1.setFillColor(0x4D00FF00);
			polygon1.setStrokeColor(Color.RED);
			updateValueText();
		}
	}

	/*@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				fetchLocation();
			}
		}
	}*/

}
