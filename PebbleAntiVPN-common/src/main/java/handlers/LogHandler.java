package handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class LogHandler {

    public static LogHandler INSTANCE;
    private final List<String> keywords;

    public LogHandler(final Logger logger) {
        INSTANCE = this;

        keywords = new ArrayList<>();

        update();

        if (logger == null) {
            new Log4JHandler();
            return;
        }

        logger.getParent().setFilter(record -> {
            String msg = record.getMessage();

            if (record.getParameters() != null) {
                for (int i = 0; i < record.getParameters().length; i++) {
                    msg = msg.replace("{" + i + "}", record.getParameters()[i].toString());
                }
            }

            return !blockMessage(msg);
        });
    }

    public void update() {
        keywords.clear();

        if (Boolean.parseBoolean(DataHandler.INSTANCE.getConfig().getOrDefault("console-filter.enabled", "true").toString())) {
            final Object o = DataHandler.INSTANCE.getConfig().getOrDefault("console-filter.keywords", new ArrayList<>());

            if (o instanceof List) {
                keywords.addAll((List<String>) o);
            }
        }
    }

    public boolean blockMessage(final String message) {
        for (final String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

}
