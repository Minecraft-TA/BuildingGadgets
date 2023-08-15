package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.backport.BlockPos;
import com.direwolf20.buildinggadgets.backport.EnumFacingPortUtil;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static com.direwolf20.buildinggadgets.common.config.SyncedConfig.rayTraceRange;

public class VectorTools {

    public static MovingObjectPosition getLookingAt(EntityPlayer player, ItemStack tool) {
        return getLookingAt(player, GadgetGeneric.shouldRayTraceFluid(tool));
    }

    public static MovingObjectPosition getLookingAt(EntityPlayer player, boolean rayTraceFluid) {
        World world = player.worldObj;
        Vec3 look = player.getLookVec();
        Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        //rayTraceRange here refers to SyncedConfig.rayTraceRange
        Vec3 end = Vec3.createVectorHelper(player.posX + look.xCoord * rayTraceRange, player.posY + player.getEyeHeight() + look.yCoord * rayTraceRange, player.posZ + look.zCoord * rayTraceRange);
        return world.func_147447_a(start, end, rayTraceFluid, false, false);
    }

    @Nullable
    public static BlockPos getPosLookingAt(EntityPlayer player, ItemStack tool) {
        MovingObjectPosition lookingAt = VectorTools.getLookingAt(player, tool);
        if (lookingAt == null)
            return null;

        return new BlockPos(lookingAt.blockX, lookingAt.blockY, lookingAt.blockZ);
    }

    public static int getAxisValue(BlockPos pos, EnumFacingPortUtil.Axis axis) {
        switch (axis) {
            case X:
                return pos.getX();
            case Y:
                return pos.getY();
            case Z:
                return pos.getZ();
        }
        throw new IllegalArgumentException("Trying to find the value an imaginary axis of a BlockPos");
    }

    public static int getAxisValue(int x, int y, int z, EnumFacingPortUtil.Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        throw new IllegalArgumentException("Trying to find the value an imaginary axis of a set of 3 values");
    }

    public static BlockPos perpendicularSurfaceOffset(BlockPos pos, EnumFacing intersector, int i, int j) {
        return perpendicularSurfaceOffset(pos, EnumFacingPortUtil.getAxis(intersector), i, j);
    }

    public static BlockPos perpendicularSurfaceOffset(BlockPos pos, EnumFacingPortUtil.Axis intersector, int i, int j) {
        switch (intersector) {
            case X:
                return pos.add(0, i, j);
            case Y:
                return pos.add(i, 0, j);
            case Z:
                return pos.add(i, j, 0);
        }
        throw new IllegalArgumentException("Unknown facing " + intersector);
    }

}
