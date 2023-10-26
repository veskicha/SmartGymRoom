package com.example.smartgymroom;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
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

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import weka.classifiers.Classifier;

public class CurrentSessionFragment extends Fragment {

    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;
    private boolean isButtonPressed = false;
    private boolean init = false;
    private MediaManager mediaManager;
    private static final String TAG = "DebugLogs";
    private Weka wekaManager;
    private static final String modelName = "tests/test_model_with_high_sliding_window_size.model";
    private BluetoothLeScanner scanner;
    private final List<ScanFilter> filters = new ArrayList<>();
    private final ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build();
    private SensorReading sensors;
    DataQueueManager manager = new DataQueueManager();
    TextView predictionTextView;

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

        Activity activity = requireActivity();

        sensors = new SensorReading(manager);
        wekaManager = new Weka(activity, manager);  // Pass the host activity as context
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

        predictionTextView = view.findViewById(R.id.prediction_text_view);  // Use the provided view

        handler.post(runnableCode);

        mediaManager = new MediaManager(activity);  // Pass the host activity as context
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();

        if (activity.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                activity.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }

        if (scanner != null && !init) {
            scanner.startScan(filters, scanSettings, scanCallback);
            Log.d(TAG, "scan started");
        } else {
            Log.e(TAG, "could not get scanner object");
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

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (init) {
                scanner.stopScan(this);
            }
            @SuppressLint("MissingPermission") String deviceName = device.getName();
            if ("ESP32".equals(deviceName)) {
                Log.d(TAG, "ESP32 device found, attempting to connect...");
                scanner.stopScan(this);
                init = true;

                // TODO: Add your connection logic here. You'll probably need a GATT callback.
                device.connectGatt(requireActivity(), false, gattCallback);

            } else {
                Log.d(TAG, "Device found but not ESP32, continuing scan...");
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed with error: " + errorCode);
            if (!init) {
                scanner.startScan(filters, scanSettings, this);
            }
        }


    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // TODO: Here you can loop through available services and characteristics
                UUID SERVICE_UUID = UUID.fromString("2fe4da9b-57da-43a8-b8f9-8877344d7dc5");
                UUID CHARACTERISTIC_UUID = UUID.fromString("541426fd-debd-471d-8e1c-a4a18a837028");
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    gatt.setCharacteristicNotification(characteristic, true);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                        gatt.readCharacteristic(characteristic);
                        Log.d(TAG, "Reading characteristics..");
                    }

                }

                // to find the ones you're interested in.
                // For instance, you can call gatt.getServices() to retrieve a list of available services.

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

    };

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
