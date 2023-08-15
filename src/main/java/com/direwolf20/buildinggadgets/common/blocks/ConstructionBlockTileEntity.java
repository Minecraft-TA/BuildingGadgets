package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.backport.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
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
        if (blockState == null || blockState == IBlockState.AIR_STATE) {
            return null;
        }
        return blockState;
    }

    @Nullable
    public IBlockState getActualBlockState() {
        if (actualBlockState == null || actualBlockState == IBlockState.AIR_STATE) {
            return null;
        }
        return actualBlockState;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        blockState = NBTPortUtil.readBlockState(compound.getCompoundTag("blockState"));
        actualBlockState = NBTPortUtil.readBlockState(compound.getCompoundTag("actualBlockState"));
        markDirtyClient();
    }

    private void markDirtyClient() {
        markDirty();
        if (worldObj != null) {
            IBlockState state = IBlockState.getStateFromWorld(worldObj, getPos());
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
                NBTPortUtil.writeBlockState(blockStateTag, blockState);
                compound.setTag("blockState", blockStateTag);
            }
            if (actualBlockState != null) {
                NBTPortUtil.writeBlockState(actualBlockStateTag, actualBlockState);
                compound.setTag("actualBlockState", actualBlockStateTag);
            }
        }
    }
}
