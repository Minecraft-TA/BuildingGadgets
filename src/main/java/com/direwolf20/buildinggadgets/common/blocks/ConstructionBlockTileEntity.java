package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.tools.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class ConstructionBlockTileEntity extends TileEntity {
    private IBlockState blockState;
    private IBlockState actualBlockState;

    public boolean setBlockState(IBlockState state) {
        blockState = state;
        markDirtyClient();
        return true;
    }

    public boolean setActualBlockState(IBlockState state) {
        actualBlockState = state;
        markDirtyClient();
        return true;
    }

    @Nullable
    public IBlockState getBlockState() {
        if (blockState == null || blockState == Blocks.air.getDefaultState()) {
            return null;
        }
        return blockState;
    }

    @Nullable
    public IBlockState getActualBlockState() {
        if (actualBlockState == null || actualBlockState == Blocks.air.getDefaultState()) {
            return null;
        }
        return actualBlockState;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        blockState = NBTUtil.readBlockState(compound.getCompoundTag("blockState"));
        actualBlockState = NBTUtil.readBlockState(compound.getCompoundTag("actualBlockState"));
        markDirtyClient();
    }

    private void markDirtyClient() {
        markDirty();
        if (worldObj != null) {
            IBlockState state = worldObj.getBlockState(getPos());
            worldObj.notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        writeToNBT(updateTag);
        return updateTag;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        IBlockState oldMimicBlock = getBlockState();
        NBTTagCompound tagCompound = packet.func_148857_g();
        super.onDataPacket(net, packet);
        readFromNBT(tagCompound);
        if (worldObj.isRemote) {
            // If needed send a render update.
            if (getBlockState() != oldMimicBlock) {
                worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (blockState != null) {
            NBTTagCompound blockStateTag = new NBTTagCompound();
            NBTTagCompound actualBlockStateTag = new NBTTagCompound();
            if (blockState != null) {
                NBTUtil.writeBlockState(blockStateTag, blockState);
                compound.setTag("blockState", blockStateTag);
            }
            if (actualBlockState != null) {
                NBTUtil.writeBlockState(actualBlockStateTag, actualBlockState);
                compound.setTag("actualBlockState", actualBlockStateTag);
            }
        }
    }
}
