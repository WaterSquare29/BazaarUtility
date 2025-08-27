package com.watersquare.bazaarutility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class Fetchur {

    private static final String SKYCRYPT_API = "https://sky.shiiyu.moe/api/profiles/";

    public static HashMap<String, com.google.gson.JsonElement> getAllProfilesNetWorth(String playerName) {
        HashMap<String, com.google.gson.JsonElement> result = new HashMap<>();
        try {
            // Get UUID from Mojang API
            URL uuidUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            InputStreamReader reader = new InputStreamReader(uuidUrl.openStream());
            JsonObject uuidObj = JsonParser.parseReader(reader).getAsJsonObject();
            String uuid = uuidObj.get("id").getAsString();

            // Fetch SkyCrypt profiles
            URL apiUrl = new URL(SKYCRYPT_API + uuid);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");

            try (InputStreamReader apiReader = new InputStreamReader(connection.getInputStream())) {
                JsonObject json = JsonParser.parseReader(apiReader).getAsJsonObject();
                var profiles = json.getAsJsonArray("profiles");

                for (var elem : profiles) {
                    JsonObject profile = elem.getAsJsonObject();
                    String profileName = profile.get("cute_name").getAsString();
                    JsonObject members = profile.getAsJsonObject("members");
                    JsonObject playerData = members.getAsJsonObject(uuid);

                    com.google.gson.JsonElement netWorth = null;
                    if (playerData.has("networth")) {
                        netWorth = playerData.getAsJsonObject("networth").get("total");
                    }
                    result.put(profileName, netWorth);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
