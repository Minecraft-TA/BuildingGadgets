package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.items.ItemModBase;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ConstructionPaste extends ItemModBase {
    public ConstructionPaste() {
        super("constructionpaste");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World worldIn, EntityPlayer player) { //TODO: Check if this is correct
        itemStack = InventoryManipulation.addPasteToContainer(player, itemStack);
        return itemStack;
    }

}
