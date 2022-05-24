package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import com.twoandahalfdevs.drimprovement.LiteModDRImprovementKt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(GuiBossOverlay.class)
public abstract class GuiBossOverlayMixin extends Gui {
  @Shadow
  @Final
  private Map<UUID, BossInfoClient> mapBossInfos;

  @Shadow
  @Final
  private Minecraft client;

  @Shadow
  protected abstract void render(int p_render_1_, int p_render_2_, BossInfo p_render_3_);

  @Shadow
  @Final
  private static ResourceLocation GUI_BARS_TEXTURES;

  /**
   * @author :)
   */
  @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
  public void renderBossHealth(CallbackInfo ci) {
    if (!this.mapBossInfos.isEmpty()) {
      ScaledResolution lvt_1_1_ = new ScaledResolution(this.client);
      int lvt_2_1_ = lvt_1_1_.getScaledWidth();
      int lvt_3_1_ = 15;
      if (LiteModDRImprovement.mod.getShowHealthBar()) {
        lvt_3_1_ += 30;
      }

      for (BossInfoClient info : this.mapBossInfos.values()) {
        if (info.getUniqueId().equals(LiteModDRImprovementKt.getHealthBarUUID()) && LiteModDRImprovement.mod.getShowHealthBar()) {
          continue;
        }

        int lvt_6_1_ = lvt_2_1_ / 2 - 91;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
        this.render(lvt_6_1_, lvt_3_1_, info);
        String lvt_8_1_ = info.getName().getFormattedText();
        this.client.fontRenderer.drawStringWithShadow(lvt_8_1_, (float) (lvt_2_1_ / 2 - this.client.fontRenderer.getStringWidth(lvt_8_1_) / 2), (float) (lvt_3_1_ - 9), 16777215);
        lvt_3_1_ += 10 + this.client.fontRenderer.FONT_HEIGHT;
        if (lvt_3_1_ >= lvt_1_1_.getScaledHeight() / 3) {
          break;
        }
      }
    }
    ci.cancel();
  }
}
