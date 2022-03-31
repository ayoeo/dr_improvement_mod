package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
  private static long lastRender = 0;

  // Limit fps to 20 when tabbed out
  @Inject(at = @At("HEAD"), method = "runGameLoop", cancellable = true)
  private void onRender(CallbackInfo callbackInfo) {
    if (LiteModDRImprovement.mod.getLimitFpsWhenTabbedOut()) {
      if (!Display.isActive() && System.currentTimeMillis() - lastRender < 50) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        callbackInfo.cancel();
      } else {
        lastRender = System.currentTimeMillis();
      }
    }
  }
}
