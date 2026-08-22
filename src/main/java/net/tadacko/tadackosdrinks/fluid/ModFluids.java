package net.tadacko.tadackosdrinks.fluid;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.WortCauldronBlock;
import net.tadacko.tadackosdrinks.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, TadackosDrinks.MOD_ID);

    /**
     * Every fluid registered through {@link #register} is added here automatically.
     * WortCauldronInteraction.bootstrap() iterates this list.
     */
    public static final List<FluidEntry> ALL_FLUIDS = new ArrayList<>();

    public record FluidEntry(
            RegistryObject<FlowingFluid> source,
            RegistryObject<FlowingFluid> flowing,
            RegistryObject<LiquidBlock> block,
            RegistryObject<Item> bucket,
            RegistryObject<Block> cauldron,
            Map<Item, CauldronInteraction> cauldronInteractions,
            ForgeFlowingFluid.Properties fluidProps) {}

    // Registers a fluid together with its liquid block, bucket item, and cauldron block.
    @SuppressWarnings("unchecked")
    private static FluidEntry register(String name, Supplier<? extends FluidType> fluidType) {
        // Placeholder array so block/bucket/props lambdas can reference the source
        // fluid before it is assigned — all lambdas are evaluated lazily at registry
        // time, by which point refs[0] is a fully assigned RegistryObject.
        RegistryObject<FlowingFluid>[] refs = new RegistryObject[2];

        // Interaction map created here and shared with the cauldron block so that
        // WortCauldronInteraction.bootstrap() can populate it after registry events.
        Map<Item, CauldronInteraction> interactionMap = CauldronInteraction.newInteractionMap();

        RegistryObject<LiquidBlock> block = ModBlocks.BLOCKS.register(name + "_block",
                () -> new LiquidBlock(refs[0], BlockBehaviour.Properties.copy(Blocks.WATER)));

        RegistryObject<Item> bucket = ModItems.ITEMS.register(name + "_bucket",
                () -> new BucketItem(refs[0],
                        new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

        RegistryObject<Block> cauldron = ModBlocks.BLOCKS.register(name + "_cauldron",
                () -> new WortCauldronBlock(
                        BlockBehaviour.Properties.copy(Blocks.CAULDRON).requiresCorrectToolForDrops().lootFrom(() -> Blocks.CAULDRON),
                        interactionMap));

        ForgeFlowingFluid.Properties props = new ForgeFlowingFluid.Properties(
                fluidType, () -> refs[0].get(), () -> refs[1].get())
                .block(block).bucket(bucket);

        refs[0] = FLUIDS.register(name + "_fluid", () -> new ForgeFlowingFluid.Source(props));
        refs[1] = FLUIDS.register("flowing_" + name, () -> new ForgeFlowingFluid.Flowing(props));

        FluidEntry entry = new FluidEntry(refs[0], refs[1], block, bucket, cauldron, interactionMap, props);
        ALL_FLUIDS.add(entry);
        return entry;
    }

    // Beer
    public static final FluidEntry WORT_WHEAT = register("wort_wheat", ModFluidsTypes.WORT_FLUID_TYPE);
    public static final FluidEntry WORT_WHEAT_HOPPED = register("wort_wheat_hopped", ModFluidsTypes.WORT_FLUID_TYPE);
    public static final FluidEntry WORT_WHEAT_BOILED = register("wort_wheat_boiled", ModFluidsTypes.WORT_FLUID_TYPE);
    public static final FluidEntry WORT_WHEAT_BOILED_HOPPED = register("wort_wheat_boiled_hopped", ModFluidsTypes.WORT_FLUID_TYPE);
    public static final FluidEntry WORT_BARLEY = register("wort_barley", ModFluidsTypes.WORT_FLUID_TYPE);
    public static final FluidEntry WORT_BARLEY_HOPPED = register("wort_barley_hopped", ModFluidsTypes.WORT_FLUID_TYPE);
    public static final FluidEntry WORT_BARLEY_BOILED = register("wort_barley_boiled", ModFluidsTypes.WORT_FLUID_TYPE);
    public static final FluidEntry WORT_BARLEY_BOILED_HOPPED = register("wort_barley_boiled_hopped", ModFluidsTypes.WORT_FLUID_TYPE);

    public static final FluidEntry WASH_WHEAT = register("wash_wheat", ModFluidsTypes.BEER_FLUID_TYPE);
    public static final FluidEntry BEER_WHEAT = register("beer_wheat", ModFluidsTypes.BEER_FLUID_TYPE);
    public static final FluidEntry BEER_WHEAT_HOPPED = register("beer_wheat_hopped", ModFluidsTypes.BEER_FLUID_TYPE);
    public static final FluidEntry WASH_BARLEY = register("wash_barley", ModFluidsTypes.BEER_FLUID_TYPE);
    public static final FluidEntry BEER_BARLEY = register("beer_barley", ModFluidsTypes.BEER_FLUID_TYPE);
    public static final FluidEntry BEER_BARLEY_HOPPED = register("beer_barley_hopped", ModFluidsTypes.BEER_FLUID_TYPE);

    // Wine
    public static final FluidEntry MUST_RED = register("must_red", ModFluidsTypes.MUST_RED_FLUID_TYPE);
    public static final FluidEntry MUST_RED_FERMENTED = register("must_red_fermented", ModFluidsTypes.MUST_RED_FLUID_TYPE);
    public static final FluidEntry JUICE_GRAPE_ROSE = register("juice_grape_rose", ModFluidsTypes.WINE_ROSE_FLUID_TYPE);
    public static final FluidEntry MUST_WHITE = register("must_white", ModFluidsTypes.MUST_WHITE_FLUID_TYPE);
    public static final FluidEntry MUST_WHITE_FERMENTED = register("must_white_fermented", ModFluidsTypes.MUST_WHITE_FLUID_TYPE);
    public static final FluidEntry JUICE_GRAPE_WHITE = register("juice_grape_white", ModFluidsTypes.WINE_WHITE_FLUID_TYPE);

    public static final FluidEntry WINE_RED = register("wine_red", ModFluidsTypes.WINE_RED_FLUID_TYPE);
    public static final FluidEntry WINE_RED_AGED = register("wine_red_aged", ModFluidsTypes.WINE_RED_FLUID_TYPE);
    public static final FluidEntry WINE_ROSE = register("wine_rose", ModFluidsTypes.WINE_ROSE_FLUID_TYPE);
    public static final FluidEntry WINE_ROSE_AGED = register("wine_rose_aged", ModFluidsTypes.WINE_ROSE_FLUID_TYPE);
    public static final FluidEntry WINE_ORANGE = register("wine_orange", ModFluidsTypes.WINE_ORANGE_FLUID_TYPE);
    public static final FluidEntry WINE_ORANGE_AGED = register("wine_orange_aged", ModFluidsTypes.WINE_ORANGE_FLUID_TYPE);
    public static final FluidEntry WINE_WHITE = register("wine_white", ModFluidsTypes.WINE_WHITE_FLUID_TYPE);
    public static final FluidEntry WINE_WHITE_AGED = register("wine_white_aged", ModFluidsTypes.WINE_WHITE_FLUID_TYPE);

    // Cider
    public static final FluidEntry MUST_APPLE = register("must_apple", ModFluidsTypes.MUST_APPLE_FLUID_TYPE);
    public static final FluidEntry JUICE_APPLE = register("juice_apple", ModFluidsTypes.CIDER_FLUID_TYPE);

    public static final FluidEntry CIDER = register("cider", ModFluidsTypes.CIDER_FLUID_TYPE);
    public static final FluidEntry CIDER_AGED = register("cider_aged", ModFluidsTypes.CIDER_FLUID_TYPE);

    // Mead
    public static final FluidEntry DILUTED_HONEY = register("diluted_honey", ModFluidsTypes.MEAD_FLUID_TYPE);

    public static final FluidEntry MEAD = register("mead", ModFluidsTypes.MEAD_FLUID_TYPE);
    public static final FluidEntry MEAD_AGED = register("mead_aged", ModFluidsTypes.MEAD_FLUID_TYPE);

    // Spirit
    public static final FluidEntry SPIRIT_WHEAT_LOW = register("spirit_wheat_low", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_WHEAT_MID = register("spirit_wheat_mid", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_WHEAT_HIGH = register("spirit_wheat_high", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_BARLEY_LOW = register("spirit_barley_low", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_BARLEY_MID = register("spirit_barley_mid", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_BARLEY_HIGH = register("spirit_barley_high", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_GRAPE_LOW = register("spirit_grape_low", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_GRAPE_MID = register("spirit_grape_mid", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_GRAPE_HIGH = register("spirit_grape_high", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_APPLE_LOW = register("spirit_apple_low", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_APPLE_MID = register("spirit_apple_mid", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_APPLE_HIGH = register("spirit_apple_high", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_HONEY_LOW = register("spirit_honey_low", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_HONEY_MID = register("spirit_honey_mid", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_HONEY_HIGH = register("spirit_honey_high", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_SUGARCANE_JUICE_LOW = register("spirit_sugarcane_juice_low", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_SUGARCANE_JUICE_MID = register("spirit_sugarcane_juice_mid", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    //public static final FluidEntry SPIRIT_SUGARCANE_JUICE_HIGH = register("spirit_sugarcane_juice_high", ModFluidsTypes.SPIRIT_FLUID_TYPE); // this is CONCENTRATED_RUM_JUICE
    public static final FluidEntry SPIRIT_SUGARCANE_MOLASSES_LOW = register("spirit_sugarcane_molasses_low", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_SUGARCANE_MOLASSES_MID = register("spirit_sugarcane_molasses_mid", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    //public static final FluidEntry SPIRIT_SUGARCANE_MOLASSES_HIGH = register("spirit_sugarcane_molasses_high", ModFluidsTypes.SPIRIT_FLUID_TYPE); // this is CONCENTRATED_RUM_MOLASSES
    public static final FluidEntry SPIRIT_POTATO_LOW = register("spirit_potato_low", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_POTATO_MID = register("spirit_potato_mid", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_POTATO_HIGH = register("spirit_potato_high", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_AGAVE_LOW = register("spirit_agave_low", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    //public static final FluidEntry SPIRIT_AGAVE_MID = register("spirit_agave_mid", ModFluidsTypes.SPIRIT_FLUID_TYPE); // this is CONCENTRATED_TEQUILA
    public static final FluidEntry SPIRIT_AGAVE_HIGH = register("spirit_agave_high", ModFluidsTypes.SPIRIT_FLUID_TYPE);

    // Whisky
    public static final FluidEntry CONCENTRATED_WHISKY_WHEAT = register("concentrated_whisky_wheat", ModFluidsTypes.CONCENTRATED_WHISKY_FLUID_TYPE);
    public static final FluidEntry WHISKY_WHEAT = register("whisky_wheat", ModFluidsTypes.WHISKY_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_WHISKY_BARLEY = register("concentrated_whisky_barley", ModFluidsTypes.CONCENTRATED_WHISKY_FLUID_TYPE);
    public static final FluidEntry WHISKY_BARLEY = register("whisky_barley", ModFluidsTypes.WHISKY_FLUID_TYPE);

    // Brandy
    public static final FluidEntry CONCENTRATED_BRANDY_GRAPE = register("concentrated_brandy_grape", ModFluidsTypes.CONCENTRATED_BRANDY_FLUID_TYPE);
    public static final FluidEntry BRANDY_GRAPE = register("brandy_grape", ModFluidsTypes.BRANDY_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_BRANDY_APPLE = register("concentrated_brandy_apple", ModFluidsTypes.CONCENTRATED_BRANDY_FLUID_TYPE);
    public static final FluidEntry BRANDY_APPLE = register("brandy_apple", ModFluidsTypes.BRANDY_FLUID_TYPE);

    // Rum
    public static final FluidEntry MUST_SUGARCANE = register("must_sugarcane", ModFluidsTypes.MUST_SUGARCANE_FLUID_TYPE); // Only used for press
    public static final FluidEntry JUICE_SUGARCANE = register("juice_sugarcane", ModFluidsTypes.JUICE_SUGARCANE_FLUID_TYPE);
    public static final FluidEntry SYRUP_SUGARCANE = register("syrup_sugarcane", ModFluidsTypes.WASH_SUGARCANE_FLUID_TYPE);
    public static final FluidEntry DILUTED_MOLASSES_SUGARCANE = register("diluted_molasses_sugarcane", ModFluidsTypes.WASH_SUGARCANE_FLUID_TYPE);

    public static final FluidEntry WASH_SUGARCANE_JUICE = register("wash_sugarcane_juice", ModFluidsTypes.WASH_SUGARCANE_FLUID_TYPE);
    public static final FluidEntry WASH_SUGARCANE_MOLASSES = register("wash_sugarcane_molasses", ModFluidsTypes.WASH_SUGARCANE_FLUID_TYPE);

    public static final FluidEntry CONCENTRATED_RUM_JUICE = register("concentrated_rum_juice", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry RUM_JUICE = register("rum_juice", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_RUM_JUICE_AGED = register("concentrated_rum_juice_aged", ModFluidsTypes.CONCENTRATED_RUM_AGED_FLUID_TYPE);
    public static final FluidEntry RUM_JUICE_AGED = register("rum_juice_aged", ModFluidsTypes.RUM_AGED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_RUM_MOLASSES = register("concentrated_rum_molasses", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry RUM_MOLASSES = register("rum_molasses", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_RUM_MOLASSES_AGED = register("concentrated_rum_molasses_aged", ModFluidsTypes.CONCENTRATED_RUM_AGED_FLUID_TYPE);
    public static final FluidEntry RUM_MOLASSES_AGED = register("rum_molasses_aged", ModFluidsTypes.RUM_AGED_FLUID_TYPE);

    // Vodka
    public static final FluidEntry MASH_POTATO = register("mash_potato", ModFluidsTypes.WASH_POTATO_FLUID_TYPE);
    public static final FluidEntry WASH_POTATO = register("wash_potato", ModFluidsTypes.WASH_POTATO_FLUID_TYPE);

    public static final FluidEntry CONCENTRATED_VODKA_GRAPE = register("concentrated_vodka_grape", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry VODKA_GRAPE = register("vodka_grape", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_VODKA_APPLE = register("concentrated_vodka_apple", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry VODKA_APPLE = register("vodka_apple", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_VODKA_HONEY = register("concentrated_vodka_honey", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry VODKA_HONEY = register("vodka_honey", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_VODKA_SUGARCANE_JUICE = register("concentrated_vodka_sugarcane_juice", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry VODKA_SUGARCANE_JUICE = register("vodka_sugarcane_juice", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_VODKA_SUGARCANE_MOLASSES = register("concentrated_vodka_sugarcane_molasses", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry VODKA_SUGARCANE_MOLASSES = register("vodka_sugarcane_molasses", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_VODKA_POTATO = register("concentrated_vodka_potato", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry VODKA_POTATO = register("vodka_potato", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_VODKA_AGAVE = register("concentrated_vodka_agave", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry VODKA_AGAVE = register("vodka_agave", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_VODKA_WHEAT = register("concentrated_vodka_wheat", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry VODKA_WHEAT = register("vodka_wheat", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_VODKA_BARLEY = register("concentrated_vodka_barley", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry VODKA_BARLEY = register("vodka_barley", ModFluidsTypes.SPIRIT_FLUID_TYPE);

    // Gin
    public static final FluidEntry SPIRIT_GRAPE_MID_SPICED = register("spirit_grape_mid_spiced", ModFluidsTypes.SPIRIT_SPICED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_GIN_GRAPE = register("concentrated_gin_grape", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry GIN_GRAPE = register("gin_grape", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_APPLE_MID_SPICED = register("spirit_apple_mid_spiced", ModFluidsTypes.SPIRIT_SPICED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_GIN_APPLE = register("concentrated_gin_apple", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry GIN_APPLE = register("gin_apple", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_HONEY_MID_SPICED = register("spirit_honey_mid_spiced", ModFluidsTypes.SPIRIT_SPICED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_GIN_HONEY = register("concentrated_gin_honey", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry GIN_HONEY = register("gin_honey", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_SUGARCANE_JUICE_MID_SPICED = register("spirit_sugarcane_juice_mid_spiced", ModFluidsTypes.SPIRIT_SPICED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_GIN_SUGARCANE_JUICE = register("concentrated_gin_sugarcane_juice", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry GIN_SUGARCANE_JUICE = register("gin_sugarcane_juice", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_SUGARCANE_MOLASSES_MID_SPICED = register("spirit_sugarcane_molasses_mid_spiced", ModFluidsTypes.SPIRIT_SPICED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_GIN_SUGARCANE_MOLASSES = register("concentrated_gin_sugarcane_molasses", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry GIN_SUGARCANE_MOLASSES = register("gin_sugarcane_molasses", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_POTATO_MID_SPICED = register("spirit_potato_mid_spiced", ModFluidsTypes.SPIRIT_SPICED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_GIN_POTATO = register("concentrated_gin_potato", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry GIN_POTATO = register("gin_potato", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_AGAVE_MID_SPICED = register("spirit_agave_mid_spiced", ModFluidsTypes.SPIRIT_SPICED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_GIN_AGAVE = register("concentrated_gin_agave", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry GIN_AGAVE = register("gin_agave", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_WHEAT_MID_SPICED = register("spirit_wheat_mid_spiced", ModFluidsTypes.SPIRIT_SPICED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_GIN_WHEAT = register("concentrated_gin_wheat", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry GIN_WHEAT = register("gin_wheat", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry SPIRIT_BARLEY_MID_SPICED = register("spirit_barley_mid_spiced", ModFluidsTypes.SPIRIT_SPICED_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_GIN_BARLEY = register("concentrated_gin_barley", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry GIN_BARLEY = register("gin_barley", ModFluidsTypes.SPIRIT_FLUID_TYPE);

    // Tequila
    public static final FluidEntry MUST_AGAVE = register("must_agave", ModFluidsTypes.MUST_AGAVE_FLUID_TYPE);
    public static final FluidEntry JUICE_AGAVE = register("juice_agave", ModFluidsTypes.WASH_AGAVE_FLUID_TYPE);
    public static final FluidEntry WASH_AGAVE = register("wash_agave", ModFluidsTypes.WASH_AGAVE_FLUID_TYPE);

    public static final FluidEntry CONCENTRATED_TEQUILA = register("concentrated_tequila", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry TEQUILA = register("tequila", ModFluidsTypes.SPIRIT_FLUID_TYPE);
    public static final FluidEntry CONCENTRATED_TEQUILA_AGED = register("concentrated_tequila_aged", ModFluidsTypes.CONCENTRATED_TEQUILA_AGED_FLUID_TYPE);
    public static final FluidEntry TEQUILA_AGED = register("tequila_aged", ModFluidsTypes.TEQUILA_AGED_FLUID_TYPE);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}