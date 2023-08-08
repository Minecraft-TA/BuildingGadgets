package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.Minecraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@GameRegistry.ObjectHolder(BuildingGadgets.MODID)
public class ModBlocks {

    @GameRegistry.ObjectHolder("effectblock")
    public static EffectBlock effectBlock;
    @GameRegistry.ObjectHolder("constructionblock")
    public static ConstructionBlock constructionBlock;
    @GameRegistry.ObjectHolder("constructionblock_dense")
    public static ConstructionBlockDense constructionBlockDense;
    @GameRegistry.ObjectHolder("constructionblockpowder")
    public static ConstructionBlockPowder constructionBlockPowder;
    @GameRegistry.ObjectHolder("templatemanager")
    public static TemplateManager templateManager;

    @SideOnly(Side.CLIENT)
    public static void initColorHandlers() {
        BlockColor blockColors = Minecraft.getMinecraft().getBlockColors(); //TODO no idea
        if (SyncedConfig.enablePaste) {constructionBlock.initColorHandler(blockColors);}
    }
}
