package de.cubiclabs.beaconfinder.util;

import com.estimote.sdk.Beacon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import de.cubiclabs.beaconfinder.types.ProfileBeacon;

public class BeaconUtils {

    public static boolean profileBeaconContainsBeacon(ProfileBeacon pb, Beacon b) {
        Beacon beaconFromProfile = pb.getBeacon();
        return (beaconFromProfile.getProximityUUID().equals(b.getProximityUUID()) &&
                beaconFromProfile.getMajor() == b.getMajor() &&
                beaconFromProfile.getMinor() == b.getMinor());
    }

    public static ProfileBeacon getMatchingProfileBeacon(List<ProfileBeacon> profileBeacons, Beacon beacon) {
        for(ProfileBeacon pb : profileBeacons) {
            if(BeaconUtils.profileBeaconContainsBeacon(pb, beacon)) {
                return pb;
            }
        }
        return null;
    }

    public static boolean profileBeaconListsAreEqual(List<ProfileBeacon> listA, List<ProfileBeacon> listB) {
        if(listA.size() != listB.size()) return false;
        boolean foundAll = true;
        for(ProfileBeacon pbA : listA) {
            boolean found = false;
            for(ProfileBeacon pbB : listB) {
                if(pbA.getId() == pbB.getId()) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                foundAll = false;
                break;
            }
        }
        return foundAll;
    }

    public static double distance(Beacon beacon) {
        return round(calculateAccuracy(beacon.getMeasuredPower(), beacon.getRssi()), 1);
    }

    public static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return 2.0*Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return 2.0*accuracy;
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
