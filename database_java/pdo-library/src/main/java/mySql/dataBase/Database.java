package mySql.dataBase;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Database {
    private String name;
    private final Map<String, Table> tables = new HashMap<>();

    public Database() {}

    public Database(String nameDB) {
        name = nameDB;
    }

    public void addTable(Table table) {
        tables.put(table.getName(), table);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Table getTable(String name) {
        return tables.get(name);
    }

    @JsonIgnore
    public Set<String> getSetTables() {
        return tables.keySet();
    }

    public boolean containsTable(String tableName) {
        return tables.containsKey(tableName);
    }

    public void rmTable(String nameTable) {
        tables.remove(nameTable);
    }

    public Map<String, Table> getTables() {
        return tables;
    }
}

