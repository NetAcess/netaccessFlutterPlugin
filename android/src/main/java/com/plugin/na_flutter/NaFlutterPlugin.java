package com.plugin.na_flutter;


import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;


import com.leopard.api.Printer;
import com.leopard.api.Setup;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * NaFlutterPlugin
 */
public class NaFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity

    private MethodChannel mChannel;
    private Context mContext;
    private Activity mActivity;

    private Result mResult;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        mContext = flutterPluginBinding.getApplicationContext();
        mChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "na_flutter");
        mChannel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        mResult = result;
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;

            case "getCurrentLocation":
                GetGPSTracker gps = new GetGPSTracker(mContext);
                String lat = gps.getLatitude() + "";
                String lon = gps.getLongitude() + "";
                if (lat.startsWith("0") || lon.startsWith("0")) {
                    String data = gps.getAnyProviderLocation();
                    if (data != null) {
                        lat = data.split(",")[0];
                        lon = data.split(",")[1];
                    }
                }
                result.success(lat + "$" + lon);
                break;

            case "goToMap":
                Intent intent = new Intent(mActivity, Offline_Area.class);
                mActivity.startActivityForResult(intent, 666);
                // mActivity.startActivityForResult(mActivity, intent, 29, null);
                break;

            case "print":
                HashMap<String, String> args = call.arguments();
                Log.e("TAG", "onMethodCall: " + args);
                Intent printIntent = new Intent(mActivity, PrintActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("inputData", args);
                printIntent.putExtra("bundle", bundle);
                mActivity.startActivityForResult(printIntent, 777);
                //result.success("successfully reached NaFlutterPlugin");
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        mChannel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        mActivity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        mActivity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        mActivity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivity() {
        mActivity = null;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 666 && resultCode == RESULT_OK) {

            Bundle resultData = data.getExtras().getBundle("resultData");
            Map<String, String> mMap = new HashMap<>();
            mMap.put("xmlValue", resultData.getString("xmlValue"));
            mMap.put("areavalue", resultData.getString("areavalue"));
            mMap.put("starttimevalue", resultData.getString("starttimevalue"));
            mMap.put("endtimevalue", resultData.getString("endtimevalue"));
            mMap.put("accuracyvalue", resultData.getString("accuracyvalue"));
            mMap.put("mType", resultData.getString("mType"));
            mMap.put("savelat", resultData.getString("savelat"));
            mMap.put("savelng", resultData.getString("savelng"));

            mResult.success(mMap);
        }
        return false;
    }
}
