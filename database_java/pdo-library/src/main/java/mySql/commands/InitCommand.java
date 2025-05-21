package mySql.commands;

import mySql.PDO;
import mySql.dataBase.Database;

import java.io.File;
import java.util.regex.*;

@CommandName("INIT")
public class InitCommand extends AbstractCommand {
    private String databaseName;

    public InitCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute(Database db) {
        validateSql();
        validateData(db);
        doRequest(db);
    }

    private void validateSql() {
        Pattern tablePattern = Pattern.compile("^INIT DATABASE ([a-zA-Z_][a-zA-Z0-9_]*)$");
        Matcher tableMatcher = tablePattern.matcher(sql);

        if (!tableMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        databaseName = tableMatcher.group(1);
    }

    private void validateData(Database db) {
        File file = new File(PDO.getPathDb() + databaseName + ".json");
        if (file.exists()) {
            throw new IllegalArgumentException("База данных с таким именем уже существует!");
        }
    }

    private void doRequest(Database db) {
        db.setName(databaseName);
    }
}
