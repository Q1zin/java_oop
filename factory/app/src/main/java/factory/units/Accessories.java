package factory.units;

import java.util.UUID;

public class Accessories implements CarPart {
    private final String accessoriesSN;

    public Accessories() {
        accessoriesSN = generateSerial();
        System.out.println("Accessories created. Accessories serial number: " + accessoriesSN);
    }

    public static String generateSerial() {
        return "accessories-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString();
    }

    public String getSN() {
        return accessoriesSN;
    }
}
