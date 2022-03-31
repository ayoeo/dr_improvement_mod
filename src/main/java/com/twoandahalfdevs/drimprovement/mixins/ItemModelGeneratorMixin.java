package com.twoandahalfdevs.drimprovement.mixins;

import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ItemModelGenerator.class)
public class ItemModelGeneratorMixin {
  private float unlerp(float delta, float start, float end) {
    return (start - delta * end) / (1 - delta);
  }

  private float[] unlerpUVs(float[] uvs, float delta) {
    float centerU = (uvs[0] + uvs[2]) / 2.0F;
    float centerV = (uvs[1] + uvs[3]) / 2.0F;
    uvs[0] = unlerp(delta, uvs[0], centerU);
    uvs[2] = unlerp(delta, uvs[2], centerU);
    uvs[1] = unlerp(delta, uvs[1], centerV);
    uvs[3] = unlerp(delta, uvs[3], centerV);
    return uvs;
  }

  private boolean isPixelTransparenthaha(TextureAtlasSprite sprite, int frameIndex, int pixelX, int pixelY) {
    int[] pixelData = sprite.getFrameTextureData(0)[frameIndex];
    int color = pixelData[pixelY * sprite.getIconWidth() + pixelX];
    return (color >> 24 & 255) == 0;
  }

  private List<BlockPart> createOutlineLayerElements(int layer, String key, TextureAtlasSprite sprite) {
    List<BlockPart> elements = new ArrayList<>();

    int width = sprite.getIconWidth();
    int height = sprite.getIconHeight();
    float f = (float) sprite.getIconWidth() / (sprite.getMaxU() - sprite.getMinU());
    float f1 = (float) sprite.getIconHeight() / (sprite.getMaxV() - sprite.getMinV());
    float shrinkratio = (float) (4.0 / Math.max(f1, f));
    float animationFrameDelta = shrinkratio;
    float xFactor = width / 16.0F;
    float yFactor = height / 16.0F;

    Map<EnumFacing, BlockPartFace> map = new HashMap<>();
    map.put(EnumFacing.SOUTH, new BlockPartFace(null, layer, key,
      createUnlerpedTexture(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0, animationFrameDelta)));
    map.put(EnumFacing.NORTH, new BlockPartFace(null, layer, key,
      createUnlerpedTexture(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0, animationFrameDelta)));
    elements.add(new BlockPart(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map, null, true));

    int first1 = -1;
    int first2 = -1;
    int last1 = -1;
    int last2 = -1;
    for (int frame = 0; frame < sprite.getFrameCount(); ++frame) {
      for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
          if (!isPixelTransparent(sprite, frame, x, y)) {
            if (isPixelTransparent(sprite, frame, x, y + 1)) { // DOWN
              if (first1 == -1) {
                first1 = x;
              }
              last1 = x;
            }
            if (isPixelTransparent(sprite, frame, x, y - 1)) { // UP
              if (first2 == -1) {
                first2 = x;
              }
              last2 = x;
            }
          } else {
            if (first1 != -1) {
              elements.add(createHorizontalOutlineElement(EnumFacing.DOWN, layer, key, first1, last1, y,
                height, animationFrameDelta, xFactor, yFactor));
              first1 = -1;
            }
            if (first2 != -1) {
              elements.add(createHorizontalOutlineElement(EnumFacing.UP, layer, key, first2, last2, y,
                height, animationFrameDelta, xFactor, yFactor));
              first2 = -1;
            }
          }
        }

        if (first1 != -1) {
          elements.add(createHorizontalOutlineElement(EnumFacing.DOWN, layer, key, first1, last1, y, height,
            animationFrameDelta, xFactor, yFactor));
          first1 = -1;
        }
        if (first2 != -1) {
          elements.add(createHorizontalOutlineElement(EnumFacing.UP, layer, key, first2, last2, y, height,
            animationFrameDelta, xFactor, yFactor));
          first2 = -1;
        }
      }

      for (int x = 0; x < width; ++x) {
        for (int y = 0; y < height; ++y) {
          if (!isPixelTransparent(sprite, frame, x, y)) {
            if (isPixelTransparent(sprite, frame, x + 1, y)) { // EAST
              if (first1 == -1) {
                first1 = y;
              }
              last1 = y;
            }
            if (isPixelTransparent(sprite, frame, x - 1, y)) { // WEST
              if (first2 == -1) {
                first2 = y;
              }
              last2 = y;
            }
          } else {
            if (first1 != -1) {
              elements.add(createVerticalOutlineElement(EnumFacing.EAST, layer, key, first1, last1, x,
                height, animationFrameDelta, xFactor, yFactor));
              first1 = -1;
            }
            if (first2 != -1) {
              elements.add(createVerticalOutlineElement(EnumFacing.WEST, layer, key, first2, last2, x,
                height, animationFrameDelta, xFactor, yFactor));
              first2 = -1;
            }
          }
        }

        if (first1 != -1) {
          elements.add(createVerticalOutlineElement(EnumFacing.EAST, layer, key, first1, last1, x, height,
            animationFrameDelta, xFactor, yFactor));
          first1 = -1;
        }
        if (first2 != -1) {
          elements.add(createVerticalOutlineElement(EnumFacing.WEST, layer, key, first2, last2, x, height,
            animationFrameDelta, xFactor, yFactor));
          first2 = -1;
        }
      }
    }

    return elements;
  }

  private BlockPart createHorizontalOutlineElement(EnumFacing direction, int layer, String key, int start,
                                                   int end, int y, int height, float animationFrameDelta, float xFactor, float yFactor) {
    Map<EnumFacing, BlockPartFace> faces = new HashMap<>();
    faces.put(direction,
      new BlockPartFace(null, layer, key,
        createUnlerpedTexture(
          new float[]{start / xFactor, y / yFactor, (end + 1) / xFactor, (y + 1) / yFactor}, 0,
          animationFrameDelta)));
    return new BlockPart(new Vector3f(start / xFactor, (height - (y + 1)) / yFactor, 7.5F),
      new Vector3f((end + 1) / xFactor, (height - y) / yFactor, 8.5F), faces, null, true);
  }

  private BlockPart createVerticalOutlineElement(EnumFacing direction, int layer, String key, int start, int end,
                                                 int x, int height, float animationFrameDelta, float xFactor, float yFactor) {
    Map<EnumFacing, BlockPartFace> faces = new HashMap<>();
    faces.put(direction,
      new BlockPartFace(null, layer, key,
        createUnlerpedTexture(
          new float[]{(x + 1) / xFactor, start / yFactor, x / xFactor, (end + 1) / yFactor}, 0,
          animationFrameDelta)));
    return new BlockPart(new Vector3f(x / xFactor, (height - (end + 1)) / yFactor, 7.5F),
      new Vector3f((x + 1) / xFactor, (height - start) / yFactor, 8.5F), faces, null, true);
  }


  private BlockFaceUV createUnlerpedTexture(float[] uvs, int rotation, float delta) {
    return new BlockFaceUV(unlerpUVs(uvs, delta), rotation);
  }

  private boolean isPixelTransparent(TextureAtlasSprite sprite, int frame, int x, int y) {
    return x < 0 || y < 0 || x >= sprite.getIconWidth() || y >= sprite.getIconHeight() || isPixelTransparenthaha(sprite, frame, x, y);
  }

  private List<BlockPart> createPixelLayerElements(int layer, String key, TextureAtlasSprite sprite) {
    List<BlockPart> elements = new ArrayList<>();

    int width = sprite.getIconWidth();
    int height = sprite.getIconHeight();
    float xFactor = width / 16.0F;
    float yFactor = height / 16.0F;

    for (int frame = 0; frame < sprite.getFrameCount(); ++frame) {
      for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
          if (!isPixelTransparent(sprite, frame, x, y)) {
            Map<EnumFacing, BlockPartFace> faces = new HashMap<>();
            BlockPartFace face = new BlockPartFace(null, layer, key, new BlockFaceUV(
              new float[]{x / xFactor, y / yFactor, (x + 1) / xFactor, (y + 1) / yFactor}, 0));
            BlockPartFace flippedFace = new BlockPartFace(null, layer, key, new BlockFaceUV(
              new float[]{(x + 1) / xFactor, y / yFactor, x / xFactor, (y + 1) / yFactor}, 0));

            faces.put(EnumFacing.SOUTH, face);
            faces.put(EnumFacing.NORTH, flippedFace);
            if (isPixelTransparent(sprite, frame, x + 1, y)) {
              faces.put(EnumFacing.EAST, flippedFace);
            }
            if (isPixelTransparent(sprite, frame, x - 1, y)) {
              faces.put(EnumFacing.WEST, flippedFace);
            }
            if (isPixelTransparent(sprite, frame, x, y + 1)) {
              faces.put(EnumFacing.DOWN, face);
            }
            if (isPixelTransparent(sprite, frame, x, y - 1)) {
              faces.put(EnumFacing.UP, face);
            }

            elements.add(new BlockPart(new Vector3f(x / xFactor, (height - (y + 1)) / yFactor, 7.5F),
              new Vector3f((x + 1) / xFactor, (height - y) / yFactor, 8.5F), faces, null, true));
          }
        }
      }
    }

    return elements;
  }

  @Inject(at = @At(value = "HEAD"), method = "getBlockParts(ILjava/lang/String;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)Ljava/util/List;", cancellable = true)
  private void onHeadAddLayerElements(
    int layer,
    String key,
    TextureAtlasSprite sprite,
    CallbackInfoReturnable<List<BlockPart>> cir
  ) {
//    if (Keyboard.isKeyDown(Keyboard.KEY_K))
//    cir.setReturnValue(createOutlineLayerElements(layer, key, sprite));
  }

  private void unlerpElements(List<BlockPart> elements, float delta) {
    for (BlockPart element : elements) {
      for (BlockPartFace face : element.mapFaces.values()) {
        unlerpUVs(face.blockFaceUV.uvs, delta);
      }
    }
  }

  @Inject(at = @At(value = "TAIL"), method = "getBlockParts(ILjava/lang/String;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)Ljava/util/List;", locals = LocalCapture.CAPTURE_FAILHARD)
  private void onTailAddLayerElements(int layer, String key, TextureAtlasSprite sprite,
                                      CallbackInfoReturnable<List<BlockPart>> cir, Map<EnumFacing, BlockPartFace> map, List<BlockPart> list) {
    if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
      float f = (float) sprite.getIconWidth() / (sprite.getMaxU() - sprite.getMinU());
      float f1 = (float) sprite.getIconHeight() / (sprite.getMaxV() - sprite.getMinV());
      float shrinkratio = (float) (4.0 / Math.max(f1, f));
//      unlerpElements(list, shrinkratio);
    }
  }
}
