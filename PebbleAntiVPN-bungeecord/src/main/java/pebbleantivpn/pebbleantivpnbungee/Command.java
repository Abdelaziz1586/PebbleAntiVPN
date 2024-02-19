package pebbleantivpn.pebbleantivpnbungee;

import handlers.CommandHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Command extends net.md_5.bungee.api.plugin.Command {

    private final PebbleAntiVPNBungeeCord main;

    public Command(final PebbleAntiVPNBungeeCord main) {
        super("pebbleantivpn", null, "pav");

        this.main = main;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
            final String message = CommandHandler.INSTANCE.execute(sender.getPermissions(), args, !(sender instanceof ProxiedPlayer));

            if (message != null) {
                sender.sendMessage(new TextComponent(message));
            }
        });
    }
}
