package com.parallelcomputing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class OutputQueue {
    // Transmitter sent from producer
    // to consumer.
    private static final BlockingQueue<Message> messageBlockingQueue = new LinkedBlockingDeque<>(1000000);

    // True if consumer should wait
    // for producer to send message,
    // false if producer should wait for
    // consumer to retrieve message.


    public Message take() {
        // Wait until message is
        // available.
        try {

            return messageBlockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void submitTextMessage(String text) {
        messageBlockingQueue.add(new Message(text));
    }
}
