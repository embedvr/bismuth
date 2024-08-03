package lol.addi.bismuth;

import lol.addi.bismuth.commands.Identify;
import lol.addi.bismuth.web.WebServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Bismuth extends JavaPlugin {
    WebServer webServer;

    @Override
    public void onEnable() {


        saveDefaultConfig();

        String apiKey = getConfig().getString("secretKey");
        String address = getConfig().getString("address");
        int port = getConfig().getInt("port");

        // Plugin startup logic
        Bukkit.getLogger().info("Initializing web server...");
        try {
            webServer = new WebServer(apiKey, address, port);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to start web server!");
            e.printStackTrace();
        }

        getCommand("identify").setExecutor(new Identify(this));
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Cleaning up...");
        webServer.stop();
    }

    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("Bismuth");
    }
}
