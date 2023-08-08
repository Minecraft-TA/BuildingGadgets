package com.direwolf20.buildinggadgets.common.blocks;

import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class EffectBlock extends BlockModBase {

    public EffectBlock() {
        super(EffectBlockMaterial.EFFECTBLOCKMATERIAL, 20F, "effectblock");
        setCreativeTab(null);
    }

    @Override
    public int getRenderType() {
        return -1; // TODO check if this is invisible
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    /*@Override TODO is this needed?
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }*/

    @Override
    public boolean isNormalCube(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return null;
    }

    @Override
    public int getMobilityFlag() {
        return 2; // TODO check if this is correct
    }

}
