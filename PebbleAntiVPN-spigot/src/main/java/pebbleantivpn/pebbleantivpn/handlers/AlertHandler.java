package pebbleantivpn.pebbleantivpn.handlers;

import handlers.DataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pebbleantivpn.pebbleantivpn.PebbleAntiVPNSpigot;

import java.util.function.Consumer;

public final class AlertHandler implements handlers.AlertHandler {

    private final Consumer<String[]> message;

    public AlertHandler(final PebbleAntiVPNSpigot main) {
        DataHandler.INSTANCE.setAlertHandler(this);

        message = s -> Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(s[0])) player.sendMessage(s[1]);
            }
        });
    }

    @Override
    public void alert(final @NotNull String message, final @NotNull String permission) {
        this.message.accept(new String[]{message, permission});
    }
}
