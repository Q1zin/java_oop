package mySql.dataBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Table {
    private String name;
    private List<Column> columns = new ArrayList<>();
    private List<Map<String, Object>> data = new ArrayList<>();

    public Table() {}

    public Table(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public void addData(List<Map<String, Object>> data) {
        this.data.addAll(data);
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public void insertData(Map<String, Object> rowData) {
        data.add(rowData);
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public boolean hasColumn(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            return false;
        }

        return columns.stream().anyMatch(column -> columnName.equals(column.getName()));
    }

    @JsonIgnore
    public int getMinArgs() {
        int minColumns = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getFlags().contains("not-null")) {
                minColumns = i;
            }
        }

        return minColumns == -1 ? 0 : minColumns + 1;
    }

    @JsonIgnore
    public List<Map<String, Object>> getWhereData(List<String> conditionList) {
        List<Map<String, Object>> newData = new ArrayList<>();

        // пока хз, как быстрее всего стравнивать значения, но пусть пока будет так
        for (Map<String, Object> row : data) {
            boolean flag = true;
            for (String condition : conditionList) {
                boolean result = checkCondition(row, condition);
                if (!result) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                Map<String, Object> rowCopy = new HashMap<>(row);
                newData.add(rowCopy);
            }
        }

        return newData;
    }

    private boolean checkCondition(Map<String, Object> row, String condition) {
        Pattern pattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)(=)(.+)");
        Matcher matcher = pattern.matcher(condition);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Некорректное условие: " + condition);
        }

        Object fieldValue = row.get(matcher.group(1));
        String operator = matcher.group(2);
        String valueStr = matcher.group(3);

        return compareValues(fieldValue, operator, valueStr);
    }

    private boolean compareValues(Object fieldValue, String operator, String valueStr) {
        if (fieldValue == null) {
            return false;
        }

        if (!operator.equals("=")) {
            throw new IllegalArgumentException("Не реализована такая операция сравнения!");
        }

        return Objects.equals(fieldValue.toString(), valueStr);
    }

    public void restructureData() {
        List<Map<String, Object>> newData = new ArrayList<>();

        for (Map<String, Object> row : data) {
            Map<String, Object> newRow = new HashMap<>();
            for (Column column : columns) {
                String columnName = column.getName();
                newRow.put(columnName, row.getOrDefault(columnName, null));
            }
            newData.add(newRow);
        }

        data = newData;
    }
}
