package de.cubiclabs.beaconfinder.types;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

import java.util.Iterator;

public class BeaconWithValueHistory extends Beacon {

    private CircularBuffer<AccuracyWithTimestamp> mAccuracies;
    private static final int BUFFER_SIZE = 100;

    public BeaconWithValueHistory(Beacon beacon) {
        super(beacon.getProximityUUID(), beacon.getName(), beacon.getMacAddress(),
                beacon.getMajor(), beacon.getMinor(),
                beacon.getMeasuredPower(), beacon.getRssi());

        // Init buffer
        mAccuracies = new CircularBuffer<AccuracyWithTimestamp>(BUFFER_SIZE);

        // Add initial beacon to buffer
        add(beacon);
    }

    public void add(Beacon beacon) {
        mAccuracies.add(new AccuracyWithTimestamp(Utils.computeAccuracy(beacon)));
    }

    public double getAverageAccuracy(long age) {
        long now = System.currentTimeMillis();

        double sum = 0;
        int iterations = 0;
        Iterator<AccuracyWithTimestamp> iterator = mAccuracies.iterator();
        while(iterator.hasNext()) {
            AccuracyWithTimestamp item = iterator.next();
            if(age != -1 && now - item.getTimestamp() > age) break;
            sum += item.getAccuracy();
            iterations++;
        }

        if(iterations == 0) return 0.0;
        else return sum / iterations;
    }

    public double getAverageAccuracy() {
        return getAverageAccuracy(-1);
    }

    public int compareTo(BeaconWithValueHistory o) {
        double accuracySelf = this.getAverageAccuracy();
        double accuracyOther = o.getAverageAccuracy();

        return Double.compare(accuracySelf, accuracyOther);
    }

    /**
     * Stores the timestamp of when the beacon is stored.
     */
    private class AccuracyWithTimestamp {
        private long mTimestamp;
        private double mAccuracy;

        public AccuracyWithTimestamp(double accuracy) {
            mTimestamp = System.currentTimeMillis();
            mAccuracy = accuracy;
        }

        public long getTimestamp() {
            return mTimestamp;
        }

        public double getAccuracy() {
            return mAccuracy;
        }
    }

    /**
     * Determines whether the passed on beacon is represented by this BeaconWithValueHistory instance
     * @param beacon Beacon
     * @return True if the passed beacon is represented by this instance.
     */
    public boolean equalsTo(Beacon beacon) {
        return this.getMacAddress() == beacon.getMacAddress();
    }

    /**
     * Get the beacon, this instance represents
     * @return Th represented beacon with the average Rssi
     */
    public Beacon toBeacon() {
        return new Beacon(this.getProximityUUID(), this.getName(), this.getMacAddress(),
                this.getMajor(), this.getMinor(),
                this.getMeasuredPower(), this.getRssi());
    }

}
