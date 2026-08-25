package net.tadacko.tadackosdrinks.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

public class SeedHopItem extends BlockItem {
    public SeedHopItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext pContext) {
        BlockState clicked = pContext.getLevel().getBlockState(pContext.getClickedPos());
        if (!clicked.is(ModBlocks.ROPE.get())) return null;

        return super.getPlacementState(pContext);
    }

    @Override
    public String getDescriptionId() {
        return "item.tadackosdrinks.hop_seeds";
    }
}
