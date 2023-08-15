package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.backport.BlockPos;
import com.direwolf20.buildinggadgets.backport.IBlockState;
import com.direwolf20.buildinggadgets.backport.NBTPortUtil;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBuildEntity extends Entity {

    //    private static final DataParameter<Integer> toolMode = EntityDataManager.<Integer>createKey(BlockBuildEntity.class, DataSerializers.VARINT);
    private static final int ID_TOOL_MODE = 1;
    //    private static final DataParameter<Optional<IBlockState>> SET_BLOCK = EntityDataManager.<Optional<IBlockState>>createKey(BlockBuildEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
    private static final int ID_SET_BLOCK_ID = 2;
    private static final int ID_SET_BLOCK_META = 3;
    //    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BLOCK_POS);
    private static final int ID_FIXED = 4;
    //    private static final DataParameter<Boolean> usePaste = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BOOLEAN);
    private static final int ID_USE_PASTE = 5;

    private int despawning = -1;
    public int maxLife = 20;
    private int mode;
    private IBlockState setBlock;
    private IBlockState originalSetBlock;
    private IBlockState actualSetBlock;
    private BlockPos setPos;
    private EntityLivingBase spawnedBy;
    private boolean useConstructionPaste;

    private World world;

    public BlockBuildEntity(World worldIn) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        world = worldIn;
    }

    public BlockBuildEntity(World worldIn, BlockPos spawnPos, EntityLivingBase player, IBlockState spawnBlock, int toolMode, IBlockState actualSpawnBlock, boolean constrPaste) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        IBlockState currentBlock = IBlockState.getStateFromWorld(worldIn, spawnPos);
        TileEntity te = worldIn.getTileEntity(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        setPos = spawnPos;
        if (te instanceof ConstructionBlockTileEntity) {
            setBlock = ((ConstructionBlockTileEntity) te).getBlockState();
            if (setBlock == null) {
                setBlock = spawnBlock;
            }
        } else {
            setBlock = spawnBlock;
        }
        originalSetBlock = spawnBlock;
        setSetBlock(setBlock);
        if (toolMode == 3) {
            if (currentBlock != null) {
                if (te instanceof ConstructionBlockTileEntity) {
                    setBlock = ((ConstructionBlockTileEntity) te).getBlockState();
                    if (setBlock == null) {
                        setBlock = currentBlock;
                    }
                } else {
                    setBlock = currentBlock;
                }
                setSetBlock(setBlock);
            } else {
                setBlock = IBlockState.AIR_STATE;
                setSetBlock(setBlock);
            }
        }
        world = worldIn;
        mode = toolMode;
        setToolMode(toolMode);
        spawnedBy = player;
        actualSetBlock = actualSpawnBlock;

        // Don't let leaves decay TODO: I don't think 1.7 has this
//        if (setBlock.getBlock() instanceof BlockLeaves)
//            setBlock = setBlock.withProperty(BlockLeaves.DECAYABLE, false);

        world.setBlock(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), ModBlocks.effectBlock);
        setUsingConstructionPaste(constrPaste);
    }

    public int getToolMode() {
        return this.getDataWatcher().getWatchableObjectInt(ID_TOOL_MODE);
    }

    public void setToolMode(int mode) {
        this.getDataWatcher().updateObject(ID_TOOL_MODE, mode);
    }

    @Nullable
    public IBlockState getSetBlock() {
        int id = this.getDataWatcher().getWatchableObjectInt(ID_SET_BLOCK_ID);
        int meta = this.getDataWatcher().getWatchableObjectInt(ID_SET_BLOCK_META);
        return id == -1 ? null : IBlockState.create(Block.getBlockById(id), meta);
    }

    public void setSetBlock(@Nullable IBlockState state) {
        if (state == null) {
            this.getDataWatcher().updateObject(ID_SET_BLOCK_ID, -1);
            this.getDataWatcher().updateObject(ID_SET_BLOCK_META, -1);
        } else {
            this.getDataWatcher().updateObject(ID_SET_BLOCK_ID, Block.getIdFromBlock(state.getBlock()));
            this.getDataWatcher().updateObject(ID_SET_BLOCK_META, state.getMeta());
        }
    }

    public void setUsingConstructionPaste(Boolean paste) {
        this.getDataWatcher().updateObject(ID_USE_PASTE, paste);
    }

    public boolean getUsingConstructionPaste() {
        return this.getDataWatcher().getWatchableObjectByte(ID_USE_PASTE) == 1;
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0; //After tr
    }

    public int getTicksExisted() {
        return ticksExisted;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (ticksExisted > maxLife) {
            setDespawning();
        }

        if (!isDespawning()) {

        } else {
            despawnTick();
        }
    }

    public boolean isDespawning() {
        return despawning != -1;
    }

    private void setDespawning() {
        if (despawning == -1) {
            despawning = 0;
            if (setPos != null && setBlock != null && (getToolMode() == 1)) {
                if (getUsingConstructionPaste()) {
                    world.setBlock(setPos.getX(), setPos.getY(), setPos.getZ(), ModBlocks.constructionBlock);
                    TileEntity te = world.getTileEntity(setPos.getX(), setPos.getY(), setPos.getZ());
                    if (te instanceof ConstructionBlockTileEntity) {
                        ((ConstructionBlockTileEntity) te).setBlockState(setBlock);
                        ((ConstructionBlockTileEntity) te).setActualBlockState(actualSetBlock);
                    }
                    world.spawnEntityInWorld(new ConstructionBlockEntity(world, setPos, false));
                } else {
                    world.setBlock(setPos.getX(), setPos.getY(), setPos.getZ(), setBlock.getBlock(), setBlock.getMeta(), 3);
                    IBlockState.getStateFromWorld(world, setPos).getBlock().onNeighborBlockChange(world, setPos.getX(), setPos.getY(), setPos.getZ(), IBlockState.getStateFromWorld(world, setPos.up()).getBlock());
                }
            } else if (setPos != null && setBlock != null && getToolMode() == 2) {
                world.setBlockToAir(setPos.getX(), setPos.getY(), setPos.getZ());
            } else if (setPos != null && setBlock != null && getToolMode() == 3) {
                world.spawnEntityInWorld(new BlockBuildEntity(world, setPos, spawnedBy, originalSetBlock, 1, actualSetBlock, getUsingConstructionPaste()));
            }
        }
    }

    private void despawnTick() {
        despawning++;
        if (despawning > 1) {
            setDead();
        }
    }

    @Override
    public void setDead() {
        this.isDead = true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("despawning", despawning);
        compound.setInteger("ticksExisted", ticksExisted);
        compound.setTag("setPos", NBTPortUtil.createPosTag(setPos));
        NBTTagCompound blockStateTag = new NBTTagCompound();
        NBTPortUtil.writeBlockState(blockStateTag, setBlock);
        compound.setTag("setBlock", blockStateTag);
        NBTTagCompound actualBlockStateTag = new NBTTagCompound();
        NBTPortUtil.writeBlockState(actualBlockStateTag, actualSetBlock);
        compound.setTag("actualSetBlock", actualBlockStateTag);
        NBTPortUtil.writeBlockState(blockStateTag, originalSetBlock);
        compound.setTag("originalBlock", blockStateTag);
        compound.setInteger("mode", mode);
        compound.setBoolean("paste", useConstructionPaste);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        despawning = compound.getInteger("despawning");
        ticksExisted = compound.getInteger("ticksExisted");
        setPos = NBTPortUtil.readPosTag(compound.getCompoundTag("setPos"));
        setBlock = NBTPortUtil.readBlockState(compound.getCompoundTag("setBlock"));
        originalSetBlock = NBTPortUtil.readBlockState(compound.getCompoundTag("originalBlock"));
        actualSetBlock = NBTPortUtil.readBlockState(compound.getCompoundTag("actualSetBlock"));
        mode = compound.getInteger("mode");
        useConstructionPaste = compound.getBoolean("paste");
    }

    @Override
    protected void entityInit() {
        this.getDataWatcher().addObject(ID_FIXED, new ChunkCoordinates(0, 0, 0));
        this.getDataWatcher().addObject(ID_TOOL_MODE, 1);
        this.getDataWatcher().addObject(ID_SET_BLOCK_ID, -1);
        this.getDataWatcher().addObject(ID_SET_BLOCK_META, -1);
        this.getDataWatcher().addObject(ID_USE_PASTE, (byte) (useConstructionPaste ? 1 : 0));
    }

}
