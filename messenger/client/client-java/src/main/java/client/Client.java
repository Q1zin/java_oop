package client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.Scanner;
import java.util.logging.Logger;

public class Client {
    private final ClientConnection connectionHolder;
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public Client(String host, int port) throws IOException {
        connectionHolder = new ClientConnection(host, port, message -> {
            System.out.println(message);
        });
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Client <host> <port>");
            return;
        }

        try {    
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            client.start();
        } catch (Exception e) {
            logger.warning("Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    private void start() {
            try (Scanner inputScanner = new Scanner(System.in)) {
                while (inputScanner.hasNextLine()) {
                    String input = inputScanner.nextLine();
                    processCommand(input);
                }
            } catch (Exception e) {
                logger.severe("Ошибка при processing, получено " + e.getMessage());
            }
        
    }

    private void processCommand(String input) {
        try {
            JsonElement parsed = JsonParser.parseString(input);

            if (parsed.isJsonPrimitive() && parsed.getAsJsonPrimitive().isString()) {
                parsed = JsonParser.parseString(parsed.getAsString());
            }

            JsonObject command = parsed.getAsJsonObject();

            if (!command.has("type") || !command.has("content")) {
                logger.warning("Нету поля команды 'type' или 'content', получено: " + input);
                return;
            }

            String type = command.get("type").getAsString();
            JsonObject content = command.getAsJsonObject("content");

            switch (type.toUpperCase()) {
                case "LOGIN" -> handleLoginCommand(content);
                case "REGISTER" -> handleRegisterCommand(content);
                case "SEND_MESSAGE" -> handleSendMessageCommand(content);
                case "GET_MESSAGES" -> handleGetMessagesCommand(content);
                case "GET_USERS" -> handleGetUsersCommand(content);
                case "CHECK_LOGIN" -> handleCheckLoginCommand(content);
                case "LOGOUT" -> handleLogoutCommand();
                case "HEARTBEAT" -> handleHeartbeatCommand(content);
                default -> {
                    logger.warning("Неизвестный тип команды: " + type);
                }
            }
        } catch (JsonParseException e) {
            logger.warning("Invalid JSON format: " + e.getMessage() + " - input: " + input);
        } catch (Exception e) {
            logger.severe("Error processing command: " + e.getMessage() + " - input: " + input);
        }
    }

    private void handleLoginCommand(JsonObject content) {
        if (!content.has("email") || !content.has("password")) {
            logger.warning("Missing email or password in LOGIN command");
            return;
        }

        String email = content.get("email").getAsString();
        String password = content.get("password").getAsString();
        connectionHolder.sendLogin(email, password);
    }

    private void handleRegisterCommand(JsonObject content) {
        if (!content.has("name") || !content.has("email") || !content.has("password")) {
            logger.warning("Missing name, email, or password in REGISTER command");
            return;
        }

        String name = content.get("name").getAsString();
        String email = content.get("email").getAsString();
        String password = content.get("password").getAsString();
        connectionHolder.sendRegister(name, email, password);
    }

    private void handleSendMessageCommand(JsonObject content) {
        if (!content.has("hash") || !content.has("to") || !content.has("text") || !content.has("timestamp")) {
            logger.warning("Missing fields in SEND_MESSAGE command");
            return;
        }

        String hash = content.get("hash").getAsString();
        int to = content.get("to").getAsInt();
        String text = content.get("text").getAsString();
        String timestamp = content.get("timestamp").getAsString();
        connectionHolder.sendPrivateMessage(hash, to, text, timestamp);
    }

    private void handleGetMessagesCommand(JsonObject content) {
        if (!content.has("hash") || !content.has("chat_id")) {
            logger.warning("Missing hash or chat_id in GET_MESSAGES command");
            return;
        }

        String hash = content.get("hash").getAsString();
        int chatId = content.get("chat_id").getAsInt();
        connectionHolder.requestMessages(hash, chatId);
    }

    private void handleGetUsersCommand(JsonObject content) {
        if (!content.has("hash")) {
            logger.warning("Missing hash in GET_USERS command");
            return;
        }

        String hash = content.get("hash").getAsString();
        connectionHolder.requestUsers(hash);
    }

    private void handleCheckLoginCommand(JsonObject content) {
        if (!content.has("hash")) {
            logger.warning("Missing hash in CHECK_LOGIN command");
            return;
        }

        String hash = content.get("hash").getAsString();
        connectionHolder.checkLogin(hash);
    }

    private void handleLogoutCommand() {
        connectionHolder.sendLogout();
    }

    private void handleHeartbeatCommand(JsonObject content) {
        if (!content.has("hash")) {
            logger.warning("Missing hash in HEARTBEAT command");
            return;
        }

        String hash = content.get("hash").getAsString();
        connectionHolder.sendHeartbeat(hash);
    }
}