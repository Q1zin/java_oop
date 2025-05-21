package mySql.types;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@TypeName("date")
public class DateType implements Type {
    public static boolean valid(String data) {
        try {
            OffsetDateTime.parse(data, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Object parse(String data) {
        return java.time.OffsetDateTime.parse(data);
    }
}
