package net.tadacko.tadackosdrinks.client.guide;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** One block placed in a BlockScene, at whole-block offsets (east/up/south) from the scene's anchor (0,0,0). */
record SceneBlock(int dx, int dy, int dz, BlockState state) {}

/**
 * A small multi-block diorama rendered as one isometric GUI icon.
 * Build one by chaining add() calls; put whichever block should visually center the icon at (0,0,0)
 * and offset the rest around it in whole-block units (+x east, +y up, +z south).
 */
final class BlockScene {
    final List<SceneBlock> blocks = new ArrayList<>();

    BlockScene add(int dx, int dy, int dz, BlockState state) {
        blocks.add(new SceneBlock(dx, dy, dz, state));
        return this;
    }
}

/**
 * Renders BlockScenes (and single block icons) as GUI icons using vanilla's fixed per-face shading
 * instead of a flat tint. Moved here from FlowchartPage so both single-icon and multi-block
 * rendering share one code path.
 */
final class GuideBlockScenes {
    private GuideBlockScenes() {}

    // Fixed isometric camera: tilt 30 down, yaw 225 - same angle FlowchartPage always used for
    // single block icons. Kept as constants so the depth-sort math below can't drift from the
    // actual render transform.
    private static final float PITCH_DEG = 30f, YAW_DEG = 225f;
    private static final double COS_PITCH = Math.cos(Math.toRadians(PITCH_DEG));
    private static final double SIN_PITCH = Math.sin(Math.toRadians(PITCH_DEG));
    private static final double COS_YAW = Math.cos(Math.toRadians(YAW_DEG));
    private static final double SIN_YAW = Math.sin(Math.toRadians(YAW_DEG));

    /**
     * View-space depth of a point under the fixed camera above (Ry(yaw) applied first, then
     * Rx(pitch), matching the pose.mulPose order in renderBlockModel). Larger = closer to the
     * viewer = must be drawn later (on top). This assumes the standard right-handed convention
     * where +Z faces the viewer post-rotation; if a scene ever renders with wrong occlusion,
     * this is the one place to flip (negate the return value).
     */
    private static double depthKey(double px, double py, double pz) {
        double z1 = -SIN_YAW * px + COS_YAW * pz; // after Ry(yaw)
        return SIN_PITCH * py + COS_PITCH * z1;   // after Rx(pitch)
    }

    // Vanilla's fixed per-face brightness multiplier (Level#getShade), used even for blocks
    // that don't get full ambient occlusion. Falls back to this if no level is available.
    static float shadeFor(ClientLevel level, Direction dir) {
        if (dir == null) return 1.0f;
        if (level != null) return level.getShade(dir, true);
        return switch (dir) {
            case UP -> 1.0f;
            case DOWN -> 0.5f;
            case NORTH, SOUTH -> 0.8f;
            case EAST, WEST -> 0.6f;
        };
    }

    /** Opaque, using the block's own model render types (solid/cutout/etc). Depth test disabled -
     * see the forceTranslucent overload's javadoc for why. */
    static void renderBlockModel(PoseStack pose, PageContext ctx, BlockState state, int x, int y, int size,
                                 double offsetX, double offsetY, double offsetZ) {
        renderBlockModel(pose, ctx, state, x, y, size, offsetX, offsetY, offsetZ, false, 1f);
    }

    /**
     * forceTranslucent: when true, quads are still sourced from whichever RenderType(s) the model
     * actually bakes per-layer (so multi-layer blocks - e.g. cutout leaves + solid stem - keep all
     * their faces), but every quad is submitted through RenderType.translucent() instead of its own
     * type. translucent is the only vanilla block render type that honors both GL blending and the
     * ColorModulator uniform (RenderSystem.setShaderColor); solid/cutout disable blending outright,
     * making alpha a no-op on those.
     *
     * Depth test is disabled for the duration of the draw, always (not just when forceTranslucent).
     * Block render types test/write the depth buffer since they're built for real 3D scenes; inside
     * a GUI screen that silently discards the draw against whatever's already in the depth buffer at
     * that pixel, with no relation to visual draw order. GUI content otherwise layers purely by call
     * order (blit never touches depth), so disabling depth test keeps block icons consistent with
     * that: visibility/order is controlled by when you call this, not by tuning z.
     */
    static void renderBlockModel(PoseStack pose, PageContext ctx, BlockState state, int x, int y, int size,
                                 double offsetX, double offsetY, double offsetZ,
                                 boolean forceTranslucent, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);

        BlockPos tintPos = mc.player != null ? mc.player.blockPosition() : BlockPos.ZERO;
        int tint = mc.getBlockColors().getColor(state, ctx.level(), tintPos, 0);
        float tr = ((tint >> 16) & 0xFF) / 255f;
        float tg = ((tint >> 8) & 0xFF) / 255f;
        float tb = (tint & 0xFF) / 255f;

        pose.pushPose();
        pose.translate(x + size / 2f, y + size / 2f, 150);
        pose.scale(size, -size, size); // flip Y: model space is Y-up, screen space is Y-down
        pose.mulPose(Axis.XP.rotationDegrees(PITCH_DEG));
        pose.mulPose(Axis.YP.rotationDegrees(YAW_DEG));
        pose.scale(0.5f, 0.5f, 0.5f);
        // -0.32,-0.25,-0.4 centers a single unit cube nicely in the icon box; offsetX/Y/Z shift
        // additional scene blocks by whole-block units around that same anchor point.
        pose.translate(-0.5 + offsetX, -0.5 + offsetY, -0.5 + offsetZ);

        RenderSystem.disableDepthTest();
        if (forceTranslucent) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        }

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RandomSource random = RandomSource.create(42L);
        Direction[] directions = new Direction[]{null, Direction.DOWN, Direction.UP,
                Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

        for (RenderType modelType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            VertexConsumer consumer = buffers.getBuffer(forceTranslucent ? RenderType.translucent() : modelType);
            for (Direction dir : directions) {
                float shade = shadeFor(ctx.level(), dir);
                random.setSeed(42L);
                for (BakedQuad quad : model.getQuads(state, dir, random, ModelData.EMPTY, modelType)) {
                    float r = (quad.isTinted() ? tr : 1f) * shade;
                    float g = (quad.isTinted() ? tg : 1f) * shade;
                    float b = (quad.isTinted() ? tb : 1f) * shade;
                    consumer.putBulkData(pose.last(), quad, r, g, b, LightTexture.pack(9, 9), OverlayTexture.NO_OVERLAY);
                }
            }
        }
        buffers.endBatch();

        if (forceTranslucent) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }
        RenderSystem.enableDepthTest();
        pose.popPose();
    }

    /** Renders every block in the scene at (x,y) with the given per-block icon box size, sorted back-to-front. */
    static void renderScene(PoseStack pose, PageContext ctx, int x, int y, int size, BlockScene scene) {
        List<SceneBlock> sorted = new ArrayList<>(scene.blocks);
        sorted.sort(Comparator.comparingDouble(b -> depthKey(b.dx() + 0.5, b.dy() + 0.5, b.dz() + 0.5)));
        for (SceneBlock b : sorted) {
            renderBlockModel(pose, ctx, b.state(), x, y, size, b.dx(), b.dy(), b.dz());
        }
    }
}

/** GuidePage rendering one BlockScene. The anchor block (0,0,0) is centered in the page's width. */
class ScenePage implements GuidePage {
    private final String title;
    private final BlockScene scene;
    private final int size;       // pixel size of a single block's icon box
    private final int pageHeight; // fixed vertical space this page reserves

    ScenePage(String title, BlockScene scene, int size, int pageHeight) {
        this.title = title;
        this.scene = scene;
        this.size = size;
        this.pageHeight = pageHeight;
    }

    @Override public String rawTitle() { return title; }

    @Override
    public void render(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        int x = ctx.pageX() + (ctx.pageWidth() - size) / 2;
        int y = ctx.pageY();
        GuideBlockScenes.renderScene(graphics.pose(), ctx, x, y, size, scene);
    }

    @Override
    public int height(PageContext ctx) {
        return pageHeight;
    }
}