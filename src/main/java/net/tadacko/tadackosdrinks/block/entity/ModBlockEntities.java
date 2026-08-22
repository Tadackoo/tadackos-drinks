package net.tadacko.tadackosdrinks.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.ModBlocks;

public class ModBlockEntities {
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TadackosDrinks.MOD_ID);

    public static final RegistryObject<BlockEntityType<PlaceableDrinkwareBlockEntity>> PLACEABLE_DRINKWARE =
            BLOCK_ENTITIES.register("placeable_drinkware",
                    () -> BlockEntityType.Builder.of(PlaceableDrinkwareBlockEntity::new, ModBlocks.PLACEABLE_DRINKWARE_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<KegBlockEntity>> KEG =
            BLOCK_ENTITIES.register("keg",
                    () -> BlockEntityType.Builder.of(KegBlockEntity::new, ModBlocks.KEG_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ManualCrusherBlockEntity>> MANUAL_CRUSHER =
            BLOCK_ENTITIES.register("manual_crusher", () ->
                    BlockEntityType.Builder.of(ManualCrusherBlockEntity::new,
                            ModBlocks.MANUAL_CRUSHER_OAK.get(),
                            ModBlocks.MANUAL_CRUSHER_SPRUCE.get(),
                            ModBlocks.MANUAL_CRUSHER_BIRCH.get(),
                            ModBlocks.MANUAL_CRUSHER_JUNGLE.get(),
                            ModBlocks.MANUAL_CRUSHER_ACACIA.get(),
                            ModBlocks.MANUAL_CRUSHER_DARK_OAK.get(),
                            ModBlocks.MANUAL_CRUSHER_MANGROVE.get(),
                            ModBlocks.MANUAL_CRUSHER_CHERRY.get(),
                            ModBlocks.MANUAL_CRUSHER_BAMBOO.get(),
                            ModBlocks.MANUAL_CRUSHER_CRIMSON.get(),
                            ModBlocks.MANUAL_CRUSHER_WARPED.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<ManualPressBlockEntity>> MANUAL_PRESS =
            BLOCK_ENTITIES.register("manual_press", () ->
                    BlockEntityType.Builder.of(ManualPressBlockEntity::new,
                            ModBlocks.MANUAL_PRESS_OAK.get(),
                            ModBlocks.MANUAL_PRESS_SPRUCE.get(),
                            ModBlocks.MANUAL_PRESS_BIRCH.get(),
                            ModBlocks.MANUAL_PRESS_JUNGLE.get(),
                            ModBlocks.MANUAL_PRESS_ACACIA.get(),
                            ModBlocks.MANUAL_PRESS_DARK_OAK.get(),
                            ModBlocks.MANUAL_PRESS_MANGROVE.get(),
                            ModBlocks.MANUAL_PRESS_CHERRY.get(),
                            ModBlocks.MANUAL_PRESS_BAMBOO.get(),
                            ModBlocks.MANUAL_PRESS_CRIMSON.get(),
                            ModBlocks.MANUAL_PRESS_WARPED.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<FermentingBarrelBlockEntity>> FERMENTING_BARREL =
            BLOCK_ENTITIES.register("fermenting_barrel", () ->
                    BlockEntityType.Builder.of(FermentingBarrelBlockEntity::new,
                            ModBlocks.FERMENTING_BARREL_OAK.get(),
                            ModBlocks.FERMENTING_BARREL_SPRUCE.get(),
                            ModBlocks.FERMENTING_BARREL_BIRCH.get(),
                            ModBlocks.FERMENTING_BARREL_JUNGLE.get(),
                            ModBlocks.FERMENTING_BARREL_ACACIA.get(),
                            ModBlocks.FERMENTING_BARREL_DARK_OAK.get(),
                            ModBlocks.FERMENTING_BARREL_MANGROVE.get(),
                            ModBlocks.FERMENTING_BARREL_CHERRY.get(),
                            ModBlocks.FERMENTING_BARREL_BAMBOO.get(),
                            ModBlocks.FERMENTING_BARREL_CRIMSON.get(),
                            ModBlocks.FERMENTING_BARREL_WARPED.get()
                    ).build(null));

    public static final RegistryObject<BlockEntityType<CopperPotBlockEntity>> COPPER_POT =
            BLOCK_ENTITIES.register("copper_pot", () ->
                    BlockEntityType.Builder.of(CopperPotBlockEntity::new,
                            ModBlocks.COPPER_POT.get()).build(null));

    public static final RegistryObject<BlockEntityType<PotStillBlockEntity>> POT_STILL =
            BLOCK_ENTITIES.register("pot_still", () ->
                    BlockEntityType.Builder.of(PotStillBlockEntity::new,
                            ModBlocks.POT_STILL.get()).build(null));

    public static final RegistryObject<BlockEntityType<ColumnStillBlockEntity>> COLUMN_STILL =
            BLOCK_ENTITIES.register("column_still", () ->
                    BlockEntityType.Builder.of(ColumnStillBlockEntity::new,
                            ModBlocks.COLUMN_STILL.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
