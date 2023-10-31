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
    private final List<ScanFilter> filters = new ArrayList<>();
    private BluetoothGatt bGatt;
    private BluetoothGattCharacteristic characteristic;
    private final int roomNumber;
    private boolean foundLights = false;
    private boolean foundMusic = false;


    String[][] roomDetails = {{"lights 0","JBL Go 3"} ,{"lights 1","idk"}};


    private final ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build();

    BluetoothCommunication(Context context, int roomNumber) {
        this.context = context;
        this.roomNumber = roomNumber;

    }


    public final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String lightsName = roomDetails[roomNumber][0];
            String musicName = roomDetails[roomNumber][1];

            BluetoothDevice device = result.getDevice();
            @SuppressLint("MissingPermission") String deviceName = device.getName();
            if (lightsName.equals(deviceName)&&!foundLights) {
                Log.d(TAG, "Lights device found, attempting to connect...");
                foundLights = true;
                bGatt = device.connectGatt(context, false, gattCallback);

//            } else
//            if(musicName.equals(deviceName)&&!foundMusic){
//                Log.d(TAG, "Music device found, attempting to connect...");
//                foundMusic = true;
//                bGatt = device.connectGatt(context, false, gattCallback);
//
            }else if(foundMusic&&foundLights){
                scanner.stopScan(this);
            }else if (deviceName!=null){
                Log.d(TAG, deviceName+ " found, continuing scan...");
            }
        }



        @SuppressLint("MissingPermission")
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed with error: " + errorCode);
            scanner.startScan(filters, scanSettings, this);

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
                UUID SERVICE_UUID = UUID.fromString("00180A00-0010-00F8-0000-00805F9B34FB");
                UUID CHARACTERISTIC_UUID = UUID.fromString("2fe4da9b-57da-43a8-b8f9-8877344d7dc5");
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    Log.d(TAG, "service not null");

                    characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    sendMessage("2");
                } else {
                    Log.d(TAG, "Service is null");

                }
            } else {
                Log.d(TAG, "Bluetooth status not success");

            }
        }
    };

    private void sendMessage(String message) {
        if(characteristic!=null&&bGatt!=null) {
            byte[] data = message.getBytes(); // Convert your data to bytes
            characteristic.setValue(data);
            @SuppressLint("MissingPermission") boolean success = bGatt.writeCharacteristic(characteristic);

            if (success) {
                Log.d(TAG, "Writing characteristics successful");
            } else {
                Log.e(TAG, "Failed to write characteristics");
            }
        } else {
            Log.e(TAG, "In sendMessage characteristic or bGatt was null");

        }
    }

    @SuppressLint("MissingPermission")
    public void startScan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();


        if (scanner != null) {
            scanner.startScan(filters, scanSettings, scanCallback);
            Log.d(TAG, "scan started");
        } else {
            Log.e(TAG, "could not get scanner object");
        }
    }
}
