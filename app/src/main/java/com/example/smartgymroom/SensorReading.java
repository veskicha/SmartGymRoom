package com.example.smartgymroom;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorReading {

    private boolean sensorsState = false;
    private DataQueueManager manager;
    private static final int samplePeriod = 20000;
    public static final double frequency = 1 / ((double) samplePeriod / 1000000);

    private SensorEventListener accelerometerListener;
    private SensorEventListener gyroscopeListener;
    private SensorEventListener magnometerListener;

    public SensorReading(DataQueueManager manager) {
        this.manager = manager;
    }

    private void accelerometer(SensorManager sensorManager) {
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometerListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO: Handle accuracy changes
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                manager.addSensorData(event.values, 3);
            }
        };
        if (accelerometer != null) {
            sensorManager.registerListener(accelerometerListener, accelerometer, samplePeriod);
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


    private void magnometer(SensorManager sensorManager) {
        Sensor magnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magnometerListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO: Handle accuracy changes
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                manager.addSensorData(event.values, 6);
            }
        };
        if (magnometer != null) {
            sensorManager.registerListener(magnometerListener, magnometer, samplePeriod);
        } else {
            Log.d("D", "No magnometer detected.");
        }
    }

    public void initSensors(SensorManager sensorManager) {
        if (!sensorsState) {
            accelerometer(sensorManager);
            magnometer(sensorManager);
            sensorsState = true;
        }
    }

    public void stopSensors(SensorManager sensorManager) {
        if (sensorsState) {
            sensorManager.unregisterListener(gyroscopeListener);
            sensorManager.unregisterListener(accelerometerListener);
            sensorManager.unregisterListener(magnometerListener);
            sensorsState = false;
        }
    }



//    public double calculateMagnitude(double x, double y, double z) {
//        return Math.sqrt(x * x + y * y + z * z);
//    }
}
