package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;

public class AnvilRepairHandler {
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (SyncedConfig.poweredByFE && (event.left.getItem() instanceof GadgetGeneric) && (event.right.getItem() == Items.diamond)) {
            event.cost = 3;
            event.materialCost = 1;
            ItemStack newItem = event.left.copy();
            newItem.setItemDamage(0);
            event.output = newItem;
        }
    }
}
