package de.cubiclabs.beaconfinder.profile;

import android.content.Context;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.os.Handler;

import de.cubiclabs.beaconfinder.types.BeaconWithValueHistory;
import de.cubiclabs.beaconfinder.background.EstimoteManager;

public class ClosestBeaconDetector {

    private BeaconManager mBeaconManager;
    public static final String EXTRAS_BEACON = "extrasBeacon";
    public static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    public static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId",
            ESTIMOTE_PROXIMITY_UUID, null, null);

    private ClosestBeaconDetectedListener mListener;
    private List<BeaconWithValueHistory> mBeacons;

    private static final int MAX_TIME_TILL_MEASUREMENTS_START = 5000;
    private static final int MAX_MEASURING_TIME = 5000;
    private static final double TOLERANCE_ACCURACY = 0.2;
    private Handler mStartMeasurementHandler;
    private Runnable mStartMeasurementRunnable;
    private boolean mStartMeasurementHandlerRunning = false;
    private Handler mStopMeasurementHandler;
    private Runnable mStopMeasurementRunnable;
    private boolean mStopMeasurementHandlerRunning = false;

    /**
     * Start looking for beacons and determining the closest one.
     * @param context Current context
     * @param listener ClosestBeaconDetectedListener. Listen to when the closest beacon has been
     *                 detected or it failed to do so.
     */
    public void start(Context context, ClosestBeaconDetectedListener listener) {
        try {
            initAndStartTimers();

            mListener = listener;
            mBeacons = new ArrayList<BeaconWithValueHistory>();

            mBeaconManager = new BeaconManager(context);

            // We want the beacons heartbeat to be set at one second
            mBeaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1),
                    0);

            // Listen to recurring beacon discovery events
            mBeaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                    if(mStartMeasurementHandlerRunning) {
                        // Don't wait, start measurement timer immediately
                        mStartMeasurementHandler.removeCallbacks(mStartMeasurementRunnable);
                        mStartMeasurementHandler.post(mStartMeasurementRunnable);
                    }

                    processDetectedBeacons(beacons);
                }
            });

            // Connect to the beacon manager
            mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    try {
                        // ... and start ranging
                        mBeaconManager.startRanging(EstimoteManager.ALL_ESTIMOTE_BEACONS);
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    /**
     * Abort trying to detect the closest beacon
     */
    private void stop() {
        try {
            mListener = null;
            if(mStartMeasurementHandler != null)
                mStartMeasurementHandler.removeCallbacks(mStartMeasurementRunnable);
            if(mStopMeasurementHandler != null)
                mStopMeasurementHandler.removeCallbacks(mStopMeasurementRunnable);
            mBeaconManager.stopRanging(EstimoteManager.ALL_ESTIMOTE_BEACONS);
            mBeaconManager.disconnect();
        } catch (Exception e) {
        }
    }

    /**
     * Beacons detected -> Store the measured values
     * @param beacons Ranged beacons
     */
    private void processDetectedBeacons(List<Beacon> beacons) {
        for(Beacon rangedBeacon : beacons) {
            BeaconWithValueHistory beaconWithValueHistroy = null;

            // Look up if the beacons is already stored
            for (int i = 0; i < mBeacons.size(); i++) {
                if (mBeacons.get(i).equalsTo(rangedBeacon)) {
                    beaconWithValueHistroy = mBeacons.get(i);
                    break;
                }
            }

            // Create new object or add a history value
            if(beaconWithValueHistroy == null) {
                mBeacons.add(new BeaconWithValueHistory(rangedBeacon));
            } else {
                beaconWithValueHistroy.add(rangedBeacon);
            }
        }
    }

    /**
     * Order the beacon list by accuracy
     */
    private void orderBeaconsByAccuracy() {
        Comparator<BeaconWithValueHistory> byAverageAccuracy = new Comparator<BeaconWithValueHistory>() {
            @Override
            public int compare(BeaconWithValueHistory lhs, BeaconWithValueHistory rhs) {
                return Double.compare(lhs.getAverageAccuracy(), rhs.getAverageAccuracy());
            }
        };

        Collections.sort(mBeacons, byAverageAccuracy);
    }

    /**
     * Determines whether the 2nd closest beacon is very close to the closest beacon.
     * @return True, if the 2nd closest beacon is very close to the closest beacon.
     */
    private boolean isNeighborBeaconTooClose() {
        if(mBeacons.size() <= 1) return false;

        double accuracy1st = mBeacons.get(0).getAverageAccuracy();
        double accuracy2nd = mBeacons.get(1).getAverageAccuracy();

        double absTolerance = accuracy1st * TOLERANCE_ACCURACY;

        return (accuracy2nd - absTolerance <= accuracy1st);
    }

    /**
     * Determine the closest beacon among all detected ones.
     * @return The closest beacon.
     */
    private Beacon getClosestBeacon() {
        /*
        int closestIndex = 0;
        double minAccuracy = 0;
        for(int i=0; i<mBeacons.size(); i++) {
            BeaconWithValueHistory beacon = mBeacons.get(i);
            double accuracy = beacon.getAverageAccuracy();
            if(accuracy <= minAccuracy) {
                minAccuracy = accuracy;
                closestIndex = i;
            }
        }

        return mBeacons.get(closestIndex).toBeacon();
        */
        return mBeacons.get(0).toBeacon();
    }

    /**
     * Timer logic:
     * Wait x seconds.
     *  a) Beacons detected within that time -> start measurements immediately
     *      Measure for y seconds and determine the closest beacon at the end
     *  b) None detected -> Failed
     *
     */
    private void initAndStartTimers() {
        mStartMeasurementHandlerRunning = true;
        mStopMeasurementHandlerRunning = false;

        mStartMeasurementRunnable = new Runnable() {
            @Override
            public void run() {
                mStartMeasurementHandlerRunning = false;
                mStopMeasurementHandlerRunning = false;

                // No beacon detected -> Failed
                if(mBeacons.size() == 0) {
                    mListener.onNoBeaconDetected();
                    stop();
                    return;
                }

                // First beacon detected -> start measurements
                mListener.onMeasurementHasStarted(MAX_MEASURING_TIME);
                mStopMeasurementHandlerRunning = true;
                mStartMeasurementHandler.postDelayed(mStopMeasurementRunnable, MAX_MEASURING_TIME);
            }
        };

        mStopMeasurementRunnable = new Runnable() {
            @Override
            public void run() {
                mStopMeasurementHandlerRunning = false;
                // Measurement has ended

                // Still no beacon detected -> Failed
                if(mBeacons.size() == 0) {
                    mListener.onNoBeaconDetected();
                    stop();
                    return;
                }

                // Determine closest beacon
                orderBeaconsByAccuracy();
                if(isNeighborBeaconTooClose()) {
                    mListener.onOtherBeaconTooClose();
                } else {
                    mListener.onClosestBeaconDetected(getClosestBeacon());
                }


                stop();
            }
        };

        mStartMeasurementHandler = new Handler();
        mStartMeasurementHandler.postDelayed(mStartMeasurementRunnable, MAX_TIME_TILL_MEASUREMENTS_START);
    }
}
