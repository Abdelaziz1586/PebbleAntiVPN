package pebbleantivpn.pebbleantivpn;

import handlers.DataHandler;
import org.bukkit.plugin.java.JavaPlugin;
import pebbleantivpn.pebbleantivpn.handlers.AlertHandler;
import pebbleantivpn.pebbleantivpn.listeners.PreLogin;

public final class PebbleAntiVPNSpigot extends JavaPlugin {

    @Override
    public void onEnable() {
        new DataHandler(getLogger());
        new AlertHandler(this);

        getServer().getPluginManager().registerEvents(new PreLogin(), this);
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§6PebbleAntiVPN §cHas Been Unloaded.");
    }

}
