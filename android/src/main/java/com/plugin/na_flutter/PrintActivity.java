package com.plugin.na_flutter;

import static com.leopard.api.Printer.PR_FONTLARGENORMAL;
import static com.mydevice.sdk.Printer.PR_FONTLARGEBOLD;
import static com.mydevice.sdk.Printer.PR_FONTSMALLBOLD;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.evolute.qrimage.QRCodeGenerator;
import com.evolute.textimage.TextGenerator;
import com.leopard.api.Printer;
import com.leopard.api.Setup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class PrintActivity extends AppCompatActivity {
    HashMap<String, String> inputData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        inputData = (HashMap<String, String>) bundle.getSerializable("inputData");
        Log.e("TAG", "onCreate: " + inputData);
        try {
            JSONObject jobj = new JSONObject(inputData);
            String mString = jobj.toString();
            Data myData = new Data.Builder().putString("WORKER_INPUT", mString).build();

            OneTimeWorkRequest attendWork = new OneTimeWorkRequest.Builder(PrintWorker.class)
                    .setInputData(myData)
                    .build();
            final WorkManager mWorkManager = WorkManager.getInstance();
            mWorkManager.enqueue(attendWork);

            mWorkManager.getWorkInfoByIdLiveData(attendWork.getId()).observe(this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(@Nullable WorkInfo workInfo) {
                    Log.e("TAG", "WorkonChanged: workInfo");
                    if (workInfo != null) {
                        Log.e("TAG", "WorkonChanged: workInfo not null");
                        WorkInfo.State state = workInfo.getState();
                        if(state == WorkInfo.State.SUCCEEDED) {
                            Log.e("TAG", "WorkonChanged: success");
                            Bundle resultBundle = new Bundle();
                            resultBundle.putString("printerResult", "Success");
                            Intent intent = new Intent().putExtra("resultData", resultBundle);
                            setResult(RESULT_OK, intent);
                            Bundle resultData = intent.getExtras().getBundle("resultData");
                            Log.e("SUCCEEDED", "onActivityResult: " + resultData.get("printerResult"));
                            finish();
                        } else if(state == WorkInfo.State.FAILED) {
                            Log.e("TAG", "WorkonChanged: failure");
                            Bundle resultBundle = new Bundle();
                            resultBundle.putString("printerResult", "failed");
                            Intent intent = new Intent().putExtra("resultData", resultBundle);
                            setResult(RESULT_OK, intent);
                            Bundle resultData = intent.getExtras().getBundle("resultData");
                            Log.e("FAILED", "onActivityResult: " + resultData.get("printerResult"));
                            finish();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e("TAG", "onCreate: " + e.toString());
            Bundle resultBundle = new Bundle();
            resultBundle.putString("printerResult", "failed");
            setResult(RESULT_OK, new Intent().putExtra("resultData", resultBundle));
            finish();
        }

    }
}