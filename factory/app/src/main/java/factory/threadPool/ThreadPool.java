package factory.threadPool;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ThreadPool {
    private final Queue<Task> tasks;
    private final ArrayList<WorkerThread> workers;
    private int delay;

    public ThreadPool(int countWorkers, int delay) {
        this.tasks = new ConcurrentLinkedDeque<>();
        this.workers = new ArrayList<>();
        this.delay = delay;

        for (int i = 0; i < countWorkers; ++i) {
            WorkerThread workerThread = new WorkerThread();
            workers.add(workerThread);
            workerThread.start();
        }
    }

    public void addTask(Task task) {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notify();
        }
    }

    public void stopWorking() {
        for (WorkerThread workerThread : workers) {
            workerThread.stopWorking();
        }
        synchronized (tasks) {
            tasks.notifyAll();
        }
    }

    public synchronized int getTasksSize() {
        return tasks.size();
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    private class WorkerThread extends Thread {
        private volatile boolean isWorking;

        @Override
        public void run() {
            isWorking = true;
            try {
                while (!Thread.currentThread().isInterrupted() && isWorking) {
                    Task task;
                    synchronized (tasks) {
                        while (tasks.isEmpty() && isWorking) {
                            tasks.wait();
                        }
                        task = tasks.poll();
                    }

                    if (task != null) {
                        task.doTask();
                    }

                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                System.err.println("WorkerThread interrupted: " + e.getLocalizedMessage());
                Thread.currentThread().interrupt();
            }
        }

        public void stopWorking() {
            isWorking = false;
        }
    }
}