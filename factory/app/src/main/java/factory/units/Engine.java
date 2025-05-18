package factory.units;

import java.util.UUID;

public class Engine implements CarPart {
    private final String engineSN;

    public Engine() {
        engineSN = generateSerial();
        System.out.println("Engine created. Engine serial number: " + engineSN);
    }

    public static String generateSerial() {
        return "engine-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString();
    }

    public String getSN() {
        return engineSN;
    }
}
