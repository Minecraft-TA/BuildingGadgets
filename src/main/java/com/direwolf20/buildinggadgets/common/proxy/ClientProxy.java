package com.direwolf20.buildinggadgets.common.proxy;

import com.direwolf20.buildinggadgets.client.events.EventClientTick;
import com.direwolf20.buildinggadgets.client.events.EventKeyInput;
import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new EventClientTick());
        MinecraftForge.EVENT_BUS.register(new EventKeyInput());
        MinecraftForge.EVENT_BUS.register(new EventTooltip());
    }
}
