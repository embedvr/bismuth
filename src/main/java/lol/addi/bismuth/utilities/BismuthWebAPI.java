package lol.addi.bismuth.utilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class BismuthWebAPI {

    public final String BASE_URL;
    public final String SECRET_KEY;
    public final HttpClient httpClient;

    public BismuthWebAPI(String baseUrl, String secretKey) {
        this.BASE_URL = baseUrl + "/v1/identify/%s";
        this.SECRET_KEY = secretKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    public Optional<BismuthProfile> getProfile(String uuid) throws IOException, InterruptedException {
        String url = String.format(BASE_URL, uuid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-API-Key", SECRET_KEY)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            return Optional.empty();
        }

        String responseBody = response.body();
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        BismuthProfile profile = new BismuthProfile(
                jsonObject.get("username").getAsString()
        );
        return Optional.of(profile);
    }

    public static class BismuthProfile {
        private final String name;

        public BismuthProfile(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
