package com.parallelcomputing;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Transaction implements Runnable {

    private final SMS sms;
    private final String tnumber;
    public Thread deviceThread;
    public final Object threadLocker=new Object();
    public volatile boolean isReady=false;

    public Transaction(SMS sms, int tNumber) {
        this.sms = sms;
        this.tnumber = tNumber + "";
    }

    @Override
    public void run() {
        Future<?> device;

        device = sms.submitToQueue(this);
        if(device!=null){
            try {
                device.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
//        synchronized (threadLocker){
//            while (!isReady){
//                try {
//                    threadLocker.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        //View.submitOutputTask("Transaction " + tnumber + " thread " + Thread.currentThread().getName() + " - is Done");
    }

    @Override
    public String toString() {
        return "Transaction " + tnumber + " thread " + Thread.currentThread().getName();
    }
}
