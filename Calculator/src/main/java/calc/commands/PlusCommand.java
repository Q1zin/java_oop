package calc.commands;

import calc.CalcContext;

@CommandName("+")
public class PlusCommand extends AbstractCommand {
    public PlusCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() throws IllegalArgumentException {
        if (args.length > 0) {
            throw new IllegalArgumentException("Не нужны аргументы");
        }

        if (context.getSizeStack() < 2) {
            System.out.println("В стеке мало значений");
            return;
        }

        double elem1, elem2;
        try {
            elem1 = context.pop();
            elem2 = context.pop();
        } catch (IllegalStateException e) {
            System.out.println("В стеке мало значений");
            return;
        }

        double result = elem1 + elem2;
        context.push(result);
    }
}

