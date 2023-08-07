package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

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
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GL11.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            GL11.enableBlend();
            GL11.tryglBlendFuncSeparate(GL11.SourceFactor.SRC_ALPHA, GL11.DestFactor.ONE_MINUS_SRC_ALPHA, GL11.SourceFactor.ONE, GL11.DestFactor.ZERO);
            GL11.glBlendFunc(GL11.SourceFactor.SRC_ALPHA, GL11.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);


            int bottomToDraw = 2;
            this.drawTexturedModalRect(this.x, this.y + this.height - bottomToDraw, 0, 66 - bottomToDraw + i * 20, this.width / 2, bottomToDraw);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y + this.height - bottomToDraw, 200 - this.width / 2, 66 - bottomToDraw + i * 20, this.width / 2, bottomToDraw);

            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (packedFGColour != 0) {
                j = packedFGColour;
            } else if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
        }
    }
}
