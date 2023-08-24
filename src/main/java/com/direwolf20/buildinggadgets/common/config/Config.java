package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

//hardcode this name, so that we use a different config file than before we used Annotations
public class Config {

    private static final Configuration config = new Configuration(); // TODO: correctly set path

    static final String CATEGORY_ROOT = "general";

    private static final String LANG_KEY_ROOT = "config." + BuildingGadgets.MODID + "." + CATEGORY_ROOT;

    private static final String LANG_KEY_BLACKLIST = LANG_KEY_ROOT + ".subCategoryBlacklist";

    private static final String LANG_KEY_GADGETS = LANG_KEY_ROOT + ".subCategoryGadgets";

    private static final String LANG_KEY_PASTE_CONTAINERS = LANG_KEY_ROOT + ".subCategoryPasteContainers";

    private static final String LANG_KEY_GADGET_BUILDING = LANG_KEY_GADGETS + ".gadgetBuilding";

    private static final String LANG_KEY_GADGET_EXCHANGER = LANG_KEY_GADGETS + ".gadgetExchanger";

    private static final String LANG_KEY_GADGET_DESTRUCTION = LANG_KEY_GADGETS + ".gadgetDestruction";

    private static final String LANG_KEY_GADGET_COPY_PASTE = LANG_KEY_GADGETS + ".gadgetCopyPaste";

    private static final String LANG_KEY_PASTE_CONTAINERS_CAPACITY = LANG_KEY_PASTE_CONTAINERS + ".capacity";

    private static final String LANG_KEY_GADGETS_ENERGY = LANG_KEY_GADGETS + ".energyCost";

    private static final String LANG_KEY_GADGETS_DAMAGE = LANG_KEY_GADGETS + ".damageCost";

    private static final String LANG_KEY_GADGETS_DURABILITY = LANG_KEY_GADGETS + ".durability";

    private static final String LANG_KEY_GADGETS_ENERGY_COMMENT = "The Gadget's Energy cost per Operation";

    private static final String LANG_KEY_GADGETS_DAMAGE_COMMENT = "The Gadget's Damage cost per Operation";

    private static final String LANG_KEY_GADGETS_DURABILITY_COMMENT = "The Gadget's Durability (0 means no durability is used) (Ignored if powered by FE)";

    public static double rayTraceRange = config.get(CATEGORY_ROOT, "Max Build Distance", 32, "Defines how far away you can build", 1, 48).setLanguageKey(LANG_KEY_ROOT + ".rayTraceRange").getDouble();

    public static boolean poweredByFE = config.get(CATEGORY_ROOT, "Powered by Forge Energy", true, "Set to true for Forge Energy Support, set to False for vanilla Item Damage").setLanguageKey(LANG_KEY_ROOT + ".poweredByFE").setLanguageKey(LANG_KEY_ROOT + ".poweredByFE").setRequiresWorldRestart(true).getBoolean();

    public static boolean enablePaste = config.get(CATEGORY_ROOT, "Enable Construction Paste", true, "Set to false to disable the recipe for construction paste.").setLanguageKey(LANG_KEY_ROOT + ".paste.enabled").setRequiresMcRestart(true).setRequiresWorldRestart(true).getBoolean();

    public static int pasteDroppedMin = config.get(CATEGORY_ROOT, "Construction Paste Drop Count - Min", 1, "The minimum number of construction paste items dropped by a dense construction block.", 0, Integer.MAX_VALUE).setLanguageKey(LANG_KEY_ROOT + ".paste.dropped.min").getInt();

    public static int pasteDroppedMax = config.get(CATEGORY_ROOT, "Construction Paste Drop Count - Max", 3, "The maximum number of construction paste items dropped by a dense construction block.", 0, Integer.MAX_VALUE).setLanguageKey(LANG_KEY_ROOT + ".paste.dropped.max").getInt();

    public static boolean enableDestructionGadget = config.get(CATEGORY_ROOT, "Enable Destruction Gadget", true, "Set to false to disable the Destruction Gadget.").setLanguageKey(LANG_KEY_ROOT + ".enableDestructionGadget").setRequiresMcRestart(true).setRequiresWorldRestart(true).getBoolean();

    public static boolean absoluteCoordDefault = config.get(CATEGORY_ROOT, "Default to absolute Coord-Mode", false, "Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default.\nSet to true for Absolute, set to False for Relative.").setLanguageKey(LANG_KEY_ROOT + ".absoluteCoordDefault").getBoolean();

    public static boolean allowAbsoluteCoords = config.get(CATEGORY_ROOT, "Allow absolute Coord-Mode", true, "Disable absolute coords-mode for the Copy-Paste gadget").setLanguageKey(LANG_KEY_ROOT + ".allowAbsoluteCoords").getBoolean();

    public static boolean canOverwriteBlocks = config.get(CATEGORY_ROOT, "Allow non-Air-Block-Overwrite", true, "Whether the Building / CopyPaste Gadget can overwrite blocks like water, lava, grass, etc (like a player can).\nFalse will only allow it to overwrite air blocks.").setLanguageKey(LANG_KEY_ROOT + ".canOverwriteBlocks").getBoolean();


    @Name("Blacklist Settings")
    @Comment("Configure your Blacklist-Settings here")
    @LangKey(LANG_KEY_BLACKLIST)
    public static CategoryBlacklist subCategoryBlacklist = new CategoryBlacklist();

    public static final class CategoryBlacklist {
        //In 1.13 this should be converted to a tag (or at least made compatible with)
        public String[] blockBlacklist = config.get(CATEGORY_BLACKLIST, "Blacklisted Blocks", new String[]{"minecraft:.*_door.*", PatternList.getName(Blocks.piston_head), "astralsorcery:blockflarelight"}, "All Blocks added to this will be treated similar to TileEntities. Not at all.\nNotice that you can use Regular Expressions as defined by Java Patterns to express more complex name combinations.\nUse for example \"awfulmod:.*\" to blacklist all awfulmod Blocks.").setLanguageKey(LANG_KEY_BLACKLIST + " + blockBlacklist").getStringList();
    }

    @Name("Gadgets")
    @Comment("Configure the Gadgets here")
    @LangKey(LANG_KEY_GADGETS)
    public static CategoryGadgets subCategoryGadgets = new CategoryGadgets();

    @Name("Paste Containers")
    @Comment("Configure the Paste Containers here")
    @LangKey(LANG_KEY_PASTE_CONTAINERS)
    public static CategoryPasteContainers subCategoryPasteContainers = new CategoryPasteContainers();

    //using unistantiable final class instead of enum, so that it doesn't cause issues with the ConfigManger trying to access the Instance field
    //No defense against reflection needed here (I think)
    public static final class CategoryGadgets {
        private CategoryGadgets() {
        }

        public int maxRange = config.get(CATEGORY_GADGETS, "Maximum allowed Range", 15, "The max range of the Gadgets", 1, 25).setLanguageKey(LANG_KEY_GADGETS + ".maxRange").getInt();

        public int maxEnergy = config.get(CATEGORY_GADGETS, "Maximum Energy", 500000, "The max energy of Building, Exchanging & Copy-Paste Gadget", 0, Integer.MAX_VALUE).setLanguageKey(LANG_KEY_GADGETS + ".maxEnergy").getInt();

        @Name("Building Gadget")
        @Comment("Energy Cost & Durability of the Building Gadget")
        @LangKey(LANG_KEY_GADGET_BUILDING)
        public CategoryGadgetBuilding subCategoryGadgetBuilding = new CategoryGadgetBuilding();


        @Name("Exchanging Gadget")
        @Comment("Energy Cost & Durability of the Exchanging Gadget")
        @LangKey(LANG_KEY_GADGET_EXCHANGER)
        public CategoryGadgetExchanger subCategoryGadgetExchanger = new CategoryGadgetExchanger();

        @Name("Destruction Gadget")
        @Comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")
        @LangKey(LANG_KEY_GADGET_DESTRUCTION)
        public CategoryGadgetDestruction subCategoryGadgetDestruction = new CategoryGadgetDestruction();

        @Name("Copy-Paste Gadget")
        @Comment("Energy Cost & Durability of the Copy-Paste Gadget")
        @LangKey(LANG_KEY_GADGET_COPY_PASTE)
        public CategoryGadgetCopyPaste subCategoryGadgetCopyPaste = new CategoryGadgetCopyPaste();

        public static final class CategoryGadgetBuilding {
            private CategoryGadgetBuilding() {
            }

            public int energyCostBuilder = config.get(CATEGORY_GADGET_BUILDING, "Energy Cost", 50, LANG_KEY_GADGETS_ENERGY_COMMENT, 0, 100000).setLanguageKey(LANG_KEY_GADGETS_ENERGY).getInt();

            public int damageCostBuilder = config.get(CATEGORY_GADGET_BUILDING, "Damage Cost", 1, LANG_KEY_GADGETS_DAMAGE_COMMENT, 0, 2000).setLanguageKey(LANG_KEY_GADGETS_DAMAGE).getInt();

            public int durabilityBuilder = config.get(CATEGORY_GADGET_BUILDING, "Durability", 10000, LANG_KEY_GADGETS_DURABILITY_COMMENT, 0, 100000).setLanguageKey(LANG_KEY_GADGETS_DURABILITY).setRequiresWorldRestart(true).getInt();
        }

        public static final class CategoryGadgetExchanger {
            private CategoryGadgetExchanger() {
            }

            public int energyCostExchanger = config.get(CATEGORY_GADGET_EXCHANGER, "Energy Cost", 100, LANG_KEY_GADGETS_ENERGY_COMMENT, 0, 100000).setLanguageKey(LANG_KEY_GADGETS_ENERGY).getInt();

            public int damageCostExchanger = config.get(CATEGORY_GADGET_EXCHANGER, "Damage Cost", 2, LANG_KEY_GADGETS_DAMAGE_COMMENT, 0, 2000).setLanguageKey(LANG_KEY_GADGETS_DAMAGE).getInt();

            public int durabilityExchanger = config.get(CATEGORY_GADGET_EXCHANGER, "Durability", 10000, LANG_KEY_GADGETS_DURABILITY_COMMENT, 0, 100000).setLanguageKey(LANG_KEY_GADGETS_DURABILITY).setRequiresWorldRestart(true).getInt();
        }

        public static final class CategoryGadgetDestruction {
            private CategoryGadgetDestruction() {
            }

            public int energyMaxDestruction = config.get(CATEGORY_GADGET_DESTRUCTION, "Maximum Energy", 1000000, "The max energy of the Destruction Gadget", 0, Integer.MAX_VALUE).setLanguageKey(LANG_KEY_GADGET_DESTRUCTION + ".maxEnergy").getInt();

            public int energyCostDestruction = config.get(CATEGORY_GADGET_DESTRUCTION, "Energy Cost", 200, LANG_KEY_GADGETS_ENERGY_COMMENT, 0, 100000).setLanguageKey(LANG_KEY_GADGETS_ENERGY).getInt();

            public int damageCostDestruction = config.get(CATEGORY_GADGET_DESTRUCTION, "Damage Cost", 2, LANG_KEY_GADGETS_DAMAGE_COMMENT, 0, 2000).setLanguageKey(LANG_KEY_GADGETS_DAMAGE).getInt();

            public int durabilityDestruction = config.get(CATEGORY_GADGET_DESTRUCTION, "Durability", 10000, LANG_KEY_GADGETS_DURABILITY_COMMENT, 0, 100000).setLanguageKey(LANG_KEY_GADGETS_DURABILITY).setRequiresWorldRestart(true).getInt();

            public double nonFuzzyMultiplier = config.get(CATEGORY_GADGET_DESTRUCTION, "Non-Fuzzy Mode Multiplier", 2, "The cost in energy/durability will increase by this amount when not in fuzzy mode", 0, Double.MAX_VALUE).setLanguageKey(LANG_KEY_GADGET_DESTRUCTION + ".nonfuzzy.multiplier").getDouble();

            public boolean nonFuzzyEnabled = config.get(CATEGORY_GADGET_DESTRUCTION, "Non-Fuzzy Mode Enabled", false, "If enabled, the Destruction Gadget can be taken out of fuzzy mode, allowing only instances of the block clicked to be removed (at a higher cost)").setLanguageKey(LANG_KEY_GADGET_DESTRUCTION + ".nonfuzzy.enabled").getBoolean();
        }

        public static final class CategoryGadgetCopyPaste {
            private CategoryGadgetCopyPaste() {
            }

            public int energyCostCopyPaste = config.get(CATEGORY_GADGET_COPY_PASTE, "Energy Cost", 50, LANG_KEY_GADGETS_ENERGY_COMMENT, 0, 100000).setLanguageKey(LANG_KEY_GADGETS_ENERGY).getInt();

            public int damageCostCopyPaste = config.get(CATEGORY_GADGET_COPY_PASTE, "Damage Cost", 1, LANG_KEY_GADGETS_DAMAGE_COMMENT, 0, 2000).setLanguageKey(LANG_KEY_GADGETS_DAMAGE).getInt();

            public int durabilityCopyPaste = config.get(CATEGORY_GADGET_COPY_PASTE, "Durability", 10000, LANG_KEY_GADGETS_DURABILITY_COMMENT, 0, 100000).setLanguageKey(LANG_KEY_GADGETS_DURABILITY).setRequiresWorldRestart(true).getInt();
        }
    }

    public static final class CategoryPasteContainers {
        private CategoryPasteContainers() {
        }

        public int t1Capacity = config.get(CATEGORY_PASTE_CONTAINERS, "T1 Container Capacity", 512, "The maximum capacity of a tier 1 (iron) Construction Paste Container", 1, Integer.MAX_VALUE).setLanguageKey(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t1").getInt();

        public int t2Capacity = config.get(CATEGORY_PASTE_CONTAINERS, "T2 Container Capacity", 2048, "The maximum capacity of a tier 2 (gold) Construction Paste Container", 1, Integer.MAX_VALUE).setLanguageKey(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t2").getInt();

        public int t3Capacity = config.get(CATEGORY_PASTE_CONTAINERS, "T3 Container Capacity", 8192, "The maximum capacity of a tier 3 (diamond) Construction Paste Container", 1, Integer.MAX_VALUE).setLanguageKey(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t3").getInt();
    }

}
