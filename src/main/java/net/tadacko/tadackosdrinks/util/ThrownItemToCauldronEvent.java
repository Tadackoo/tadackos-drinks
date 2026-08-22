package net.tadacko.tadackosdrinks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.fluid.ModFluids;
import net.tadacko.tadackosdrinks.item.ModItems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ThrownItemToCauldronEvent {

    // Keyed by (dimension, BlockPos) to prevent cross-dimension state contamination.
    // e.g. Overworld (0,64,0) and Nether (0,64,0) are different cauldrons with the same BlockPos.
    private static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> hopsCount = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> juniperCount = new HashMap<>();
    private static final Set<ItemEntity> trackedItems = new HashSet<>();

    // Bitmask state per cauldron: has sugar been added? has a glass bottle been added?
    // Both must be present simultaneously to convert, unlike the threshold-based counters above.
    private static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> syrupState = new HashMap<>();
    private static final int SUGAR_FLAG  = 1;
    private static final int BOTTLE_FLAG = 2;

    public static boolean throwIngredientCauldron = false; // fallback default, overridden by config value

    // Generic ingredient inventory for water cauldrons: per (dimension, pos), how many of each
    // relevant item have been added so far. Lets recipes share ingredients regardless of the
    // order they're added in (e.g. seed-first-then-potato or potato-first-then-seed both work),
    // since completion is checked against the accumulated inventory after every addition.
    private static final Map<ResourceKey<Level>, Map<BlockPos, Map<Item, Integer>>> waterCauldronInventory = new HashMap<>();

    /** A water-cauldron recipe: exact item->amount requirements, and the resulting cauldron block. */
    private record WaterCauldronRecipe(Map<Item, Integer> ingredients, Block result) {}

    // Lazy to prevent "Registry Object not present" at class-load time.
    // Order matters as a tie-break: if inventory happens to satisfy multiple recipes at once,
    // the first match in this list wins — so more specific (multi-ingredient) recipes are listed
    // before simpler ones that could otherwise trigger prematurely.
    private static List<WaterCauldronRecipe> waterCauldronRecipes = null;

    private static List<WaterCauldronRecipe> getWaterCauldronRecipes() {
        if (waterCauldronRecipes == null) {
            waterCauldronRecipes = List.of(
                    // Mash potato: 4 potato_crushed + 1 crushed seed of either grain (either counts).
                    new WaterCauldronRecipe(
                            Map.of(ModItems.POTATO_CRUSHED.get(), 4, ModItems.WHEAT_SEEDS_CRUSHED.get(), 1),
                            ModFluids.MASH_POTATO.cauldron().get()),
                    new WaterCauldronRecipe(
                            Map.of(ModItems.POTATO_CRUSHED.get(), 4, ModItems.BARLEY_SEEDS_CRUSHED.get(), 1),
                            ModFluids.MASH_POTATO.cauldron().get()),
                    // Plain wort: 8 crushed seeds of a single grain type.
                    new WaterCauldronRecipe(
                            Map.of(ModItems.WHEAT_SEEDS_CRUSHED.get(), 16),
                            ModFluids.WORT_WHEAT.cauldron().get()),
                    new WaterCauldronRecipe(
                            Map.of(ModItems.BARLEY_SEEDS_CRUSHED.get(), 16),
                            ModFluids.WORT_BARLEY.cauldron().get())
            );
        }
        return waterCauldronRecipes;
    }

    private static Set<Item> waterCauldronIngredientItems = null;

    private static Set<Item> getWaterCauldronIngredientItems() {
        if (waterCauldronIngredientItems == null) {
            waterCauldronIngredientItems = new HashSet<>();
            for (WaterCauldronRecipe recipe : getWaterCauldronRecipes()) {
                waterCauldronIngredientItems.addAll(recipe.ingredients().keySet());
            }
        }
        return waterCauldronIngredientItems;
    }

    /** True if this item is used by at least one water-cauldron recipe (i.e. should be intercepted at all). */
    private static boolean isWaterCauldronIngredient(Item item) {
        return getWaterCauldronIngredientItems().contains(item);
    }

    /**
     * Adds up to as much of `item` as any still-unsatisfied recipe could use, capped so a full
     * stack doesn't get consumed at once (e.g. seed cap is 8 even though mash only needs 1 of it).
     *
     * @return number of items actually consumed (0 if every recipe using this item is already satisfied)
     */
    private static int addToWaterCauldronInventory(Level level, BlockPos pos, Item item, int suppliedCount) {
        Map<Item, Integer> inventory = waterCauldronInventory
                .computeIfAbsent(level.dimension(), k -> new HashMap<>())
                .computeIfAbsent(pos, k -> new HashMap<>());

        int have = inventory.getOrDefault(item, 0);
        int maxNeeded = 0;
        for (WaterCauldronRecipe recipe : getWaterCauldronRecipes()) {
            Integer required = recipe.ingredients().get(item);
            if (required != null) maxNeeded = Math.max(maxNeeded, required - have);
        }
        int toConsume = Math.min(suppliedCount, maxNeeded);
        if (toConsume <= 0) return 0;
        inventory.merge(item, toConsume, Integer::sum);
        return toConsume;
    }

    /** Checks all recipes against the current inventory at pos; converts the block on the first match. */
    private static void tryCompleteWaterCauldronRecipe(Level level, BlockPos pos) {
        Map<BlockPos, Map<Item, Integer>> posMap = waterCauldronInventory.get(level.dimension());
        if (posMap == null) return;
        Map<Item, Integer> inventory = posMap.get(pos);
        if (inventory == null) return;

        for (WaterCauldronRecipe recipe : getWaterCauldronRecipes()) {
            boolean satisfied = true;
            for (Map.Entry<Item, Integer> required : recipe.ingredients().entrySet()) {
                if (inventory.getOrDefault(required.getKey(), 0) < required.getValue()) {
                    satisfied = false;
                    break;
                }
            }
            if (satisfied) {
                level.setBlock(pos, recipe.result().defaultBlockState(), 3);
                posMap.remove(pos);
                return;
            }
        }
    }

    // Lazy to prevent "Registry Object not present" at class-load time.
    private static Map<Block, Block> unhoppedToHopped = null;

    private static Map<Block, Block> getUnhoppedToHopped() {
        if (unhoppedToHopped == null) {
            unhoppedToHopped = Map.of(
                    ModFluids.WORT_WHEAT.cauldron().get(),         ModFluids.WORT_WHEAT_HOPPED.cauldron().get(),
                    ModFluids.WORT_WHEAT_BOILED.cauldron().get(),  ModFluids.WORT_WHEAT_BOILED_HOPPED.cauldron().get(),
                    ModFluids.WORT_BARLEY.cauldron().get(),        ModFluids.WORT_BARLEY_HOPPED.cauldron().get(),
                    ModFluids.WORT_BARLEY_BOILED.cauldron().get(), ModFluids.WORT_BARLEY_BOILED_HOPPED.cauldron().get()
            );
        }
        return unhoppedToHopped;
    }

    private static Map<Block, Block> unspicedToSpiced = null;

    private static Map<Block, Block> getUnspicedToSpiced() {
        if (unspicedToSpiced == null) {
            unspicedToSpiced = Map.of(
                    ModFluids.SPIRIT_WHEAT_MID.cauldron().get(),  ModFluids.SPIRIT_WHEAT_MID_SPICED.cauldron().get(),
                    ModFluids.SPIRIT_BARLEY_MID.cauldron().get(), ModFluids.SPIRIT_BARLEY_MID_SPICED.cauldron().get(),
                    ModFluids.SPIRIT_GRAPE_MID.cauldron().get(), ModFluids.SPIRIT_GRAPE_MID_SPICED.cauldron().get(),
                    ModFluids.SPIRIT_APPLE_MID.cauldron().get(), ModFluids.SPIRIT_APPLE_MID_SPICED.cauldron().get(),
                    ModFluids.SPIRIT_HONEY_MID.cauldron().get(), ModFluids.SPIRIT_HONEY_MID_SPICED.cauldron().get(),
                    ModFluids.SPIRIT_SUGARCANE_JUICE_MID.cauldron().get(), ModFluids.SPIRIT_SUGARCANE_JUICE_MID_SPICED.cauldron().get(),
                    ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID.cauldron().get(), ModFluids.SPIRIT_SUGARCANE_MOLASSES_MID_SPICED.cauldron().get(),
                    ModFluids.SPIRIT_POTATO_MID.cauldron().get(), ModFluids.SPIRIT_POTATO_MID_SPICED.cauldron().get(),
                    ModFluids.CONCENTRATED_TEQUILA.cauldron().get(), ModFluids.SPIRIT_AGAVE_MID_SPICED.cauldron().get(),
                    ModFluids.CONCENTRATED_TEQUILA_AGED.cauldron().get(), ModFluids.SPIRIT_AGAVE_MID_SPICED.cauldron().get()
            );
        }
        return unspicedToSpiced;
    }

    /**
     * Shared counter logic used by both the tick handler (thrown items) and right-click handler.
     * Updates progress toward the threshold and converts the cauldron block when reached.
     * Does NOT handle item consumption (stack.shrink / entity.discard) — caller's responsibility.
     *
     * @return number of items consumed (0 if already at threshold)
     */
    private static int addIngredient(Level level, BlockPos pos,
                                     Map<ResourceKey<Level>, Map<BlockPos, Integer>> counterMap,
                                     int suppliedCount, int required, Block resultBlock) {
        Map<BlockPos, Integer> counter = counterMap.computeIfAbsent(level.dimension(), k -> new HashMap<>());
        int existing  = counter.getOrDefault(pos, 0);
        int toConsume = Math.min(suppliedCount, required - existing);
        if (toConsume <= 0) return 0;
        int newTotal = existing + toConsume;
        if (newTotal >= required) {
            level.setBlock(pos, resultBlock.defaultBlockState(), 3);
            counter.remove(pos);
        } else {
            counter.put(pos, newTotal);
        }
        return toConsume;
    }

    /** Returns the current sugar/bottle flag bitmask for the syrup cauldron at pos, without mutating it. */
    private static int getSyrupState(Level level, BlockPos pos) {
        Map<BlockPos, Integer> stateMap = syrupState.get(level.dimension());
        return stateMap == null ? 0 : stateMap.getOrDefault(pos, 0);
    }

    /**
     * Marks a flag (sugar or bottle) as present for the syrup cauldron at pos.
     * Caller must check getSyrupState() first — this does not guard against re-adding an already-set flag.
     * Returns true once both flags are present (conversion should happen), and clears the state.
     */
    private static boolean addSyrupIngredient(Level level, BlockPos pos, int flag) {
        Map<BlockPos, Integer> stateMap = syrupState.computeIfAbsent(level.dimension(), k -> new HashMap<>());
        int updated = stateMap.getOrDefault(pos, 0) | flag;
        if (updated == (SUGAR_FLAG | BOTTLE_FLAG)) {
            stateMap.remove(pos);
            return true;
        }
        stateMap.put(pos, updated);
        return false;
    }

    /** Converts a completed syrup cauldron into an empty cauldron and drops the results. */
    private static void completeSyrup(Level level, BlockPos pos) {
        level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
        spawnItem(level, pos, Items.SUGAR, 6);
        spawnItem(level, pos, ModItems.MOLASSES_SUGARCANE.get(), 1);
    }

    @SubscribeEvent
    public static void onItemSpawn(EntityJoinLevelEvent event) {
        if (!throwIngredientCauldron) return;
        if (event.getLevel().isClientSide) return; // Server-side only; avoids populating the set on the client
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            Item item = itemEntity.getItem().getItem();
            if (item == ModItems.BARLEY_SEEDS_CRUSHED.get() ||
                    item == ModItems.WHEAT_SEEDS_CRUSHED.get() ||
                    item == ModItems.POTATO_CRUSHED.get() ||
                    item == ModItems.HOPS.get() ||
                    item == Items.HONEY_BOTTLE ||
                    item == Items.SUGAR ||
                    item == Items.GLASS_BOTTLE ||
                    item == ModItems.MOLASSES_SUGARCANE.get() ||
                    item == ModItems.JUNIPER_BERRIES.get()) {
                trackedItems.add(itemEntity);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!throwIngredientCauldron) return;
        if (event.phase != TickEvent.Phase.END) return;
        Level level = event.level;
        if (level.isClientSide) return;

        Iterator<ItemEntity> iterator = trackedItems.iterator();
        while (iterator.hasNext()) {
            ItemEntity entity = iterator.next();

            if (!entity.isAlive()) {
                iterator.remove();
                continue;
            }

            // trackedItems is global; skip entities that belong to a different level this tick
            if (entity.level() != level) continue;

            BlockPos pos   = entity.blockPosition();
            BlockState state = level.getBlockState(pos);
            ItemStack stack = entity.getItem();
            Item item  = stack.getItem();
            int count  = stack.getCount();

            if (state.is(Blocks.WATER_CAULDRON)) {
                // Any ingredient shared by the wort/mash-potato recipes (crushed seeds, potato_crushed).
                // Order-independent: completion is checked against the accumulated inventory after
                // every addition, so seed-then-potato and potato-then-seed both work.
                if (isWaterCauldronIngredient(item)) {
                    int consumed = addToWaterCauldronInventory(level, pos, item, count);
                    if (consumed > 0) {
                        entity.discard(); iterator.remove();
                        int remaining = count - consumed;
                        if (remaining > 0) spawnItem(level, pos, item, remaining);
                        tryCompleteWaterCauldronRecipe(level, pos);
                    }
                    // consumed == 0 means every recipe using this item is already fully supplied;
                    // leave the extra item entity alone instead of consuming it for nothing.
                    continue;
                }

                // Honey bottle -> diluted honey cauldron (thrown)
                if (item == Items.HONEY_BOTTLE) {
                    level.setBlock(pos, ModFluids.DILUTED_HONEY.cauldron().get().defaultBlockState(), 3);
                    entity.discard(); iterator.remove();
                    // Drop empty glass bottle
                    spawnItem(level, pos, Items.GLASS_BOTTLE, 1);
                    continue;
                }

                // Molasses -> diluted molasses cauldron (thrown)
                if (item == ModItems.MOLASSES_SUGARCANE.get()) {
                    level.setBlock(pos, ModFluids.DILUTED_MOLASSES_SUGARCANE.cauldron().get().defaultBlockState(), 3);
                    entity.discard(); iterator.remove();
                    // Drop empty glass bottle
                    spawnItem(level, pos, Items.GLASS_BOTTLE, 1);
                    continue;
                }
            } else {
                // Sugar + glass bottle -> empty cauldron, dropping molasses (thrown)
                if (state.is(ModFluids.SYRUP_SUGARCANE.cauldron().get())) {
                    int syrupFlags = getSyrupState(level, pos);
                    // Only accept the ingredient if its flag isn't already set; otherwise leave the
                    // item entity alone (it stays tracked/on the ground) instead of consuming extras.
                    if (item == Items.SUGAR && (syrupFlags & SUGAR_FLAG) == 0) {
                        entity.discard(); iterator.remove();
                        int remaining = count - 1;
                        if (remaining > 0) spawnItem(level, pos, item, remaining);
                        if (addSyrupIngredient(level, pos, SUGAR_FLAG)) completeSyrup(level, pos);
                        continue;
                    }
                    if (item == Items.GLASS_BOTTLE && (syrupFlags & BOTTLE_FLAG) == 0) {
                        entity.discard(); iterator.remove();
                        int remaining = count - 1;
                        if (remaining > 0) spawnItem(level, pos, item, remaining);
                        if (addSyrupIngredient(level, pos, BOTTLE_FLAG)) completeSyrup(level, pos);
                        continue;
                    }
                }

                // Wort -> hopped wort (only check non-water-cauldron blocks to avoid needless map lookup)
                if (item == ModItems.HOPS.get()) {
                    Block hoppedTarget = getUnhoppedToHopped().get(state.getBlock());
                    if (hoppedTarget != null) {
                        int consumed = addIngredient(level, pos, hopsCount, count, 4, hoppedTarget);
                        entity.discard(); iterator.remove();
                        int remaining = count - consumed;
                        if (remaining > 0) spawnItem(level, pos, item, remaining);
                        continue;
                    }
                }

                if (item == ModItems.JUNIPER_BERRIES.get()) {
                    Block spicedTarget = getUnspicedToSpiced().get(state.getBlock());
                    if (spicedTarget != null) {
                        int consumed = addIngredient(level, pos, juniperCount, count, 4, spicedTarget);
                        entity.discard(); iterator.remove();
                        int remaining = count - consumed;
                        if (remaining > 0) spawnItem(level, pos, item, remaining);
                        continue;
                    }
                }
            }
        }
    }

    /**
     * Handles right-clicking cauldrons.
     * Consumes from the player's held stack (respects creative mode) and updates the same counters.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockPos pos   = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack  = event.getItemStack();
        Player player  = event.getEntity();
        boolean creative = player.getAbilities().instabuild;
        Item item = stack.getItem();

        if (state.is(Blocks.WATER_CAULDRON)) {
            // Any ingredient shared by the wort/mash-potato recipes — see onLevelTick for details.
            if (isWaterCauldronIngredient(item)) {
                int consumed = addToWaterCauldronInventory(level, pos, item, stack.getCount());
                if (!creative) stack.shrink(consumed);
                if (consumed > 0) {
                    tryCompleteWaterCauldronRecipe(level, pos);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
                return;
            }

            // Honey bottle -> diluted honey cauldron (right click)
            if (item == Items.HONEY_BOTTLE) {
                if (!creative) {
                    stack.shrink(1);
                    // Give empty bottle back
                    if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                        player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                    }
                }
                level.setBlock(pos, ModFluids.DILUTED_HONEY.cauldron().get().defaultBlockState(), 3);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }

            // Molasses -> diluted molasses cauldron (right click)
            if (item == ModItems.MOLASSES_SUGARCANE.get()) {
                if (!creative) {
                    stack.shrink(1);
                    // Give empty bottle back
                    if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                        player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                    }
                }
                level.setBlock(pos, ModFluids.DILUTED_MOLASSES_SUGARCANE.cauldron().get().defaultBlockState(), 3);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
        }

        // Sugar + glass bottle -> empty cauldron, dropping molasses (right click)
        Block syrupCauldronBlock = ModFluids.SYRUP_SUGARCANE.cauldron().get();
        if (state.is(syrupCauldronBlock)) {
            int syrupFlags = getSyrupState(level, pos);
            // Only consume the item if its flag isn't already set; otherwise do nothing (don't
            // cancel the event either, so the player's normal right-click behavior isn't eaten).
            if (item == Items.SUGAR && (syrupFlags & SUGAR_FLAG) == 0) {
                if (!creative) stack.shrink(1);
                if (addSyrupIngredient(level, pos, SUGAR_FLAG)) completeSyrup(level, pos);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
            if (item == Items.GLASS_BOTTLE && (syrupFlags & BOTTLE_FLAG) == 0) {
                if (!creative) stack.shrink(1);
                if (addSyrupIngredient(level, pos, BOTTLE_FLAG)) completeSyrup(level, pos);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
        }

        // Wort -> hopped wort by right-click with hops
        if (item == ModItems.HOPS.get()) {
            Block hoppedTarget = getUnhoppedToHopped().get(state.getBlock());
            if (hoppedTarget != null) {
                int consumed = addIngredient(level, pos, hopsCount, stack.getCount(), 4, hoppedTarget);
                if (!creative) stack.shrink(consumed);
                if (consumed > 0) {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }

        if (item == ModItems.JUNIPER_BERRIES.get()) {
            Block spicedTarget = getUnspicedToSpiced().get(state.getBlock());
            if (spicedTarget != null) {
                int consumed = addIngredient(level, pos, juniperCount, stack.getCount(), 2, spicedTarget);
                if (!creative) stack.shrink(consumed);
                if (consumed > 0) {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }

    /** Clears per-dimension brewing state when a level unloads, preventing memory leaks across world switches. */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        hopsCount.remove(dim);
        juniperCount.remove(dim);
        syrupState.remove(dim);
        waterCauldronInventory.remove(dim);
        trackedItems.removeIf(e -> e.level() == serverLevel);
    }

    /**
     * Clears stale counter entries when a cauldron block is broken.
     * Without this, a new cauldron placed at the same position would inherit partial brewing progress.
     * NOTE: Cauldrons replaced without breaking (e.g. emptied by bucket) do not trigger this. Don't worry about it...
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ResourceKey<Level> dim = serverLevel.dimension();
        BlockPos pos = event.getPos();
        // Only touch inner maps that actually exist for this dimension
        Map<BlockPos, Integer> h = hopsCount.get(dim);
        Map<BlockPos, Integer> j = juniperCount.get(dim);
        Map<BlockPos, Integer> s = syrupState.get(dim);
        Map<BlockPos, Map<Item, Integer>> wci = waterCauldronInventory.get(dim);
        if (h != null) h.remove(pos);
        if (j != null) j.remove(pos);
        if (s != null) s.remove(pos);
        if (wci != null) wci.remove(pos);
    }

    private static void spawnItem(Level level, BlockPos pos, Item item, int count) {
        ItemStack remStack = new ItemStack(item, count);
        ItemEntity drop = new ItemEntity(level,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                remStack);
        drop.setPickUpDelay(10);
        level.addFreshEntity(drop);
    }

    public static void clearTracked() { trackedItems.clear(); }
}