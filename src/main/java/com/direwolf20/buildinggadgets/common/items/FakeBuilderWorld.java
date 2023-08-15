package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.tools.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import com.direwolf20.buildinggadgets.common.tools.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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

    @Nullable
    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return null;
    }



    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return positions.contains(pos) ? state : AIR;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return !positions.contains(pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return realWorld.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return realWorld.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return getBlockState(pos).isSideSolid(this, pos, side);
    }
}
