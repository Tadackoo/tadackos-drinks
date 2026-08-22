package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static net.tadacko.tadackosdrinks.block.TrellisBlock.getWireProp;

public class GrapeCropBlock extends CropBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 10);
    public static final BooleanProperty TRELLIS = BooleanProperty.create("trellis");
    public static final BooleanProperty WIRE_NORTH = BooleanProperty.create("wire_north");
    public static final BooleanProperty WIRE_SOUTH = BooleanProperty.create("wire_south");
    public static final BooleanProperty WIRE_EAST = BooleanProperty.create("wire_east");
    public static final BooleanProperty WIRE_WEST = BooleanProperty.create("wire_west");

    public static final int MAX_AGE = 3;

    // natural spread chance: 1 in SPREAD_TIME each eligible tick (set to 1 for always)
    private static final int SPREAD_TIME = 5;

    private final Supplier<Item> seedItem;
    private final Supplier<Item> grapeItem;
    private final Supplier<Block> wireCropVariant;

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D),
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D)
    };

    public GrapeCropBlock(Properties properties, Supplier<Item> seedItem, Supplier<Item> grapeItem, Supplier<Block> wireCropVariant) {
        super(properties);
        // default: age 0, no trellis
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(VARIANT, 0)
                .setValue(TRELLIS, false)
                .setValue(WIRE_NORTH,  false)
                .setValue(WIRE_SOUTH,  false)
                .setValue(WIRE_EAST,  false)
                .setValue(WIRE_WEST,  false));
        this.seedItem = seedItem;
        this.grapeItem = grapeItem;
        this.wireCropVariant = wireCropVariant;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        boolean hasTrellis = pState.getValue(TRELLIS);
        return SHAPES[hasTrellis ? 1 : 0];
    }

    @Override
    protected int getBonemealAgeIncrease(Level pLevel) {
        return Mth.nextInt(pLevel.random, 1, 2);
    }

    // Use our AGE property for CropBlock mechanics
    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seedItem.get();
    }

    public Item getGrapeItem() {
        return grapeItem.get();
    }

    // IMPORTANT: Do NOT call super.createBlockStateDefinition(builder) here ---
    // CropBlock already registers an "age" property (vanilla AGE_0_7) which would
    // conflict with our custom AGE property (also named "age"). Add only our properties.
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, VARIANT, TRELLIS, WIRE_NORTH, WIRE_SOUTH, WIRE_EAST, WIRE_WEST);
    }

    // Only plantable on farmland
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter world, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        return super.mayPlaceOn(state, world, pos);
    }

    // Survival logic: must have farmland below; if age > 1 require a trellis above to keep higher ages
    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return super.canSurvive(pState, pLevel, pPos) || pLevel.getBlockState(pPos.below()).is(this);
    }

    // When placed, set TRELLIS according to what is above
    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        if (!world.isClientSide) {
            boolean hasTrellisAbove = world.getBlockState(pos.above()).getBlock() instanceof TrellisBlock ||
                    world.getBlockState(pos.above()).getBlock() instanceof GrapeCropBlock;
            BlockState newState = state.setValue(TRELLIS, hasTrellisAbove);

            // compute wire flags
            newState = updateWireConnections(newState, world, pos);

            world.setBlock(pos, newState, 2);

            if (!this.canSurvive(newState, world, pos)) {
                world.destroyBlock(pos, true);
            }
        }
    }

    // Update trellis property when the block above changes.
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;

        if (fromPos.equals(pos.above())) {
            boolean hasTrellis = level.getBlockState(pos.above()).getBlock() instanceof TrellisBlock ||
                    level.getBlockState(pos.above()).getBlock() instanceof GrapeCropBlock;
            int age = state.getValue(getAgeProperty());
            BlockState newState = state.setValue(TRELLIS, hasTrellis);

            if (!hasTrellis && age > 1) {
                newState = newState.setValue(getAgeProperty(), 1);
            }

            // recompute wire flags for any horizontal changes
            newState = updateWireConnections(newState, level, pos);

            if (newState != state) {
                level.setBlock(pos, newState, 2);
            }

            if (!this.canSurvive(newState, level, pos)) {
                level.destroyBlock(pos, true);
            }
        } else {
            // If a horizontal neighbor changed (wire added/removed), recompute wires as well.
            if (fromPos.getY() == pos.getY()) {
                BlockState newState = updateWireConnections(state, level, pos);
                if (newState != state) level.setBlock(pos, newState, 3);
            }
        }
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pLevel.isAreaLoaded(pPos, 1)) return;
        if (pLevel.getRawBrightness(pPos, 0) < 9) return;

        // keep wire flags up-to-date
        pLevel.setBlock(pPos, updateWireConnections(pState, pLevel, pPos), 2);

        int currentAge = this.getAge(pState);
        float growthSpeed = getGrowthSpeed(this, pLevel, pPos);

        // decide whether a growth *attempt* happens this tick (same as vanilla flow)
        boolean growthHappens = pRandom.nextInt((int) (120.0F / growthSpeed) + 1) == 0;
        if (!ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, growthHappens)) return;
        if (!growthHappens) return;

        // If we are age 1, attempt sideways spread on a successful growth tick.
        if (currentAge == 1) {
            if (pRandom.nextInt(SPREAD_TIME) == 0) attemptSideSpread(pLevel, pPos);
            // continue: we *also* allow the crop to try to age further (but aging past 1 requires trellis above)
        }

        // Normal growth handling (age increment or max-age behavior)
        if (currentAge == MAX_AGE) {
            // Already max: vertical spread (age 3 → spawn above)
            if (pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock) {
                // Preserve VARIANT when spawning above
                BlockState newState = this.defaultBlockState()
                        .setValue(AGE, 0)
                        .setValue(VARIANT, pState.getValue(VARIANT));
                pLevel.setBlock(pPos.above(), newState, 3);
            }
            // For max-age we also attempt side spread (keep previous behavior)
            if (pRandom.nextInt(SPREAD_TIME) == 0) attemptSideSpread(pLevel, pPos);
        } else {
            int newAge = currentAge + 1;
            if (newAge >= 2 && !(pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock)) {
                if (currentAge == 0) {
                    newAge = 1; // allow the initial jump 0 -> 1
                } else {
                    ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
                    return;
                }
            }

            // Preserve VARIANT when aging
            pLevel.setBlock(pPos, pState.setValue(AGE, newAge), 3);

            // If we just reached MAX_AGE, do vertical spread, but no extra side spread here
            // (side spread for age 1 already handled above; MAX_AGE side spread still allowed)
            if (newAge == MAX_AGE) {
                if (pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock) {
                    // Preserve VARIANT when spawning above
                    BlockState newState = this.defaultBlockState()
                            .setValue(AGE, 0)
                            .setValue(VARIANT, pState.getValue(VARIANT));
                    pLevel.setBlock(pPos.above(), newState, 3);
                }
                if (pRandom.nextInt(SPREAD_TIME) == 0) attemptSideSpread(pLevel, pPos);
            }
        }

        ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
    }


    @Override
    public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
        int age = getAge(pState);

        // If it can still grow normally (age < MAX_AGE), bonemeal works
        if (age < MAX_AGE) return true;

        // If it's fully grown, allow bonemeal if it can spread vertically or sideways
        boolean canSpreadUp = pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock;
        boolean canSpreadSideways = false;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos wirePos = pPos.relative(dir);
            if (canGroundSpreadToWire((LevelAccessor) pLevel, wirePos, dir)) {
                canSpreadSideways = true;
                break;
            }
        }

        return canSpreadUp || canSpreadSideways;
    }

    @Override
    public void growCrops(Level pLevel, BlockPos pPos, BlockState pState) {
        int currentAge = this.getAge(pState);
        int nextAge = currentAge + this.getBonemealAgeIncrease(pLevel);
        if (nextAge > MAX_AGE) nextAge = MAX_AGE;

        // If applying bonemeal when crop is (or becomes) age 1, attempt side spread
        boolean willBeAgeOne = (currentAge == 1) || (nextAge == 1);
        if (willBeAgeOne) {
            attemptSideSpread(pLevel, pPos);
        }

        // If fully grown already, keep vertical behavior (spawn above)
        if (currentAge == MAX_AGE) {
            if (pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock) {
                // Preserve VARIANT when spawning above
                BlockState newState = this.defaultBlockState()
                        .setValue(AGE, 0)
                        .setValue(VARIANT, pState.getValue(VARIANT));
                pLevel.setBlock(pPos.above(), newState, 2);
            }
            // also try side spread at max age (already attempted above if was age1, but keep it here to mirror randomTick behavior)
            attemptSideSpread(pLevel, pPos);
            return;
        }

        // Normal bonemeal growth rules (can't grow past age 1->2 without trellis above)
        if (nextAge >= 2 && !(pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock)) {
            if (currentAge == 0) nextAge = 1; else return;
        }

        // Preserve VARIANT when aging
        pLevel.setBlock(pPos, pState.setValue(AGE, nextAge), 2);

        // If we just reached MAX_AGE via bonemeal, do vertical spread as usual
        if (nextAge == MAX_AGE) {
            if (pLevel.getBlockState(pPos.above()).getBlock() instanceof TrellisBlock) {
                // Preserve VARIANT when spawning above
                BlockState newState = this.defaultBlockState()
                        .setValue(AGE, 0)
                        .setValue(VARIANT, pState.getValue(VARIANT));
                pLevel.setBlock(pPos.above(), newState, 2);
            }
            attemptSideSpread(pLevel, pPos);
        }
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = this.defaultBlockState();
        boolean hasTrellisAbove = level.getBlockState(pos.above()).getBlock() instanceof TrellisBlock ||
                level.getBlockState(pos.above()).getBlock() instanceof GrapeCropBlock;
        state.setValue(getAgeProperty(), 0).setValue(TRELLIS, hasTrellisAbove);
        // initialize WIRE_* properties (horizontal only)
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BooleanProperty wireProp = getWireProp(dir);
            BlockPos checkPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(checkPos);
            boolean hasWire = false;

            // Count trellis wire or grape wire crop only when:
            // - the neighbor has no FACING property (treat as always connected), OR
            // - the neighbor's FACING points either toward OR away from this block
            if (neighborState.is(ModBlocks.TRELLIS_WIRE.get()) || neighborState.is(ModBlocks.GRAPE_WIRE_CROP_RED.get()) ||
                    neighborState.is(ModBlocks.GRAPE_WIRE_CROP_WHITE.get())) {
                if (!neighborState.hasProperty(GrapeWireCropBlock.FACING)) {
                    // neighbor has no facing property (e.g. trellis wire); count it
                    hasWire = true;
                } else {
                    Direction neighborFacing = neighborState.getValue(GrapeWireCropBlock.FACING);
                    // Accept facing toward this block OR away from this block
                    if (neighborFacing == dir.getOpposite() || neighborFacing == dir) {
                        hasWire = true;
                    }
                }
            }

            state = state.setValue(wireProp, hasWire);
        }

        return state;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        // Always tick, because grapes can spread even when mature
        return true;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity tileEntity, ItemStack stack) {
        super.playerDestroy(level, player, pos, state, tileEntity, stack);
        if (!level.isClientSide) {
            BlockState trellis;
            switch (state.getValue(VARIANT)) {
                case 1 -> trellis = ModBlocks.TRELLIS_SPRUCE.get().defaultBlockState();
                case 2 -> trellis = ModBlocks.TRELLIS_BIRCH.get().defaultBlockState();
                case 3 -> trellis = ModBlocks.TRELLIS_JUNGLE.get().defaultBlockState();
                case 4 -> trellis = ModBlocks.TRELLIS_ACACIA.get().defaultBlockState();
                case 5 -> trellis = ModBlocks.TRELLIS_DARK_OAK.get().defaultBlockState();
                case 6 -> trellis = ModBlocks.TRELLIS_MANGROVE.get().defaultBlockState();
                case 7 -> trellis = ModBlocks.TRELLIS_CHERRY.get().defaultBlockState();
                case 8 -> trellis = ModBlocks.TRELLIS_BAMBOO.get().defaultBlockState();
                case 9 -> trellis = ModBlocks.TRELLIS_CRIMSON.get().defaultBlockState();
                case 10 -> trellis = ModBlocks.TRELLIS_WARPED.get().defaultBlockState();
                default -> trellis = ModBlocks.TRELLIS_OAK.get().defaultBlockState();
            }
            level.setBlock(pos, trellis, 3);
            BlockState updated = trellis.updateShape(Direction.UP, level.getBlockState(pos.above()), level, pos, pos.above());
            level.setBlock(pos, updated, 3);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockState trellis;
            switch (state.getValue(VARIANT)) {
                case 1 -> trellis = ModBlocks.TRELLIS_SPRUCE.get().defaultBlockState();
                case 2 -> trellis = ModBlocks.TRELLIS_BIRCH.get().defaultBlockState();
                case 3 -> trellis = ModBlocks.TRELLIS_JUNGLE.get().defaultBlockState();
                case 4 -> trellis = ModBlocks.TRELLIS_ACACIA.get().defaultBlockState();
                case 5 -> trellis = ModBlocks.TRELLIS_DARK_OAK.get().defaultBlockState();
                case 6 -> trellis = ModBlocks.TRELLIS_MANGROVE.get().defaultBlockState();
                case 7 -> trellis = ModBlocks.TRELLIS_CHERRY.get().defaultBlockState();
                case 8 -> trellis = ModBlocks.TRELLIS_BAMBOO.get().defaultBlockState();
                case 9 -> trellis = ModBlocks.TRELLIS_CRIMSON.get().defaultBlockState();
                case 10 -> trellis = ModBlocks.TRELLIS_WARPED.get().defaultBlockState();
                default -> trellis = ModBlocks.TRELLIS_OAK.get().defaultBlockState();
            }
            level.setBlock(pos, trellis, 3);
            BlockState updated = trellis.updateShape(Direction.UP, level.getBlockState(pos.above()), level, pos, pos.above());
            level.setBlock(pos, updated, 3);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);

        // When being removed and replaced by air (i.e. actually broken), place a trellis.
        // Do nothing if being replaced by another trellis (prevents loops) or replaced by same block.
        if (!level.isClientSide && newState.isAir()) {
            BlockState trellis;
            switch (state.getValue(VARIANT)) {
                case 1 -> trellis = ModBlocks.TRELLIS_SPRUCE.get().defaultBlockState();
                case 2 -> trellis = ModBlocks.TRELLIS_BIRCH.get().defaultBlockState();
                case 3 -> trellis = ModBlocks.TRELLIS_JUNGLE.get().defaultBlockState();
                case 4 -> trellis = ModBlocks.TRELLIS_ACACIA.get().defaultBlockState();
                case 5 -> trellis = ModBlocks.TRELLIS_DARK_OAK.get().defaultBlockState();
                case 6 -> trellis = ModBlocks.TRELLIS_MANGROVE.get().defaultBlockState();
                case 7 -> trellis = ModBlocks.TRELLIS_CHERRY.get().defaultBlockState();
                case 8 -> trellis = ModBlocks.TRELLIS_BAMBOO.get().defaultBlockState();
                case 9 -> trellis = ModBlocks.TRELLIS_CRIMSON.get().defaultBlockState();
                case 10 -> trellis = ModBlocks.TRELLIS_WARPED.get().defaultBlockState();
                default -> trellis = ModBlocks.TRELLIS_OAK.get().defaultBlockState();
            }
            level.setBlock(pos, trellis, 3);
            BlockState updated = trellis.updateShape(Direction.UP, level.getBlockState(pos.above()), level, pos, pos.above());
            level.setBlock(pos, updated, 3);
        }
    }

    public BlockState updateWireConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        BlockState updated = state;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BooleanProperty wireProp = getWireProp(dir);
            BlockPos checkPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(checkPos);
            boolean hasWire = false;

            // Count trellis wire or grape wire crop only when:
            // - the neighbor has no FACING property (treat as always connected), OR
            // - the neighbor's FACING points either toward OR away from this block
            if (neighborState.is(ModBlocks.TRELLIS_WIRE.get()) || neighborState.is(ModBlocks.GRAPE_WIRE_CROP_RED.get()) ||
                    neighborState.is(ModBlocks.GRAPE_WIRE_CROP_WHITE.get())) {
                if (!neighborState.hasProperty(GrapeWireCropBlock.FACING)) {
                    // neighbor has no facing property (e.g. trellis wire); count it
                    hasWire = true;
                } else {
                    Direction neighborFacing = neighborState.getValue(GrapeWireCropBlock.FACING);
                    // Accept facing toward this block OR away from this block
                    if (neighborFacing == dir.getOpposite() || neighborFacing == dir) {
                        hasWire = true;
                    }
                }
            }

            updated = updated.setValue(wireProp, hasWire);
        }
        return updated;
    }

    // Helper that decides whether we can spread from ground crop at `pos` into a given wire at `wirePos`.
    // Uses same policy as GrapeWireCropBlock.canSpreadToWire but also ensures there's a TrellisWire block to replace.
    private boolean canGroundSpreadToWire(LevelAccessor level, BlockPos wirePos, Direction dir) {
        BlockState s = level.getBlockState(wirePos);

        // must be a trellis wire or grape wire crop to be a candidate
        if (!s.is(ModBlocks.TRELLIS_WIRE.get()) && !s.is(ModBlocks.GRAPE_WIRE_CROP_RED.get()) &&
                !s.is(ModBlocks.GRAPE_WIRE_CROP_WHITE.get())) return false;

        // If the neighbor has a FACING property (directional), require it to be aligned (toward or away)
        if (s.hasProperty(GrapeWireCropBlock.FACING)) {
            Direction neighborFacing = s.getValue(GrapeWireCropBlock.FACING);
            return neighborFacing == dir || neighborFacing == dir.getOpposite();
        }

        // If no facing property, allow (legacy / non-directional wires)
        return true;
    }

    /**
     * Attempt to spread from the ground grape at 'pPos' into adjacent TrellisWire blocks.
     * Places the GRAPE_WIRE_CROP block at each valid wire position.
     */
    private void attemptSideSpread(LevelAccessor pLevel, BlockPos pPos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos wirePos = pPos.relative(dir);
            if (!canGroundSpreadToWire(pLevel, wirePos, dir)) continue;

            // Create child block state (default) for the wire-crop.
            // NOTE: we attempt to set initial wire flags so the new block's WIRE_* properties are correct immediately.
            // This requires that TrellisBlock.getWireProp(dir) returns the same BooleanProperty instances the wire-crop uses.
            BlockState child = this.wireCropVariant.get().defaultBlockState()
            .setValue(GrapeWireCropBlock.AGE, 0).setValue(GrapeWireCropBlock.FACING, dir); // set facing so it spreads on this axis

            // Place the wire-crop (3 = update client & neighbors)
            pLevel.setBlock(wirePos, child, 3);

            // Optional: update neighboring grape blocks so they recompute their wire flags immediately.
            // This is useful to avoid one-tick inconsistencies:
            for (Direction nDir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = wirePos.relative(nDir);
                // if neighbor is a ground grape, call neighbor's update helper by re-setting its state to trigger neighborChanged
                if (pLevel.getBlockState(neighbor).is(this)) {
                    BlockState ns = pLevel.getBlockState(neighbor);
                    // re-apply the same state (this triggers block updates) if needed
                    pLevel.setBlock(neighbor, ns, 3);
                }
            }
        }
    }
}
