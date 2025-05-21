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
            throw new IllegalStateException("В стеке мало значений");
        }

        double elem1 = context.pop();
        double elem2 = context.pop();

        double result = elem1 + elem2;
        context.push(result);
    }
}

