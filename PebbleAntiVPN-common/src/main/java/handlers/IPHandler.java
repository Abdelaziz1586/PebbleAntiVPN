package handlers;

import functions.IPData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IPHandler {

    private int limitation;
    public static IPHandler INSTANCE;
    private final HashMap<String, IPData> data;
    private String bypassPerm, limitBlockMessage;
    private final HashMap<String, Integer> connectionsPerIP;

    public IPHandler() {
        INSTANCE = this;

        data = new HashMap<>();
        connectionsPerIP = new HashMap<>();

        update();
    }

    public void update() {
        data.clear();

        if (Boolean.parseBoolean(DataHandler.INSTANCE.getConfig().getOrDefault("connection-limitation.enabled", "true").toString())) {
            final Object o = DataHandler.INSTANCE.getConfig().getOrDefault("connection-limitation.limit", 3);

            if (o instanceof Integer) {
                limitation = (Integer) o;

                if (limitation <= 0) {
                    limitation = -1;

                    bypassPerm = null;
                    limitBlockMessage = null;

                    DataHandler.INSTANCE.getLogger().warning("Connection limitation per IP must be greater than 0! Automatically disabled connection limitation.");

                    System.gc();
                }

                bypassPerm = DataHandler.INSTANCE.getConfig().getOrDefault("connection-limitation.bypass-permission", "pav.connections.bypass").toString();
                limitBlockMessage = DataHandler.INSTANCE.translateAlternateColorCodes(DataHandler.INSTANCE.getConfig().getOrDefault("connection-limitation.block-message", "&cYou have reached the maximum number of connections per IP").toString());

                return;
            }

            limitation = -1;

            bypassPerm = null;
            limitBlockMessage = null;

            DataHandler.INSTANCE.getLogger().warning("Failed to cast connection limitation as Integer! Automatically disabled connection limitation.");

            System.gc();
        }

        limitation = -1;

        bypassPerm = null;
        limitBlockMessage = null;

        System.gc();
    }

    public void decrementConnection(final String IP) {
        connectionsPerIP.put(IP, Math.max(connectionsPerIP.getOrDefault(IP, 0)-1, 0));
    }

    public boolean isWhitelisted(final @NotNull String IP) {
        return Boolean.parseBoolean(DataHandler.INSTANCE.getData().getOrDefault("IPs." + APIHandler.INSTANCE.getName() + "." + IP.replace(".", "_") + ".whitelisted", "false").toString());
    }

    public Boolean whitelist(final @NotNull String query, final boolean whitelist) {
        return query.contains(".") ? whitelistIP(query.replace(".", "_"), whitelist) : whitelistName(query.toLowerCase(), whitelist);
    }

    private Boolean whitelistIP(final String IP, final boolean whitelist) {
        final Object o = DataHandler.INSTANCE.getData().get("IPs." + APIHandler.INSTANCE.getName() + "." + IP);

        if (o instanceof HashMap) {
            if (Boolean.parseBoolean(DataHandler.INSTANCE.getData().getOrDefault("IPs." + APIHandler.INSTANCE.getName() + "." + IP + ".whitelisted", false).toString()) == whitelist) return false;

            DataHandler.INSTANCE.getData().set("IPs." + APIHandler.INSTANCE.getName() + "." + IP + ".whitelisted", whitelist);
            return true;
        }

        return null;
    }

    private Boolean whitelistName(final String name, final boolean whitelist) {
        final Object o = DataHandler.INSTANCE.getData().get("IPs." + APIHandler.INSTANCE.getName());

        if (o instanceof HashMap) {
            for (final Map.Entry<String, HashMap<String, Object>> entry : ((HashMap<String, HashMap<String, Object>>) o).entrySet()) {
                if (entry.getValue().containsValue(name)) {
                    if (Boolean.parseBoolean(entry.getValue().getOrDefault("whitelisted", false).toString()) == whitelist) return false;

                    DataHandler.INSTANCE.getData().set("IPs." + APIHandler.INSTANCE.getName() + "." + entry.getKey() + ".whitelisted", whitelist);
                    return true;
                }
            }
        }
        return null;
    }

    public String getBlockMessage(final String IP, final String name, final List<String> permissions) {
        if (limitation != -1 && !permissions.contains(bypassPerm) && connectionsPerIP.getOrDefault(IP, 0) >= limitation) {
            return limitBlockMessage;
        }

        connectionsPerIP.put(IP, connectionsPerIP.getOrDefault(IP, 0)+1);

        try {
            final IPData ipData = getIPData(IP, name, permissions);

            return DataHandler.INSTANCE.translateAlternateColorCodes(ipData.getBlockMessage());
        } catch (final InterruptedException e) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't fetch data of IP " + IP + ": " + e.getMessage());
            return null;
        }
    }

    private IPData getIPData(final String IP, final String name, final List<String> permissions) throws InterruptedException {
        if (data.containsKey(IP)) {
            final IPData ipData = data.get(IP);

            startThread(ipData, name, permissions);
            waitForCompletion(ipData);

            data.put(IP,ipData);

            return ipData;
        }

        final IPData ipData = new IPData(IP, name, permissions);

        startThread(ipData, null, null);
        waitForCompletion(ipData);

        data.put(IP, ipData);

        return ipData;
    }

    private void startThread(final IPData ipData, final String name, final List<String> permissions) {
        new Thread(() -> {
            if (name == null && permissions == null) {
                ipData.prepare();
            } else {
                ipData.editForNewRequest(name, permissions);
            }

            synchronized (ipData) {
                ipData.notifyAll();
            }
        }).start();
    }

    private void waitForCompletion(final IPData ipData) throws InterruptedException {
        synchronized (ipData) {
            ipData.wait();
        }
    }

}
