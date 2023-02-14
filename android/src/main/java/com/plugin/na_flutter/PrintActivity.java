package com.plugin.na_flutter;

import static com.leopard.api.Printer.PR_FONTLARGENORMAL;
import static com.mydevice.sdk.Printer.PR_FONTLARGEBOLD;
import static com.mydevice.sdk.Printer.PR_FONTSMALLBOLD;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evolute.qrimage.QRCodeGenerator;
import com.evolute.textimage.TextGenerator;
import com.leopard.api.Printer;
import com.leopard.api.Setup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import io.flutter.Log;

public class PrintActivity extends AppCompatActivity {
    Setup setupInstance = null;
    HashMap<String, String> inputData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        inputData = (HashMap<String, String>) bundle.getSerializable("inputData");

        Button btn = (Button) findViewById(R.id.btn);

        try {
            setupInstance = new Setup();
            boolean activate = setupInstance.blActivateLibrary(this, R.raw.licence);
            Log.e("activate", String.valueOf(activate));
            GlobalPool mGP = new GlobalPool();
            boolean connected = mGP.createConn(inputData.get("mac"));
            Log.e("TAG1", "isCon ->" + connected);
            Toast.makeText(PrintActivity.this, "Connect -> " + connected, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                print();
            }

        });
    }

    private void showResult(int iRtl) {
        if (iRtl == Printer.PR_SUCCESS) {
            Toast.makeText(getApplicationContext(), "Graphics print is success", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_ILLEGAL_LIBRARY) {
            Toast.makeText(getApplicationContext(), "Library Invalid", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_DEMO_VERSION) {
            Toast.makeText(getApplicationContext(), "API Not supported for Demo Version", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_INVALID_DEVICE_ID) {
            Toast.makeText(getApplicationContext(), "Invalid device serial number", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_PLATEN_OPEN) {
            Toast.makeText(getApplicationContext(), "printer platen is open", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_PAPER_OUT) {
            Toast.makeText(getApplicationContext(), "printer paper is out", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_HIGH_HEADTEMP) {
            Toast.makeText(getApplicationContext(), "printer High headtemp", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_LOW_HEADTEMP) {
            Toast.makeText(getApplicationContext(), "printer Low headtemp", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_IMPROPER_VOLTAGE) {
            Toast.makeText(getApplicationContext(), "printer improper voltage", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_FAIL) {
            Toast.makeText(getApplicationContext(), "printer failed: ", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_PARAM_ERROR) {
            Toast.makeText(getApplicationContext(), "Passed invalid parameter: ", Toast.LENGTH_SHORT).show();
        } else if (iRtl == Printer.PR_INACTIVE_PERIPHERAL) {
            Toast.makeText(getApplicationContext(), "printer failed: ", Toast.LENGTH_SHORT).show();
        }

    }

    private void print() {
        String TAG = "PRINT_METHOD";
        try {
            OutputStream outSt = BluetoothComm.mosOut;
            InputStream inSt = BluetoothComm.misIn;
            Printer ptr = new Printer(setupInstance, outSt, inSt);

            // todo -> print design here

            int logoResult = ptr.iBmpPrint(getApplicationContext(), R.raw.logo);
            android.util.Log.e(TAG, "print: -> logo 12.14" + logoResult);

            Bitmap bmpDrawQRCode = QRC.bmpDrawQRCode(TextGenerator.ImageWidth.Inch_2,   inputData.get("qrData"));
            byte[] bBmpFileData = TextGenerator.bGetBmpFileData(bmpDrawQRCode);
            ByteArrayInputStream bis = new ByteArrayInputStream(bBmpFileData);
            int qrCodeResult = ptr.iBmpPrint(bis);
            android.util.Log.e(TAG, "print: -> QRcode" + qrCodeResult);

            String name =  inputData.get("name"), title = "";
            if(name.length() < 24) {
                int val = (24 - name.length()) / 2, i = 0;
                System.out.println("" + val);
                while(i < val) {
                    title = title + " ";
                    i++;
                }
                title = title + name;
            }

            ptr.iPrinterAddData(PR_FONTLARGEBOLD, title);
            ptr.iPrinterAddData(PR_FONTLARGEBOLD, "       TRIP SHEET       ");
            ptr.iPrinterAddData(PR_FONTLARGEBOLD, "CRUSHING SEASON: " + inputData.get("crushingSeason"));

            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "TS NO: " + inputData.get("mac"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "TS DATE&TIME: " + inputData.get("tsDateTime"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL,  inputData.get("tsDateTime"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "DIV: " + inputData.get("div"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "SEC: " + inputData.get("sec"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "C O NO: " + inputData.get("cono"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "RNO: " + inputData.get("rNo"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "R NAME: " + inputData.get("rName"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "F NAME: " + inputData.get("fName"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "VIL: " + inputData.get("vil"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "VILDIS: " + inputData.get("vildis"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "D.O.P - P/R: " + inputData.get("dop"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "VAR: " + inputData.get("var"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "GL NO: " + inputData.get("glNo"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "GL NAME: " + inputData.get("glName"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "T MODE: " + inputData.get("tMode"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "PASS NO: " + inputData.get("passNo"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "VEH NO: " + inputData.get("vehNo"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "BURNT CANE: " + inputData.get("burntCane"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "FALL ON CANE: " + inputData.get("fallOnCane"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "PLOT OVER:  " + inputData.get("plotOver"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "PART LOAD: " + inputData.get("partLoad"));

            int finalResult = ptr.iStartPrinting(1);

            ptr.iPaperFeed();

            if (finalResult == Printer.PR_SUCCESS) {
                Bundle resultBundle = new Bundle();
                resultBundle.putString("printerResult", "success");
                setResult(RESULT_OK, new Intent().putExtra("resultData", resultBundle));
                finish();
            }
        } catch (Exception e) {
            Log.e("TAG1", e.toString());
            Toast.makeText(PrintActivity.this, "Something went wrong, Please try agin", Toast.LENGTH_SHORT).show();
        }
    }

}