package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.backport.BlockPos;

import java.util.List;

public class UndoState {

    public final int dimension;
    public final List<BlockPos> coordinates;

    public UndoState(int dim, List<BlockPos> coords) {
        dimension = dim;
        coordinates = coords;
    }

}
