package factory;

import factory.threadPool.*;
import factory.units.*;

public class ControllerProduct extends Thread {
    private final ThreadPool workers;
    private final Storage<Body> storageBody;
    private final Storage<Accessories> storageAccessories;
    private final Storage<Engine> storageEngine;
    private final Storage<Car> storageCar;
    private volatile boolean isWorking = true;

    public ControllerProduct(ThreadPool workers, Storage<Body> storageBody, Storage<Accessories> storageAccessories, Storage<Engine> storageEngine, Storage<Car> storageCar) {
        this.workers = workers;
        this.storageBody = storageBody;
        this.storageAccessories = storageAccessories;
        this.storageEngine = storageEngine;
        this.storageCar = storageCar;
    }

    @Override
    public void run() {
        while (isWorking && !Thread.currentThread().isInterrupted()) {
            synchronized (storageCar) {
                try {
                    int availableSpace = storageCar.getSize() - storageCar.getCapacity();
                    int taskCount = availableSpace - workers.getTasksSize();

                    while (taskCount <= 0) {
                        storageCar.wait();
                        if (!isWorking) return;
                        availableSpace = storageCar.getSize() - storageCar.getCapacity();
                        taskCount = availableSpace - workers.getTasksSize();
                    }

                    for (int i = 0; i < taskCount; i++) {
                        workers.addTask(new Task(storageBody, storageEngine, storageAccessories, storageCar));
                    }

                    storageCar.notifyAll();
                } catch (InterruptedException e) {
                    System.err.println("ControllerProduct interrupted: " + e.getLocalizedMessage());
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.err.println("Ошибка в ControllerProduct: " + e.getMessage());
                }
            }
        }
    }

    public void stopWorking() {
        isWorking = false;
        synchronized (storageCar) {
            storageCar.notifyAll();
        }
    }
}