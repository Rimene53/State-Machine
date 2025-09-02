package com.example.generic;

import com.google.gson.*;
import java.io.FileReader;
import java.util.*;

public class ConfigLoader {

    private static final Gson gson = new Gson();
    public static List<String> getConfigFiles(String controllerPath) throws Exception {
        JsonObject controller = gson.fromJson(new FileReader(controllerPath), JsonObject.class);
        JsonArray configs = controller.getAsJsonArray("xmlConfigurations");
        List<String> paths = new ArrayList<>();

        for (JsonElement elem : configs) {
            String configFile = elem.getAsJsonObject().get("configFile").getAsString();
            paths.add(configFile);
        }
        return paths;
    }
    public static Map<String, String> loadSingleConfig(String configFilePath) throws Exception {
        JsonObject configObj = gson.fromJson(new FileReader(configFilePath), JsonObject.class);
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : configObj.entrySet()) {
            JsonObject details = entry.getValue().getAsJsonObject();
            result.put("path", details.get("path").getAsString());
            result.put("class", details.get("class").getAsString());
        }
        return result;
    }
}
