package lol.addi.bismuth.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class MojangAPI {

    public static final String BASE_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    public final HttpClient httpClient;
    public final Gson gson;

    public MojangAPI() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public Optional<MinecraftProfile> getProfile(String username) throws IOException, InterruptedException {
        String url = String.format(BASE_URL, username);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            return Optional.empty();
        }

        String responseBody = response.body();
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        MinecraftProfile profile = new MinecraftProfile(
                jsonObject.get("id").getAsString(),
                jsonObject.get("name").getAsString()
        );
        return Optional.of(profile);
    }

    public static class MinecraftProfile {
        private final String id;
        private final String name;

        public MinecraftProfile(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getUUID() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
