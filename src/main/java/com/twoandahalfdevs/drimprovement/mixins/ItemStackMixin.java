package com.twoandahalfdevs.drimprovement.mixins;

import com.twoandahalfdevs.drimprovement.LiteModDRImprovement;
import kotlin.text.MatchGroupCollection;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
  @Shadow
  private NBTTagCompound stackTagCompound;

  private final Regex originRegex = new Regex("(.*) \\((.*)\\/.*\\)");

  @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
  private void getTooltip(EntityPlayer i, ITooltipFlag nbttagcompound, CallbackInfoReturnable<List<String>> cir) {
    if (!LiteModDRImprovement.mod.getShowExtraLore()) return;

    List<String> tooltipList = cir.getReturnValue();
    List<String> additionalList = new ArrayList<>();


    if (this.stackTagCompound != null && this.stackTagCompound.hasKey("origin")) {
      String origin = this.stackTagCompound.getString("origin");
      MatchResult results = originRegex.find(origin, 0);
      if (results != null) {
        MatchGroupCollection groups = results.getGroups();
        if (groups.size() == 3) {
          String originType = groups.get(1).getValue();
          String player = groups.get(2).getValue();

          additionalList.add(TextFormatting.GRAY + "Origin: " + originType + " - " + TextFormatting.ITALIC + player);
        }
      }
    }


    if (this.stackTagCompound != null && this.stackTagCompound.hasKey("RepairCost")) {
      int cost = this.stackTagCompound.getInteger("RepairCost");
      if (cost != 0) {
        additionalList.add(TextFormatting.GRAY + "Durability: " + cost);
      }
    }

    if (!additionalList.isEmpty()) {
      tooltipList.add("");
      tooltipList.addAll(additionalList);
    }
//    for (String s : list) {
//      System.out.println("s: " + s);
//    }
//    list.add("Lmao get fucked");
//    if (LiteModDRImprovement.mod.getOnlySprintFov()) {
//      cir.setReturnValue(this.isSprinting() ? 1.08f : 1f);
//    }
  }
}
