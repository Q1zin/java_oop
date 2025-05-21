package mySql;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import mySql.commands.Command;
import mySql.commands.CommandName;

public class CommandFactory {
    private static final Map<String, Class<? extends Command>> commands = new HashMap<>();

    static {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("mySql.commands")
                .scan()) {

            ClassInfoList annotatedClasses = scanResult.getClassesWithAnnotation(CommandName.class.getName());

            for (ClassInfo classInfo : annotatedClasses) {
                Class<?> classImage = classInfo.loadClass();
                if (Command.class.isAssignableFrom(classImage)) {
                    CommandName annotation = classImage.getAnnotation(CommandName.class);
                    commands.put(annotation.value(), (Class<? extends Command>) classImage);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Критическая ошибка, не удалось загрузить команды!! " + e.getMessage());
        }
    }

    public static Command createCommand(String sql) {
        Class<? extends Command> classImage = commands.get(sql.split(" ")[0]);
        if (classImage == null) {
            return null;
        }

        try {
            Constructor<? extends Command> constructor = classImage.getDeclaredConstructor(String.class);
            return constructor.newInstance(sql);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать команду: " + sql.split(" ")[0], e);
        }
    }

}
