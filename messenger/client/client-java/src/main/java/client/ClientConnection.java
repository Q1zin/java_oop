package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import client.utils.XMLConverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

public class ClientConnection {
    private Socket socket;
    private Scanner serverInput;
    private PrintWriter serverOutput;
    private boolean xmlMode = false; // Сделал просто полем, потому что особо не вижу смысла менять на клиенте.
    // Глобально это бесполезно, но если сервер решит в какой-то момент переключиться на XML, то просто setXmlMode(true) и всё.

    public ClientConnection(String host, int port, Consumer<String> messageHandler) throws IOException {
        this.socket = new Socket(host, port);
        this.serverInput = new Scanner(socket.getInputStream());
        this.serverOutput = new PrintWriter(socket.getOutputStream(), true);

        Thread reader = new Thread(() -> {
            while (serverInput.hasNextLine()) {
                String line = serverInput.nextLine();
                String responce = isXmlMode() ? XMLConverter.parseXmlToJson(line).toString() : line;

                messageHandler.accept(responce);
            }
        });

        reader.setDaemon(true);
        reader.start();
    }

    public void sendLogin(String email, String password) {
        JsonObject content = new JsonObject();
        content.addProperty("email", email);
        content.addProperty("password", password);
        sendJson("LOGIN", content);
    }

    public void sendRegister(String name, String email, String password) {
        JsonObject content = new JsonObject();
        content.addProperty("name", name);
        content.addProperty("email", email);
        content.addProperty("password", password);
        sendJson("REGISTER", content);
    }

    public void sendPrivateMessage(String hash, int toUserId, String text, String timestamp) {
        JsonObject content = new JsonObject();
        content.addProperty("hash", hash);
        content.addProperty("to", toUserId);
        content.addProperty("text", text);
        content.addProperty("timestamp", timestamp);
        sendJson("SEND_MESSAGE", content);
    }

    public void checkLogin(String hash) {
        JsonObject content = new JsonObject();
        content.addProperty("hash", hash);
        sendJson("CHECK_LOGIN", content);
    }

    public void sendHeartbeat(String hash) {
        JsonObject content = new JsonObject();
        content.addProperty("hash", hash);
        sendJson("HEARTBEAT", content);
    }

    public void newUser(int userId, String name) {
        JsonObject content = new JsonObject();
        content.addProperty("user_id", userId);
        content.addProperty("name", name);
        sendJson("NEW_USER", content);
    }

    public void requestMessages(String hash, int chat_id) {
        JsonObject content = new JsonObject();
        content.addProperty("hash", hash);
        content.addProperty("chat_id", chat_id);
        sendJson("GET_MESSAGES", content);
    }

    public void requestUsers(String hash) {
        JsonObject content = new JsonObject();
        content.addProperty("hash", hash);
        sendJson("GET_USERS", content);
    }

    public void sendLogout() {
        sendJson("LOGOUT", new JsonObject());
    }

    private void sendJson(String type, JsonObject content) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", type);
        msg.add("content", content);

        Gson gson = new Gson();
        serverOutput.println(isXmlMode() ? XMLConverter.toXml(msg) : gson.toJson(msg));
    }

    public boolean isXmlMode() {
        return xmlMode;
    }
}