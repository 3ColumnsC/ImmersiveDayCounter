package com.threecolumnsstudio.immersivedaycounter.mixin;

import com.threecolumnsstudio.immersivedaycounter.client.lumen.LumenBrightness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsSaveMixin {

    @Inject(method = "save", at = @At("HEAD"))
    private void idc$restoreGammaBeforeSave(CallbackInfo ci) {
        if (LumenBrightness.isOverridden()) {
            LumenBrightness.restore(Minecraft.getInstance());
        }
    }
}
