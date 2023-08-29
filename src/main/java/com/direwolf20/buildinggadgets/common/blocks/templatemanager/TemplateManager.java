package com.direwolf20.buildinggadgets.common.blocks.templatemanager;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.network.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.direwolf20.buildinggadgets.backport.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import com.direwolf20.buildinggadgets.backport.BlockPos;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TemplateManager extends Block {
    private static final int GUI_ID = 1;

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyDirection FACING_HORIZ = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public TemplateManager() {
        super(Material.rock);
        setHardness(2.0f);
        setBlockName(BuildingGadgets.MODID + ".templatemanager");
        setBlockTextureName("templatemanager");
        setCreativeTab(BuildingGadgets.BUILDING_CREATIVE_TAB);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING_HORIZ, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {

        EnumFacing enumfacing = EnumFacing.getFront(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }


    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TemplateManagerTileEntity();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        if (world.isRemote) {
            return true;
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TemplateManagerTileEntity)) {
            return false;
        }
        TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);
        for (int i = 0; i <= 1; i++) {
            ItemStack itemStack = container.getSlot(i).getStack();
            if (!(itemStack.getItem() instanceof ITemplate)) continue;

            ITemplate template = (ITemplate) itemStack.getItem();
            String UUID = template.getUUID(itemStack);
            if (UUID == null) continue;

            NBTTagCompound tagCompound = template.getWorldSave(world).getCompoundFromUUID(UUID);
            if (tagCompound != null) {
                PacketHandler.INSTANCE.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);
            }
        }
        player.openGui(BuildingGadgets.instance, GUI_ID, world, x, y, z);
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block blockBroken, int meta) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TemplateManagerTileEntity) {
            /*IItemHandler cap = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null); TODO Inventories are impossible in 1.7.
            if (cap != null) {
                for (int i = 0; i < cap.getSlots(); i++) {
                    ItemStack stack = cap.getStackInSlot(i);
                    if (stack != null) {
                        InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }*/
        }
        super.breakBlock(world, x, y, z, blockBroken, meta);
    }

}
