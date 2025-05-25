package mySql;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import mySql.types.Type;
import mySql.types.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeFactory {
    private static final Map<String, Class<? extends Type>> types = new HashMap<>();

    static {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("mySql.types")
                .scan()) {

            ClassInfoList annotatedClasses = scanResult.getClassesWithAnnotation(TypeName.class.getName());

            for (ClassInfo classInfo : annotatedClasses) {
                Class<?> classImage = classInfo.loadClass();
                if (Type.class.isAssignableFrom(classImage)) {
                    TypeName annotation = classImage.getAnnotation(TypeName.class);
                    types.put(annotation.value(), (Class<? extends Type>) classImage);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Критическая ошибка, не удалось загрузить типы!! " + e.getMessage());
        }
    }

    public static boolean valid(String typeName, String data) {
        Class<? extends Type> classImage = types.get(typeName);
        if (classImage == null) {
            throw new IllegalArgumentException("Такого типа не существует: " + typeName);
        }

        try {
            return (boolean) classImage.getMethod("valid", String.class).invoke(null, data);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при валидации типа " + typeName, e);
        }
    }

    public static Object parse(String typeName, String data) {
        Class<? extends Type> classImage = types.get(typeName);
        if (classImage == null) {
            throw new IllegalArgumentException("Такого типа не существует: " + typeName);
        }

        try {
            return classImage.getMethod("parse", String.class).invoke(null, data);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при валидации типа " + typeName, e);
        }
    }

    public static List<String> getTypes() {
        return new ArrayList<>(types.keySet());
    }
}