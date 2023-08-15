package com.direwolf20.buildinggadgets.backport;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

public class IBlockState {

    public static final IBlockState AIR_STATE = create(Blocks.air, 0);

    private final Block block;
    private final int meta;

    private IBlockState(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    public static IBlockState getStateFromWorld(IBlockAccess world, BlockPos pos) {
        return create(world.getBlock(pos.getX(), pos.getY(), pos.getZ()), world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ()));
    }

    public IBlockState withMeta(int meta) {
        return create(getBlock(), meta);
    }

    public Block getBlock() {
        return block;
    }

    public int getMeta() {
        return meta;
    }

    public IBlockState getDefaultState() {
        return create(getBlock(), 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        IBlockState that = (IBlockState) o;

        if (meta != that.meta)
            return false;
        return block.equals(that.block);
    }

    @Override
    public int hashCode() {
        int result = block.hashCode();
        result = 31 * result + meta;
        return result;
    }

    public static IBlockState create(Block block, int meta) {
        if (block == Blocks.air && meta == 0)
            return AIR_STATE;
        return new IBlockState(block, meta);
    }
}
