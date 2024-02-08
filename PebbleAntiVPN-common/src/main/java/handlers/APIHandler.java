package handlers;

import functions.BlockMessage;
import functions.YamlFile;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public final class APIHandler {

    private YamlFile api;
    private Timer limitationCooldown;
    public static APIHandler INSTANCE;
    private int limitation, connections;
    private final HashMap<String, HashMap<String, Object>> variables;
    private String name, url, header, value, limitationMessage, method;

    public APIHandler() {
        INSTANCE = this;

        variables = new HashMap<>();

        update();
    }

    public void update() {
        final long l = System.currentTimeMillis();

        reset();

        Object o = DataHandler.INSTANCE.getConfig().get("api-config-file");

        if (o == null) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't find the value of 'api-config-file' in the config");
            return;
        }

        File file = new File("plugins/PebbleAntiVPN/APIs");

        if (file.mkdir()) {
            DataHandler.INSTANCE.getLogger().info("Created APIs folder");

            DataHandler.INSTANCE.copyFromIDE("APIs/IP-API.yml");
        }

        file = new File("plugins/PebbleAntiVPN/APIs/" + o);

        if (!file.exists()) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't find API " + o);
            return;
        }

        api = new YamlFile(file.toPath());

        api.update();

        o = api.get("name");
        if (o == null) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't find 'name' key in API file " + file.getName());
            reset();
            return;
        }

        name = o.toString();

        o = api.get("url");
        if (o == null) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't find 'url' key in API file " + file.getName());
            reset();
            return;
        }

        url = o.toString();

        o = api.get("request-method");
        if (o == null) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't find 'request-method' key in API file " + file.getName());
            reset();
            return;
        }

        method = o.toString();

        o = api.get("key.enabled");
        if (o == null) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't find 'key.enabled' key in API file " + file.getName());
            reset();
            return;
        }

        if (Boolean.parseBoolean(o.toString())) {
            o = api.get("key.header");
            if (o == null) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't find 'key.header' key in API file " + file.getName());
                reset();
                return;
            }

            header = o.toString();

            o = api.get("key.value");
            if (o == null) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't find 'key.value' key in API file " + file.getName());
                reset();
                return;
            }
        } else {
            value = null;
            header = null;
        }

        o = api.get("limitation.enabled");
        if (o == null) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't find 'limitation.enabled' key in API file " + file.getName());
            reset();
            return;
        }

        if (Boolean.parseBoolean(o.toString())) {
            o = api.get("limitation.maxNumberOfChecksPerTime");
            if (o == null) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't find 'limitation.maxNumberOfChecksPerTime' key in API file " + file.getName());
                reset();
                return;
            }

            final String[] temp = o.toString().split(",");

            try {
                limitation = Integer.parseInt(temp[0]);

                if (limitation <= 0) {
                    DataHandler.INSTANCE.getLogger().severe("Limitation limit must be greater than 0!");
                    reset();
                    return;
                }

                final int i = Integer.parseInt(temp[1]);

                if (i <= 0) {
                    DataHandler.INSTANCE.getLogger().severe("Limitation reset time must be greater than 0!");
                    reset();
                    return;
                }

                if (limitationCooldown != null) {
                    limitationCooldown.cancel();
                }

                limitationCooldown = new Timer();

                limitationCooldown.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        connections = 0;
                    }
                }, 0, i * 1000L);
            } catch (final NumberFormatException ignored) {
                DataHandler.INSTANCE.getLogger().severe("The format of limitation written in API " + file.getName() + " is invalid.");
                reset();
                return;
            }

            o = api.get("limitation.limitation-message");
            if (o == null) {
                DataHandler.INSTANCE.getLogger().warning("Couldn't find 'limitation.limitation-message' key in API file " + file.getName());
                reset();
                return;
            }

            limitationMessage = DataHandler.INSTANCE.translateAlternateColorCodes(o.toString());
        } else {
            limitation = -1;
            limitationMessage = null;

            if (limitationCooldown != null) {
                limitationCooldown.cancel();
                limitationCooldown = null;
            }
        }

        o = api.get("vars");
        if (o == null) {
            DataHandler.INSTANCE.getLogger().warning("Couldn't find 'vars' key in API file " + file.getName());
            reset();
            return;
        }

        final HashMap<String, HashMap<String, Object>> cache = (HashMap<String, HashMap<String, Object>>) o;

        String var;
        HashMap<String, Object> map;
        for (final Map.Entry<String, HashMap<String, Object>> entry : cache.entrySet()) {
            var = entry.getKey();

            if (variables.containsKey(var)) {
                DataHandler.INSTANCE.getLogger().severe("Duplicated variables detected (" + var + ") in API file " + file.getName());
                reset();
                return;
            }

            map = entry.getValue();

            if (!map.containsKey("alert-query")) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't find 'vars." + var + ".alert-query' key in API file " + file.getName());
                reset();
                return;
            }
            setVariableData(var, "alert-query", map.get("alert-query"));

            if (!map.containsKey("bypass-permission")) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't find 'vars." + var + ".bypass-permission' key in API file " + file.getName());
                reset();
                return;
            }
            setVariableData(var, "bypass-permission", map.get("bypass-permission"));

            if (!map.containsKey("cancel-message")) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't find 'vars." + var + ".cancel-message' key in API file " + file.getName());
                reset();
                return;
            }
            setVariableData(var, "cancel-message", map.get("cancel-message"));

            if (!map.containsKey("conditions")) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't find 'vars." + var + ".conditions' key in API file " + file.getName());
                reset();
                return;
            }

            if (!(map.get("conditions") instanceof HashMap)) {
                DataHandler.INSTANCE.getLogger().severe("Invalid format of 'vars." + var + ".conditions' key in API file " + file.getName());
                reset();
                return;
            }

            for (final Map.Entry<String, Object> entry1 : ((HashMap<String, Object>) map.get("conditions")).entrySet()) {
                if (!(entry1.getValue() instanceof List)) {
                    DataHandler.INSTANCE.getLogger().severe("Invalid format of 'vars." + var + ".conditions." + entry1.getKey() + "' key in API file " + file.getName());
                    reset();
                    return;
                }

                for (final String action : (List<String>) entry1.getValue()) {
                    if (!action.equalsIgnoreCase("CANCEL") && !action.equalsIgnoreCase("ALERT")) {
                        DataHandler.INSTANCE.getLogger().severe("Invalid action in 'vars." + var + ".conditions." + entry1.getKey() + "' key in API file " + file.getName());
                        reset();
                        return;
                    }
                }
            }
            setVariableData(var, "conditions", map.get("conditions"));


        }

        DataHandler.INSTANCE.getLogger().info("Successfully loaded API file " + file.getName() + " in " + (System.currentTimeMillis()-l) + "ms");
    }

    public BlockMessage getActions(final String json, final List<String> permissions) {
        HashMap<String, Object> map;
        for (final Map.Entry<String, HashMap<String, Object>> entry : variables.entrySet()) {
            map = entry.getValue();

            if (permissions.contains(map.get("bypass-permission").toString())) continue;

            for (final Map.Entry<Object, List<String>> entry1 : ((HashMap<Object, List<String>>) map.get("conditions")).entrySet()) {
                if (getJSON(json, entry.getKey()).equals(entry1.getKey().toString())) {
                    return new BlockMessage()
                            .setActions(entry1.getValue())
                            .setAlertQuery(map.get("alert-query").toString())
                            .setMessage(map.get("cancel-message").toString());
                }
            }
        }

        return null;
    }

    public String sendAndGet(final @NotNull String IP, final @NotNull String name, final @NotNull String time) {
        try {
            final BufferedReader in = getBufferedReader(IP, name, time);
            final StringBuilder response = new StringBuilder();

            String currentLine;

            while ((currentLine = in.readLine()) != null) response.append(currentLine);

            in.close();

            if (limitation != -1) connections++;

            return response.toString();
        } catch (final Exception e) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't send request to '" + url + "'");
        }

        return null;
    }

    public String getLimitationMessage() {
        return connections < limitation ? null : limitationMessage;
    }

    public String getJSON(final @NotNull String json, final @NotNull String key) {
        return new JSONObject(json).optString(key);
    }

    private void reset() {
        api = null;
        url = null;
        name = null;
        value = null;
        header = null;
        limitation = -1;
        limitationMessage = null;

        variables.clear();

        if (limitationCooldown != null) {
            limitationCooldown.cancel();
            limitationCooldown = null;
        }

        System.gc();
    }

    private void setVariableData(final String variable, final String key, final Object value) {
        final HashMap<String, Object> variableData = variables.getOrDefault(variable, new HashMap<>());

        variableData.put(key, value);

        variables.put(variable, variableData);
    }

    private BufferedReader getBufferedReader(final @NotNull String IP, final @NotNull String name, final @NotNull String time) throws IOException {
        final URL url = new URL(this.url.replace("%ip%", IP).replace("%player%", name).replace("%time%", time));

        final HttpURLConnection http = (HttpURLConnection) url.openConnection();

        http.setConnectTimeout(1000);
        http.setReadTimeout(1000);

        http.setRequestMethod(method);
        http.setRequestProperty("Accept", "application/json");

        if (header != null)
            http.setRequestProperty(header, value);

        final int responseCode = http.getResponseCode();

        final InputStream inputStream = 200 <= responseCode && responseCode <= 299 ? http.getInputStream() : http.getErrorStream();

        return new BufferedReader(new InputStreamReader(inputStream));
    }

}
