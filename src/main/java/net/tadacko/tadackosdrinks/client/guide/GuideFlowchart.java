package net.tadacko.tadackosdrinks.client.guide;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Cached geometry helpers for flowchart node rendering.
 * The original code recomputed trig (360 sin/cos calls) and per-pixel sqrt for
 * every node, every single frame. Node radius/dash settings never change at
 * runtime, so all of that is precomputed once per radius and reused.
 */
final class GuideGeometry {
    private GuideGeometry() {}

    // All circle drawing derives from one boolean mask per size, sampled at pixel centers.
    // This guarantees the circle's bounding box is exactly size x size - identical to the
    // square nodes - instead of the old parametric approach which drew a 2r+1 circle (1px
    // taller/wider than the square) using a radius that didn't share the square's origin.
    private static final Map<Integer, boolean[][]> MASKS = new HashMap<>();
    private static final Map<Integer, int[][]> FILLED_SPANS = new HashMap<>(); // per row: {minX, maxX} inclusive, or {1,0} for none
    private static final Map<Integer, int[][]> OUTLINE_POINTS = new HashMap<>(); // {dx, dy} from top-left
    private static final Map<String, int[][]> DASHED_OUTLINE_POINTS = new HashMap<>();

    private static boolean[][] mask(int size) {
        return MASKS.computeIfAbsent(size, sz -> {
            double r = sz / 2.0;
            boolean[][] m = new boolean[sz][sz];
            for (int j = 0; j < sz; j++) {
                double dy = (j + 0.5) - r;
                for (int i = 0; i < sz; i++) {
                    double dx = (i + 0.5) - r;
                    m[j][i] = dx * dx + dy * dy <= r * r;
                }
            }
            return m;
        });
    }

    private static int[][] filledSpans(int size) {
        return FILLED_SPANS.computeIfAbsent(size, sz -> {
            boolean[][] m = mask(sz);
            int[][] spans = new int[sz][2];
            for (int j = 0; j < sz; j++) {
                int minI = sz, maxI = -1;
                for (int i = 0; i < sz; i++) {
                    if (m[j][i]) {
                        if (i < minI) minI = i;
                        maxI = i;
                    }
                }
                spans[j] = new int[]{minI, maxI};
            }
            return spans;
        });
    }

    /** Draws a filled circle inscribed in the size x size box at top-left (x,y) - same box a same-size square would use. */
    static void drawFilledCircle(GuiGraphics graphics, int x, int y, int size, int color) {
        if (size <= 0) return;
        int[][] spans = filledSpans(size);
        for (int j = 0; j < size; j++) {
            int minI = spans[j][0], maxI = spans[j][1];
            if (maxI < minI) continue;
            graphics.fill(x + minI, y + j, x + maxI + 1, y + j + 1, color);
        }
    }

    private static int[][] outlinePoints(int size) {
        return OUTLINE_POINTS.computeIfAbsent(size, sz -> {
            boolean[][] m = mask(sz);
            List<int[]> pts = new ArrayList<>();
            for (int j = 0; j < sz; j++) {
                for (int i = 0; i < sz; i++) {
                    if (!m[j][i]) continue;
                    boolean edge = i == 0 || i == sz - 1 || j == 0 || j == sz - 1
                            || !m[j][i - 1] || !m[j][i + 1] || !m[j - 1][i] || !m[j + 1][i];
                    if (edge) pts.add(new int[]{i, j});
                }
            }
            return pts.toArray(new int[0][]);
        });
    }

    /** Draws a 1px circle outline inscribed in the size x size box at top-left (x,y). */
    static void drawCircleOutline(GuiGraphics graphics, int x, int y, int size, int color) {
        if (size <= 0) return;
        for (int[] p : outlinePoints(size))
            graphics.fill(RenderType.guiOverlay(), x + p[0], y + p[1], x + p[0] + 1, y + p[1] + 1, color);
    }

    /** Dashed variant of drawCircleOutline - same size x size box, dash pattern picked by each point's angle. */
    static void drawDashedCircleOutline(GuiGraphics graphics, int x, int y, int size, int color, int dashPx, int gapPx) {
        if (size <= 0) return;
        String key = size + "_" + dashPx + "_" + gapPx;
        int[][] pts = DASHED_OUTLINE_POINTS.computeIfAbsent(key, k -> {
            double r = size / 2.0;
            double circumference = Math.PI * size;
            double dashDeg = Math.max(1.0, (dashPx / circumference) * 360.0);
            double gapDeg = Math.max(1.0, (gapPx / circumference) * 360.0);
            double cycle = dashDeg + gapDeg;
            List<int[]> out = new ArrayList<>();
            for (int[] p : outlinePoints(size)) {
                double dx = (p[0] + 0.5) - r, dy = (p[1] + 0.5) - r;
                double angle = Math.toDegrees(Math.atan2(dy, dx));
                if (angle < 0) angle += 360.0;
                if (angle % cycle < dashDeg) out.add(p);
            }
            return out.toArray(new int[0][]);
        });
        for (int[] p : pts)
            graphics.fill(RenderType.guiOverlay(), x + p[0], y + p[1], x + p[0] + 1, y + p[1] + 1, color);
    }

    // Matches the vanilla recipe arrow: a 14x3 shaft plus an 8x15 triangular head, in the same gray.
    static final int ARROW_WIDTH = 22, ARROW_HEIGHT = 15;
    private static final int ARROW_SHAFT_W = 14, ARROW_SHAFT_H = 3;
    private static final int ARROW_HEAD_W = 8;
    private static final int ARROW_COLOR = 0xFF8B8B8B;

    /** Draws the vanilla-style recipe arrow, top-left anchored at (x,y), 22x15 total. */
    static void drawArrowRight(GuiGraphics graphics, int x, int y) {
        int shaftTop = y + (ARROW_HEIGHT - ARROW_SHAFT_H) / 2;
        graphics.fill(RenderType.guiOverlay(), x, shaftTop, x + ARROW_SHAFT_W, shaftTop + ARROW_SHAFT_H, ARROW_COLOR);

        // Top and bottom edges of the head are each computed as one continuous line and rounded
        // independently per column. Rounding a per-column *height* and re-centering it (the previous
        // approach) makes the two edges drift relative to each other depending on that height's
        // parity, producing a slight kink partway through the taper instead of a straight line.
        double cyF = y + ARROW_HEIGHT / 2.0;
        for (int i = 0; i < ARROW_HEAD_W; i++) {
            double edgeHalf = (ARROW_HEIGHT / 2.0) * (1.0 - (double) i / ARROW_HEAD_W); // base -> tip
            int top = (int) Math.round(cyF - edgeHalf);
            int bottom = (int) Math.round(cyF + edgeHalf);
            if (bottom <= top) continue;
            int colX = x + ARROW_SHAFT_W + i;
            graphics.fill(RenderType.guiOverlay(), colX, top, colX + 1, bottom, ARROW_COLOR);
        }
    }

    static void drawSolidRectBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(RenderType.guiOverlay(), x, y, x + w, y + 1, color);
        graphics.fill(RenderType.guiOverlay(), x, y + h - 1, x + w, y + h, color);
        graphics.fill(RenderType.guiOverlay(), x, y, x + 1, y + h, color);
        graphics.fill(RenderType.guiOverlay(), x + w - 1, y, x + w, y + h, color);
    }

    static void drawDashedRectBorder(GuiGraphics graphics, int x, int y, int w, int h, int color, int dashPx, int gapPx) {
        int segment = dashPx + gapPx;
        for (int i = 0; i < w; i += segment) {
            int x1 = x + i, x2 = Math.min(x + i + dashPx, x + w);
            if (x2 > x1) {
                graphics.fill(RenderType.guiOverlay(), x1, y, x2, y + 1, color);
                graphics.fill(RenderType.guiOverlay(), x1, y + h - 1, x2, y + h, color);
            }
        }
        for (int i = 0; i < h; i += segment) {
            int y1 = y + i, y2 = Math.min(y + i + dashPx, y + h);
            if (y2 > y1) {
                graphics.fill(RenderType.guiOverlay(), x, y1, x + 1, y2, color);
                graphics.fill(RenderType.guiOverlay(), x + w - 1, y1, x + w, y2, color);
            }
        }
    }

    /** Draws a line whose thickness is centered on the (x1,y1)-(x2,y2) path, rather than offset down-right of it. */
    static void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int width, int color) {
        int dx = x2 - x1, dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;
        int half = width / 2;
        for (int i = 0; i <= steps; i++) {
            int px = x1 + dx * i / steps, py = y1 + dy * i / steps;
            graphics.fill(RenderType.guiOverlay(), px - half, py - half, px - half + width, py - half + width, color);
        }
    }

    static void drawOrthogonalConnection(GuiGraphics graphics, int x1, int y1, int x2, int y2, int width, int color) {
        if (x1 == x2 || y1 == y2) {
            drawLine(graphics, x1, y1, x2, y2, width, color);
            return;
        }
        drawLine(graphics, x1, y1, x1, y2, width, color);
        drawLine(graphics, x1, y2, x2, y2, width, color);
    }
}

/** Node graph data for one flowchart page. */
class Flowchart {
    final List<FlowNode> nodes = new ArrayList<>();
    final List<int[]> edges = new ArrayList<>(); // {fromNodeIndex, toNodeIndex}

    int hovered = -1;
    int selected = -1;

    void addEdge(int from, int to) {
        edges.add(new int[]{from, to});
    }

    static class FlowNode {
        enum Shape { CIRCLE, SQUARE }
        enum BorderStyle { FULL, DASHED }

        final List<SubIcon> subIcons;
        final String title;
        final String text;
        final int x, y;
        final int size = 20;
        final Shape shape;
        final BorderStyle borderStyle;
        final int borderColor = 0xFF652816 /*0xFF000000*/;
        final List<ResourceLocation> popupRecipes;

        FlowNode(List<SubIcon> subIcons, String title, String text, int x, int y,
                 Shape shape, BorderStyle borderStyle, List<ResourceLocation> popupRecipes) {
            this.subIcons = subIcons;
            this.title = title;
            this.text = text;
            this.x = x;
            this.y = y;
            this.shape = shape;
            this.borderStyle = borderStyle;
            this.popupRecipes = popupRecipes == null ? List.of() : popupRecipes;
        }

        boolean hit(int mx, int my) {
            int half = size / 2;
            return mx >= x - half && mx < x + half && my >= y - half && my < y + half;
        }
    }

    static class SubIcon {
        final List<ItemStack> icons;
        final List<BlockState> blockStates;
        final int offsetX, offsetY, size, intervalSeconds;
        final List<String> timeLabels;

        SubIcon(List<ItemStack> icons, List<BlockState> blockStates, int offsetX, int offsetY, int size, int intervalSeconds) {
            this(icons, blockStates, offsetX, offsetY, size, intervalSeconds, (List<String>) null);
        }

        SubIcon(List<ItemStack> icons, List<BlockState> blockStates, int offsetX, int offsetY, int size, int intervalSeconds, String timeLabel) {
            this(icons, blockStates, offsetX, offsetY, size, intervalSeconds, timeLabel == null ? null : List.of(timeLabel));
        }

        SubIcon(List<ItemStack> icons, List<BlockState> blockStates, int offsetX, int offsetY, int size, int intervalSeconds, List<String> timeLabels) {
            this.icons = icons;
            this.blockStates = blockStates;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.size = size;
            this.intervalSeconds = intervalSeconds;
            this.timeLabels = timeLabels;
        }

        private int currentCycleIndex() {
            int size = (icons != null && !icons.isEmpty()) ? icons.size() : (blockStates != null && !blockStates.isEmpty() ? blockStates.size() : 0);
            if (size <= 1) return 0;
            long epochSec = System.currentTimeMillis() / 1000L;
            return (int) ((epochSec / Math.max(1, intervalSeconds)) % size);
        }

        ItemStack getCurrentIcon() {
            if (icons == null || icons.isEmpty()) return ItemStack.EMPTY;
            if (icons.size() == 1) return icons.get(0);
            return icons.get(currentCycleIndex());
        }

        BlockState getCurrentBlockState() {
            if (blockStates == null || blockStates.isEmpty()) return null;
            if (blockStates.size() == 1) return blockStates.get(0);
            return blockStates.get(currentCycleIndex());
        }

        /** Constant if timeLabels has 1 entry, otherwise follows the same cycling index as the icon/blockState. */
        String getCurrentTimeLabel() {
            if (timeLabels == null || timeLabels.isEmpty()) return null;
            if (timeLabels.size() == 1) return timeLabels.get(0);
            int idx = currentCycleIndex();
            return idx < timeLabels.size() ? timeLabels.get(idx) : null;
        }
    }
}

/** GuidePage wrapping a Flowchart: node rendering, hover, click, and popup. */
class FlowchartPage implements GuidePage {
    private final String title;
    private final Flowchart flow;
    private Map<ResourceLocation, GuideRecipes.ResolvedRecipe> popupRecipes = Map.of();
    private final Map<String, LinkedText> popupTextCache = new HashMap<>();

    private static final int POPUP_PADDING = 6, POPUP_MAX_WIDTH = 140, RECIPE_SLOT_SIZE = 18, RECIPE_SPACING = 2;

    FlowchartPage(String title, Flowchart flow) {
        this.title = title;
        this.flow = flow;
    }

    @Override public String rawTitle() { return title; }

    @Override
    public void onShown(PageContext ctx) {
        Set<ResourceLocation> ids = new HashSet<>();
        for (Flowchart.FlowNode n : flow.nodes) ids.addAll(n.popupRecipes);
        popupRecipes = GuideRecipes.resolveAllWithFamilies(ctx.level(), ids);
    }

    @Override
    public int height(PageContext ctx) {
        int max = 0;
        for (Flowchart.FlowNode n : flow.nodes) max = Math.max(max, n.y + n.size / 2);
        return max + 4;
    }

    @Override
    public void render(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        int pageTextX = ctx.pageX(), pageTextY = ctx.pageY();
        int mx = mouseX - pageTextX, my = mouseY - pageTextY;

        for (int[] e : flow.edges) {
            Flowchart.FlowNode a = flow.nodes.get(e[0]);
            Flowchart.FlowNode b = flow.nodes.get(e[1]);
            GuideGeometry.drawOrthogonalConnection(graphics, pageTextX + a.x, pageTextY + a.y,
                    pageTextX + b.x, pageTextY + b.y, 2, 0xFFC83129 /*0xFF9FB9C6*/);
        }

        flow.hovered = -1;
        for (int i = 0; i < flow.nodes.size(); i++) {
            Flowchart.FlowNode n = flow.nodes.get(i);
            int centerX = pageTextX + n.x, centerY = pageTextY + n.y;
            int drawX = centerX - n.size / 2, drawY = centerY - n.size / 2;

            if (n.shape == Flowchart.FlowNode.Shape.CIRCLE)
                GuideGeometry.drawFilledCircle(graphics, drawX, drawY, n.size, 0xFFF6EBCB /*0xFFDDDDDD*/);
            else
                graphics.fill(RenderType.guiOverlay(), drawX, drawY, drawX + n.size, drawY + n.size, 0xFFF6EBCB /*0xFFDDDDDD*/);

            if (n.borderStyle == Flowchart.FlowNode.BorderStyle.FULL) {
                if (n.shape == Flowchart.FlowNode.Shape.CIRCLE)
                    GuideGeometry.drawCircleOutline(graphics, drawX, drawY, n.size, n.borderColor);
                else
                    GuideGeometry.drawSolidRectBorder(graphics, drawX, drawY, n.size, n.size, n.borderColor);
            } else {
                if (n.shape == Flowchart.FlowNode.Shape.CIRCLE)
                    GuideGeometry.drawDashedCircleOutline(graphics, drawX, drawY, n.size, n.borderColor, 4, 3);
                else
                    GuideGeometry.drawDashedRectBorder(graphics, drawX, drawY, n.size, n.size, n.borderColor, 4, 3);
            }

            for (Flowchart.SubIcon sub : n.subIcons) {
                int subX = centerX + sub.offsetX - sub.size / 2;
                int subY = centerY + sub.offsetY - sub.size / 2;
                ItemStack stack = sub.getCurrentIcon();
                BlockState bs = sub.getCurrentBlockState();
                int iconBoxSize = -1; // -1 = nothing drawn, skip the label
                if (!stack.isEmpty()) {
                    graphics.renderItem(stack, subX, subY);
                    graphics.renderItemDecorations(ctx.font(), stack, subX, subY);
                    iconBoxSize = 16; // renderItem always draws at a fixed 16x16, regardless of sub.size
                } else if (bs != null) {
                    GuideBlockScenes.renderBlockModel(graphics.pose(), ctx, bs, subX, subY, sub.size, 0.18, 0.25, 0.1); // to get to -0.32,-0.25,-0.4
                    iconBoxSize = sub.size;
                }
                String timeLabel = sub.getCurrentTimeLabel();
                if (iconBoxSize > 0 && timeLabel != null && !timeLabel.isEmpty()) {
                    drawTimeLabel(graphics, ctx.font(), timeLabel, subX, subY, iconBoxSize);
                }
            }

            if (n.hit(mx, my)) flow.hovered = i;
        }

        if (flow.hovered != -1) {
            Flowchart.FlowNode h = flow.nodes.get(flow.hovered);
            int cx = pageTextX + h.x, cy = pageTextY + h.y;
            int hlSize = h.size + 4;
            int hlX = cx - hlSize / 2, hlY = cy - hlSize / 2;
            if (h.shape == Flowchart.FlowNode.Shape.CIRCLE)
                GuideGeometry.drawFilledCircle(graphics, hlX, hlY, hlSize, 0x5588CCFF);
            else
                graphics.fill(RenderType.guiOverlay(), hlX, hlY, hlX + hlSize, hlY + hlSize, 0x5588CCFF);
        }
    }

    /**
     * Draws a small badge anchored at the icon's bottom-right corner.
     * If the stack also shows a real count there, the two will overlap.
     */
    private static void drawTimeLabel(GuiGraphics graphics, Font font, String label, int x, int y, int iconBoxSize) {
        float scale = 1f;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 10000f);
        int localRight = (int) ((x + iconBoxSize) / scale);
        int localBottom = y + iconBoxSize + 2;
        int width = font.width(label);
        graphics.drawString(font, label, localRight - width / 2, localBottom - font.lineHeight, 0xFFFFFFFF, true);
        graphics.pose().popPose();
    }

    private record PopupGeometry(int x, int y, int width, int height, int textX, int textY, LinkedText text) {}

    /** Shared by render and click so the two never drift apart. */
    private PopupGeometry computePopupGeometry(PageContext ctx, Flowchart.FlowNode node) {
        Font font = ctx.font();

        LinkedText linked = popupTextCache.computeIfAbsent(
                flow.selected + "#" + node.text.hashCode(),
                k -> GuideText.linked(font, node.text, POPUP_MAX_WIDTH - 2 * POPUP_PADDING));
        int textHeight = GuideText.height(font, linked.lines().size());
        int titleHeight = GuideText.lineAdvance(font);

        int recipesHeight = 0;
        for (ResourceLocation id : node.popupRecipes) {
            GuideRecipes.ResolvedRecipe r = popupRecipes.get(GuideRecipes.currentVariant(id));
            int h = r != null ? r.grid().rows() * RECIPE_SLOT_SIZE : RECIPE_SLOT_SIZE * 2;
            recipesHeight += h + RECIPE_SPACING;
        }
        if (recipesHeight > 0) recipesHeight -= RECIPE_SPACING;

        int popupW = POPUP_MAX_WIDTH;
        int popupH = titleHeight + 2 + textHeight + 2 * POPUP_PADDING + (recipesHeight > 0 ? recipesHeight + 8 : 0);

        int px = ctx.pageX() + node.x + node.size / 2 + 4;
        int py = ctx.pageY() + node.y - popupH / 2;
        py = Math.max(ctx.pageY(), Math.min(py, ctx.pageY() + ctx.pageHeight() - popupH));

        int textX = px + POPUP_PADDING;
        int textY = py + POPUP_PADDING + titleHeight + 2;
        return new PopupGeometry(px, py, popupW, popupH, textX, textY, linked);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, PageContext ctx) {
        if (flow.selected != -1) {
            PopupGeometry g = computePopupGeometry(ctx, flow.nodes.get(flow.selected));
            int target = GuideText.clickLinked(g.text(), ctx.font(), g.textX(), g.textY(), mouseX, mouseY);
            if (target >= 0) {
                flow.selected = -1; // close this page's popup before navigating away
                ctx.onNavigate().accept(target);
                return true;
            }
        }

        int mx = (int) mouseX - ctx.pageX(), my = (int) mouseY - ctx.pageY();
        for (int i = 0; i < flow.nodes.size(); i++) {
            if (flow.nodes.get(i).hit(mx, my)) {
                flow.selected = i;
                return true;
            }
        }
        flow.selected = -1;
        return false;
    }

    @Override
    public void renderPopup(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        if (flow.selected == -1) return;
        Flowchart.FlowNode s = flow.nodes.get(flow.selected);
        Font font = ctx.font();
        PopupGeometry g = computePopupGeometry(ctx, s);

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);
        graphics.fill(RenderType.guiOverlay(), g.x(), g.y(), g.x() + g.width(), g.y() + g.height(), 0xFF652816);
        graphics.fill(RenderType.guiOverlay(), g.x() + 1, g.y() + 1, g.x() + g.width() - 1, g.y() + g.height() - 1, 0xFFF9EED0);
        GuideText.drawLine(graphics, font, s.title, g.textX(), g.y() + POPUP_PADDING, 0xFF000000);

        int ly = g.textY();
        ly += GuideText.drawLinked(graphics, font, g.text(), g.textX(), ly, 0x333333, Integer.MAX_VALUE, mouseX, mouseY);

        int recipeY = ly + 6;
        for (ResourceLocation id : s.popupRecipes) {
            GuideRecipes.ResolvedRecipe r = popupRecipes.get(GuideRecipes.currentVariant(id));
            if (r == null) continue;
            int used = GuideRecipes.renderGrid(graphics, font, ctx.itemRenderer(), g.textX(), recipeY, RECIPE_SLOT_SIZE, r.grid());
            recipeY += used + RECIPE_SPACING;
        }
        graphics.pose().popPose();
    }
}