package de.cubiclabs.beaconfinder.background;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Created by thimokluser on 1/14/15.
 */
public class EstimoteService extends Service {
    private EstimoteManager mEstimoteManager;

    public static String PROFILE_REFRESHED_INTENT = "de.cubiclabs.beaconfinder.PROFILE_REFRESHED_INTENT";
    public static String MAIN_ACTIVITY_ACTIVE_INTENT = "de.cubiclabs.beaconfinder.MAIN_ACTIVITY_ACTIVE_INTENT";
    public static String MAIN_ACTIVITY_INACTIVE_INTENT = "de.cubiclabs.beaconfinder.MAIN_ACTIVITY_ACTIVE_INACTIVE_INTENT";
    private UpdateProfileReceiver mProfileReceiver;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mEstimoteManager = new EstimoteManager();
            mEstimoteManager.create((NotificationManager) this
                            .getSystemService(Context.NOTIFICATION_SERVICE), this,
                    intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(mProfileReceiver == null) mProfileReceiver = new UpdateProfileReceiver();
        IntentFilter intentFilter = new IntentFilter(EstimoteService.PROFILE_REFRESHED_INTENT);
        intentFilter.addAction(EstimoteService.MAIN_ACTIVITY_ACTIVE_INTENT);
        intentFilter.addAction(EstimoteService.MAIN_ACTIVITY_INACTIVE_INTENT);
        registerReceiver(mProfileReceiver, intentFilter);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEstimoteManager.stop();
        unregisterReceiver(mProfileReceiver);
    }

    private class UpdateProfileReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mEstimoteManager == null) return;
            if (intent.getAction().equals(EstimoteService.PROFILE_REFRESHED_INTENT)) {
                mEstimoteManager.refreshProfile();
            } else if (intent.getAction().equals(EstimoteService.MAIN_ACTIVITY_ACTIVE_INTENT)) {
                mEstimoteManager.fastRanging();
            } else if (intent.getAction().equals(EstimoteService.MAIN_ACTIVITY_INACTIVE_INTENT)) {
                mEstimoteManager.slowRanging();
            }
        }
    }

}
