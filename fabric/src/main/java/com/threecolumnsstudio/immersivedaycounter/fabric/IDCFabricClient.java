package com.threecolumnsstudio.immersivedaycounter.fabric;

import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import com.threecolumnsstudio.immersivedaycounter.client.ClientDayState;
import com.threecolumnsstudio.immersivedaycounter.client.DisplayRenderer;
import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import com.threecolumnsstudio.immersivedaycounter.network.DayPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;

public final class IDCFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        IDCConfig.load(FabricLoader.getInstance().getConfigDir());

        ClientPlayNetworking.registerGlobalReceiver(DayPayload.TYPE,
                (payload, context) -> ClientDayState.update(payload.day(), payload.announce()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientDayState.reset());
        ClientTickEvents.END_CLIENT_TICK.register(client -> IDCConfig.reloadIfChanged());

        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
                ImmersiveDayCounter.id("hud"), DisplayRenderer::render);
    }
}
