package com.parallelcomputing;

public class Transmitter {
    // Transmitter sent from producer
    // to consumer.
    private Message message;
    // True if consumer should wait
    // for producer to send message,
    // false if producer should wait for
    // consumer to retrieve message.
    private boolean empty = true;


    public synchronized Message take() {
        // Wait until message is
        // available.
        while (empty) {
            try {
                System.out.println("Producer is waiting");
                wait();
            } catch (InterruptedException e) {}
        }
        // Toggle status.
        empty = true;
        // Notify producer that
        // status has changed.
        notifyAll();
        return message;
    }

    public  synchronized void put(Message message) {
        // Wait until message has
        // been retrieved.
        while (!empty) {
            try {
                System.out.println("Consumer is waiting");
                wait();
            } catch (InterruptedException e) {}
        }
        // Toggle status.
        empty = false;
        // Store message.
        this.message = message;
        // Notify consumer that status
        // has changed.
        notify();
    }
}
