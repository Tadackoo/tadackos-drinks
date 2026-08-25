package net.tadacko.tadackosdrinks.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.tadacko.tadackosdrinks.block.GrapeCropBlock;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.TrellisBlock;
import org.jetbrains.annotations.Nullable;

public class SeedGrapeItem extends BlockItem {

    public SeedGrapeItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext pContext) {
        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        BlockState clicked = level.getBlockState(clickedPos);
        if (!(clicked.getBlock() instanceof TrellisBlock && level.getBlockState(clickedPos.below()).is(Blocks.FARMLAND))) return null;

        // get the normal placement state (vanilla logic)
        BlockState placeState = super.getPlacementState(pContext);
        if (placeState == null) return null;

        // determine variant from the clicked trellis and set it on the crop state
        int variant = getVariantFromTrellis(clicked);
        return placeState.setValue(GrapeCropBlock.VARIANT, variant);
    }

    @Override
    public String getDescriptionId() {
        if (this == ModItems.GRAPE_SEEDS_RED.get()) return "item.tadackosdrinks.grape_seeds_red";
        if (this == ModItems.GRAPE_SEEDS_WHITE.get()) return "item.tadackosdrinks.grape_seeds_white";
        return super.getDescriptionId();
    }

    private int getVariantFromTrellis(BlockState trellisState) {
        if (trellisState.is(ModBlocks.TRELLIS_OAK.get())) return 0;
        if (trellisState.is(ModBlocks.TRELLIS_SPRUCE.get())) return 1;
        if (trellisState.is(ModBlocks.TRELLIS_BIRCH.get())) return 2;
        if (trellisState.is(ModBlocks.TRELLIS_JUNGLE.get())) return 3;
        if (trellisState.is(ModBlocks.TRELLIS_ACACIA.get())) return 4;
        if (trellisState.is(ModBlocks.TRELLIS_DARK_OAK.get())) return 5;
        if (trellisState.is(ModBlocks.TRELLIS_MANGROVE.get())) return 6;
        if (trellisState.is(ModBlocks.TRELLIS_CHERRY.get())) return 7;
        if (trellisState.is(ModBlocks.TRELLIS_BAMBOO.get())) return 8;
        if (trellisState.is(ModBlocks.TRELLIS_CRIMSON.get())) return 9;
        if (trellisState.is(ModBlocks.TRELLIS_WARPED.get())) return 10;

        return 0;
    }
}
