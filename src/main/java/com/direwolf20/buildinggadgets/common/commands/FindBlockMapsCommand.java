package com.direwolf20.buildinggadgets.common.commands;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.EnumChatFormatting;

public class FindBlockMapsCommand extends CommandAlterBlockMaps {
    public FindBlockMapsCommand() {
        super("FindBlockMaps", false);
    }

    @Override
    protected String getActionFeedback(NBTTagCompound tagCompound) {
        return EnumChatFormatting.WHITE + tagCompound.getString("owner") + ":" + tagCompound.getString("UUID");
    }

    @Override
    protected String getCompletionFeedback(int counter) {
        return EnumChatFormatting.WHITE + "Found " + counter + " blockmaps in world data.";
    }
}
