package net.tadacko.tadackosdrinks.client.guide;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders a block model once into an offscreen RGBA texture, then caches the result, so callers
 * can blit the flat result as one quad with real alpha (avoiding the "see interior" artifact from
 * blending raw untested 3D geometry directly).
 *
 * HOW: calls GuideBlockScenes.renderBlockModel(...) UNCHANGED - the exact RenderType/
 * MultiBufferSource-based method already proven correct elsewhere (flowchart sub-icons,
 * BlockScene rendering). We deliberately do NOT hand-roll shader/vertex-format code (an earlier
 * attempt at that kept missing pieces RenderType normally handles automatically - depth state,
 * lightmap binding, atlas binding - and was hard to diagnose without a GPU debugger).
 *
 * The ONE thing RenderType does that fights a custom FBO is force-rebind
 * Minecraft.getMainRenderTarget() as part of its own setupRenderState(). So we temporarily swap
 * what that call RETURNS, via reflection on Minecraft's private mainRenderTarget field, so that
 * forced rebind lands on our FBO instead of the real window framebuffer. Always restored in a
 * finally block.
 *
 * FRAGILE POINT: relies on a field literally named "mainRenderTarget" on Minecraft (true for
 * Mojang-mapped 1.19.4/Forge). If MAIN_TARGET_FIELD ends up null (logged once), the field was
 * renamed for your setup - enumerate Minecraft.class.getDeclaredFields() to find the RenderTarget
 * one and update FIELD_NAME.
 */
final class BlockIconCache {
    private BlockIconCache() {}

    private static final String FIELD_NAME = "mainRenderTarget";
    private static final Field MAIN_TARGET_FIELD = resolveField();

    private static Field resolveField() {
        try {
            Field f = Minecraft.class.getDeclaredField(FIELD_NAME);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            System.err.println("[TadackosDrinks] BlockIconCache: Minecraft." + FIELD_NAME
                    + " not found - block background icons disabled. Enumerate Minecraft.class fields to find the correct name.");
            return null;
        }
    }

    private record Key(BlockState state, int size) {}
    private static final Map<Key, RenderTarget> CACHE = new HashMap<>();

    /** Returns a GL texture id for this block's icon, or -1 if unavailable (reflection failed). */
    static int get(PageContext ctx, BlockState state, int size) {
        if (MAIN_TARGET_FIELD == null) return -1;
        RenderTarget cached = CACHE.computeIfAbsent(new Key(state, size), k -> render(ctx, state, size));
        return cached == null ? -1 : cached.getColorTextureId();
    }

    private static RenderTarget render(PageContext ctx, BlockState state, int size) {
        Minecraft mc = Minecraft.getInstance();

        // FBO must be sized in physical pixels, not logical GUI pixels, or the result gets
        // upscaled (blurry) when later blitted at real screen resolution. Everything else
        // on screen benefits from this scale automatically via the main render target;
        // our offscreen target needs it applied explicitly.
        Window window = mc.getWindow();
        double guiScale = window.getGuiScale();
        int texSize = Math.max(1, Mth.ceil(size * guiScale));

        RenderTarget fbo = new TextureTarget(texSize, texSize, true, Minecraft.ON_OSX); // true = needs its own depth buffer
        fbo.setClearColor(0f, 0f, 0f, 0f);
        RenderTarget realMain;
        try {
            realMain = (RenderTarget) MAIN_TARGET_FIELD.get(mc);
        } catch (IllegalAccessException e) {
            return null;
        }

        RenderSystem.backupProjectionMatrix();
        PoseStack modelView = null;
        try {
            MAIN_TARGET_FIELD.set(mc, fbo); // redirect getMainRenderTarget() -> fbo for this draw only

            fbo.clear(Minecraft.ON_OSX);
            fbo.bindWrite(true);

            RenderSystem.backupProjectionMatrix();
            Matrix4f ortho = new Matrix4f().setOrtho(0, size, size, 0, -1000, 1000);
            RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);

            // GameRenderer leaves its own GUI-frame model-view transform active in RenderSystem's
            // separate static matrix (independent of any PoseStack we pass around). Our custom ortho
            // projection was very likely being combined with that leftover transform, pushing our
            // geometry outside the clip volume entirely - drawn, but never visible. Reset to identity
            // for the duration of this isolated draw, then restore.
            modelView = RenderSystem.getModelViewStack();
            modelView.pushPose();
            modelView.setIdentity();
            RenderSystem.applyModelViewMatrix();

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f); // defensive: this bakes into the cache once, don't inherit stale alpha

            // Proven path - identical call used for flowchart sub-icons / BlockScene rendering.
            // Its internal RenderType draws now rebind onto `fbo` (via the field swap above),
            // and RenderType's own state setup handles depth test/write, atlas + lightmap
            // binding for us - none of that needs to be replicated by hand here.
            GuideBlockScenes.renderBlockModel(new PoseStack(), ctx, state, 0, 0, size, 0, 0, 0);
        } catch (IllegalAccessException e) {
            return null;
        } finally {
            try {
                MAIN_TARGET_FIELD.set(mc, realMain); // always restore, even if the draw threw
            } catch (IllegalAccessException ignored) {
            }
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            realMain.bindWrite(true); // rebind + restore viewport for normal GUI rendering to continue
        }
        return fbo;
    }
}