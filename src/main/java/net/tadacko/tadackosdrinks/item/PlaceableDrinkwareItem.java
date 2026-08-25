package net.tadacko.tadackosdrinks.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.tadacko.tadackosdrinks.block.DrinkVariant;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.PlaceableDrinkwareBlock;
import net.tadacko.tadackosdrinks.block.entity.PlaceableDrinkwareBlockEntity;
import net.tadacko.tadackosdrinks.item.client.DrinkItemRenderer;

import java.util.function.Consumer;

public class PlaceableDrinkwareItem extends Item {
    private final DrinkVariant variant;

    public PlaceableDrinkwareItem(Properties properties, DrinkVariant variant) {
        super(properties);
        this.variant = variant;
    }

    public DrinkVariant getVariant() {
        return variant;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!player.isCrouching()) return super.useOn(context);

        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        if (!level.getBlockState(placePos).canBeReplaced()) return InteractionResult.FAIL;

        Direction facing = context.getHorizontalDirection().getOpposite();

        level.setBlock(placePos, ModBlocks.PLACEABLE_DRINKWARE_BLOCK.get()
                .defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(PlaceableDrinkwareBlock.VARIANT, this.variant), Block.UPDATE_ALL);
        level.playSound(null, placePos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

        // store the item in the block entity
        if (!level.isClientSide) {
            if (level.getBlockEntity(placePos) instanceof PlaceableDrinkwareBlockEntity be) {
                ItemStack copy = context.getItemInHand().copy();
                copy.setCount(1);
                be.setStoredStack(copy);
            }
        }

        if (!context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return DrinkItemRenderer.getInstance();
            }
        });
    }
}
