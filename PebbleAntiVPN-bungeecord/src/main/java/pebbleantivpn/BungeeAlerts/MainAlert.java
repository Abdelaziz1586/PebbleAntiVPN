package pebbleantivpn.BungeeAlerts;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import pebbleantivpn.data.BungeeHandler;
import pebbleantivpn.pebbleantivpn.PebbleAntiVPNBungeeCord;

public class MainAlert {

    private final PebbleAntiVPNBungeeCord main;
    private final BungeeHandler handler;

    public MainAlert(PebbleAntiVPNBungeeCord plugin) {
        this.main = plugin;
        this.handler = plugin.getHandler();

    }

    public void execute(String IP, String name, String country, String countryCode, String time) {
        String message = this.handler.getConfig("alerts.console.message", true).toString().replace("%ip%", IP).replace("%player%", name).replace("%time%", time).replace("%country%", country).replace("%countryCode%", countryCode);
        if ((boolean) this.handler.getConfig("alerts.console.enabled", false))
            this.main.getLogger().info(message);
        if ((boolean) this.handler.getConfig("alerts.players.enabled", false)) {
            this.main.getProxy().getPlayers().forEach(player -> player.sendMessage(player.hasPermission(this.handler.getConfig("alerts.players.permission", false).toString()) ? new TextComponent(message) : new TextComponent("")));
        }
    }

}
