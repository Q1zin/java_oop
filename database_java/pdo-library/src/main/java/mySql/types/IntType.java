package mySql.types;

@TypeName("int")
public class IntType implements Type {
    public static boolean valid(String data) {
        try {
            Integer.parseInt(data);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Object parse(String data) {
        return Integer.parseInt(data);
    }
}

