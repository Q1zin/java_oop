package db.newSql;

import mySql.PDO;

import java.io.File;

public class App {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar database.jar --db <database-file> -c <sql-command>");
            return;
        }

        String dbName = "";
        String sqlCommand = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--db") && i + 1 < args.length) {
                dbName = args[i + 1];
            } else if (args[i].equals("-c") && i + 1 < args.length) {
                sqlCommand = args[i + 1];
            }
        }

        if (sqlCommand == null ||
                dbName.isEmpty() && !(sqlCommand.split(" ")[0].equals("INIT") ||
            sqlCommand.split(" ")[0].equals("ERASE"))) {
            System.out.println("Error: Missing --db or -c parameter. \n Usage: java -jar database.jar --db <database-file> -c <sql-command>");
            return;
        }

        File dbFile = new File(dbName.isEmpty() ? "./" : dbName);
        String dbPath = dbFile.getParent();
        String dbFileName = dbFile.getName();
        String dbBaseName = dbFileName.substring(0, dbFileName.lastIndexOf('.'));

        PDO pdo = new PDO(dbPath);

        try {
            pdo.executeSQL(sqlCommand, dbBaseName);

            if (sqlCommand.split(" ")[0].equals("SELECT")) {
                System.out.println(PDO.getResultSql());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


}
