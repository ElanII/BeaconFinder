package de.cubiclabs.beaconfinder.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.estimote.sdk.Beacon;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import de.cubiclabs.beaconfinder.R;
import de.cubiclabs.beaconfinder.database.ProfileBeaconDAO;
import de.cubiclabs.beaconfinder.types.ProfileBeacon;
import de.cubiclabs.beaconfinder.util.BeaconUtils;

public class MapActivity extends ActionBarActivity implements OnMapReadyCallback {

    private ProfileBeacon mProfileBeacon;
    private Beacon mBeacon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(getIntent() != null && getIntent().hasExtra("beacon")) {
            mBeacon = (Beacon) getIntent().getParcelableExtra("beacon");
        } else if(savedInstanceState != null) {
            mBeacon = (Beacon) savedInstanceState.getParcelable("beacon");
        }

        if(mBeacon == null) finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ProfileBeaconDAO dao = new ProfileBeaconDAO(this);
        mProfileBeacon = BeaconUtils.getMatchingProfileBeacon(dao.getAll(), mBeacon);
        if(mProfileBeacon == null) finish();

        setTitle(mProfileBeacon.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng cameraLocation = new LatLng(mProfileBeacon.getLastKnownLatitude(), mProfileBeacon.getLastKnownLongitude());

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraLocation, 19));

        map.addMarker(new MarkerOptions()
                .title(mProfileBeacon.getName())
                .position(cameraLocation));
    }
}
