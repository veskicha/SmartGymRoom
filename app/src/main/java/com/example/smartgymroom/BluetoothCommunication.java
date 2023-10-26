package com.example.smartgymroom;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothCommunication {

    private final Context context;
    private static final String TAG = "BluetoothLogs";
    private BluetoothLeScanner scanner;
    private boolean init = false;
    private final List<ScanFilter> filters = new ArrayList<>();
    private final ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build();

    BluetoothCommunication(Context context) {
        this.context = context;

    }



    public final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            @SuppressLint("MissingPermission") String deviceName = device.getName();
            if ("Nano 33 IoT".equals(deviceName)) {
                Log.d(TAG, "Nano 33 IoT device found, attempting to connect...");
                scanner.stopScan(this); // Stop the scan

                // TODO: Add your connection logic here. You'll probably need a GATT callback.
                BluetoothGatt bluetoothGatt = device.connectGatt(context, false, gattCallback);

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

    @SuppressLint("MissingPermission")
    public void startScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();


        if (scanner != null && !init) {
            scanner.startScan(filters, scanSettings, scanCallback);
            Log.d(TAG, "scan started");
        } else {
            Log.e(TAG, "could not get scanner object");
        }
    }
}
