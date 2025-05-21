package factory.staff;

import factory.units.Car;
import factory.Storage;

import java.io.*;

public class Dealer extends Thread {
    private final Storage<Car> storage;
    private int delay;
    private final int id;
    private volatile boolean isWorking;
    private final boolean log;
    private BufferedWriter writer;

    public Dealer(Storage<Car> storage, int id, boolean log) {
        this.storage = storage;
        this.id = id;
        this.log = log;
        if (log) {
            try {
                writer = new BufferedWriter(new FileWriter("logs.log", true));
            } catch (IOException e) {
                System.err.println("Ошибка при открытии файла логов: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void run() {
        isWorking = true;
        double startTime = System.currentTimeMillis() / 1000.0;
        try {
            while (!Thread.currentThread().isInterrupted() && isWorking) {
                Car car = storage.take();
                double curTime = System.currentTimeMillis() / 1000.0 - startTime;
                double roundNumberTime = Math.round(curTime * 100) / 100.0;

                String logs = roundNumberTime + ": Dealer " + id + ": Auto " + car.getSN() + " (Body: " + car.getBody().getSN() + ", Engine: " + car.getEngine().getSN() + ", Accessories: " + car.getAccessories().getSN() + ")";

                if (log) {
                    try {
                        System.out.println(logs);
                        writer.write(logs);
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        System.err.println("Ошибка при записи в лог: " + e.getLocalizedMessage());
                    }
                }
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
