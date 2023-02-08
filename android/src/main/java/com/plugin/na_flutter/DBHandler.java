
package com.plugin.na_flutter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "area.db";


    public static final String getlocation = "getlocation";
    public static final String getxml = "getxml";

    //for Location Service
    public static final String getlocationServiceData = "getlocationservicedata";

    public DBHandler(Context context, String name, CursorFactory factory,
                     int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub


        String CREATE_GETLOCATION_TABLE = "CREATE TABLE IF NOT EXISTS getlocation ( "
                +
                "fcnt VARCHAR(50), " +
                "accy VARCHAR(50), " +
                "ftim VARCHAR(50), " +
                "lat VARCHAR(50), " +
                "lng VARCHAR(50)) ";

        String CREATE_GETXML_TABLE = "CREATE TABLE IF NOT EXISTS getxml ( "
                +

                "sno VARCHAR(50), " +
                "xml VARCHAR(10000)) ";


        String CREATE_GETLOCATIONSERVICE_TABLE = "CREATE TABLE IF NOT EXISTS getlocationservicedata ( "
                +

                "time VARCHAR(50) PRIMARY KEY, " +

                "lat VARCHAR(50), " +

                "lng VARCHAR(50), " +

                "accd VARCHAR(50), " +

                "manf VARCHAR(50), " +

                "modl VARCHAR(50), " +

                "apiv VARCHAR(50), " +

                "andv VARCHAR(50))";


        db.execSQL(CREATE_GETLOCATIONSERVICE_TABLE);

        db.execSQL(CREATE_GETLOCATION_TABLE);
        db.execSQL(CREATE_GETXML_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    public void addgetlocation(GETLOCATION_POJO getlocation_pojo) {
        //

        String[] args = {getlocation_pojo.getFcnt(), getlocation_pojo.getAccy(), getlocation_pojo.getFtim(), getlocation_pojo.getLat(), getlocation_pojo.getLng()}; // where
        // 1
        // is
        // the
        // category
        // id
        getWritableDatabase()
                .execSQL(
                        "INSERT OR REPLACE INTO"
                                + " getlocation "
                                + "(fcnt,accy,ftim,lat,lng) VALUES (?, ?, ?, ?, ?)",
                        args);

    }

    public void addXML(GETXML_POJO xmlpojo) {
        //

        String[] args = {xmlpojo.getSno(), xmlpojo.getXml()
        }; // where 1
        // is
        // the
        // category
        // id
        getWritableDatabase()
                .execSQL(
                        "INSERT OR REPLACE INTO"
                                + " getxml "
                                + "(sno,xml) VALUES (?, ?)",
                        args);

    }

    public List<GETLOCATION_POJO> Listall_latLngs(String accy) {

//		String query = "Select * FROM " + getlocation + " WHERE accy <"+ "cast("+accy+" as integer)" ;
        /* + " WHERE actv = '1' ORDER BY  prty, " + field ; */
		
	/*	String query1 = "Select * FROM " + getlocation + " WHERE accy <= " + "cast("+accy+"as integer"
				;*/
        String query = "";
        if (accy.equalsIgnoreCase("")) {
            query = "Select * FROM " + getlocation;
        } else {
            query = "Select * FROM " + getlocation +

                    " WHERE cast(accy as integer)<=CAST(" + accy + " as INTEGER)";

        }

        //query = "Select * from getlocation where cast(accy as integer) <= cast(2 as integer)";

        SQLiteDatabase db = this.getWritableDatabase();
        List<GETLOCATION_POJO> product = new ArrayList<GETLOCATION_POJO>();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {

            do {
                GETLOCATION_POJO product1 = new GETLOCATION_POJO();
                product1.setFcnt(cursor.getString(0));
                product1.setAccy(cursor.getString(1));
                product1.setFtim(cursor.getString(2));
                product1.setLat(cursor.getString(3));
                product1.setLng(cursor.getString(4));


                // adding to todo list
                product.add(product1);

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            product = null;
        }
        // db.close();
        return product;
    }


    public void delete_table(String tname) {

        String query = "DELETE FROM " + tname;
        /* + " WHERE actv = '1' ORDER BY  prty, " + field ; */

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();

            cursor.close();
        } else {

        }
        // db.close();

    }

}
