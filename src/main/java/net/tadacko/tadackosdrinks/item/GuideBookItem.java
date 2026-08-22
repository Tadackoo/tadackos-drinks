package net.tadacko.tadackosdrinks.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tadacko.tadackosdrinks.client.guide.GuideBookScreen;

/**
 * Item that opens the guide book GUI. The actual pages live in GuideBookScreen.PAGES
 * so the Table of Contents can jump to correct pages.
 *
 * Note: we only call client-only code when level.isClientSide is true.
 */
public class GuideBookItem extends Item {
    public GuideBookItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        // Server: consume no action, tell client it was used (return success)
        // Client: open the GUI
        if (pLevel.isClientSide) {
            // fully-qualified client call to avoid accidental top-level client imports
            net.minecraft.client.Minecraft.getInstance()
                    .setScreen(new GuideBookScreen());
        }

        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide);
    }
}
