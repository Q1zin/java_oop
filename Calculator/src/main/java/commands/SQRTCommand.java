package main.java.commands;

import main.java.CalcContext;
import java.lang.Math;

@CommandName("SQRT")
public class SQRTCommand extends AbstractCommand {
    public SQRTCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() throws Exception {
        if (args.length > 0) {
            throw new IllegalArgumentException("Не нужны аргументы");
        }

        double value;
        try {
            value = context.pop();
        } catch (IllegalStateException e) {
            System.out.println("В стеке мало значений");
            return;
        }

        if (value < 0) {
            context.push(value);
            throw new IllegalStateException("Невозможно извлечь корень из отрицательного числа");
        }
        double result = Math.sqrt(value);

        context.push(result);
    }
}
