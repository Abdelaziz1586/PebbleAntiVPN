package pebbleantivpn.pebbleantivpn;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import pebbleantivpn.data.BungeeHandler;

import java.io.IOException;
import java.util.HashMap;

public class BungeeCommands extends Command {

    private final PebbleAntiVPNBungeeCord main;
    private final BungeeHandler handler;

    public BungeeCommands(PebbleAntiVPNBungeeCord plugin) {
        super("Pav");
        this.main = plugin;
        this.handler = plugin.getHandler();
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    this.handler.update();
                    sender.sendMessage(new TextComponent("§aReloaded all configs."));
                } else if (args[0].equalsIgnoreCase("toggle")) {
                    this.main.togglePlugin();
                    if (this.main.isPluginEnabled())
                        sender.sendMessage(new TextComponent("§aAll PebbleAntiVPN checks and events have been enabled."));
                    else
                        sender.sendMessage(new TextComponent("§cAll PebbleAntiVPN checks and events have been disabled."));
                } else if (args[0].equalsIgnoreCase("whitelist")) {
                    if (args.length == 3) {
                        String IP = args[2];
                        String dataIP = IP.replace(".", "_");
                        HashMap<String, Boolean> details = this.handler.getDetials(dataIP);
                        if (args[1].equalsIgnoreCase("add")) {
                            if (!details.get("joined")) {
                                sender.sendMessage(new TextComponent("§cThis IP haven't joined the server before."));
                                return;
                            }
                            if (!details.get("proxy")) {
                                sender.sendMessage(new TextComponent("§cThis IP address is not blocked."));
                                return;
                            }

                            if (details.get("whitelist")) {
                                sender.sendMessage(new TextComponent("§cThis IP is already whitelisted."));
                                return;
                            }

                            try {
                                this.handler.writeData("details." + dataIP + ".whitelisted", true);
                                sender.sendMessage(new TextComponent("§aAdded §b" + IP + " §ato whitelist."));
                            } catch (IOException e) {
                                sender.sendMessage(new TextComponent("§cAn error occurred while whitelisting IP §b" + IP + "\n"));
                                e.printStackTrace();
                            }
                        } else if (args[1].equalsIgnoreCase("remove")) {
                            if (!details.get("whitelist")) {
                                sender.sendMessage(new TextComponent("§cThis IP is not whitelisted."));
                                return;
                            }

                            try {
                                this.handler.writeData("details." + dataIP + ".whitelisted", false);
                                sender.sendMessage(new TextComponent("§aRemoved §b" + IP + " §afrom whitelist."));
                            } catch (IOException e) {
                                sender.sendMessage(new TextComponent("§cAn error occurred while whitelisting IP §b" + IP + "\n"));
                                e.printStackTrace();
                            }
                        } else {
                            sender.sendMessage(new TextComponent("§cInvalid arguments (pav whitelist <add/remove> <IP>)"));
                        }
                    } else {
                        sender.sendMessage(new TextComponent("§cInvalid arguments (pav whitelist <add/remove> <IP>)"));
                    }
                } else {
                    sender.sendMessage(new TextComponent("§cInvalid arguments (reload/toggle/whitelist)"));
                }
            } else {
                sender.sendMessage(new TextComponent("§cInvalid arguments (reload/toggle/whitelist)"));
            }
        } else {
            sender.sendMessage(new TextComponent("§cInvalid arguments (reload/toggle/whitelist)"));
        }
    }
}