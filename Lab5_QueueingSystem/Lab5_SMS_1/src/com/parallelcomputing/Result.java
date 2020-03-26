package com.parallelcomputing;

public class Result {

    private final int transactionQuantity;

    private volatile double averageQueueLength;

    private long denyingNum = 0;

    public Result(int transactionQuantity) {
        this.transactionQuantity = transactionQuantity;
    }

    public double getAverageQueueLength() {
        return averageQueueLength;
    }

    public void setAverageQueueLength(double averageQueueLength) {
        this.averageQueueLength = averageQueueLength;
    }

    public synchronized double getDenyingProbability() {
        return denyingNum / (double)transactionQuantity;
    }

    public synchronized void incDenyingNumber() {
        this.denyingNum++;
    }
}
