package com.chattriggers.ctjs.internal.mixins;

import com.chattriggers.ctjs.internal.engine.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "run", at = @At("HEAD"))
    private void injectRun(CallbackInfo ci) {
        new Thread(ModuleManager.INSTANCE::entryPass).start();
    }
}
