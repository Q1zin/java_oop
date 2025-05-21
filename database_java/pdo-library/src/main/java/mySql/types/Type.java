package mySql.types;

public interface Type {
    static boolean valid(String data) {
        throw new UnsupportedOperationException("Not implemented");
    }

    static Object parse(String data) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
