package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.backport.BlockPos;
import com.direwolf20.buildinggadgets.backport.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;
import java.util.Set;

public class FakeBuilderWorld implements IBlockAccess {
    private Set<BlockPos> positions;
    private IBlockState state;
    private World realWorld;
    private final IBlockState AIR = IBlockState.AIR_STATE;


    public void setWorldAndState(World rWorld, IBlockState setBlock, Set<BlockPos> coordinates) {
        this.state = setBlock;
        this.realWorld = rWorld;
        positions = coordinates;
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int p_72802_1_, int p_72802_2_, int p_72802_3_, int p_72802_4_) {
        return realWorld.getLightBrightnessForSkyBlocks(p_72802_1_, p_72802_2_, p_72802_3_, p_72802_4_);
    }

    @Override
    public Block getBlock(int p_147439_1_, int p_147439_2_, int p_147439_3_) {
        return positions.contains(new BlockPos(p_147439_1_, p_147439_2_, p_147439_3_)) ? state.getBlock() : null;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return null;
    }

    @Override
    public int getBlockMetadata(int p_72805_1_, int p_72805_2_, int p_72805_3_) {
        return positions.contains(new BlockPos(p_72805_1_, p_72805_2_, p_72805_3_)) ? state.getMeta() : 0;
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        return !positions.contains(new BlockPos(x, y, z));
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return realWorld.getBiomeGenForCoords(x, z);
    }

    @Override
    public int getHeight() {
        return realWorld.getHeight();
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int directionIn) {
        return 0;
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        return getBlock(x,y,z).isSideSolid(this, x,y,z, side);
    }
}
