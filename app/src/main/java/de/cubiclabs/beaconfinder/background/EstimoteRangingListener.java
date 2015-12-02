package de.cubiclabs.beaconfinder.background;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.List;

import de.cubiclabs.beaconfinder.database.ProfileBeaconDAO;
import de.cubiclabs.beaconfinder.location.LocationManager;
import de.cubiclabs.beaconfinder.location.LocationReceivedListener;
import de.cubiclabs.beaconfinder.types.ProfileBeacon;
import de.cubiclabs.beaconfinder.ui.MainActivity;
import de.cubiclabs.beaconfinder.util.BeaconUtils;

public class EstimoteRangingListener implements BeaconManager.RangingListener, LocationReceivedListener {

    private static final String TAG = EstimoteRangingListener.class.getName();
    private List<ProfileBeacon> mProfileBeacons;

    private LocationManager mLocationManager;
    private Location mLastLocation;
    private Location mLastLocationUsedForUpdates;

    private Context mContext;
    public boolean isConnected = false;

    public EstimoteRangingListener() {
        mProfileBeacons = new ArrayList<ProfileBeacon>();

    }

    public void connect(Context context) {
        mLocationManager = new LocationManager(context, this);
        mLocationManager.connect();
        mContext = context;
        isConnected = true;
    }

    public void disconnect() {
        if(mLocationManager != null) {
            mLocationManager.disconnect();
        }
        isConnected = false;
    }

    @Override
    public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
        Log.i(TAG, "onBeaconsDiscovered");
        boolean beaconLocationUpdated = false;
        ArrayList<Beacon> availableBeacons = new ArrayList<Beacon>();

        for(Beacon beacon : beacons) {
            ProfileBeacon pb = BeaconUtils.getMatchingProfileBeacon(mProfileBeacons, beacon);
            if(pb == null) {
                Log.i(TAG, "UNKNOWN BEACON: " + beacon);
                continue;
            }
            Log.i(TAG, "Ranged beacon: " + beacon);

            availableBeacons.add(beacon);

            if(mLastLocation != null) {
                pb.setLocation(mLastLocation);

                if(mLastLocation != mLastLocationUsedForUpdates) {
                    ProfileBeaconDAO dao = new ProfileBeaconDAO(mContext);
                    dao.update(pb);
                    beaconLocationUpdated = true;
                    mLastLocationUsedForUpdates = mLastLocation;
                }
            }
        }

        if(beaconLocationUpdated) {
            //System.out.println("Sending REFRESH_LIST_INTENT");
            Intent intent = new Intent(MainActivity.REFRESH_LIST_INTENT);
            mContext.sendBroadcast(intent);
        }

        if(availableBeacons.size() > 0) {
            Log.d(TAG, "Sending AVAILABLE_PROFILE_BEACONS_INTENT");
            Intent intent = new Intent(MainActivity.AVAILABLE_PROFILE_BEACONS_INTENT);
            intent.putParcelableArrayListExtra("availableBeacons", availableBeacons);
            mContext.sendBroadcast(intent);
        }
    }

    @Override
    public void onLocationReceived(Location location) {
        mLastLocation = location;
        System.out.println("New location received");
    }

    public void setProfile(List<ProfileBeacon> profileBeacons) {
        mProfileBeacons = profileBeacons;
    }
}
