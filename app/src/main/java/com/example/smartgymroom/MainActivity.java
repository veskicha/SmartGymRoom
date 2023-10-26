package com.example.smartgymroom;

import android.Manifest;
import android.annotation.SuppressLint;
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

import java.io.ObjectInputStream;


import weka.classifiers.Classifier;

public class MainActivity extends AppCompatActivity {
    private boolean isButtonPressed = false;
    private MediaManager mediaManager;
    private static final String TAG = "DebugLogs";
    private Weka wekaManager;
    private static final String modelName = "tests/test_model_with_high_sliding_window_size.model";
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
        BluetoothCommunication bluetooth = new BluetoothCommunication(this);
        bluetooth.startScan();

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
