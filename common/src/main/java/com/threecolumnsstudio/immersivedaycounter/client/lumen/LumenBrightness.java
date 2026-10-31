package com.threecolumnsstudio.immersivedaycounter.client.lumen;

import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import com.threecolumnsstudio.immersivedaycounter.mixin.GammaAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

public final class LumenBrightness {

    private static Double savedGamma;

    private LumenBrightness() {
    }

    public static boolean isOverridden() {
        return savedGamma != null;
    }

    public static void ensureActive(Minecraft minecraft) {
        OptionInstance<Double> gamma = minecraft.options.gamma();
        if (savedGamma == null) {
            savedGamma = gamma.get();
        }
        double target = IDCConfig.get().lumen.brightness;
        if (gamma.get() != target) {
            ((GammaAccessor) (Object) gamma).idc$setValue(target);
        }
    }

    public static void restore(Minecraft minecraft) {
        if (savedGamma != null) {
            ((GammaAccessor) (Object) minecraft.options.gamma()).idc$setValue(savedGamma);
            savedGamma = null;
        }
    }
}
