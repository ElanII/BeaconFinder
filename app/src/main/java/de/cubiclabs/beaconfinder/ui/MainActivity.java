package de.cubiclabs.beaconfinder.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import de.cubiclabs.beaconfinder.R;
import de.cubiclabs.beaconfinder.background.BluetoothStateChangeReceiver;
import de.cubiclabs.beaconfinder.background.EstimoteService;
import de.cubiclabs.beaconfinder.database.ProfileBeaconDAO;
import de.cubiclabs.beaconfinder.types.ProfileBeacon;


public class MainActivity extends ActionBarActivity {

    private static final int ACTIVITY_ADD_BEACON_TO_PROFILE = 1000;

    public static String REFRESH_LIST_INTENT = "de.cubiclabs.beaconfinder.REFRESH_LIST_INTENT";
    public static String AVAILABLE_PROFILE_BEACONS_INTENT = "de.cubiclabs.beaconfinder.AVAILABLE_PROFILE_BEACONS_INTENT";
    private UpdateBeaconListReceiver mBeaconListReceiver;

    @InjectView(R.id.beaconListView) ListView mBeaconListView;
    private ProfileBeaconListAdapter mBeaconListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mBeaconListAdapter = new ProfileBeaconListAdapter(getBaseContext(), R.layout.beacon_list_item);
        mBeaconListView.setEmptyView(findViewById(android.R.id.empty));
        mBeaconListView.setAdapter(mBeaconListAdapter);

        registerForContextMenu(mBeaconListView);

        mBeaconListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Beacon beacon = mBeaconListAdapter.getItem(position).getBeacon();
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("beacon", beacon);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.beaconListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(mBeaconListAdapter.getItem(info.position).getName());
            String[] menuItems = getResources().getStringArray(R.array.contextMenu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.contextMenu);
        String menuItemName = menuItems[menuItemIndex];

        switch(item.getOrder()) {
            case 0:
                ProfileBeacon pb = mBeaconListAdapter.getItem(info.position);

                ProfileBeaconDAO dao = new ProfileBeaconDAO(getBaseContext());
                dao.delete(pb);

                refreshBeaconListView();

                Intent intent = new Intent();
                intent.setAction(EstimoteService.PROFILE_REFRESHED_INTENT);
                sendBroadcast(intent);
                break;
            case 1:
                // Mark as lost
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent();
        intent.setAction("de.cubiclabs.beaconfinder.APP_STARTED");
        sendBroadcast(intent);

        Intent intent2 = new Intent();
        intent2.setAction(EstimoteService.MAIN_ACTIVITY_ACTIVE_INTENT);
        sendBroadcast(intent2);


        if(mBeaconListReceiver == null) mBeaconListReceiver = new UpdateBeaconListReceiver();
        IntentFilter intentFilter = new IntentFilter(MainActivity.REFRESH_LIST_INTENT);
        intentFilter.addAction(MainActivity.AVAILABLE_PROFILE_BEACONS_INTENT);
        registerReceiver(mBeaconListReceiver, intentFilter);

        refreshBeaconListView();
    }

    @Override
    protected void onPause() {
        if(mBeaconListReceiver != null) unregisterReceiver(mBeaconListReceiver);

        Intent intent = new Intent();
        intent.setAction(EstimoteService.MAIN_ACTIVITY_INACTIVE_INTENT);
        sendBroadcast(intent);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_add_beacon_to_profile) {
            Intent intent = new Intent(this, AddBeaconToProfileActivity.class);
            startActivityForResult(intent, MainActivity.ACTIVITY_ADD_BEACON_TO_PROFILE);
        } else if(id == R.id.action_refresh) {
            refreshBeaconListView();
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshBeaconListView() {
        //Toast.makeText(getApplicationContext(), "List updated",
        //        Toast.LENGTH_SHORT).show();

        ProfileBeaconDAO dao = new ProfileBeaconDAO(getBaseContext());
        mBeaconListAdapter.updateList(dao.getAll());
    }

    private void updateBeaconListAvailability(ArrayList<Beacon> beacons) {
        mBeaconListAdapter.updateAvailability(beacons);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MainActivity.ACTIVITY_ADD_BEACON_TO_PROFILE
                && resultCode == RESULT_OK) {
            refreshBeaconListView();

            Intent intent = new Intent();
            intent.setAction(EstimoteService.PROFILE_REFRESHED_INTENT);
            sendBroadcast(intent);
        }
    }

    private class UpdateBeaconListReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.REFRESH_LIST_INTENT)) {

                //System.out.println("Received REFRESH_LIST_INTENT");
                refreshBeaconListView();
            } else if(intent.getAction().equals(MainActivity.AVAILABLE_PROFILE_BEACONS_INTENT)) {
                //System.out.println("Received AVAILABLE_PROFILE_BEACONS_INTENT");
                ArrayList<Beacon> beacons = intent.getParcelableArrayListExtra("availableBeacons");
                updateBeaconListAvailability(beacons);
            }
        }
    }

}
