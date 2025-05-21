package factory.gui;

import factory.Factory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Enumeration;

public class Gui extends JFrame {
    private final Factory factory;
    private final Controller controller;
    
    private final JLabel bodyStorageInfo = new JLabel("Body Storage: ?");
    private final JLabel engineStorageInfo = new JLabel("Engine Storage: ?");
    private final JLabel accessoriesStorageInfo = new JLabel("Accessories Storage: ?");
    private final JLabel carStorageInfo = new JLabel("Car Storage: ?");
    
    private final ButtonGroup bodySupplierSpeedGroup = new ButtonGroup();
    private final ButtonGroup engineSupplierSpeedGroup = new ButtonGroup();
    private final ButtonGroup accessoriesSupplierSpeedGroup = new ButtonGroup();
    private final ButtonGroup dealerSpeedGroup = new ButtonGroup();
    private final ButtonGroup workerSpeedGroup = new ButtonGroup();
    
    private static final int FAST_SPEED = 500;
    private static final int NORMAL_SPEED = 1000;
    private static final int SLOW_SPEED = 2000;

    public Gui(Factory factory, Controller controller) {
        super("Factory Control Panel");
        this.factory = factory;
        this.controller = controller;
        
        setupUI();
        startUpdateTimer();
    }

    private void setupUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(controller);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel storagePanel = new JPanel(new GridLayout(4, 1, 5, 5));
        storagePanel.setBorder(BorderFactory.createTitledBorder("Storage Status"));
        storagePanel.add(bodyStorageInfo);
        storagePanel.add(engineStorageInfo);
        storagePanel.add(accessoriesStorageInfo);
        storagePanel.add(carStorageInfo);
        
        JPanel controlPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Speed Control"));
        
        controlPanel.add(createSpeedControlPanel("Body Suppliers", bodySupplierSpeedGroup, 
            e -> factory.setDelaySuppliersBody(getSelectedDelay(bodySupplierSpeedGroup))));
        
        controlPanel.add(createSpeedControlPanel("Engine Suppliers", engineSupplierSpeedGroup,
            e -> factory.setDelaySuppliersEngine(getSelectedDelay(engineSupplierSpeedGroup))));
        
        controlPanel.add(createSpeedControlPanel("Accessories Suppliers", accessoriesSupplierSpeedGroup,
            e -> factory.setDelaySuppliersAccessories(getSelectedDelay(accessoriesSupplierSpeedGroup))));
        
        controlPanel.add(createSpeedControlPanel("Dealers", dealerSpeedGroup, 
            e -> factory.setDelayDealers(getSelectedDelay(dealerSpeedGroup))));
        
        controlPanel.add(createSpeedControlPanel("Workers", workerSpeedGroup, 
            e -> factory.setDelayWorkers(getSelectedDelay(workerSpeedGroup))));
        
        setDefaultSpeed(bodySupplierSpeedGroup);
        setDefaultSpeed(engineSupplierSpeedGroup);
        setDefaultSpeed(accessoriesSupplierSpeedGroup);
        setDefaultSpeed(dealerSpeedGroup);
        setDefaultSpeed(workerSpeedGroup);
        
        mainPanel.add(storagePanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        pack();
        setSize(500, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createSpeedControlPanel(String title, ButtonGroup group, ActionListener listener) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JRadioButton fastBtn = new JRadioButton("Fast");
        JRadioButton normalBtn = new JRadioButton("Normal");
        JRadioButton slowBtn = new JRadioButton("Slow");
        
        fastBtn.setActionCommand("Fast");
        normalBtn.setActionCommand("Normal");
        slowBtn.setActionCommand("Slow");
        
        fastBtn.addActionListener(listener);
        normalBtn.addActionListener(listener);
        slowBtn.addActionListener(listener);
        
        group.add(fastBtn);
        group.add(normalBtn);
        group.add(slowBtn);
        
        panel.add(fastBtn);
        panel.add(normalBtn);
        panel.add(slowBtn);
        
        return panel;
    }

    private void setDefaultSpeed(ButtonGroup group) {
        for (AbstractButton button : java.util.Collections.list(group.getElements())) {
            if (button.getText().equals("Normal")) {
                button.setSelected(true);
                break;
            }
        }
    }

    private int getSelectedDelay(ButtonGroup group) {
        ButtonModel selectedModel = group.getSelection();
        if (selectedModel == null) {
            return NORMAL_SPEED;
        }

        for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();
            if (button.getModel() == selectedModel) {
                switch (button.getText()) {
                    case "Fast": return FAST_SPEED;
                    case "Slow": return SLOW_SPEED;
                    default: return NORMAL_SPEED;
                }
            }
        }

        return NORMAL_SPEED;
    }

    private void startUpdateTimer() {
        Timer timer = new Timer(500, e -> updateStorageInfo());
        timer.start();
    }

    private void updateStorageInfo() {
        bodyStorageInfo.setText("Body Storage: " + factory.getBodyStorageCapacity());
        engineStorageInfo.setText("Engine Storage: " + factory.getEngineStorageCapacity());
        accessoriesStorageInfo.setText("Accessories Storage: " + factory.getAccessoriesStorageCapacity());
        carStorageInfo.setText("Car Storage: " + factory.getCarStorageCapacity());
    }
}