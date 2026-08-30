package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tadacko.tadackosdrinks.item.ModItems;

import java.util.Map;

public class WortCauldronBlock extends AbstractCauldronBlock {
    public WortCauldronBlock(Properties pProperties, Map<Item, CauldronInteraction> pInteractions) { super(pProperties, pInteractions); }

    @Override
    public boolean isFull(BlockState pState) { return false; }

    @Override
    public Item asItem() { return Items.CAULDRON; }
}