package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.client.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.FakeBuilderWorld;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.items.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multiset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.energy.CapabilityEnergy;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.getAnchor;
import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.getToolBlock;

public class ToolRenders {

    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    private static Minecraft mc = Minecraft.getMinecraft();
    private static RemoteInventoryCache cacheInventory = new RemoteInventoryCache(false);
    private static Cache<Triple<UniqueItemStack, BlockPos, Integer>, Integer> cacheDestructionOverlay = CacheBuilder.newBuilder().maximumSize(1).
        expireAfterWrite(1, TimeUnit.SECONDS).removalListener(removal -> GLAllocation.deleteDisplayLists((int) removal.getValue())).build();

    // We use these as highlighters
    private static final IBlockState stainedGlassYellow = IBlockState.create(Blocks.stained_glass, 11);
    private static final IBlockState stainedGlassRed = IBlockState.create(Blocks.stained_glass_pane, 1);
    private static final IBlockState stainedGlassWhite = IBlockState.create(Blocks.stained_glass_pane, 15);

    public static void setInventoryCache(Multiset<UniqueItem> cache) {
        ToolRenders.cacheInventory.setCache(cache);
    }

    public static void updateInventoryCache() {
        cacheInventory.forceUpdate();
    }

    public static void renderBuilderOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack heldItem) {
        // Calculate the players current position, which is needed later
        Vec3 playerPos = ToolRenders.Utils.getPlayerglTranslatef(player, evt.partialTicks);

        // Render if we have a remote inventory selected
        renderLinkedInventoryOutline(heldItem, playerPos, player);

        MovingObjectPosition lookingAt = VectorTools.getLookingAt(player, heldItem);
        List<BlockPos> coordinates = getAnchor(heldItem);

        if (lookingAt == null && coordinates.size() == 0)
            return;

        IBlockState startBlock = ToolRenders.Utils.getStartBlock(lookingAt, player);
        if (startBlock.getBlock() == ModBlocks.effectBlock)
            return;
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        IBlockState renderBlockState = getToolBlock(heldItem);

        //Don't render anything if there is no block selected (Air)
        if (renderBlockState == IBlockState.AIR_STATE)
            return;

        //Build a list of coordinates based on the tool mode and range
        if (coordinates.isEmpty() && lookingAt != null)
            coordinates = BuildingModes.collectPlacementPos(player.worldObj, player, lookingAt.getBlockPos(), lookingAt.sideHit, heldItem, lookingAt.getBlockPos());

        // Figure out how many of the block we're rendering are in the player inventory.
        ItemStack itemStack = ToolRenders.Utils.getSilkDropIfPresent(player.worldObj, renderBlockState, player);

        // Check if we have the blocks required
        long hasBlocks = InventoryManipulation.countItem(itemStack, player, cacheInventory);
        hasBlocks += InventoryManipulation.countPaste(player);

        int hasEnergy = SyncedConfig.energyMax == 0 ? Integer.MAX_VALUE : ToolRenders.Utils.getStackEnergy(heldItem, player);

        // Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
        Set<BlockPos> coords = new HashSet<>(coordinates);
        fakeWorld.setWorldAndState(player.worldObj, renderBlockState, coords);

        GL11.glPushMatrix();
        ToolRenders.Utils.stateManagerPrepareBlend();

        // Render all the raw blocks
        coordinates.forEach(coordinate -> {
            GL11.glPushMatrix();
            ToolRenders.Utils.stateManagerPrepare(playerPos, coordinate, null);
            GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering

            IBlockState state = IBlockState.AIR_STATE;
            if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES)
                state = renderBlockState.getActualState(fakeWorld, coordinate);

            mc.getBlockRendererDispatcher().renderBlockBrightness(state, 1f);//Render the defined block
            GL11.glPopMatrix();
        });

        // Render if the block can be built or not
        for (BlockPos coordinate : coordinates) {
            GL11.glPushMatrix();
            ToolRenders.Utils.stateManagerPrepare(playerPos, coordinate, 0.01f);
            GL11.glScalef(1.006f, 1.006f, 1.006f);
            GL14.glBlendColor(1F, 1F, 1F, 0.35f);

            hasBlocks--;
            if (heldItem.hasCapability(CapabilityEnergy.ENERGY, null))
                hasEnergy -= ModItems.gadgetBuilding.getEnergyCost(heldItem);
            else
                hasEnergy -= ModItems.gadgetBuilding.getDamageCost(heldItem);

            if (hasBlocks < 0 || hasEnergy < 0)
                mc.getBlockRendererDispatcher().renderBlockBrightness(stainedGlassRed, 1f);

            // Move the render position back to where it was
            GL11.glPopMatrix();
        }

        //Set blending back to the default mode
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());
        //Disable blend
        GL11.glDisable(GL11.GL_BLEND);
        //Pop from the original push in this method
        GL11.glPopMatrix();
    }

    public static void renderExchangerOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack heldItem) {
        // Calculate the players current position, which is needed later
        Vec3 playerPos = ToolRenders.Utils.getPlayerglTranslatef(player, evt.partialTicks);

        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
        renderLinkedInventoryOutline(heldItem, playerPos, player);

        MovingObjectPosition lookingAt = VectorTools.getLookingAt(player, heldItem);
        IBlockState state = IBlockState.AIR_STATE;
        List<BlockPos> coordinates = getAnchor(heldItem);

        if (lookingAt == null && coordinates.size() == 0)
            return;

        IBlockState startBlock = ToolRenders.Utils.getStartBlock(lookingAt, player);
        if (startBlock == ModBlocks.effectBlock.getDefaultState())
            return;

        IBlockState renderBlockState = getToolBlock(heldItem);
        Minecraft mc = Minecraft.getMinecraft();
        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        if (renderBlockState == IBlockState.AIR_STATE) {//Don't render anything if there is no block selected (Air)
            return;
        }
        if (coordinates.size() == 0 && lookingAt != null) { //Build a list of coordinates based on the tool mode and range
            coordinates = ExchangingModes.collectPlacementPos(player.worldObj, player, lookingAt.getBlockPos(), lookingAt.sideHit, heldItem, lookingAt.getBlockPos());
        }

        // Figure out how many of the block we're rendering we have in the inventory of the player.
        ItemStack itemStack = ToolRenders.Utils.getSilkDropIfPresent(player.worldObj, renderBlockState, player);

        long hasBlocks = InventoryManipulation.countItem(itemStack, player, cacheInventory);
        hasBlocks = hasBlocks + InventoryManipulation.countPaste(player);
        int hasEnergy = SyncedConfig.energyMax == 0 ? Integer.MAX_VALUE : ToolRenders.Utils.getStackEnergy(heldItem, player);

        // Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
        Set<BlockPos> coords = new HashSet<>(coordinates);
        fakeWorld.setWorldAndState(player.worldObj, renderBlockState, coords);

        GL11.glPushMatrix();
        ToolRenders.Utils.stateManagerPrepareBlend();

        for (BlockPos coordinate : coordinates) {
            GL11.glPushMatrix();
            ToolRenders.Utils.stateManagerPrepare(playerPos, coordinate, 0.001f);
            GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering

            // Get the block state in the fake world
            if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                state = renderBlockState.getActualState(fakeWorld, coordinate);
            }

            if (renderBlockState.getRenderType() != EnumBlockRenderType.INVISIBLE) {
                try {
                    dispatcher.renderBlockBrightness(state, 1f);//Render the defined block
                } catch (NullPointerException ex) {
                    // This is to stop crashes with blocks that have not been implemented
                    // correctly by their mod authors.
                    BuildingGadgets.logger.error(ToolRenders.class.getSimpleName() + ": Error within overlay rendering -> " + ex);
                }

                GL11.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            }

            GL14.glBlendColor(1F, 1F, 1F, 0.1f); //Set the alpha of the blocks we are rendering
            dispatcher.renderBlockBrightness(stainedGlassWhite, 1f);//Render the defined block - White glass to show non-full block renders (Example: Torch)
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            ToolRenders.Utils.stateManagerPrepare(playerPos, coordinate, 0.002f);

            GL11.glScalef(1.02f, 1.02f, 1.02f); //Slightly Larger block to avoid z-fighting.
            GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
            hasBlocks--;

            if (heldItem.hasCapability(CapabilityEnergy.ENERGY, null))
                hasEnergy -= ModItems.gadgetExchanger.getEnergyCost(heldItem);
            else
                hasEnergy -= ModItems.gadgetExchanger.getDamageCost(heldItem);

            if (hasBlocks < 0 || hasEnergy < 0)
                dispatcher.renderBlockBrightness(stainedGlassRed, 1f);

            // Move the render position back to where it was
            GL11.glPopMatrix();
        }

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void renderDestructionOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
        MovingObjectPosition lookingAt = VectorTools.getLookingAt(player, stack);
        if (lookingAt == null && GadgetDestruction.getAnchor(stack) == null)
            return;
        World world = player.worldObj;
        BlockPos startBlock = (GadgetDestruction.getAnchor(stack) == null) ? lookingAt.getBlockPos() : GadgetDestruction.getAnchor(stack);
        EnumFacing facing = (GadgetDestruction.getAnchorSide(stack) == null) ? lookingAt.sideHit : GadgetDestruction.getAnchorSide(stack);
        if (startBlock == ModBlocks.effectBlock.getDefaultState())
            return;

        if (!GadgetDestruction.getOverlay(stack))
            return;
        GL11.glPushMatrix();
        double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.partialTicks;
        double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.partialTicks;
        double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.partialTicks;
        GL11.glTranslatef(-doubleX, -doubleY, -doubleZ);
        try {
            GL11.callList(cacheDestructionOverlay.get(new ImmutableTriple<>(new UniqueItemStack(stack), startBlock, facing.ordinal()), () -> {
                int displayList = GLAllocation.generateDisplayLists(1);
                GL11.glNewList(displayList, GL11.GL_COMPILE);
                renderDestructionOverlay(player, world, startBlock, facing, stack);
                GL11.glEndList();
                return displayList;
            }));
        } catch (ExecutionException e) {
            BuildingGadgets.logger.error("Error encountered while rendering destruction gadget overlay", e);
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private static void renderDestructionOverlay(EntityPlayer player, World world, BlockPos startBlock, EnumFacing facing, ItemStack heldItem) {
        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        Set<BlockPos> coordinates = GadgetDestruction.getArea(world, startBlock, facing, player, heldItem);

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.SourceFactor.SRC_ALPHA, GL11.DestFactor.ONE_MINUS_SRC_ALPHA, GL11.SourceFactor.ONE, GL11.DestFactor.ZERO);

        List<BlockPos> sortedCoordinates = Sorter.Blocks.byDistance(coordinates, player); //Sort the coords by distance to player.

        Tessellator t = Tessellator.getInstance();
        BufferBuilder bufferBuilder = t.getBuffer();

        for (BlockPos coordinate : sortedCoordinates) {
            boolean invisible = true;
            IBlockState state = world.getBlockState(coordinate);
            for (EnumFacing side : EnumFacing.values()) {
                if (state.shouldSideBeRendered(world, coordinate, side)) {
                    invisible = false;
                    break;
                }
            }

            if (invisible)
                continue;

            GL11.glPushMatrix();//Push matrix again just because
            GL11.glTranslatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
            GL11.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GL11.glTranslatef(-0.005f, -0.005f, 0.005f);
            GL11.glScalef(1.01f, 1.01f, 1.01f);//Slightly Larger block to avoid z-fighting.

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            renderBoxSolid(t, bufferBuilder, 0, 0, -1, 1, 1, 0, 1, 0, 0, 0.5f);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void renderPasteOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
        //Calculate the players current position, which is needed later
        Vec3 playerPos = ToolRenders.Utils.getPlayerglTranslatef(player, evt.partialTicks);

        renderLinkedInventoryOutline(stack, playerPos, player);
        if (ModItems.gadgetCopyPaste.getStartPos(stack) == null || ModItems.gadgetCopyPaste.getEndPos(stack) == null)
            return;

        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        String UUID = ModItems.gadgetCopyPaste.getUUID(stack);
        World world = player.worldObj;
        if (GadgetCopyPaste.getToolMode(stack) == GadgetCopyPaste.ToolMode.Paste) {
            //First check if we have an anchor, if not check if we're looking at a block, if not, exit
            BlockPos startPos = GadgetCopyPaste.getAnchor(stack);
            if (startPos == null) {
                startPos = VectorTools.getPosLookingAt(player, stack);
                if (startPos == null)
                    return;
                startPos = startPos.up(GadgetCopyPaste.getY(stack));
                startPos = startPos.east(GadgetCopyPaste.getX(stack));
                startPos = startPos.south(GadgetCopyPaste.getZ(stack));
            } else {
                startPos = startPos.up(GadgetCopyPaste.getY(stack));
                startPos = startPos.east(GadgetCopyPaste.getX(stack));
                startPos = startPos.south(GadgetCopyPaste.getZ(stack));
            }

            //We store our buffers in PasteToolBufferBuilder (A client only class) -- retrieve the buffer from this locally cache'd map
            ToolDireBuffer toolDireBuffer = PasteToolBufferBuilder.getBufferFromMap(UUID);
            if (toolDireBuffer == null) {
                return;
            }
            //Also get the blockMapList from the local cache - If either the buffer or the blockmap list are empty, exit.
            List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
            if (toolDireBuffer.getVertexCount() == 0 || blockMapList.size() == 0) {
                return;
            }

            //Don't draw on top of blocks being built by our tools.
            IBlockState startBlock = world.getBlockState(startPos);
            if (startBlock == ModBlocks.effectBlock.getDefaultState())
                return;

            //Save the current position that is being rendered
            GL11.glPushMatrix();

            //Enable Blending (So we can have transparent effect)
            GL11.glEnable(GL11.GL_BLEND);

            //This blend function allows you to use a constant alpha, which is defined later
            GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

            GL11.glPushMatrix();//Push matrix again just because
            GL11.glTranslatef(startPos.getX() - playerPos.x, startPos.getY() - playerPos.y, startPos.getZ() - playerPos.z);//Now move the render position to the coordinates we want to render at
            GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering

            GL11.glTranslatef(0.0005f, 0.0005f, -0.0005f);
            GL11.glScalef(0.999f, 0.999f, 0.999f);//Slightly Larger block to avoid z-fighting.
            PasteToolBufferBuilder.draw(player, playerPos.x, playerPos.y, playerPos.z, startPos, UUID); //Draw the cached buffer in the world.

            GL11.glPopMatrix();

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();

        } else {
            BlockPos startPos = ModItems.gadgetCopyPaste.getStartPos(stack);
            BlockPos endPos = ModItems.gadgetCopyPaste.getEndPos(stack);
            BlockPos blankPos = new BlockPos(0, 0, 0);
            if (startPos == null || endPos == null || startPos.equals(blankPos) || endPos.equals(blankPos)) {
                return;
            }

            List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
            if (blockMapList.size() == 0)
                return;

            // We want to draw from the starting position to the (ending position)+1
            int x = (startPos.getX() <= endPos.getX()) ? startPos.getX() : endPos.getX();
            int y = (startPos.getY() <= endPos.getY()) ? startPos.getY() : endPos.getY();
            int z = (startPos.getZ() <= endPos.getZ()) ? startPos.getZ() : endPos.getZ();
            int dx = (startPos.getX() > endPos.getX()) ? startPos.getX() + 1 : endPos.getX() + 1;
            int dy = (startPos.getY() > endPos.getY()) ? startPos.getY() + 1 : endPos.getY() + 1;
            int dz = (startPos.getZ() > endPos.getZ()) ? startPos.getZ() + 1 : endPos.getZ() + 1;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            GL11.glPushMatrix();
            GL11.glTranslated(-playerPos.xCoord, -playerPos.yCoord, -playerPos.zCoord);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendFuncSeparate(GL11.SourceFactor.SRC_ALPHA, GL11.DestFactor.ONE_MINUS_SRC_ALPHA, GL11.SourceFactor.ONE, GL11.DestFactor.ZERO);

            renderBox(tessellator, bufferbuilder, x, y, z, dx, dy, dz, 255, 223, 127); // Draw the box around the blocks we've copied.

            GL11.glLineWidth(1.0F);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.depthMask(true);

            GL11.glPopMatrix();
        }
    }

    private static void renderLinkedInventoryOutline(ItemStack item, Vec3 playerPos, EntityPlayer player) {
        Integer dim = GadgetUtils.getDIMFromNBT(item, "boundTE");
        BlockPos pos = GadgetUtils.getPOSFromNBT(item, "boundTE");

        if (dim == null || pos == null)
            return;

        if (player.dimension != dim)
            return;

        GL11.glPushMatrix();
        ToolRenders.Utils.stateManagerPrepare(playerPos, pos, 0.0005f);
        ToolRenders.Utils.stateManagerPrepareBlend();
        GL14.glBlendColor(1F, 1F, 1F, 0.35f);

        // Render the overlay
        mc.getBlockRendererDispatcher().renderBlockBrightness(stainedGlassYellow, 1f);
        GL11.glPopMatrix();
    }

    private static void renderBox(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, int R, int G, int B) {
        GL11.glLineWidth(2.0F);
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        tessellator.set
        bufferBuilder.pos(startX, startY, startZ).color(G, G, G, 0.0F).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, B, B, R).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(B, B, G, R).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(B, G, B, R).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, G, G, 0.0F).endVertex();
        tessellator.draw();
        GL11.glLineWidth(1.0F);
    }

    private static void renderBoxSolid(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, float red, float green, float blue, float alpha) {
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        //down
        bufferBuilder.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //up
        bufferBuilder.pos(startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(red, green, blue, alpha).endVertex();

        //east
        bufferBuilder.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(red, green, blue, alpha).endVertex();

        //west
        bufferBuilder.pos(startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(red, green, blue, alpha).endVertex();

        //south
        bufferBuilder.pos(endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //north
        bufferBuilder.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }

    private static class Utils {

        private static IBlockState getStartBlock(MovingObjectPosition lookingAt, EntityPlayer player) {
            IBlockState startBlock = IBlockState.AIR_STATE;
            if (lookingAt != null)
                startBlock = player.worldObj.getBlockState(lookingAt.getBlockPos());

            return startBlock;
        }

        private static int getStackEnergy(ItemStack stack, EntityPlayer player) {
            if (player.capabilities.isCreativeMode || (!stack.hasCapability(CapabilityEnergy.ENERGY, null) && !stack.isItemStackDamageable()))
                return Integer.MAX_VALUE;

            if (stack.hasCapability(CapabilityEnergy.ENERGY, null))
                return CapabilityProviderEnergy.getCap(stack).getEnergyStored();

            return stack.getMaxDamage() - stack.getItemDamage();
        }

        /**
         * Returns a Vec3i of the players position based on partial tick.
         * Used for Render translation.
         */
        private static Vec3 getPlayerglTranslatef(EntityPlayer player, float partialTick) {
            return Vec3.createVectorHelper(
                player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTick,
                player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTick,
                player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTick
            );
        }

        /**
         * Attempts to get the Silk Touch Drop item but if it fails it'll return the original
         * non-silk touch ItemStack.
         */
        private static ItemStack getSilkDropIfPresent(World world, IBlockState state, EntityPlayer player) {
            ItemStack itemStack = ItemStack.EMPTY;
            if (state.getBlock().canSilkHarvest(world, BlockPos.ORIGIN, state, player))
                itemStack = InventoryManipulation.getSilkTouchDrop(state);

            if (itemStack.isEmpty()) {
                try {
                    itemStack = state.getBlock().getPickBlock(state, null, world, BlockPos.ORIGIN, player);
                } catch (Exception ignored) {
                    // This may introduce issues. I hope it doesn't
                    itemStack = InventoryManipulation.getSilkTouchDrop(state);
                }
            }

            return itemStack;
        }

        private static void stateManagerPrepareBlend() {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
        }

        /**
         * Prepares our render using base properties
         */
        private static void stateManagerPrepare(Vec3 playerPos, BlockPos blockPos, Float shift) {
            GL11.glTranslated(blockPos.getX() - playerPos.xCoord, blockPos.getY() - playerPos.yCoord, blockPos.getZ() - playerPos.zCoord);//Now move the render position to the coordinates we want to render at
            // Rotate it because i'm not sure why but we need to
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(1f, 1f, 1f);

            // Slightly Larger block to avoid z-fighting.
            if (shift != null) {
                GL11.glTranslatef(-shift, -shift, shift);
                GL11.glScalef(1.005f, 1.005f, 1.005f);
            }
        }
    }
}
