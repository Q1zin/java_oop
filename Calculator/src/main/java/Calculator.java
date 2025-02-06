package main.java;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import main.java.CalcContext;
import main.java.commands.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Calculator {
    private final CalcContext context = new CalcContext();
    private static final Logger logger = LogManager.getLogger(CalcContext.class);

    public static void main(String[] args) {
        Calculator calc = new Calculator();
        if (args.length > 0) {
            try(BufferedReader buff = new BufferedReader(new FileReader(args[0]))) {
                calc.executeCommands(buff);
            } catch (FileNotFoundException e) {
                System.out.println("Файл не найден: " + args[0]);
                logger.error("Файл не найден: {}", args[0]);
            } catch (Exception e) {
                System.out.println("Неизвестная ошибка, вводите команды вручную: ");
                logger.error("Произошла критическая ошибка: calc.executeCommands(buff from {}) {}", args[0], e.getMessage());
                calc.executeCommands(new InputStreamReader(System.in));
            }
        } else {
            System.out.println("Вводите команды вручную:");
            calc.executeCommands(new InputStreamReader(System.in));
        }
    }

    public void executeCommands(Reader input) {
        try (Scanner scanner = new Scanner(input)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                try {
                    executeCommand(line.trim());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println(e.getMessage());
                    logger.warn("Ошибка ввода: {}", e.getMessage());
                } catch (Exception e) {
                    System.out.println("Неизвестная ошибка, вводите команды вручную: ");
                    logger.error("Произошла критическая ошибка: executeCommand({}) {}", line.trim(), e.getMessage());
                }
            }
        }
    }

    public void executeCommand(String line) throws Exception {
        String[] parts = line.split("\\s+");

        String commandName = parts[0];
        String[] commandArgs = Arrays.copyOfRange(parts, 1, parts.length);

        Command command = CommandFactory.createCommand(commandName, commandArgs, context);
        if (command != null) {
            command.execute();
        } else {
            throw new IllegalArgumentException("Неизвестная команда: " + commandName);
        }
    }

    public CalcContext getContext() {
        return context;
    }
}