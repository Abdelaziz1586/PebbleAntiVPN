package pebbleantivpn.pebbleantivpnspigot;

import handlers.DataHandler;
import handlers.LogHandler;
import org.bukkit.plugin.java.JavaPlugin;
import pebbleantivpn.pebbleantivpnspigot.handlers.AlertHandler;
import pebbleantivpn.pebbleantivpnspigot.listeners.PlayerQuit;
import pebbleantivpn.pebbleantivpnspigot.listeners.PreLogin;

import java.util.Objects;

public final class PebbleAntiVPNSpigot extends JavaPlugin {

    @Override
    public void onEnable() {
        new DataHandler(getLogger());
        new AlertHandler(this);

        new LogHandler(null);

        getServer().getPluginManager().registerEvents(new PreLogin(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(), this);

        Objects.requireNonNull(getCommand("pebbleantivpn")).setExecutor(new Command(this));
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§6PebbleAntiVPN §cHas Been Unloaded.");
    }

}
