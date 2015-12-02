package de.cubiclabs.beaconfinder.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by thimokluser on 1/22/15.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    private static MySQLiteHelper mInstance;

    private static final String DATABASE_NAME = "beacons.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_BEACONS = "beacons";
    public static final String COL_ID = "_id";
    public static final String COL_UUID = "uuid";
    public static final String COL_INTERNAL_NAME = "int_name";
    public static final String COL_NAME = "name";
    public static final String COL_MAC = "mac";
    public static final String COL_MAJOR = "major";
    public static final String COL_MINOR = "minor";
    public static final String COL_LAST_KNOWN_LATITUDE = "lat";
    public static final String COL_LAST_KNOWN_LONGITUDE = "lon";
    public static final String COL_LAST_KNOWN_ADDRESS = "address";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_BEACONS + "("
            + COL_ID + " integer primary key autoincrement, "
            + COL_UUID + " text not null, "
            + COL_INTERNAL_NAME + " text not null, "
            + COL_NAME + " text not null, "
            + COL_MAC + " text not null, "
            + COL_MAJOR + " text not null, "
            + COL_MINOR + " text not null, "
            + COL_LAST_KNOWN_LATITUDE + " real not null, "
            + COL_LAST_KNOWN_LONGITUDE + " real not null, "
            + COL_LAST_KNOWN_ADDRESS + " text not null);";

    private MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static MySQLiteHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (mInstance == null) {
            mInstance = new MySQLiteHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BEACONS);
        onCreate(db);
    }

}
