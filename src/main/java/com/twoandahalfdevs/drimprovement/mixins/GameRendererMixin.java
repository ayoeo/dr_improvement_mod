package com.twoandahalfdevs.drimprovement.mixins;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(Minecraft.class)
public abstract class GameRendererMixin {
  private static long lastRender = 0;

  @Inject(at = @At("HEAD"), method = "runGameLoop", cancellable = true)
  private void onRender(CallbackInfo callbackInfo) {
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
