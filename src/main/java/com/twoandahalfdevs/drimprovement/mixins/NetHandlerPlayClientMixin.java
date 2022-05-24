package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {
  @Inject(method = "handleChat", at = @At("HEAD"))
  private void handleTitle(SPacketChat packet, CallbackInfo ci) {
    
    if (packet.getType() == ChatType.GAME_INFO) {
      LiteModDRImprovement.mod.updateActionBar(packet.getChatComponent().getUnformattedText());
    }
  }
}
