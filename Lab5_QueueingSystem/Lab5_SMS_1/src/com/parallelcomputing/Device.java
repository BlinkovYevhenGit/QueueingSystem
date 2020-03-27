package com.parallelcomputing;


public class Device {

    private final String deviceName;

    public Device(int i) {
        deviceName = "Device #" + i;
    }

    public void process(Transaction transaction) {
        View.submitOutputTask("Device - " + deviceName + " thread "+ Thread.currentThread().getName() + " STARTED " + transaction);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        View.submitOutputTask("Device - " + deviceName + " thread "+ Thread.currentThread().getName() + " FINISHED " + transaction);
    }
}
