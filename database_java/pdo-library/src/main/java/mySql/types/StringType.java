package mySql.types;

@TypeName("string")
public class StringType implements Type {
    public static boolean valid(String data) {
        return data != null;
    }

    public static Object parse(String data) {
        return data;
    }
}
