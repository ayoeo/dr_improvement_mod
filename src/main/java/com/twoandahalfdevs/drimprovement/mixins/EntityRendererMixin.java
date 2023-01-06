package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(at = @At("HEAD"), method = "hurtCameraEffect", cancellable = true)
    public void bobViewWhenHurt(float p_hurtCameraEffect_1_, CallbackInfo ci) {
        if (LiteModDRImprovement.mod.getNoHurtCam()) ci.cancel();
    }
}