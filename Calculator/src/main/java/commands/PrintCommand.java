package main.java.commands;

import main.java.CalcContext;

@CommandName("PRINT")
public class PrintCommand extends AbstractCommand {
    public PrintCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() {
        try {
            double result = context.peek();
            if (result % 1 == 0) {
                System.out.println((int) result);
            } else {
                System.out.printf("%.6f%n", result);
            }
        } catch (IllegalStateException e) {
            System.out.println("Стек пуст");
        }
    }
}
