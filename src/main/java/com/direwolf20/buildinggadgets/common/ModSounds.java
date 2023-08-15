package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.client.proxy.ClientProxy;
import net.minecraft.util.ResourceLocation;

public enum ModSounds {
    BEEP("beep");

    private final ResourceLocation location;

    ModSounds(String name) {
        location = new ResourceLocation(BuildingGadgets.MODID, name);
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void playSound() {
        playSound(1.0F);
    }

    public void playSound(float pitch) {
        ClientProxy.playSound(getLocation().toString(), pitch);
    }
}
