package de.cubiclabs.beaconfinder.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.List;

import de.cubiclabs.beaconfinder.R;
import de.cubiclabs.beaconfinder.types.ProfileBeacon;
import de.cubiclabs.beaconfinder.util.BeaconUtils;

public class EstimoteMonitoringListener implements BeaconManager.MonitoringListener {

    private static final String TAG = EstimoteMonitoringListener.class.getName();

    private static final int NOTIFICATION_ID = 123;
    private BeaconManager mBeaconManager;
    private NotificationManager mNotificationManager;
    private Context mContext;
    private Intent mIntent;
    private EstimoteRangingListener mRangingListener;

    private List<Beacon> mLastFoundBeacons;

    private List<ProfileBeacon> mProfileBeacons;

    public EstimoteMonitoringListener(BeaconManager beaconMngr, NotificationManager notificationMngr,
                                      Context context, final Intent i,
                                      EstimoteRangingListener rangingListener) {

        mBeaconManager = beaconMngr;
        mNotificationManager = notificationMngr;
        mContext = context;
        mIntent = i;
        mProfileBeacons = new ArrayList<ProfileBeacon>();
        mRangingListener = rangingListener;
        mLastFoundBeacons = new ArrayList<Beacon>();
    }

    // ... close to us.
    @Override
    public void onEnteredRegion(Region region, List<Beacon> beacons) {

        //Log.i(TAG, "onEnteredRegion");
        boolean profileBeaconFound = false;
        for(Beacon beacon : beacons) {
            if(BeaconUtils.getMatchingProfileBeacon(mProfileBeacons, beacon) != null) {
                profileBeaconFound = true;
                break;
            }
        }

        try {
            if(profileBeaconFound) {
                mBeaconManager.startRanging(EstimoteManager.ALL_ESTIMOTE_BEACONS);
                mRangingListener.connect(mContext);

                //Log.i(TAG, "Monitored beacons: " + beacons);

                postNotificationIntent("Estimote testing",
                        "I have found an estimote !!!", mIntent);
            } else {
                mLastFoundBeacons = beacons;
                Log.i(TAG, "Entered region, but didn't find anything :-(");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Can't start ranging", e);
            System.out.println("Start ranging failed");
        }
    }

    // ... far away from us.
    @Override
    public void onExitedRegion(Region region) {
        postNotificationIntent("Estimote testing",
                "I have lost my estimote !!!", mIntent);

        //System.out.println("onExitedRegion");
        try {
            mRangingListener.disconnect();
            mBeaconManager.stopRanging(EstimoteManager.ALL_ESTIMOTE_BEACONS);
        } catch (RemoteException e) {
            Log.e(TAG, "Can't stop ranging.", e);
            System.out.println("Stop ranging failed");
        }
    }

    // Pops a notification in the task bar
    public void postNotificationIntent(String title, String msg, Intent i) {
        Notification notification = new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.ic_launcher).setContentTitle(title)
                .setContentText(msg).setAutoCancel(true).build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void setProfile(List<ProfileBeacon> profileBeacons) {
        mProfileBeacons = profileBeacons;
        if(mLastFoundBeacons != null && mRangingListener != null && !mRangingListener.isConnected) {
            onEnteredRegion(null, mLastFoundBeacons);
        }
    }

}
