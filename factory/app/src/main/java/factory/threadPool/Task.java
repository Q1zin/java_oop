package factory.threadPool;

import factory.units.*;
import factory.Storage;

public class Task {
    private final Storage<Body> bodyStorage;
    private final Storage<Engine> engineStorage;
    private final Storage<Accessories> accessoriesStorage;
    private final Storage<Car> carsStorage;

    public Task(Storage<Body> bodyStorage, Storage<Engine> engineStorage, Storage<Accessories> accessoriesStorage, Storage<Car> carsStorage) {
        this.bodyStorage = bodyStorage;
        this.engineStorage = engineStorage;
        this.accessoriesStorage = accessoriesStorage;
        this.carsStorage = carsStorage;
    }

    public void doTask(){
        Body body = bodyStorage.take();
        Engine engine = engineStorage.take();
        Accessories accessories = accessoriesStorage.take();
        Car car = new Car(body, engine, accessories);
        carsStorage.put(car);
    }
}