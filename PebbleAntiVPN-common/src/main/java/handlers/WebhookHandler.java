package handlers;

import discord.DiscordWebhook;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class WebhookHandler {

    private boolean randomColor;
    private DiscordWebhook webhook;
    public static WebhookHandler INSTANCE;
    private DiscordWebhook.EmbedObject embed;

    public WebhookHandler() {
        INSTANCE = this;

        update();
    }

    public void update() {
        webhook = null;

        Object o = DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.enabled", "false");

        if (Boolean.parseBoolean(o.toString())) {
            o = DataHandler.INSTANCE.getConfig().get("IP-blockage-alert.discord-webhook.webhook-url");

            if (o == null) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't fetch webhook URL from the config! Automatically disabled webhook alerts.");
                reset();
                return;
            }

            webhook = new DiscordWebhook(o.toString());

            webhook.setUsername(DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.username", "PebbleAnti-VPN Alert").toString());

            webhook.setContent(Boolean.parseBoolean(DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.text-message.enabled", "true").toString()) ? DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.text-message.content", "**New Alert!**").toString() : null);

            o = DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.enabled", "true");

            if (Boolean.parseBoolean(o.toString())) {
                embed = new DiscordWebhook.EmbedObject();

                o = DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.title.enabled", "true");

                embed.setTitle(Boolean.parseBoolean(o.toString()) ? DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.title.text", "PebbleAntiVPN IP Blockage").toString() : null);

                o = DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.description.enabled", "true");

                embed.setDescription(Boolean.parseBoolean(o.toString()) ? DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.description.text", "A player has connected using a suspicious IP").toString() : null);

                randomColor = Boolean.parseBoolean(DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.color.random", "false").toString());

                if (!randomColor) {
                    o = DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.color.rgb-color", "242, 146, 13").toString().replace(" ", "");
                    try {
                        final String[] s = o.toString().split(",");

                        if (s.length != 3) {
                            DataHandler.INSTANCE.getLogger().severe("Couldn't fetch, split, or cast embed RGB color! Automatically setting it to '242, 146, 13'");
                            embed.setColor(new Color(242, 146, 13));
                        } else {
                            embed.setColor(new Color(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2])));
                        }
                    } catch (final Exception ignored) {
                        DataHandler.INSTANCE.getLogger().severe("Couldn't fetch, split, or cast embed RGB color! Automatically setting it to '242, 146, 13'");
                        embed.setColor(new Color(242, 146, 13));
                    }
                }


                if (Boolean.parseBoolean(DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.fields.enabled", "true").toString())) {
                    o = DataHandler.INSTANCE.getConfig().get("IP-blockage-alert.discord-webhook.embed.fields.embed-fields");

                    if (!(o instanceof HashMap)) {
                        DataHandler.INSTANCE.getLogger().severe("Couldn't fetch embed fields! Automatically disabled webhook alerts.");
                        reset();
                        return;
                    }

                    for (final HashMap<String, Object> map : ((HashMap<String, HashMap<String, Object>>) o).values()) {
                        if (map.containsKey("name")) {
                            DataHandler.INSTANCE.getLogger().severe("Couldn't fetch 'name' key from an embed-field! Automatically disabled webhook alerts.");
                            reset();
                            return;
                        }
                        if (map.containsKey("value")) {
                            DataHandler.INSTANCE.getLogger().severe("Couldn't fetch 'value' key from an embed-field! Automatically disabled webhook alerts.");
                            reset();
                            return;
                        }
                        if (map.containsKey("inline")) {
                            DataHandler.INSTANCE.getLogger().severe("Couldn't fetch 'inline' key from an embed-field! Automatically disabled webhook alerts.");
                            reset();
                            return;
                        }

                        embed.addField(map.get("name").toString(), map.get("value").toString(), Boolean.parseBoolean(map.get("inline").toString()));
                    }
                }

                if (Boolean.parseBoolean(DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.footer.enabled", "true").toString())) {
                    embed.setFooter(DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.footer.text", "Detected at: %time%").toString(), Boolean.parseBoolean(DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.footer.icon.enabled", "true").toString()) ? DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.footer.icon.url", "https://i.imgur.com/obCxZdl.png").toString() : null);
                }

                if (Boolean.parseBoolean(DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.thumbnail.enabled", "true").toString())) {
                    embed.setThumbnail(DataHandler.INSTANCE.getConfig().getOrDefault("IP-blockage-alert.discord-webhook.embed.thumbnail.thumbnail-url", "https://mc-heads.net/avatar/%player%/").toString());
                }
            }
        }

        reset();
    }

    public void alert(final String IP, final String name, final String alertQuery, final String time) {
        if (webhook != null) {
            webhook.clearEmbeds();

            final String[] s = new String[]{IP, name, alertQuery, time};

            webhook.setContent(replaceString(webhook.getContent(), s));

            if (embed != null) {
                final DiscordWebhook.EmbedObject.Footer footer = embed.getFooter();

                embed.setFooter(replaceString(footer.getText(), s), replaceString(footer.getIconUrl(), s));

                embed.setTitle(replaceString(embed.getTitle(), s));

                embed.setDescription(replaceString(embed.getDescription(), s));

                if (randomColor) embed.setColor(getRandomColor());

                for (final DiscordWebhook.EmbedObject.Field field : embed.getFields()) {
                    field.setName(replaceString(field.getName(), s));
                    field.setValue(replaceString(field.getValue(), s));
                }

                final DiscordWebhook.EmbedObject.Thumbnail thumbnail = embed.getThumbnail();

                embed.setThumbnail(replaceString(thumbnail.getUrl(), s));

                webhook.addEmbed(embed);
            }

            try {
                webhook.execute();
            } catch (final IOException e) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't send webhook message: " + e.getMessage());
            }
        }
    }

    private String replaceString(final String text, final String[] s) {
        return text != null ? text.replace("%ip%", s[0]).replace("%name%", s[1]).replace("%alert-query%", s[2]).replace("%time%", s[3]) : null;
    }

    private Color getRandomColor() {
        final Random random = new Random();

        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    private void reset() {
        embed = null;
        webhook = null;

        System.gc();
    }


}
