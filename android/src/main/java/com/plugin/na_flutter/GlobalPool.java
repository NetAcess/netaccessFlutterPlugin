package com.plugin.na_flutter;

import android.app.Application;
import android.util.Log;


import java.io.InputStream;
import java.io.OutputStream;

public class GlobalPool extends Application
{
	/**Bluetooth communication connection object*/
	public BluetoothComm mBTcomm = null;
	public static InputStream misIn = BluetoothComm.misIn;
	/** Output stream object */
	public static OutputStream mosOut = BluetoothComm.mosOut;
	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	/**
	 * Set up a Bluetooth connection 
	 * @return Boolean
	 * */
	public boolean createConn(String sMac){
		Log.e("Connect","Create Connection");
		if (null == this.mBTcomm)
		{   
			
			this.mBTcomm = new BluetoothComm(sMac);
			if (this.mBTcomm.createConn()){
				
				return true;
			}
			else{
				this.mBTcomm = null;
				return false;
			}
		}
		else
			return true;
	}
	
	/**
	 * Close and release the connection
	 * @return void
	 * */
	public void closeConn(){
		if (null != this.mBTcomm){
			this.mBTcomm.closeConn();
			this.mBTcomm = null;
		}
	}
}
