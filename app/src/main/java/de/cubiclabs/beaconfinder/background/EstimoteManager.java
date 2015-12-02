package de.cubiclabs.beaconfinder.background;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.BeaconManager.MonitoringListener;

import de.cubiclabs.beaconfinder.R;
import de.cubiclabs.beaconfinder.database.ProfileBeaconDAO;
import de.cubiclabs.beaconfinder.types.ProfileBeacon;
import de.cubiclabs.beaconfinder.ui.MainActivity;

public class EstimoteManager {
    private static BeaconManager mBeaconManager;
    public static final String EXTRAS_BEACON = "extrasBeacon";
    public static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    public static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId",
            ESTIMOTE_PROXIMITY_UUID, null, null);

    private NotificationManager mNm;
    private Context mContext;

    EstimoteRangingListener mRangingListener;
    EstimoteMonitoringListener mMonitoringListener;


    private List<ProfileBeacon> mProfileBeacons;

    // Init everything we need to monitor the beacons
    public void create(NotificationManager notificationMngr,
                              Context context, final Intent i) {
        try {
            mNm = notificationMngr;
            mContext = context;


            mBeaconManager = new BeaconManager(context);

            // We want the beacons heartbeat to be set at one second
            mBeaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1),
                    0);

            // Listen to recurring beacon discovery events
            mRangingListener = new EstimoteRangingListener();
            mRangingListener.setProfile(mProfileBeacons);
            mBeaconManager.setRangingListener(mRangingListener);

            // Listen to beacon exit/entry events
            mMonitoringListener = new EstimoteMonitoringListener(mBeaconManager, notificationMngr, context, i, mRangingListener);
            mMonitoringListener.setProfile(mProfileBeacons);
            mBeaconManager.setMonitoringListener(mMonitoringListener);

            refreshProfile();

            // Connect to the beacon manager
            mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    try {
                        // ... and start the monitoring
                        mBeaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS);
                    } catch (Exception e) {
                }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Stop beacon monitoring and close the service
    public void stop() {
        try {
            mBeaconManager.stopMonitoring(ALL_ESTIMOTE_BEACONS);
            mBeaconManager.disconnect();
            mNm.cancel(1234567);
        } catch (Exception e) {
        }
    }

    private void showStatusBarNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "BeaconFinder is running";

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification.Builder(mContext)
                .setContentTitle(text)
                .setContentText("running")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        // Send the notification.
        mNm.notify(1234567, notification);

        //notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        //mNm.notify(1, notification);
    }

    public void refreshProfile() {
        ProfileBeaconDAO dao = new ProfileBeaconDAO(mContext);
        mProfileBeacons = dao.getAll();
        mMonitoringListener.setProfile(mProfileBeacons);
        mRangingListener.setProfile(mProfileBeacons);
    }

    public void fastRanging() {
        if(mBeaconManager != null) {
            mBeaconManager.setForegroundScanPeriod(200, 0);
            System.out.println("GO FAST");
        }
    }

    public void slowRanging() {
        if(mBeaconManager != null) {
            //mBeaconManager.setBackgroundScanPeriod(3000, 0);
            mBeaconManager.setForegroundScanPeriod(3000, 0);
            System.out.println("GO SLOW");
        }
    }
}

