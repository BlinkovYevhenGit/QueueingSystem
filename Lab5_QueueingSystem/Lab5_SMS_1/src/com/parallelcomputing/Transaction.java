package com.parallelcomputing;


public class Transaction implements Runnable {

    private final SMS sms;
    private final String tnumber;

    public Transaction(SMS sms, int tNumber) {
        this.sms = sms;
        this.tnumber = tNumber + "";
    }

    @Override
    public void run() {
        sms.submitToQueue(this);
        View.submitOutputTask("Transaction " + tnumber + " thread " + Thread.currentThread().getName() + " - is Done");
    }

    @Override
    public String toString() {
        return "Transaction " + tnumber + " thread " + Thread.currentThread().getName();
    }
}
