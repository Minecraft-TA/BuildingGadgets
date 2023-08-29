package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.backport.BlockPos;
import com.direwolf20.buildinggadgets.backport.IBlockState;
import com.direwolf20.buildinggadgets.common.blocks.Models.BlockstateProperty;
import com.direwolf20.buildinggadgets.common.blocks.Models.ConstructionBakedModel;
import com.direwolf20.buildinggadgets.common.items.FakeRenderWorld;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fml.common.Optional;
import team.chisel.ctm.api.IFacade;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Optional.Interface(iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public class ConstructionBlock extends BlockModBase implements IFacade {

    //public static final ConstructionProperty FACADEID = new ConstructionProperty("facadeid");
    public static final PropertyBool BRIGHT = PropertyBool.create("bright");
    public static final PropertyBool NEIGHBOR_BRIGHTNESS = PropertyBool.create("neighbor_brightness");

    public static final IUnlistedProperty<IBlockState> FACADE_ID = new BlockstateProperty("facadestate");
    public static final IUnlistedProperty<IBlockState> FACADE_EXT_STATE = new BlockstateProperty("facadeextstate");

    public ConstructionBlock() {
        super(Material.rock, 2F, "constructionblock");
        setDefaultState(blockState.getBaseState().withProperty(BRIGHT, true).withProperty(NEIGHBOR_BRIGHTNESS, false));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        super.initModel();
        StateMapperBase ignoreState = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return ConstructionBakedModel.modelFacade;
            }
        };
        ModelLoader.setCustomStateMapper(this, ignoreState);
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return ModItems.constructionPaste;
    }

    @Override
    public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        return false;
    }

    @Override
    public boolean hasTileEntity(int state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World worldIn, int state) {
        return new ConstructionBlockTileEntity();
    }

    /*private static ConstructionBlockTileEntity getTE(World world, BlockPos pos) {
        return (ConstructionBlockTileEntity) world.getTileEntity(pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        //super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        ConstructionBlockTileEntity te = getTE(world, pos);
        ItemStack heldItem = player.getHeldItem(hand);
        IBlockState newState = Block.getBlockFromItem(heldItem.getItem()).getStateFromMeta(heldItem.getMetadata());
        if (newState != null && newState != IBlockState.AIR_STATE) {
            te.setBlockState(newState);
            te.setActualBlockState(newState);
            return true;
        }
        System.out.println("Failed: " + newState + ":" + te.getBlockState() + ":" + world.isRemote + ":" + te.getActualBlockState());
        return false;
    }*/

    @Nullable
    private IBlockState getActualMimicBlock(IBlockAccess blockAccess, int x, int y, int z) {
        try {
            TileEntity te = blockAccess.getTileEntity(x, y, z);
            if (te instanceof ConstructionBlockTileEntity) {
                return ((ConstructionBlockTileEntity) te).getActualBlockState();
            }
            return null;
        } catch (Exception var8) {
            return null;
        }
    }

    // TODO: Idk
    /*@Override
    public int getRenderType() {
        return super.getRenderType();
    }*/

    // TODO:
    /*@Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true; // delegated to FacadeBakedModel#getQuads
    }*/

    /*@Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState mimicBlock = getActualMimicBlock(blockAccess, pos);
        return mimicBlock == null ? true : mimicBlock.getBlock().shouldSideBeRendered(mimicBlock, blockAccess, pos, side);
    }*/

    @Override
    @Deprecated
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    @Deprecated
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        // TODO: Could be wrong
        IBlockState mimicBlock = getActualMimicBlock(world, x, y, z);
        return mimicBlock != null ? mimicBlock.getBlock().getLightOpacity(world, x, y, z) : super.getLightOpacity(world, x, y, z);
    }

    // TODO: Idk
    /*@Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState mimicBlock = getActualMimicBlock(world, pos);
        return mimicBlock == null ? true : mimicBlock.getBlock().doesSideBlockRendering(mimicBlock, world, pos, face);
    }*/

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess worldIn, int x, int y, int z) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, x, y, z);
        return mimicBlock != null ? mimicBlock.getBlock().colorMultiplier(worldIn, x, y, z) : super.colorMultiplier(worldIn, x, y, z);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        IBlockState mimicBlock = getActualMimicBlock(world, x, y, z);
        try {
            return mimicBlock == null || mimicBlock.getBlock().isSideSolid(world, x, y, z, side);
        } catch (Exception var8) {
            return true;
        }
    }

    @Override
    @Deprecated
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, x, y, z);
        if (mimicBlock == null) {
            super.addCollisionBoxesToList(worldIn, x, y, z, entityBox, collidingBoxes, entityIn);
        } else {
            try {
                mimicBlock.getBlock().addCollisionBoxesToList(worldIn, x, y, z, entityBox, collidingBoxes, entityIn);
            } catch (Exception var8) {
                super.addCollisionBoxesToList(worldIn, x, y, z, entityBox, collidingBoxes, entityIn);
            }
        }
    }

    @Override
    @Nullable
    @Deprecated
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, x, y, z);
        if (mimicBlock == null) {
            return super.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
        }
        try {
            return mimicBlock.getBlock().getCollisionBoundingBoxFromPool(worldIn, x, y, z);
        } catch (Exception var8) {
            return super.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
        }
    }


    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
        FakeRenderWorld fakeWorld = new FakeRenderWorld();

        BlockPos pos = new BlockPos(x, y, z);

        IBlockState mimicBlock = getActualMimicBlock(blockAccess, x, y, z);
        if (mimicBlock == null) {
            return super.shouldSideBeRendered(fakeWorld, x, y, z, side);
        }
        BlockPos pos2 = pos.offset(EnumFacing.values()[side]);
        IBlockState sideBlockState = IBlockState.getStateFromWorld(blockAccess, pos2);
        if (sideBlockState.getBlock().equals(ModBlocks.constructionBlock)) {
            if (!(getActualMimicBlock(blockAccess, pos2.getX(), pos2.getY(), pos2.getZ()) == null)) {
                sideBlockState = getActualMimicBlock(blockAccess, pos2.getX(), pos2.getY(), pos2.getZ());
            }
        }

        fakeWorld.setState(blockAccess, mimicBlock, pos);
        fakeWorld.setState(blockAccess, sideBlockState, pos2);

        try {
            return mimicBlock.getBlock().shouldSideBeRendered(fakeWorld, x, y, z, side);
        } catch (Exception var8) {
            return super.shouldSideBeRendered(fakeWorld, x, y, z, side);
        }
    }

    @Override
    public boolean isNormalCube(IBlockAccess world, int x, int y, int z) {
        IBlockState mimicBlock = getActualMimicBlock(world, x, y, z);
        if (mimicBlock == null) {
            return super.isNormalCube(world, x, y, z);
        }
        try {
            return mimicBlock.getBlock().isNormalCube(world, x, y, z);
        } catch (Exception var8) {
            return super.isNormalCube(world, x, y, z);
        }
    }

    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue() {
        // TODO: Idk how
//        Boolean bright = state.getValue(ConstructionBlock.BRIGHT);
//        Boolean neighborBrightness = state.getValue(ConstructionBlock.NEIGHBOR_BRIGHTNESS);
//        if (bright || neighborBrightness) {
//            return 1f;
//        }
//        return 0.2f;
        return 1f;
    }

    // TODO: Idk how
//    @Override
//    @Deprecated
//    public boolean getUseNeighborBrightness(IBlockState state) {
//        return state.getValue(ConstructionBlock.NEIGHBOR_BRIGHTNESS);
//    }

    /**
     * The below implements support for CTM's Connected Textures to work properly
     *
     * @param world IBlockAccess
     * @param pos   BlockPos
     * @param side  EnumFacing
     * @return IBlockState
     * @deprecated see {@link IFacade#getFacade(IBlockAccess, BlockPos, EnumFacing, BlockPos)}
     */
    // TODO: ConnectedTexturesMod support
    /*@Override
    @Nonnull
    @Deprecated
    public IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        IBlockState mimicBlock = getActualMimicBlock(world, pos);
        return mimicBlock != null ? mimicBlock : IBlockState.getStateFromWorld(world, pos);
        //return mimicBlock;
    }*/
}
