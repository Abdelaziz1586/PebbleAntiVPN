package pebbleantivpn.pebbleantivpnspigot;

import handlers.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Command implements CommandExecutor {

    private final PebbleAntiVPNSpigot main;

    public Command(final PebbleAntiVPNSpigot main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull org.bukkit.command.Command command, final @NotNull String label, final @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            final String message = CommandHandler.INSTANCE.execute(getPermissions(sender), args, !(sender instanceof Player));

            if (message != null) {
                sender.sendMessage(message);
            }
        });
        return false;
    }

    private List<String> getPermissions(final CommandSender sender) {
        final List<String> permissions = new ArrayList<>();

        for (final PermissionAttachmentInfo permission : sender.getEffectivePermissions()) {
            if (permission.getValue()) permissions.add(permission.getPermission());
        }

        return permissions;
    }
}
