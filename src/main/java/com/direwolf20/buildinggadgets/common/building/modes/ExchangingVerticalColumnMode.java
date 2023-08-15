package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.backport.BlockPos;
import com.direwolf20.buildinggadgets.backport.EnumFacingPortUtil;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.common.building.placement.Column;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.MathTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/**
 * Vertical column mode for Exchanging Gadget.
 * <p>
 * If a 2D x-y coordinate plane was built on the selected side with the selected block as origin, the column will be the
 * Y axis in the plane.
 * The column will be centered at the origin. Length of the column will be the tool range that is floored to an odd
 * number with a lower bound of 1.
 *
 * @see Column
 */
public class ExchangingVerticalColumnMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "vertical_column");

    public ExchangingVerticalColumnMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        int radius = MathTool.floorToOdd(range);
        return Column.centerAt(hit, EnumFacingPortUtil.getAxis(sideHit).isVertical() ? EnumFacingPortUtil.getAxis(EnumFacingPortUtil.getHorizontalFacing(player)) : EnumFacingPortUtil.Axis.Y, radius);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}
