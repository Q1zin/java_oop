package mySql.types;

@TypeName("[]strings")
public class ListStringsType implements Type {
    public static boolean valid(String data) {
        return data.matches("\\[\"[^\"]*\"( \"[^\"]*\")*]");
    }

    public static Object parse(String data) {
        data = data.substring(1, data.length() - 1);
        String[] elements = data.split(" ");
        return java.util.Arrays.asList(elements);
    }
}

