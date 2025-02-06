package main.java.commands;

import main.java.CalcContext;
import java.lang.Math;

@CommandName("*")
public class MultCommand extends AbstractCommand {
    public MultCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() throws Exception {
        if (args.length > 0) {
            throw new IllegalArgumentException("Не нужны аргументы");
        }

        double elem1;
        try {
            elem1 = context.pop();
        } catch (IllegalStateException e) {
            System.out.println("В стеке мало значений");
            return;
        }

        double elem2;
        try {
            elem2 = context.pop();
        } catch (IllegalStateException e) {
            context.push(elem1);
            System.out.println("В стеке мало значений");
            return;
        }

        double result = elem1 * elem2;
        context.push(result);
    }
}

