package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.EnergyRendererKt;
import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public abstract class GuiIngameMixin extends Gui {
  @Shadow
  private String overlayMessage;

  @Shadow
  private int overlayMessageTime;

  /**
   * @author :)
   */
  @Inject(method = "renderPlayerStats", at = @At("HEAD"), cancellable = true)
  private void renderPlayerStats(ScaledResolution scaledRes, CallbackInfo ci) {
    if (LiteModDRImprovement.mod.getCreativeModeLook()) {
      ci.cancel();
    }
  }

  /**
   * @author :)
   */
  @Inject(method = "renderExpBar", at = @At("HEAD"), cancellable = true)
  public void renderExpBar(ScaledResolution scaledRes, int x, CallbackInfo ci) {
    if (LiteModDRImprovement.mod.getCreativeModeLook()) {
      ci.cancel();
    }
  }

  /**
   * @author :)
   */
  @Inject(method = "renderSelectedItem", at = @At("HEAD"), cancellable = true)
  public void renderSelectedItem(ScaledResolution scaledRes, CallbackInfo ci) {
    if (LiteModDRImprovement.mod.getCreativeModeLook()) {
      ci.cancel();
    }
  }

  private static int oldTime = 0;

  @Inject(method = "renderGameOverlay", at = @At("HEAD"))
  private void renderGameOverlayHead(float p_renderGameOverlay_1_, CallbackInfo info) {
    // Don't display the action bar who cares
    // TODO - DUNGEONS? THEY CARE!?
    oldTime = this.overlayMessageTime;
    EnergyRendererKt.setActionBarMsg(this.overlayMessage);
    EnergyRendererKt.setActionBarTime(this.overlayMessageTime);

    if (LiteModDRImprovement.mod.getCreativeModeLook()) {
      this.overlayMessageTime = 0;
    }
  }

  @Inject(method = "renderGameOverlay", at = @At("TAIL"))
  private void renderGameOverlayTail(float p_renderGameOverlay_1_, CallbackInfo info) {
    if (LiteModDRImprovement.mod.getCreativeModeLook()) {
      this.overlayMessageTime = oldTime;
    }
  }
}
