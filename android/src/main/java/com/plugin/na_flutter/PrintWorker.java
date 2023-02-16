package com.plugin.na_flutter;

import static android.app.Activity.RESULT_OK;
import static com.mydevice.sdk.Printer.PR_FONTLARGEBOLD;
import static com.mydevice.sdk.Printer.PR_FONTLARGENORMAL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.evolute.textimage.TextGenerator;
import com.leopard.api.Printer;
import com.leopard.api.Setup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.flutter.Log;

public class PrintWorker extends Worker {
    GlobalPool mGP = new GlobalPool();
    Setup setupInstance;

    public PrintWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data taskData = getInputData();
        String taskDataString = taskData.getString("WORKER_INPUT");
        JSONObject inputData = null;
        try {
            inputData = new JSONObject(taskDataString);
            setupInstance = new Setup();
            boolean activate = setupInstance.blActivateLibrary(getApplicationContext(), R.raw.licence);
            boolean connected = mGP.createConn(inputData.getString("mac"));
            Log.e("TAG1", "isCon ->" + connected);
            if (connected) {
                OutputStream outSt = BluetoothComm.mosOut;
                InputStream inSt = BluetoothComm.misIn;
                Printer ptr = new Printer(setupInstance, outSt, inSt);
                ptr.iFlushBuf();
                // todo -> print design here
                int logoResult = ptr.iBmpPrint(getApplicationContext(), R.raw.logo);
                android.util.Log.e("TAG", "print: -> logo 12.14" + logoResult);
                Bitmap bmpDrawQRCode = QRC.bmpDrawQRCode(TextGenerator.ImageWidth.Inch_2, inputData.getString("qrdt"));
                byte[] bBmpFileData = TextGenerator.bGetBmpFileData(bmpDrawQRCode);
                ByteArrayInputStream bis = new ByteArrayInputStream(bBmpFileData);
                int qrCodeResult = ptr.iBmpPrint(bis);
                android.util.Log.e("TAG", "print: -> QRcode" + qrCodeResult);

                String name = inputData.getString("fact"), title = "";
                if (name.length() < 24) {
                    int val = (24 - name.length()) / 2, i = 0;
                    System.out.println("" + val);
                    while (i < val) {
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
                android.util.Log.e("TAG", "print: -> finalResult" + finalResult);
                if (finalResult == Printer.PR_SUCCESS) {
                    mGP.closeConn();
                    Data outputData = new Data.Builder().putString("WORK_RESULT", "success").build();
                    return Result.success(outputData);
                } else {
                    mGP.closeConn();
                    Data outputData = new Data.Builder().putString("WORK_RESULT", getResultError(finalResult)).build();
                    return Result.failure(outputData);
                }
            } else {
                mGP.closeConn();

                Data outputData = new Data.Builder().putString("WORK_RESULT", "ConnectionFailed").build();
                return Result.failure(outputData);
            }
        } catch (Exception e) {
            Data outputData = new Data.Builder().putString("WORK_RESULT", e.toString()).build();
            return Result.failure(outputData);
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
