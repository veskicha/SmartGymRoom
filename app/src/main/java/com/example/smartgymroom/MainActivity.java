package com.example.smartgymroom;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import weka.classifiers.Classifier;

public class MainActivity extends AppCompatActivity {

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
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnableCode); // Stop the loop when the activity is destroyed
    }
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
        sensors = new SensorReading(manager);
        wekaManager = new Weka(this, manager);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors.initSensors(sensorManager);
        Log.d("Sensors started", "in oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        predictionTextView = findViewById(R.id.prediction_text_view);

        handler.post(runnableCode);


        mediaManager = new MediaManager(this);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();


        if (scanner != null && !init) {
            scanner.startScan(filters, scanSettings, scanCallback);
            Log.d(TAG, "scan started");
        } else {
            Log.e(TAG, "could not get scanner object");
        }

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (!isButtonPressed) {
                    b.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed, null));
                    b.setTextColor(Color.parseColor("#000000"));
                    b.setText("Stop");
                    sensors.toggleSensors(sensorManager);
                } else {
                    b.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_border, null));
                    b.setTextColor(Color.parseColor("#2fff65"));
                    b.setText("Start");
                    sensors.toggleSensors(sensorManager);
                }
                isButtonPressed = !isButtonPressed;
            }
        });

    }
    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            @SuppressLint("MissingPermission") String deviceName = device.getName();
            if ("Nano 33 IoT".equals(deviceName)) {
                Log.d(TAG, "Nano 33 IoT device found, attempting to connect...");
                scanner.stopScan(this); // Stop the scan

                // TODO: Add your connection logic here. You'll probably need a GATT callback.
                BluetoothGatt bluetoothGatt = device.connectGatt(MainActivity.this, false, gattCallback);

            } else {
                Log.d(TAG, "Device found but not Nano 33 IoT, continuing scan...");
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
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.d(TAG, "in onservicesdiscovered");
//
//                // TODO: Here you can loop through available services and characteristics
//                UUID SERVICE_UUID = UUID.fromString("180A");
//                UUID CHARACTERISTIC_UUID = UUID.fromString("00002A57-0000-1000-80000-00805F9B34FB");
//                BluetoothGattService service = gatt.getService(SERVICE_UUID);
//
//                if (service != null) {
//                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
//
//                    // Write your data to the characteristic here
////                    byte[] data = "your_data_here".getBytes(); // Convert your data to bytes
////                    characteristic.setValue(data);
////                    boolean success = gatt.writeCharacteristic(characteristic);
////
////                    if (success) {
////                        Log.d(TAG, "Writing characteristics successful");
////                    } else {
////                        Log.e(TAG, "Failed to write characteristics");
////                    }
//                } else {
//                    Log.e(TAG, "Characteristic not found");
//                }
//
////                if (service != null) {
////                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
////                    gatt.setCharacteristicNotification(characteristic, true);
//////                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//////                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//////                    gatt.writeDescriptor(descriptor);
////                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
//////                        gatt.readCharacteristic(characteristic);
////                        Log.d(TAG, "Writing characteristics..");
////                    }
////
////                }
//
//                // to find the ones you're interested in.
//                // For instance, you can call gatt.getServices() to retrieve a list of available services.
//
//            } else {
//                Log.w(TAG, "onServicesDiscovered received: " + status);
//            }
//        }
//        @SuppressLint("MissingPermission")
//        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Status success");

                // TODO: Here you can loop through available services and characteristics
                /**UUID SERVICE_UUID = UUID.fromString("2fe4da9b-57da-43a8-b8f9-8877344d7dc5");
                 UUID CHARACTERISTIC_UUID = UUID.fromString("541426fd-debd-471d-8e1c-a4a18a837028");*/
                UUID SERVICE_UUID = UUID.fromString("00180A00-0010-00F8-0000-00805F9B34FB");
                UUID CHARACTERISTIC_UUID = UUID.fromString("2fe4da9b-57da-43a8-b8f9-8877344d7dc5");
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                Log.d(TAG, "Before if");

                if (service != null) {
                    Log.d(TAG, "service not null");

                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
//                    gatt.setCharacteristicNotification(characteristic, true);
//                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                    gatt.writeDescriptor(descriptor);
                    byte[] data = "0".getBytes(); // Convert your data to bytes
                    characteristic.setValue(data);
                    boolean success = gatt.writeCharacteristic(characteristic);

                    if (success) {
                        Log.d(TAG, "Writing characteristics successful");
                    } else {
                        Log.e(TAG, "Failed to write characteristics");
                    }
//                    if (characteristic != null) {
//                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
//                            gatt.readCharacteristic(characteristic);
//                            Log.d(TAG, "Reading characteristics..");
//                        }
//                    }
                } else {
                    Log.d(TAG, "go home");

                }
            } else {
                Log.d(TAG, "go home 2.0");

            }
        }
    };

    private Classifier initializeWeka() {
        Classifier classifier = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(getAssets().open(modelName));
            classifier = (Classifier) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classifier;
    }

}
