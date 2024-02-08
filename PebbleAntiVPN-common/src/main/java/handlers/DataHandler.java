package handlers;

import functions.YamlFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public final class DataHandler {

    private final Logger logger;
    private YamlFile data, config;
    private final String ALL_CODES;
    private AlertHandler alertHandler;
    public static DataHandler INSTANCE;

    public DataHandler(final Logger logger) {
        INSTANCE = this;

        this.logger = logger;

        update();

        ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

        new IPHandler();
        new APIHandler();
        new TimeHandler();
    }

    public void update() {
        final File file = new File("plugins/PebbleAntiVPN");

        if (file.mkdir()) {
            logger.info("Created plugin folder");
        }

        try {
            if (data == null) updateData();
            if (config == null) updateConfig();

            data.update();
            config.update();
        } catch (final IOException e) {
            logger.severe("Error while updating files: " + e.getMessage());
        }

        try {
            IPHandler.INSTANCE.update();
            APIHandler.INSTANCE.update();
            TimeHandler.INSTANCE.update();
        } catch (final NullPointerException ignored) {}
    }

    public void setAlertHandler(final AlertHandler alertHandler) {
        this.alertHandler = alertHandler;
    }

    public void alert(final @NotNull String message, final @NotNull String permission) {
        if (alertHandler != null)
            alertHandler.alert(message, permission);
    }

    public Logger getLogger() {
        return logger;
    }

    public YamlFile getData() {
        return data;
    }

    public YamlFile getConfig() {
        return config;
    }

    public String translateAlternateColorCodes(final String textToTranslate) {
        final char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && ALL_CODES.indexOf(b[i + 1]) > -1) {
                b[i] = '§';
                b[i + 1] = Character.toLowerCase( b[i + 1] );
            }
        }
        return new String(b);
    }

    private void updateData() throws IOException {
        final File file = new File("plugins/PebbleAntiVPN/data.yml");

        if (file.createNewFile()) {
            logger.info("Created data.yml");
        }

        data = new YamlFile(file.toPath());
    }

    private void updateConfig() {
        config = new YamlFile(copyFromIDE("config.yml").toPath());
    }

    public File copyFromIDE(final String key) {
        final File file = new File("plugins/PebbleAntiVPN/" + key);
        final ClassLoader classloader = getClass().getClassLoader();

        if (!file.exists()) {
            final InputStream is = classloader.getResourceAsStream(key);
            if (is == null) {
                logger.severe("Couldn't find " + key + " inside the plugin files, please report to the developer ASAP!");
                return file;
            }

            try {
                Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                logger.severe("Couldn't copy file from plugin to disk: " + e.getMessage());
                return file;
            }
        }

        return file;
    }

}
