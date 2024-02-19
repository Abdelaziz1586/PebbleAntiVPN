package handlers;

import java.util.Collection;

public final class CommandHandler {

    public static CommandHandler INSTANCE;

    public CommandHandler() {
        INSTANCE = this;
    }

    public String execute(final Collection<String> permissions, final String[] args, final boolean console) {
        final boolean reload = hasPermission(DataHandler.INSTANCE.getConfig().getOrDefault("command.reload.permission", "pav.command.reload"), permissions, console),
                whitelist = hasPermission(DataHandler.INSTANCE.getConfig().getOrDefault("command.whitelist.permission", "pav.command.whitelist"), permissions, console);

        if (!reload && !whitelist) return null;

        if (args.length == 0) {
            return DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.invalid-argument", "&7[&6PebbleAntiVPN&7] &c/pav reload/whitelist").toString());
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                DataHandler.INSTANCE.update();
                return DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.reload.message", "&7[&6PebbleAntiVPN&7] &aReloaded all config, data, and APIs!\n&7Info: Check the console to make sure there aren't any errors.").toString());
            case "whitelist":
                if (args.length < 3) {
                    return DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.whitelist.messages.no-given-query", "&7[&6PebbleAntiVPN&7] &aReloaded all config, data, and APIs!\n&7Info: Check the console to make sure there aren't any errors.").toString());
                }

                switch (args[1].toLowerCase()) {
                    case "add":
                        Boolean b = IPHandler.INSTANCE.whitelist(args[2], true);
                        if (b == null) {
                            return DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.whitelist.messages.no-such-ip-or-player", "&7[&6PebbleAntiVPN&7] &cThere is no such IP or player saved in the data file.").toString());
                        }

                        return b ? DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.whitelist.messages.add.success", "&7[&6PebbleAntiVPN&7] &aSuccessfully added &e%query% &ato the whitelist").toString().replace("%query%", args[2])) : DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.whitelist.messages.add.fail", "&7[&6PebbleAntiVPN&7] &c%query% is already whitelisted!").toString().replace("%query%", args[2]));
                    case "remove":
                        b = IPHandler.INSTANCE.whitelist(args[2], false);
                        if (b == null) {
                            return DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.whitelist.messages.no-such-ip-or-player", "&7[&6PebbleAntiVPN&7] &cThere is no such IP or player saved in the data file.").toString().replace("%query%", args[2]));
                        }

                        return b ? DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.whitelist.messages.remove.success", "&7[&6PebbleAntiVPN&7] &aSuccessfully removed &e%query% &afrom the whitelist").toString().replace("%query%", args[2])) : DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.whitelist.messages.remove.fail", "&7[&6PebbleAntiVPN&7] &c%query% is not whitelisted!").toString().replace("%query%", args[2]));
                    default:
                        return DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.whitelist.no-given-query", "&7[&6PebbleAntiVPN&7] &c/pav whitelist (add/remove) (player name/IP)").toString());
                }
            default:
                return DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("command.invalid-argument", "&7[&6PebbleAntiVPN&7] &c/pav reload/whitelist").toString());
        }
    }

    private boolean hasPermission(final Object permission, final Collection<String> permissions, final boolean console) {
        return console || permission != null && permissions.contains(permission.toString());
    }

}
