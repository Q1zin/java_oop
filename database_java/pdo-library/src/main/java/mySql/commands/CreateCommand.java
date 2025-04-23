package mySql.commands;

import mySql.TypeFactory;
import mySql.dataBase.Column;
import mySql.dataBase.Database;
import mySql.dataBase.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import java.util.stream.Collectors;

@CommandName("CREATE")
public class CreateCommand extends AbstractCommand {
    private String tableName;
    private final List<Column> columnsList = new ArrayList<>();

    private String columnsString;

    public CreateCommand(String sql) {
        super(sql);
    }

    @Override
    public void execute(Database db) {
        validateSql();
        parseData();
        validateData(db);
        doRequest(db);
    }

    private void validateSql() {
        Pattern tablePattern = Pattern.compile("^CREATE TABLE ([a-zA-Z_][a-zA-Z0-9_]*) \\((.*)\\)$");
        Matcher tableMatcher = tablePattern.matcher(sql);

        if (!tableMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        tableName = tableMatcher.group(1);
        columnsString = tableMatcher.group(2);
    }

    private void parseData() {
        if (columnsString.trim().isEmpty()) { return; }
        String[] columnsArray = columnsString.trim().split(";\\s*");

        String regexValue = TypeFactory.getTypes().stream()
                .map(type -> type.replace("[", "\\[").replace("]", "\\]"))
                .collect(Collectors.joining("|"));

        Pattern columnPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*) (" + regexValue + ")(?: (unique|not-null))?(?: (unique|not-null))?");

        for (String column : columnsArray) {
            Column newColumn = getColumn(column, columnPattern);

            columnsList.add(newColumn);
        }
    }

    private static Column getColumn(String column, Pattern columnPattern) {
        Matcher columnMatcher = columnPattern.matcher(column.trim());

        if (!columnMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат столбца: " + column);
        }


        List<String> attributes = new ArrayList<>();
        for (int i = 3; i <= columnMatcher.groupCount(); i++) {
            if (columnMatcher.group(i) != null) {
                attributes.add(columnMatcher.group(i));
            }
        }

        return new Column(columnMatcher.group(1), columnMatcher.group(2), attributes);
    }

    private void validateData(Database db) {
        if (db.containsTable(tableName)) {
            throw new IllegalArgumentException("Таблица с таким именем уже есть");
        }
    }

    private void doRequest(Database db) {
        Table newTable = new Table(tableName);
        for (Column column : columnsList) {
            newTable.addColumn(column);
        }
        db.addTable(newTable);
    }
}
