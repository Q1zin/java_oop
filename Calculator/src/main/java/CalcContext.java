package main.java;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CalcContext {
    private final Stack<Double> stack = new Stack<>();
    private final Map<String, Double> variables = new HashMap<>();

    public void push(double value) {
        stack.push(value);
    }

    public double pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Стек пуст");
        }

        return stack.pop();
    }

    public double peek() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Стек пуст");
        }

        return stack.peek();
    }

    public void define(String name, double value) {
        variables.put(name, value);
    }

    public double getVariable(String name) {
        if (!variables.containsKey(name)) {
            throw new IllegalStateException("Переменная не объявлена: " + name);
        }

        return variables.get(name);
    }

    public boolean contains(String name) {
        return variables.containsKey(name);
    }

    public int size() {
        return variables.size();
    }

    public int getSizeStack() {
        return stack.size();
    }
}