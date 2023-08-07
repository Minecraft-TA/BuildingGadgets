package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.function.Predicate;

/**
 * A one stop shop for all your icon screens related needs. We support colors,
 * icons, selected and deselected states, sound and loads more. Come on
 * down!
 */
public class GuiButtonActionable extends GuiButton {
    private Predicate<Boolean> action;
    private boolean selected;
    private boolean isSelectable;

    private Color selectedColor = Color.GREEN;
    private Color deselectedColor = new Color(255, 255, 255);
    private Color activeColor;

    private ResourceLocation selectedTexture;
    private ResourceLocation deselectedTexture;

    private float alpha = 1f;

    public GuiButtonActionable(int x, int y, String texture, String message, boolean isSelectable, Predicate<Boolean> action) {
        super(0, x, y, 25, 25, message);
        this.activeColor = deselectedColor;
        this.isSelectable = isSelectable;
        this.action = action;

        this.setSelected(action.test(false));

        // Set the selected and deselected textures.
        String assetLocation = "textures/gui/setting/%s.png";

        this.deselectedTexture = new ResourceLocation(BuildingGadgets.MODID, String.format(assetLocation, texture));
        this.selectedTexture = !isSelectable ? this.deselectedTexture : new ResourceLocation(BuildingGadgets.MODID, String.format(assetLocation, texture + "_selected"));
    }

    /**
     * If yo do not need to be able to select / toggle something then use this constructor as
     * you'll hit missing texture issues if you don't have an active (_selected) texture.
     */
    public GuiButtonActionable(int x, int y, String texture, String message, Predicate<Boolean> action) {
        this(x, y, texture, message, false, action);
    }

    public void setFaded(boolean faded) {
        alpha = faded ? .6f : 1f;
    }

    /**
     * This should be used when ever changing select.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        this.activeColor = selected ? selectedColor : deselectedColor;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(ModSounds.BEEP.getSound(), selected ? .6F : 1F));
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        super.mousePressed(mc, mouseX, mouseY);

        if (!(mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height))
            return false;

        this.action.test(true);
        if (!this.isSelectable)
            return false;

        this.setSelected(!this.selected);
        return true;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!visible)
            return;

        GL11.enableBlend();
        GL11.tryglBlendFuncSeparate(GL11.SourceFactor.SRC_ALPHA, GL11.DestFactor.ONE_MINUS_SRC_ALPHA, GL11.SourceFactor.ONE, GL11.DestFactor.ZERO);
        GL11.glBlendFunc(GL11.SourceFactor.SRC_ALPHA, GL11.DestFactor.ONE_MINUS_SRC_ALPHA);

        GL11.disableTexture2D();
        GL11.color(activeColor.getRed() / 255f, activeColor.getGreen() / 255f, activeColor.getBlue() / 255f, .15f);
//        blit(this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        drawTexturedModalRect(this.x, this.y, 0, 0, this.width, this.height);
        GL11.enableTexture2D();

        GL11.color(1, 1, 1, alpha);
        Minecraft.getMinecraft().getTextureManager().bindTexture(selected ? selectedTexture : deselectedTexture);
//        blit(this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        drawModalRectWithCustomSizedTexture(this.xPosition, this.yPosition, 0, 0, this.width, this.height, this.width, this.height);

        ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        if (mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height)
            drawString(Minecraft.getMinecraft().fontRenderer, this.displayString, mouseX > (scaledresolution.getScaledWidth() / 2) ? mouseX + 2 : mouseX - Minecraft.getMinecraft().fontRenderer.getStringWidth(this.displayString), mouseY - 10, activeColor.getRGB());
    }
}
