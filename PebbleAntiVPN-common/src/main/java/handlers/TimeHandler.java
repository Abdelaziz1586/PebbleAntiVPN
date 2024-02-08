package handlers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeHandler {

    public static TimeHandler INSTANCE;
    private DateTimeFormatter dateTimeFormatter;

    public TimeHandler() {
        INSTANCE = this;

        update();
    }

    public void update() {
        final Object o = DataHandler.INSTANCE.getConfig().get("IP-blockage-alert.date-time-formatter-pattern");

        if (o == null) {
            DataHandler.INSTANCE.getLogger().warning("Couldn't find the value of 'IP-blockage-alert.date-time-formatter-pattern' in the config, using default one 'yyyy/MM/dd HH:mm:ss'");
            dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            return;
        }

        try {
            dateTimeFormatter = DateTimeFormatter.ofPattern(o.toString());
        } catch (final IllegalArgumentException ignored) {
            DataHandler.INSTANCE.getLogger().warning("Couldn't find Date Time Format Pattern of '" + o + "', using default one 'yyyy/MM/dd HH:mm:ss'");
            dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        }
    }

    public String getFormattedTime() {
        return dateTimeFormatter.format(LocalDateTime.now());
    }

}
