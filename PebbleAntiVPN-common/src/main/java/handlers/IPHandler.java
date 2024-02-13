package handlers;

import functions.IPData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IPHandler {

    public static IPHandler INSTANCE;
    private final HashMap<String, IPData> data;

    public IPHandler() {
        INSTANCE = this;

        data = new HashMap<>();
    }

    public void update() {
        data.clear();
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
        try {
            final IPData ipData = getIPData(IP, name, permissions);

            return ipData.getBlockMessage();
        } catch (final InterruptedException e) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't fetch data of IP " + IP + ": " + e.getMessage());
        }

        return null;
    }

    private IPData getIPData(final String IP, final String name, final List<String> permissions) throws InterruptedException {
        if (data.containsKey(IP)) {
            return data.get(IP);
        }

        final IPData ipData = new IPData(IP, name, permissions);

        startThread(ipData);
        waitForCompletion(ipData);

        data.put(IP, ipData);

        return ipData;
    }

    private void waitForCompletion(final IPData ipData) throws InterruptedException {
        synchronized (ipData) {
            ipData.wait();
        }
    }

    private void startThread(final IPData ipData) {
        new Thread(() -> {
            ipData.prepare();

            synchronized (ipData) {
                ipData.notifyAll();
            }
        }).start();
    }

}
