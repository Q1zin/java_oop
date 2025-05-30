package server;

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import server.utils.ResponseBuilder;

public class Server {
    private static int PORT;
    private static String DB_PATH;
    private static boolean xmlMode;

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService heartbeatChecker = Executors.newScheduledThreadPool(2);
    private ServerSocket serverSocket;
    private final Set<Integer> onlineUsers = Collections.synchronizedSet(new HashSet<>());
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public Server() {
        loadConfig();
        initDatabase();
    }

    public void start() {
        startHeartbeatMonitor();

        try {
            serverSocket = new ServerSocket(PORT);
            logger.info("Server started on port " + PORT);

            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected: " + clientSocket.getInetAddress());

                Connection clientConnection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, clientConnection, xmlMode);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException | SQLException e) {
            logger.severe("Server error: " + e.getMessage());
        }
    }

    private void shutdown() {
        try {
            logger.info("Shutting down server...");
            serverSocket.close();
            heartbeatChecker.shutdownNow();
        } catch (IOException e) {
            logger.warning("Shutdown error: " + e.getMessage());
        }
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (FileReader reader = new FileReader("server.properties")) {
            props.load(reader);
            PORT = Integer.parseInt(props.getProperty("port", "4042"));
            DB_PATH = props.getProperty("db_path", "database.db");
            xmlMode = Boolean.parseBoolean(props.getProperty("xml_mode", "false"));
            ResponseBuilder.setXmlMode(xmlMode);
        } catch (IOException e) {
            logger.warning("Could not load server.properties. Using defaults.");
            PORT = 4042;
            DB_PATH = "database.db";
        }
    }

    public boolean isXmlMode() {
        return xmlMode;
    }

    private void initDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            initUserTable(connection);
            initMessagesTable(connection);
        } catch (SQLException e) {
            logger.severe("Database init error: " + e.getMessage());
        }
    }

    private void initUserTable(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    email TEXT UNIQUE,
                    password TEXT,
                    hash TEXT
                )
            """);
        }
    }

    private void initMessagesTable(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender_id INTEGER,
                    receiver_id INTEGER,
                    message TEXT,
                    timestamp TEXT
                )
            """);
        }
    }

    private void startHeartbeatMonitor() {
        heartbeatChecker.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();

            List<ClientHandler> toRemove = new ArrayList<>();
            for (ClientHandler ch : clients) {
                if (now - ch.getLastHeartbeatTime() > 20_000) {
                    ch.forceClose();
                    ch.notifyUserStatusChange(false);
                    toRemove.add(ch);
                }
            }
            clients.removeAll(toRemove);
        }, 10, 10, TimeUnit.SECONDS);
    }

    public void sendMessageToAllClients(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void sendMessageToClient(int userId, String message) {
        for (ClientHandler client : clients) {
            if (client.getUserId() == userId) {
                client.sendMessage(message);
                break;
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public Set<Integer> getOnlineUsers() {
        return onlineUsers;
    }
    
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
