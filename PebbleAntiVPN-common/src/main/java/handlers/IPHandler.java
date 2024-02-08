package handlers;

import functions.IPData;

import java.util.HashMap;
import java.util.List;

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
