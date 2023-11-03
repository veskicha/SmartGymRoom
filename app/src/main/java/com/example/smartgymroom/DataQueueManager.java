package com.example.smartgymroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataQueueManager {

    private static final int QUEUE_SIZE_SECONDS = 4;
    public static final double INTERVAL_SECONDS = 0.5;
    private static final double QUEUE_SIZE = QUEUE_SIZE_SECONDS * SensorReading.frequency;
    private static final int QUEUE_COUNT = 6;
    private static final double AVERAGE_INTERVAL = INTERVAL_SECONDS * SensorReading.frequency;

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
        //queue order: accelerometer,(x,y,z) magnetometer (x,y,z)
        this.dataQueues = new DataQueue[QUEUE_COUNT];
        for (int i = 0; i < QUEUE_COUNT; i++) {
            dataQueues[i] = new DataQueue();
        }
    }

    private class DataQueue {
        private Queue<Double> queue;
        private int dataCountSinceLastAverage;
        private double mean;
        private double iQr;
        private double sTd;

        public DataQueue() {
            this.queue = new LinkedList<>();
            this.dataCountSinceLastAverage = 0;
            this.mean = 0.0;
            this.iQr = 0.0;
            this.sTd = 0.0;
//            this.scaleOverhead = 0.05;
        }

        public void addData(double data) {
            if (queue.size() == QUEUE_SIZE) {
                queue.poll(); // Remove the oldest data
            }
            queue.offer(data);

            if (queue.size() == QUEUE_SIZE && dataCountSinceLastAverage == 0) {
                calculateStatistics(); // Initial average calculation
                dataCountSinceLastAverage++;
            } else if (queue.size() == QUEUE_SIZE) {
                dataCountSinceLastAverage++;
            }


            if (dataCountSinceLastAverage == AVERAGE_INTERVAL) {
                calculateStatistics();
                dataCountSinceLastAverage = 1;
            }
        }

        void calculateStatistics() {
//            totalAverages++;

            // Calculate the mean
            mean = 0.0;
            for (double num : queue) {
                mean += num;
            }
            mean /= queue.size();
            System.out.println("queue: " + queue.size());

            // Calculate the standard deviation
            sTd = 0.0;
            for (double num : queue) {
                sTd += Math.pow(num - mean, 2);
            }
            sTd = Math.sqrt(sTd / QUEUE_SIZE);

            // Calculate the interquartile range
            List<Double> sortedList = new ArrayList<>(queue);
            Collections.sort(sortedList);
            System.out.println((QUEUE_SIZE + 1) / 4);
            int q1Index = (int) (((QUEUE_SIZE + 1) / 4) - 1);
            int q3Index = (int) (((3 * (QUEUE_SIZE + 1)) / 4) - 1);
            double q1 = sortedList.get(q1Index);
            double q3 = sortedList.get(q3Index);
            iQr = q3 - q1;

            System.out.println("New mean calculated: " + mean);
            System.out.println("New standard deviation calculated: " + sTd);
            System.out.println("New interquartile range calculated: " + iQr);
        }

    }


    public double[] getData() {
        double[] data = new double[12];
        int dataIndex = 0;
        for (int queueIndex = 0; queueIndex < 3; queueIndex++) {
            data[dataIndex] = dataQueues[queueIndex].mean;
            data[dataIndex + 1] = dataQueues[queueIndex].sTd;
            data[dataIndex + 2] = dataQueues[queueIndex].iQr;
            dataIndex++;
        }

        for (int restQueueIndex = 3; restQueueIndex < QUEUE_COUNT; restQueueIndex++) {
            data[dataIndex] = dataQueues[restQueueIndex].mean;
            dataIndex++;
        }
        return data;
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
