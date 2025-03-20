package main.java.commands;

import main.java.CalcContext;

@CommandName("DEFINE")
public class DefineCommand extends AbstractCommand {
    public DefineCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() throws IllegalArgumentException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Нужно 2 аргумента");
        }

        if (!hasNonDigits(args[0])){
            System.out.println("Переменная не может состоять только из цифр!");
            return;
        }

        try {
            context.define(args[0], Double.parseDouble(args[1]));
        } catch (NumberFormatException e) {
            System.out.println("Вводите число!");
        } catch (Exception e) {
            System.out.println("Произошла неожиданная ошибка: " + e.getMessage());
        }
    }

    public static boolean hasNonDigits(String str) {
        return !str.matches("\\d+");
    }
}
