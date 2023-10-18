package com.example.smartgymroom;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import kotlinx.coroutines.CoroutineScope;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class MainActivity extends AppCompatActivity {

    private boolean init = false;
    private MediaManager mediaManager;
    private static final String TAG = "DebugLogs";
    private BluetoothLeScanner scanner;
    private final List<ScanFilter> filters = new ArrayList<>();
    private final ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build();
    private ArrayList<String> activityLabels = new ArrayList<>(Arrays.asList("strength", "cardio", "stretching"));
    private Classifier wekaModel;
    private SensorReading sensors;
    DataQueueManager manager = new DataQueueManager();

    private Handler handler = new Handler();
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            double[] averages = manager.getAverages();
            try {
                String prediction = classifyInstance(averages);
                Log.d("recognition", prediction);

                // Here you can update the UI elements if needed

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            handler.postDelayed(this, 1000);
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

        wekaModel = initializeWeka();
        sensors = new SensorReading(manager);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors.initSensors(sensorManager);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler.post(runnableCode);


        mediaManager = new MediaManager(this);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
        if (scanner != null && !init) {
            scanner.startScan(filters, scanSettings, scanCallback);
            Log.d(TAG, "scan started");
        } else {
            Log.e(TAG, "could not get scanner object");
        }

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
                device.connectGatt(MainActivity.this, false, gattCallback);

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
            ObjectInputStream objectInputStream = new ObjectInputStream(getAssets().open("tests/simple_test.model"));
            classifier = (Classifier) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classifier;
    }

    private String classifyInstance(double[] input) throws IOException {
        try {
            double result = wekaModel.classifyInstance(createInstance(input));
            return activityLabels.get((int) result);
        } catch (Exception e) {
            Log.d("WekaErrorE", e.getMessage());
            return "Error";
        }
    }

    private Instance createInstance(double[] inputData) {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("aX"));
        attributes.add(new Attribute("aY"));
        attributes.add(new Attribute("aZ"));
        attributes.add(new Attribute("mX"));
        attributes.add(new Attribute("mY"));
        attributes.add(new Attribute("mZ"));
        attributes.add(new Attribute("gX"));
        attributes.add(new Attribute("gY"));
        attributes.add(new Attribute("gZ"));
        Attribute activity = new Attribute("Activity", activityLabels);
        attributes.add(activity);

        Instances dataset = new Instances("TestInstances", new ArrayList<>(attributes), 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);
        Instance inst = new DenseInstance(1.0, inputData);
        dataset.add(inst);
        inst.setDataset(dataset);
        return inst;
    }

}
