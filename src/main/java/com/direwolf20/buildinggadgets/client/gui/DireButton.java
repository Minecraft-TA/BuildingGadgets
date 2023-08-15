package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class DireButton extends GuiButtonHelpText {

    public DireButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        this(buttonId, x, y, widthIn, heightIn, buttonText, "");
    }

    public DireButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String helpTextKey) {
        super(buttonId, x, y, widthIn, heightIn, buttonText, helpTextKey);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int i = this.getHoverState(this.field_146123_n);
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO); //TODO Maybe switch to GL11
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);


            int bottomToDraw = 2;
            this.drawTexturedModalRect(this.xPosition, this.yPosition + this.height - bottomToDraw, 0, 66 - bottomToDraw + i * 20, this.width / 2, bottomToDraw);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition + this.height - bottomToDraw, 200 - this.width / 2, 66 - bottomToDraw + i * 20, this.width / 2, bottomToDraw);

            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (packedFGColour != 0) {
                j = packedFGColour;
            } else if (!this.enabled) {
                j = 10526880;
            } else if (this.field_146123_n) {
                j = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }
    }
}
