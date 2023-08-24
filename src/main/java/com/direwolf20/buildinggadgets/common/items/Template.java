package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.gui.GuiProxy;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.WorldSave;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.EnumChatFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class Template extends ItemModBase implements ITemplate {
    public Template() {
        super("template");
        setMaxStackSize(1);
    }

    @Override
    public WorldSave getWorldSave(World world) {
        return WorldSave.getTemplateWorldSave(world);
    }

    @Override
    @Nullable
    public String getUUID(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        String uuid = tagCompound.getString("UUID");
        if (uuid.isEmpty()) {
            UUID uid = UUID.randomUUID();
            tagCompound.setString("UUID", uid.toString());
            stack.setTagCompound(tagCompound);
            uuid = uid.toString();
        }
        return uuid;
    }

    public static void setName(ItemStack stack, String name) {
        GadgetUtils.writeStringToNBT(stack, name, "TemplateName");
    }

    public static String getName(ItemStack stack) {
        return GadgetUtils.getStringFromNBT(stack, "TemplateName");
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean b) {
        //Add tool information to the tooltip
        super.addInformation(stack, player, list, b);
        list.add(EnumChatFormatting.AQUA + I18n.format("tooltip.template.name") + ": " + getName(stack));
        EventTooltip.addTemplatePadding(stack, list);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int p_77648_4_, int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        onItemRightClick(stack, world, player);
        return super.onItemUse(stack, player, world, p_77648_4_, p_77648_5_, p_77648_6_, p_77648_7_, p_77648_8_, p_77648_9_, p_77648_10_);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(BuildingGadgets.instance, GuiProxy.MaterialListID, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return itemStack; //TODO: Check if this is correct
    }

}
