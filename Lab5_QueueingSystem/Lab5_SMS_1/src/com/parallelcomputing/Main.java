package com.parallelcomputing;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static final int TRANSACTION_NUMBER = 100;

    private static final int MODELLING_LAUNCH_NUMBER = 2;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(MODELLING_LAUNCH_NUMBER);

    public static void main(String[] args) {

        View.startPrinting();

        List<CompletableFuture<Result>> list = IntStream.range(0, MODELLING_LAUNCH_NUMBER)
                .mapToObj(i -> CompletableFuture.supplyAsync(
                        new Experiment(TRANSACTION_NUMBER), EXECUTOR)
                ).collect(Collectors.toList());

        double averageQueueSize = 0;
        double averageDenyingProb = 0;

        for (CompletableFuture<Result> f : list) {
            try {
                Result result = f.get();

                averageQueueSize += result.getAverageQueueLength();
                averageDenyingProb += result.getDenyingProbability();

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        averageQueueSize /= MODELLING_LAUNCH_NUMBER;
        averageDenyingProb /= MODELLING_LAUNCH_NUMBER;
        View.submitOutputTask("<==========================Середні значення за всі прогони==============================>");
        View.submitOutputTask(String.format("Середня довжина черги становить - %.3f од.", averageQueueSize));
        View.submitOutputTask(String.format("Ймовірність відмови в обслуговуванні становить - %.3f%%", averageDenyingProb*100));

        EXECUTOR.shutdown();

        View.submitOutputTask("Done");
    }
}
