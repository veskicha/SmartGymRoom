package com.example.smartgymroom;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartgymroom.Location.*;


import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;

public class CurrentSessionFragment extends Fragment {

    private Activity activity;

    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;
    private boolean isButtonPressed = false;
    private boolean init = false;
    private MediaManager mediaManager;
    private static final String TAG = "DebugLogs";
    private Weka wekaManager;
    private static final String modelName = "tests/test_model_with_high_sliding_window_size.model";
//    private BluetoothLeScanner scanner;
    private final List<ScanFilter> filters = new ArrayList<>();
//    private final ScanSettings scanSettings = new ScanSettings.Builder()
//            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
//            .build();
    private SensorReading sensors;
    DataQueueManager manager = new DataQueueManager();
    TextView predictionTextView;

    private BeaconHandler beaconHandler;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private BeaconViewModel viewModel;
    private Location location = new Location();

    private Handler handler = new Handler();
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            predictionTextView.setText(wekaManager.getActivity());
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnableCode); // Stop the loop when the activity is destroyed
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_session, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = requireActivity();

        sensors = new SensorReading(manager);
        wekaManager = new Weka(activity, manager);  // Pass the host activity as context
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

        predictionTextView = view.findViewById(R.id.prediction_text_view);  // Use the provided view

        handler.post(runnableCode);

        mediaManager = new MediaManager(activity);  // Pass the host activity as context


        viewModel = new ViewModelProvider(this).get(BeaconViewModel.class);
        beaconHandler = new BeaconHandler(requireContext(), viewModel,this);
        beaconHandler.createBeacon();

        if (activity.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                activity.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }


        Button button = view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (!isButtonPressed) {
                    b.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed, null));
                    b.setTextColor(Color.parseColor("#000000"));
                    b.setText("Stop");
                    sensors.initSensors(sensorManager);
                    startChronometer(activity.findViewById(R.id.chronometer));
                    startMonitoring();
                } else {
                    b.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_border, null));
                    b.setTextColor(Color.parseColor("#2fff65"));
                    b.setText("Start");
                    sensors.stopSensors(sensorManager);
                    pauseChronometer(activity.findViewById(R.id.chronometer));
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetChronometer(activity.findViewById(R.id.chronometer));
                        }
                    }, 3000);

                }
                isButtonPressed = !isButtonPressed;
            }
        });

        chronometer = activity.findViewById(R.id.chronometer);
        chronometer.setFormat("%s");
    }

    private void startMonitoring() {
        String tag = "monitoring";
        Boolean currentState = viewModel.getIsBeaconSearchStarted();
        Log.d(tag, currentState.toString());

        beaconHandler.startBeaconMonitoring();
        viewModel.setBeaconsState(!currentState);
        viewModel.deleteBeacons();
        Log.d(tag, Boolean.toString(viewModel.getIsBeaconSearchStarted()));
    }

    public void foundBeacons(){
        String tag = "monitoring";
        Log.d(tag, "found 3 beacons");

        beaconHandler.stopBeaconMonitoring();

        Point point = location.getOurLocation(viewModel.getComparedBeaconsList());

        Log.d(tag, point.toString()+ " is our location");
        Room roomFinder = new Room();
        int room = roomFinder.getRoom(point);

        BluetoothCommunication bluetooth = new BluetoothCommunication(activity, 0);
        bluetooth.startScan();

    }

    private Classifier initializeWeka() {
        Classifier classifier = null;
        try {
            Activity activity = requireActivity();
            ObjectInputStream objectInputStream = new ObjectInputStream(activity.getAssets().open(modelName));
            classifier = (Classifier) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classifier;
    }

    public void startChronometer(View v) {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }

    public void pauseChronometer(View v) {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    public void resetChronometer(View v) {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }
}
