package com.twoandahalfdevs.drimprovement.mixins;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClientSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.twoandahalfdevs.drimprovement.LiteModDRImprovement.getLatestSettings;
import static com.twoandahalfdevs.drimprovement.LiteModDRImprovement.setLatestSettings;

@Mixin(NetworkManager.class)
public class NetworkManagerMixin {
  // Please don't kick me when I change my volume
  @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
  private void sendPacket(Packet<?> packet, CallbackInfo ci) {
    if (packet instanceof CPacketClientSettings) {
      if (packet == getLatestSettings()) {
        setLatestSettings(null);
      } else {
        setLatestSettings((CPacketClientSettings) packet);
        ci.cancel();
      }
    }
  }
}
