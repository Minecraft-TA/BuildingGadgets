package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.backport.BlockPos;
import com.direwolf20.buildinggadgets.backport.EnumFacingPortUtil;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.common.building.placement.ConnectedSurface;
import com.direwolf20.buildinggadgets.common.building.placement.Surface;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/**
 * Surface mode for Building Gadget.
 * <p>
 * It will build on top of every block that has a valid path to the target position if its underside (the block offset
 * towards the side clicked) is same as the underside of the target position.
 * What is a valid path depends on whether the tool uses connected surface mode or not.
 *
 * @see ConnectedSurface
 * @see Surface
 */
public class BuildingSurfaceMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "surface");

    public BuildingSurfaceMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeWithTransformed(EntityPlayer player, BlockPos transformed, BlockPos original, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool) / 2;
        boolean fuzzy = GadgetGeneric.getFuzzy(tool);
        if (GadgetGeneric.getConnectedArea(tool))
            return ConnectedSurface.create(player.worldObj, transformed, EnumFacingPortUtil.getOpposite(sideHit), range , fuzzy);
        return Surface.create(player.worldObj, transformed, EnumFacingPortUtil.getOpposite(sideHit), range, fuzzy);
    }

    @Override
    public BlockPos transformAtop(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        return hit.offset(sideHit);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}
