package org.nrnr.neverdies.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

import static org.nrnr.neverdies.util.Globals.mc;

public final class RenderUtils {
    public static final VertexConsumerProvider.Immediate bufferSource = mc.getBufferBuilders().getEntityVertexConsumers();
    public static final MatrixStack pose = new MatrixStack();
    public static final DrawContext guiGraphics = new DrawContext(mc, bufferSource);


    public static void drawRect(int x1, int y1, int x2, int y2, int color) {

        Matrix4f matrix4f = pose.peek().getPositionMatrix();

        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            int j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float h = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float j = (float) ColorHelper.Argb.getBlue(color) / 255.0F;
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderLayer.getGui());
        vertexconsumer.vertex(matrix4f, (float)x1, (float)y1, (float)0).color(g, h, j, f).next();
        vertexconsumer.vertex(matrix4f, (float)x1, (float)y2, (float)0).color(g, h, j, f).next();
        vertexconsumer.vertex(matrix4f, (float)x2, (float)y2, (float)0).color(g, h, j, f).next();
        vertexconsumer.vertex(matrix4f, (float)x2, (float)y1, (float)0).color(g, h, j, f).next();
        guiGraphics.draw();
    }

    public static void drawOutlineRect(int x1, int y1, int x2, int y2, int depth, int colour) {
        int outlineX;
        int outlineY;
        outlineX = x1 > x2 ? -depth : depth;
        outlineY = y1 > y2 ? depth : -depth;

        drawRect(x1,y1, x1 + outlineX, y2, colour);
        drawRect(x1 + outlineX, y2, x2, y2 + outlineY, colour);
        drawRect(x2, y2 + outlineY, x2 - outlineX, y1, colour);
        drawRect(x2 - outlineX, y1, x1, y1 - outlineY, colour);
    }

    public static void drawString(String text, int x1, int y1, int color) {
        Text component = Text.of(text);

        guiGraphics.drawText(mc.textRenderer, component, x1, y1, color, true);
    }
}
