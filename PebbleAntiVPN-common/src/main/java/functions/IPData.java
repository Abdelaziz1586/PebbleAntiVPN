package functions;

import handlers.APIHandler;
import handlers.DataHandler;
import handlers.TimeHandler;

import java.util.List;

public final class IPData {

    private String blockMessage;
    private final String IP, name;
    private final List<String> permissions;

    public IPData(final String IP, final String name, final List<String> permissions) {
        this.IP = IP;
        this.name = name;
        this.permissions = permissions;
    }

    public void prepare() {
        Object o = DataHandler.INSTANCE.getData().get(IP.replace(".", "_") + ".json");

        if (o == null) {
            blockMessage = APIHandler.INSTANCE.getLimitationMessage();

            if (blockMessage == null) {
                final String time = TimeHandler.INSTANCE.getFormattedTime(),
                        json = APIHandler.INSTANCE.sendAndGet(IP, name, time);

                final BlockMessage message = APIHandler.INSTANCE.getActions(json, permissions);

                if (message == null) return;

                boolean alert = false;
                for (final String action : message.getActions()) {
                    switch (action.toLowerCase()) {
                        case "cancel":
                            blockMessage = message.getMessage();
                            break;
                        case "alert":
                            if (!alert) {
                                alert = true;

                                o = DataHandler.INSTANCE.getConfig().get("IP-blockage-alert.console.enabled");
                                if (!(o instanceof Boolean)) {
                                    DataHandler.INSTANCE.getLogger().severe("Couldn't find the value of 'IP-blockage-alert.console.enabled' in the config");
                                    break;
                                }

                                if (Boolean.parseBoolean(o.toString())) {
                                    o = DataHandler.INSTANCE.getConfig().get("IP-blockage-alert.console.message");
                                    if (o == null) {
                                        DataHandler.INSTANCE.getLogger().severe("Couldn't find the value of 'IP-blockage-alert.console.message' in the config");
                                        break;
                                    }

                                    DataHandler.INSTANCE.getLogger().info(DataHandler.INSTANCE.translateAlternateColorCodes(o.toString()).replace("%ip%", IP).replace("%alert-query%", message.getAlertQuery()).replace("%name%", name).replace("%time%", time));
                                }



                                o = DataHandler.INSTANCE.getConfig().get("IP-blockage-alert.players.enabled");
                                if (!(o instanceof Boolean)) {
                                    DataHandler.INSTANCE.getLogger().severe("Couldn't find the value of 'IP-blockage-alert.players.enabled' in the config");
                                    break;
                                }

                                if (Boolean.parseBoolean(o.toString())) {
                                    o = DataHandler.INSTANCE.getConfig().get("IP-blockage-alert.players.message");
                                    if (o == null) {
                                        DataHandler.INSTANCE.getLogger().severe("Couldn't find the value of 'IP-blockage-alert.players.message' in the config");
                                        break;
                                    }

                                    final String alertMessage = DataHandler.INSTANCE.translateAlternateColorCodes(o.toString()).replace("%ip%", IP).replace("%alert-query%", message.getAlertQuery()).replace("%name%", name).replace("%time%", time);

                                    o = DataHandler.INSTANCE.getConfig().get("IP-blockage-alert.players.permission");
                                    if (o == null) {
                                        DataHandler.INSTANCE.getLogger().severe("Couldn't find the value of 'IP-blockage-alert.players.permission' in the config");
                                        break;
                                    }

                                    DataHandler.INSTANCE.alert(alertMessage, o.toString());

                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    public String getBlockMessage() {
        return blockMessage;
    }

}
