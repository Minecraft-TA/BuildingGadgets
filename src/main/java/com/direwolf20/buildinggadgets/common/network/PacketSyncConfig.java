package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Packet containing a {@link NBTTagCompound} representing the serialized Config Data, to be updated on the Client.
 */
public class PacketSyncConfig implements IMessage {
    private NBTTagCompound tagCompound;
    public PacketSyncConfig() {
        this(new NBTTagCompound());
    }

    public PacketSyncConfig(NBTTagCompound tagCompound) {
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf,tagCompound);
    }

    public NBTTagCompound getTagCompound() {
        return tagCompound;
    }
    /**
     * Client-Side Handler for {@link PacketSyncConfig}
     */
    public static class Handler implements IMessageHandler<PacketSyncConfig,IMessage> {

        @Override
        public IMessage onMessage(PacketSyncConfig message, MessageContext ctx) {
            if (ctx.side!= Side.CLIENT)
                return null;
            Minecraft.getMinecraft().func_152344_a(() -> {
                NBTTagCompound compound = message.getTagCompound();
                BuildingGadgets.logger.info("Received SyncedConfig from Server.");
                SyncedConfig.onReadSynchronisation(compound);
            });
            return null;
        }
    }
}
