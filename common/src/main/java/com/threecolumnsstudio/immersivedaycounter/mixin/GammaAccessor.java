package com.threecolumnsstudio.immersivedaycounter.mixin;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionInstance.class)
public interface GammaAccessor {

    @Accessor("value")
    void idc$setValue(Object value);
}
