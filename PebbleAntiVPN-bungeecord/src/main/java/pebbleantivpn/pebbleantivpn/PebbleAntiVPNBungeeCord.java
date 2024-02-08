package pebbleantivpn.pebbleantivpn;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import pebbleantivpn.BungeeAlerts.MainAlert;
import pebbleantivpn.BungeeAlerts.WebhookAlert;
import pebbleantivpn.Loggers.PebbleAntiVPNLoggerBungee;
import pebbleantivpn.data.BungeeHandler;
import pebbleantivpn.events.Disconnect;
import pebbleantivpn.events.PostLogin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;


public final class PebbleAntiVPNBungeeCord extends Plugin {

    private BungeeHandler handler;
    private WebhookAlert webhook;
    private MainAlert bungeeAlert;
    private BungeeProxyChecker proxyChecker;
    private boolean isEnabled = true;

    @Override
    public void onEnable() {
        getLogger().info("§eThe beta version doesn't support Bungee yet.");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                disable();
            }
        }, 1000);
//        getLogger().info("§eLoading §6PebbleAntiVPN§e...");
//        this.handler = new BungeeHandler(this);
//        this.bungeeAlert = new MainAlert(this);
//        this.webhook = new WebhookAlert(this);
//        this.proxyChecker = new BungeeProxyChecker(this);
//        getProxy().getPluginManager().registerListener(this, new PostLogin(this));
//        getProxy().getPluginManager().registerListener(this, new Disconnect(this));
//        getProxy().getPluginManager().registerCommand(this, new BungeeCommands(this));
//        new PebbleAntiVPNLoggerBungee(this);
//        getLogger().info("§6PebbleAntiVPN §bHas Been Loaded.");
    }

    @Override
    public void onDisable() {

    }

    public BungeeHandler getHandler() {
        return this.handler;
    }

    public WebhookAlert getWebhook() {
        return this.webhook;
    }

    public MainAlert getBungeeAlert() {
        return this.bungeeAlert;
    }

    public BungeeProxyChecker getProxyChecker() {
        return this.proxyChecker;
    }

    public void togglePlugin() {
        this.isEnabled = !this.isEnabled;
    }

    public boolean isPluginEnabled() {
        return this.isEnabled;
    }

    private void disable() {
        boolean exception = false;
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        try {
            this.onDisable();
            for (Handler handler : this.getLogger().getHandlers()) {
                handler.close();
            }
        } catch (Throwable t) {
            getLogger().warning("§cCouldn't unload §6PebbleAntiVPN, §cplease avoid using the beta version on bungee!");
            exception = true;
        }

        pluginManager.unregisterCommands(this);

        pluginManager.unregisterListeners(this);

        ProxyServer.getInstance().getScheduler().cancel(this);

        this.getExecutorService().shutdownNow();

        Field pluginsField = null;
        try {
            pluginsField = PluginManager.class.getDeclaredField("plugins");
            pluginsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            getLogger().warning("§cCouldn't unload §6PebbleAntiVPN, §cplease avoid using the beta version on bungee!");
            return;
        }

        Map<String, Plugin> plugins;

        try {
            plugins = (Map<String, Plugin>) pluginsField.get(pluginManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            getLogger().warning("§cCouldn't unload §6PebbleAntiVPN, §cplease avoid using the beta version on bungee!");
            return;
        }

        plugins.remove(this.getDescription().getName());

        ClassLoader cl = this.getClass().getClassLoader();

        if (cl instanceof URLClassLoader) {

            try {

                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);

                Field pluginInitField = cl.getClass().getDeclaredField("desc");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);

                Field allLoadersField = cl.getClass().getDeclaredField("allLoaders");
                allLoadersField.setAccessible(true);
                Set allLoaders = (Set) allLoadersField.get(cl);
                allLoaders.remove(cl);

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                getLogger().warning("§cCouldn't unload §6PebbleAntiVPN, §cplease avoid using the beta version on bungee!");

                return;
            }

            try {

                ((URLClassLoader) cl).close();
            } catch (IOException ex) {
                getLogger().warning("§cCouldn't unload §6PebbleAntiVPN, §cplease avoid using the beta version on bungee!");
                return;
            }

        }

        // Will not work on processes started with the -XX:+DisableExplicitGC flag, but lets try it anyway.
        // This tries to get around the issue where Windows refuses to unlock jar files that were previously loaded into the JVM.
        System.gc();
        if (exception) {
            getLogger().warning("§cCouldn't unload §6PebbleAntiVPN, §cplease avoid using the beta version on bungee!");
            return;
        }
        getLogger().info("§6PebbleAntiVPN was unloaded successfully!");
    }

}
