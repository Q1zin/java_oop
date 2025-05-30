package client.utils;

import com.google.gson.*;

public class ResponseParser {
    public static ParsedResponse parse(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String type = obj.get("type").getAsString();
            JsonObject content = obj.has("content") && obj.get("content").isJsonObject()
                    ? obj.getAsJsonObject("content")
                    : new JsonObject();

            return new ParsedResponse(type, content);
        } catch (Exception e) {
            System.err.println("Невозможно распарсить ответ: " + e.getMessage() + " input: " + json);
            return null;
        }
    }

    public record ParsedResponse(String type, JsonObject content) {}
}
