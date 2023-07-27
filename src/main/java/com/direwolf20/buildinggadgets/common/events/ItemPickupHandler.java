package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class ItemPickupHandler {
    @SubscribeEvent
    public static void GetDrops(EntityItemPickupEvent event) {
        EntityItem entityItem = event.item;
        ItemStack itemStack = entityItem.getEntityItem();
        if (itemStack.getItem() instanceof ConstructionPaste) {
            itemStack = InventoryManipulation.addPasteToContainer(event.entityPlayer, itemStack);
            entityItem.setEntityItemStack(itemStack);
        }
    }
}
