package mySql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mySql.commands.Command;
import mySql.commands.SelectCommand;
import mySql.dataBase.Column;
import mySql.dataBase.Database;
import mySql.dataBase.Table;

import java.io.File;
import java.util.ArrayList;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PDO {
    private static String PATH_DB = "";
    private static String RESULT_SQL = "";
    private static String DB_LAST = "";
    private static String TABLE_LAST = "";

    public PDO(String pathDb) {
        setPathDb(pathDb);
    }

    public PDO() {}

    public void executeSQL(String sql, String dbName) {
        boolean isErase = sql.split(" ")[0].equals("ERASE");
        boolean isInit = sql.split(" ")[0].equals("INIT");

        setDbLast(dbName);

        Database db = (isErase || isInit) ? loadDB(null) : loadDB(dbName);
        
        setResultSql("");
        Command commandObj = CommandFactory.createCommand(sql);
        if (commandObj == null) {
            throw new IllegalArgumentException("Неизвестная команда: " + sql);
        }

        commandObj.execute(db);

        if (commandObj.getClass() == SelectCommand.class) {
            setTableLast(((SelectCommand) commandObj).getTableName());
        }

        if (!isErase) {
            writeDB(db);
        }
    }

    public Table getTable(String dbName, String tableName) {
        return loadDB(dbName).getTable(tableName);
    }

    private Database loadDB(String dbName) {
        if (dbName == null) return new Database();

        Database db = null;

        ObjectMapper mapper = new ObjectMapper();
        try {
            db = mapper.readValue(new File(getPathDb() + dbName + ".json"), Database.class);
            return db;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Ошибка загрузки базы данных: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка загрузки базы данных 2: " + e.getMessage());
        }
    }

    private void writeDB(Database db) {
        try {
            new ObjectMapper().writeValue(new File(getPathDb() + db.getName() + ".json"), db);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка записи базы данных: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Неожиданная ошибка записи базы данных: " + e.getMessage());
        }
    }

    public void importDB(String pathToDB) {
        Database db;

        ObjectMapper mapper = new ObjectMapper();
        try {
            db = mapper.readValue(new File(pathToDB), Database.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка считывания базы данных: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Неожиданная ошибка считывания базы данных: " + e.getMessage());
        }

        List<String> listDb = getListDataBase();

        if (listDb.contains(db.getName())) {
            throw new IllegalArgumentException("Такая база данных уже существует!");
        }

        writeDB(db);
    }

    public String exportDb(String nameDB) {
        String jsonDb = "";
        try {
            StringWriter writer = new StringWriter();
            new ObjectMapper().writeValue(writer, loadDB(nameDB));
            jsonDb = writer.toString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка записи базы данных: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Неожиданная ошибка записи базы данных: " + e.getMessage() + " ");
        }
        return jsonDb;
    }

    public static String getPathDb() {
        return PATH_DB;
    }

    public static void setPathDb(String pathDb) {
        if (pathDb == null) return;
        if (pathDb.charAt(pathDb.length() - 1) != '/') {
            pathDb += "/";
        }
        PATH_DB = pathDb;
    }

    public static String getResultSql() {
        return RESULT_SQL;
    }

    public static void setResultSql(String resultSql) {
        RESULT_SQL = resultSql;
    }

    public List<String> getListDataBase() {
        File folderDB = new File(getPathDb());
        File[] listFileDB = folderDB.listFiles();
        List<String> listDB = new ArrayList<>();

        if (listFileDB == null) return listDB;

        for (File file : listFileDB) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                String fileNameWithoutExtension = file.getName().substring(0, file.getName().length() - 5);
                try {
                    if (!fileNameWithoutExtension.equals(loadDB(fileNameWithoutExtension).getName())) continue;
                    listDB.add(fileNameWithoutExtension);
                } catch (Exception ignored) {}
            }
        }
        return listDB;
    }

    public Set<String> getSetTables(String nameDB) {
        Database db = loadDB(nameDB);

        return db.getSetTables();
    }

    public void createDB(String nameDB) {
        File file = new File(getPathDb() + nameDB + ".json");

        if (file.exists() && file.isFile()) {
            throw new IllegalArgumentException("База данных с именем " + nameDB + " уже существует");
        }

        writeDB(new Database(nameDB));
    }

    public List<Column> getColumnsTable(String nameDB, String tableName) {
        Database db = loadDB(nameDB);

        return db.getTable(tableName).getColumns();
    }

    // TODO: добавить проверку типа данных
    private void validateStruct(List<Column> newStruct) {
        Set<String> columnNames = new HashSet<>();
        for (Column column : newStruct) {
            if (column.getName() == null || column.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Нельзя создавать столбик с пустым именем");
            }
            if (!columnNames.add(column.getName())) {
                throw new IllegalArgumentException("Есть повторяющиеся столбики");
            }
        }
    }

    public void updateTableStruct(String nameDB, String tableName, List<Column> newStruct) {
        Database db = loadDB(nameDB);
        Table table = db.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException("Таблица " + tableName + " не найдена в базе данных " + nameDB);
        }

        validateStruct(newStruct);

        table.setColumns(newStruct);
        table.restructureData();

        writeDB(db);
    }

    public void renameTable(String nameDB, String tableName, String newTableName) {
        Database db = loadDB(nameDB);
        Table table = db.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException("Таблица " + tableName + " не найдена в базе данных " + nameDB);
        }

        table.setName(newTableName);

        db.getTables().remove(tableName);
        db.getTables().put(newTableName, table);

        writeDB(db);
    }

    public void setDbLast(String dbLast) {
        DB_LAST = dbLast;
    }

    public void setTableLast(String tableLast) {
        TABLE_LAST = tableLast;
    }

    public static String getDbLast() {
        return DB_LAST;
    }

    public static String getTableLast() {
        return TABLE_LAST;
    }
}
