package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
  private static long lastRender = 0;

  // Limit fps to 20 when tabbed out
  @Inject(at = @At("HEAD"), method = "runGameLoop", cancellable = true)
  private void onRender(CallbackInfo callbackInfo) {
    if (LiteModDRImprovement.mod.getLimitFpsWhenTabbedOut()) {
      if (!Display.isActive() && Minecraft.getSystemTime() - lastRender < 50) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        callbackInfo.cancel();
      } else {
        lastRender = Minecraft.getSystemTime();
      }
    }
  }

  @Redirect(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"))
  private void getCooledAttackStrength(InventoryPlayer instance, int direction) {
    if (LiteModDRImprovement.mod.getPreventHotbarScrolling()) {
      return;
    }

    if (direction > 0) {
      direction = 1;
    }

    if (direction < 0) {
      direction = -1;
    }

    for (instance.currentItem -= direction; instance.currentItem < 0; instance.currentItem += 9) {
    }

    while (instance.currentItem >= 9) {
      instance.currentItem -= 9;
    }
  }
}
