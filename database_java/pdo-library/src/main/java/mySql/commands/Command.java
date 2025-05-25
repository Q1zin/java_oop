package mySql.commands;

import mySql.dataBase.Database;

public interface Command {
    void execute(Database db);
}