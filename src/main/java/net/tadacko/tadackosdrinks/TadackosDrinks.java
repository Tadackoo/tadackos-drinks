package net.tadacko.tadackosdrinks;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.entity.ModBlockEntities;
import net.tadacko.tadackosdrinks.block.entity.client.*;
import net.tadacko.tadackosdrinks.config.ModCommonConfigs;
import net.tadacko.tadackosdrinks.effect.InebriationEffect;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import net.tadacko.tadackosdrinks.fluid.CauldronFluidRegistry;
import net.tadacko.tadackosdrinks.fluid.DrinkwareFluidRegistry;
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import net.tadacko.tadackosdrinks.fluid.ModFluidsTypes;
import net.tadacko.tadackosdrinks.item.ModCreativeModeTab;
import net.tadacko.tadackosdrinks.item.ModItems;
import net.tadacko.tadackosdrinks.loot.ModLootModifiers;
import net.tadacko.tadackosdrinks.network.ModNetwork;
import net.tadacko.tadackosdrinks.recipe.ModRecipeSerializers;
import net.tadacko.tadackosdrinks.util.IFluidColorProvider;
import net.tadacko.tadackosdrinks.util.WortCauldronInteraction;
import net.tadacko.tadackosdrinks.worldgen.feature.ModFeatures;
import software.bernie.geckolib.GeckoLib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TadackosDrinks.MOD_ID)
public class TadackosDrinks
{
    public static final String MOD_ID = "tadackosdrinks";

    public TadackosDrinks() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeModeTab.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModFluids.register(modEventBus); // triggers ModFluids static init, which calls into the above two
        ModFluidsTypes.register(modEventBus);

        ModBlockEntities.register(modEventBus);

        ModEffects.register(modEventBus);

        ModNetwork.register();

        ModLootModifiers.register(modEventBus);

        ModFeatures.register(modEventBus);

        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        GeckoLib.initialize();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfigs.SPEC, "tadackosdrinks-common.toml");

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.addListener(InebriationEffect::onEntityLeave);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(WortCauldronInteraction::bootstrap);

        DrinkwareFluidRegistry.register(ModFluids.BEER_WHEAT.source().get(), ModItems.BEER_GLASS_EMPTY.get(), ModItems.BEER_WHEAT_GLASS.get(), 500);
        DrinkwareFluidRegistry.register(ModFluids.BEER_WHEAT_HOPPED.source().get(), ModItems.BEER_GLASS_EMPTY.get(), ModItems.BEER_WHEAT_HOPPED_GLASS.get(), 500);
        DrinkwareFluidRegistry.register(ModFluids.BEER_BARLEY.source().get(), ModItems.BEER_GLASS_EMPTY.get(), ModItems.BEER_BARLEY_GLASS.get(), 500);
        DrinkwareFluidRegistry.register(ModFluids.BEER_BARLEY_HOPPED.source().get(), ModItems.BEER_GLASS_EMPTY.get(), ModItems.BEER_BARLEY_HOPPED_GLASS.get(), 500);

        DrinkwareFluidRegistry.register(ModFluids.CIDER.source().get(), ModItems.BEER_GLASS_EMPTY.get(), ModItems.CIDER_GLASS.get(), 500);
        DrinkwareFluidRegistry.register(ModFluids.CIDER_AGED.source().get(), ModItems.BEER_GLASS_EMPTY.get(), ModItems.CIDER_AGED_GLASS.get(), 500);

        DrinkwareFluidRegistry.register(ModFluids.WINE_RED.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.WINE_RED_GLASS.get(), 200);
        DrinkwareFluidRegistry.register(ModFluids.WINE_RED_AGED.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.WINE_RED_AGED_GLASS.get(), 200);
        DrinkwareFluidRegistry.register(ModFluids.WINE_ROSE.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.WINE_ROSE_GLASS.get(), 200);
        DrinkwareFluidRegistry.register(ModFluids.WINE_ROSE_AGED.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.WINE_ROSE_AGED_GLASS.get(), 200);
        DrinkwareFluidRegistry.register(ModFluids.WINE_ORANGE.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.WINE_ORANGE_GLASS.get(), 200);
        DrinkwareFluidRegistry.register(ModFluids.WINE_ORANGE_AGED.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.WINE_ORANGE_AGED_GLASS.get(), 200);
        DrinkwareFluidRegistry.register(ModFluids.WINE_WHITE.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.WINE_WHITE_GLASS.get(), 200);
        DrinkwareFluidRegistry.register(ModFluids.WINE_WHITE_AGED.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.WINE_WHITE_AGED_GLASS.get(), 200);

        DrinkwareFluidRegistry.register(ModFluids.MEAD.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.MEAD_GLASS.get(), 200);
        DrinkwareFluidRegistry.register(ModFluids.MEAD_AGED.source().get(), ModItems.WINE_GLASS_EMPTY.get(), ModItems.MEAD_AGED_GLASS.get(), 200);

        DrinkwareFluidRegistry.register(ModFluids.WHISKY_WHEAT.source().get(), ModItems.WHISKY_GLASS_EMPTY.get(), ModItems.WHISKY_WHEAT_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.WHISKY_BARLEY.source().get(), ModItems.WHISKY_GLASS_EMPTY.get(), ModItems.WHISKY_BARLEY_GLASS.get(), 50);

        DrinkwareFluidRegistry.register(ModFluids.BRANDY_GRAPE.source().get(), ModItems.BRANDY_GLASS_EMPTY.get(), ModItems.BRANDY_GRAPE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.BRANDY_APPLE.source().get(), ModItems.BRANDY_GLASS_EMPTY.get(), ModItems.BRANDY_APPLE_GLASS.get(), 50);

        DrinkwareFluidRegistry.register(ModFluids.RUM_JUICE.source().get(), ModItems.WHISKY_GLASS_EMPTY.get(), ModItems.RUM_JUICE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.RUM_JUICE_AGED.source().get(), ModItems.WHISKY_GLASS_EMPTY.get(), ModItems.RUM_JUICE_AGED_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.RUM_MOLASSES.source().get(), ModItems.WHISKY_GLASS_EMPTY.get(), ModItems.RUM_MOLASSES_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.RUM_MOLASSES_AGED.source().get(), ModItems.WHISKY_GLASS_EMPTY.get(), ModItems.RUM_MOLASSES_AGED_GLASS.get(), 50);

        DrinkwareFluidRegistry.register(ModFluids.VODKA_GRAPE.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.VODKA_GRAPE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.VODKA_APPLE.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.VODKA_APPLE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.VODKA_HONEY.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.VODKA_HONEY_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.VODKA_SUGARCANE_JUICE.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.VODKA_SUGARCANE_JUICE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.VODKA_SUGARCANE_MOLASSES.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.VODKA_SUGARCANE_MOLASSES_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.VODKA_POTATO.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.VODKA_POTATO_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.VODKA_AGAVE.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.VODKA_AGAVE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.VODKA_WHEAT.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.VODKA_WHEAT_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.VODKA_BARLEY.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.VODKA_BARLEY_GLASS.get(), 50);

        DrinkwareFluidRegistry.register(ModFluids.GIN_GRAPE.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.GIN_GRAPE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.GIN_APPLE.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.GIN_APPLE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.GIN_HONEY.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.GIN_HONEY_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.GIN_SUGARCANE_JUICE.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.GIN_SUGARCANE_JUICE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.GIN_SUGARCANE_MOLASSES.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.GIN_SUGARCANE_MOLASSES_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.GIN_POTATO.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.GIN_POTATO_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.GIN_AGAVE.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.GIN_AGAVE_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.GIN_WHEAT.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.GIN_WHEAT_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.GIN_BARLEY.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.GIN_BARLEY_GLASS.get(), 50);

        DrinkwareFluidRegistry.register(ModFluids.TEQUILA.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.TEQUILA_GLASS.get(), 50);
        DrinkwareFluidRegistry.register(ModFluids.TEQUILA_AGED.source().get(), ModItems.SHOT_GLASS_EMPTY.get(), ModItems.TEQUILA_AGED_GLASS.get(), 50);

        CauldronFluidRegistry.register(Blocks.WATER_CAULDRON, Fluids.WATER, 1000);

        // Every cauldron-enabled fluid is registered here automatically via ModFluids.ALL_FLUIDS.
        for (ModFluids.FluidEntry entry : ModFluids.ALL_FLUIDS) {
            CauldronFluidRegistry.register(entry.cauldron().get(), entry.source().get(), 1000);
        }

        event.enqueueWork(() -> {
            ComposterBlock.COMPOSTABLES.put(ModItems.WHEAT_SEEDS_MALTED.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.WHEAT_SEEDS_CRUSHED.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.BARLEY_SEEDS.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.BARLEY_SEEDS_MALTED.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.BARLEY_SEEDS_CRUSHED.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.BARLEY.get(), 0.65f);
            ComposterBlock.COMPOSTABLES.put(ModItems.HOP_SEEDS.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.HOPS.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.GRAPE_SEEDS_RED.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.GRAPE_SEEDS_WHITE.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.GRAPES_RED.get(), 0.5f);
            ComposterBlock.COMPOSTABLES.put(ModItems.GRAPES_WHITE.get(), 0.5f);
            ComposterBlock.COMPOSTABLES.put(ModItems.SUGAR_CANE_CRUSHED.get(), 0.5f);
            ComposterBlock.COMPOSTABLES.put(ModItems.POTATO_CRUSHED.get(), 0.85f);
            ComposterBlock.COMPOSTABLES.put(ModItems.JUNIPER_BERRIES.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.AGAVE_SHOOT.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.AGAVE_PINA.get(), 0.5f);
            ComposterBlock.COMPOSTABLES.put(ModItems.AGAVE_PINA_BAKED.get(), 0.5f);
            ComposterBlock.COMPOSTABLES.put(ModItems.AGAVE_PINA_CRUSHED.get(), 0.3f);
            ComposterBlock.COMPOSTABLES.put(ModItems.YEAST.get(), 0.65f);
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModCreativeModeTab.TADACKOS_DRINKS_TAB.get()) {
            event.accept(ModItems.GUIDE_BOOK);
            event.accept(ModItems.ROPE_ITEM);
            event.accept(ModItems.TRELLIS_WIRE_ITEM);
            event.accept(ModItems.WHEAT_SEEDS_MALTED);
            event.accept(ModItems.WHEAT_SEEDS_CRUSHED);
            event.accept(ModItems.BARLEY_SEEDS);
            event.accept(ModItems.BARLEY_SEEDS_MALTED);
            event.accept(ModItems.BARLEY_SEEDS_CRUSHED);
            event.accept(ModItems.BARLEY);
            event.accept(ModItems.HOP_SEEDS);
            event.accept(ModItems.HOPS);
            event.accept(ModItems.GRAPE_SEEDS_RED);
            event.accept(ModItems.GRAPE_SEEDS_WHITE);
            event.accept(ModItems.GRAPES_RED);
            event.accept(ModItems.GRAPES_WHITE);
            event.accept(ModItems.SUGAR_CANE_CRUSHED);
            event.accept(ModItems.MOLASSES_SUGARCANE);
            event.accept(ModItems.POTATO_CRUSHED);
            event.accept(ModItems.JUNIPER_BERRIES);
            event.accept(ModItems.AGAVE_SHOOT);
            event.accept(ModItems.AGAVE_PINA);
            event.accept(ModItems.AGAVE_PINA_BAKED);
            event.accept(ModItems.AGAVE_PINA_CRUSHED);
            event.accept(ModItems.YEAST);
            event.accept(ModItems.HANGOVER_CURE);
            event.accept(ModItems.KEG);
            event.accept(ModItems.BEER_GLASS_EMPTY);
            event.accept(ModItems.WINE_GLASS_EMPTY);
            event.accept(ModItems.WHISKY_GLASS_EMPTY);
            event.accept(ModItems.BRANDY_GLASS_EMPTY);
            event.accept(ModItems.SHOT_GLASS_EMPTY);

            event.accept(ModItems.BEER_WHEAT_GLASS);
            event.accept(ModItems.BEER_WHEAT_HOPPED_GLASS);
            event.accept(ModItems.BEER_BARLEY_GLASS);
            event.accept(ModItems.BEER_BARLEY_HOPPED_GLASS);

            event.accept(ModItems.WINE_RED_GLASS);
            event.accept(ModItems.WINE_RED_AGED_GLASS);
            event.accept(ModItems.WINE_ROSE_GLASS);
            event.accept(ModItems.WINE_ROSE_AGED_GLASS);
            event.accept(ModItems.WINE_ORANGE_GLASS);
            event.accept(ModItems.WINE_ORANGE_AGED_GLASS);
            event.accept(ModItems.WINE_WHITE_GLASS);
            event.accept(ModItems.WINE_WHITE_AGED_GLASS);

            event.accept(ModItems.CIDER_GLASS);
            event.accept(ModItems.CIDER_AGED_GLASS);

            event.accept(ModItems.MEAD_GLASS);
            event.accept(ModItems.MEAD_AGED_GLASS);

            event.accept(ModItems.WHISKY_WHEAT_GLASS);
            event.accept(ModItems.WHISKY_BARLEY_GLASS);

            event.accept(ModItems.BRANDY_GRAPE_GLASS);
            event.accept(ModItems.BRANDY_APPLE_GLASS);

            event.accept(ModItems.RUM_JUICE_GLASS);
            event.accept(ModItems.RUM_JUICE_AGED_GLASS);
            event.accept(ModItems.RUM_MOLASSES_GLASS);
            event.accept(ModItems.RUM_MOLASSES_AGED_GLASS);

            event.accept(ModItems.VODKA_GRAPE_GLASS);
            event.accept(ModItems.VODKA_APPLE_GLASS);
            event.accept(ModItems.VODKA_HONEY_GLASS);
            event.accept(ModItems.VODKA_SUGARCANE_JUICE_GLASS);
            event.accept(ModItems.VODKA_SUGARCANE_MOLASSES_GLASS);
            event.accept(ModItems.VODKA_POTATO_GLASS);
            event.accept(ModItems.VODKA_AGAVE_GLASS);
            event.accept(ModItems.VODKA_WHEAT_GLASS);
            event.accept(ModItems.VODKA_BARLEY_GLASS);

            event.accept(ModItems.GIN_GRAPE_GLASS);
            event.accept(ModItems.GIN_APPLE_GLASS);
            event.accept(ModItems.GIN_HONEY_GLASS);
            event.accept(ModItems.GIN_SUGARCANE_JUICE_GLASS);
            event.accept(ModItems.GIN_SUGARCANE_MOLASSES_GLASS);
            event.accept(ModItems.GIN_POTATO_GLASS);
            event.accept(ModItems.GIN_AGAVE_GLASS);
            event.accept(ModItems.GIN_WHEAT_GLASS);
            event.accept(ModItems.GIN_BARLEY_GLASS);

            event.accept(ModItems.TEQUILA_GLASS);
            event.accept(ModItems.TEQUILA_AGED_GLASS);

            // Every fluid's bucket, in the order it was registered in ModFluids.ALL_FLUIDS.
            for (ModFluids.FluidEntry entry : ModFluids.ALL_FLUIDS) {
                event.accept(entry.bucket());
            }

            event.accept(ModBlocks.MANUAL_CRUSHER_OAK);
            event.accept(ModBlocks.MANUAL_CRUSHER_SPRUCE);
            event.accept(ModBlocks.MANUAL_CRUSHER_BIRCH);
            event.accept(ModBlocks.MANUAL_CRUSHER_JUNGLE);
            event.accept(ModBlocks.MANUAL_CRUSHER_ACACIA);
            event.accept(ModBlocks.MANUAL_CRUSHER_DARK_OAK);
            event.accept(ModBlocks.MANUAL_CRUSHER_MANGROVE);
            event.accept(ModBlocks.MANUAL_CRUSHER_CHERRY);
            event.accept(ModBlocks.MANUAL_CRUSHER_BAMBOO);
            event.accept(ModBlocks.MANUAL_CRUSHER_CRIMSON);
            event.accept(ModBlocks.MANUAL_CRUSHER_WARPED);
            event.accept(ModBlocks.MANUAL_PRESS_OAK);
            event.accept(ModBlocks.MANUAL_PRESS_SPRUCE);
            event.accept(ModBlocks.MANUAL_PRESS_BIRCH);
            event.accept(ModBlocks.MANUAL_PRESS_JUNGLE);
            event.accept(ModBlocks.MANUAL_PRESS_ACACIA);
            event.accept(ModBlocks.MANUAL_PRESS_DARK_OAK);
            event.accept(ModBlocks.MANUAL_PRESS_MANGROVE);
            event.accept(ModBlocks.MANUAL_PRESS_CHERRY);
            event.accept(ModBlocks.MANUAL_PRESS_BAMBOO);
            event.accept(ModBlocks.MANUAL_PRESS_CRIMSON);
            event.accept(ModBlocks.MANUAL_PRESS_WARPED);
            event.accept(ModBlocks.FERMENTING_BARREL_OAK);
            event.accept(ModBlocks.FERMENTING_BARREL_SPRUCE);
            event.accept(ModBlocks.FERMENTING_BARREL_BIRCH);
            event.accept(ModBlocks.FERMENTING_BARREL_JUNGLE);
            event.accept(ModBlocks.FERMENTING_BARREL_ACACIA);
            event.accept(ModBlocks.FERMENTING_BARREL_DARK_OAK);
            event.accept(ModBlocks.FERMENTING_BARREL_MANGROVE);
            event.accept(ModBlocks.FERMENTING_BARREL_CHERRY);
            event.accept(ModBlocks.FERMENTING_BARREL_BAMBOO);
            event.accept(ModBlocks.FERMENTING_BARREL_CRIMSON);
            event.accept(ModBlocks.FERMENTING_BARREL_WARPED);
            event.accept(ModBlocks.COPPER_POT);
            event.accept(ModBlocks.POT_STILL);
            event.accept(ModBlocks.COLUMN_STILL);
            event.accept(ModBlocks.CONDENSER);
            event.accept(ModBlocks.TRELLIS_OAK);
            event.accept(ModBlocks.TRELLIS_SPRUCE);
            event.accept(ModBlocks.TRELLIS_BIRCH);
            event.accept(ModBlocks.TRELLIS_JUNGLE);
            event.accept(ModBlocks.TRELLIS_ACACIA);
            event.accept(ModBlocks.TRELLIS_DARK_OAK);
            event.accept(ModBlocks.TRELLIS_MANGROVE);
            event.accept(ModBlocks.TRELLIS_CHERRY);
            event.accept(ModBlocks.TRELLIS_BAMBOO);
            event.accept(ModBlocks.TRELLIS_CRIMSON);
            event.accept(ModBlocks.TRELLIS_WARPED);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Source + flowing render layer for every fluid, kept in sync via ModFluids.ALL_FLUIDS.
            for (ModFluids.FluidEntry entry : ModFluids.ALL_FLUIDS) {
                ItemBlockRenderTypes.setRenderLayer(entry.source().get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(entry.flowing().get(), RenderType.translucent());
            }

            BlockEntityRenderers.register(ModBlockEntities.COPPER_POT.get(), CopperPotRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.MANUAL_CRUSHER.get(), ManualCrusherRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.MANUAL_PRESS.get(), ManualPressRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.FERMENTING_BARREL.get(), FermentingBarrelRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.POT_STILL.get(), PotStillRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.COLUMN_STILL.get(), ColumnStillRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.PLACEABLE_DRINKWARE.get(), PlaceableDrinkwareRenderer::new);

            setupCauldronFluids();
        }

        public static final Map<Block, Fluid> CAULDRON_FLUIDS = new HashMap<>();

        public static void setupCauldronFluids() {
            // Populated from ModFluids.ALL_FLUIDS so every cauldron-enabled fluid is covered automatically.
            for (ModFluids.FluidEntry entry : ModFluids.ALL_FLUIDS) {
                CAULDRON_FLUIDS.put(entry.cauldron().get(), entry.source().get());
            }
        }

        @SubscribeEvent
        public static void onBlockColorRegister(RegisterColorHandlersEvent.Block event) {
            BlockColor cauldronColorHandler = (state, world, pos, tintIndex) -> {
                // Try block entity first — only possible with a real world/pos (in-world rendering)
                if (world != null && pos != null) {
                    var blockEntity = world.getBlockEntity(pos);
                    if (blockEntity instanceof IFluidColorProvider fluidProvider) {
                        var fluidStack = fluidProvider.getFluid();
                        if (!fluidStack.isEmpty()) return IClientFluidTypeExtensions.of(fluidStack.getFluid().getFluidType()).getTintColor();
                    }
                }

                // Fallback: works even with no world/pos (e.g. GUI/icon rendering of an isolated
                // blockstate), since the block type itself identifies the fluid.
                Fluid fluid = CAULDRON_FLUIDS.get(state.getBlock());
                if (fluid != null) return IClientFluidTypeExtensions.of(fluid.getFluidType()).getTintColor();

                return 0xFFFFFF;
            };

            // Every fluid's cauldron block, kept in sync via ModFluids.ALL_FLUIDS.
            List<Block> cauldronBlocks = new ArrayList<>();
            for (ModFluids.FluidEntry entry : ModFluids.ALL_FLUIDS) {
                cauldronBlocks.add(entry.cauldron().get());
            }

            event.register(cauldronColorHandler, cauldronBlocks.toArray(new Block[0]));
        }
    }
}