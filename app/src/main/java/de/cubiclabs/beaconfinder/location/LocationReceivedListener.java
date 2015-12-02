package de.cubiclabs.beaconfinder.location;

import android.location.Location;

/**
 * Created by thimokluser on 1/24/15.
 */
public interface LocationReceivedListener {
    public void onLocationReceived(Location location);
}
