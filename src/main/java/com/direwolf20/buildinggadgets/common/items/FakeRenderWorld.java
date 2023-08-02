package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.tools.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import com.direwolf20.buildinggadgets.common.tools.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FakeRenderWorld implements IBlockAccess {
    private final Map<BlockPos, Block> posMap = new HashMap<>();
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

    @Override
    public int getBlockMetadata(int p_72805_1_, int p_72805_2_, int p_72805_3_) {
        return 0;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return realWorld.getTileEntity(x, y, z);
    }


    @Override
    public Block getBlock(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z); //TODO: Meh
        return posMap.containsKey(pos) ? posMap.get(pos) : realWorld.getBlock(x, y, z);
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (posMap.containsKey(pos)) {
            return posMap.get(pos).equals(Blocks.air);
        }
        return realWorld.isAirBlock(x, y, z);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return realWorld.getBiomeGenForCoords(x, z);
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        return false;
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int directionIn) {
        return 0;
    }



}
