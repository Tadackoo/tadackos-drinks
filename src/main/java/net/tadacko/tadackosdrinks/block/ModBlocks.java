package net.tadacko.tadackosdrinks.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TadackosDrinks.MOD_ID);

    // .forceSolidOn() to stop fluids from breaking blocks
    public static final RegistryObject<Block> PLACEABLE_DRINKWARE_BLOCK = BLOCKS.register("placeable_drinkware_block",
            () -> new PlaceableDrinkwareBlock(BlockBehaviour.Properties.copy(Blocks.GLASS).strength(0.2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));

    public static final RegistryObject<Block> KEG_BLOCK = BLOCKS.register("keg",
            () -> new KegBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(1.0F).sound(SoundType.METAL).noOcclusion().forceSolidOn()));

    public static final RegistryObject<Block> MANUAL_CRUSHER_OAK = registerBlock("manual_crusher_oak",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_SPRUCE = registerBlock("manual_crusher_spruce",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_BIRCH = registerBlock("manual_crusher_birch",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_JUNGLE = registerBlock("manual_crusher_jungle",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_ACACIA = registerBlock("manual_crusher_acacia",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_DARK_OAK = registerBlock("manual_crusher_dark_oak",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_MANGROVE = registerBlock("manual_crusher_mangrove",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_CHERRY = registerBlock("manual_crusher_cherry",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_BAMBOO = registerBlock("manual_crusher_bamboo",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_CRIMSON = registerBlock("manual_crusher_crimson",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_CRUSHER_WARPED = registerBlock("manual_crusher_warped",
            () -> new ManualCrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(2f).sound(SoundType.METAL).noOcclusion().forceSolidOn()));
    public static final RegistryObject<Block> MANUAL_PRESS_OAK = registerBlock("manual_press_oak",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_SPRUCE = registerBlock("manual_press_spruce",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_BIRCH = registerBlock("manual_press_birch",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_JUNGLE = registerBlock("manual_press_jungle",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_ACACIA = registerBlock("manual_press_acacia",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_DARK_OAK = registerBlock("manual_press_dark_oak",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_MANGROVE = registerBlock("manual_press_mangrove",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_CHERRY = registerBlock("manual_press_cherry",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_BAMBOO = registerBlock("manual_press_bamboo",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_CRIMSON = registerBlock("manual_press_crimson",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_PLANKS).strength(2f).sound(SoundType.NETHER_WOOD).noOcclusion()));
    public static final RegistryObject<Block> MANUAL_PRESS_WARPED = registerBlock("manual_press_warped",
            () -> new ManualPressBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_PLANKS).strength(2f).sound(SoundType.NETHER_WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_OAK = registerBlock("fermenting_barrel_oak",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_SPRUCE = registerBlock("fermenting_barrel_spruce",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_BIRCH = registerBlock("fermenting_barrel_birch",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_JUNGLE = registerBlock("fermenting_barrel_jungle",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_ACACIA = registerBlock("fermenting_barrel_acacia",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_DARK_OAK = registerBlock("fermenting_barrel_dark_oak",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_MANGROVE = registerBlock("fermenting_barrel_mangrove",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_CHERRY = registerBlock("fermenting_barrel_cherry",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_BAMBOO = registerBlock("fermenting_barrel_bamboo",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2f).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_CRIMSON = registerBlock("fermenting_barrel_crimson",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_PLANKS).strength(2f).sound(SoundType.NETHER_WOOD).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_BARREL_WARPED = registerBlock("fermenting_barrel_warped",
            () -> new FermentingBarrelBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_PLANKS).strength(2f).sound(SoundType.NETHER_WOOD).noOcclusion()));
    public static final RegistryObject<Block> COPPER_POT = registerBlock("copper_pot",
            () -> new CopperPotBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
                    .strength(2f).sound(SoundType.COPPER).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistryObject<Block> POT_STILL = registerBlock("pot_still",
            () -> new PotStillBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).strength(2f).sound(SoundType.COPPER).requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final RegistryObject<Block> COLUMN_STILL = registerBlock("column_still",
            () -> new ColumnStillBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).strength(2f).sound(SoundType.COPPER).requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final RegistryObject<Block> CONDENSER = registerBlock("condenser",
            () -> new CondenserBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).strength(2f).sound(SoundType.COPPER).requiresCorrectToolForDrops()
                    .noOcclusion().forceSolidOn()));

    public static final RegistryObject<Block> ROPE = BLOCKS.register("rope",
            () -> new RopeBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(0.25f).sound(SoundType.WOOL).noOcclusion()
                    .noCollission()));
    public static final RegistryObject<Block> TRELLIS_OAK = registerBlock("trellis_oak",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_SPRUCE = registerBlock("trellis_spruce",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_BIRCH = registerBlock("trellis_birch",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_JUNGLE = registerBlock("trellis_jungle",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_ACACIA = registerBlock("trellis_acacia",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_DARK_OAK = registerBlock("trellis_dark_oak",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_MANGROVE = registerBlock("trellis_mangrove",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_CHERRY = registerBlock("trellis_cherry",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_BAMBOO = registerBlock("trellis_bamboo",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_CRIMSON = registerBlock("trellis_crimson",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.NETHER_WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_WARPED = registerBlock("trellis_warped",
            () -> new TrellisBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.NETHER_WOOD).noOcclusion().forceSolidOff()));
    public static final RegistryObject<Block> TRELLIS_WIRE = BLOCKS.register("trellis_wire",
            () -> new TrellisWireBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(0.5f).sound(SoundType.METAL).noOcclusion()
                    .noCollission()));

    public static final RegistryObject<Block> BARLEY_CROP = BLOCKS.register("barley_crop",
            () -> new BarleyCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)));
    public static final RegistryObject<Block> HOP_CROP = BLOCKS.register("hop_crop",
            () -> new HopCropBlock(BlockBehaviour.Properties.copy(Blocks.SUGAR_CANE)));
    public static final RegistryObject<Block> GRAPE_CROP_RED = BLOCKS.register("grape_crop_red",
            () -> new GrapeCropBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).forceSolidOff(),
                    ModItems.GRAPE_SEEDS_RED, ModItems.GRAPES_RED, ModBlocks.GRAPE_WIRE_CROP_RED));
    public static final RegistryObject<Block> GRAPE_CROP_WHITE = BLOCKS.register("grape_crop_white",
            () -> new GrapeCropBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(1f).sound(SoundType.WOOD).forceSolidOff(),
                    ModItems.GRAPE_SEEDS_WHITE, ModItems.GRAPES_WHITE, ModBlocks.GRAPE_WIRE_CROP_WHITE));
    public static final RegistryObject<Block> GRAPE_WIRE_CROP_RED = BLOCKS.register("grape_wire_crop_red",
            () -> new GrapeWireCropBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(0.5f).sound(SoundType.AZALEA_LEAVES),
                    ModItems.GRAPES_RED, ModBlocks.GRAPE_WIRE_CROP_RED));
    public static final RegistryObject<Block> GRAPE_WIRE_CROP_WHITE = BLOCKS.register("grape_wire_crop_white",
            () -> new GrapeWireCropBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET).strength(0.5f).sound(SoundType.AZALEA_LEAVES),
                    ModItems.GRAPES_WHITE, ModBlocks.GRAPE_WIRE_CROP_WHITE));
    public static final RegistryObject<Block> JUNIPER = BLOCKS.register("juniper",
            () -> new JuniperBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH)));
    public static final RegistryObject<Block> AGAVE = BLOCKS.register("agave",
            () -> new AgaveBlock(BlockBehaviour.Properties.copy(Blocks.SEAGRASS)));

    public static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
