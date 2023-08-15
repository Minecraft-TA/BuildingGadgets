package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.GuiTextField;

public class GuiUtils {

    public static boolean textFieldMouseClicked(GuiTextField textField, int x, int y, int button) {
        textField.mouseClicked(x, y, button);
        return x >= textField.xPosition && x < textField.xPosition + textField.width && y >= textField.yPosition && y < textField.yPosition + textField.height;
    }
}
