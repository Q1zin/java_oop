package server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static server.utils.ErrorCodes.*;

public class ResponseBuilder {
    private static boolean xmlMode = false;
    
    public static void setXmlMode(boolean mode) {
        xmlMode = mode;
    }

    public static boolean isXmlMode() {
        return xmlMode;
    }

    public static String build(String type, String status, JsonObject content) {
        JsonObject response = new JsonObject();
        response.addProperty("type", type);
        response.addProperty("status", status);
        response.add("content", content);

        return isXmlMode() ? XMLConverter.toXml(response) : response.toString();
    }

    private static JsonObject errorContent(int code, String message) {
        JsonObject data = new JsonObject();
        data.addProperty("code", code);
        data.addProperty("message", message);
        return data;
    }

    public static String buildLoginResponse(int userId, String newHash) {
        JsonObject data = new JsonObject();
        data.addProperty("id", userId);
        data.addProperty("hash", newHash);
        return build("LOGIN", "STATE_OK", data);
    }

    public static String buildLoginErrorResponse(int code, String message) {
        return build("LOGIN", "STATE_ERR", errorContent(code, message));
    }

    public static String buildRegisterResponse(int userId, String hash) {
        JsonObject data = new JsonObject();
        data.addProperty("id", userId);
        data.addProperty("hash", hash);
        return build("REGISTER", "STATE_OK", data);
    }

    public static String buildRegisterErrorResponse(int code, String message) {
        return build("REGISTER", "STATE_ERR", errorContent(code, message));
    }

    public static String buildGetUsersResponse(JsonArray users) {
        JsonObject data = new JsonObject();
        data.add("users", users);
        return build("GET_USERS", "STATE_OK", data);
    }

    public static String buildGetUsersErrorResponse(int code, String message) {
        return build("GET_USERS", "STATE_ERR", errorContent(code, message));
    }

    public static String buildSendMessageErrorResponse(int code, String message) {
        return build("SEND_MESSAGE", "STATE_ERR", errorContent(code, message));
    }

    public static String buildNewMessageBroadcast(JsonObject msg) {
        JsonObject content = new JsonObject();
        content.add("message", msg);
        return build("NEW_MESSAGE", "STATE_OK", content);
    }

    public static String buildGetMessagesResponse(JsonArray messages) {
        JsonObject data = new JsonObject();
        data.add("messages", messages);
        return build("GET_MESSAGES", "STATE_OK", data);
    }

    public static String buildGetMessagesErrorResponse(int code, String message) {
        return build("GET_MESSAGES", "STATE_ERR", errorContent(code, message));
    }

    public static String buildCheckLoginResponse(int userId) {
        JsonObject data = new JsonObject();
        data.addProperty("id", userId);
        return build("CHECK_LOGIN", "STATE_OK", data);
    }

    public static String buildCheckLoginErrorResponse(int code, String message) {
        return build("CHECK_LOGIN", "STATE_ERR", errorContent(code, message));
    }

    public static String buildLogoutResponse() {
        JsonObject data = new JsonObject();
        data.addProperty("message", "Вы вышли из системы.");
        return build("LOGOUT", "STATE_OK", data);
    }

    public static String buildLogoutErrorResponse(int code, String message) {
        return build("LOGOUT", "STATE_ERR", errorContent(code, message));
    }

    public static String buildHeartbeatErrorResponse(int code, String message) {
        return build("HEARTBEAT", "STATE_ERR", errorContent(code, message));
    }

    public static String buildUserStatusChanged(int userId, boolean online) {
        JsonObject data = new JsonObject();
        data.addProperty("id", userId);
        data.addProperty("online", online);
        return build("USER_STATUS_CHANGED", "STATE_OK", data);
    }

    public static String buildNewUserNotification(int userId, String name) {
        JsonObject data = new JsonObject();
        data.addProperty("user_id", userId);
        data.addProperty("name", name);
        return build("NEW_USER", "STATE_OK", data);
    }

    public static String buildUnknownRequestTypeError(String type) {
        return build("ERROR", "STATE_ERR", errorContent(REQUEST_UNKNOWN_TYPE, "Неизвестный тип запроса: " + type));
    }

    public static String buildInvalidJsonError(String errorDetail) {
        return build("ERROR", "STATE_ERR", errorContent(REQUEST_INVALID_JSON, "Невалидный формат JSON: " + errorDetail));
    }
}