package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketRequestConfigSync;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;

public final class ConfigEventHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            BuildingGadgets.logger.info("Sending SyncedConfig to freshly logged in player {}.", event.player.getDisplayName()); //TODO Check if correct method
            SyncedConfig.sendConfigUpdateTo((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public static void onConfigurationChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(BuildingGadgets.MODID)) {
            BuildingGadgets.logger.info("Configuration changed. Syncing config File.");
            //ConfigManager.sync(BuildingGadgets.MODID, Type.INSTANCE); //TODO Check for replacement
            PacketHandler.INSTANCE.sendToServer(new PacketRequestConfigSync());
        }
    }
}
