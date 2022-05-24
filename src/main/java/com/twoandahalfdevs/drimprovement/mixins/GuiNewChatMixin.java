package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiNewChat.class)
public abstract class GuiNewChatMixin extends Gui {
  @Inject(method = "setChatLine", at = @At("HEAD"), cancellable = true)
  private void onSetChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, CallbackInfo ci) {
    String text = chatComponent.getUnformattedText();
    boolean matches = LiteModDRImprovement.mod.getDebugRegex().matches(text);
    if (matches && LiteModDRImprovement.mod.getHideDebug()) {
      ci.cancel();
    }
  }
}
