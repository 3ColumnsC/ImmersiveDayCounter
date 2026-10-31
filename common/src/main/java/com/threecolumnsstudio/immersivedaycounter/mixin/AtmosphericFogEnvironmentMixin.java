package com.threecolumnsstudio.immersivedaycounter.mixin;

import com.threecolumnsstudio.immersivedaycounter.client.lumen.LumenFeature;
import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogEnvironmentMixin {

    @Inject(method = "setupFog", at = @At("TAIL"))
    private void idc$clearNetherEndFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (LumenFeature.isEnabled() && IDCConfig.get().lumen.clearFog) {
            ResourceKey<Level> dimension = level.dimension();
            if (dimension.equals(Level.NETHER) || dimension.equals(Level.END)) {
                fog.environmentalStart = Float.MAX_VALUE;
                fog.environmentalEnd = Float.MAX_VALUE;
                fog.skyEnd = Float.MAX_VALUE;
                fog.cloudEnd = Float.MAX_VALUE;
            }
        }
    }
}
