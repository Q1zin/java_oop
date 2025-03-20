package main.java.commands;

import main.java.CalcContext;

public abstract class AbstractCommand implements Command {
    protected final String[] args;
    protected final CalcContext context;

    public AbstractCommand(String[] args, CalcContext context) {
        this.args = args;
        this.context = context;
    }

    public abstract void execute() throws IllegalArgumentException, IllegalStateException;
}