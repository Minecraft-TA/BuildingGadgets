package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IBlockProvider;
import com.direwolf20.buildinggadgets.common.building.TranslationWrapper;
import com.direwolf20.buildinggadgets.common.tools.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import com.direwolf20.buildinggadgets.common.tools.BlockPos;

/**
 * Immutable block provider that always return the same block state regardless of which position is requested.
 */
public class SingleTypeProvider implements IBlockProvider {

    private final IBlockState state;

    /**
     * @param state value that {@link #at(BlockPos)} will return
     */
    public SingleTypeProvider(IBlockState state) {
        this.state = state;
    }

    @Override
    public TranslationWrapper glTranslatef(BlockPos origin) {
        return new TranslationWrapper(this, origin);
    }

    /**
     * @return {@link #state}, which is initialized in the constructor, regardless of the parameter.
     */
    @Override
    public IBlockState at(BlockPos pos) {
        return state;
    }

    public IBlockState getBlockState() {
        return state;
    }

    @Override
    public void serialize(NBTTagCompound tag) {
        NBTPortUtil.writeBlockState(tag, state);
    }

    @Override
    public SingleTypeProvider deserialize(NBTTagCompound tag) {
        return new SingleTypeProvider(NBTPortUtil.readBlockState(tag));
    }

}
