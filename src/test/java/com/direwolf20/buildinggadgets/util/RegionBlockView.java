package com.direwolf20.buildinggadgets.util;

import com.direwolf20.buildinggadgets.backport.IBlockState;
import com.direwolf20.buildinggadgets.common.building.Region;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;

/**
 * A fake world that has a set of positions "filled" with some type of block and the rest will be air ({@link UniqueBlockState#AIR}).
 * This class should be used for tests that checks for range overflow or need a world for reference.
 *
 * <p>"Region" means the effective range is limited and "View" indicates this class is immutable.</p>
 */
public class RegionBlockView implements IBlockAccess {

    private Region region;
    private IBlockState state;

    public RegionBlockView(Region region, IBlockState state) {
        this.region = region;
        this.state = state;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return region.contains(x, y, z) ? state.getBlock() : UniqueBlockState.AIR.getBlock();
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        return !region.contains(x, y, z);
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int directionIn) {
        // getBlockState(pos).getStrongPower(this, pos, facing); TODO Fix
        return 0;
    }


    /* @Override TODO doesnt exist
    public WorldType getWorldType() {
        return WorldType.DEBUG_ALL_BLOCK_STATES;
    }*/

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        return getBlock(x, y, z).isSideSolid(this, x, y, z, side); // TODO IDKDKDKDKD
    }

    @Deprecated
    @Nullable
    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return null;
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int p_72802_1_, int p_72802_2_, int p_72802_3_, int p_72802_4_) {
        return 0;
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return null;
    }

    // TODO These are new so idk what to do with them
    @Override
    public int getBlockMetadata(int p_72805_1_, int p_72805_2_, int p_72805_3_) {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }
}
