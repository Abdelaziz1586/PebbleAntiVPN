package functions;

import handlers.*;

import java.util.List;

public final class IPData {

    private final String IP;
    private List<String> permissions;
    private String blockMessage, name;

    public IPData(final String IP, final String name, final List<String> permissions) {
        this.IP = IP;
        this.name = name;
        this.permissions = permissions;
    }

    public void prepare() {
        final Object o = DataHandler.INSTANCE.getData().get("IPs." + APIHandler.INSTANCE.getName() + "." + IP.replace(".", "_") + ".json");

        if (o == null) {
            blockMessage = APIHandler.INSTANCE.getLimitationMessage();

            if (blockMessage == null) {
                final String time = TimeHandler.INSTANCE.getFormattedTime(),
                        json = APIHandler.INSTANCE.sendAndGet(IP, name, time);

                if (json == null) return;

                DataHandler.INSTANCE.getData().set("IPs." + APIHandler.INSTANCE.getName() + "." + IP.replace(".", "_") + ".player", name.toLowerCase());
                DataHandler.INSTANCE.getData().set("IPs." + APIHandler.INSTANCE.getName() + "." + IP.replace(".", "_") + ".whitelisted", false);
                DataHandler.INSTANCE.getData().set("IPs." + APIHandler.INSTANCE.getName() + "." + IP.replace(".", "_") + ".json", json);

                getBlockMessageReady(json, time, true);
            }
            return;
        }

        final Boolean b = (Boolean) DataHandler.INSTANCE.getData().get("IPs." + APIHandler.INSTANCE.getName() + IP.replace(".", "_") + ".whitelisted");

        if (b != null && b) {
            blockMessage = null;
            return;
        }

        getBlockMessageReady(o.toString(), null, false);
    }

    public String getBlockMessage() {
        return IPHandler.INSTANCE.isWhitelisted(IP) ? null : blockMessage;
    }

    public void editForNewRequest(final String name, final List<String> permissions) {
        this.name = name;
        if (permissions.equals(this.permissions)) return;

        this.permissions = permissions;

        prepare();
    }

    private void getBlockMessageReady(final String json, final String time, boolean alert) {
        if (json == null) {
            blockMessage = null;
            return;
        }

        final BlockMessage message = APIHandler.INSTANCE.getActions(json, permissions);

        if (message == null) {
            blockMessage = null;
            return;
        }

        for (final String action : message.getActions()) {
            switch (action.toLowerCase()) {
                case "cancel":
                    blockMessage = message.getMessage();
                    break;
                case "alert":
                    if (alert) {
                        alert = false;

                        Object o = DataHandler.INSTANCE.getConfig().get("IP-blockage-alert.console.enabled");
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
                            WebhookHandler.INSTANCE.alert(IP, name, alertMessage, time);
                        }
                    }
                    break;
            }
        }
    }

}
