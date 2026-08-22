package net.tadacko.tadackosdrinks.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TadackosDrinks.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(
                        ModBlocks.MANUAL_PRESS_OAK.get(),
                        ModBlocks.MANUAL_PRESS_SPRUCE.get(),
                        ModBlocks.MANUAL_PRESS_BIRCH.get(),
                        ModBlocks.MANUAL_PRESS_JUNGLE.get(),
                        ModBlocks.MANUAL_PRESS_ACACIA.get(),
                        ModBlocks.MANUAL_PRESS_DARK_OAK.get(),
                        ModBlocks.MANUAL_PRESS_MANGROVE.get(),
                        ModBlocks.MANUAL_PRESS_CRIMSON.get(),
                        ModBlocks.MANUAL_PRESS_WARPED.get(),
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
                        ModBlocks.FERMENTING_BARREL_WARPED.get(),
                        ModBlocks.TRELLIS_OAK.get(),
                        ModBlocks.TRELLIS_SPRUCE.get(),
                        ModBlocks.TRELLIS_BIRCH.get(),
                        ModBlocks.TRELLIS_JUNGLE.get(),
                        ModBlocks.TRELLIS_ACACIA.get(),
                        ModBlocks.TRELLIS_DARK_OAK.get(),
                        ModBlocks.TRELLIS_MANGROVE.get(),
                        ModBlocks.TRELLIS_CHERRY.get(),
                        ModBlocks.TRELLIS_BAMBOO.get(),
                        ModBlocks.TRELLIS_CRIMSON.get(),
                        ModBlocks.TRELLIS_WARPED.get()
                        );

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
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
                        ModBlocks.MANUAL_CRUSHER_WARPED.get(),
                        ModBlocks.COPPER_POT.get(),
                        ModBlocks.POT_STILL.get(),
                        ModBlocks.COLUMN_STILL.get(),
                        ModBlocks.CONDENSER.get(),
                        ModBlocks.KEG_BLOCK.get()
                        );
        for (ModFluids.FluidEntry entry : ModFluids.ALL_FLUIDS) {
            this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(entry.cauldron().get());
        }
    }
}
