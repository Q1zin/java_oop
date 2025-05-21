package mySql.commands;

import mySql.PDO;
import mySql.dataBase.Database;
import mySql.dataBase.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;


@CommandName("SELECT")
public class SelectCommand extends AbstractCommand {
    private String tableName;
    private String conditionString;
    private List<String> conditionList = new ArrayList<>();
    private String fieldsString;
    private List<String> fieldList = new ArrayList<>();
    private boolean allFields = false;

    List<Map<String, Object>> listSelected;

    public SelectCommand(String sql) {
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
        Pattern tablePattern = Pattern.compile("SELECT (\\*|[a-zA-Z_][a-zA-Z0-9_,]+) FROM ([a-zA-Z_][a-zA-Z0-9_]*)(?: WHERE (.+))?");
        Matcher tableMatcher = tablePattern.matcher(sql);

        if (!tableMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        fieldsString = tableMatcher.group(1);
        tableName = tableMatcher.group(2);
        conditionString = tableMatcher.group(3);
    }

    private void parseData() {
        if (fieldsString.equals("*")) {
            allFields = true;
        }

        fieldList = List.of(fieldsString.split(","));

        if (conditionString == null || conditionString.isEmpty()) {
            return;
        }

        Pattern conditionPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=)\\s*(\\\"[^\\\"]*\\\"|\\[(?:[^\\[\\]]*?)\\]|[a-zA-Z0-9_\\-]+)");
        Matcher conditionMatcher = conditionPattern.matcher(conditionString);

        while (conditionMatcher.find()) {
            conditionList.add(conditionMatcher.group(1) + conditionMatcher.group(2) +  conditionMatcher.group(3));
        }
    }

    private void validateData(Database db) {
        if (!db.containsTable(tableName)) {
            throw new IllegalArgumentException("Таблицы с таким именем нету");
        }

        Table table = db.getTable(tableName);

        if (!allFields) {
            for (String col : fieldList) {
                if (!table.hasColumn(col)) {
                    throw new IllegalArgumentException("Нету такого поля: " + col);
                }
            }
        }

        listSelected = table.getWhereData(conditionList);
    }

    private void doRequest(Database db) {
        if (allFields) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                PDO.setResultSql(objectMapper.writeValueAsString(listSelected));
            } catch (Exception e) {
                throw new RuntimeException("Неожиданная ошибка при сохранении результата");
            }
            return;
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (var row : listSelected) {
            Map<String, Object> newRow = new HashMap<>();
            for (String col : fieldList) {
                if (row.containsKey(col)) {
                    newRow.put(col, row.get(col));
                }
            }
            if (!newRow.isEmpty()) {
                result.add(newRow);
            }
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PDO.setResultSql(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            throw new RuntimeException("Неожиданная ошибка при сохранении результата");
        }
    }

    public String getTableName() {
        return tableName;
    }
}