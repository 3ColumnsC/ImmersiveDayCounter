package com.threecolumnsstudio.immersivedaycounter.fabric;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import com.threecolumnsstudio.immersivedaycounter.client.ClientDayState;
import com.threecolumnsstudio.immersivedaycounter.client.DisplayRenderer;
import com.threecolumnsstudio.immersivedaycounter.client.lumen.LumenFeature;
import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import com.threecolumnsstudio.immersivedaycounter.network.DayPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

public final class IDCFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        IDCConfig.load(FabricLoader.getInstance().getConfigDir());
        KeyBindingHelper.registerKeyBinding(LumenFeature.KEY);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("idcclient")
                .then(ClientCommandManager.literal("objective")
                    .then(ClientCommandManager.argument("day", LongArgumentType.longArg(IDCConfig.Objective.MIN_DAY, IDCConfig.Objective.MAX_DAY))
                        .executes(context -> setObjective(context.getSource(),
                            LongArgumentType.getLong(context, "day")))))));

        ClientPlayNetworking.registerGlobalReceiver(DayPayload.TYPE,
                (payload, context) -> ClientDayState.update(payload.day(), payload.announce()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientDayState.reset();
            LumenFeature.reset();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            IDCConfig.reloadIfChanged();
            LumenFeature.onClientTick(client);
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
                ImmersiveDayCounter.id("hud"), DisplayRenderer::render);

        ImmersiveDayCounter.LOGGER.info("Lumen loaded successfully");
    }

    private static int setObjective(FabricClientCommandSource source, long day) {
        IDCConfig.get().objective.setCustom(day);
        IDCConfig.save();
        source.sendFeedback(Component.translatable("commands.idc.objective.set", day));
        return 1;
    }
}
