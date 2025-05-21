package factory.units;

import java.util.UUID;

public class Body implements CarPart {
    private final String bodySN;

    public Body() {
        bodySN = generateSerial();
        System.out.println("Body created. Body serial number: " + bodySN);
    }

    public static String generateSerial() {
        return "body-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString();
    }

    public String getSN() {
        return bodySN;
    }
}
