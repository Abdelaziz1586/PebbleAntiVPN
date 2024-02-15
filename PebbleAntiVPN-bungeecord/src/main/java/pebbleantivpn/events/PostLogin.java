package pebbleantivpn.events;

import handlers.IPHandler;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

public final class PostLogin implements Listener {

    @EventHandler
    public void onPostLogin(final PostLoginEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        final String s = IPHandler.INSTANCE.getBlockMessage(event.getPlayer().getSocketAddress().toString().split(":")[0].replace("/", ""), player.getName(), (List<String>) player.getPermissions());

        if (s != null) {
            event.getPlayer().disconnect(new TextComponent(s));
        }
    }
}
