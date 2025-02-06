package main.java.commands;

import main.java.CalcContext;

@CommandName("PUSH")
public class PushCommand extends AbstractCommand {
    public PushCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Push command requires one argument");
        }
        double value;
        try {
            if (context.contains(args[0])){
                value = context.getVariable(args[0]);
            } else {
                value = Double.parseDouble(args[0]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Push command requires a number");
        }

        context.push(value);
    }
}
