package factory.units;

import java.util.UUID;

public class Car {
    private String carSN;
    private final Engine engine;
    private final Body body;
    private final Accessories accessories;

    public Car(Body body, Engine engine, Accessories accessories) {
        this.body = body;
        this.engine = engine;
        this.accessories = accessories;

        carSN = generateSerial();
        System.out.println("Car created. Car serial number: " + carSN);
    }

    public static String generateSerial() {
        return "car-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString();
    }

    public String getSN() {
        return carSN;
    }

    public Body getBody() {
        return body;
    }

    public Engine getEngine() {
        return engine;
    }

    public Accessories getAccessories() {
        return accessories;
    }
}
