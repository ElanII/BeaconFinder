package de.cubiclabs.beaconfinder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.estimote.sdk.Beacon;

import java.util.ArrayList;
import java.util.List;

import de.cubiclabs.beaconfinder.types.ProfileBeacon;

public class ProfileBeaconDAO {
    private SQLiteDatabase mDb;
    private MySQLiteHelper mDbHelper;

    private String[] allColumns = {
            MySQLiteHelper.COL_ID,
            MySQLiteHelper.COL_UUID,
            MySQLiteHelper.COL_INTERNAL_NAME,
            MySQLiteHelper.COL_NAME,
            MySQLiteHelper.COL_MAC,
            MySQLiteHelper.COL_MAJOR,
            MySQLiteHelper.COL_MINOR,
            MySQLiteHelper.COL_LAST_KNOWN_LATITUDE,
            MySQLiteHelper.COL_LAST_KNOWN_LONGITUDE,
            MySQLiteHelper.COL_LAST_KNOWN_ADDRESS
    };

    public ProfileBeaconDAO(Context context) {
        initHelper(context);
    }

    synchronized private void initHelper(Context context) {
        mDbHelper = MySQLiteHelper.getInstance(context);
    }

    synchronized private void open() throws SQLException {
        mDb = mDbHelper.getWritableDatabase();
    }

    synchronized private void close() {
        //mDbHelper.close();
        mDb.close();
    }

    synchronized public ProfileBeacon create(ProfileBeacon pb) {
        Beacon b = pb.getBeacon();

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COL_UUID, b.getProximityUUID());
        values.put(MySQLiteHelper.COL_INTERNAL_NAME, b.getName());
        values.put(MySQLiteHelper.COL_MAC, b.getMacAddress());
        values.put(MySQLiteHelper.COL_MAJOR, b.getMajor());
        values.put(MySQLiteHelper.COL_MINOR, b.getMinor());
        values.put(MySQLiteHelper.COL_NAME, pb.getName());
        values.put(MySQLiteHelper.COL_LAST_KNOWN_LATITUDE, pb.getLastKnownLatitude());
        values.put(MySQLiteHelper.COL_LAST_KNOWN_LONGITUDE, pb.getLastKnownLongitude());
        values.put(MySQLiteHelper.COL_LAST_KNOWN_ADDRESS, pb.getLastKnownAddress());

        open();
        long insertId = mDb.insert(MySQLiteHelper.TABLE_BEACONS, null,
                values);
        close();

        pb.setId(insertId);

        System.out.println("Beacon stored in database");

        return pb;
    }

    synchronized public void update(ProfileBeacon pb) {
        Beacon b = pb.getBeacon();

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COL_UUID, b.getProximityUUID());
        values.put(MySQLiteHelper.COL_INTERNAL_NAME, b.getName());
        values.put(MySQLiteHelper.COL_MAC, b.getMacAddress());
        values.put(MySQLiteHelper.COL_MAJOR, b.getMajor());
        values.put(MySQLiteHelper.COL_MINOR, b.getMinor());
        values.put(MySQLiteHelper.COL_NAME, pb.getName());
        values.put(MySQLiteHelper.COL_LAST_KNOWN_LATITUDE, pb.getLastKnownLatitude());
        values.put(MySQLiteHelper.COL_LAST_KNOWN_LONGITUDE, pb.getLastKnownLongitude());
        values.put(MySQLiteHelper.COL_LAST_KNOWN_ADDRESS, pb.getLastKnownAddress());

        open();
        mDb.update(MySQLiteHelper.TABLE_BEACONS, values, MySQLiteHelper.COL_ID + "=?", new String[] {String.valueOf(pb.getId())});
        //long insertId = mDb.insert(MySQLiteHelper.TABLE_BEACONS, null,
        //        values);
        close();

        //pb.setId(insertId);

        System.out.println("Beacon updated in database");

    }

    synchronized public void delete(ProfileBeacon pb) {
        long id = pb.getId();
        open();
        mDb.delete(MySQLiteHelper.TABLE_BEACONS, MySQLiteHelper.COL_ID
                + " = " + id, null);
        close();
    }

    synchronized public List<ProfileBeacon> getAll() {
        List<ProfileBeacon> pbs = new ArrayList<ProfileBeacon>();

        open();
        Cursor cursor = mDb.query(MySQLiteHelper.TABLE_BEACONS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ProfileBeacon pb = cursorToProfileBeacon(cursor);
            pbs.add(pb);
            cursor.moveToNext();
        }
        System.out.println("Amount of profile beacons: " + cursor.getCount());

        cursor.close();
        close();
        return pbs;
    }

    synchronized private ProfileBeacon cursorToProfileBeacon(Cursor cursor) {
        Beacon beacon = new Beacon(cursor.getString(1),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getInt(5),
                cursor.getInt(6),
                0,
                0);

        ProfileBeacon profileBeacon = new ProfileBeacon(beacon, cursor.getString(3),
                cursor.getDouble(7), cursor.getDouble(8), cursor.getString(9));
        profileBeacon.setId(cursor.getLong(0));

        return profileBeacon;
    }
}
