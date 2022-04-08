package com.twoandahalfdevs.drimprovement

import net.minecraft.util.MouseHelper

class RawMouseHelper : MouseHelper() {
  @Override
  override fun mouseXYChange() {
    LiteModDRImprovement.mod.pollingSemaphore.acquire()
    this.deltaX = LiteModDRImprovement.mod.dx
    LiteModDRImprovement.mod.dx = 0
    this.deltaY = -LiteModDRImprovement.mod.dy
    LiteModDRImprovement.mod.dy = 0
    LiteModDRImprovement.mod.pollingSemaphore.release()
  }
}
