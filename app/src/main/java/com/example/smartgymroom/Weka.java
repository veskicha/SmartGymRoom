package com.example.smartgymroom;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class Weka {

    private static Classifier wekaModel;
    private static final ArrayList<String> activityLabels = new ArrayList<>(Arrays.asList("strength", "cardio", "stretching"));
    private static final String modelName = "lmt-all-data-combined.model";
    private DataQueueManager manager;
    private ArrayList<String> recentClassifications;

    private Handler handler = new Handler();
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            double[] averages = manager.getData();
            try {
                String prediction = classifyInstance(averages);
                Log.d("ML", prediction);
                recentClassifications.add(prediction);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            handler.postDelayed(this, (long) (DataQueueManager.INTERVAL_SECONDS * 0.5 * 1000));
        }
    };

    public Weka(Context context, DataQueueManager manager) {
        wekaModel = initializeWeka(context);
        this.manager = manager;
        recentClassifications = new ArrayList<>();

    }

    public String getActivity() {
        String mostFrequestActivity = manager.getMostFrequentString(recentClassifications);
        recentClassifications.clear();
        return mostFrequestActivity;
    }

    public void startClassifying() {
        handler.post(runnableCode);
    }

    public void stopClassifying() {
        handler.removeCallbacks(runnableCode);
    }


    private Classifier initializeWeka(Context context) {
        Classifier classifier = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(context.getAssets().open(modelName));
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
        String[] attributeNames = new String[]{"aX", "aXstd", "aXiqr", "aY", "aYstd", "aYiqr", "aZ", "aZstd", "aZiqr", "mX", "mY", "mZ"};
        for (String attributeName : attributeNames) {
            attributes.add(new Attribute(attributeName));
        }
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
