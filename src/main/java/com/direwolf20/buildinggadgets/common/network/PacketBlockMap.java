package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.tools.PasteToolBufferBuilder;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class PacketBlockMap implements IMessage {

    private NBTTagCompound tag = new NBTTagCompound();

    @Override
    public void fromBytes(ByteBuf buf) {
        tag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tag);
    }

    public PacketBlockMap() {
    }

    public PacketBlockMap(NBTTagCompound tagCompound) {
        tag = (NBTTagCompound) tagCompound.copy();
    }

    public static class Handler implements IMessageHandler<PacketBlockMap, IMessage> {
        @Override
        public IMessage onMessage(PacketBlockMap message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message));
            return null;
        }

        private void handle(PacketBlockMap message) {
            if (message.tag.equals(new NBTTagCompound())) {
                PasteToolBufferBuilder.clearMaps();
            }
            String UUID = message.tag.getString("UUID");
            PasteToolBufferBuilder.addToMap(UUID, message.tag);
            PasteToolBufferBuilder.addMapToBuffer(UUID);
            //System.out.println("Sent blockmap for: " + UUID);
        }
    }
}
