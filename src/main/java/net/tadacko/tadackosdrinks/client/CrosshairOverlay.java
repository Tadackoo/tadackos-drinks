package net.tadacko.tadackosdrinks.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.GrapeCropBlock;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.block.TrellisBlock;
import net.tadacko.tadackosdrinks.item.RopeBlockItem;
import net.tadacko.tadackosdrinks.item.TrellisWireItem;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CrosshairOverlay {
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Player player = mc.player;
        ItemStack heldItem = player.getMainHandItem();

        boolean hasWire = heldItem.getItem() instanceof TrellisWireItem;
        boolean hasRope = heldItem.getItem() instanceof RopeBlockItem;

        // offhand
        if (!hasWire && !hasRope) {
            heldItem = player.getOffhandItem();
            hasWire = heldItem.getItem() instanceof TrellisWireItem;
            hasRope = heldItem.getItem() == ModBlocks.ROPE.get().asItem();
            if (!hasWire && !hasRope) return;
        }

        // Get what the player is looking at
        HitResult hitResult = mc.hitResult;

        boolean isViable = false;

        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos clickedPos = blockHit.getBlockPos();
            Level level = mc.level;
            BlockState clickedState = level.getBlockState(clickedPos);

            if (hasWire) {
                boolean isTrellis = clickedState.getBlock() instanceof TrellisBlock ||
                        clickedState.getBlock() instanceof GrapeCropBlock;

                if (isTrellis) {
                    if (!TrellisWireClientState.hasFirstPos()) {
                        isViable = true; // selecting first is always viable
                    } else {
                        BlockPos firstPos = TrellisWireClientState.getFirstPos();
                        isViable = isValidSecondPosition(level, player, firstPos, clickedPos);
                    }
                }

            } else if (hasRope) {
                Direction face = blockHit.getDirection();

                boolean ok;

                if (clickedState.getBlock() instanceof TrellisBlock || clickedState.is(ModBlocks.ROPE.get())) {
                    BlockPos belowPos = clickedPos.below();
                    if (canPlaceRopeHere(level, belowPos)) {
                        ok = true;
                    } else {
                        BlockPos normalTarget = clickedPos.relative(face);
                        ok = canPlaceRopeHere(level, normalTarget);
                    }
                } else {
                    BlockPos normalTarget = clickedPos.relative(face);
                    ok = canPlaceRopeHere(level, normalTarget);
                }

                isViable = ok;
            }
        }

        renderCrosshairOverlay(event.getGuiGraphics().pose(), mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(), isViable);
    }

    private static boolean isValidSecondPosition(Level level, Player player, BlockPos firstPos, BlockPos secondPos) {
        // Must be same Y
        if (firstPos.getY() != secondPos.getY()) return false;

        // Must be aligned on X or Z (not diagonal)
        boolean sameX = firstPos.getX() == secondPos.getX();
        boolean sameZ = firstPos.getZ() == secondPos.getZ();
        if (sameX == sameZ) return false;

        int distance = sameX ? Math.abs(firstPos.getZ() - secondPos.getZ()) : Math.abs(firstPos.getX() - secondPos.getX());
        int countNeeded = distance - 1;

        if (countNeeded <= 0) return false;

        if (!player.isCreative()) {
            int totalAvailable = countItemInPlayerInventory(player, player.getMainHandItem().getItem());
            if (totalAvailable < countNeeded) return false;
        }

        // Build positions
        List<BlockPos> positions = new ArrayList<>();
        if (sameX) {
            int minZ = Math.min(firstPos.getZ(), secondPos.getZ());
            for (int i = 1; i <= countNeeded; i++) {
                positions.add(new BlockPos(secondPos.getX(), secondPos.getY(), minZ + i));
            }
        } else {
            int minX = Math.min(firstPos.getX(), secondPos.getX());
            for (int i = 1; i <= countNeeded; i++) {
                positions.add(new BlockPos(minX + i, secondPos.getY(), secondPos.getZ()));
            }
        }

        // Check for obstacles
        for (BlockPos wirePos : positions) {
            BlockState stateAt = level.getBlockState(wirePos);
            if (!stateAt.isAir() && !stateAt.canBeReplaced()) return false;
        }

        return true;
    }

    private static boolean canPlaceRopeHere(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        BlockState stateAtPos = level.getBlockState(pos);

        boolean hasSupport = aboveState.getBlock() instanceof TrellisBlock || aboveState.is(ModBlocks.ROPE.get());

        boolean canReplace = stateAtPos.isAir() || stateAtPos.canBeReplaced();

        return hasSupport && canReplace;
    }

    private static int countItemInPlayerInventory(Player player, net.minecraft.world.item.Item target) {
        int count = 0;
        for (ItemStack s : player.getInventory().items) {
            if (!s.isEmpty() && s.getItem() == target) count += s.getCount();
        }
        for (ItemStack s : player.getInventory().offhand) {
            if (!s.isEmpty() && s.getItem() == target) count += s.getCount();
        }
        return count;
    }

    private static void renderCrosshairOverlay(PoseStack poseStack, int screenWidth, int screenHeight, boolean viable) {
        float cx = screenWidth / 2f - 0.5f;
        float cy = screenHeight / 2f;

        // sizes in screen pixels (floats for smoothness across resolutions)
        float outerSize = 6f; // distance from center to outer corner
        float redOuterSize = outerSize - 2f; // shorten red lines by 2 pixels
        float innerSize = 2f; // distance from center to inner corner / tip
        float cornerThickness = 1f; // thickness of L-shape arms
        float pixelSize = 1f; // size of each "pixel" square for diagonal
        float pixelSpacing = 1f; // spacing between squares along diagonal

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull(); // ensure filled rects render correctly

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        // Use TRIANGLES (filled quads) for everything
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        if (viable) {
            // Draw green L-shapes in corners (two rectangles each)
            float r = 0f, g = 1f, b = 0f, a = 0.95f;

            // inverted L (arms extend outward from inner corner toward outer)
            // Top-left: horizontal arm placed just above inner row, vertical arm placed just left of inner column
            addRect(buffer, matrix,
                    cx - outerSize, cy - innerSize - cornerThickness,    // left, top
                    cx - innerSize, cy - innerSize, // right, bottom
                    r, g, b, a); // horizontal arm
            addRect(buffer, matrix,
                    cx - innerSize - cornerThickness, cy - outerSize,    // left, top
                    cx - innerSize, cy - innerSize, // right, bottom
                    r, g, b, a); // vertical arm

            // Top-right
            addRect(buffer, matrix,
                    cx + innerSize, cy - innerSize - cornerThickness,
                    cx + outerSize, cy - innerSize,
                    r, g, b, a);
            addRect(buffer, matrix,
                    cx + innerSize, cy - outerSize,
                    cx + innerSize + cornerThickness, cy - innerSize,
                    r, g, b, a);

            // Bottom-left
            addRect(buffer, matrix,
                    cx - outerSize, cy + innerSize,
                    cx - innerSize, cy + innerSize + cornerThickness,
                    r, g, b, a);
            addRect(buffer, matrix,
                    cx - innerSize - cornerThickness, cy + innerSize,
                    cx - innerSize, cy + outerSize,
                    r, g, b, a);

            // Bottom-right
            addRect(buffer, matrix,
                    cx + innerSize, cy + innerSize,
                    cx + outerSize, cy + innerSize + cornerThickness,
                    r, g, b, a);
            addRect(buffer, matrix,
                    cx + innerSize, cy + innerSize,
                    cx + innerSize + cornerThickness, cy + outerSize,
                    r, g, b, a);
        } else {
            // Draw red diagonal "pixel" lines: place axis-aligned squares along the diagonal
            float r = 1f, g = 0f, b = 0f, a = 0.95f;

            // top-left -> inner
            addPixelatedDiagonal(buffer, matrix,
                    cx - redOuterSize, cy - redOuterSize,
                    cx - innerSize, cy - innerSize,
                    pixelSize, pixelSpacing, r, g, b, a);

            // top-right -> inner
            addPixelatedDiagonal(buffer, matrix,
                    cx + redOuterSize, cy - redOuterSize,
                    cx + innerSize, cy - innerSize,
                    pixelSize, pixelSpacing, r, g, b, a);

            // bottom-left -> inner
            addPixelatedDiagonal(buffer, matrix,
                    cx - redOuterSize, cy + redOuterSize,
                    cx - innerSize, cy + innerSize,
                    pixelSize, pixelSpacing, r, g, b, a);

            // bottom-right -> inner
            addPixelatedDiagonal(buffer, matrix,
                    cx + redOuterSize, cy + redOuterSize,
                    cx + innerSize, cy + innerSize,
                    pixelSize, pixelSpacing, r, g, b, a);
        }

        tesselator.end();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // Add a filled rectangle defined by two opposite corners
    private static void addRect(BufferBuilder buffer, Matrix4f matrix,
                         float x1, float y1, float x2, float y2,
                         float r, float g, float b, float a) {
        float left = Math.min(x1, x2);
        float right = Math.max(x1, x2);
        float top = Math.min(y1, y2);
        float bottom = Math.max(y1, y2);

        // two triangles forming rectangle
        buffer.vertex(matrix, left, top, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, right, top, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, right, bottom, 0).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, left, top, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, right, bottom, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, left, bottom, 0).color(r, g, b, a).endVertex();
    }

    // Place a series of axis-aligned squares from (x1,y1) -> (x2,y2).
    // squareSize = size of each square in pixels; spacing = distance between square centers.
    private static void addPixelatedDiagonal(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2,
                                      float squareSize, float spacing, float r, float g, float b, float a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.hypot(dx, dy);
        if (len <= 0f) {
            // fallback: draw a single square at x2,y2
            addCenteredSquare(buffer, matrix, x2, y2, squareSize, r, g, b, a);
            return;
        }

        // How many steps (ensure at least 1)
        int steps = Math.max(1, (int) Math.floor(len / spacing));
        // spacing fraction along the segment
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            float px = x1 + dx * t;
            float py = y1 + dy * t;
            addCenteredSquare(buffer, matrix, px, py, squareSize, r, g, b, a);
        }
    }

    // Draw an axis-aligned square centered on (cx,cy)
    private static void addCenteredSquare(BufferBuilder buffer, Matrix4f matrix,
                                   float cx, float cy, float size,
                                   float r, float g, float b, float a) {
        float half = size / 2f;
        addRect(buffer, matrix, cx - half, cy - half, cx + half, cy + half, r, g, b, a);
    }
}
