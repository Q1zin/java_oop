package factory;

import factory.gui.Controller;
import factory.gui.Gui;

public class Main {
    public static void main(String[] args) {
        try {
            Factory factory = new Factory("config.properties");
            Controller controller = new Controller(factory);
            Gui gui = new Gui(factory, controller);
        } catch (RuntimeException e) {
            System.err.println("Ошибка при запуске: " + e.getMessage());
            e.printStackTrace();
        }
    }
}