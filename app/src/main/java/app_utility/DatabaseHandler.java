package app_utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "BUTTERFLY_MESSAGE_DB";

    private static final String TABLE_MESSAGES = "TABLE_MESSAGES";

    private static final String KEY_ID = "_id";
    private static final String KEY_TYPE = "KEY_TYPE";
    private static final String KEY_MESSAGE = "KEY_MESSAGE";
    private static final String KEY_TIME = "KEY_TIME";
    private static final String KEY_STATUS = "KEY_STATUS";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    /*
    leaving gap between "CREATE TABLE" & TABLE_RECENT gives error watch out!
    Follow the below format
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_MAIN_MESSAGE_DB = "CREATE TABLE " + TABLE_MESSAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_TYPE + " INTEGER, "
                + KEY_MESSAGE + " TEXT, "
                + KEY_TIME + " TEXT, "
                + KEY_STATUS + " INTEGER)";

        db.execSQL(CREATE_MAIN_MESSAGE_DB);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);

        // Create tables again
        onCreate(db);
    }

    // Adding new data
    public void addDataToMessageTable(DataBaseHelper dataBaseHelper) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, dataBaseHelper.get_type());
        values.put(KEY_MESSAGE, dataBaseHelper.get_message());
        values.put(KEY_TIME, dataBaseHelper.get_time());
        values.put(KEY_STATUS, dataBaseHelper.get_status());

        db.insert(TABLE_MESSAGES, null, values);

        db.close();
    }

    /*public String lastDate(){
        String date;
        Cursor cursor;
        SQLiteDatabase db = getReadableDatabase();
        cursor = db.query(TABLE_MESSAGES, new String[]{KEY_TIME,
                }, KEY_ID + "=?",
                new String[]{String.valueOf(lastID())}, null, null, null, null);
        //cursor = db.rawQuery("SELECT TABLEALL FROM last_seen WHERE _id" +" = "+ID +" ", new String[] {KEY_ID + ""});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            date = cursor.getString(cursor.getColumnIndex(KEY_TIME));
        } else {
            date = "";
        }
        *//*if(sName==null){
            return "";
        }*//*
        cursor.close();
        return date;
    }*/

    /*
    gets data of last record added to database
     */
    public String lastDate(){
        String date;
        Cursor cursor;
        SQLiteDatabase db = getReadableDatabase();
        cursor = db.query(TABLE_MESSAGES, new String[]{KEY_TIME,
        }, null, null, null, null, KEY_ID +" DESC", "1");

        //cursor = db.rawQuery("SELECT TABLEALL FROM last_seen WHERE _id" +" = "+ID +" ", new String[] {KEY_ID + ""});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            date = cursor.getString(cursor.getColumnIndex(KEY_TIME));
        } else {
            date = "";
        }
        /*if(sName==null){
            return "";
        }*/
        cursor.close();
        return date;
    }

    public List<String> lastMessageAndTime(){
        ArrayList<String> alData = new ArrayList<>();
        Cursor cursor;
        SQLiteDatabase db = getReadableDatabase();
        cursor = db.query(TABLE_MESSAGES, new String[]{KEY_MESSAGE, KEY_TIME,
        }, null, null, null, null, KEY_ID +" DESC", "1");

        //cursor = db.rawQuery("SELECT TABLEALL FROM last_seen WHERE _id" +" = "+ID +" ", new String[] {KEY_ID + ""});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            alData.add(cursor.getString(cursor.getColumnIndex(KEY_MESSAGE)));
            alData.add(cursor.getString(cursor.getColumnIndex(KEY_TIME)));
        }/* else {
            message = "";
        }*/
        /*if(sName==null){
            return "";
        }*/
        cursor.close();
        return alData;
    }


    public String lastMessage(){
        String message;
        Cursor cursor;
        SQLiteDatabase db = getReadableDatabase();
        cursor = db.query(TABLE_MESSAGES, new String[]{KEY_MESSAGE,
        }, null, null, null, null, KEY_ID +" DESC", "1");

        //cursor = db.rawQuery("SELECT TABLEALL FROM last_seen WHERE _id" +" = "+ID +" ", new String[] {KEY_ID + ""});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            message = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE));
        } else {
            message = "";
        }
        /*if(sName==null){
            return "";
        }*/
        cursor.close();
        return message;
    }
    /*public int lastID() {
        int res;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{COLUMN_ID,
        }, null, null, null, null, null);
        cursor.moveToLast();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            res = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        } else {
            res = -1;
        }
        cursor.close();
        return res;
    }

    public int lastIDOfMainProducts() {
        int res;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{COLUMN_ID,
        }, null, null, null, null, null);
        cursor.moveToLast();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            res = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        } else {
            res = -1;
        }
        cursor.close();
        return res;
    }*/

    public List<DataBaseHelper> getMessages() {
        List<DataBaseHelper> dataBaseHelperList = new ArrayList<>();
        //ArrayList<String> alTechSpecs = new ArrayList<>();
        // Select All Query
        //String selectQuery = "SELECT  * FROM " + TABLE_INDIVIDUAL_PRODUCTS;
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DataBaseHelper dataBaseHelper = new DataBaseHelper();
                dataBaseHelper.set_id(Integer.parseInt(cursor.getString(0)));
                dataBaseHelper.set_type(cursor.getInt(1));
                dataBaseHelper.set_message(cursor.getString(2));
                dataBaseHelper.set_time(cursor.getString(3));
                dataBaseHelper.set_status(cursor.getInt(4));
                // Adding data to list
                dataBaseHelperList.add(dataBaseHelper);
            } while (cursor.moveToNext());
        }

        // return recent list
        return dataBaseHelperList;
    }

    public List<DataBaseHelper> getMessageByStatusFilter(int Key) {
        List<DataBaseHelper> dataBaseHelperList = new ArrayList<>();
        //ArrayList<String> alTechSpecs = new ArrayList<>();
        // Select All Query
        //String selectQuery = "SELECT  * FROM " + TABLE_INDIVIDUAL_PRODUCTS;
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES + " WHERE "
                + KEY_STATUS + "=" + Key;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DataBaseHelper dataBaseHelper = new DataBaseHelper();
                dataBaseHelper.set_id(Integer.parseInt(cursor.getString(0)));
                dataBaseHelper.set_type(cursor.getInt(1));
                dataBaseHelper.set_message(cursor.getString(2));
                dataBaseHelper.set_time(cursor.getString(3));
                dataBaseHelper.set_status(cursor.getInt(4));
                // Adding data to list
                dataBaseHelperList.add(dataBaseHelper);
            } while (cursor.moveToNext());
        }

        // return recent list
        return dataBaseHelperList;
    }


    public List<DataBaseHelper> getSingleProductByID(int ID) {
        List<DataBaseHelper> dataBaseHelperList = new ArrayList<>();
        //ArrayList<String> alTechSpecs = new ArrayList<>();
        // Select All Query
        //String selectQuery = "SELECT  * FROM " + TABLE_INDIVIDUAL_PRODUCTS;
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES + " WHERE " + KEY_ID + "=" + ID;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DataBaseHelper dataBaseHelper = new DataBaseHelper();
                dataBaseHelper.set_id(Integer.parseInt(cursor.getString(0)));
                dataBaseHelper.set_type(cursor.getInt(1));
                dataBaseHelper.set_message(cursor.getString(2));
                dataBaseHelper.set_time(cursor.getString(3));
                dataBaseHelper.set_status(cursor.getInt(4));
                // Adding data to list
                dataBaseHelperList.add(dataBaseHelper);
            } while (cursor.moveToNext());
        }

        // return recent list
        return dataBaseHelperList;
    }

    public int updateStatusOfMessages(DataBaseHelper dataBaseHelper, int KEY_ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        //String column = "last_seen";
        ContentValues values = new ContentValues();
        //values.put(KEY_NAME, dataBaseHelper.getName());
        //values.put(KEY_NUMBER, dataBaseHelper.getPhoneNumber());
        //values.put(KEY_TYPE, dataBaseHelper.get_type());
        //values.put(KEY_MESSAGE, dataBaseHelper.get_message());
        //values.put(KEY_TIME, dataBaseHelper.get_time());
        values.put(KEY_STATUS, dataBaseHelper.get_status());

        // updating row
        //return db.update(TABLE_RECENT, values, column + "last_seen", new String[] {String.valueOf(KEY_ID)});
        return db.update(TABLE_MESSAGES, values, "_id" + " = " + KEY_ID, null);
        //*//**//*ContentValues data=new ContentValues();
        //data.put("Field1","bob");
        //DB.update(Tablename, data, "_id=" + id, null);*//**//*
    }

    public void deleteData(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        //db.delete(TABLE_RECENT, KEY_ID + " = ?", new String[] { String.valueOf(recent.getID()) });
        db.delete(TABLE_MESSAGES, KEY_ID + " = " + id, null);
        db.close();
    }

    /*public void deleteDataFromTempTable(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        //db.delete(TABLE_RECENT, KEY_ID + " = ?", new String[] { String.valueOf(recent.getID()) });
        db.delete(TABLE_TEMP_PRODUCTS, KEY_ID + " = " + id, null);
        db.close();
    }*/

}
