package test.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import main.java.Calculator;

public class CalculatorTests {
    private Calculator calculator;

    @BeforeEach
    public void setUp() {
        calculator = new Calculator();
    }

    @Test
    void TestExecuteCommand() throws Exception {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                calculator.executeCommand("bla-bla-bla"));
        assertEquals("Неизвестная команда: bla-bla-bla", exception.getMessage());

        calculator.executeCommand("PUSH 20");
        assertEquals(20, calculator.getContext().peek());
    }

    @Test
    void TestPush() throws Exception {
        calculator.executeCommand("PUSH 5");
        assertEquals(5, calculator.getContext().peek(), "Верхний элемент должен быть 5 после PUSH");

        calculator.executeCommand("PUSH 8");
        assertEquals(8, calculator.getContext().peek(), "Верхний элемент должен быть 8 после PUSH");
    }

    @Test
    void TestPopCommand() throws Exception {
        calculator.executeCommand("POP");
        assertEquals(0, calculator.getContext().size(), "Стек не измениться");

        calculator.executeCommand("PUSH 10");
        calculator.executeCommand("POP");
        assertEquals(0, calculator.getContext().size(), "Стек должен быть пустым после PUSH, POP");

        calculator.executeCommand("PUSH 20");
        calculator.executeCommand("PUSH 30");
        calculator.executeCommand("POP");
        assertEquals(20, calculator.getContext().peek(), "Верхний элемент должен быть 20 после POP");
    }

    @Test
    void TestPlusCommand() throws Exception {
        calculator.executeCommand("+");
        assertEquals(0, calculator.getContext().size(), "Стек не должен измениться");

        calculator.executeCommand("PUSH 5");
        calculator.executeCommand("PUSH 10");
        calculator.executeCommand("+");
        assertEquals(15, calculator.getContext().peek(), "Результат должен быть 15");

        calculator.executeCommand("PUSH -3");
        calculator.executeCommand("PUSH 7");
        calculator.executeCommand("+");
        assertEquals(4, calculator.getContext().peek(), "Результат должен быть 4");
    }

    @Test
    void TestMinusCommand() throws Exception {
        calculator.executeCommand("-");
        assertEquals(0, calculator.getContext().size(), "Стек не должен измениться");

        calculator.executeCommand("PUSH 10");
        calculator.executeCommand("PUSH 3");
        calculator.executeCommand("-");
        assertEquals(7, calculator.getContext().peek(), "Результат должен быть 7");

        calculator.executeCommand("PUSH -5");
        calculator.executeCommand("PUSH 2");
        calculator.executeCommand("-");
        assertEquals(-7, calculator.getContext().peek(), "Результат должен быть -7");
    }

    @Test
    void TestMultCommand() throws Exception {
        calculator.executeCommand("*");
        assertEquals(0, calculator.getContext().size(), "Стек не должен измениться");

        calculator.executeCommand("PUSH 4");
        calculator.executeCommand("PUSH 6");
        calculator.executeCommand("*");
        assertEquals(24, calculator.getContext().peek(), "Результат должен быть 24");

        calculator.executeCommand("PUSH -3");
        calculator.executeCommand("PUSH 8");
        calculator.executeCommand("*");
        assertEquals(-24, calculator.getContext().peek(), "Результат должен быть -24");
    }

    @Test
    void TestDivisionCommand() throws Exception {
        calculator.executeCommand("/");
        assertEquals(0, calculator.getContext().size(), "Стек не должен измениться");

        calculator.executeCommand("PUSH 0");
        calculator.executeCommand("PUSH 10");
        calculator.executeCommand("/");
        assertEquals(0, calculator.getContext().peek(), "Деление 0 на число");

        calculator.executeCommand("PUSH 10");
        calculator.executeCommand("PUSH 0");
        calculator.executeCommand("/");
        assertEquals(0, calculator.getContext().peek(), "Деление на 0");

        calculator.executeCommand("PUSH 20");
        calculator.executeCommand("PUSH 4");
        calculator.executeCommand("/");
        assertEquals(5, calculator.getContext().peek(), "Результат должен быть 5");

        calculator.executeCommand("PUSH -6");
        calculator.executeCommand("PUSH 2");
        calculator.executeCommand("/");
        assertEquals(-3, calculator.getContext().peek(), "Результат должен быть -3");
    }

    @Test
    void TestSQRTCommand() throws Exception {
        calculator.executeCommand("SQRT");
        assertEquals(0, calculator.getContext().size(), "Стек не должен измениться");

        calculator.executeCommand("PUSH 16");
        calculator.executeCommand("SQRT");
        assertEquals(4, calculator.getContext().peek(), "Корень из 16 должен быть 4");

        calculator.executeCommand("PUSH 9.0");
        calculator.executeCommand("SQRT");
        assertEquals(3, calculator.getContext().peek(), "Корень из 9.0 должен быть 3");

        calculator.executeCommand("PUSH -4");
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            calculator.executeCommand("SQRT");
        });
        assertEquals("Невозможно извлечь корень из отрицательного числа", exception.getMessage());
    }

    @Test
    void TestDefineCommand() throws Exception {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.executeCommand("DEFINE x");
        });
        assertEquals("Нужно 2 аргумента", exception.getMessage());

        calculator.executeCommand("DEFINE y notANumber");
        Exception exception2 = assertThrows(IllegalStateException.class, () -> {
            calculator.getContext().getVariable("y");
        });
        assertEquals("Переменная не объявлена: y", exception2.getMessage());

        calculator.executeCommand("DEFINE z 10");
        assertEquals(10.0, calculator.getContext().getVariable("z"), "Значение переменной z должно быть 10.0");

        calculator.executeCommand("DEFINE z 20");
        assertEquals(20.0, calculator.getContext().getVariable("z"), "Значение переменной z должно быть обновлено на 20.0");

        calculator.executeCommand("DEFINE a 5.5");
        assertEquals(5.5, calculator.getContext().getVariable("a"), "Значение переменной a должно быть 5.5");
    }
}