package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.client.gui.materiallist.MaterialListGUI;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import com.direwolf20.buildinggadgets.common.tools.BlockPos;
import net.minecraft.world.World;

public class GuiProxy implements IGuiHandler {

    public static final int CopyPasteID = 0;
    public static final int DestructionID = 1;
    public static final int PasteID = 2;
    public static final int MaterialListID = 3;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
        if (te instanceof TemplateManagerTileEntity) {
            return new TemplateManagerContainer(player.inventory, (TemplateManagerTileEntity) te);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TemplateManagerTileEntity) {
            TemplateManagerTileEntity containerTileEntity = (TemplateManagerTileEntity) te;
            return new TemplateManagerGUI(containerTileEntity, new TemplateManagerContainer(player.inventory, containerTileEntity));
        }
        if (ID == CopyPasteID) {
            if (player.getHeldItem().getItem() instanceof GadgetCopyPaste)
                return new CopyPasteGUI(player.getHeldItem());
            else
                return null;
        } else if (ID == DestructionID) {
            if (player.getHeldItem().getItem() instanceof GadgetDestruction)
                return new DestructionGUI(player.getHeldItem());
            else
                return null;
        } else if (ID == PasteID) {
            if (player.getHeldItem().getItem() instanceof GadgetCopyPaste)
                return new PasteGUI(player.getHeldItem());
            else
                return null;
        } else if (ID == MaterialListID) {
            ItemStack template = InventoryManipulation.getStackInEitherHand(player, ITemplate.class);
            if (template != null)
                return new MaterialListGUI(template);
            return null;
        }
        return null;
    }

}
