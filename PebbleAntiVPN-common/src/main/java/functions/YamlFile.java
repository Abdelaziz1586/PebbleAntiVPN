package functions;

import handlers.DataHandler;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class YamlFile {

    private final Yaml yaml;
    private final Path filePath;
    private HashMap<String, Object> data;

    public YamlFile(final @NotNull Path filePath) {
        this.filePath = filePath;

        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        yaml = new Yaml(options);
    }

    public void update() {
        try {
            data = yaml.load(new String(Files.readAllBytes(filePath)));

            if (data == null) data = new HashMap<>();
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't load yaml from file " + filePath.toFile().getName() + ": " + e.getMessage());
            data = new HashMap<>();
        }
    }

    public Object get(final @NotNull String key) {
        Map<String, Object> cache = new HashMap<>(data);

        for (final String k : key.split("\\.")) {
            if (cache.containsKey(k)) {
                final Object value = cache.get(k);
                if (value instanceof Map) {
                    cache = (Map<String, Object>) value;
                    continue;
                }

                return value;
            }

            return null;
        }

        return cache;
    }

    public Object getOrDefault(final @NotNull String key, final @NotNull Object defaultValue) {
        Map<String, Object> cache = new HashMap<>(data);

        for (final String k : key.split("\\.")) {
            if (cache.containsKey(k)) {
                final Object value = cache.get(k);
                if (value instanceof Map) {
                    cache = (Map<String, Object>) value;
                    continue;
                }

                return value;
            }

            return defaultValue;
        }
        return cache;
    }

    public void set(final @NotNull String key, final Object value) {
        final String[] keys = key.split("\\.");
        Map<String, Object> cache = data;

        for (int i = 0; i < keys.length - 1; i++) {
            final String k = keys[i];
            if (!cache.containsKey(k) || !(cache.get(k) instanceof Map)) {
                cache.put(k, new LinkedHashMap<>());
            }

            cache = (Map<String, Object>) cache.get(k);
        }

        if (value == null) {
            cache.remove(keys[keys.length - 1]);
        } else {
            cache.put(keys[keys.length - 1], value);
        }

        try (final FileWriter writer = new FileWriter(filePath.toFile())) {
            yaml.dump(data, writer);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().severe("Error while writing in file " + filePath + ": " + e.getMessage());
        }
    }

}
