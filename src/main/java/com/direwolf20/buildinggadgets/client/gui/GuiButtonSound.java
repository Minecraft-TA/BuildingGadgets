package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

public class GuiButtonSound extends GuiButtonSelect {
    private String soundSelect, soundDeselect;
    private float pitchSelect, pitchDeselect;
    private boolean silent;

    public GuiButtonSound(int buttonId, int x, int y, int width, int height, String text, String helpTextKey) {
        super(buttonId, x, y, width, height, text, helpTextKey);
        pitchSelect = pitchDeselect = 1;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setSounds(String soundSelect, String soundDeselect) {
        setSounds(soundSelect, soundDeselect, 1, 1);
    }

    public void setSounds(String soundSelect, String soundDeselect, float pitchSelect, float pitchDeselect) {
        this.soundSelect = soundSelect;
        this.soundDeselect = soundDeselect;
        this.pitchSelect = pitchSelect;
        this.pitchDeselect = pitchDeselect;
        silent = false;
    }

    @Override
    public void func_146113_a(SoundHandler soundHandler) {
        if (silent)
            return;

        String sound = soundSelect == null ? "gui.button.click" : (selected ? soundDeselect : soundSelect);
        soundHandler.playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(sound), selected ? pitchDeselect : pitchSelect));
    }
}
