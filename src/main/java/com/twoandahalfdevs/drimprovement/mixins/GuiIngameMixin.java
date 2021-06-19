package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.EnergyRendererKt;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public abstract class GuiIngameMixin extends Gui {
  @Shadow
  private int titlesTimer;

  @Shadow
  private String displayedTitle;

  @Shadow
  private String overlayMessage;

  @Shadow
  private int overlayMessageTime;

  /**
   * @author :)
   */
  @Overwrite
  private void renderPlayerStats(ScaledResolution p_renderPlayerStats_1_) {
  }

  /**
   * @author :)
   */
  @Overwrite
  public void renderExpBar(ScaledResolution p_renderExpBar_1_, int p_renderExpBar_2_) {
  }


  /**
   * @author :)
   */
  @Overwrite
  public void renderSelectedItem(ScaledResolution p_renderSelectedItem_1_) {
  }

  private static int oldTime = 0;

  @Inject(method = "renderGameOverlay", at = @At("HEAD"))
  private void renderGameOverlayHead(float p_renderGameOverlay_1_, CallbackInfo info) {
    // Don't display the action bar who cares
    // TODO - DUNGEONS? THEY CARE!?
    oldTime = this.overlayMessageTime;
    EnergyRendererKt.setActionBarMsg(this.overlayMessage);
    EnergyRendererKt.setActionBarTime(this.overlayMessageTime);
    this.overlayMessageTime = 0;
  }

  @Inject(method = "renderGameOverlay", at = @At("TAIL"))
  private void renderGameOverlayTail(float p_renderGameOverlay_1_, CallbackInfo info) {
    this.overlayMessageTime = oldTime;
  }
}
