package pebbleantivpn.handlers;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import pebbleantivpn.pebbleantivpn.PebbleAntiVPNBungeeCord;


import java.util.function.Consumer;

public class AlertHandler implements handlers.AlertHandler {

    private final Consumer<String[]> message;

    public AlertHandler(final PebbleAntiVPNBungeeCord main) {
        message = s -> ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            final TextComponent msg = new net.md_5.bungee.api.chat.TextComponent(s[1]);
            for (final ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (player.hasPermission(s[0])) player.sendMessage(msg);
            }
        });
    }

    @Override
    public void alert(final @NotNull String message, final @NotNull String permission) {
        this.message.accept(new String[]{message, permission});
    }
}
