package calc.commands;

public interface Command {
    void execute() throws IllegalArgumentException, IllegalStateException;
}

