package mySql.types;

@TypeName("float")
public class FloatType implements Type {
    public static boolean valid(String data) {
        try {
            Float.parseFloat(data);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Object parse(String data) {
        return Float.parseFloat(data);
    }
}
