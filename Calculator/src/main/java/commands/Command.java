package main.java.commands;

public interface Command {
    void execute() throws IllegalArgumentException, IllegalStateException;
}

