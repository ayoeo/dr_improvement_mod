package com.twoandahalfdevs.drimprovement.mixins;

import com.mojang.authlib.GameProfile;
import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import com.twoandahalfdevs.drimprovement.LiteModDRImprovementKt;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends EntityPlayer {
  public AbstractClientPlayerMixin(World worldIn, GameProfile gameProfileIn) {
    super(worldIn, gameProfileIn);
  }

  @Inject(method = "getFovModifier", at = @At("HEAD"), cancellable = true)
  private void getFovModifierHead(CallbackInfoReturnable<Float> cir) {
    if (LiteModDRImprovement.mod.getOnlySprintFov()) {
      cir.setReturnValue(this.isSprinting() ? 1.08f : 1f);
    }
  }
}
