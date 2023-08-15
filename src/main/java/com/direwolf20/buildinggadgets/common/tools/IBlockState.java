package com.direwolf20.buildinggadgets.common.tools;

import net.minecraft.world.IBlockAccess;

public class IBlockState {

    private final int meta;

    public IBlockState(int meta) {
        this.meta = meta;
    }

    public static IBlockState getStateFromWorld(IBlockAccess world, BlockPos pos) {
        return new IBlockState(world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ()));
    }

    public int getMeta() {
        return meta;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IBlockState) {
            return ((IBlockState) obj).meta == meta;
        }
        return false;
    }
}
