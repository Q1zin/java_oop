package factory;

import factory.staff.*;
import factory.threadPool.*;
import factory.units.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class Factory {
    private final Properties properties = new Properties();
    private final ArrayList<Dealer> dealers = new ArrayList<>();
    private final ArrayList<Supplier<Accessories>> suppliersAccessories = new ArrayList<>();
    private final ArrayList<Supplier<Body>> suppliersBody = new ArrayList<>();
    private final ArrayList<Supplier<Engine>> suppliersEngine = new ArrayList<>();
    private final ControllerProduct controllerProduct;
    private ThreadPool workers;
    private Storage<Body> storageBody;
    private Storage<Engine> storageEngine;
    private Storage<Accessories> storageAccessories;
    private Storage<Car> storageCar;
    private final int delay;


    private void createWorkersArray() {
        workers = new ThreadPool(Integer.parseInt(properties.getProperty("Workers")), delay);
    }

    private void createStorages() {
        storageBody = new Storage<>(Integer.parseInt(properties.getProperty("StorageBodySize")));
        storageAccessories = new Storage<>(Integer.parseInt(properties.getProperty("StorageAccessoriesSize")));
        storageEngine = new Storage<>(Integer.parseInt(properties.getProperty("StorageEngineSize")));
        storageCar = new Storage<>(Integer.parseInt(properties.getProperty("StorageCarSize")));
    }

    public Factory(String resourceName) {
        System.out.println(resourceName);
        delay = 1000;

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getLocalizedMessage());
            throw new RuntimeException("Failed to load factory configuration", e);
        }

        createWorkersArray();
        createStorages();
        initDealers();
        initSuppliersBody();
        initSuppliersAccessories();
        initSuppliersEngine();
        controllerProduct = new ControllerProduct(workers, storageBody, storageAccessories, storageEngine, storageCar);
        startWorking();
    }

    private void initDealers() {
        int countDealers = Integer.parseInt(properties.getProperty("Dealers"));
        for (int i = 0; i < countDealers; i++) {
            Dealer dealer = new Dealer(storageCar, i, Boolean.parseBoolean(properties.getProperty("LogSale")));
            dealer.setDelay(delay);
            dealers.add(dealer);
        }
    }

    private void initSuppliersBody() {
        int countSuppliersBody = Integer.parseInt(properties.getProperty("BodySuppliers"));
        for (int i = 0; i < countSuppliersBody; i++) {
            Supplier<Body> supplier = new Supplier<>(storageBody, Body::new);
            supplier.setDelay(delay);
            suppliersBody.add(supplier);
        }
    }

    private void initSuppliersAccessories() {
        int countSuppliersAccessories = Integer.parseInt(properties.getProperty("AccessoriesSuppliers"));
        for (int i = 0; i < countSuppliersAccessories; i++) {
            Supplier<Accessories> supplier = new Supplier<>(storageAccessories, Accessories::new);
            supplier.setDelay(delay);
            suppliersAccessories.add(supplier);
        }
    }

    private void initSuppliersEngine() {
        int countSuppliersEngine = Integer.parseInt(properties.getProperty("EngineSuppliers"));
        for (int i = 0; i < countSuppliersEngine; i++) {
            Supplier<Engine> supplier = new Supplier<>(storageEngine, Engine::new);
            supplier.setDelay(delay);
            suppliersEngine.add(supplier);
        }
    }

    public void startWorking() {
        for (Dealer dealer : dealers) {
            dealer.start();
        }

        for (Supplier<Body> supplier : suppliersBody) {
            supplier.start();
        }
        for (Supplier<Accessories> supplier : suppliersAccessories) {
            supplier.start();
        }
        for (Supplier<Engine> supplier : suppliersEngine) {
            supplier.start();
        }
        controllerProduct.start();
    }

    public void stopWorking() {
        for (Supplier<Body> supplier : suppliersBody) {
            supplier.stopWorking();
        }
        for (Supplier<Accessories> supplier : suppliersAccessories) {
            supplier.stopWorking();
        }
        for (Supplier<Engine> supplier : suppliersEngine) {
            supplier.stopWorking();
        }
        controllerProduct.stopWorking();
        workers.stopWorking();
    }

    public int getBodyStorageCapacity() {
        return storageBody.getCapacity();
    }

    public int getEngineStorageCapacity() {
        return storageEngine.getCapacity();
    }

    public int getAccessoriesStorageCapacity() {
        return storageAccessories.getCapacity();
    }

    public int getCarStorageCapacity() {
        return storageCar.getCapacity();
    }

    public void setDelayDealers(int time) {
        for (Dealer dealer : dealers) {
            dealer.setDelay(time);
        }
    }

    public void setDelaySuppliersBody(int time) {
        for (Supplier<Body> supplier : suppliersBody) {
            supplier.setDelay(time);
        }
    }

    public void setDelaySuppliersEngine(int time) {
        for (Supplier<Engine> supplier : suppliersEngine) {
            supplier.setDelay(time);
        }
    }

    public void setDelaySuppliersAccessories(int time) {
        for (Supplier<Accessories> supplier : suppliersAccessories) {
            supplier.setDelay(time);
        }
    }

    public void setDelayWorkers(int time) {
        workers.setDelay(time);
    }
}