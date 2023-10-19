package com.example.smartgymroom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataQueueManager {

    private static final int QUEUE_SIZE = 50;
    private static final int QUEUE_COUNT = 9;
    private static final int AVERAGE_INTERVAL = 10;

    public String getMostFrequentString(ArrayList<String> queue) {
        return queue.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private DataQueue[] dataQueues;

    public DataQueueManager() {
        //queue order: accelerometer (x,y,z) LinAccelerometer (x,y,z) gyroscope (x,y,z) magnometer (x,y,z)
        this.dataQueues = new DataQueue[QUEUE_COUNT];
        for (int i = 0; i < QUEUE_COUNT; i++) {
            dataQueues[i] = new DataQueue();
        }
    }

    private class DataQueue {
        private Queue<Double> queue;
        private int dataCountSinceLastAverage;
        private double currentAverage;

        public DataQueue() {
            this.queue = new LinkedList<>();
            this.dataCountSinceLastAverage = 0;
            this.currentAverage = 0.0;
        }

        public void addData(double data) {
            if (queue.size() == QUEUE_SIZE) {
                queue.poll(); // Remove the oldest data
            }
            queue.offer(data);

            if (queue.size() == QUEUE_SIZE && dataCountSinceLastAverage == 0) {
                calculateAverage(); // Initial average calculation
                dataCountSinceLastAverage++;
            } else if (queue.size() == QUEUE_SIZE) {
                dataCountSinceLastAverage++;
            }


            if (dataCountSinceLastAverage == AVERAGE_INTERVAL) {
                calculateAverage();
                dataCountSinceLastAverage = 1;
            }
        }

        private void calculateAverage() {
            double sum = 0.0;
            for (double value : queue) {
                sum += value;
            }
            currentAverage = sum / queue.size();
            System.out.println("New average calculated: " + currentAverage);
        }
    }

    public double[] getAverages() {
        double[] averages = new double[QUEUE_COUNT];
        for (int i = 0; i < dataQueues.length; i++) {
            averages[i] = dataQueues[i].currentAverage;
        }
        return averages;
    }

    // Method to add data from sensors. For demonstration, this just accepts an array.
    public void addSensorData(float[] sensorData, int queueIndex) {
        int j = 0;
        for (int i = queueIndex - 3; i < queueIndex; i++) {
            dataQueues[i].addData(sensorData[j]);
            j++;
        }
    }

}
