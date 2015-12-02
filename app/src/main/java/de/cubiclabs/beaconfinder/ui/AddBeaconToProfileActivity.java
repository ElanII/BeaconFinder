package de.cubiclabs.beaconfinder.ui;

import android.content.Intent;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.estimote.sdk.Beacon;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.cubiclabs.beaconfinder.R;
import de.cubiclabs.beaconfinder.database.ProfileBeaconDAO;
import de.cubiclabs.beaconfinder.location.LocationManager;
import de.cubiclabs.beaconfinder.location.LocationReceivedListener;
import de.cubiclabs.beaconfinder.profile.ClosestBeaconDetectedListener;
import de.cubiclabs.beaconfinder.profile.ClosestBeaconDetector;
import de.cubiclabs.beaconfinder.types.ProfileBeacon;

public class AddBeaconToProfileActivity extends ActionBarActivity implements
        ClosestBeaconDetectedListener {

    private ClosestBeaconDetector mBeaconDetector;
    private Beacon mDetectedBeacon;

    @InjectView(R.id.btnStartSearch) Button mBtnStartSearch;
    @InjectView(R.id.btnSave) Button mBtnSave;
    @InjectView(R.id.txtStatus) TextView mTxtStatus;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;
    @InjectView(R.id.etName) EditText mEtName;

    private CountDownTimer mTimer;
    private int mProgressStatus = 0;

    private enum State {
        BEFORE_SEARCH, SEARCH, BEACON_FOUND
    }
    private State mState = State.BEFORE_SEARCH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beacon_to_profile);
        ButterKnife.inject(this);

        if (savedInstanceState != null) {
            mState = (State)savedInstanceState.getSerializable("state");
            mEtName.setText(savedInstanceState.getString("name"));
            if(savedInstanceState.containsKey("beacon")) {
                mDetectedBeacon = savedInstanceState.getParcelable("beacon");
            }
            changeViewState(mState);
        } else {
            changeViewState(State.BEFORE_SEARCH);
        }

        mEtName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                invalidateOptionsMenu();
            }
        });

        setResult(RESULT_CANCELED);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("state", mState);
        savedInstanceState.putString("name", mEtName.getText().toString());
        if(mDetectedBeacon != null) {
            savedInstanceState.putParcelable("beacon", mDetectedBeacon);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTxtStatus.setText("Bitte auf den Button klicken");
    }

    @Override
    protected void onPause() {
        if(mBeaconDetector != null) {
            mTxtStatus.setText("Measurements were stopped!");
        }

        super.onPause();
    }


    @Override
    public boolean onPrepareOptionsMenu (Menu menu){
        super.onPrepareOptionsMenu(menu);

        if(mDetectedBeacon == null) {
            menu.findItem(R.id.action_accept).setEnabled(false);
            menu.findItem(R.id.action_cancel).setEnabled(false);
        } else {
            menu.findItem(R.id.action_accept).setEnabled(true);
            menu.findItem(R.id.action_cancel).setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_beacon_to_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_accept:
                storeBeaconInDatabase();
                break;
            case R.id.action_cancel:
                onBackPressed();
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void storeBeaconInDatabase() {
        if(mDetectedBeacon == null) return;

        ProfileBeacon pb = new ProfileBeacon(mDetectedBeacon, mEtName.getText().toString(), 0.0, 0.0, "");

        ProfileBeaconDAO dao = new ProfileBeaconDAO(getBaseContext());
        dao.create(pb);

        setResult(RESULT_OK);

        onBackPressed();
    }


    @OnClick(R.id.btnStartSearch)
    public void btnSearch_Clicked(View view) {
        mTxtStatus.setText("Preparing measurements");
        mBeaconDetector = new ClosestBeaconDetector();
        mBeaconDetector.start(getBaseContext(), (ClosestBeaconDetectedListener)this);
        mProgressBar.setProgress(0);
        mProgressStatus = 0;
        mBtnStartSearch.setActivated(false);
        changeViewState(State.SEARCH);
    }

    @OnClick(R.id.btnSave)
    public void btnSave_Clicked(View view) {
        System.out.println("btnSave_Clicked");
        storeBeaconInDatabase();
    }

    @Override
    public void onClosestBeaconDetected(Beacon beacon) {
        mDetectedBeacon = beacon;
        mTxtStatus.setText("Closest beacon: " + beacon.getMacAddress());
        stopProgressBar();
        changeViewState(State.BEACON_FOUND);
    }

    @Override
    public void onOtherBeaconTooClose() {
        mTxtStatus.setText("Beacons are too close");
        stopProgressBar();
        mBtnStartSearch.setActivated(true);
        changeViewState(State.SEARCH);
    }

    @Override
    public void onNoBeaconDetected() {
        mTxtStatus.setText("FAILED");
        stopProgressBar();
        mBtnStartSearch.setActivated(true);
        changeViewState(State.SEARCH);
    }

    private void stopProgressBar() {
        if(mTimer != null) mTimer.cancel();
        mProgressBar.setProgress(mProgressBar.getMax());
    }

    @Override
    public void onMeasurementHasStarted(int durationOfMeasurement) {
        mTxtStatus.setText("Measurements have started");

        int resolution = 200;
        final int timePerTick = durationOfMeasurement / resolution;
        mProgressBar.setMax(resolution);
        mProgressBar.setProgress(0);
        mProgressStatus = 0;

        mTimer = new CountDownTimer(durationOfMeasurement, timePerTick) {

            public void onTick(long millisUntilFinished) {
                mProgressStatus++;
                mProgressBar.setProgress(mProgressStatus);
            }

            public void onFinish() {
                mProgressBar.setProgress(100);
            }
        }.start();
    }

    private void changeViewState(State state) {
        mState = state;
        switch(state) {
            default:
            case BEFORE_SEARCH:
                mBtnStartSearch.setVisibility(View.VISIBLE);
                mBtnSave.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                mEtName.setVisibility(View.GONE);
                mTxtStatus.setVisibility(View.VISIBLE);
                break;
            case SEARCH:
                mBtnStartSearch.setVisibility(View.VISIBLE);
                mBtnSave.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mEtName.setVisibility(View.GONE);
                mTxtStatus.setVisibility(View.VISIBLE);
                break;
            case BEACON_FOUND:
                mBtnStartSearch.setVisibility(View.GONE);
                mBtnSave.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mEtName.setVisibility(View.VISIBLE);
                mTxtStatus.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
                break;
        }
    }

}
