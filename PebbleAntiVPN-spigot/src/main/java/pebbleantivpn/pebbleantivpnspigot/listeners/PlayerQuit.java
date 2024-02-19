package pebbleantivpn.pebbleantivpnspigot.listeners;

import handlers.IPHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetSocketAddress;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final InetSocketAddress socketAddress = event.getPlayer().getAddress();

        if (socketAddress != null) {
            IPHandler.INSTANCE.decrementConnection(socketAddress.toString().split(":")[0].replace("/", ""));
        }
    }

}
