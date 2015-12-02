package de.cubiclabs.beaconfinder.types;

import android.location.Location;

import com.estimote.sdk.Beacon;

/**
 * Created by thimokluser on 1/22/15.
 */
public class ProfileBeacon {

    private Beacon mBeacon;
    private double mLastKnownLatitude;
    private double mLastKnownLongitude;
    private String mLastKnownAddress = "";
    private String mName = "";
    private long mId = 0;

    private boolean mIsAvailable = false;

    public ProfileBeacon(Beacon beacon, String name, double lastKnownLatitude, double lastKnownLongitude, String lastKnownAddress) {
        mBeacon = beacon;
        mName = name;
        mLastKnownLatitude = lastKnownLatitude;
        mLastKnownLongitude = lastKnownLongitude;
        mLastKnownAddress = lastKnownAddress;
    }

    public Beacon getBeacon() {
        return mBeacon;
    }

    public void setBeacon(Beacon beacon) {
        this.mBeacon = beacon;
    }

    public double getLastKnownLatitude() {
        return mLastKnownLatitude;
    }

    public double getLastKnownLongitude() {
        return mLastKnownLongitude;
    }

    public Location getLastKnownGPS() {
        Location location = new Location("");
        location.setLatitude(mLastKnownLatitude);
        location.setLongitude(mLastKnownLongitude);
        return location;

    }

    public String getLastKnownAddress() {
        return mLastKnownAddress;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public void setLocation(Location location) {
        mLastKnownLatitude = location.getLatitude();
        mLastKnownLongitude = location.getLongitude();
    }

    public boolean isAvailable() {
        return mIsAvailable;
    }

    public void setAvailability(boolean isAvailable) {
        mIsAvailable = isAvailable;
    }
}
