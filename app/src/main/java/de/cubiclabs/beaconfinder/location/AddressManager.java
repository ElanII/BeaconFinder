package de.cubiclabs.beaconfinder.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressManager {

    private Context mContext;

    public interface AddressListener {
        public void onAddressReceived(String address);
        public void onAddressFailed();
    }

    public AddressManager(Context context) {
        mContext = context;
    }

    public void getAddress(Location location, AddressListener receiver) {
        (new GetAddressTask(mContext, receiver)).execute(location);
    }

    private class GetAddressTask extends
            AsyncTask<Location, Void, String> {
        private Context mContext;
        private boolean mSuccess = true;
        private AddressListener mListener;

        public GetAddressTask(Context context, AddressListener receiver) {
            super();
            mContext = context;
            mListener = receiver;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         * @params params One or more Location objects
         */
        @Override
        protected String doInBackground(Location... params) {
            mSuccess = true;

            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            Location loc = params[0];
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
            } catch (IOException e1) {
                e1.printStackTrace();
                mSuccess = false;
                return ("Failed");
            } catch (IllegalArgumentException e2) {
                mSuccess = false;
                return "Failed";
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                String addressText = String.format(
                        "%s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality());

                return addressText;
            } else {
                mSuccess = false;
                return "No address found";
            }
        }

        @Override
        protected void onPostExecute(String address) {
            if(mListener != null) {
                if (mSuccess) {
                    mListener.onAddressReceived(address);
                } else {
                    mListener.onAddressFailed();
                }
            }
        }
    }

}
