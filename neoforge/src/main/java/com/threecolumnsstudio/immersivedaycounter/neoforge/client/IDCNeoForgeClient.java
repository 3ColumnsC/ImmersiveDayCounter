package com.threecolumnsstudio.immersivedaycounter.neoforge.client;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import com.threecolumnsstudio.immersivedaycounter.client.ClientDayState;
import com.threecolumnsstudio.immersivedaycounter.client.DisplayRenderer;
import com.threecolumnsstudio.immersivedaycounter.client.lumen.LumenFeature;
import com.threecolumnsstudio.immersivedaycounter.client.screen.IDCConfigScreen;
import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ImmersiveDayCounter.MOD_ID, dist = Dist.CLIENT)
public final class IDCNeoForgeClient {

    public IDCNeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterGuiLayers);
        modEventBus.addListener(this::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
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

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(LumenFeature.KEY);
        ImmersiveDayCounter.LOGGER.info("Lumen loaded successfully");
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("idcclient")
            .then(Commands.literal("objective")
                .then(Commands.argument("day", LongArgumentType.longArg(IDCConfig.Objective.MIN_DAY, IDCConfig.Objective.MAX_DAY))
                    .executes(context -> setObjective(context.getSource(),
                        LongArgumentType.getLong(context, "day"))))));
    }

    private static int setObjective(CommandSourceStack source, long day) {
        IDCConfig.get().objective.setCustom(day);
        IDCConfig.save();
        source.sendSuccess(() -> Component.translatable("commands.idc.objective.set", day), false);
        return 1;
    }

    private void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientDayState.reset();
        LumenFeature.reset();
    }

    private void onClientTick(ClientTickEvent.Post event) {
        IDCConfig.reloadIfChanged();
        LumenFeature.onClientTick(Minecraft.getInstance());
    }
}
