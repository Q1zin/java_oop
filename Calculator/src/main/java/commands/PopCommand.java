package main.java.commands;

import main.java.CalcContext;

@CommandName("POP")
public class PopCommand extends AbstractCommand {
    public PopCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() throws IllegalArgumentException {
        if (args.length > 0) {
            throw new IllegalArgumentException("Не нужны аргументы");
        }
        try {
            context.pop();
        } catch (IllegalStateException e) {
            System.out.println("В стеке и так уже ничего нету!");
        }
    }
}
