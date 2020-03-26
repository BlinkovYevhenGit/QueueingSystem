package com.parallelcomputing;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


class SMS {

    private static final int DEVICE_NUMBER = 1;

    private final QueueStat queueStat;

    private final Result result;
    private final int transactionQuantity;

    private final Object deviceLock = new Object();

    private boolean isFree = true;

    private final ThreadLocal<Device> devices;
    private final ThreadPoolExecutor deviceExecutor;
    private final AtomicInteger deviceNumber = new AtomicInteger();

    SMS(int transactionQuantity) {
        this.transactionQuantity = transactionQuantity;
        this.result = new Result(this.transactionQuantity);

        BlockingQueue<Runnable> workingQueue = new LinkedBlockingDeque<>(QueueStat.CAPACITY);
        this.queueStat = new QueueStat();

        this.deviceExecutor = new ThreadPoolExecutor(
                DEVICE_NUMBER,
                DEVICE_NUMBER,
                0,
                TimeUnit.MILLISECONDS,
                workingQueue,
                new ThreadFactory() {
                    private int threadsNum = 0;

                    @Override
                    public Thread newThread(Runnable runnable){
                        threadsNum++;
                        return new Thread(runnable, String.format("device-executor-%d", threadsNum));
                    }
                }
        );

        this.devices = ThreadLocal.withInitial(() -> new Device(deviceNumber.incrementAndGet()));
    }

    public Result calculateResult(long time) {
        result.setAverageQueueLength(countAverageNumberOfTransactsInQueue(time));
        return result;
    }

    public void submitToQueue(Transaction transaction) {
        int queueLength = deviceExecutor.getQueue().size();
        queueStat.updateQueueStat(queueLength);
        View.submitOutputTask("ВХІД в чергу - кількість транзактів: "+queueLength);
        try {


            deviceExecutor.submit(() -> {
                Device device = devices.get();

                device.process(transaction);

                int queueLengthAfterCurrentTransactFinish = deviceExecutor.getQueue().size()-1;
                queueStat.updateQueueStat(queueLengthAfterCurrentTransactFinish);
                View.submitOutputTask("ВИХІД з черги - кількість транзактів в черзі:"+queueLengthAfterCurrentTransactFinish);

            });

        } catch (RejectedExecutionException e) {
            View.submitOutputTask("Thread - " + Thread.currentThread().getName() + " hasn't entered the queue, because it is overloaded.");
            View.submitOutputTask("ВИХІД з СМО - кількість транзактів в черзі:"+deviceExecutor.getQueue().size());

            result.incDenyingNumber();
        }
    }

    private double countAverageNumberOfTransactsInQueue(long totalTime) {
        double result = 0;
        for (Map.Entry<Integer, Long> indication : queueStat.indicators.entrySet()) {
            result += indication.getKey().doubleValue() * indication.getValue().doubleValue();
        }
        return result / totalTime;
    }
    public void releaseAllDevices(){
        deviceExecutor.shutdown();
    }
}
