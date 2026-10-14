package com.threecolumnsstudio.immersivedaycounter.neoforge.client;

import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import com.threecolumnsstudio.immersivedaycounter.client.ClientDayState;
import com.threecolumnsstudio.immersivedaycounter.client.DisplayRenderer;
import com.threecolumnsstudio.immersivedaycounter.client.screen.IDCConfigScreen;
import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ImmersiveDayCounter.MOD_ID, dist = Dist.CLIENT)
public final class IDCNeoForgeClient {

    public IDCNeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterGuiLayers);
        NeoForge.EVENT_BUS.addListener(this::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        IDCConfig.load(FMLPaths.CONFIGDIR.get());
        ModList.get().getModContainerById(ImmersiveDayCounter.MOD_ID).ifPresent(container ->
            container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new IDCConfigScreen(parent)));
    }

    private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CHAT, ImmersiveDayCounter.id("hud"), DisplayRenderer::render);
    }

    private void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientDayState.reset();
    }

    private void onClientTick(ClientTickEvent.Post event) {
        IDCConfig.reloadIfChanged();
    }
}
