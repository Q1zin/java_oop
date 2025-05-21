package calc.commands;

import calc.CalcContext;

@CommandName("/")
public class DivisionCommand extends AbstractCommand {
    public DivisionCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() throws IllegalArgumentException {
        if (args.length > 0) {
            throw new IllegalArgumentException("Не нужны аргументы");
        }

        if (context.getSizeStack() < 2) {
            System.out.println("В стеке мало значений");
            throw new IllegalStateException("В стеке мало значений");
        }

        double elem1 = context.pop();

        if (elem1 == 0) {
            context.push(elem1);
            System.out.println("На 0 делить нельзя!");
            throw new IllegalArgumentException("На 0 делить нельзя!");
        }

        double elem2 = context.pop();

        double result = elem2 / elem1;
        context.push(result);
    }
}

