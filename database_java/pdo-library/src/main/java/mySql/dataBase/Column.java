package mySql.dataBase;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Column {
    private final String name;
    private final String type;
    private final List<String> flags;

    @JsonCreator
    public Column(
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("flags") List<String> flags
    ) {
        this.name = name;
        this.type = type;
        this.flags = flags;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getFlags() {
        return flags;
    }
}
