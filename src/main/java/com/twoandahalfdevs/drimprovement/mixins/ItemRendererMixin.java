package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

  @Shadow
  private ItemStack itemStackMainHand;

  @Shadow
  @Final
  private Minecraft mc;

  private int lastHeldItem;

  @Redirect(method = "updateEquippedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;getCooledAttackStrength(F)F"))
  private float getCooledAttackStrength(EntityPlayerSP instance, float v) {
    if (LiteModDRImprovement.mod.getRemoveCooldownHandEffect()) {
      return 1f;
    } else {
      return instance.getCooledAttackStrength(v);
    }
  }

  @Inject(method = "updateEquippedItem", at = @At("HEAD"))
  private void updateEquippedItemHead(CallbackInfo ci) {
    if (LiteModDRImprovement.mod.getRemoveCooldownHandEffect()) {
      if (this.mc.player.inventory.currentItem == this.lastHeldItem && this.itemStackMainHand.getUnlocalizedName().equals(this.mc.player.getHeldItemMainhand().getUnlocalizedName())) {
        this.itemStackMainHand = this.mc.player.getHeldItemMainhand();
      }
    }
    this.lastHeldItem = this.mc.player.inventory.currentItem;
  }
}
