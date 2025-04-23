package org.openjfx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class TableRowData {
    private final SimpleStringProperty name;
    private final SimpleStringProperty type;
    private final SimpleBooleanProperty notNull;
    private final SimpleBooleanProperty unique;

    public TableRowData(String name, String type, boolean notNull, boolean unique) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.notNull = new SimpleBooleanProperty(notNull);
        this.unique = new SimpleBooleanProperty(unique);
    }

    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public SimpleStringProperty nameProperty() {
        return name;
    }
    
    public String getType() {
        return type.get();
    }
    public void setType(String type) {
        this.type.set(type);
    }
    public SimpleStringProperty typeProperty() {
        return type;
    }
    
    public boolean isNotNull() {
        return notNull.get();
    }
    public void setNotNull(boolean notNull) {
        this.notNull.set(notNull);
    }
    public SimpleBooleanProperty notNullProperty() {
        return notNull;
    }
    
    public boolean isUnique() {
        return unique.get();
    }
    public void setUnique(boolean unique) {
        this.unique.set(unique);
    }
    public SimpleBooleanProperty uniqueProperty() {
        return unique;
    }
}