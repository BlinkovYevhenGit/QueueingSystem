package com.parallelcomputing;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


class SMS {

    private static final int DEVICE_NUMBER = 10;

    private final QueueStat queueStat;

    private final Result result;
    private final int transactionQuantity;



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

    public Future<?> submitToQueue(Transaction transaction) {
        updateQueueLengthAndSend(transaction, ": ВХІД в чергу - кількість транзактів: ",true);
        try {

            Future<?> submit = deviceExecutor.submit(() -> {
                updateQueueLengthAndSend(transaction, " :ВИХІД з черги - кількість транзактів в черзі:",true);
                Device device = devices.get();

                device.process(transaction);
            });
            return submit;
        } catch (RejectedExecutionException e) {
            View.submitOutputTask("Thread - " + transaction + " hasn't entered the queue, because it is overloaded.");
            updateQueueLengthAndSend(transaction, " :ВИХІД з СМО - кількість транзактів в черзі:", false);
            result.incDenyingNumber();
            return null;
        }

    }

    private synchronized void updateQueueLengthAndSend(Transaction transaction, String s, boolean updateStatistics) {
        AtomicInteger queueLength = new AtomicInteger(deviceExecutor.getQueue().size());
        if(updateStatistics)queueStat.updateQueueStat(queueLength.get());

        View.submitOutputTask(transaction + s + queueLength.get());
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
