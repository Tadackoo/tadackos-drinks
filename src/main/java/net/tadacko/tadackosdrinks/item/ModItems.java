package net.tadacko.tadackosdrinks.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.tadacko.tadackosdrinks.block.DrinkVariant;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import net.tadacko.tadackosdrinks.util.BacUtils;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TadackosDrinks.MOD_ID);

    public static final RegistryObject<Item> GUIDE_BOOK = ITEMS.register("guide_book",
            () -> new GuideBookItem(new Item.Properties()));

    public static final RegistryObject<Item> ROPE_ITEM = ITEMS.register("rope_item",
            () -> new RopeBlockItem(ModBlocks.ROPE.get(), new Item.Properties()));
    public static final RegistryObject<Item> TRELLIS_WIRE_ITEM = ITEMS.register("trellis_wire_item",
            () -> new TrellisWireItem(new Item.Properties()));

    public static final RegistryObject<Item> WHEAT_SEEDS_MALTED = ITEMS.register("wheat_seeds_malted",
            () -> new ItemNameBlockItem(Blocks.WHEAT, new Item.Properties()));
    public static final RegistryObject<Item> WHEAT_SEEDS_CRUSHED = ITEMS.register("wheat_seeds_crushed",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BARLEY_SEEDS = ITEMS.register("barley_seeds",
            () -> new ItemNameBlockItem(ModBlocks.BARLEY_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> BARLEY_SEEDS_MALTED = ITEMS.register("barley_seeds_malted",
            () -> new ItemNameBlockItem(ModBlocks.BARLEY_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> BARLEY_SEEDS_CRUSHED = ITEMS.register("barley_seeds_crushed",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BARLEY = ITEMS.register("barley",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HOP_SEEDS = ITEMS.register("hop_seeds",
            () -> new SeedHopItem(ModBlocks.HOP_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> HOPS = ITEMS.register("hops",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GRAPE_SEEDS_RED = ITEMS.register("grape_seeds_red",
            () -> new SeedGrapeItem(ModBlocks.GRAPE_CROP_RED.get(), new Item.Properties()));
    public static final RegistryObject<Item> GRAPE_SEEDS_WHITE = ITEMS.register("grape_seeds_white",
            () -> new SeedGrapeItem(ModBlocks.GRAPE_CROP_WHITE.get(), new Item.Properties()));
    public static final RegistryObject<Item> GRAPES_RED = ITEMS.register("grapes_red",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));
    public static final RegistryObject<Item> GRAPES_WHITE = ITEMS.register("grapes_white",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));
    public static final RegistryObject<Item> SUGAR_CANE_CRUSHED = ITEMS.register("sugar_cane_crushed",
            () -> new Item(new Item.Properties()));
    public static int molassesStackSize = 16; // fallback default, overridden by config value
    public static final RegistryObject<Item> MOLASSES_SUGARCANE = ITEMS.register("molasses_sugarcane",
            () -> new Item(new Item.Properties()) { @Override public int getMaxStackSize(ItemStack stack) { return molassesStackSize; } });
    public static final RegistryObject<Item> POTATO_CRUSHED = ITEMS.register("potato_crushed",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationMod(0.6f).build())));
    public static final RegistryObject<Item> JUNIPER_BERRIES = ITEMS.register("juniper_berries",
            () -> new ItemNameBlockItem(ModBlocks.JUNIPER.get(), new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.1f)
                            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 1200, 0), 0.05F).build())));
    public static final RegistryObject<Item> AGAVE_SHOOT = ITEMS.register("agave_shoot",
            () -> new ItemNameBlockItem(ModBlocks.AGAVE.get(), new Item.Properties()));
    public static final RegistryObject<Item> AGAVE_PINA = ITEMS.register("agave_pina",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> AGAVE_PINA_BAKED = ITEMS.register("agave_pina_baked",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> AGAVE_PINA_CRUSHED = ITEMS.register("agave_pina_crushed",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> YEAST = ITEMS.register("yeast",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HANGOVER_CURE = ITEMS.register("hangover_cure",
            () -> new HangoverCureItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> HANGOVER_ICON = ITEMS.register("hangover_icon",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> INEBRIATION_ICON = ITEMS.register("inebriation_icon",
            () -> new Item(new Item.Properties()));

    public static int kegStackSize = 1; // fallback default, overridden by config value
    public static final RegistryObject<Item> KEG = ITEMS.register("keg",
            () -> new KegItem(new Item.Properties()) { @Override public int getMaxStackSize(ItemStack stack) { return kegStackSize; } });

    public static int glassStackSize = 16; // fallback default, overridden by config value
    public static final RegistryObject<Item> BEER_GLASS_EMPTY = ITEMS.register("beer_glass_empty",
            () -> new PlaceableDrinkwareItem(new Item.Properties(), DrinkVariant.BEER_EMPTY)
            { @Override public int getMaxStackSize(ItemStack stack) { return glassStackSize; } });
    public static final RegistryObject<Item> WINE_GLASS_EMPTY = ITEMS.register("wine_glass_empty",
            () -> new PlaceableDrinkwareItem(new Item.Properties(), DrinkVariant.WINE_EMPTY)
            { @Override public int getMaxStackSize(ItemStack stack) { return glassStackSize; } });
    public static final RegistryObject<Item> WHISKY_GLASS_EMPTY = ITEMS.register("whisky_glass_empty",
            () -> new PlaceableDrinkwareItem(new Item.Properties(), DrinkVariant.WHISKY_EMPTY)
            { @Override public int getMaxStackSize(ItemStack stack) { return glassStackSize; } });
    public static final RegistryObject<Item> BRANDY_GLASS_EMPTY = ITEMS.register("brandy_glass_empty",
            () -> new PlaceableDrinkwareItem(new Item.Properties(), DrinkVariant.BRANDY_EMPTY)
            { @Override public int getMaxStackSize(ItemStack stack) { return glassStackSize; } });
    public static final RegistryObject<Item> SHOT_GLASS_EMPTY = ITEMS.register("shot_glass_empty",
            () -> new PlaceableDrinkwareItem(new Item.Properties(), DrinkVariant.SHOT_EMPTY)
            { @Override public int getMaxStackSize(ItemStack stack) { return glassStackSize; } });

    public static int drinkStackSize = 1; // fallback default, overridden by config value
    public static final RegistryObject<Item> BEER_WHEAT_GLASS = ITEMS.register("beer_wheat_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), MobEffects.DAMAGE_RESISTANCE, 1, 0,
                    0.05, 0.5, DrinkVariant.BEER, BEER_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> BEER_WHEAT_HOPPED_GLASS = ITEMS.register("beer_wheat_hopped_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), MobEffects.DAMAGE_RESISTANCE, 2, 0,
                    0.05, 0.5, DrinkVariant.BEER, BEER_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> BEER_BARLEY_GLASS = ITEMS.register("beer_barley_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), MobEffects.DAMAGE_RESISTANCE, 1, 0,
                    0.05, 0.5, DrinkVariant.BEER, BEER_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> BEER_BARLEY_HOPPED_GLASS = ITEMS.register("beer_barley_hopped_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), MobEffects.DAMAGE_RESISTANCE, 2, 0,
                    0.05, 0.5, DrinkVariant.BEER, BEER_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static final RegistryObject<Item> WINE_RED_GLASS = ITEMS.register("wine_red_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.HEALTH_BOOST_RED.get(), 1, 0,
                    0.12, 0.2, DrinkVariant.WINE_RED, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> WINE_RED_AGED_GLASS = ITEMS.register("wine_red_aged_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.HEALTH_BOOST_RED.get(), 1, 1,
                    0.12, 0.2, DrinkVariant.WINE_RED, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> WINE_ROSE_GLASS = ITEMS.register("wine_rose_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.HEALTH_BOOST_ROSE.get(), 1, 0,
                    0.12, 0.2, DrinkVariant.WINE_ROSE, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> WINE_ROSE_AGED_GLASS = ITEMS.register("wine_rose_aged_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.HEALTH_BOOST_ROSE.get(), 1, 1,
                    0.12, 0.2, DrinkVariant.WINE_ROSE, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> WINE_ORANGE_GLASS = ITEMS.register("wine_orange_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.ABSORPTION_ORANGE.get(), 1, 0,
                    0.12, 0.2, DrinkVariant.WINE_ORANGE, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> WINE_ORANGE_AGED_GLASS = ITEMS.register("wine_orange_aged_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.ABSORPTION_ORANGE.get(), 1, 1,
                    0.12, 0.2, DrinkVariant.WINE_ORANGE, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> WINE_WHITE_GLASS = ITEMS.register("wine_white_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.ABSORPTION_WHITE.get(), 1, 0,
                    0.12, 0.2, DrinkVariant.WINE_WHITE, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> WINE_WHITE_AGED_GLASS = ITEMS.register("wine_white_aged_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.ABSORPTION_WHITE.get(), 1, 1,
                    0.12, 0.2, DrinkVariant.WINE_WHITE, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static final RegistryObject<Item> CIDER_GLASS = ITEMS.register("cider_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), MobEffects.DIG_SPEED, 1, 0,
                    0.05, 0.5, DrinkVariant.CIDER, BEER_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> CIDER_AGED_GLASS = ITEMS.register("cider_aged_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), MobEffects.DIG_SPEED, 1, 1,
                    0.05, 0.5, DrinkVariant.CIDER, BEER_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static final RegistryObject<Item> MEAD_GLASS = ITEMS.register("mead_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.WISDOM.get(), 1, 0,
                    0.12, 0.2, DrinkVariant.MEAD, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> MEAD_AGED_GLASS = ITEMS.register("mead_aged_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.WISDOM.get(), 1, 1,
                    0.12, 0.2, DrinkVariant.MEAD, WINE_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static final RegistryObject<Item> WHISKY_WHEAT_GLASS = ITEMS.register("whisky_wheat_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.ERUDITION.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.WHISKY, WHISKY_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> WHISKY_BARLEY_GLASS = ITEMS.register("whisky_barley_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.ERUDITION.get(), 1, 1,
                    0.40, 0.05, DrinkVariant.WHISKY, WHISKY_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static final RegistryObject<Item> BRANDY_GRAPE_GLASS = ITEMS.register("brandy_grape_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.IMPROVED_DIGESTION.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.BRANDY, BRANDY_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> BRANDY_APPLE_GLASS = ITEMS.register("brandy_apple_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.IMPROVED_DIGESTION.get(), 1, 1,
                    0.40, 0.05, DrinkVariant.BRANDY, BRANDY_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static final RegistryObject<Item> RUM_JUICE_GLASS = ITEMS.register("rum_juice_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.PIRACY.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.RUM_LIGHT, WHISKY_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> RUM_JUICE_AGED_GLASS = ITEMS.register("rum_juice_aged_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.PIRACY.get(), 2, 0,
                    0.40, 0.05, DrinkVariant.RUM, WHISKY_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> RUM_MOLASSES_GLASS = ITEMS.register("rum_molasses_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.PIRACY.get(), 1, 1,
                    0.40, 0.05, DrinkVariant.RUM_LIGHT, WHISKY_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> RUM_MOLASSES_AGED_GLASS = ITEMS.register("rum_molasses_aged_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.PIRACY.get(), 2, 1,
                    0.40, 0.05, DrinkVariant.RUM, WHISKY_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static final RegistryObject<Item> VODKA_GRAPE_GLASS = ITEMS.register("vodka_grape_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.CHARISMA.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> VODKA_APPLE_GLASS = ITEMS.register("vodka_apple_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.CHARISMA.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> VODKA_HONEY_GLASS = ITEMS.register("vodka_honey_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.CHARISMA.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> VODKA_SUGARCANE_JUICE_GLASS = ITEMS.register("vodka_sugarcane_juice_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.CHARISMA.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> VODKA_SUGARCANE_MOLASSES_GLASS = ITEMS.register("vodka_sugarcane_molasses_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.CHARISMA.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> VODKA_POTATO_GLASS = ITEMS.register("vodka_potato_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.CHARISMA.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> VODKA_AGAVE_GLASS = ITEMS.register("vodka_agave_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.CHARISMA.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> VODKA_WHEAT_GLASS = ITEMS.register("vodka_wheat_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.CHARISMA.get(), 1, 1,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> VODKA_BARLEY_GLASS = ITEMS.register("vodka_barley_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.CHARISMA.get(), 1, 1,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static final RegistryObject<Item> GIN_GRAPE_GLASS = ITEMS.register("gin_grape_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.SAVAGERY.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> GIN_APPLE_GLASS = ITEMS.register("gin_apple_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.SAVAGERY.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> GIN_HONEY_GLASS = ITEMS.register("gin_honey_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.SAVAGERY.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> GIN_SUGARCANE_JUICE_GLASS = ITEMS.register("gin_sugarcane_juice_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.SAVAGERY.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> GIN_SUGARCANE_MOLASSES_GLASS = ITEMS.register("gin_sugarcane_molasses_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.SAVAGERY.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> GIN_POTATO_GLASS = ITEMS.register("gin_potato_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.SAVAGERY.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> GIN_AGAVE_GLASS = ITEMS.register("gin_agave_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.SAVAGERY.get(), 1, 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> GIN_WHEAT_GLASS = ITEMS.register("gin_wheat_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.SAVAGERY.get(), 1, 1,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> GIN_BARLEY_GLASS = ITEMS.register("gin_barley_glass",
            () -> new DrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), ModEffects.SAVAGERY.get(), 1, 1,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static final RegistryObject<Item> TEQUILA_GLASS = ITEMS.register("tequila_glass",
            () -> new TequilaDrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), 0,
                    0.40, 0.05, DrinkVariant.SHOT, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });
    public static final RegistryObject<Item> TEQUILA_AGED_GLASS = ITEMS.register("tequila_aged_glass",
            () -> new TequilaDrinkItem(new Item.Properties()
                    .food(new FoodProperties.Builder().alwaysEat().build()), 1,
                    0.40, 0.05, DrinkVariant.TEQUILA, SHOT_GLASS_EMPTY.get())
            { @Override public int getMaxStackSize(ItemStack stack) { return drinkStackSize; } });

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}