package mySql.types;

@TypeName("boolean")
public class BooleanType implements Type {
    public static boolean valid(String data) {
        return "true".equalsIgnoreCase(data) || "false".equalsIgnoreCase(data);
    }

    public static Object parse(String data) {
        if ("true".equalsIgnoreCase(data)) return true;
        if ("false".equalsIgnoreCase(data)) return false;
        throw new IllegalArgumentException(": " + data);
    }
}
