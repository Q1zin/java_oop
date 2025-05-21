package org.openjfx.services;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.Properties;

public class FileHandler {
    public File openFileDialog(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database file", "*.json"));
        return fileChooser.showOpenDialog(null);
    }

    public File saveFileDialog(String title, String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database file", "*.json"));
        return fileChooser.showSaveDialog(null);
    }

    public void saveToFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    public File chooseDatabaseDirectory(String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        return directoryChooser.showDialog(null);
    }

    public File getAppDataDirectory() {
        String home = System.getProperty("user.home");
        String appDataPath;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            appDataPath = home + "\\AppData\\Local\\VovixBD\\";
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            appDataPath = home + "/Library/Application Support/VovixBD/";
        } else {
            appDataPath = home + "/.VovixBD/";
        }

        File appDataDir = new File(appDataPath);
        if (!appDataDir.exists()) {
            appDataDir.mkdirs();
        }

        return appDataDir;
    }

    public String getDbPath() {
        File settingsFile = new File(getAppDataDirectory(), "settings.properties");

        if (!settingsFile.exists()) {
            return null;
        }

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(settingsFile)) {
            properties.load(fis);
            return properties.getProperty("db_path");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить настройки приложения", e);
        }
    }

    public void saveDbPath(File settingsFile, String dbPath) {
        Properties properties = new Properties();

        if (settingsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException("Не удалось загрузить настройки приложения", e);
            }
        }

        properties.setProperty("db_path", dbPath);

        try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
            properties.store(fos, "Application Settings");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить настройки приложения", e);
        }
    }
}