package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class GrapeWireCropBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 1);
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 16.0D);
    private static final VoxelShape SHAPE_EAST_WEST = Block.box(0.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);

    // natural spread chance: 1 in SPREAD_TIME each eligible tick (set to 1 for always)
    private static final int SPREAD_TIME = 5;
    private static final int GRAPE_TIME = 20;

    public static final IntegerProperty UNSUPPORTED = IntegerProperty.create("unsupported", 0, 2);
    // how many scheduled ticks without support before breaking
    private static final int UNSUPPORTED_THRESHOLD = 2;
    // schedule delay between checks (in ticks)
    private static final int CHECK_DELAY = 1; // check every tick while unsupported

    private final Supplier<Item> grapeItem;
    private final Supplier<Block> sameVariantWire;

    public GrapeWireCropBlock(Properties properties, Supplier<Item> grapeItem, Supplier<Block> sameVariantWire) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(FACING, Direction.NORTH)
                .setValue(UNSUPPORTED, 0));
        this.grapeItem = grapeItem;
        this.sameVariantWire = sameVariantWire;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return (facing.getAxis() == Direction.Axis.X) ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, FACING, UNSUPPORTED);
    }

    // Random tick: grow 0->1 sometimes, and attempt sideways spread at age 0 or 1
    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!world.isAreaLoaded(pos, 1)) return;

        int age = state.getValue(AGE);

        // Grow occasionally from 0 -> 1
        if (age < 1 && random.nextInt(GRAPE_TIME) == 0) {
            BlockState grown = state.setValue(AGE, 1);
            world.setBlock(pos, grown, 3);
            return;
        }

        // Spread attempts at both age 0 and age 1 (with a little natural randomness)
        if (age == 0 || age == 1) {
            if (random.nextInt(SPREAD_TIME) != 0) return; // natural probability; remove if you want guaranteed

            Direction facing = state.getValue(FACING);
            Direction dir1 = facing;
            Direction dir2 = facing.getOpposite();

            for (Direction dir : new Direction[]{dir1, dir2}) {
                BlockPos target = pos.relative(dir);
                if (world.getBlockState(target).is(ModBlocks.TRELLIS_WIRE.get())) {
                    // place child without doing support checks here; the child will validate itself
                    BlockState child = this.sameVariantWire.get().defaultBlockState()
                            .setValue(AGE, 0)
                            .setValue(FACING, facing);
                    world.setBlock(target, child, 3);
                }
            }
        }
    }

    // Right-click harvesting: when age == 1 give 4 grapes and revert to age 0
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            // client thinks success so the server handles the actual action
            return InteractionResult.SUCCESS;
        }

        if (player.getItemInHand(hand).is(Items.BONE_MEAL)) return InteractionResult.PASS;

        int age = state.getValue(AGE);
        if (age == 1) {
            ItemStack grapesStack = new ItemStack(this.grapeItem.get(), 4);

            // try to add to player's inventory, otherwise spawn into world as entity
            boolean added = player.addItem(grapesStack);
            if (!added) {
                ItemEntity ent = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, grapesStack);
                level.addFreshEntity(ent);
            }

            // revert to age 0 and update block
            BlockState newState = state.setValue(AGE, 0);
            level.setBlock(pos, newState, 3);

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = this.defaultBlockState();

        // inherit facing from existing neighboring wire crops if possible
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState neigh = level.getBlockState(pos.relative(dir));
            if (neigh.getBlock() instanceof GrapeWireCropBlock) {
                state = state.setValue(FACING, neigh.getValue(FACING));
                break;
            }
        }
        return state;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        // tick when age 0 or 1 so it can grow and spread
        int age = state.getValue(AGE);
        return age == 0 || age == 1;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Check East & West (east must face west, west must face east)
        boolean east = isConnectorFacing(level, pos.east(), Direction.WEST);
        boolean west = isConnectorFacing(level, pos.west(), Direction.EAST);

        // Check North & South (north must face south, south must face north)
        boolean north = isConnectorFacing(level, pos.north(), Direction.SOUTH);
        boolean south = isConnectorFacing(level, pos.south(), Direction.NORTH);

        // survive if connected on both sides along X (east+west) OR along Z (north+south)
        return (east && west) || (north && south);
    }

    // Destroy when supports removed
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, changedBlock, fromPos, isMoving);
        if (level.isClientSide) return;

        Direction.Axis axis = state.getValue(FACING).getAxis();
        // If the line is not connected to any ground grape, break the entire line
        boolean connected = isLineConnectedToGround(level, pos, axis);
        if (!connected) {
            breakLine(level, pos, axis);
        }

        // schedule a survival check shortly after neighbor changed
        level.scheduleTick(pos, this, CHECK_DELAY);
    }

    // --- Bonemeal support --------------------------------------------
    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, boolean isClient) {
        int age = state.getValue(AGE);
        if (age < 1) return true; // can grow to age 1
        // age == 1: bonemeal valid if there's any adjacent trellis_wire along facing axis
        Direction facing = state.getValue(FACING);
        BlockPos a = pos.relative(facing);
        BlockPos b = pos.relative(facing.getOpposite());
        return world.getBlockState(a).is(ModBlocks.TRELLIS_WIRE.get()) || world.getBlockState(b).is(ModBlocks.TRELLIS_WIRE.get());
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource rand, BlockPos pos, BlockState state) {
        // make it always succeed; change to probabilistic if you want
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource rand, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        Direction facing = state.getValue(FACING);

        // If not yet age 1, grow to age 1
        if (age < 1) {
            BlockState grown = state.setValue(AGE, 1);
            world.setBlock(pos, grown, 3);
            return;
        }

        // If already age 1, attempt to place children on trellis wires along facing axis
        for (Direction dir : new Direction[]{facing, facing.getOpposite()}) {
            BlockPos target = pos.relative(dir);
            if (world.getBlockState(target).is(ModBlocks.TRELLIS_WIRE.get())) {
                BlockState child = this.sameVariantWire.get().defaultBlockState()
                        .setValue(AGE, 0)
                        .setValue(FACING, facing);
                world.setBlock(target, child, 3);
            }
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity tileEntity, ItemStack stack) {
        super.playerDestroy(level, player, pos, state, tileEntity, stack);
        if (!level.isClientSide) {
            BlockState trellisWire = ModBlocks.TRELLIS_WIRE.get().defaultBlockState().setValue(TrellisWireBlock.FACING, state.getValue(FACING));
            level.setBlock(pos, trellisWire, 3);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockState trellisWire = ModBlocks.TRELLIS_WIRE.get().defaultBlockState().setValue(TrellisWireBlock.FACING, state.getValue(FACING));
            level.setBlock(pos, trellisWire, 3);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);

        // When being removed and replaced by air (i.e. actually broken), place a trellis.
        // Do nothing if being replaced by another trellis (prevents loops) or replaced by same block.
        if (!level.isClientSide && newState.isAir()) {
            BlockState trellisWire = ModBlocks.TRELLIS_WIRE.get().defaultBlockState().setValue(TrellisWireBlock.FACING, state.getValue(FACING));
            level.setBlock(pos, trellisWire, 3);
            level.scheduleTick(pos, ModBlocks.TRELLIS_WIRE.get(), CHECK_DELAY);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        // check survival (use your existing canSurvive logic)
        boolean survives = this.canSurvive(state, level, pos);

        int unsupported = state.getValue(UNSUPPORTED);

        if (survives) {
            // reset counter if it was non-zero
            if (unsupported != 0) {
                BlockState reset = state.setValue(UNSUPPORTED, 0);
                level.setBlock(pos, reset, 3);
            }
            return;
        }

        // still unsupported: increment counter
        int next = Math.min(UNSUPPORTED_THRESHOLD, unsupported + CHECK_DELAY);
        BlockState updated = state.setValue(UNSUPPORTED, next);
        level.setBlock(pos, updated, 3);

        if (next >= UNSUPPORTED_THRESHOLD) {
            // destroy and drop items
            level.destroyBlock(pos, true);
        } else {
            // schedule another check
            level.scheduleTick(pos, this, CHECK_DELAY);
        }
    }

    private boolean isConnectorFacing(BlockGetter world, BlockPos checkPos, Direction requiredFacing) {
        BlockState s = world.getBlockState(checkPos);

        // Always accept ground grape and plain trellis (they don't have FACING)
        if (s.is(ModBlocks.GRAPE_CROP_RED.get()) || s.is(ModBlocks.GRAPE_CROP_WHITE.get()) || s.getBlock() instanceof TrellisBlock) {
            return true;
        }

        // For wire-like blocks, require a FACING that points toward this block
        if (s.is(this.sameVariantWire.get()) || s.is(ModBlocks.TRELLIS_WIRE.get())) {
            if (s.hasProperty(GrapeWireCropBlock.FACING)) {
                Direction neighborFacing = s.getValue(GrapeWireCropBlock.FACING);
                return neighborFacing == requiredFacing || neighborFacing == requiredFacing.getOpposite();
            }
            // If the wire block doesn't have a FACING property, treat it as NOT a valid facing connector.
            // (Change to `return true;` here if you want non-directional trellis_wire to count.)
        }

        return false;
    }

    private boolean isLineConnectedToGround(LevelAccessor level, BlockPos startPos, Direction.Axis axis) {
        // Check the whole contiguous line of grape-wire-crops along `axis`
        // Return true if any wire in that contiguous segment has an adjacent GRAPE_CROP.
        // axis == X => iterate EAST/WEST, axis == Z => iterate SOUTH/NORTH

        // direction along axis
        Direction positive = (axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        Direction negative = positive.getOpposite();

        // check starting position and extend in both directions
        // check start pos first
        if (adjacentToGroundGrape(level, startPos)) return true;

        // scan positive direction
        BlockPos p = startPos.relative(positive);
        while (level.getBlockState(p).is(this)) {
            if (adjacentToGroundGrape(level, p)) return true;
            p = p.relative(positive);
        }

        // scan negative direction
        p = startPos.relative(negative);
        while (level.getBlockState(p).is(this)) {
            if (adjacentToGroundGrape(level, p)) return true;
            p = p.relative(negative);
        }

        // no ground grape found in the whole contiguous segment
        return false;
    }

    /** Helper: true if any orthogonal neighbor (N/S/E/W) of pos is a ground grape block. */
    private boolean adjacentToGroundGrape(LevelAccessor level, BlockPos pos) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(d)).is(ModBlocks.GRAPE_CROP_RED.get()) ||
                    level.getBlockState(pos.relative(d)).is(ModBlocks.GRAPE_CROP_WHITE.get())) return true;
        }
        return false;
    }

    /** Destroy the entire contiguous line of grape-wire-crops along the axis of the wire at 'pos'. */
    private void breakLine(LevelAccessor level, BlockPos startPos, Direction.Axis axis) {
        Direction positive = (axis == Direction.Axis.X) ? Direction.EAST : Direction.SOUTH;
        Direction negative = positive.getOpposite();

        // collect positions to destroy first (avoid modifying world while scanning)
        java.util.ArrayList<BlockPos> toDestroy = new java.util.ArrayList<>();
        toDestroy.add(startPos);

        BlockPos p = startPos.relative(positive);
        while (level.getBlockState(p).is(this)) {
            toDestroy.add(p);
            p = p.relative(positive);
        }

        p = startPos.relative(negative);
        while (level.getBlockState(p).is(this)) {
            toDestroy.add(p);
            p = p.relative(negative);
        }

        // Now destroy them (server-side). Use ServerLevel if available to drop properly.
        for (BlockPos pos : toDestroy) {
            if (level instanceof ServerLevel server) {
                server.destroyBlock(pos, true); // drops items
            } else {
                level.destroyBlock(pos, true);
            }
        }
    }
}
