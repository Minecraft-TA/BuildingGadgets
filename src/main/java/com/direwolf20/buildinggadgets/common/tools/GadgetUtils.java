package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.backport.BlockPos;
import com.direwolf20.buildinggadgets.backport.IBlockState;
import com.direwolf20.buildinggadgets.backport.NBTPortUtil;
import com.direwolf20.buildinggadgets.backport.Vec3i;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.integration.NetworkProvider;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.network.PacketRotateMirror;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GadgetUtils {

    private static final ImmutableList<String> LINK_STARTS = ImmutableList.of("http", "www");
    private static Supplier<IInventory> remoteInventorySupplier;

    public static boolean mightBeLink(final String s) {
        return LINK_STARTS.stream().anyMatch(s::startsWith);
    }

    public static final Comparator<Vec3i> POSITION_COMPARATOR = Comparator
        .comparingInt(Vec3i::getX)
        .thenComparingInt(Vec3i::getY)
        .thenComparingInt(Vec3i::getZ);

    public static String getStackErrorSuffix(ItemStack stack) {
        return getStackErrorText(stack) + " with NBT tag: " + stack.getTagCompound();
    }

    private static String getStackErrorText(ItemStack stack) {
        return "the following stack: [" + stack + "]";
    }

    @Nullable
    public static ByteArrayOutputStream getPasteStream(@Nonnull NBTTagCompound compound, @Nullable String name) throws IOException {
        NBTTagCompound withText = name != null && !name.isEmpty() ? (NBTTagCompound) compound.copy() : compound;
        if (name != null && !name.isEmpty())
            withText.setString("name", name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(withText, baos);
        return baos.size() < Short.MAX_VALUE - 200 ? baos : null;
    }

    @Nonnull
    public static NBTTagCompound getStackTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            throw new IllegalArgumentException("An NBT tag could net be retrieved from " + getStackErrorText(stack));

        return tag;
    }

    public static void pushUndoList(ItemStack stack, UndoState undoState) {
        //When we have a new set of Undo Coordinates, push it onto a list stored in NBT, max 10
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList undoStates = (NBTTagList) tagCompound.getTag("undoStack");
        if (undoStates == null) {
            undoStates = new NBTTagList();
        }
        if (undoStates.tagCount() >= 10) {
            undoStates.removeTag(0);
        }
        undoStates.appendTag(undoStateToNBT(undoState));
        tagCompound.setTag("undoStack", undoStates);
        stack.setTagCompound(tagCompound);
    }

    @Nullable
    public static UndoState popUndoList(ItemStack stack) {
        //Get the most recent Undo Coordinate set from the list in NBT
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagList undoStates = (NBTTagList) tagCompound.getTag("undoStack");
        if (undoStates == null || undoStates.tagCount() == 0) {
            return null;
        }
        UndoState undoState = NBTToUndoState(undoStates.getCompoundTagAt(undoStates.tagCount() - 1));
        undoStates.removeTag(undoStates.tagCount() - 1);
        tagCompound.setTag("undoStack", undoStates);
        return undoState;
    }

    private static NBTTagCompound undoStateToNBT(UndoState undoState) {
        //Convert an UndoState object into NBT data. Uses ints to store relative positions to a start block for data compression..
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dim", undoState.dimension);
        BlockPos startBlock = undoState.coordinates.get(0);
        int[] array = new int[undoState.coordinates.size()];
        int idx = 0;
        for (BlockPos coord : undoState.coordinates) {
            //Converts relative blockPos coordinates to a single integer value. Max range 127 due to 8 bits.
            int px = (((coord.getX() - startBlock.getX()) & 0xff) << 16);
            int py = (((coord.getY() - startBlock.getY()) & 0xff) << 8);
            int pz = (((coord.getZ() - startBlock.getZ()) & 0xff));
            int p = (px + py + pz);
            array[idx++] = p;
        }
        compound.setTag("startBlock", NBTPortUtil.createPosTag(startBlock));
        compound.setIntArray("undoIntCoords", array);
        return compound;
    }

    private static UndoState NBTToUndoState(NBTTagCompound compound) {
        //Convert an integer list stored in NBT into UndoState
        int dim = compound.getInteger("dim");
        List<BlockPos> coordinates = new ArrayList<BlockPos>();
        int[] array = compound.getIntArray("undoIntCoords");
        BlockPos startBlock = NBTPortUtil.readPosTag(compound.getCompoundTag("startBlock"));
        for (int i = 0; i <= array.length - 1; i++) {
            int p = array[i];
            int x = startBlock.getX() + (byte) ((p & 0xff0000) >> 16);
            int y = startBlock.getY() + (byte) ((p & 0x00ff00) >> 8);
            int z = startBlock.getZ() + (byte) (p & 0x0000ff);
            coordinates.add(new BlockPos(x, y, z));
        }
        UndoState undoState = new UndoState(dim, coordinates);
        return undoState;
    }

    public static void setAnchor(ItemStack stack, List<BlockPos> coordinates) {
        //Store 1 set of BlockPos in NBT to anchor the Ghost Blocks in the world when the anchor key is pressed
        NBTTagCompound tagCompound = stack.getTagCompound();
        NBTTagList coords = new NBTTagList();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        for (BlockPos coord : coordinates) {
            coords.appendTag(NBTPortUtil.createPosTag(coord));
        }
        tagCompound.setTag("anchorcoords", coords);
        stack.setTagCompound(tagCompound);
    }

    public static List<BlockPos> getAnchor(ItemStack stack) {
        //Return the list of coordinates in the NBT Tag for anchor Coordinates
        NBTTagCompound tagCompound = stack.getTagCompound();
        List<BlockPos> coordinates = new ArrayList<BlockPos>();
        if (tagCompound == null) {
            setAnchor(stack, coordinates);
            tagCompound = stack.getTagCompound();
            return coordinates;
        }
        NBTTagList coordList = (NBTTagList) tagCompound.getTag("anchorcoords");
        if (coordList == null) {
            setAnchor(stack, coordinates);
            tagCompound = stack.getTagCompound();
            return coordinates;
        }
        if (coordList.tagCount() == 0) {
            return coordinates;
        }
        for (int i = 0; i < coordList.tagCount(); i++) {
            coordinates.add(NBTPortUtil.readPosTag(coordList.getCompoundTagAt(i)));
        }
        return coordinates;
    }

    public static void setToolRange(ItemStack stack, int range) {
        //Store the tool's range in NBT as an Integer
        NBTTagCompound tagCompound = NBTTool.getOrNewTag(stack);
        tagCompound.setInteger("range", range);
    }

    public static int getToolRange(ItemStack stack) {
        NBTTagCompound tagCompound = NBTTool.getOrNewTag(stack);
        return MathHelper.clamp_int(tagCompound.getInteger("range"), 1, SyncedConfig.maxRange);
    }

    public static IBlockState rotateOrMirrorBlock(EntityPlayer player, PacketRotateMirror.Operation operation, IBlockState state) {
        // TODO: Not possible this easily anymore
        /*if (operation == PacketRotateMirror.Operation.MIRROR)
            return state.withMirror(EnumFacingPortUtil.getHorizontalFacing(player).getAxis() == Axis.X ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK);

        return state.withRotation(Rotation.CLOCKWISE_90);*/
        return state;
    }

    public static void rotateOrMirrorToolBlock(ItemStack stack, EntityPlayer player, PacketRotateMirror.Operation operation) {
        setToolBlock(stack, rotateOrMirrorBlock(player, operation, getToolBlock(stack)));
        setToolActualBlock(stack, rotateOrMirrorBlock(player, operation, getToolActualBlock(stack)));
    }

    private static void setToolBlock(ItemStack stack, @Nullable IBlockState state) {
        //Store the selected block in the tool's NBT
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (state == null) {
            state = IBlockState.AIR_STATE;
        }
        tagCompound.setInteger("blockstate", state.getMeta());
        stack.setTagCompound(tagCompound);
    }

    private static void setToolActualBlock(ItemStack stack, @Nullable IBlockState state) {
        //Store the selected block actual state in the tool's NBT
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (state == null) {
            state = IBlockState.AIR_STATE;
        }
        NBTTagCompound stateTag = new NBTTagCompound();
        NBTPortUtil.writeBlockState(stateTag, state);
        tagCompound.setTag("actualblockstate", stateTag);
        stack.setTagCompound(tagCompound);
    }

    public static IBlockState getToolBlock(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            setToolBlock(stack, IBlockState.AIR_STATE);
            return IBlockState.AIR_STATE;
        }
        return NBTPortUtil.readBlockState(tagCompound.getCompoundTag("blockstate"));
    }

    public static IBlockState getToolActualBlock(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            setToolBlock(stack, IBlockState.AIR_STATE);
            tagCompound = stack.getTagCompound();
            return IBlockState.AIR_STATE;
        }
        return NBTPortUtil.readBlockState(tagCompound.getCompoundTag("actualblockstate"));
    }

    public static void selectBlock(ItemStack stack, EntityPlayer player) {
        //Used to find which block the player is looking at, and store it in NBT on the tool.
        World world = player.worldObj;
        MovingObjectPosition lookingAt = VectorTools.getLookingAt(player, false);
        if (lookingAt == null)
            return;

        BlockPos pos = new BlockPos(lookingAt.blockX, lookingAt.blockY, lookingAt.blockZ);
        EnumActionResult result = setRemoteInventory(stack, player, world, pos, true);
        if (result == EnumActionResult.SUCCESS)
            return;

        IBlockState state = IBlockState.getStateFromWorld(world, pos);
        if (result == EnumActionResult.FAIL || SyncedConfig.blockBlacklist.contains(state.getBlock()) || state.getBlock() instanceof EffectBlock) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + new ChatComponentTranslation("message.gadget.invalidblock").getUnformattedTextForChat()));
            return;
        }
        IBlockState placeState = InventoryManipulation.getSpecificStates(state, world, player, pos, stack);
        // TODO: getActualState doesn't exist, but there's custom stuff like BlockDoor.func_150012_g (getFullMetadata)
//        IBlockState actualState = placeState.getActualState(world, pos);
        IBlockState actualState = placeState;
        setToolBlock(stack, placeState);
        setToolActualBlock(stack, actualState);
    }

    public static EnumActionResult setRemoteInventory(ItemStack stack, EntityPlayer player, World world, BlockPos pos, boolean setTool) {
        TileEntity te = world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
        if (te == null)
            return EnumActionResult.PASS;

        if (setTool && te instanceof ConstructionBlockTileEntity && ((ConstructionBlockTileEntity) te).getBlockState() != null) {
            setToolBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockState());
            setToolActualBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockState());
            return EnumActionResult.SUCCESS;
        }
        if (setRemoteInventory(player, stack, pos, world.provider.dimensionId, world))
            return EnumActionResult.SUCCESS;

        return EnumActionResult.FAIL;
    }

    public static boolean anchorBlocks(EntityPlayer player, ItemStack stack) {
        //Stores the current visual blocks in NBT on the tool, so the player can look around without moving the visual render
        World world = player.worldObj;
        List<BlockPos> currentCoords = getAnchor(stack);
        if (currentCoords.isEmpty()) {  //If we don't have an anchor, find the block we're supposed to anchor to
            MovingObjectPosition lookingAt = VectorTools.getLookingAt(player, stack);
            if (lookingAt == null) {  //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = new BlockPos(lookingAt.blockX, lookingAt.blockY, lookingAt.blockZ);
            int sideHit = lookingAt.sideHit;
            if (IBlockState.getStateFromWorld(world, startBlock) == IBlockState.AIR_STATE) { //If we are looking at air, exit
                return false;
            }
            List<BlockPos> coords = new ArrayList<BlockPos>();
            if (stack.getItem() instanceof GadgetBuilding) {
                coords = BuildingModes.collectPlacementPos(world, player, startBlock, sideHit, stack, startBlock); // Build the positions list based on tool mode and range
            } else if (stack.getItem() instanceof GadgetExchanger) {
                coords = ExchangingModes.collectPlacementPos(world, player, startBlock, sideHit, stack, startBlock); // Build the positions list based on tool mode and range
            }
            setAnchor(stack, coords); //Set the anchor NBT
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + new ChatComponentTranslation("message.gadget.anchorrender").getUnformattedTextForChat()));
        } else {  //If theres already an anchor, remove it.
            setAnchor(stack, new ArrayList<BlockPos>());
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + new ChatComponentTranslation("message.gadget.anchorremove").getUnformattedTextForChat()));
        }
        return true;
    }

    public static boolean setRemoteInventory(EntityPlayer player, ItemStack tool, BlockPos pos, int dim, World world) {
        if (getRemoteInventory(pos, dim, world, player) != null) {
            boolean same = pos.equals(getPOSFromNBT(tool, "boundTE"));
            writePOSToNBT(tool, same ? null : pos, "boundTE", dim);
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + new ChatComponentTranslation("message.gadget." + (same ? "unboundTE" : "boundTE")).getUnformattedTextForChat()));
            return true;
        }
        return false;
    }

    public static void clearCachedRemoteInventory() {
        remoteInventorySupplier = null;
    }

    @Nullable
    public static IInventory getRemoteInventory(ItemStack tool, World world, EntityPlayer player) {
        return getRemoteInventory(tool, world, player, NetworkIO.Operation.EXTRACT);
    }

    /**
     * Call {@link #clearCachedRemoteInventory clearCachedRemoteInventory} when done using this method
     */
    @Nullable
    public static IInventory getRemoteInventory(ItemStack tool, World world, EntityPlayer player, NetworkIO.Operation operation) {
        if (remoteInventorySupplier == null) {
            remoteInventorySupplier = Suppliers.memoizeWithExpiration(() -> {
                Integer dim = getDIMFromNBT(tool, "boundTE");
                if (dim == null)
                    return null;

                // Check if the Dimension actually exists. (Thanks RFTools...)
                if (DimensionManager.getWorld(dim) == null)
                    return null;

                BlockPos pos = getPOSFromNBT(tool, "boundTE");
                return pos == null ? null : getRemoteInventory(pos, dim, world, player, operation);
            }, 500, TimeUnit.MILLISECONDS);
        }
        return remoteInventorySupplier.get();
    }

    @Nullable
    public static IInventory getRemoteInventory(BlockPos pos, int dim, World world, EntityPlayer player) {
        return getRemoteInventory(pos, dim, world, player, NetworkIO.Operation.EXTRACT);
    }

    @Nullable
    public static IInventory getRemoteInventory(BlockPos pos, int dim, World world, EntityPlayer player, NetworkIO.Operation operation) {
        if (world.isRemote)
            return null;
        World worldServer = MinecraftServer.getServer().worldServerForDimension(dim);
        if (worldServer == null)
            return null;
        return getRemoteInventory(pos, worldServer, player, operation);
    }

    @Nullable
    public static IInventory getRemoteInventory(BlockPos pos, World world, EntityPlayer player, NetworkIO.Operation operation) {
        TileEntity te = world.getTileEntity(pos.getX(), pos.getY(), pos.getZ());
        if (te == null)
            return null;
        IInventory network = NetworkProvider.getWrappedNetwork(te, player, operation);
        if (network != null)
            return network;
        if (!(te instanceof IInventory))
            return null;
        return (IInventory) te;
    }

    public static String withSuffix(int count) {
        if (count < 1000)
            return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
            count / Math.pow(1000, exp),
            "kMGTPE".charAt(exp - 1));
    }

    public static void writePOSToNBT(ItemStack stack, @Nullable BlockPos pos, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (pos == null) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
                stack.setTagCompound(tagCompound);
            }
            return;
        }
        tagCompound.setTag(tagName, NBTPortUtil.createPosTag(pos));
        stack.setTagCompound(tagCompound);
    }

    public static void writePOSToNBT(ItemStack stack, @Nullable BlockPos pos, String tagName, Integer dim) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (pos == null) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
                stack.setTagCompound(tagCompound);
            }
            return;
        }
        NBTTagCompound posTag = NBTPortUtil.createPosTag(pos);
        posTag.setInteger("dim", dim);
        tagCompound.setTag(tagName, posTag);
        stack.setTagCompound(tagCompound);
    }

    public static void writePOSToNBT(NBTTagCompound tagCompound, @Nullable BlockPos pos, String tagName, Integer dim) {
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (pos == null) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
            }
            return;
        }
        tagCompound.setTag(tagName, NBTPortUtil.createPosTag(pos));
        tagCompound.setInteger("dim", dim);
    }

    @Nullable
    public static BlockPos getPOSFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound posTag = tagCompound.getCompoundTag(tagName);
        if (posTag.equals(new NBTTagCompound())) {
            return null;
        }
        return NBTPortUtil.readPosTag(posTag);
    }

    public static void writeIntToNBT(ItemStack stack, int tagInt, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInteger(tagName, tagInt);
        stack.setTagCompound(tagCompound);
    }

    public static int getIntFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return tagCompound == null ? 0 : tagCompound.getInteger(tagName);
    }

    public static void writeStringToNBT(ItemStack stack, String string, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (string.equals(null)) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
            }
            return;
        }
        tagCompound.setString(tagName, string);
    }

    public static void writeStringToNBT(NBTTagCompound tagCompound, String string, String tagName) {//TODO unused
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (string.equals(null)) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
            }
            return;
        }
        tagCompound.setString(tagName, string);
    }

    @Nullable
    public static String getStringFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        return tagCompound.getString(tagName);
    }

    @Nullable
    public static BlockPos getPOSFromNBT(NBTTagCompound tagCompound, String tagName) {
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound posTag = tagCompound.getCompoundTag(tagName);
        if (posTag.equals(new NBTTagCompound())) {
            return null;
        }
        return NBTPortUtil.readPosTag(posTag);
    }

    @Nullable
    public static Integer getDIMFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound posTag = tagCompound.getCompoundTag(tagName);
        if (posTag.equals(new NBTTagCompound())) {
            return null;
        }
        return posTag.getInteger("dim");
    }

    public static NBTTagCompound stateToCompound(IBlockState state) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        NBTPortUtil.writeBlockState(tagCompound, state);
        return tagCompound;
    }

    @Nullable
    public static IBlockState compoundToState(@Nullable NBTTagCompound tagCompound) {
        if (tagCompound == null) {
            return null;
        }
        return NBTPortUtil.readBlockState(tagCompound);
    }

    public static int relPosToInt(BlockPos startPos, BlockPos relPos) {
        int px = (((relPos.getX() - startPos.getX()) & 0xff) << 16);
        int py = (((relPos.getY() - startPos.getY()) & 0xff) << 8);
        int pz = (((relPos.getZ() - startPos.getZ()) & 0xff));
        int p = (px + py + pz);
        return p;
    }

    public static BlockPos relIntToPos(BlockPos startPos, int relInt) {
        int p = relInt;
        int x = startPos.getX() + (byte) ((p & 0xff0000) >> 16);
        int y = startPos.getY() + (byte) ((p & 0x00ff00) >> 8);
        int z = startPos.getZ() + (byte) (p & 0x0000ff);
        return new BlockPos(x, y, z);
    }

    public static NBTTagList itemCountToNBT(Multiset<UniqueItem> itemCountMap) {
        NBTTagList tagList = new NBTTagList();

        for (Multiset.Entry<UniqueItem> entry : itemCountMap.entrySet()) {
            int item = Item.getIdFromItem(entry.getElement().item);
            int meta = entry.getElement().meta;
            int count = entry.getCount();
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setInteger("item", item);
            tagCompound.setInteger("meta", meta);
            tagCompound.setInteger("count", count);
            tagList.appendTag(tagCompound);
        }
        return tagList;
    }

    public static Multiset<UniqueItem> nbtToItemCount(@Nullable NBTTagList tagList) {
        if (tagList == null)
            return HashMultiset.create();
        Multiset<UniqueItem> itemCountMap = HashMultiset.create(tagList.tagCount());
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            UniqueItem uniqueItem = new UniqueItem(Item.getItemById(tagCompound.getInteger("item")), tagCompound.getInteger("meta"));
            int count = tagCompound.getInteger("count");
            itemCountMap.setCount(uniqueItem, count);
        }

        return itemCountMap;
    }

}
