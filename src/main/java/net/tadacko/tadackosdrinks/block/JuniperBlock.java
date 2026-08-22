package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.tadacko.tadackosdrinks.item.ModItems;

public class JuniperBlock extends BushBlock implements BonemealableBlock {
    public static final int MAX_AGE = 4;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
    public static final EnumProperty<JuniperPart> PART = EnumProperty.create("part", JuniperPart.class);

    public JuniperBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(PART, JuniperPart.BOTTOM));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
        if (pState.getValue(AGE) == 0 || (pState.getValue(AGE) == 2 && pState.getValue(PART) == JuniperPart.MIDDLE) ||
                ((pState.getValue(AGE) == 3 || pState.getValue(AGE) == 4) && pState.getValue(PART) == JuniperPart.TOP)) {
            shape = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);
        }
        return shape;
    }

    // Only the bottom block drives growth for the whole column.
    public boolean isRandomlyTicking(BlockState pState) {
        return pState.getValue(PART) == JuniperPart.BOTTOM && pState.getValue(AGE) < MAX_AGE;
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pLevel.isAreaLoaded(pPos, 1)) return;
        int age = pState.getValue(AGE);
        if (age < MAX_AGE && pLevel.getRawBrightness(pPos.above(), 0) >= 9 &&
                ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt(30) == 0)) { // 1/30 chance per random tick
            growColumn(pLevel, pPos, age + 1);
            ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
        }
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        int age = pState.getValue(AGE);
        boolean maxAge = age == MAX_AGE;
        if (!maxAge && pPlayer.getItemInHand(pHand).is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        } else if (maxAge) {
            if (!pLevel.isClientSide) {
                int j = 2 + pLevel.random.nextInt(2);
                ItemStack drop = new ItemStack(ModItems.JUNIPER_BERRIES.get(), j);

                // try to give to player, otherwise spawn as an entity
                if (!pPlayer.addItem(drop)) {
                    ItemEntity itemEntity = new ItemEntity(pLevel,
                            pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, drop);
                    pLevel.addFreshEntity(itemEntity);
                }

                // drop age of the WHOLE column by one; height stays the same (age 3 and 4 are both 3-tall)
                BlockPos basePos = getBasePos(pLevel, pPos);
                growColumn(pLevel, basePos, MAX_AGE - 1);
            }
            pLevel.playSound(null, pPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F +
                    pLevel.random.nextFloat() * 0.4F);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        } else {
            return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE, PART);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        if (pState.getValue(PART) == JuniperPart.BOTTOM) {
            return super.canSurvive(pState, pLevel, pPos);
        }
        // middle/top segments require another juniper block directly below them
        return pLevel.getBlockState(pPos.below()).is(this);
    }

    public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
        return pState.getValue(AGE) < MAX_AGE;
    }

    public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        return true;
    }

    public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        BlockPos basePos = getBasePos(pLevel, pPos);
        int newAge = Math.min(MAX_AGE, pState.getValue(AGE) + 1);
        growColumn(pLevel, basePos, newAge);
    }

    /**
     * Height (in blocks) of the plant at a given age: 1 block for age 0-1, 2 blocks at age 2, 3 blocks at age 3-4.
     */
    private static int heightForAge(int age) {
        if (age <= 1) return 1;
        if (age == 2) return 2;
        return 3;
    }

    private static JuniperPart partForIndex(int index) {
        return switch (index) {
            case 0 -> JuniperPart.BOTTOM;
            case 1 -> JuniperPart.MIDDLE;
            default -> JuniperPart.TOP;
        };
    }

    /**
     * Walks down from pPos through juniper blocks until it finds the BOTTOM segment.
     */
    private static BlockPos getBasePos(BlockGetter pLevel, BlockPos pPos) {
        BlockPos.MutableBlockPos mutable = pPos.mutable();
        while (true) {
            BlockState state = pLevel.getBlockState(mutable);
            if (!(state.getBlock() instanceof JuniperBlock) || state.getValue(PART) == JuniperPart.BOTTOM) {
                break;
            }
            mutable.move(Direction.DOWN);
        }
        return mutable.immutable();
    }

    /**
     * Sets AGE on every existing segment of the column starting at basePos, and extends the column
     * upward with new segments if the target age requires more height (stops early if blocked).
     */
    public void growColumn(LevelAccessor pLevel, BlockPos basePos, int newAge) {
        int newHeight = heightForAge(newAge);
        for (int i = 0; i < newHeight; i++) {
            BlockPos pos = basePos.above(i);
            BlockState existing = pLevel.getBlockState(pos);
            JuniperPart part = partForIndex(i);
            BlockState newState;
            if (existing.getBlock() == this) {
                newState = existing.setValue(AGE, newAge).setValue(PART, part);
            } else if (existing.canBeReplaced()) {
                newState = this.defaultBlockState().setValue(AGE, newAge).setValue(PART, part);
            } else {
                break; // something is blocking growth upward
            }
            pLevel.setBlock(pos, newState, 2);
            pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        // pNewState.getBlock() != this means the segment was actually removed (not a growth/part update
        // where it stays a JuniperBlock, just with a different AGE/PART value).
        if (!pLevel.isClientSide && pNewState.getBlock() != this) {
            destroyColumnExcept(pLevel, pPos, pState);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    /**
     * Destroys every remaining segment of the column pPos belonged to (dropping their contents),
     * skipping pPos itself since the caller is already replacing it.
     */
    private void destroyColumnExcept(Level pLevel, BlockPos pPos, BlockState pState) {
        BlockPos basePos = getBaseFromPart(pPos, pState.getValue(PART));
        int height = heightForAge(pState.getValue(AGE));
        for (int i = 0; i < height; i++) {
            BlockPos segPos = basePos.above(i);
            if (segPos.equals(pPos)) continue;
            if (pLevel.getBlockState(segPos).getBlock() == this) {
                pLevel.destroyBlock(segPos, true);
            }
        }
    }

    /**
     * Computes the base position from a known segment position + its PART, without querying the
     * live world state at pPos (which may already have changed to pNewState by the time onRemove runs).
     */
    private static BlockPos getBaseFromPart(BlockPos pPos, JuniperPart part) {
        int index = switch (part) {
            case BOTTOM -> 0;
            case MIDDLE -> 1;
            case TOP -> 2;
        };
        return pPos.below(index);
    }

    public enum JuniperPart implements StringRepresentable {
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top");

        private final String name;

        JuniperPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}