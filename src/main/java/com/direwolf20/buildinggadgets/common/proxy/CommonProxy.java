package com.direwolf20.buildinggadgets.common.proxy;

import com.direwolf20.buildinggadgets.client.gui.GuiProxy;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.ModSounds;
import com.direwolf20.buildinggadgets.common.blocks.*;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.building.CapabilityBlockProvider;
import com.direwolf20.buildinggadgets.common.config.CompatConfig;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.entities.ModEntities;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionChunkDense;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainerCreative;
import com.direwolf20.buildinggadgets.common.items.pastes.RegularPasteContainerTypes;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.SoundEvent;

import java.io.File;

@Mod.EventBusSubscriber
public class CommonProxy {

    private boolean applyCompatConfig = false;

    public void preInit(FMLPreInitializationEvent e) {
        ModEntities.init();
        PacketHandler.registerMessages();
        File cfgFile = new File(e.getModConfigurationDirectory(), "BuildingGadgets.cfg");
        if (cfgFile.exists()) {
            BuildingGadgets.logger.info("Preparing to migrate old config Data to new Format");
            applyCompatConfig = CompatConfig.readConfig(cfgFile);
        }
        IntegrationHandler.preInit(e);

        registerBlocks();
        registerItems();
        registerSounds();
    }

    public void init() {
        CapabilityBlockProvider.register();

        NetworkRegistry.INSTANCE.registerGuiHandler(BuildingGadgets.instance, new GuiProxy());
        if (applyCompatConfig) {
            BuildingGadgets.logger.info("Migrating old config Data.");
            CompatConfig.applyCompatConfig();
        }
        IntegrationHandler.init();
    }

    public void postInit() {
        IntegrationHandler.postInit();
    }

    public static void registerBlocks() {
        GameRegistry.registerBlock(new EffectBlock(), "effectblock");
        GameRegistry.registerBlock(new TemplateManager(), "templatemanager");
        GameRegistry.registerTileEntity(TemplateManagerTileEntity.class, "templateManager");
        if (SyncedConfig.enablePaste) {
            GameRegistry.registerBlock(new ConstructionBlockDense(), "constructionblock_dense");
            GameRegistry.registerBlock(new ConstructionBlock(), "constructionblock");
            GameRegistry.registerBlock(new ConstructionBlockPowder(), "constructionblockpowder");
            GameRegistry.registerTileEntity(ConstructionBlockTileEntity.class, "constructionblock");
        }
    }

    public static void registerItems() {
        GameRegistry.registerItem(new GadgetBuilding(), "buildingtool");
        GameRegistry.registerItem(new GadgetExchanger(), "exchangertool");
        GameRegistry.registerItem(new GadgetCopyPaste(), "copypastetool");
        GameRegistry.registerItem(createWeirdItemBlock(ModBlocks.templateManager), "templatemanager");
        GameRegistry.registerItem(new Template(), "template");
        if (SyncedConfig.enableDestructionGadget) {
            GameRegistry.registerItem(new GadgetDestruction(), "destructiontool");
        }
        if (SyncedConfig.enablePaste) {
            GameRegistry.registerItem(createWeirdItemBlock(ModBlocks.constructionBlockDense), "constructionblock_dense");
            GameRegistry.registerItem(createWeirdItemBlock(ModBlocks.constructionBlock), "constructionblock");
            GameRegistry.registerItem(createWeirdItemBlock(ModBlocks.constructionBlockPowder), "constructionblockpowder");
            GameRegistry.registerItem(new ConstructionPaste(), "constructionpaste");
            GameRegistry.registerItem(new ConstructionChunkDense(), "construction_chunk_dense");
            for (RegularPasteContainerTypes type : RegularPasteContainerTypes.values()) {
                GameRegistry.registerItem(new ConstructionPasteContainer(type.itemSuffix, type.capacitySupplier), "constructionpastecontainer" + type.itemSuffix);
            }
            GameRegistry.registerItem(new ConstructionPasteContainerCreative(), "constructionpastecontainercreative");
        }
    }

    private static ItemBlock createWeirdItemBlock(Block block) {
        ItemBlock itemBlock = new ItemBlock(block);
        itemBlock.setUnlocalizedName(block.getUnlocalizedName());
        return itemBlock;
    }

    public static void registerSounds() {
        // TODO: Maybe needs sounds.json https://github.com/GTNewHorizons/Random-Things/blob/master/src/main/resources/assets/randomthings/sounds.json , nothing else
//        for (ModSounds sound : ModSounds.values()) {
//            GameRegistry.regis.register(sound.getSound());
//        }
    }

}
