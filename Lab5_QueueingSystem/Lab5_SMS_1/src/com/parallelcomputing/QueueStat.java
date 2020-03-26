package com.parallelcomputing;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class QueueStat {

    public static final int CAPACITY = 20;

    private final AtomicLong smoStartTime = new AtomicLong(System.nanoTime());

    public final HashMap<Integer, Long> indicators = new HashMap<>();

    public void updateQueueStat(int activeCount) {
        long finish = System.nanoTime();
        long start = smoStartTime.get();
        smoStartTime.set(finish);
        updateIndicators(finish - start, activeCount);
    }

    private void updateIndicators(long time, int currentQueueOccupancy) {
        synchronized (indicators) {
            if (indicators.containsKey(currentQueueOccupancy)) {
                long number = indicators.get(currentQueueOccupancy);//збереження інформації про попередню кількість повторень
                indicators.put(currentQueueOccupancy, number + time);//внесення оновленої інформації про букву
            } else {//перевірка відсутності визначеної букви у списку всіх букв
                indicators.put(currentQueueOccupancy, time);//внесення нової букви у список
            }
        }
    }
}

