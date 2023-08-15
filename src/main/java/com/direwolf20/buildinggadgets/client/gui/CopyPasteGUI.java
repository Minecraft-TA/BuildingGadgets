/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.network.PacketCopyCoords;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tools.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class CopyPasteGUI extends GuiScreen {
//     public static final int WIDTH = 256;
//     public static final int HEIGHT = 256;

    private GuiTextField startX;
    private GuiTextField startY;
    private GuiTextField startZ;
    private GuiTextField endX;
    private GuiTextField endY;
    private GuiTextField endZ;

    private boolean absoluteCoords = SyncedConfig.absoluteCoordDefault && SyncedConfig.allowAbsoluteCoords;

    private int guiLeft = 15;
    private int guiTop = 15;

    private ItemStack copyPasteTool;
    private BlockPos startPos;
    private BlockPos endPos;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");

    public CopyPasteGUI(ItemStack tool) {
        super();
        this.copyPasteTool = tool;
    }

    /*@Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }*/

    @Override
    public void initGui() {
        super.initGui();
        startPos = ModItems.gadgetCopyPaste.getStartPos(copyPasteTool);
        endPos = ModItems.gadgetCopyPaste.getEndPos(copyPasteTool);
        if (startPos == null) startPos = new BlockPos(0, 0, 0);
        if (endPos == null) endPos = new BlockPos(0, 0, 0);

        startX = new GuiTextField(this.fontRendererObj, this.guiLeft + 65, this.guiTop + 15, 40, this.fontRendererObj.FONT_HEIGHT);
        startX.setMaxStringLength(50);
        startX.setVisible(true);

        startY = new GuiTextField(this.fontRendererObj, this.guiLeft + 165, this.guiTop + 15, 40, this.fontRendererObj.FONT_HEIGHT);
        startY.setMaxStringLength(50);
        startY.setVisible(true);

        startZ = new GuiTextField(this.fontRendererObj, this.guiLeft + 265, this.guiTop + 15, 40, this.fontRendererObj.FONT_HEIGHT);
        startZ.setMaxStringLength(50);
        startZ.setVisible(true);


        endX = new GuiTextField(this.fontRendererObj, this.guiLeft + 65, this.guiTop + 35, 40, this.fontRendererObj.FONT_HEIGHT);
        endX.setMaxStringLength(50);
        endX.setVisible(true);

        endY = new GuiTextField(this.fontRendererObj, this.guiLeft + 165, this.guiTop + 35, 40, this.fontRendererObj.FONT_HEIGHT);
        endY.setMaxStringLength(50);
        endY.setVisible(true);

        endZ = new GuiTextField(this.fontRendererObj, this.guiLeft + 265, this.guiTop + 35, 40, this.fontRendererObj.FONT_HEIGHT);
        endZ.setMaxStringLength(50);
        endZ.setVisible(true);

        updateTextFields();
        //NOTE: the id always has to be different or else it might get called twice or never!
        this.buttonList.add(new GuiButton(1, this.guiLeft + 45, this.guiTop + 60, 40, 20, "Ok"));
        this.buttonList.add(new GuiButton(2, this.guiLeft + 145, this.guiTop + 60, 40, 20, "Cancel"));
        this.buttonList.add(new GuiButton(3, this.guiLeft + 245, this.guiTop + 60, 40, 20, "Clear"));

        if( SyncedConfig.allowAbsoluteCoords )
            this.buttonList.add(new GuiButton(4, this.guiLeft + 325, this.guiTop + 60, 80, 20, "CoordsMode"));

        this.buttonList.add(new DireButton(5, this.guiLeft + 50, this.guiTop + 14, 10, 10, "-"));
        this.buttonList.add(new DireButton(6, this.guiLeft + 110, this.guiTop + 14, 10, 10, "+"));
        this.buttonList.add(new DireButton(7, this.guiLeft + 150, this.guiTop + 14, 10, 10, "-"));
        this.buttonList.add(new DireButton(8, this.guiLeft + 210, this.guiTop + 14, 10, 10, "+"));
        this.buttonList.add(new DireButton(9, this.guiLeft + 250, this.guiTop + 14, 10, 10, "-"));
        this.buttonList.add(new DireButton(10, this.guiLeft + 310, this.guiTop + 14, 10, 10, "+"));
        this.buttonList.add(new DireButton(11, this.guiLeft + 50, this.guiTop + 34, 10, 10, "-"));
        this.buttonList.add(new DireButton(12, this.guiLeft + 110, this.guiTop + 34, 10, 10, "+"));
        this.buttonList.add(new DireButton(13, this.guiLeft + 150, this.guiTop + 34, 10, 10, "-"));
        this.buttonList.add(new DireButton(14, this.guiLeft + 210, this.guiTop + 34, 10, 10, "+"));
        this.buttonList.add(new DireButton(15, this.guiLeft + 250, this.guiTop + 34, 10, 10, "-"));
        this.buttonList.add(new DireButton(16, this.guiLeft + 310, this.guiTop + 34, 10, 10, "+"));
    }

    private void fieldChange(GuiTextField textField, int amount) {
        nullCheckTextBoxes();
        if (GuiScreen.isShiftKeyDown()) amount = amount * 10;
        try {
            int i = Integer.valueOf(textField.getText());
            i = i + amount;
            textField.setText(String.valueOf(i));
        } catch (Throwable t) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        //drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        this.startX.drawTextBox();
        this.startY.drawTextBox();
        this.startZ.drawTextBox();
        this.endX.drawTextBox();
        this.endY.drawTextBox();
        this.endZ.drawTextBox();
        fontRendererObj.drawStringWithShadow("Start X", this.guiLeft, this.guiTop + 15, 0xFFFFFF);
        fontRendererObj.drawStringWithShadow("Y", this.guiLeft + 131, this.guiTop + 15, 0xFFFFFF);
        fontRendererObj.drawStringWithShadow("Z", this.guiLeft + 231, this.guiTop + 15, 0xFFFFFF);
        fontRendererObj.drawStringWithShadow("End X", this.guiLeft + 8, this.guiTop + 35, 0xFFFFFF);
        fontRendererObj.drawStringWithShadow("Y", this.guiLeft + 131, this.guiTop + 35, 0xFFFFFF);
        fontRendererObj.drawStringWithShadow("Z", this.guiLeft + 231, this.guiTop + 35, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void nullCheckTextBoxes() {
        if (absoluteCoords) {
            if (startX.getText().isEmpty()) {
                startX.setText(String.valueOf(startPos.getX()));
            }
            if (startY.getText().isEmpty()) {
                startY.setText(String.valueOf(startPos.getY()));
            }
            if (startZ.getText().isEmpty()) {
                startZ.setText(String.valueOf(startPos.getZ()));
            }
            if (endX.getText().isEmpty()) {
                endX.setText(String.valueOf(endPos.getX()));
            }
            if (endY.getText().isEmpty()) {
                endY.setText(String.valueOf(endPos.getY()));
            }
            if (endZ.getText().isEmpty()) {
                endZ.setText(String.valueOf(endPos.getZ()));
            }
        } else {
            if (startX.getText().isEmpty()) {
                startX.setText("0");
            }
            if (startY.getText().isEmpty()) {
                startY.setText("0");
            }
            if (startZ.getText().isEmpty()) {
                startZ.setText("0");
            }
            if (endX.getText().isEmpty()) {
                endX.setText("0");
            }
            if (endY.getText().isEmpty()) {
                endY.setText("0");
            }
            if (endZ.getText().isEmpty()) {
                endZ.setText("0");
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        if (b.id == 1) {
            nullCheckTextBoxes();
            try {
                if (absoluteCoords) {
                    startPos = new BlockPos(Integer.parseInt(startX.getText()), Integer.parseInt(startY.getText()), Integer.parseInt(startZ.getText()));
                    endPos = new BlockPos(Integer.parseInt(endX.getText()), Integer.parseInt(endY.getText()), Integer.parseInt(endZ.getText()));
                } else {
                    startPos = new BlockPos(startPos.getX() + Integer.parseInt(startX.getText()), startPos.getY() + Integer.parseInt(startY.getText()), startPos.getZ() + Integer.parseInt(startZ.getText()));
                    endPos = new BlockPos(startPos.getX() + Integer.parseInt(endX.getText()), startPos.getY() + Integer.parseInt(endY.getText()), startPos.getZ() + Integer.parseInt(endZ.getText()));
                }
                PacketHandler.INSTANCE.sendToServer(new PacketCopyCoords(startPos, endPos));
            } catch (Throwable t) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + new ChatComponentTranslation("message.gadget.copyguierror").getUnformattedTextForChat()));
            }
            this.mc.displayGuiScreen(null);
        } else if (b.id == 2) {
            this.mc.displayGuiScreen(null);
        } else if (b.id == 3) {
            PacketHandler.INSTANCE.sendToServer(new PacketCopyCoords(BlockPos.ORIGIN, BlockPos.ORIGIN));
            this.mc.displayGuiScreen(null);
        } else if (b.id == 4) {
            coordsModeSwitch();
            updateTextFields();
        } else if (b.id == 5) {
            fieldChange(startX, -1);
        } else if (b.id == 6) {
            fieldChange(startX, 1);
        } else if (b.id == 7) {
            fieldChange(startY, -1);
        } else if (b.id == 8) {
            fieldChange(startY, 1);
        } else if (b.id == 9) {
            fieldChange(startZ, -1);
        } else if (b.id == 10) {
            fieldChange(startZ, 1);
        } else if (b.id == 11) {
            fieldChange(endX, -1);
        } else if (b.id == 12) {
            fieldChange(endX, 1);
        } else if (b.id == 13) {
            fieldChange(endY, -1);
        } else if (b.id == 14) {
            fieldChange(endY, 1);
        } else if (b.id == 15) {
            fieldChange(endZ, -1);
        } else if (b.id == 16) {
            fieldChange(endZ, 1);
        }

    }

    private void coordsModeSwitch() {
        absoluteCoords = !absoluteCoords;
    }

    private void updateTextFields() {
        String x, y, z;
        if (absoluteCoords) {
            BlockPos start = !"".equals(startX.getText()) ? new BlockPos(startPos.getX() + Integer.parseInt(startX.getText()), startPos.getY() + Integer.parseInt(startY.getText()), startPos.getZ() + Integer.parseInt(startZ.getText())) : startPos;
            BlockPos end = !"".equals(endX.getText()) ? new BlockPos(startPos.getX() + Integer.parseInt(endX.getText()), startPos.getY() + Integer.parseInt(endY.getText()), startPos.getZ() + Integer.parseInt(endZ.getText())) : endPos;
            startX.setText(String.valueOf(start.getX()));
            startY.setText(String.valueOf(start.getY()));
            startZ.setText(String.valueOf(start.getZ()));
            endX.setText(String.valueOf(end.getX()));
            endY.setText(String.valueOf(end.getY()));
            endZ.setText(String.valueOf(end.getZ()));
        } else {
            x = !"".equals(startX.getText()) ? String.valueOf(Integer.parseInt(startX.getText()) - startPos.getX()) : "0";
            startX.setText(x);
            y = !"".equals(startY.getText()) ? String.valueOf(Integer.parseInt(startY.getText()) - startPos.getY()) : "0";
            startY.setText(y);
            z = !"".equals(startZ.getText()) ? String.valueOf(Integer.parseInt(startZ.getText()) - startPos.getZ()) : "0";
            startZ.setText(z);
            x = !"".equals(endX.getText()) ? String.valueOf(Integer.parseInt(endX.getText()) - startPos.getX()) : String.valueOf(endPos.getX() - startPos.getX());
            endX.setText(x);
            y = !"".equals(endY.getText()) ? String.valueOf(Integer.parseInt(endY.getText()) - startPos.getY()) : String.valueOf(endPos.getY() - startPos.getY());
            endY.setText(y);
            z = !"".equals(endZ.getText()) ? String.valueOf(Integer.parseInt(endZ.getText()) - startPos.getZ()) : String.valueOf(endPos.getZ() - startPos.getZ());
            endZ.setText(z);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.startX.textboxKeyTyped(typedChar, keyCode) || this.startY.textboxKeyTyped(typedChar, keyCode) || this.startZ.textboxKeyTyped(typedChar, keyCode) || this.endX.textboxKeyTyped(typedChar, keyCode) || this.endY.textboxKeyTyped(typedChar, keyCode) || this.endZ.textboxKeyTyped(typedChar, keyCode)) {

        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 1) {
            if (GuiUtils.textFieldMouseClicked(startX, mouseX, mouseY, 0)) {
                startX.setText("");
            } else if (GuiUtils.textFieldMouseClicked(startY, mouseX, mouseY, 0)) {
                startY.setText("");
            } else if (GuiUtils.textFieldMouseClicked(startZ, mouseX, mouseY, 0)) {
                startZ.setText("");
            } else if (GuiUtils.textFieldMouseClicked(endX, mouseX, mouseY, 0)) {
                endX.setText("");
            } else if (GuiUtils.textFieldMouseClicked(endY, mouseX, mouseY, 0)) {
                endY.setText("");
            } else if (GuiUtils.textFieldMouseClicked(endZ, mouseX, mouseY, 0)) {
                endZ.setText("");
            } else {
                //startX.setFocused(false);
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        } else {
            if (GuiUtils.textFieldMouseClicked(startX, mouseX, mouseY, mouseButton)) {
                startX.setFocused(true);
            } else if (GuiUtils.textFieldMouseClicked(startY, mouseX, mouseY, mouseButton)) {
                startY.setFocused(true);
            } else if (GuiUtils.textFieldMouseClicked(startZ, mouseX, mouseY, mouseButton)) {
                startZ.setFocused(true);
            } else if (GuiUtils.textFieldMouseClicked(endX, mouseX, mouseY, mouseButton)) {
                endX.setFocused(true);
            } else if (GuiUtils.textFieldMouseClicked(endY, mouseX, mouseY, mouseButton)) {
                endY.setFocused(true);
            } else if (GuiUtils.textFieldMouseClicked(endZ, mouseX, mouseY, mouseButton)) {
                endZ.setFocused(true);
            } else {
                //startX.setFocused(false);
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
