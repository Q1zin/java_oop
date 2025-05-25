package mySql.commands;

import mySql.PDO;
import mySql.dataBase.Database;

import java.io.File;
import java.util.regex.*;

@CommandName("ERASE")
public class EraseCommand extends AbstractCommand {
    private String dbName;
    File file;

    public EraseCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute(Database db) {
        validateSql();
        validateData(db);
        doRequest(db);
    }

    private void validateSql() {
        Pattern databasePattern = Pattern.compile("^ERASE DATABASE ([a-zA-Z_][a-zA-Z0-9_]*)$");
        Matcher databaseMatcher = databasePattern.matcher(sql);

        if (!databaseMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        dbName = databaseMatcher.group(1);
    }

    private void validateData(Database db) {
        file = new File(PDO.getPathDb() + dbName + ".json");
        if (!file.exists()) {
            throw new IllegalArgumentException("Базы данных с таким именем нету");
        }
    }

    private void doRequest(Database db) {
        file.delete();
    }
}
