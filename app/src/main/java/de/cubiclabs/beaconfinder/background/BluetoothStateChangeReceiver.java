package de.cubiclabs.beaconfinder.background;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by thimokluser on 1/14/15.
 */
public class BluetoothStateChangeReceiver extends BroadcastReceiver {
    private Intent mEstimoteServiceIntent;

    // Method called when bluetooth is turned on or off.
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
                    // When bluetooth is turning off, lets stop estimotes ranging
                    if (mEstimoteServiceIntent != null) {
                        context.stopService(mEstimoteServiceIntent);
                        mEstimoteServiceIntent = null;
                    }
                    break;
                case BluetoothAdapter.STATE_ON:
                    // When bluethooth is turned on, let's start estimotes monitoring
                    if (mEstimoteServiceIntent == null) {
                        mEstimoteServiceIntent = new Intent(context,
                                EstimoteService.class);
                        context.startService(mEstimoteServiceIntent);
                    }
                    break;
            }
        } else if(action.equals("de.cubiclabs.beaconfinder.APP_STARTED")) {
            try {

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (bluetoothAdapter == null) {
                    System.out.println("Bluetooth is not available");
                    return;
                }

                final int state = bluetoothAdapter.getState();
                if(state == BluetoothAdapter.STATE_ON) {
                    System.out.println("Bluetooth is on -> start Ranging!!!!! ");
                    if (mEstimoteServiceIntent == null) {
                        mEstimoteServiceIntent = new Intent(context,
                                EstimoteService.class);
                        context.startService(mEstimoteServiceIntent);
                    }
                }
            } catch (Exception e) {
                System.out.println("Couldn't get bluetooth adapter");
            }
        }
    }
}