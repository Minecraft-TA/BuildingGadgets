package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.backport.IBlockState;
import com.direwolf20.buildinggadgets.backport.BlockPos;
import net.minecraft.world.World;

/**
 * Indicates the position and type of block to use for placing something in a {@link World}.
 */
public final class PlacementTarget {

    private final IBlockState state;
    private final BlockPos pos;

    public PlacementTarget(IBlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
    }

    public IBlockState getState() {
        return state;
    }

    public BlockPos getPos() {
        return pos;
    }

    /**
     * Sets the block state of the {@link #getPos()} to {@link #getState()}.
     *
     * @param world the world to place block
     */
    public void placeIn(World world) {
        world.setBlockState(pos, state);
    }

}
