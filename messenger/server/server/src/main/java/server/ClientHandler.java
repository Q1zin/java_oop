package server;

import com.google.gson.*;

import server.utils.ResponseBuilder;
import server.utils.XMLConverter;

import static server.utils.ErrorCodes.*;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    public final Socket clientSocket;
    private final Server server;
    private final Connection db;
    private final boolean xmlMode;
    private final PrintWriter out;
    private final Scanner in;
    private int userId = -1;
    private String userHash = "";
    private volatile long lastHeartbeatTime;
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket socket, Server server, Connection db, boolean xmlMode) throws IOException {
        this.clientSocket = socket;
        this.server = server;
        this.db = db;
        this.xmlMode = xmlMode;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new Scanner(socket.getInputStream());
        updateLastHeartbeatTime();
    }

    public synchronized void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try (Scanner input = in; PrintWriter output = out) {
            while (input.hasNextLine()) {
                String line = input.nextLine();
                doRequest(line);
            }
        } catch (Exception e) {
            logger.warning("Exception in client handler: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void doRequest(String message) {
        try {
            JsonObject obj = isXmlMode() ? XMLConverter.parseXmlToJson(message) : JsonParser.parseString(message).getAsJsonObject();

            String type = obj.get("type").getAsString();
            JsonElement contentElement = obj.get("content");

            switch (type.toUpperCase()) {
                case "LOGIN" -> handleLogin(contentElement.getAsJsonObject());
                case "REGISTER" -> handleRegister(contentElement.getAsJsonObject());
                case "GET_USERS" -> handleGetUsers(contentElement.getAsJsonObject());
                case "SEND_MESSAGE" -> handleSendMessage(contentElement.getAsJsonObject());
                case "GET_MESSAGES" -> handleGetMessages(contentElement.getAsJsonObject());
                case "CHECK_LOGIN" -> handleCheckLogin(contentElement.getAsJsonObject());
                case "LOGOUT" -> handleLogout();
                case "HEARTBEAT" -> handleHeartbeat(contentElement.getAsJsonObject());
                default -> sendMessage(ResponseBuilder.buildUnknownRequestTypeError(type));
            }
        } catch (Exception e) {
            sendMessage(ResponseBuilder.buildInvalidJsonError(e.getMessage()));
        }
    }

    private void handleLogin(JsonObject content) {
        if (!content.has("email") || !content.has("password")) {
            sendMessage(ResponseBuilder.buildLoginErrorResponse(GEN_MISSING_FIELDS, "Отсутствуют обязательные поля"));
            return;
        }

        String email = content.get("email").getAsString();
        String password = content.get("password").getAsString();

        try (PreparedStatement stmt = db.prepareStatement("SELECT id, password FROM users WHERE email = ?")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                sendMessage(ResponseBuilder.buildLoginErrorResponse(LOGIN_USER_NOT_FOUND, "Такого пользователя не существует"));
                return;
            }
            
            String storedPassword = rs.getString("password");
            if (!PasswordManager.checkPassword(password, storedPassword)) {
                sendMessage(ResponseBuilder.buildLoginErrorResponse(LOGIN_INVALID_PASSWORD, "Неверный пароль"));
                return;
            }

            userId = rs.getInt("id");
            String newHash = generateRandomHash();

            try (PreparedStatement update = db.prepareStatement("UPDATE users SET hash = ? WHERE id = ?")) {
                update.setString(1, newHash);
                update.setInt(2, userId);
                update.executeUpdate();
            }

            userHash = newHash;
            notifyUserStatusChange(true);

            sendMessage(ResponseBuilder.buildLoginResponse(userId, newHash));
        } catch (SQLException e) {
            sendMessage(ResponseBuilder.buildLoginErrorResponse(GEN_UNEXPECTED_ERROR, "Неожиданная ошибка: " + e.getMessage()));
        }
    }

    private void handleRegister(JsonObject content) {
        if (!content.has("name") || !content.has("email") || !content.has("password")) {
            sendMessage(ResponseBuilder.buildRegisterErrorResponse(GEN_MISSING_FIELDS, "Отсутствуют обязательные поля"));
            return;
        }

        String name = content.get("name").getAsString();
        String email = content.get("email").getAsString();
        String password = content.get("password").getAsString();
        String hash = generateRandomHash();

        try (PreparedStatement check = db.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            check.setString(1, email);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                sendMessage(ResponseBuilder.buildRegisterErrorResponse(REGISTER_USER_EXISTS, "Пользователь с таким email уже существует"));
                return;
            }
        } catch (SQLException e) {
            sendMessage(ResponseBuilder.buildRegisterErrorResponse(REGISTER_SQL_ERROR, "Ошибка при проверке email: " + e.getMessage()));
            return;
        }

        try (PreparedStatement insert = db.prepareStatement(
                "INSERT INTO users (name, email, password, hash) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            insert.setString(1, name);
            insert.setString(2, email);
            insert.setString(3, PasswordManager.hashPassword(password));
            insert.setString(4, hash);

            int inserted = insert.executeUpdate();

            if (inserted == 1) {
                ResultSet keys = insert.getGeneratedKeys();
                if (keys.next()) {
                    userId = keys.getInt(1);
                    userHash = hash;
                    sendMessage(ResponseBuilder.buildRegisterResponse(userId, hash));
                    notifyUserAddFrind(userId, name);
                }
            } else {
                sendMessage(ResponseBuilder.buildRegisterErrorResponse(REGISTER_INSERT_FAILED, "Не удалось создать пользователя"));
            }
        } catch (SQLException e) {
            sendMessage(ResponseBuilder.buildRegisterErrorResponse(REGISTER_SQL_ERROR, "Ошибка при создании пользователя: " + e.getMessage()));
        }
    }

    private void handleGetUsers(JsonObject content) {
        if (!content.has("hash")) {
            sendMessage(ResponseBuilder.buildGetUsersErrorResponse(GEN_MISSING_FIELDS, "Отсутствует поле hash"));
            return;
        }

        String hash = content.get("hash").getAsString();

        try (PreparedStatement stmt = db.prepareStatement("SELECT id FROM users WHERE hash = ?")) {
            stmt.setString(1, hash);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                sendMessage(ResponseBuilder.buildGetUsersErrorResponse(GET_USERS_INVALID_HASH, "Неверный хэш"));
                return;
            }

            int userId = rs.getInt("id");
            JsonArray users = new JsonArray();
            Set<Integer> onlineSet = server.getOnlineUsers();

            try (PreparedStatement listStmt = db.prepareStatement("SELECT id, name FROM users WHERE id != ?")) {
                listStmt.setInt(1, userId);
                ResultSet usersRs = listStmt.executeQuery();

                while (usersRs.next()) {
                    JsonObject user = new JsonObject();
                    int id = usersRs.getInt("id");
                    user.addProperty("id", id);
                    user.addProperty("name", usersRs.getString("name"));
                    user.addProperty("online", onlineSet.contains(id));
                    users.add(user);
                }
            }

            sendMessage(ResponseBuilder.buildGetUsersResponse(users));
        } catch (SQLException e) {
            sendMessage(ResponseBuilder.buildGetUsersErrorResponse(GET_USERS_SQL_ERROR, "Ошибка базы данных: " + e.getMessage()));
        }
    }

    private void handleSendMessage(JsonObject content) {
        updateLastHeartbeatTime();
        if (!content.has("hash") || !content.has("text") || !content.has("to") || !content.has("timestamp")) {
            sendMessage(ResponseBuilder.buildSendMessageErrorResponse(SEND_MESSAGE_MISSING_FIELDS, "Отсутствуют обязательные поля"));
            return;
        }

        String hash = content.get("hash").getAsString();
        String text = content.get("text").getAsString();
        int receiverId = content.get("to").getAsInt();
        String timestamp = content.get("timestamp").getAsString();

        try (PreparedStatement auth = db.prepareStatement("SELECT id FROM users WHERE hash = ?")) {
            auth.setString(1, hash);
            ResultSet rs = auth.executeQuery();

            if (!rs.next()) {
                sendMessage(ResponseBuilder.buildSendMessageErrorResponse(SEND_MESSAGE_INVALID_HASH, "Неверный хэш"));
                return;
            }

            int senderId = rs.getInt("id");

            try (PreparedStatement insert = db.prepareStatement(
                    "INSERT INTO messages (sender_id, receiver_id, message, timestamp) VALUES (?, ?, ?, ?)")) {
                insert.setInt(1, senderId);
                insert.setInt(2, receiverId);
                insert.setString(3, text);
                insert.setString(4, timestamp);
                insert.executeUpdate();
            }

            JsonObject msg = new JsonObject();
            msg.addProperty("sender_id", senderId);
            msg.addProperty("receiver_id", receiverId);
            msg.addProperty("message", text);
            msg.addProperty("timestamp", timestamp);

            try (PreparedStatement nameStmt = db.prepareStatement("SELECT name FROM users WHERE id = ?")) {
                nameStmt.setInt(1, senderId);
                ResultSet nameRs = nameStmt.executeQuery();
                if (nameRs.next()) {
                    msg.addProperty("name", nameRs.getString("name"));
                }
            }

            String json = ResponseBuilder.buildNewMessageBroadcast(msg);

            if (receiverId == 0) {
                server.sendMessageToAllClients(json);
            } else {
                server.sendMessageToClient(receiverId, json);
                sendMessage(json);
            }
        } catch (SQLException e) {
            sendMessage(ResponseBuilder.buildSendMessageErrorResponse(SEND_MESSAGE_SQL_ERROR, "Ошибка базы данных: " + e.getMessage()));
        }
    }

    private void handleGetMessages(JsonObject content) {
        updateLastHeartbeatTime();
        if (!content.has("hash") || !content.has("chat_id")) {
            sendMessage(ResponseBuilder.buildGetMessagesErrorResponse(GET_MESSAGES_MISSING_FIELDS, "Отсутствуют обязательные поля"));
            return;
        }

        String hash = content.get("hash").getAsString();
        int chatId = content.get("chat_id").getAsInt();

        try (PreparedStatement auth = db.prepareStatement("SELECT id FROM users WHERE hash = ?")) {
            auth.setString(1, hash);
            ResultSet rs = auth.executeQuery();

            if (!rs.next()) {
                sendMessage(ResponseBuilder.buildGetMessagesErrorResponse(GET_MESSAGES_INVALID_HASH, "Неверный хэш"));
                return;
            }

            int userId = rs.getInt("id");

            PreparedStatement stmt;
            if (chatId == 0) {
                stmt = db.prepareStatement(
                    "SELECT m.sender_id, m.receiver_id, m.message, m.timestamp, u.name " +
                    "FROM messages m JOIN users u ON u.id = m.sender_id " +
                    "WHERE m.receiver_id = 0 ORDER BY m.timestamp"
                );
            } else {
                stmt = db.prepareStatement(
                    "SELECT m.sender_id, m.receiver_id, m.message, m.timestamp, u.name " +
                    "FROM messages m JOIN users u ON u.id = m.sender_id " +
                    "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) " +
                    "ORDER BY m.timestamp"
                );
                stmt.setInt(1, userId);
                stmt.setInt(2, chatId);
                stmt.setInt(3, chatId);
                stmt.setInt(4, userId);
            }

            ResultSet rsMsgs = stmt.executeQuery();
            JsonArray messages = new JsonArray();

            while (rsMsgs.next()) {
                JsonObject msg = new JsonObject();
                int senderId = rsMsgs.getInt("sender_id");
                msg.addProperty("sender_id", senderId);
                msg.addProperty("name", senderId == userId ? "Вы" : rsMsgs.getString("name"));
                msg.addProperty("message", rsMsgs.getString("message"));
                msg.addProperty("timestamp", rsMsgs.getString("timestamp"));
                messages.add(msg);
            }

            sendMessage(ResponseBuilder.buildGetMessagesResponse(messages));
        } catch (SQLException e) {
            sendMessage(ResponseBuilder.buildGetMessagesErrorResponse(GET_MESSAGES_SQL_ERROR, "Ошибка базы данных: " + e.getMessage()));
        }
    }

    private void handleCheckLogin(JsonObject content) {
        updateLastHeartbeatTime();
        if (!content.has("hash")) {
            sendMessage(ResponseBuilder.buildCheckLoginErrorResponse(CHECK_LOGIN_MISSING_FIELDS, "Отсутствует поле hash"));
            return;
        }

        String hash = content.get("hash").getAsString();

        try (PreparedStatement stmt = db.prepareStatement("SELECT id FROM users WHERE hash = ?")) {
            stmt.setString(1, hash);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id");
                notifyUserStatusChange(true);
                sendMessage(ResponseBuilder.buildCheckLoginResponse(userId));
            } else {
                sendMessage(ResponseBuilder.buildCheckLoginErrorResponse(CHECK_LOGIN_INVALID_HASH, "Неверный хэш"));
            }
        } catch (SQLException e) {
            sendMessage(ResponseBuilder.buildCheckLoginErrorResponse(CHECK_LOGIN_SQL_ERROR, "Ошибка базы данных: " + e.getMessage()));
        }
    }

    private void handleLogout() {
        notifyUserStatusChange(false);

        try (PreparedStatement stmt = db.prepareStatement("UPDATE users SET hash = NULL WHERE id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            sendMessage(ResponseBuilder.buildLogoutErrorResponse(LOGOUT_SQL_ERROR, "Ошибка базы данных: " + e.getMessage()));
            return;
        }

        userId = -1;
        sendMessage(ResponseBuilder.buildLogoutResponse());
}

    private void handleHeartbeat(JsonObject content) {
        updateLastHeartbeatTime();

        if (!content.has("hash")) {
            if (userId != -1) {
                notifyUserStatusChange(false);
                userId = -1;
            }
            return;
        }

        String hash = content.get("hash").getAsString();

        if (hash == null || hash.isEmpty()) {
            if (userId != -1) {
                notifyUserStatusChange(false);
                userId = -1;
            }
            return;
        }

        try (PreparedStatement stmt = db.prepareStatement("SELECT id FROM users WHERE hash = ?")) {
            stmt.setString(1, hash);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int dbUserId = rs.getInt("id");

                if (userId != dbUserId || !hash.equals(userHash)) {
                    notifyUserStatusChange(false);
                }

                userId = dbUserId;
                userHash = hash;
                notifyUserStatusChange(true);
            } else {
                notifyUserStatusChange(false);
                userId = -1;
                userHash = "";
                sendMessage(ResponseBuilder.buildHeartbeatErrorResponse(HEARTBEAT_INVALID_HASH, "Неверный хэш. Требуется повторная авторизация."));
            }
        } catch (SQLException e) {
            sendMessage(ResponseBuilder.buildHeartbeatErrorResponse(HEARTBEAT_SQL_ERROR, "Ошибка базы данных: " + e.getMessage()));
        }
    }

    private void updateLastHeartbeatTime() {
        lastHeartbeatTime = System.currentTimeMillis();
    }

    public long getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public int getUserId() {
        return userId;
    }

    public void notifyUserStatusChange(boolean online) {
        if (userId == -1) return;

        boolean wasOnline = server.getOnlineUsers().contains(userId);
        boolean nowOnline = online;

        if (wasOnline == nowOnline) return;

        if (nowOnline) {
            server.getOnlineUsers().add(userId);
        } else {
            server.getOnlineUsers().remove(userId);
        }

        String json = ResponseBuilder.buildUserStatusChanged(userId, nowOnline);
        server.sendMessageToAllClients(json);
    }

    private void notifyUserAddFrind(int id, String name) {
        String json = ResponseBuilder.buildNewUserNotification(id, name);
        server.sendMessageToAllClients(json);
    }

    private String generateRandomHash() {
        return java.util.UUID.randomUUID().toString();
    }

    private boolean isXmlMode() {
        return xmlMode;
    }

    private void closeConnection() {
        try {
            if (userId != -1) {
                notifyUserStatusChange(false);
                logger.info("User ID " + userId + " disconnected.");
            }
        } catch (Exception e) {
            logger.warning("Error during disconnection handling: " + e.getMessage());
        } finally {
            try {
                server.removeClient(this);
                clientSocket.close();
            } catch (IOException e) {
                logger.warning("Error closing socket: " + e.getMessage());
            }
        }
    }

    public void forceClose() {
        logger.info("Force closing connection for user " + userId);
        closeConnection();
    }
} 