package de.cubiclabs.beaconfinder.profile;

import com.estimote.sdk.Beacon;

/**
 * Created by thimokluser on 1/15/15.
 */
public interface ClosestBeaconDetectedListener {

    /**
     * Is called when the closest beacon has been determined
     * @param beacon Closest beacon
     */
    public void onClosestBeaconDetected(Beacon beacon);

    /**
     * Detecting a beacon has failed.
     */
    public void onNoBeaconDetected();

    /**
     * Other beacon was too close
     */
    public void onOtherBeaconTooClose();

    /**
     * The measurements have started
     */
    public void onMeasurementHasStarted(int durationOfMeasurement);
}
