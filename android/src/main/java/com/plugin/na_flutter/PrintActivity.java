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

    GlobalPool mGP = new GlobalPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        inputData = (HashMap<String, String>) bundle.getSerializable("inputData");

        try {
            setupInstance = new Setup();
            boolean activate = setupInstance.blActivateLibrary(this, R.raw.licence);
            Log.e("activate", String.valueOf(activate));
            boolean connected = mGP.createConn(inputData.get("mac"));
            Log.e("TAG1", "isCon ->" + connected);
            Toast.makeText(PrintActivity.this, "Connect -> " + connected, Toast.LENGTH_SHORT).show();
            if(connected) {
                print();
            } else {
                mGP.closeConn();
                Bundle resultBundle = new Bundle();
                resultBundle.putString("printerResult", "ConnectionFailed");
                setResult(RESULT_OK, new Intent().putExtra("resultData", resultBundle));
                finish();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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

            Bitmap bmpDrawQRCode = QRC.bmpDrawQRCode(TextGenerator.ImageWidth.Inch_2,   inputData.get("qrdt"));
            byte[] bBmpFileData = TextGenerator.bGetBmpFileData(bmpDrawQRCode);
            ByteArrayInputStream bis = new ByteArrayInputStream(bBmpFileData);
            int qrCodeResult = ptr.iBmpPrint(bis);
            android.util.Log.e(TAG, "print: -> QRcode" + qrCodeResult);

            String name =  inputData.get("fact"), title = "";
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
            ptr.iPrinterAddData(PR_FONTLARGEBOLD, "CRUSHING SEASON: " + inputData.get("crsc"));

            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "TS NO: " + inputData.get("tsno") + "(" + inputData.get("sqno") + ")");
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "TS DATE&TIME: " + inputData.get("tsdt"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "DIV: " + inputData.get("dvnm"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "SEC: " + inputData.get("scnm"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "C O NO: " + inputData.get("cono"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "RNO: " + inputData.get("ryno") + "/" + inputData.get("ryld"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "R NAME: " + inputData.get("rynm"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "F NAME: " + inputData.get("fhnm"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "VIL: " + inputData.get("vlnm"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "VILDIS: " + inputData.get("vlds"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "D.O.P - P/R: " + inputData.get("pldt") + "-" + inputData.get("plty"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "VAR: " + inputData.get("vrty"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "GL NO: " + inputData.get("glcd"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "GL NAME: " + inputData.get("glnm"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "T MODE: " + inputData.get("crty"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "PASS NO: " + inputData.get("psno"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "VEH NO: " + inputData.get("vhno"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "BURNT CANE: " + inputData.get("brnt"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "FALL ON CANE: " + inputData.get("fall"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "PLOT OVER:  " + inputData.get("plvr"));
            ptr.iPrinterAddData(PR_FONTLARGENORMAL, "PART LOAD: " + inputData.get("prld"));

            int finalResult = ptr.iStartPrinting(1);

            ptr.iPaperFeed();

            if (finalResult == Printer.PR_SUCCESS) {
                mGP.closeConn();
                Bundle resultBundle = new Bundle();
                resultBundle.putString("printerResult", "success");
                setResult(RESULT_OK, new Intent().putExtra("resultData", resultBundle));
                finish();
            } else {
                mGP.closeConn();
                Bundle resultBundle = new Bundle();
                resultBundle.putString("printerResult", getResultError(finalResult));
                setResult(RESULT_OK, new Intent().putExtra("resultData", resultBundle));
                finish();
            }
        } catch (Exception e) {
            Log.e("TAG1", e.toString());
            Toast.makeText(PrintActivity.this, "Something went wrong, Please try agin", Toast.LENGTH_SHORT).show();
        }
    }

    private String getResultError(int iRtl) {
        if (iRtl == Printer.PR_SUCCESS) {
            return "Graphics print is success";
        } else if (iRtl == Printer.PR_ILLEGAL_LIBRARY) {
            return "Library Invalid";
        } else if (iRtl == Printer.PR_DEMO_VERSION) {
            return "API Not supported for Demo Version";
        } else if (iRtl == Printer.PR_INVALID_DEVICE_ID) {
            return "Invalid device serial number";
        } else if (iRtl == Printer.PR_PLATEN_OPEN) {
            return "printer platen is open";
        } else if (iRtl == Printer.PR_PAPER_OUT) {
            return "printer paper is out";
        } else if (iRtl == Printer.PR_HIGH_HEADTEMP) {
            return "printer High headtemp";
        } else if (iRtl == Printer.PR_LOW_HEADTEMP) {
            return "printer Low headtemp";
        } else if (iRtl == Printer.PR_IMPROPER_VOLTAGE) {
            return "printer improper voltage";
        } else if (iRtl == Printer.PR_FAIL) {
            return "printer failed: ";
        } else if (iRtl == Printer.PR_PARAM_ERROR) {
            return "Passed invalid parameter: ";
        } else if (iRtl == Printer.PR_INACTIVE_PERIPHERAL) {
            return "printer failed: ";
        }
        return "";
    }

}