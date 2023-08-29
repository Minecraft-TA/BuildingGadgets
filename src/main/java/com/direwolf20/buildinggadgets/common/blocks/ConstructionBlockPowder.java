package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import com.direwolf20.buildinggadgets.backport.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import com.direwolf20.buildinggadgets.backport.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ConstructionBlockPowder extends BlockFalling {

    public ConstructionBlockPowder() {
        super(Material.sand);
        BlockModBase.init(this, 0.5F, "constructionblockpowder");
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        BlockModBase.initModel(this);
    }

    /*@Override TODO: Doesnt exist?
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list) {
        super.addInformation(stack, world, list, b);
        list.add(I18n.format("tooltip.constructionblockpowder.helptext"));
    }*/

  /*  @Override
    public void onEndFalling(World worldIn, BlockPos pos, IBlockState p_176502_3_, IBlockState p_176502_4_) {

    }*/



    @Override
    public void func_149828_a(World world, int x, int y, int z, int p_149828_5_) {
        if (this.getMaterial().isLiquid()) {
            world.spawnEntityInWorld(new ConstructionBlockEntity(world, new BlockPos(x, y, z), true));
        }
    }

    private boolean tryTouchWater(World worldIn, BlockPos pos) {
        boolean flag = false;

        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (enumfacing != EnumFacing.DOWN) {
                BlockPos blockpos = pos.offset(enumfacing);

                if (IBlockState.getStateFromWorld(worldIn, blockpos).getBlock().getMaterial() == Material.water) { //TODO Get material from blockstate that is an int :(
                    flag = true;
                    break;
                }
            }
        }

        if (flag) {
            if (worldIn.getEntitiesWithinAABB(ConstructionBlockEntity.class, AxisAlignedBB.getBoundingBox(pos.getX()-0.5, pos.getY()-0.5, pos.getZ()-0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)).isEmpty()) {
                worldIn.spawnEntityInWorld(new ConstructionBlockEntity(worldIn, pos, true));
            }
        }

        return flag;
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    @Override
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
        if (!this.tryTouchWater((World) world, new BlockPos(x, y, z))) {
            super.onNeighborChange(world, x, y, z, tileX, tileY, tileZ);
        }
    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    @Override
    public void onBlockAdded(World worldIn, int x, int y, int z) {
        if (!this.tryTouchWater(worldIn, new BlockPos(x, y, z))) {
            super.onBlockAdded(worldIn, x, y, z);
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false; // TODO check if this is the correct method
    }

}
