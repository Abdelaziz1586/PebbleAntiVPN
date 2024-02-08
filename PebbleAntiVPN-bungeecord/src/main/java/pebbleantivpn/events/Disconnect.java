package pebbleantivpn.events;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import pebbleantivpn.data.BungeeHandler;
import pebbleantivpn.pebbleantivpn.PebbleAntiVPNBungeeCord;

public class Disconnect implements Listener {

    private final BungeeHandler handler;

    public Disconnect(PebbleAntiVPNBungeeCord plugin) {
        this.handler = plugin.getHandler();
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        if ((boolean) this.handler.getConfig("users-per-ip.enabled", false)) {
            String IP = e.getPlayer().getSocketAddress().toString().split(":")[0].replace("/", "");
            this.handler.removeConnection(IP);
        }
    }

}
