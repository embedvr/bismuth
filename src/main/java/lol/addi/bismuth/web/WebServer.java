package lol.addi.bismuth.web;

import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class WebServer {
    HttpServer server;
    ThreadPoolExecutor executor;
    public WebServer(String apiKey, String address, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(address, port), 0);
        server.createContext("/v1/whitelist", new RequestHandler(apiKey));
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.setExecutor(executor);
        server.start();
        Bukkit.getLogger().info("Bismuth Plugin API started on port 8001");
    }

    public void stop() {
        server.stop(1);
        executor.shutdownNow();
    }
}
