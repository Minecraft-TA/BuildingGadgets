package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

public class GuiButtonHelp extends GuiButtonSelect {

    public GuiButtonHelp(int buttonId, int x, int y) {
        super(buttonId, x, y, 12, 12, "?", "");
    }

    public String getHoverText() {
        return IHoverHelpText.get("button." + (selected ? "help.exit" : "help.enter"));
    }


    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible)
            return;

        GL11.glColor4f(1, 1, 1, 1);
        selected = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

        float x = this.xPosition + 5.5F;
        int y = this.yPosition + 6;
        double radius = 6;
        int red, green, blue;
        if (selected) {
            red = blue = 0;
            green = 200;
        } else {
            red = green = blue = 120;
        }
        Tessellator tessellator = Tessellator.instance;
        BufferBuilder buffer = tessellator.getBuffer();
        GL11.enableBlend();
        GL11.disableTexture2D();
        GL11.tryglBlendFuncSeparate(GL11.SourceFactor.SRC_ALPHA,
                GL11.DestFactor.ONE_MINUS_SRC_ALPHA, GL11.SourceFactor.ONE, GL11.DestFactor.ZERO);
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0).color(red, green, blue, 255).endVertex();
        double s = 30;
        for(int k = 0; k <= s; k++)  {
            double angle = (Math.PI * 2 * k / s) + Math.toRadians(180);
            buffer.pos(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).color(red, green, blue, 255).endVertex();
        }
        tessellator.draw();
        GL11.enableTexture2D();
        GL11.disableBlend();

        mouseDragged(mc, mouseX, mouseY);
        int colorText = -1;
        if (packedFGColour != 0)
            colorText = packedFGColour;
        else if (!enabled)
            colorText = 10526880;
        else if (selected)
            colorText = 16777120;

        mc.fontRenderer.drawString(displayString, this.xPosition + width / 2 - mc.fontRenderer.getStringWidth(displayString) / 2, this.yPosition + (height - 8) / 2, colorText);
    }

}
