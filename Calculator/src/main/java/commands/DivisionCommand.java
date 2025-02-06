package main.java.commands;

import main.java.CalcContext;
import java.lang.Math;

@CommandName("/")
public class DivisionCommand extends AbstractCommand {
    public DivisionCommand(String[] args, CalcContext context) {
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

        if (elem1 == 0) {
            context.push(elem1);
            System.out.println("На 0 делить нельзя!");
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

        double result = elem2 / elem1;
        context.push(result);
    }
}

