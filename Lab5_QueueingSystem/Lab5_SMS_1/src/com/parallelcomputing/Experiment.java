package com.parallelcomputing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Experiment implements Supplier<Result> {

    private final ScheduledExecutorService service;
    private final int transactionNumber;
    private static final Object resultLocker=new Object();

    public Experiment(int transactionNumber) {
        this.transactionNumber = transactionNumber;
        this.service = Executors.newScheduledThreadPool(transactionNumber);//if we want to generate transactions in parallel then > 1
    }

    public Result get() {
        SMS sms = new SMS(this.transactionNumber);

        long time = -System.nanoTime();
        List<ScheduledFuture<?>> futures = new ArrayList<>();
        for (int i = 0; i < this.transactionNumber; i++) {
            Transaction transaction = new Transaction(sms, i);
            ScheduledFuture<?> scheduledFuture = service.schedule(transaction, i * 10, TimeUnit.MILLISECONDS);
            futures.add(scheduledFuture);
        }
        for (ScheduledFuture<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        time += System.nanoTime();

        Result result = sms.calculateResult(time);

        double averageQueueLength = result.getAverageQueueLength();
        double denyingProbability = result.getDenyingProbability();
        synchronized (resultLocker){
            View.submitOutputTask("<==========================Результат==============================>");
            View.submitOutputTask(String.format("Середня довжина черги становить - %.3f од.", averageQueueLength));
            View.submitOutputTask(String.format("Ймовірність відмови в обслуговуванні становить - %.3f%%", denyingProbability * 100));
        }
        sms.releaseAllDevices();
        service.shutdown();
        return result;
    }
}
