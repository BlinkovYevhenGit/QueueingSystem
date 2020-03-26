package com.parallelcomputing;

public class View {

    private static final OutputQueue OUTPUT_QUEUE = new OutputQueue();

    public static void submitOutputTask(String text) {
        OUTPUT_QUEUE.submitTextMessage(text);
    }

    public static void startPrinting() {
        new Thread(new OutputPrinter()).start();
    }

    public static class OutputPrinter implements Runnable {

        private static final int OUTPUT_SLEEP_INTERVAL_MILLIS = 10;

        private volatile boolean active = true;

        @Override
        public void run() {
            while (active) {
                Message take = OUTPUT_QUEUE.take();
                if (take.getTextMessage().equals("Done")) {
                    active = false;
                    break;
                } else {
                    outputMessage(take.getTextMessage());
                }
                try {
                    Thread.sleep(OUTPUT_SLEEP_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            outputMessage("OutputPrinter - is Done");
        }

        private void outputMessage(String message) {
            System.out.println(message);
        }
    }
}
