package mySql.commands;

import mySql.TypeFactory;
import mySql.dataBase.Column;
import mySql.dataBase.Database;
import mySql.dataBase.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandName("INSERT")
public class InsertCommand extends AbstractCommand {
    private String tableName;
    private String stringData;
    private List<List> dataStringList = new ArrayList<>();
    private List<Map<String, Object>> dataList = new ArrayList<>();


    public InsertCommand(String sql) {
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
        Pattern inserPattern = Pattern.compile("^INSERT INTO ([a-zA-Z_][a-zA-Z0-9_]*) \\((.*)\\)$");
        Matcher insertMatcher = inserPattern.matcher(sql);

        if (!insertMatcher.matches()) {
            throw new IllegalArgumentException("Неверный формат команды!");
        }

        tableName = insertMatcher.group(1);
        stringData = insertMatcher.group(2);

        if (!stringData.trim().startsWith("(") || !stringData.trim().endsWith(")")) {
            stringData = "(" + stringData + ")";
        }
    }

    public void parseData() {
        if (stringData.isEmpty()) {
            return;
        }

        Pattern groupPattern = Pattern.compile("\\((.*?)\\)");
        Matcher groupMatcher = groupPattern.matcher(stringData);

        while (groupMatcher.find()) {
            String data = groupMatcher.group(1);
            dataStringList.add(List.of(data.split(", ")));
        }
    }

    private void validateData(Database db) {
        if (!db.containsTable(tableName)) {
            throw new IllegalArgumentException("Таблицы с таким именем нету");
        }

        Table table = db.getTable(tableName);
        List<Column> columns = table.getColumns();
        int minArgs = table.getMinArgs();

        for (var dataCollection : dataStringList) {
            if (dataCollection.size() < minArgs) {
                throw new IllegalArgumentException("Недостаточно аргументов в блоке: " + dataCollection);
            }

            Map<String, Object> data = new HashMap<>();

            for (int i = 0; i < dataCollection.size(); i++) {
                Column col = columns.get(i);
                String typeString = col.getType();
                
                if (dataCollection.get(i).toString().trim().isEmpty()) {
                    throw new IllegalArgumentException("Неверный формат данных в: " + dataCollection);
                }

                if (!TypeFactory.valid(typeString, dataCollection.get(i).toString())) {
                    throw new IllegalArgumentException("Неверный формат данных! Ошибка преобразований: " + dataCollection.get(i).toString() + " в тип " + typeString);
                }

                Object value = TypeFactory.parse(typeString, dataCollection.get(i).toString());

                data.put(col.getName(), value);
            }

            dataList.add(data);
        }

    }
    private void doRequest(Database db) {
        db.getTable(tableName).addData(dataList);
    }
}