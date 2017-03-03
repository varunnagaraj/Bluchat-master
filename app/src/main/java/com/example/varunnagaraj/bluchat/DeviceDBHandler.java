package com.example.varunnagaraj.bluchat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Varun Nagaraj on 27-02-2017.
 */

public class DeviceDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ScanDevices.db";
    private static final String TABLE_NAME = "DevicesInRange";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DEVICENAME = "DeviceName";
    public static final String COLUMN_DEVICEADDRESS = "DeviceAddress";
    public static final String COLUMN_DEVICERSSI = "DeviceRSSI";

    public DeviceDBHandler(Context context){//, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//        db.close();
        String query = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICENAME + " TEXT, "+
                COLUMN_DEVICEADDRESS + " TEXT, "+
                COLUMN_DEVICERSSI +" INTEGER " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(db);
    }

    //Add a new row
    public void addDevice(Devices devices){
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICENAME, devices.get_deviceName());
        values.put(COLUMN_DEVICEADDRESS, devices.get_deviceAddress());
        values.put(COLUMN_DEVICERSSI, devices.get_deviceRSSI());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    //Delete a product from the database
    public void deleteDevice(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + "WHERE" + COLUMN_ID + "=\"" + Integer.toString(id) + "\";");
    }

    public void deletetable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void createtable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DEVICENAME + " TEXT, "+
                COLUMN_DEVICEADDRESS + " TEXT, "+
                COLUMN_DEVICERSSI +" INTEGER " +
                ");";
        db.execSQL(query);
    }
    //get specific row from database
    public String [] deviceAt(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,new String[]{COLUMN_DEVICENAME, COLUMN_DEVICEADDRESS, COLUMN_DEVICERSSI},COLUMN_ID+"=?",new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor!=null)
            cursor.moveToFirst();
        String deviceIs [] = {cursor.getString(0), cursor.getString(1), cursor.getString(2)};
        cursor.close();
        return deviceIs;
    }

    //Printing database
//    public String databaseToString(int id){
//        String dbString = "";
//        SQLiteDatabase db = this.getWritableDatabase();
//        String query = "SELECT "+COLUMN_DEVICENAME + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID +" = "+String.valueOf(id);
//
//        //CURSOR POINT TO A LOCATION IN RESULTS
//        Cursor c = db.rawQuery(query, null);
//        c.moveToFirst();
//
//        while(!c.isAfterLast()){
//            if(c.getString(0)!=null){
//                dbString += c.getString(0);//c.getColumnIndex("DeviceName"));
//                dbString += "\n";
//            }
//        }
//        c.close();
//        db.close();
//        return dbString;
//    }

}
