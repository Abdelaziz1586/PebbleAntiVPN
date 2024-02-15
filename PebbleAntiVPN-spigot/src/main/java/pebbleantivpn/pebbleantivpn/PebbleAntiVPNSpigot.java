package pebbleantivpn.pebbleantivpn;

import handlers.DataHandler;
import handlers.LogHandler;
import org.bukkit.plugin.java.JavaPlugin;
import pebbleantivpn.pebbleantivpn.handlers.AlertHandler;
import pebbleantivpn.pebbleantivpn.listeners.PreLogin;

import java.util.Objects;

public final class PebbleAntiVPNSpigot extends JavaPlugin {

    @Override
    public void onEnable() {
        new DataHandler(getLogger());
        new AlertHandler(this);

        new LogHandler(null);

        getServer().getPluginManager().registerEvents(new PreLogin(), this);

        Objects.requireNonNull(getCommand("pebbleantivpn")).setExecutor(new Command(this));
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§6PebbleAntiVPN §cHas Been Unloaded.");
    }

}
