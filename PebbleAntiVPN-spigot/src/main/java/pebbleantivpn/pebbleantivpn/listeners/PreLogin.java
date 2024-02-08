package pebbleantivpn.pebbleantivpn.listeners;

import handlers.IPHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

public final class PreLogin implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerPreLogin(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        final String s = IPHandler.INSTANCE.getBlockMessage(event.getAddress().getHostAddress(), player.getName(), getPermissions(player));

        if (s != null) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, s);
        }
    }

    private List<String> getPermissions(final Player player) {
        final List<String> permissions = new ArrayList<>();

        for (final PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getValue()) {
                permissions.add(permission.getPermission());
            }
        }

        return permissions;
    }

}
