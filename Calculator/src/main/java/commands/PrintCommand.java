package main.java.commands;

import main.java.CalcContext;

@CommandName("PRINT")
public class PrintCommand extends AbstractCommand {
    public PrintCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() throws Exception {
        try {
            double result = context.peek();
            if (result == (int)result) {
                System.out.println((int)result);
            } else {
                System.out.println(result);
            }
        } catch (IllegalStateException e) {
            System.out.println("Стек пуст");
        }
    }
}
