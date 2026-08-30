package net.tadacko.tadackosdrinks.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceableDrinkwareBlockEntity extends BlockEntity {
    private ItemStack stored = ItemStack.EMPTY;

    public PlaceableDrinkwareBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.PLACEABLE_DRINKWARE.get(), pos, state); }

    public ItemStack getStoredStack() { return stored; }

    public void setStoredStack(ItemStack stack) {
        this.stored = (stack == null) ? ItemStack.EMPTY : stack.copy();
        if (this.level != null && !this.level.isClientSide) {
            setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Stored")) this.stored = ItemStack.of(tag.getCompound("Stored"));
        else this.stored = ItemStack.EMPTY;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.stored != null && !this.stored.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            this.stored.save(itemTag);
            tag.put("Stored", itemTag);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (this.stored != null && !this.stored.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            this.stored.save(itemTag);
            tag.put("Stored", itemTag);
        }
        return tag;
    }
}
