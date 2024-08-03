package lol.addi.bismuth.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lol.addi.bismuth.Bismuth;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class RequestHandler implements HttpHandler {
    String apiKey;
    public RequestHandler(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        if(!method.equals("POST") && !method.equals("DELETE")) {
            OutputStream outStream = httpExchange.getResponseBody();
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"error\":\"Method Not Allowed.\"}");

            httpExchange.sendResponseHeaders(405, jsonBuilder.length());
            outStream.write(jsonBuilder.toString().getBytes());
            outStream.flush();
            outStream.close();
        }

        if(!httpExchange.getRequestHeaders().containsKey("X-API-Key")) {
            Bukkit.getLogger().warning("Unauthorized request from " + httpExchange.getRemoteAddress());
            OutputStream outStream = httpExchange.getResponseBody();
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"error\":\"Unauthorized.\"}");

            httpExchange.sendResponseHeaders(401, jsonBuilder.length());
            outStream.write(jsonBuilder.toString().getBytes());
            outStream.flush();
            outStream.close();
        }
        if(!httpExchange.getRequestHeaders().getFirst("X-API-Key").equals(apiKey)) {
            Bukkit.getLogger().warning("Unauthorized request from " + httpExchange.getRemoteAddress());
            OutputStream outStream = httpExchange.getResponseBody();
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"error\":\"Unauthorized.\"}");

            httpExchange.sendResponseHeaders(401, jsonBuilder.length());
            outStream.write(jsonBuilder.toString().getBytes());
            outStream.flush();
            outStream.close();
        }

        InputStream inStream = httpExchange.getRequestBody();
        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseReader(new InputStreamReader(inStream)).getAsJsonObject();
            jsonObject.remove("error");
        } catch (Exception e) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("error", "Invalid JSON");
        }

        if(jsonObject.has("error")) {
            OutputStream outStream = httpExchange.getResponseBody();
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"error\":\"").append(jsonObject.get("error").getAsString()).append("\"}");

            httpExchange.sendResponseHeaders(400, jsonBuilder.length());
            outStream.write(jsonBuilder.toString().getBytes());
            outStream.flush();
            outStream.close();
        }

        if(!jsonObject.has("username")) {
            OutputStream outStream = httpExchange.getResponseBody();
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"error\":\"Missing username.\"}");

            httpExchange.sendResponseHeaders(400, jsonBuilder.length());
            outStream.write(jsonBuilder.toString().getBytes());
            outStream.flush();
            outStream.close();
        }

        String username = jsonObject.get("username").getAsString();
        boolean success;
        try {
            success = Bukkit.getScheduler().callSyncMethod(Bismuth.getPlugin(), new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    ConsoleCommandSender console = Bukkit.getConsoleSender();
                    if(method.equals("POST")) {
                        return Bukkit.dispatchCommand(console, "whitelist add " + username);
                    } else {
                        return Bukkit.dispatchCommand(console, "whitelist remove " + username);
                    }
                }
            }).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        OutputStream outStream = httpExchange.getResponseBody();
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"success\":\"").append(success).append("\"}");

        httpExchange.sendResponseHeaders(200, jsonBuilder.length());
        outStream.write(jsonBuilder.toString().getBytes());
        outStream.flush();
        outStream.close();
    }
}
