package de.cubiclabs.beaconfinder.ui;


import android.content.Context;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.List;

import de.cubiclabs.beaconfinder.R;
import de.cubiclabs.beaconfinder.location.AddressManager;
import de.cubiclabs.beaconfinder.types.ProfileBeacon;
import de.cubiclabs.beaconfinder.util.BeaconUtils;

public class ProfileBeaconListAdapter extends ArrayAdapter<ProfileBeacon> {

    private List<ProfileBeacon> mItems;
    private List<Beacon> mLastAvailabilityList = new ArrayList<Beacon>();
    private Context mContext;

    public ProfileBeaconListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mItems = new ArrayList<ProfileBeacon>();
        mContext = context;
    }

    public ProfileBeaconListAdapter(Context context, int resource, List<ProfileBeacon> items) {
        super(context, resource, items);
        mItems = new ArrayList<ProfileBeacon>();
        mItems.addAll(items);
        mContext = context;
    }

    public void updateList(List<ProfileBeacon> items) {
        if(!BeaconUtils.profileBeaconListsAreEqual(items, mItems)) {
            mItems.clear();
            mItems.addAll(items);
            applyAvailability(mLastAvailabilityList);
            notifyDataSetChanged();
        } else {
            // Identical lists -> Update contents
            for(int i=0; i<mItems.size(); i++) {
                for(ProfileBeacon pb : items) {
                    if(mItems.get(i).getId() == pb.getId()) {
                        ProfileBeacon tmp = mItems.get(i);
                        tmp.setBeacon(pb.getBeacon());
                        tmp.setLocation(pb.getLastKnownGPS());
                        tmp.setName(pb.getName());
                        mItems.set(i, tmp);
                        break;
                    }
                }
            }
            applyAvailability(mLastAvailabilityList);
            notifyDataSetChanged();
        }
    }

    public void updateAvailability(List<Beacon> beacons) {
        mLastAvailabilityList = beacons;
        applyAvailability(beacons);
        notifyDataSetChanged();
    }

    private void applyAvailability(List<Beacon> beacons) {
        for(int i=0; i<mItems.size(); i++) {
            boolean isAvailable = false;
            for(Beacon beacon : beacons) {
                ProfileBeacon pb = BeaconUtils.getMatchingProfileBeacon(mItems, beacon);
                if(pb != null && mItems.get(i).getId() == pb.getId()) {
                    ProfileBeacon tmp = mItems.get(i);
                    tmp.setAvailability(true);
                    tmp.setBeacon(beacon);
                    mItems.set(i, tmp);
                    isAvailable = true;
                    break;
                }
            }
            if(!isAvailable) {
                //System.out.println("AWW... NOT AVAILABLE");
                mItems.get(i).setAvailability(false);
            }
        }

    }

    @Override
    public ProfileBeacon getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.beacon_list_item, null);

        }

        ProfileBeacon item = getItem(position);

        if (item != null) {

            final TextView tv1 = (TextView) v.findViewById(R.id.firstLine);
            final TextView tv2 = (TextView) v.findViewById(R.id.secondLine);
            final TextView tvDistance = (TextView) v.findViewById(R.id.txtDistance);

            if (tv1 != null) {
                tv1.setText(item.getName());
            }
            if (tv2 != null) {
                AddressManager addressManager = new AddressManager(mContext);
                addressManager.getAddress(item.getLastKnownGPS(), new AddressManager.AddressListener() {
                    @Override
                    public void onAddressReceived(String address) {
                        tv2.setText(address);
                    }

                    @Override
                    public void onAddressFailed() {
                        tv2.setText("Unbekannte Adresse");
                    }
                });

            }

            if(tvDistance != null) {
                if(item.isAvailable()) {
                    tvDistance.setBackground( tvDistance.getResources().getDrawable(getProximityDrawable(Utils.computeProximity(item.getBeacon()))));
                    tvDistance.setText(BeaconUtils.distance(item.getBeacon()) + "0m");
                } else {
                    tvDistance.setBackground(tvDistance.getResources().getDrawable(getProximityDrawable(Utils.Proximity.UNKNOWN)));
                    tvDistance.setText("n/a");
                }


            }
        }

        return v;

    }

    private static int getProximityDrawable(Utils.Proximity proximity) {
        if(proximity == Utils.Proximity.IMMEDIATE) {
            return R.drawable.circle_immediate;
        } else if(proximity == Utils.Proximity.NEAR) {
            return R.drawable.circle_near;
        } else if(proximity == Utils.Proximity.FAR) {
            return R.drawable.circle_far;
        } else {
            return R.drawable.circle_unknown;
        }
    }

}