package org.openjfx;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import mySql.dataBase.Column;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openjfx.services.FileHandler;
import org.openjfx.services.NotificationManager;

import javafx.event.ActionEvent;
import javafx.scene.control.TableCell;
import javafx.util.Callback;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import mySql.PDO;

public class FXMLController {
    private PDO pdo;
    private NotificationManager notificationManager;
    private final FileHandler fileHandler = new FileHandler();
    private static final Logger logger = LogManager.getLogger(FXMLController.class);
    private boolean isProgrammaticChange = false;
    private Stage settingsStage = null;
    Parent windowEditStruct = null;
    FXMLLoader loaderEditStruct = null;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnAddField, btnClearSql, btnDoSql, btnCreateNewTable, btnRemoveDb,
            btnRemoveTable, btnSaveStruct, btnWriteDeleteCommand, btnWriteDropCommand,
            btnWriteInsertCommand, btnWriteSelectCommand, createDBBtn, exportDBBtn, importDBBtn, btnSettings;

    @FXML
    private TextField fieldAddField, fieldNameTable, fieldNewDB;

    @FXML
    private Label lableNameDb, viewCommad;

    @FXML
    private VBox messageBlock;

    @FXML
    private Tab tabSql, tabStruct, tabView, tabOperation;

    @FXML
    private TableView<TableRowData> tableStruct;
    
    @FXML
    private TableColumn<TableRowData, String> nameCol;

    @FXML
    private TableColumn<TableRowData, String> typeCol;

    @FXML
    private TableColumn<TableRowData, Boolean> notNullCol;

    @FXML
    private TableColumn<TableRowData, Boolean> uniqueCol;

    @FXML
    private TableColumn<TableRowData, Void> actionCol;

    @FXML
    private ListView<String> ListDB;

    @FXML
    private TableView<Map<String, Object>> tableView;

    @FXML
    private TabPane tabsActions, tabsTable;

    @FXML
    private TextArea textareaSql;

    @FXML
    void initialize() {
        pdo = new PDO(fileHandler.getDbPath());
        notificationManager = new NotificationManager(messageBlock);

        closeDbInterface();
        updateListDB();

        tabsActions.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == null || newTab != tabView) { return; }
            if (isProgrammaticChange) {
                isProgrammaticChange = false;
                return;
            }

            loadTabView();
        });

        ListDB.setOnMouseClicked(event -> openDbInterface());
        createDBBtn.setOnMouseClicked(event -> createDatabase());
        importDBBtn.setOnMouseClicked(event -> importDatabase());
        exportDBBtn.setOnMouseClicked(event -> exportDatabase());
        btnSettings.setOnMouseClicked(event -> openSettings());
        tabsTable.setOnMouseClicked(event -> openTableInterface());
        btnCreateNewTable.setOnMouseClicked(event -> createNewTable());
        btnSaveStruct.setOnMouseClicked(event -> saveStruct());
        btnAddField.setOnMouseClicked(event -> addNewColumn());
        btnDoSql.setOnMouseClicked(event -> doSql());

        btnWriteSelectCommand.setOnMouseClicked(event -> {
            textareaSql.setText("SELECT * FROM " + getSelectTable());
        });
        btnWriteInsertCommand.setOnMouseClicked(event -> {
            textareaSql.setText("INSERT INTO " + getSelectTable() + " ()");
        });
        btnWriteDropCommand.setOnMouseClicked(event -> {
            textareaSql.setText("DROP TABLE " + getSelectTable());
        });
        btnWriteDeleteCommand.setOnMouseClicked(event -> {
            textareaSql.setText("DELETE FROM " + getSelectTable() + " WHERE");
        });
        btnClearSql.setOnMouseClicked(event -> {
            textareaSql.setText("");
        });

        btnRemoveDb.setOnMouseClicked(event -> removeDb());
        btnRemoveTable.setOnMouseClicked(event -> removeTable());

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        notNullCol.setCellValueFactory(new PropertyValueFactory<>("notNull"));
        notNullCol.setCellFactory(tc -> new CheckBoxTableCell<>());
        
        uniqueCol.setCellValueFactory(new PropertyValueFactory<>("unique"));
        uniqueCol.setCellFactory(tc -> new CheckBoxTableCell<>());
        
        Callback<TableColumn<TableRowData, Void>, TableCell<TableRowData, Void>> cellFactory =
            new Callback<>() {
                @Override
                public TableCell<TableRowData, Void> call(final TableColumn<TableRowData, Void> param) {
                    return new TableCell<>() {
                        private final Button deleteButton = new Button("Удалить");
                        private final Button editButton = new Button("Редактировать");
                        private final HBox pane = new HBox(10, editButton, deleteButton);

                        {
                            deleteButton.setOnAction((ActionEvent event) -> {
                                TableRowData rowData = getTableView().getItems().get(getIndex());
                                getTableView().getItems().remove(rowData);
                            });

                            editButton.setOnAction((ActionEvent event) -> {
                                TableRowData rowData = getTableView().getItems().get(getIndex());
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("edit_struct_scene.fxml"));
                                    Parent root = loader.load();
                            
                                    FXMLControllerEditStruct editController = loader.getController();
                                    editController.setData(rowData);
                                    editController.setOnSaveCallback(() -> {
                                        getTableView().refresh();
                                    });
                            
                                    Stage newStage = new Stage();
                                    newStage.setScene(new Scene(root));
                                    newStage.setResizable(false);
                                    newStage.setTitle("VovixBD - Редактирование структуры базы данных");
                                    newStage.show();
                                } catch (IOException e) {
                                    notificationManager.showError("Произошла неожиданная ошибка при редактировании");
                                    logger.error("Ошибка при инициализации окна редактирования. Ошибка: {}. StackTrase: {}", e.getMessage(), e.getStackTrace());
                                }
                            });
                        }
                        
                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(pane);
                            }
                        }
                    };
                }
        };
        actionCol.setCellFactory(cellFactory);
    }

    public void addNewColumn() {
        try {
            int countCol = Integer.parseInt(fieldAddField.getText());
            if (countCol <= 0 || countCol > 20) {
                logger.info("Ввели неверный размер для вставки колонок: {}, Ошибка: {}", fieldAddField.getText(), countCol);
                notificationManager.showWarning("Количество новых строк быть от 1 до 20");
                return;
            }
            int countAllCol = getCountRowStruct();
            for (int i = 1; i <= countCol; i++) {
                addRowStruct("Col " + (countAllCol + i), "string", false, false);
            }

        } catch (NumberFormatException e) {
            logger.warn("Ошибка парсинга количества строк, строка: {}, Ошибка: {}", fieldAddField.getText(), e.getMessage());
            notificationManager.showWarning("Введите целое число");
        }

    }

    public void addRowStruct(String name, String type, boolean notNull, boolean unique) {
        if (tableStruct.getItems() == null) {
            tableStruct.setItems(FXCollections.observableArrayList());
        }

        TableRowData newRow = new TableRowData(name, type, notNull, unique);
        tableStruct.getItems().add(newRow);
    }

    public int getCountRowStruct() {
        return tableStruct.getItems().size();
    }

    public void clearRowStruct() {
        tableStruct.getItems().clear();
    }

    private void removeTable() {
        try {
            pdo.executeSQL("DROP TABLE " + getSelectTable(), getSelectDb());

            tabsTable.getTabs().remove(tabsTable.getSelectionModel().getSelectedItem());
            openDbInterface();

            notificationManager.showNotify("Таблица успешно удалена");
        } catch (IllegalArgumentException e) {
            logger.error("Не удалось удалить таблицу {}. Произошла внутренняя ошибка: {}. StackTrace: {}", getSelectTable(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось удалить таблицу " + getSelectTable() + ". Произошла внутренняя ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Не удалось удалить таблицу {}. Произошла внутренняя ошибка (неожиданная ошибка) : {}. StackTrace: {}", getSelectTable(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось удалить таблицу " + getSelectTable() + ". Произошла внутренняя ошибка (неожиданная ошибка): " + e.getMessage());
        }
    }

    private void removeDb() {
        try {
            pdo.executeSQL("ERASE DATABASE " + getSelectDb(), getSelectDb());

            ListDB.getItems().remove(ListDB.getSelectionModel().getSelectedItem());
            openDbInterface();

            notificationManager.showNotify("База данных успешно удалена");
        } catch (IllegalArgumentException e) {
            logger.error("Не удалось удалить базу данных {}. Произошла внутренняя ошибка: {}. StackTrace: {}", getSelectDb(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось удалить базу данных " + getSelectDb() + ". Произошла внутренняя ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Не удалось удалить базу данных {}. Произошла внутренняя ошибка (неожиданная ошибка) : {}. StackTrace: {}", getSelectDb(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось удалить базу данных " + getSelectDb() + ". Произошла внутренняя ошибка (неожиданная ошибка): " + e.getMessage());
        }
    }

    private void openDbInterface() {
        if (getSelectDb().isEmpty()) {
            closeDbInterface();
            return;
        }

        btnCreateNewTable.setVisible(true);
        tabsTable.setVisible(true);
        tabsActions.setVisible(true);

        openTableInterface();
    }

    private void closeDbInterface() {
        btnCreateNewTable.setVisible(false);
        tabsTable.setVisible(false);
        tabsActions.setVisible(false);
    }

    private void createDatabase() {
        String nameNewDB = fieldNewDB.getText();

        if (nameNewDB.isEmpty()) {
            notificationManager.showWarning("Введите имя базы данных!");
            return;
        }

        try {
            logger.info("Создаём базу данных: INIT DATABASE {}", nameNewDB);
            pdo.executeSQL("INIT DATABASE " + nameNewDB, nameNewDB);

            ListDB.getItems().addFirst(nameNewDB);
            fieldNewDB.setText("");

            ListDB.getSelectionModel().selectFirst();
            openDbInterface();

            notificationManager.showNotify("База данных создана.");
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании базы данных: : {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Ошибка при создании базы данных: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка при создании базы данных (неожиданная ошибка): {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Ошибка при создании базы данных (неожиданная ошибка): " + e.getMessage());
        }
    }

    private void importDatabase() {
        logger.info("Начали импорт базы данных");
        File file = fileHandler.openFileDialog("Выберите файл базы данных");
        if (file != null) {
            try {
                logger.info("Импорт базы данных в PDO: {}", file.getAbsolutePath());
                pdo.importDB(file.getAbsolutePath());
                notificationManager.showNotify("База данных импортирована.");

                updateListDB();
                ListDB.getSelectionModel().selectLast();

                openDbInterface();
            } catch (IllegalArgumentException e) {
                logger.error("Ошибка импорта: : {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                notificationManager.showError("Ошибка импорта: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Ошибка импорта (неожиданная ошибка): {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                notificationManager.showError("Ошибка импорта (неожиданная ошибка): " + e.getMessage());
            }
        } else {
            logger.info("Пользователь не выбрал файл для импорта");
        }
    }

    private void exportDatabase() {
        logger.info("Начали экспорт базы данных");
        String dbName = getSelectDb();
        if (dbName.isEmpty()) {
            logger.info("Пользователь не выбрал базу данных для экспорта");
            notificationManager.showWarning("Выберите базу данных.");
            return;
        }

        File file = fileHandler.saveFileDialog("Сохранить базу данных", dbName + ".json");
        if (file != null) {
            try {
                fileHandler.saveToFile(file, pdo.exportDb(dbName));
                notificationManager.showNotify("Файл сохранен.");
            } catch (IOException e) {
                logger.error("Ошибка экспорта: {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                notificationManager.showError("Ошибка экспорта: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Ошибка экспорта (неожиданная ошибка): {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                notificationManager.showError("Ошибка экспорта (неожиданная ошибка): " + e.getMessage());
            }
        } else {
            logger.info("Пользователь не выбрал файл для экспорта");
        }
    }

    private void openSettings() {
        if (settingsStage != null) {
            settingsStage.toFront();
            settingsStage.requestFocus();
            return;
        }

        try {
            settingsStage = new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("settings_scene.fxml"));
            Parent settingsRoot = loader.load();
            settingsStage.setScene(new Scene(settingsRoot));

            settingsStage.setTitle("VovixBD - Настройки");
            settingsStage.setResizable(false);

            FXMLControllerSettings controller = loader.getController();
            controller.updateText(PDO.getPathDb());

            settingsStage.setOnHiding(event -> {
                String selectedPath = controller.getSelectedPath();
                if (selectedPath != null && !selectedPath.isEmpty() && !selectedPath.equals(PDO.getPathDb())) {
                    PDO.setPathDb(selectedPath);
                    File settingsFile = new File(fileHandler.getAppDataDirectory(), "settings.properties");
                    fileHandler.saveDbPath(settingsFile, PDO.getPathDb());
                    closeDbInterface();
                    updateListDB();
                }
                settingsStage = null;
            });

            settingsStage.show();
            controller.initialize();
        } catch (IOException e) {
            logger.error("Ошибка при открытии окна настроек: {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Ошибка: Не удалось загрузить окно настроек.");
        }
    }

    private void openTableInterface() {
        updateTabsTable();
        updateTabsActions();
    }

    private void createNewTable() {
        if (!tabsTable.getTabs().isEmpty() && tabsTable.getTabs().getLast().getText().equals("?")) {
            return;
        }

        tabsTable.getTabs().addLast(new Tab("?"));
        tabsTable.getSelectionModel().selectLast();
        updateTabsActions();
    }

    private boolean validateNameTable(String tableName) {
        int length = tableName.length();
        return length >= 3 && length <= 32 && Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)$").matcher(tableName).matches();
    }

    private void saveStruct() {
        String tableName = fieldNameTable.getText();
        if (!validateNameTable(tableName)) {
            notificationManager.showError("Имя таблицы не прошло валидацию: " + tableName);
            logger.info("Имя таблицы не прошло валидацию: {}", tableName);
            return;
        }

        if (tableStruct.getItems().isEmpty()) {
            notificationManager.showError("Необходим хотя бы один столбик");
            logger.info("Необходим хотя бы один столбик");
            return;
        }

        List<Column> newStruct = new ArrayList<>();
        List<TableRowData> rowData = new ArrayList<>(tableStruct.getItems());
        for (var row : rowData) {
            String name = row.getName();
            String type = row.getType();
            List<String> flags = new ArrayList<>();
            if (row.isNotNull()) {
                flags.add("not-null");
            }
            if (row.isUnique()) {
                flags.add("unique");
            }

            newStruct.add(new Column(name, type, flags));
        }

        try {
            pdo.updateTableStruct(getSelectDb(), getSelectTable(), newStruct);

            if (!tableName.equals(getSelectTable())) {
                pdo.renameTable(getSelectDb(), getSelectTable(), tableName);
                updateTabsTable();
                tabsTable.getSelectionModel().selectLast();
                updateTabsActions();
                tabsActions.getSelectionModel().select(1);

            }

            notificationManager.showNotify("Структура успешно изменена");
            logger.info("Структура успешно изменена в {} в {}", getSelectDb(), getSelectTable());
        } catch (Exception e) {
            logger.info("Ошибка при изменении структуры в {} в {} на {}. Ошибка: {}, StackTrace: {}", getSelectDb(), getSelectTable(), newStruct, e.getMessage(), e.getStackTrace());
            notificationManager.showError("Ошибка при изменении структуры. Ошибка: " + e.getMessage());
        }


    }

    private void doSql() {
        String sql = textareaSql.getText();
        String nameDB = getSelectDb();

        if (sql.isEmpty() || nameDB.isEmpty()) { return; }

        logger.info("Начали выполнение sql запроса пользователя: nameDb: {}, sql: {}", nameDB, sql);

        String command = sql.split(" ")[0];

        try {
            if (command.equals("SELECT")) {
                logger.info("Переходим в обзор и выводим результат (Команда SELECT)");
                if (!loadTabView(sql)) {
                    notificationManager.showError("Не удалось выполнить запрос: Неверный формат команды!");
                    return;
                }
                isProgrammaticChange = true;
                tabsActions.getSelectionModel().select(0);
                textareaSql.setText("");
                notificationManager.showNotify("Запрос успешно выполнен.");
                return;
            }
            pdo.executeSQL(sql, nameDB);

            textareaSql.setText("");
            notificationManager.showNotify("Запрос успешно выполнен.");
            logger.info("Запрос успешно выполнен ({}). Результат {}", sql, PDO.getResultSql());

            switch (command) {
                case "DROP" -> openDbInterface();
                case "ERASE" -> {
                    updateListDB();
                    openDbInterface();
                }
                case "CREATE" -> updateTabsTable();
                case "INIT" -> updateListDB();
            }
        } catch (IllegalArgumentException e) {
            notificationManager.showError("Не удалось выполнить запрос: " + e.getMessage());
            logger.error("Не удалось выполнить запрос {}: {}. StackTrace: {}", sql, e.getMessage(), Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            logger.error("Не удалось выполнить запрос (неожиданная ошибка) {}: {}. StackTrace: {}", sql, e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось выполнить запрос. " + e.getMessage());
        }
    }

    private void updateListDB() {
        List<String> dbs = pdo.getListDataBase();
        Set<String> existingDbNames = new HashSet<>(ListDB.getItems());
        List<String> newDb = new ArrayList<>();

        for (String dbName : dbs) {
            if (!existingDbNames.contains(dbName)) {
                newDb.add(dbName);
            }
        }

        ListDB.getItems().addAll(newDb);

        ListDB.getItems().removeIf(db -> !dbs.contains(db));

        if (ListDB.getItems().isEmpty()) {
            closeDbInterface();
        }
    }

    private void updateTabsTable() {
        Set<String> existingTabNames = new HashSet<>();
        for (Tab tab : tabsTable.getTabs()) {
            existingTabNames.add(tab.getText());
        }

        Set<String> tables = pdo.getSetTables(getSelectDb());

        List<Tab> newTabs = new ArrayList<>();
        for (String tableName : tables) {
            if (!existingTabNames.contains(tableName)) {
                newTabs.add(new Tab(tableName));
            }
        }

        tabsTable.getTabs().addAll(newTabs);

        tabsTable.getTabs().removeIf(tab -> !tables.contains(tab.getText()));

        if (tabsTable.getTabs().isEmpty()) {
            tabsTable.getTabs().add(new Tab("?"));
        }
    }

    private void updateTabsActions() {
        if (tabsTable.getTabs().getLast().getText().equals("?")) {
            tabsActions.getSelectionModel().select(1);
        } else {
            tabsActions.getSelectionModel().select(0);
        }

        loadTabView();
        loadTabStruct();
        loadTabSql();
        loadTabOperation();
    }

    private void loadTabView() {
        loadTabView("SELECT * FROM " + getSelectTable());
    }

    private boolean loadTabView(String sql) {
        if (getSelectTable().equals("?")) {
            viewCommad.setText(sql);
            return false;
        }

        try {
            pdo.executeSQL(sql, getSelectDb());

            viewCommad.setText(sql);
            clearTableData();
            tableView.setPlaceholder(new Label("Таблица пуста"));

            showTable(PDO.getResultSql());

            return true;
        } catch (IllegalArgumentException e) {
            logger.error("Не удалось выполнить запрос для отображения данных {}: {}. StackTrace: {}", sql, e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось отобразить данные. Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Не удалось выполнить запрос для отображения данных (неожиданная ошибка) {}: {}. StackTrace: {}", sql, e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось отобразить данные, передайте логи администратору. Ошибка: " + e.getMessage());
        }

        return false;
    }

    private void loadTabOperation() {
        btnRemoveTable.setVisible(!tabsTable.getTabs().getLast().getText().equals("?"));
        btnRemoveDb.setVisible(!getSelectDb().isEmpty());
    }

    private void loadTabSql() {
        lableNameDb.setText(getSelectDb());
        textareaSql.setText("");
    }

    private void loadTabStruct() {
        boolean isNone = tabsTable.getTabs().getLast().getText().equals("?");
        fieldNameTable.setText(isNone ? "" : getSelectTable());
        clearRowStruct();
        if (isNone) {
            return;
        }

        List<Column> cols = pdo.getColumnsTable(getSelectDb(), getSelectTable());
        for (Column col : cols) {
            String name = col.getName();
            String type = col.getType();
            List<String> flags = col.getFlags();

            addRowStruct(name, type, flags.contains("not-null"), flags.contains("unique"));
        }
    }

    private String getSelectDb() {
        String dbName = ListDB.getSelectionModel().getSelectedItem();
        return (dbName != null) ? dbName : "";
    }

    private String getSelectTable() {
        var tableName = tabsTable.getSelectionModel().getSelectedItem();
        return (tableName != null) ? tableName.getText() : "";
    }

    private void clearTableData() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
    }

    private void showTable(String sqlResult) {
        List<Column> cols = pdo.getColumnsTable(PDO.getDbLast(), PDO.getTableLast());
        List<String> colNames = cols.stream().map(Column::getName).toList();

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> items = mapper.readValue(
                    sqlResult,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            ObservableList<Map<String, Object>> itemsList = FXCollections.observableArrayList();
            itemsList.addAll(items);

            tableView.getColumns().clear();

            for (String colName : colNames) {
                TableColumn<Map<String, Object>, Object> column = new TableColumn<>(colName);
                column.setCellValueFactory(cellData ->
                        new SimpleObjectProperty<>(cellData.getValue().get(colName)));
                tableView.getColumns().add(column);
            }

            tableView.setItems(itemsList);
        } catch (Exception e) {
            logger.error("Не удалось выполнить преобразования для отображения данных {}: {}. StackTrace: {}", sqlResult, e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось выполнить преобразования для отображения данных. Ошибка: " + e.getMessage());
        }
    }
}
