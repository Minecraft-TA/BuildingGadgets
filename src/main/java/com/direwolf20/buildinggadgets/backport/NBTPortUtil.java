package com.direwolf20.buildinggadgets.backport;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public class NBTPortUtil {

    public static NBTTagCompound createPosTag(BlockPos pos) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("x", pos.getX());
        compound.setInteger("y", pos.getY());
        compound.setInteger("z", pos.getZ());
        return compound;
    }

    public static BlockPos readPosTag(NBTTagCompound compound) {
        return new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
    }

    public static void writeBlockState(NBTTagCompound compound, IBlockState state) {
        compound.setInteger("meta", state.getMeta());
        compound.setInteger("id", Block.getIdFromBlock(state.getBlock()));
    }

    public static IBlockState readBlockState(NBTTagCompound compound) {
        return IBlockState.create(Block.getBlockById(compound.getInteger("id")), compound.getInteger("meta"));
    }
}
