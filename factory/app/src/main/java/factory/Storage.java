package factory;

import java.util.LinkedList;
import java.util.Queue;

public class Storage<T> {
    private final int sizeStorage;
    private int capacityStorage;
    private final Queue<T> storage;
    private int countItemAllTime;

    public Storage(int sizeStorage) {
        this.countItemAllTime = 0;
        this.storage = new LinkedList<>();
        this.sizeStorage = sizeStorage;
        this.capacityStorage = 0;
    }

    public synchronized T take() {
        while (storage.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("take(): " + e.getLocalizedMessage());
                Thread.currentThread().interrupt();
                return null;
            }
        }
        T element = storage.poll();
        capacityStorage--;
        notifyAll();
        return element;
    }

    public synchronized void put(T objectStorage) {
        while (capacityStorage >= sizeStorage) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("put(): " + e.getLocalizedMessage());
                Thread.currentThread().interrupt();
                return;
            }
        }
        storage.add(objectStorage);
        countItemAllTime++;
        capacityStorage++;
        notifyAll();
    }

    public synchronized int getCapacity() {
        return capacityStorage;
    }

    public int getSize() {
        return sizeStorage;
    }
}
