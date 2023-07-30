package com.direwolf20.buildinggadgets.common.commands;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.EnumChatFormatting;

public class DeleteBlockMapsCommand extends CommandAlterBlockMaps {
    public DeleteBlockMapsCommand() {
        super("DeleteBlockMaps", true);
    }

    @Override
    protected String getActionFeedback(NBTTagCompound tagCompound) {
        return EnumChatFormatting.RED + "Deleted stored map for " + tagCompound.getString("owner") + " with UUID:" + tagCompound.getString("UUID");
    }

    @Override
    protected String getCompletionFeedback(int counter) {
        return EnumChatFormatting.WHITE + "Deleted " + counter + " blockmaps in world data.";
    }
}
