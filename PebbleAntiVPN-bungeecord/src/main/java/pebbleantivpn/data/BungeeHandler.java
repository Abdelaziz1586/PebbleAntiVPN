package pebbleantivpn.data;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import pebbleantivpn.pebbleantivpn.PebbleAntiVPNBungeeCord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@SuppressWarnings("all")
public class BungeeHandler {

    private Configuration config;
    private Configuration webhook;
    private Configuration data;
    private int checks = 0;
    private final PebbleAntiVPNBungeeCord main;

    private final HashMap<String, Integer> IPConnections = new HashMap<>();

    public BungeeHandler(PebbleAntiVPNBungeeCord plugin) {
        this.main = plugin;
        update();
    }

    public void update() {
        try {
            updateConfig();
            updateWebhook();
            updateData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateConfig() throws IOException {
        File file = getConfigFile();
        this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    public void updateData() throws IOException {
        File file = getDataFile();
        this.data = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);;
    }

    public void updateWebhook() throws IOException {
        File file = getWebhookFile();
        this.webhook = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    public void writeData(String key, Object value) throws IOException {
        this.data.set(key, value);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.data, getDataFile());
    }

    public File getConfigFile() {
        if (!this.main.getDataFolder().exists())
            this.main.getDataFolder().mkdir();

        File file = new File(this.main.getDataFolder().getPath(), "config.yml");
        if (!file.exists()) {
            InputStream config = this.main.getResourceAsStream("config.yml");
            this.copyFromIDE(config, file);
        }
        return file;
    }

    public File getWebhookFile() {
        if (!this.main.getDataFolder().exists())
            this.main.getDataFolder().mkdir();

        File file = new File(this.main.getDataFolder().getPath(), "webhook.yml");
        if (!file.exists()) {
            InputStream config = this.main.getResourceAsStream("webhook.yml");
            this.copyFromIDE(config, file);
        }
        return file;
    }

    public File getDataFile() {
        File file = new File(this.main.getDataFolder().getPath(), "data.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public Object getConfig(String key, boolean translate) {
        if (this.config.get(key) != null) {
            if (translate)
                return ChatColor.translateAlternateColorCodes('&', (String) Objects.requireNonNull(this.config.get(key))).replace("%nl%", "\n");
            return this.config.get(key);
        }
        return null;
    }

    public Object getWebhook(String key) {
        if (this.webhook.get(key) != null)
            return this.webhook.get(key);
        return null;
    }

    public Object getData(String key) {
        if (this.data.get(key) != null)
            return this.data.get(key);
        return null;
    }

    public Configuration getConfigSelection(String key) {
        if (this.webhook.get(key) != null)
            return this.webhook.getSection(key);
        return null;
    }

    public List<?> getList(String key) {
        if (this.config.get(key) != null)
            return this.config.getList(key);
        return null;
    }

    public int getConnections(String IP) {
        if (this.IPConnections.containsKey(IP))
            return this.IPConnections.get(IP);
        return 0;
    }

    public void addConnection(String IP) {
        int i = 0;
        if (this.IPConnections.containsKey(IP))
            i = this.IPConnections.get(IP);
        i++;
        this.IPConnections.remove(IP);
        this.IPConnections.put(IP, i);
    }

    public void removeConnection(String IP) {
        if (!this.IPConnections.containsKey(IP))
            return;
        int i = this.IPConnections.get(IP);
        if (i <= 0)
            return;
        i--;
        this.IPConnections.remove(IP);
        this.IPConnections.put(IP, i);
    }

    private void copyFromIDE(InputStream from, File to) {
        try {
            Files.copy(from, to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCleaner() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                clearCheckerCount();
            }
        }, 0, 60000);
    }

    public void addCheckerCount() {
        this.checks++;
    }

    public void clearCheckerCount() {
        this.checks = 0;
    }

    public HashMap<String, Boolean> getDetials(String dataIP) {
        Object proxyDetails = this.getData("details." + dataIP + ".proxy");
        Object whitelistDetails = this.getData("details." + dataIP + ".whitelisted");

        HashMap<String, Boolean> details = new HashMap<>();

        details.put("joined", proxyDetails != null);
        if (proxyDetails == null)
            details.put("proxy", false);
        else
            details.put("proxy", (boolean) proxyDetails);
        if (whitelistDetails == null)
            details.put("whitelist", false);
        else
            details.put("whitelist", (boolean) whitelistDetails);

        return details;
    }

}
