package com.example.smartgymroom;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorReading {

    private DataQueueManager manager;
    private final int samplePeriod = 20000;

    public SensorReading(DataQueueManager manager) {
        this.manager = manager;
    }

    private void accelerometer(SensorManager sensorManager) {
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO: Handle accuracy changes
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d("D", "accelerometer.");
                Log.d("Dacc", event.values.toString());
                manager.addSensorData(event.values, 3);
            }
        };
        if (accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, samplePeriod);
        } else {
            Log.d("D", "No accelerometer detected.");
        }
    }

//    private void linearAcceleration(SensorManager sensorManager) {
//        Sensor linearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        SensorEventListener sensorEventListener = new SensorEventListener() {
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int accuracy) {
//                // TODO: Handle accuracy changes
//            }
//
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//                Log.d("Dlinacc", event.values.toString());
//                manager.addSensorData(calculateMagnitude(event.values[0], event.values[1], event.values[2]), 1);
//            }
//        };
//        if (linearAcc != null) {
//            sensorManager.registerListener(sensorEventListener, linearAcc, samplePeriod);
//        } else {
//            Log.d("D", "No linear acc detected.");
//        }
//    }

    private void gyroscope(SensorManager sensorManager) {
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO: Handle accuracy changes
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d("Dgyro", event.values.toString());
                manager.addSensorData(event.values, 9);
            }
        };
        if (gyroscope != null) {
            sensorManager.registerListener(sensorEventListener, gyroscope, samplePeriod);
        } else {
            Log.d("D", "No gyroscope detected.");
        }
    }

    private void magnometer(SensorManager sensorManager) {
        Sensor magnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO: Handle accuracy changes
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d("Dmagno", event.values.toString());
                manager.addSensorData(event.values, 6);
            }
        };
        if (magnometer != null) {
            sensorManager.registerListener(sensorEventListener, magnometer, samplePeriod);
        } else {
            Log.d("D", "No magnometer detected.");
        }
    }

    public void initSensors(SensorManager sensorManager) {
        accelerometer(sensorManager);
        magnometer(sensorManager);
        gyroscope(sensorManager);
//        linearAcceleration(sensorManager);
    }

//    public double calculateMagnitude(double x, double y, double z) {
//        return Math.sqrt(x * x + y * y + z * z);
//    }
}
