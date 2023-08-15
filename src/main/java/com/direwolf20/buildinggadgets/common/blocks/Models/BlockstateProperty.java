package com.direwolf20.buildinggadgets.common.blocks.Models;

import com.direwolf20.buildinggadgets.backport.IBlockState;

public class BlockstateProperty implements IUnlistedProperty<IBlockState> {
    private final String name;

    public BlockstateProperty(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(IBlockState value) {
        return true;
    }

    @Override
    public Class<IBlockState> getType() {
        return IBlockState.class;
    }

    @Override
    public String valueToString(IBlockState value) {
        return value.toString();
    }
}
