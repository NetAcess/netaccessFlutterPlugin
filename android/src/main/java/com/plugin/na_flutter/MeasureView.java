package com.plugin.na_flutter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Vector;


public class MeasureView extends View {
    Context c;

    public static final int LENGTH_UNITS_AUTO_METRIC = 0;
    public static final int LENGTH_UNITS_AUTO_IMPERIAL = 1;
    public static final int LENGTH_UNITS_METER = 2;
    public static final int LENGTH_UNITS_KILOMETER = 3;
    public static final int LENGTH_UNITS_FOOT = 4;
    public static final int LENGTH_UNITS_YARD = 5;
    public static final int LENGTH_UNITS_CHAIN = 6;
    public static final int LENGTH_UNITS_MILE = 7;
    private int lengthUnits;
    public static final int AREA_UNITS_AUTO_METRIC = 0;
    public static final int AREA_UNITS_AUTO_IMPERIAL = 1;
    public static final int AREA_UNITS_SQUARE_METER = 2;
    public static final int AREA_UNITS_HECTARE = 3;
    public static final int AREA_UNITS_SQUARE_KILOMETER = 4;
    public static final int AREA_UNITS_SQUARE_FOOT = 5;
    public static final int AREA_UNITS_SQUARE_YARD = 6;
    public static final int AREA_UNITS_ACRE = 7;
    public static final int AREA_UNITS_SQUARE_MILE = 8;
    private int areaUnits;

    public static final double FOOT_IN_METER = 0.3048;
    public static final double YARD_IN_METER = 0.9144;
    public static final double CHAIN_IN_METER = 20.1168;
    public static final double MILE_IN_METER = 1609.344;
    public static final double ACRE_IN_SQUARE_METER = 4840 * YARD_IN_METER * YARD_IN_METER;

    public static final int MODE_MEASURE = 0;
    public static final int MODE_VIEW = 1;
    public static int mode, loggingMode, acuracy, time;
    public static String type;

    public static final int LOGGING_MODE_AUTO = 0;
    public static final int LOGGING_MODE_MANUAL = 1;

    public static boolean measure = false;
    public static boolean stop = false;

    private Polygon polygon;
    Coordinate origo;
    private double rotation;
    private double zoom;

    private Paint backgroundPaint;
    private Paint linePaint;
    private Paint closingLinePaint;
    private Paint textPaint;
    private Paint gpsOnPaint;
    private Paint gpsOffPaint;
    private Paint gpsViewPaint;
    private Paint positionPaint;
    private Paint translatedByPaint;
    private Paint loggingLinePaint;

    LocationManager locationManager;
    GPSLocationListener gps;

    Offline_Area offlineArea;

    SensorManager sensorManager;
    SensorEventListener sensorEventListener;
    ArrayList<Double> list_lat, list_lng, fetch_list_lat, fetch_list_lng, maplist_lat, maplist_lng;
    ArrayList<LatLng> list_latlng, fetch_list_latlng;


    ArrayList<String> list_Time, list_Count, list_fTime, maplist_fTime;
    ArrayList<Integer> list_Accuracy, list_finaccuracy, fetch_list_accuracy, maplist_Accuracy;
    ArrayList<String> list_fetch_time;


    //for getting time accuracy check (30 secs)
    public static long TIME_INTERVAL = 30000;

    SharedPreferences pref;
    Editor editor;
    DBHandler db;

    private static double longitude = 0;
    private static double latitude = 0;
    private static double altitude = 0;
    private static int accuracy = 0;

    /*private static double getTime = 0;
    private static String getType = "Auto";*/
    private class GPSLocationListener implements LocationListener {
        private Context context;
        private View view;
        private int currentStatus = LocationProvider.OUT_OF_SERVICE;
        private int numSatellites = 0;

        private boolean hasRecievedSignal;
        private boolean isStarted;

        private static final int radiusEarth = 6378137;

        private double flatXUnitX;
        private double flatXUnitY;
        private double flatXUnitZ;
        private double flatYUnitX;
        private double flatYUnitY;
        private double flatYUnitZ;
        private boolean origoIsFixed = false;

        public Coordinate coordinate;

        public GPSLocationListener(Context sContext, View v) {
            context = sContext;
            view = v;

            coordinate = new Coordinate(0, 0);

            origoIsFixed = false;
            hasRecievedSignal = false;
            isStarted = false;
        }

        public void start() {
            isStarted = true;
        }

        public void reset() {
            origoIsFixed = false;
        }

        public boolean isActive() {
            return hasRecievedSignal;
        }

        public void onLocationChanged(Location location) {
            hasRecievedSignal = true;
            //Toast.makeText(getContext(), "In_Time : "+getTime()+"In_Accuracy : "+getaccuracy(), Toast.LENGTH_SHORT).show();
            if (mode == MODE_MEASURE /*&& (isStarted || loggingMode == LOGGING_MODE_MANUAL)*/) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                altitude = location.getAltitude();
                accuracy = (int) location.getAccuracy();
                //getTime = location.getTime();
                //Toast.makeText(getContext(),"Accuracy : "+accuracy, Toast.LENGTH_SHORT).show();
                double x = (radiusEarth + altitude) * Math.cos(latitude * Math.PI / 180) * Math.cos(longitude * Math.PI / 180);
                double y = (radiusEarth + altitude) * Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180);
                double z = (radiusEarth + altitude) * Math.sin(latitude * Math.PI / 180);

                if (!origoIsFixed) {
                    flatXUnitX = -y / Math.sqrt(x * x + y * y);
                    flatXUnitY = x / Math.sqrt(x * x + y * y);
                    flatXUnitZ = 0;
                    flatYUnitX = (y * flatXUnitZ - z * flatXUnitY);
                    flatYUnitY = (z * flatXUnitX - x * flatXUnitZ);
                    flatYUnitZ = (x * flatXUnitY - y * flatXUnitX);
                    double length = Math.sqrt(flatYUnitX * flatYUnitX + flatYUnitY * flatYUnitY + flatYUnitZ * flatYUnitZ);
                    flatYUnitX = flatYUnitX / length;
                    flatYUnitY = flatYUnitY / length;
                    flatYUnitZ = flatYUnitZ / length;
                }

                double spaceX = (radiusEarth + altitude) * Math.cos(latitude * Math.PI / 180) * Math.cos(longitude * Math.PI / 180);
                double spaceY = (radiusEarth + altitude) * Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180);
                double spaceZ = (radiusEarth + altitude) * Math.sin(latitude * Math.PI / 180);

                coordinate.x = spaceX * flatXUnitX + spaceY * flatXUnitY + spaceZ * flatXUnitZ;
                coordinate.y = spaceX * flatYUnitX + spaceY * flatYUnitY + spaceZ * flatYUnitZ;
                Log.d("acc : ", "" + location.getAccuracy() + "getacc: " + getaccuracy());
                if (location.hasAccuracy() && (int) location.getAccuracy() <= getaccuracy()) {
                    //Log.d("loggingMode: ",""+loggingMode+" isStarted : "+isStarted+ " LOGGING_MODE_AUTO :"+ LOGGING_MODE_AUTO);
                    if (loggingMode == LOGGING_MODE_AUTO && isStarted) {
                        boolean CHECK = pref.getBoolean("TIMEACCURACYCHECK", false);

                        Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR);
                        int minute = c.get(Calendar.MINUTE);
                        int seconds = c.get(Calendar.SECOND);
                        String time = String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(seconds);
                        if (CHECK) {
                            int tacc = (int) accuracy;
                            list_finaccuracy.add(tacc);
                            list_fTime.add(time);
                            list_lat.add(latitude);
                            list_lng.add(longitude);
                            list_latlng.add(new LatLng(latitude, longitude));
                            ((MeasureView) view).importFromGPS();
                            //	Toast.makeText(getContext(), "Time : "+getTime()+"Accuracy : "+getaccuracy(), Toast.LENGTH_SHORT).show();
                        } else {
                            int tempacc = accuracy;
                            Log.d("initial accuracy : ", "" + tempacc);
                            list_Accuracy.add(tempacc);
                            list_Time.add(time);

                        }
                    }
                }
                setOrigo(coordinate.x, coordinate.y);

                view.postInvalidate();

            }
        }

        public void onProviderDisabled(String provider) {
            if (gps.isStarted) {
                showAlertDialog(getContext(), "Gps is Disabled! ,Please enable and continue to measure.. ", "Gps Disabled");
            }

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            currentStatus = status;
            numSatellites = extras.getInt("satellites");
        }

        public int getStatus() {
            return currentStatus;
        }

        public int getNumSatellites() {
            return numSatellites;
        }

        public void ensureOrigoIsFixed() {
            if (!origoIsFixed) {
                polygon.setOrigo(origo);
                origoIsFixed = true;
            }
        }
    }

    private double directionInRadians = 0;

    private class CompassSensorEventListener implements SensorEventListener {
        View view;

        private static final int AVERAGING_INTERVAL = 10;
        private int magneticFieldCounter = 0;
        private Vector<Vector<Float>> magneticFieldValues;

        public CompassSensorEventListener(View v) {
            magneticFieldValues = new Vector<Vector<Float>>();
            magneticFieldValues.add(new Vector<Float>());
            magneticFieldValues.add(new Vector<Float>());
            magneticFieldValues.add(new Vector<Float>());
            for (int n = 0; n < AVERAGING_INTERVAL; n++) {
                magneticFieldValues.elementAt(0).add(new Float(0));
                magneticFieldValues.elementAt(1).add(new Float(0));
                magneticFieldValues.elementAt(2).add(new Float(0));
            }

            view = v;
        }

        public void onSensorChanged(SensorEvent event) {
            magneticFieldValues.elementAt(0).setElementAt(new Float(event.values[0]), magneticFieldCounter);
            magneticFieldValues.elementAt(1).setElementAt(new Float(event.values[1]), magneticFieldCounter);
            magneticFieldValues.elementAt(2).setElementAt(new Float(event.values[2]), magneticFieldCounter);
            if (++magneticFieldCounter == AVERAGING_INTERVAL)
                magneticFieldCounter = 0;
            float mx = 0;
            float my = 0;
            float mz = 0;
            for (int n = 0; n < AVERAGING_INTERVAL; n++) {
                mx += magneticFieldValues.elementAt(0).elementAt(n);
                my += magneticFieldValues.elementAt(1).elementAt(n);
                mz += magneticFieldValues.elementAt(2).elementAt(n);
            }

            mx /= AVERAGING_INTERVAL;
            my /= AVERAGING_INTERVAL;
            mz /= AVERAGING_INTERVAL;

            directionInRadians = Math.acos(my / Math.sqrt(mx * mx + my * my));
            if (mx < 0)
                directionInRadians *= -1;

            setRotation(-directionInRadians);
            view.postInvalidate();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }


    public MeasureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        c = context;

        origo = new Coordinate(0, 0);

        setPolygon(new Polygon());

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFFFFFFF);
        float stroke = Float.valueOf(getResources().getString(R.string.storke_width));
        linePaint = new Paint();
        linePaint.setColor(0xFF000000);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Style.STROKE);
        linePaint.setStrokeWidth(stroke);

        closingLinePaint = new Paint();
        closingLinePaint.setColor(0x44015993);
        closingLinePaint.setAntiAlias(true);
        closingLinePaint.setStyle(Style.STROKE);
        closingLinePaint.setStrokeWidth(stroke);

        textPaint = new Paint();
        textPaint.setColor(0xDDFFFFFF);
        float scaledSize = getResources().getDimensionPixelSize(R.dimen.myFontSize);
        textPaint.setTextSize(scaledSize);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        gpsOnPaint = new Paint();
        gpsOnPaint.setColor(0xDD079662);
        gpsOnPaint.setAntiAlias(true);
        gpsOnPaint.setTextSize(scaledSize);

        gpsOffPaint = new Paint();
        gpsOffPaint.setColor(0xDDb50800);
        gpsOffPaint.setAntiAlias(true);
        gpsOffPaint.setTextSize(scaledSize);

        gpsViewPaint = new Paint();
        gpsViewPaint.setColor(0xDD0000FF);
        gpsViewPaint.setAntiAlias(true);
        gpsViewPaint.setTextSize(20);

        positionPaint = new Paint();
        positionPaint.setColor(0x88FF0000);
        positionPaint.setAntiAlias(true);
        positionPaint.setStyle(Style.STROKE);
        positionPaint.setStrokeWidth(stroke);

        translatedByPaint = new Paint();
        translatedByPaint.setColor(0x44888888);
        translatedByPaint.setAntiAlias(true);

        loggingLinePaint = new Paint();
        loggingLinePaint.setColor(0xFF00FF00);
        loggingLinePaint.setAntiAlias(true);

        zoom = 1;

        init();
    }

    public void setPolygon(Polygon sPolygon) {
        polygon = sPolygon;
        if (polygon.getSize() > 0)
            setZoom(1 / polygon.getSize());
        else
            setZoom(1);
        origo = polygon.getOrigo();
    }

    public Polygon getPolygon() {
        return polygon;
    }


    public float convertRelativeToView(double x) {
        double scale = Math.max(getWidth() / 2, getHeight() / 2);

        return (float) (x * zoom * scale);
    }

    public double convertViewToRelative(float x) {
        double scale = Math.min(getWidth() / 2, getHeight() / 2);

        return x / (zoom * scale);
    }

    protected float calculateInViewPositionX(double x, double y) {
        double scale = Math.min(getWidth() / 2, getHeight() / 2);

        float rotatedX = (float) (Math.cos(rotation) * (x - origo.x) - Math.sin(rotation) * (y - origo.y));
        float rotatedY = (float) (Math.sin(rotation) * (x - origo.x) + Math.cos(rotation) * (y - origo.y));
        return (float) (getWidth() / 2 + zoom * scale * rotatedX);
    }

    protected float calculateInViewPositionY(double x, double y) {
        double scale = Math.min(getWidth() / 2, getHeight() / 2);

        float rotatedX = (float) (Math.cos(rotation) * (x - origo.x) - Math.sin(rotation) * (y - origo.y));
        float rotatedY = (float) (Math.sin(rotation) * (x - origo.x) + Math.cos(rotation) * (y - origo.y));
        return (float) (getHeight() / 2 - zoom * scale * rotatedY);
    }

    protected double calculateRelativePositionX(int x, int y) {
        double scale = Math.min(getWidth() / 2, getHeight() / 2);

        double nonBackRotatedX = (x - getWidth() / 2) / (scale * zoom);
        double nonBackRotatedY = (y - getHeight() / 2) / (scale * zoom);
        return Math.cos(-rotation) * nonBackRotatedX - Math.sin(-rotation) * nonBackRotatedY + origo.x;
    }

    protected double calculateRelativePositionY(int x, int y) {
        double scale = Math.min(getWidth() / 2, getHeight() / 2);

        double nonBackRotatedX = (x - getWidth() / 2) / (scale * zoom);
        double nonBackRotatedY = (-y + getHeight() / 2) / (scale * zoom);
        return Math.sin(-rotation) * nonBackRotatedX + Math.cos(-rotation) * nonBackRotatedY + origo.y;
    }

    public void moveOrigo(double moveX, double moveY) {
        origo.x -= moveX;
        origo.y -= moveY;
        postInvalidate();
    }

    public void setOrigo(double setX, double setY) {
        origo.x = setX;
        origo.y = setY;
    }

    public void setZoom(double sZoom) {
        zoom = sZoom;
    }

    public double getZoom() {
        return zoom;
    }

    public void setRotation(double sRotation) {
        rotation = sRotation;
    }

    @SuppressWarnings("deprecation")
    public void init() {
        pref = getContext().getSharedPreferences("MyPref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        db = new DBHandler(getContext(), null, null, 1);
        list_lat = new ArrayList<Double>();
        list_lng = new ArrayList<Double>();
        list_latlng = new ArrayList<LatLng>();
        fetch_list_lat = new ArrayList<Double>();
        fetch_list_lng = new ArrayList<Double>();
        fetch_list_accuracy = new ArrayList<Integer>();
        list_fetch_time = new ArrayList<String>();
        fetch_list_latlng = new ArrayList<LatLng>();


        list_Accuracy = new ArrayList<Integer>();
        list_finaccuracy = new ArrayList<Integer>();
        list_Time = new ArrayList<String>();
        list_fTime = new ArrayList<String>();

        maplist_lat = new ArrayList<Double>();
        maplist_lng = new ArrayList<Double>();
        maplist_Accuracy = new ArrayList<Integer>();
        maplist_fTime = new ArrayList<String>();
        setTime(0);
        setaccuracy(80);

        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new CompassSensorEventListener(this);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        gps = new GPSLocationListener(getContext(), this);
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gps);
            //Toast.makeText(getContext(),"Inside Init - "+ "Time : "+getTime()+"Accuracy : "+getaccuracy(),Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mode = MODE_MEASURE;
        if (type != null && type.equalsIgnoreCase("Manual")) {
            setLoggingMode(LOGGING_MODE_MANUAL);
        } else if (type != null && type.equalsIgnoreCase("Auto")) {
            setLoggingMode(LOGGING_MODE_AUTO);
        }

    }

    public void destroy() {
        sensorManager.unregisterListener(sensorEventListener);
        locationManager.removeUpdates(gps);
    }

    private int numPointsToPlot = 1000;

    public void increaseResolution() {
        numPointsToPlot *= 10;
    }

    public void decreaseResolution() {
        if (numPointsToPlot > 10)
            numPointsToPlot /= 10;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawPaint(backgroundPaint);

        Paint p = new Paint();
        //p.setColor(0xDD1F272F);
        //canvas.drawColor(0xDD1F272F);

        /*p.setColor(0xDD1F272F);
        p.setStrokeWidth(10);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(100, 100, 200, 200, p);*/
        int dist_back_height = Integer.parseInt(getResources().getString(R.string.dist_back_height));
        Rect rect = new Rect(0, 0, getWidth(), dist_back_height);
        p.setColor(0xFF323233);
        canvas.drawRect(rect, p);
        //Rect rect = new Rect(0,0,3,3);

        //RectF rectF = new RectF(rect);


        // canvas.drawRoundRect( rectF, 1,1, p);


        Polygon figure = getPolygon();

        //Draw lines and vertice markers
        int plotPointStep = 1;
        int temp = figure.getNumVertices();
        temp /= numPointsToPlot;
        while (temp != 0) {
            plotPointStep *= 10;
            temp /= 10;
        }
        for (int n = 0; n < figure.getNumVertices(); n += plotPointStep) {
            float thisVertexX = calculateInViewPositionX(figure.getVertexCoordinateX(n), figure.getVertexCoordinateY(n));
            float thisVertexY = calculateInViewPositionY(figure.getVertexCoordinateX(n), figure.getVertexCoordinateY(n));
            float nextVertexX;
            float nextVertexY;

            if (n < figure.getNumVertices() - plotPointStep) {
                nextVertexX = calculateInViewPositionX(figure.getVertexCoordinateX(n + plotPointStep), figure.getVertexCoordinateY(n + plotPointStep));
                nextVertexY = calculateInViewPositionY(figure.getVertexCoordinateX(n + plotPointStep), figure.getVertexCoordinateY(n + plotPointStep));
            } else {
                nextVertexX = calculateInViewPositionX(figure.getVertexCoordinateX(0), figure.getVertexCoordinateY(0));
                nextVertexY = calculateInViewPositionY(figure.getVertexCoordinateX(0), figure.getVertexCoordinateY(0));
            }
            if (n < figure.getNumVertices() - plotPointStep)
                canvas.drawLine(thisVertexX, thisVertexY, nextVertexX, nextVertexY, linePaint);
            else
                canvas.drawLine(thisVertexX, thisVertexY, nextVertexX, nextVertexY, closingLinePaint);
        }
        if (loggingMode == LOGGING_MODE_MANUAL && figure.getNumVertices() == 1)
            canvas.drawLine(calculateInViewPositionX(figure.getVertexCoordinateX(0), figure.getVertexCoordinateY(0)),
                    calculateInViewPositionY(figure.getVertexCoordinateX(0), figure.getVertexCoordinateY(0)),
                    calculateInViewPositionX(gps.coordinate.x, gps.coordinate.y), calculateInViewPositionY(gps.coordinate.x, gps.coordinate.y), loggingLinePaint);

        Coordinate coord = gps.coordinate;
        if (mode == MODE_MEASURE) {
            canvas.drawCircle((float) (calculateInViewPositionX(coord.x, coord.y)), (float) (calculateInViewPositionY(coord.x, coord.y)), 5, positionPaint);
            canvas.drawCircle((float) (calculateInViewPositionX(coord.x, coord.y)), (float) (calculateInViewPositionY(coord.x, coord.y)), 1, positionPaint);
        }
        float x_dist = Float.valueOf(getResources().getString(R.string.x_dist));
        float y_dist = Float.valueOf(getResources().getString(R.string.y_dist));
        float x_dist_val = Float.valueOf(getResources().getString(R.string.x_dist_val));
        float y_dist_val = Float.valueOf(getResources().getString(R.string.y_dist_val));
        float x_area = Float.valueOf(getResources().getString(R.string.x_area));
        float y_area = Float.valueOf(getResources().getString(R.string.y_area));
        float x_area_val = Float.valueOf(getResources().getString(R.string.x_area_val));
        float y_area_val = Float.valueOf(getResources().getString(R.string.y_area_val));

        float x_signal = Float.valueOf(getResources().getString(R.string.x_signal));
        float y_signal = Float.valueOf(getResources().getString(R.string.y_signal));
        float x_signal_icon = Float.valueOf(getResources().getString(R.string.x_signal_icon));
        float y_signal_icon = Float.valueOf(getResources().getString(R.string.y_signal_icon));

        canvas.drawText(c.getString(R.string.distance), x_dist, y_dist, textPaint);
        canvas.drawText(c.getString(R.string.area), x_area, y_area, textPaint);
        canvas.drawText("" + Math.round(100 * getLength()) / (double) 100 + " " + getLengthUnit(), /*105*/x_dist_val, y_dist_val, textPaint);
        canvas.drawText("" + Math.round(100 * getArea()) / (double) 100 + " " + getAreaUnit(), /*105*/x_area_val, y_area_val, textPaint);

        if (mode == MODE_MEASURE) {
            if (gps.isStarted && loggingMode == LOGGING_MODE_AUTO) {
                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gps_on);
                //canvas.drawBitmap(bitmap, x_signal_icon, y_signal_icon, null);
                //canvas.drawCircle(20, 20, 10, gpsOnPaint);
                if (measure) {
                    canvas.drawText(c.getString(R.string.running), x_signal, y_signal, gpsOnPaint);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gps_on);
                    canvas.drawBitmap(bitmap, x_signal_icon, y_signal_icon, null);
                    canvas.drawText(c.getString(R.string.check_accuracy), x_signal, y_signal, gpsOnPaint);
                }
            } else if (gps.isActive()) {
                if (loggingMode == LOGGING_MODE_AUTO) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gps_on);
                    canvas.drawBitmap(bitmap, x_signal_icon, y_signal_icon, null);
                    if (stop) {
                        canvas.drawText(c.getString(R.string.stopped), x_signal, y_signal, gpsOnPaint);
                    } else {

                        //canvas.drawCircle(20, 20, 10, gpsOnPaint);

                        canvas.drawText(c.getString(R.string.ready), x_signal, y_signal, gpsOnPaint);
				/*offlineArea = new Offline_Area();
				offlineArea.buttonVisiblity();*/
                    }
                }

            } else {
                if (loggingMode == LOGGING_MODE_AUTO) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gps_off);
                    canvas.drawBitmap(bitmap, x_signal_icon, y_signal_icon, null);
                    //canvas.drawCircle(20, 20, 10, gpsOffPaint);
                    canvas.drawText(c.getString(R.string.awaiting_signal), x_signal, y_signal, gpsOffPaint);
                }
            }
        } else {
            canvas.drawCircle(20, 20, 10, gpsViewPaint);
            canvas.drawText(c.getString(R.string.view_mode), 40, 27, gpsViewPaint);
        }
        //canvas.drawText(c.getString(R.string.translated_by), 20, 47, translatedByPaint);

        super.onDraw(canvas);
    }

    public String getLengthUnit() {
        double length = getPolygon().getLength();
        int scale = 1;
        if (lengthUnits == LENGTH_UNITS_METER) {
            return c.getString(R.string.length_unit_meter);
        } else if (lengthUnits == LENGTH_UNITS_KILOMETER) {
            return c.getString(R.string.length_unit_kilometer);
        } else if (lengthUnits == LENGTH_UNITS_AUTO_METRIC) {
            if (length > 1000)
                return c.getString(R.string.length_unit_kilometer);
            else
                return c.getString(R.string.length_unit_meter);
        } else if (lengthUnits == LENGTH_UNITS_FOOT) {
            return c.getString(R.string.length_unit_foot);
        } else if (lengthUnits == LENGTH_UNITS_YARD) {
            return c.getString(R.string.length_unit_yard);
        } else if (lengthUnits == LENGTH_UNITS_CHAIN) {
            return c.getString(R.string.length_unit_chain);
        } else if (lengthUnits == LENGTH_UNITS_MILE) {
            return c.getString(R.string.length_unit_mile);
        } else if (lengthUnits == LENGTH_UNITS_AUTO_IMPERIAL) {
            if (length > MILE_IN_METER)
                return c.getString(R.string.length_unit_mile);
            else
                return c.getString(R.string.length_unit_yard);
        }

        return "undefined";
    }

    public String getAreaUnit() {
        double area = getPolygon().getArea();
        double scale = 1;
        if (areaUnits == AREA_UNITS_SQUARE_METER) {
            return c.getString(R.string.area_unit_square_meter);
        } else if (areaUnits == AREA_UNITS_HECTARE) {
            return c.getString(R.string.area_unit_hectare);
        } else if (areaUnits == AREA_UNITS_SQUARE_KILOMETER) {
            return c.getString(R.string.area_unit_square_kilometermeter);
        } else if (areaUnits == AREA_UNITS_AUTO_METRIC) {
            if (area >= 1000000)
                return c.getString(R.string.area_unit_square_kilometermeter);
            else if (area >= 10000)
                return c.getString(R.string.area_unit_hectare);
            else
                return c.getString(R.string.area_unit_square_meter);
        } else if (areaUnits == AREA_UNITS_SQUARE_FOOT) {
            return c.getString(R.string.area_unit_square_foot);
        } else if (areaUnits == AREA_UNITS_SQUARE_YARD) {
            return c.getString(R.string.area_unit_square_yard);
        } else if (areaUnits == AREA_UNITS_ACRE) {
            return c.getString(R.string.area_unit_acre);
        } else if (areaUnits == AREA_UNITS_SQUARE_MILE) {
            return c.getString(R.string.area_unit_square_mile);
        } else if (areaUnits == AREA_UNITS_AUTO_IMPERIAL) {
            if (area > MILE_IN_METER * MILE_IN_METER)
                return c.getString(R.string.area_unit_square_mile);
            else if (area > ACRE_IN_SQUARE_METER)
                return c.getString(R.string.area_unit_acre);
            else
                return c.getString(R.string.area_unit_square_yard);
        }

        return "undefined";
    }

    public double getLength() {
        double length = getPolygon().getLength();
        double scale = 1;
        if (lengthUnits == LENGTH_UNITS_METER) {
        } else if (lengthUnits == LENGTH_UNITS_KILOMETER) {
            scale = 1000;
        } else if (lengthUnits == LENGTH_UNITS_AUTO_METRIC) {
            if (length > 1000)
                scale = 1000;
        } else if (lengthUnits == LENGTH_UNITS_FOOT) {
            scale = FOOT_IN_METER;
        } else if (lengthUnits == LENGTH_UNITS_YARD) {
            scale = YARD_IN_METER;
        } else if (lengthUnits == LENGTH_UNITS_CHAIN) {
            scale = CHAIN_IN_METER;
        } else if (lengthUnits == LENGTH_UNITS_MILE) {
            scale = MILE_IN_METER;
        } else if (lengthUnits == LENGTH_UNITS_AUTO_IMPERIAL) {
            if (length > MILE_IN_METER)
                scale = MILE_IN_METER;
            else
                scale = YARD_IN_METER;
        }

        return length / scale;
    }

    public double getArea() {
        double area = getPolygon().getArea();
        double scale = 1;
        if (areaUnits == AREA_UNITS_SQUARE_METER) {
        } else if (areaUnits == AREA_UNITS_HECTARE) {
            scale = 10000;
        } else if (areaUnits == AREA_UNITS_SQUARE_KILOMETER) {
            scale = 1000000;
        } else if (areaUnits == AREA_UNITS_AUTO_METRIC) {
            if (area >= 1000000)
                scale = 1000000;
            else if (area >= 10000)
                scale = 10000;
        } else if (areaUnits == AREA_UNITS_SQUARE_FOOT) {
            scale = FOOT_IN_METER * FOOT_IN_METER;
        } else if (areaUnits == AREA_UNITS_SQUARE_YARD) {
            scale = YARD_IN_METER * YARD_IN_METER;
        } else if (areaUnits == AREA_UNITS_ACRE) {
            scale = ACRE_IN_SQUARE_METER;
        } else if (areaUnits == AREA_UNITS_SQUARE_MILE) {
            scale = MILE_IN_METER * MILE_IN_METER;
        } else if (areaUnits == AREA_UNITS_AUTO_IMPERIAL) {
            if (area > MILE_IN_METER * MILE_IN_METER)
                scale = MILE_IN_METER * MILE_IN_METER;
            else if (area > ACRE_IN_SQUARE_METER)
                scale = ACRE_IN_SQUARE_METER;
            else
                scale = YARD_IN_METER * YARD_IN_METER;
        }

        return area / scale;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void start() {
//		reset();
        gps.start();
    }

    public void reset() {
        setPolygon(new Polygon());

        gps.reset();
    }

    public void stop() {
        stop = true;
        gps.isStarted = false;
    }

    public boolean isReadyToStart() {
        if (gps.isActive() && !gps.isStarted)
            return true;
        else
            return false;
    }

    public boolean isRunning() {
        if (gps.isStarted)
            return true;
        else
            return false;
    }

    public void importFromGPS() {
        if (getType().equalsIgnoreCase("Manual")) {
            if (latitude != 0 && longitude != 0) {

                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR);
                int minute = c.get(Calendar.MINUTE);
                int seconds = c.get(Calendar.SECOND);
                String time = String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(seconds);

                fetch_list_lat.add(latitude);
                fetch_list_lng.add(longitude);
                int taccy = (int) accuracy;
                fetch_list_accuracy.add(taccy);
                list_fetch_time.add(time);
                fetch_list_latlng.add(new LatLng(latitude, longitude));
            }

        }
        gps.ensureOrigoIsFixed();
        getPolygon().addVertex(new Coordinate(gps.coordinate.x, gps.coordinate.y),
                new GPSCoordinate(latitude, longitude, altitude));
        if (getPolygon().getSize() > 0)
            setZoom(1 / getPolygon().getSize());
        else
            setZoom(1);
    }

    public void importFromMap(Double maplat, Double maplng, Integer mapacc, int pos, boolean shouldReplace) {
        if (getType().equalsIgnoreCase("Map")) {
            if (maplat != 0 && maplng != 0) {

                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR);
                int minute = c.get(Calendar.MINUTE);
                int seconds = c.get(Calendar.SECOND);
                String time = String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(seconds);
                if (pos == -1) {
                    maplist_lat.add(maplat);
                    maplist_lng.add(maplng);
                    maplist_Accuracy.add(mapacc);
                    maplist_fTime.add(time);
                } else {
                    if (shouldReplace) {
                        maplist_lat.remove(pos);
                        maplist_lng.remove(pos);
                        maplist_Accuracy.remove(pos);
                        maplist_fTime.remove(pos);
                    }

                    maplist_lat.add(pos, maplat);
                    maplist_lng.add(pos, maplng);
                    maplist_Accuracy.add(pos, mapacc);
                    maplist_fTime.add(pos, time);
                }
            }

        }
    }

    public void removeMarker(int pos) {
        maplist_lat.remove(pos);
        maplist_lng.remove(pos);
        maplist_Accuracy.remove(pos);
        maplist_fTime.remove(pos);
    }

    public void setLengthUnits(int sUnits) {
        lengthUnits = sUnits;
    }

    public void setAreaUnits(int sUnits) {
        areaUnits = sUnits;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public void setaccuracy(int acuracy) {
        this.acuracy = acuracy;
    }

    public int getaccuracy() {
        return acuracy;
    }

    public void setType(String Type) {
        this.type = Type;
    }

    public String getType() {
        return type;
    }

    public void setLoggingMode(int loggingMode) {
        this.loggingMode = loggingMode;
    }

    public int getLoggingMode() {
        return loggingMode;
    }

    public String estimatedAreaError(int estimatedGPSAccuracy) {
        if (getPolygon().getNumVertices() > 0) {
            double length = getPolygon().getLength();
            double area = getPolygon().getArea();

            double estimatedAreaError = length * estimatedGPSAccuracy;

            double relativeError = estimatedAreaError / area;

            return "" + ((int) (relativeError * getArea() * 100)) / (float) 100 + getAreaUnit() + "\n" + (int) (relativeError * 100) + "%";
        } else {
            return "0" + getAreaUnit() + "\n0%";
        }
    }

    public ArrayList<Integer> getAccList() {
        return list_finaccuracy;
    }

    public ArrayList<String> getFetchTimeList() {
        return list_fetch_time;
    }

    public ArrayList<String> getTimeList() {
        return list_fTime;
    }

    public ArrayList<Double> getLatList() {
        return list_lat;
    }

    public ArrayList<Double> getLngList() {
        return list_lng;
    }

    public ArrayList<LatLng> getlist_latlng() {
        return list_latlng;
    }

    public ArrayList<Double> get_fetch_LatList() {
        return fetch_list_lat;
    }

    public ArrayList<Integer> get_fetch_AccyList() {
        return fetch_list_accuracy;
    }

    public ArrayList<Double> get_fetch_LngList() {
        return fetch_list_lng;
    }

    public ArrayList<LatLng> get_fetch_list_latlng() {
        return fetch_list_latlng;
    }

    public ArrayList<Double> getMapLatList() {
        return maplist_lat;
    }

    public ArrayList<Double> getMapLngList() {
        return maplist_lng;
    }

    public ArrayList<Integer> get_Map_AccyList() {
        return maplist_Accuracy;
    }

    public ArrayList<String> getMapTimeList() {
        return maplist_fTime;
    }


    public void clearArraylist() {
        list_lat.clear();
        list_lng.clear();
        list_latlng.clear();

        fetch_list_lat.clear();
        fetch_list_lng.clear();
        fetch_list_latlng.clear();
        fetch_list_accuracy.clear();
        list_fetch_time.clear();

        list_Accuracy.clear();
        list_finaccuracy.clear();
        list_Time.clear();

        maplist_lat.clear();
        maplist_lng.clear();
        maplist_Accuracy.clear();
        maplist_fTime.clear();
    }

    public double area(ArrayList<Double> lats, ArrayList<Double> lons) {
        double sum = 0;
        double prevcolat = 0;
        double prevaz = 0;
        double colat0 = 0;
        double az0 = 0;
        for (int i = 0; i < lats.size(); i++) {
            double colat = 2 * Math.atan2(Math.sqrt(Math.pow(Math.sin(lats.get(i) * Math.PI / 180 / 2), 2) + Math.cos(lats.get(i) * Math.PI / 180) * Math.pow(Math.sin(lons.get(i) * Math.PI / 180 / 2), 2)), Math.sqrt(1 - Math.pow(Math.sin(lats.get(i) * Math.PI / 180 / 2), 2) - Math.cos(lats.get(i) * Math.PI / 180) * Math.pow(Math.sin(lons.get(i) * Math.PI / 180 / 2), 2)));
            double az = 0;
            if (lats.get(i) >= 90) {
                az = 0;
            } else if (lats.get(i) <= -90) {
                az = Math.PI;
            } else {
                az = Math.atan2(Math.cos(lats.get(i) * Math.PI / 180) * Math.sin(lons.get(i) * Math.PI / 180), Math.sin(lats.get(i) * Math.PI / 180)) % (2 * Math.PI);
            }
            if (i == 0) {
                colat0 = colat;
                az0 = az;
            }
            if (i > 0 && i < lats.size()) {
                sum = sum + (1 - Math.cos(prevcolat + (colat - prevcolat) / 2)) * Math.PI * ((Math.abs(az - prevaz) / Math.PI) - 2 * Math.ceil(((Math.abs(az - prevaz) / Math.PI) - 1) / 2)) * Math.signum(az - prevaz);
            }
            prevcolat = colat;
            prevaz = az;
        }
        sum = sum + (1 - Math.cos(prevcolat + (colat0 - prevcolat) / 2)) * (az0 - prevaz);

        // It calculates the "suface per unit", so I multiplied the answer by Earth's Surface Area (5.10072e14 sq m).
        return 5.10072E14 * Math.min(Math.abs(sum) / 4 / Math.PI, 1 - Math.abs(sum) / 4 / Math.PI);
    }

    public void startCheckingAccuracy() {


        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Checking...");

        pDialog.setCancelable(false);
        pDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        pDialog.show();

        new Handler().postDelayed(new Runnable() {
            public void run() {

                //	boolean CHECK = pref.getBoolean("TIMEACCURACYCHECK",false);

                int acc_length = list_Accuracy.size();
                if (acc_length >= 0) {
                    String acc = "", tim = "";

                    for (int i = 0; i < acc_length; i++) {
                        if (i == 0) {
                            acc = "" + list_Accuracy.get(i);
                            tim = list_Time.get(i);
                        } else {
                            acc = acc + "," + list_Accuracy.get(i);
                            tim = tim + "," + list_Time.get(i);
                        }
                    }
                    int tempfacc = 0;
                    if (list_Accuracy != null && list_Accuracy.size() > 0) {
                        tempfacc = Collections.max(list_Accuracy);
                        Log.d("tempfacc : ", "" + tempfacc);
                        if (tempfacc == 0) {
                            tempfacc = accuracy;
                        }
                    }
                    //	tempfacc = 100;// Accuracy replaced by 100
                    setaccuracy(tempfacc);
                    pref = getContext().getSharedPreferences("MyPref", Activity.MODE_PRIVATE);
                    editor = pref.edit();
                    editor.putString("ACCURACYVALUE", String.valueOf(tempfacc));

                    editor.commit();
                    int fTime;
                    if (list_Time.size() >= 3) {
                        fTime = list_Time.size() / 3;
                    } else {
                        fTime = 1;
                    }

                    int tmptime = 1 * 1000;//ftime is replaced by 1 (default 1 sec)
                    setTime(tmptime);
                    try {
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }

                        showAlertDialog(getContext(),
                                "Ready to measure, Please walk..", "Close");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }, TIME_INTERVAL);


    }

    public boolean checkGps() {

        boolean gpsStatus = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        return gpsStatus;
    }

    public void showAlertDialog(Context context, String msg, final String purpose) {
        String message = msg;


        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        RelativeLayout rl_action_bar = (RelativeLayout) dialog
                .findViewById(R.id.rl_action_bar);


        TextView txt_app_name = (TextView) dialog
                .findViewById(R.id.txt_app_name);
        String headText = "Alert";
        if (purpose.equalsIgnoreCase("Reset") || purpose.equalsIgnoreCase("Save")) {
            headText = "Alert";
        } else {
            headText = "NSLSugar";
        }
        txt_app_name.setText(headText);
        TextView text = (TextView) dialog.findViewById(R.id.txt_alert);
        text.setText(message);
        Button btn_Yes = (Button) dialog.findViewById(R.id.btn_yes);
        Button btn_No = (Button) dialog.findViewById(R.id.btn_no);


        btn_No.setVisibility(View.GONE);
        btn_Yes.setText("OK");

        btn_Yes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (purpose.equalsIgnoreCase("Start")) {
                    dialog.dismiss();
                    startCheckingAccuracy();
                } else {
                    measure = true;
                    editor.putBoolean("TIMEACCURACYCHECK", true);
                    editor.commit();
                    dialog.dismiss();
                }

            }
        });

        dialog.show();
    }
}
