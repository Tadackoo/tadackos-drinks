package net.tadacko.tadackosdrinks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.item.ModItems;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WaterInteractionHandler {
    private static final Set<ItemEntity> trackedItems = new HashSet<>();

    public static boolean throwMalting = false; // fallback default, overridden by config value

    // -----------------------
    // Dropped items handling
    // -----------------------
    @SubscribeEvent
    public static void onItemSpawn(EntityJoinLevelEvent event) {
        if (!throwMalting) return;
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.getItem() != ModItems.BARLEY_SEEDS.get() && itemStack.getItem() != Items.WHEAT_SEEDS) return;

        trackedItems.add(itemEntity);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!throwMalting) return;
        if (event.phase != TickEvent.Phase.END) return;
        Level level = event.level;
        if (level.isClientSide) return;

        Iterator<ItemEntity> iterator = trackedItems.iterator();
        while (iterator.hasNext()) {
            ItemEntity itemEntity = iterator.next();
            if (!itemEntity.isAlive()) {
                iterator.remove();
                continue;
            }

            if (itemEntity.level() != level) continue;

            if (!isOverWaterOrCauldron(itemEntity, level)) continue;

            ItemStack oldStack = itemEntity.getItem();
            ItemStack newStack = getMaltedVersion(oldStack);
            if (newStack == null) continue;

            // Set the new stack count to match the original
            newStack.setCount(oldStack.getCount());
            itemEntity.setItem(newStack);
            iterator.remove();
        }
    }

    // -----------------------
    // Right-click handling
    // -----------------------

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleInteract(event);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        handleInteract(event);
    }

    public static void handleInteract(PlayerInteractEvent event) {
        if (event.getLevel().isClientSide) return;

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack held = player.getItemInHand(hand);
        ItemStack maltedSample = getMaltedVersion(held);
        boolean converted = false;
        if (maltedSample == null) return; // nothing to do

        // Quick checks for RightClickBlock
        if (event instanceof PlayerInteractEvent.RightClickBlock rcBlock) {
            BlockPos pos = rcBlock.getPos();
            Direction face = rcBlock.getFace();

            if (posHasWater(event.getLevel(), pos) ||
                    (face != null && posHasWater(event.getLevel(), pos.relative(face))) ||
                    posHasWater(event.getLevel(), pos.above()) ||
                    posHasWater(event.getLevel(), pos.below()) ||
                    tryHitVecWater(event.getLevel(), rcBlock.getHitVec().getLocation())) {
                converted = true;
                doConvertStack(player, hand, maltedSample);
            }
        }

        // Raycast fallback for deep/open water
        if (!converted) {
            converted = doFluidRaycastAndConvert(event.getLevel(), player, hand, maltedSample);
        }

        // Only cancel if conversion actually happened
        if (converted) cancelEvent(event);
    }

    // -----------------------
    // Conversion helpers
    // -----------------------
    private static boolean doFluidRaycastAndConvert(Level level, Player player, InteractionHand hand, ItemStack maltedSample) {
        double range = 5.0D;
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.x * range, look.y * range, look.z * range);

        ClipContext ctx = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player);
        HitResult hr = level.clip(ctx);

        if (hr == null || hr.getType() != HitResult.Type.BLOCK) return false;

        BlockHitResult bhr = (BlockHitResult) hr;
        BlockPos hit = bhr.getBlockPos();

        if (posHasWater(level, hit) || posHasWater(level, hit.above()) || posHasWater(level, hit.relative(bhr.getDirection()))) {
            doConvertStack(player, hand, maltedSample);
            return true;
        }
        return false;
    }

    private static boolean tryHitVecWater(Level level, Vec3 hitVec) {
        BlockPos hp = new BlockPos(Mth.floor(hitVec.x), Mth.floor(hitVec.y), Mth.floor(hitVec.z));
        return posHasWater(level, hp) || posHasWater(level, hp.above()) || posHasWater(level, hp.below());
    }

    private static boolean doConvertStack(Player player, InteractionHand hand, ItemStack maltedSample) {
        ItemStack held = player.getItemInHand(hand);
        int count = held.getCount();
        held.shrink(count);

        ItemStack out = maltedSample.copy();
        out.setCount(count);

        if (!player.addItem(out)) player.drop(out, false);
        return true;
    }

    private static void cancelEvent(PlayerInteractEvent ev) {
        ev.setCancellationResult(InteractionResult.SUCCESS);
        ev.setCanceled(true);
    }

    private static ItemStack getMaltedVersion(ItemStack stack) {
        if (stack == null) return null;
        if (stack.getItem() == ModItems.BARLEY_SEEDS.get()) {
            return new ItemStack(ModItems.BARLEY_SEEDS_MALTED.get());
        } else if (stack.getItem() == Items.WHEAT_SEEDS) {
            return new ItemStack(ModItems.WHEAT_SEEDS_MALTED.get());
        }
        return null;
    }

    private static boolean isOverWaterOrCauldron(ItemEntity item, LevelReader level) {
        AABB box = item.getBoundingBox();
        int x0 = Mth.floor(box.minX), x1 = Mth.floor(box.maxX);
        int y0 = Mth.floor(box.minY), y1 = Mth.floor(box.maxY);
        int z0 = Mth.floor(box.minZ), z1 = Mth.floor(box.maxZ);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = x0; x <= x1; x++)
            for (int y = y0; y <= y1; y++)
                for (int z = z0; z <= z1; z++)
                    if (posHasWater(level, pos.set(x, y, z))) return true;
        return false;
    }

    private static boolean posHasWater(LevelReader level, BlockPos pos) {
        BlockState bs = level.getBlockState(pos);
        FluidState fs = level.getFluidState(pos);
        if (bs.getBlock() == Blocks.WATER) return true;
        if (bs.getBlock() == Blocks.WATER_CAULDRON) return true;
        if (fs.is(Fluids.WATER)) return true;
        if (bs.getFluidState().is(Fluids.WATER)) return true;
        return false;
    }

    public static void clearTracked() { trackedItems.clear(); }
}
