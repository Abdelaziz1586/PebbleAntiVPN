package pebbleantivpn.pebbleantivpn;

import pebbleantivpn.BungeeAlerts.MainAlert;
import pebbleantivpn.BungeeAlerts.WebhookAlert;
import pebbleantivpn.data.BungeeHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BungeeProxyChecker {

    private final BungeeHandler handler;
    private final WebhookAlert webhook;
    private final MainAlert bungeeAlert;

    public BungeeProxyChecker(PebbleAntiVPNBungeeCord plugin) {
        this.handler = plugin.getHandler();
        this.webhook = plugin.getWebhook();
        this.bungeeAlert = plugin.getBungeeAlert();
    }

    public boolean isProxy(String IP, String name) throws IOException {
        String dataIP = IP.replace(".", "_");
        if (this.handler.getData("details." + dataIP + ".proxy") != null) {
            return (boolean) this.handler.getData("details." + dataIP + ".proxy");
        }
        this.handler.addCheckerCount();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        InputStream inputStream;
        URL url = new URL("http://ip-api.com/json/" + IP + "?fields=country,proxy,countryCode");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestProperty("Accept", "application/json");
        int responseCode = http.getResponseCode();
        if (200 <= responseCode && responseCode <= 299) {
            inputStream = http.getInputStream();
        } else {
            inputStream = http.getErrorStream();
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String currentLine;
        while ((currentLine = in.readLine()) != null)
            response.append(currentLine);
        in.close();
        String country = response.toString().split("country\":\"")[1].split("\",")[0];
        String countryCode = response.toString().split("countryCode\":\"")[1].split("\",")[0];
        boolean proxy = Boolean.parseBoolean(response.toString().split("proxy\":")[1].replace("}", ""));
        this.handler.writeData("details." + dataIP + ".proxy", proxy);
        this.handler.writeData("details." + dataIP + ".country.name", country);
        this.handler.writeData("details." + dataIP + ".country.code", countryCode);
        if (proxy) {
            this.bungeeAlert.execute(IP, name, country, countryCode, dtf.format(now));
            this.webhook.discordAlert(IP, name, country, countryCode, dtf.format(now));
        }
        return proxy;
    }

}
