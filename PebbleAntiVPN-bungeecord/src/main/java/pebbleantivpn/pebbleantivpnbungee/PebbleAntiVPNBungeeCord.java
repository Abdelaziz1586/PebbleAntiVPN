package pebbleantivpn.pebbleantivpnbungee;

import handlers.DataHandler;
import handlers.LogHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import pebbleantivpn.pebbleantivpnbungee.handlers.AlertHandler;
import pebbleantivpn.pebbleantivpnbungee.listeners.PostLogin;


public final class PebbleAntiVPNBungeeCord extends Plugin {

    public PebbleAntiVPNBungeeCord() {
        new DataHandler(getLogger());
        new AlertHandler(this);

        new LogHandler(ProxyServer.getInstance().getVersion().contains("BungeeCord") ? getLogger() : null);

        getProxy().getPluginManager().registerCommand(this, new Command(this));

        getProxy().getPluginManager().registerListener(this, new PostLogin());
    }
}
