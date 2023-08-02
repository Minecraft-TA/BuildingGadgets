package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.tools.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import com.direwolf20.buildinggadgets.common.tools.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FakeRenderWorld implements IBlockAccess {
    private Map<BlockPos, IBlockState> posMap = new HashMap<BlockPos, IBlockState>();
    private IBlockAccess realWorld;


    public void setState(IBlockAccess rWorld, IBlockState setBlock, BlockPos coordinate) {
        this.realWorld = rWorld;
        posMap.put(coordinate, setBlock);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLightBrightnessForSkyBlocks(int p_72802_1_, int p_72802_2_, int p_72802_3_, int p_72802_4_) {
        return realWorld.getLightBrightnessForSkyBlocks(p_72802_1_, p_72802_2_, p_72802_3_, p_72802_4_);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return realWorld.getTileEntity(x, y, z);
    }


    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return posMap.containsKey(pos) ? posMap.get(pos) : realWorld.getBlockState(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        if (posMap.containsKey(pos)) {
            return posMap.get(pos).equals(Blocks.air.getDefaultState());
        }
        return realWorld.isAirBlock(pos);
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
