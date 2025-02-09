package com.enderio.core.api.client.render;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.RenderUtil;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public interface IWidgetMap {

  int getSize();

  @Nonnull
  ResourceLocation getTexture();

  @OnlyIn(Dist.CLIENT)
  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y);

  @OnlyIn(Dist.CLIENT)
  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, boolean doDraw);

  @OnlyIn(Dist.CLIENT)
  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, boolean doDraw, boolean flipY);

  @OnlyIn(Dist.CLIENT)
  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw);

  @OnlyIn(Dist.CLIENT)
  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw, boolean flipY);

  @OnlyIn(Dist.CLIENT)
  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw);

  @OnlyIn(Dist.CLIENT)
  void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw, boolean flipY);

   class WidgetMapImpl implements IWidgetMap {

    private final int size;
    private final @Nonnull ResourceLocation res;

    public WidgetMapImpl(int size, @Nonnull ResourceLocation res) {
      this.size = size;
      this.res = res;
    }

    @Override
    public int getSize() {
      return size;
    }

    @Override
    public @Nonnull ResourceLocation getTexture() {
      return res;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y) {
      render(matrixStack,widget, x, y, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, boolean doDraw) {
      render(matrixStack,widget, x, y, widget.getWidth(), widget.getHeight(), 0, doDraw);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, boolean doDraw, boolean flipY) {
      render(matrixStack,widget, x, y, widget.getWidth(), widget.getHeight(), 0, doDraw, flipY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw) {
      render(matrixStack,widget, x, y, widget.getWidth(), widget.getHeight(), zLevel, doDraw);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw, boolean flipY) {
      render(matrixStack,widget, x, y, widget.getWidth(), widget.getHeight(), zLevel, doDraw, flipY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw) {
      render(matrixStack,widget, x, y, width, height, zLevel, doDraw, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack matrixStack, @Nonnull IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw, boolean flipY) {
      if (doDraw) {
        Minecraft.getInstance().textureManager.bindTexture(getTexture());
        if (flipY) {
          AbstractGui.blit(matrixStack, (int) x, (int) y, (int)width, (int)height, widget.getX(), (float)(widget.getY()+height), (int) width, (int) -height, getSize(), getSize());
        } else {
          AbstractGui.blit(matrixStack, (int) x, (int) y, (int)zLevel, widget.getX(), widget.getY(), (int) width, (int) height, getSize(), getSize());
        }
        final IWidgetIcon overlay = widget.getOverlay();
        if (overlay != null) {
          overlay.getMap().render(matrixStack, overlay, x, y, width, height, zLevel, false, flipY);
        }
      }
    }
  }
}
