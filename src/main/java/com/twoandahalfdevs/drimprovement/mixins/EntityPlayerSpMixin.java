package com.twoandahalfdevs.drimprovement.mixins;

import com.mojang.authlib.GameProfile;
import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSpMixin extends EntityPlayer {
  @Shadow
  protected int sprintToggleTimer;

  public EntityPlayerSpMixin(World worldIn, GameProfile gameProfileIn) {
    super(worldIn, gameProfileIn);
  }

  @Inject(method = "onLivingUpdate", at = @At("HEAD"))
  private void getFovModifierHead(CallbackInfo ci) {
    if (LiteModDRImprovement.mod.getNoWTapSprint()) {
      this.sprintToggleTimer = 0;
    }
  }
}
