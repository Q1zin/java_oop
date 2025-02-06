package main.java.commands;

import main.java.CalcContext;

@CommandName("DEFINE")
public class DefineCommand extends AbstractCommand {
    public DefineCommand(String[] args, CalcContext context) {
        super(args, context);
    }

    @Override
    public void execute() throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Нужно 2 аргумента");
        }

        try {
            context.define(args[0], Double.parseDouble(args[1]));
        } catch (NumberFormatException e) {

        } catch (Exception e) {
            System.out.println("Ты довн, вводи число!");
        }
    }
}
