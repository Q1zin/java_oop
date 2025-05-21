package calc;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import calc.commands.Command;
import calc.commands.CommandName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandFactory {
    private static final Map<String, Class<? extends Command>> commands = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(CommandFactory.class);

    static {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("calc.commands")
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
            logger.error("Критичиская ошибка, команды не загрузились: {}", e.getMessage());
            throw new RuntimeException("Критичиская ошибка, команды не загрузились", e);
        }
    }

    public static Command createCommand(String name, String[] args, CalcContext context) {
        Class<? extends Command> classImage = commands.get(name.toUpperCase());
        if (classImage == null) {
            return null;
        }

        try {
            Constructor<? extends Command> constructor = classImage.getDeclaredConstructor(String[].class, CalcContext.class);
            return constructor.newInstance(args, context);
        } catch (Exception e) {
            logger.error("Не удалось создать команду: {} {}", name, Arrays.toString(e.getStackTrace()));
            throw new RuntimeException("Не удалось создать команду: " + name, e);
        }
    }
}