package lol.addi.bismuth.commands;

import lol.addi.bismuth.Bismuth;
import lol.addi.bismuth.utilities.BismuthWebAPI;
import lol.addi.bismuth.utilities.MojangAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.Optional;

public class Identify implements CommandExecutor {
    private final Bismuth plugin;
    private final BismuthWebAPI bismuthApi;
    private final MojangAPI mojangApi;

    public Identify(Bismuth plugin) {
        this.plugin = plugin;
        this.bismuthApi = new BismuthWebAPI(
                plugin.getConfig().getString("externalAPI"),
                plugin.getConfig().getString("secretKey")
        );
        this.mojangApi = new MojangAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bismuth.identify")) {
            if(!sender.isOp()) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
        }

        String playerName = args[0];
        Optional<MojangAPI.MinecraftProfile> mojangProfile;
        try {
            mojangProfile = mojangApi.getProfile(playerName);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(mojangProfile.isEmpty()) {
            sender.sendMessage("§f[§5Bismuth§f] §cThat player doesn't exist.");
            return true;
        }

        Optional<BismuthWebAPI.BismuthProfile> twitchHandle;
        try {
            twitchHandle = bismuthApi.getProfile(mojangProfile.get().getUUID());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(twitchHandle.isEmpty()) {
            sender.sendMessage("§f[§5Bismuth§f] §cThere was an error getting the Twitch username.");
            return true;
        }

        sender.sendMessage("§f[§5Bismuth§f] §aThe Twitch username for §f" + playerName + "§a is §f" + twitchHandle.get().getName());

        return true;
    }
}
