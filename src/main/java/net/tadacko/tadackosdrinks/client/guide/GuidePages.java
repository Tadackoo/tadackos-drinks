package net.tadacko.tadackosdrinks.client.guide;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// =====================================================================================
// Everything in this file is package-private: it's only ever used by GuideBookScreen
// and GuideBookContent, which live in the same package. No need to expose it further.
// =====================================================================================

/**
 * Shared text scale for all in-book prose - titles, body text, TOC, tables, popups, recipe arrows.
 * Everything routes through here so the whole book renders at one consistent scale.
 */
final class GuideText {
    static final float SCALE = 0.5f;

    private GuideText() {}

    /** Wraps text to a real-pixel width (converts to the larger logical width first). */
    static List<FormattedCharSequence> wrap(Font font, String text, int realWidth) {
        return TextWrapCache.split(font, text, (int) Math.ceil(realWidth / SCALE));
    }

    /** Real-pixel vertical space one line of scaled text takes, including the 2px logical line gap. */
    static int lineAdvance(Font font) {
        return Math.round((font.lineHeight + 2) * SCALE);
    }

    static int height(Font font, int lineCount) {
        return lineAdvance(font) * lineCount;
    }

    /** Draws a single line, scaled, anchored at (realX, realY). Returns the real height it took. */
    static int drawLine(GuiGraphics graphics, Font font, String text, int realX, int realY, int color) {
        graphics.pose().pushPose();
        graphics.pose().scale(SCALE, SCALE, 1f);
        graphics.drawString(font, Component.literal(text), (int)(realX / SCALE), (int)(realY / SCALE), color, false);
        graphics.pose().popPose();
        return lineAdvance(font);
    }

    /** Draws pre-wrapped lines starting at (realX, realY), stopping past maxRealY. Returns real height used. */
    static int draw(GuiGraphics graphics, Font font, List<FormattedCharSequence> lines, int realX, int realY, int color, int maxRealY) {
        graphics.pose().pushPose();
        graphics.pose().scale(SCALE, SCALE, 1f);
        int y = realY;
        int advance = lineAdvance(font);
        for (FormattedCharSequence line : lines) {
            if (y > maxRealY) break;
            graphics.drawString(font, line, realX / SCALE, y / SCALE, color, false);
            y += advance;
        }
        graphics.pose().popPose();
        return y - realY;
    }

    static int draw(GuiGraphics graphics, Font font, List<FormattedCharSequence> lines, int realX, int realY, int color) {
        return draw(graphics, font, lines, realX, realY, color, Integer.MAX_VALUE);
    }

    /** Wraps text (parsing {{label|page}} link markup) to a real-pixel width, with clickable spans resolved. */
    static LinkedText linked(Font font, String text, int realWidth) {
        if (text.isEmpty()) return LinkedText.EMPTY;
        List<FormattedCharSequence> lines = wrap(font, text, realWidth);
        return new LinkedText(lines, computeLinkSpans(font, lines));
    }

    private static List<LinkSpan> computeLinkSpans(Font font, List<FormattedCharSequence> lines) {
        List<LinkSpan> spans = new ArrayList<>();
        for (int li = 0; li < lines.size(); li++) {
            final int lineIndex = li;
            int[] x = {0};
            int[] runStart = {-1};
            int[] runTarget = {-1};
            lines.get(li).accept((index, style, codePoint) -> {
                int target = GuideLinks.targetPage(style);
                if (target != runTarget[0]) {
                    if (runTarget[0] != -1) spans.add(new LinkSpan(lineIndex, runStart[0], x[0], runTarget[0]));
                    runTarget[0] = target;
                    runStart[0] = x[0];
                }
                x[0] += font.width(String.valueOf(Character.toChars(codePoint)));
                return true;
            });
            if (runTarget[0] != -1) spans.add(new LinkSpan(lineIndex, runStart[0], x[0], runTarget[0]));
        }
        return spans;
    }

    /** Finds the link span (if any) under (mouseX, mouseY), for text drawn starting at (realX, realY). */
    private static LinkSpan linkAt(LinkedText text, Font font, int realX, int realY, double mouseX, double mouseY) {
        int advance = lineAdvance(font);
        int relY = (int) mouseY - realY;
        if (relY < 0 || advance <= 0) return null;
        int lineIndex = relY / advance;
        int logicalX = (int) Math.round((mouseX - realX) / SCALE);
        for (LinkSpan s : text.spans())
            if (s.line() == lineIndex && logicalX >= s.startX() && logicalX < s.endX()) return s;
        return null;
    }

    /** Draws pre-wrapped linked text, highlighting the hovered link. Returns real height used. */
    static int drawLinked(GuiGraphics graphics, Font font, LinkedText text, int realX, int realY, int color,
                          int maxRealY, double mouseX, double mouseY) {
        LinkSpan hovered = linkAt(text, font, realX, realY, mouseX, mouseY);
        if (hovered != null) {
            int advance = lineAdvance(font);
            int hx1 = realX + Math.round(hovered.startX() * SCALE);
            int hx2 = realX + Math.round(hovered.endX() * SCALE);
            int hy = realY + hovered.line() * advance;
            graphics.fill(RenderType.guiOverlay(), hx1, hy, hx2, hy + advance, 0x5588CCFF);
        }
        return draw(graphics, font, text.lines(), realX, realY, color, maxRealY);
    }

    /** Target page for a click at (mouseX, mouseY) against linked text drawn at (realX, realY), or -1. */
    static int clickLinked(LinkedText text, Font font, int realX, int realY, double mouseX, double mouseY) {
        LinkSpan s = linkAt(text, font, realX, realY, mouseX, mouseY);
        return s == null ? -1 : s.targetPage();
    }
}

/** A single unit of book content (text block, table, recipe, or a composite of these). */
interface GuidePage {
    /** First line drawn by the screen above the content area. Empty = no title line. */
    default String rawTitle() {
        return "";
    }

    /** Called once, the first time this page becomes visible. Use it to warm caches (text wrap, recipe lookups). */
    default void onShown(PageContext ctx) {
    }

    void render(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY);

    default boolean mouseClicked(double mouseX, double mouseY, int button, PageContext ctx) {
        return false;
    }

    /** Rendered last, after every page/button, so popups always draw on top. */
    default void renderPopup(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
    }

    /** Vertical pixels this page consumes - used by CompositePage to stack parts. */
    default int height(PageContext ctx) {
        return 0;
    }
}

/** Everything a GuidePage needs to render/click-test itself. pageY is the content start (below the title line). */
record PageContext(Font font, ItemRenderer itemRenderer, ClientLevel level, IntConsumer onNavigate,
                   int pageX, int pageY, int pageWidth, int pageHeight) {

    PageContext withY(int newY) {
        return new PageContext(font, itemRenderer, level, onNavigate, pageX, newY, pageWidth, pageHeight);
    }

    static PageContext of(int pageX, int pageY, int pageWidth, int pageHeight, IntConsumer onNavigate) {
        Minecraft mc = Minecraft.getInstance();
        return new PageContext(mc.font, mc.getItemRenderer(), mc.level, onNavigate, pageX, pageY, pageWidth, pageHeight);
    }
}

/**
 * Caches Font#split results. The original code called font.split(...) for every
 * page/table cell/popup on every single frame. Book text never changes at runtime,
 * so wrapping only needs to happen once per (text, width) pair.
 */
final class TextWrapCache {
    private static final Map<String, List<FormattedCharSequence>> CACHE = new HashMap<>();

    private TextWrapCache() {}

    static List<FormattedCharSequence> split(Font font, String text, int width) {
        return CACHE.computeIfAbsent(width + ":" + text, k -> font.split(GuideLinks.parse(text), width));
    }
}

/**
 * In-book hyperlinks: text wrapped in {@code {{label|pageIndex}}} renders underlined in the
 * link color, highlights on hover, and navigates to pageIndex on click. Parsing happens once
 * in TextWrapCache.split(), so anything that already goes through GuideText.wrap()/linked()
 * gets link support for free - no call-site changes needed elsewhere.
 */
final class GuideLinks {
    private static final Pattern PATTERN = Pattern.compile("\\{\\{([^{}|]+)\\|(\\d+)}}");
    private static final String CLICK_PREFIX = "guide_nav:";
    private static final int LINK_COLOR = 0xFF652816;

    private GuideLinks() {}

    /** Parses {@code {{label|pageIndex}}} markup into a Component; plain text if none is found. */
    static Component parse(String raw) {
        Matcher m = PATTERN.matcher(raw);
        if (!m.find()) return Component.literal(raw);

        MutableComponent result = Component.literal("");
        int last = 0;
        do {
            if (m.start() > last) result.append(Component.literal(raw.substring(last, m.start())));
            int targetPage = Integer.parseInt(m.group(2));
            result.append(Component.literal(m.group(1)).withStyle(linkStyle(targetPage)));
            last = m.end();
        } while (m.find());
        if (last < raw.length()) result.append(Component.literal(raw.substring(last)));
        return result;
    }

    private static Style linkStyle(int targetPage) {
        // RUN_COMMAND is never actually dispatched - it's just a carrier for the page index,
        // read back out via targetPage(Style). Navigation is handled entirely by our own hit-testing.
        return Style.EMPTY
                .withColor(TextColor.fromRgb(LINK_COLOR))
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, CLICK_PREFIX + targetPage));
    }

    /** -1 if this style isn't a guide link. */
    static int targetPage(Style style) {
        ClickEvent event = style.getClickEvent();
        if (event == null || event.getAction() != ClickEvent.Action.RUN_COMMAND) return -1;
        String value = event.getValue();
        if (!value.startsWith(CLICK_PREFIX)) return -1;
        try {
            return Integer.parseInt(value.substring(CLICK_PREFIX.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

/** One clickable run within a LinkedText's wrapped lines, in logical (pre-SCALE) pixel coordinates. */
record LinkSpan(int line, int startX, int endX, int targetPage) {}

/** Wrapped lines plus the clickable spans found within them. */
record LinkedText(List<FormattedCharSequence> lines, List<LinkSpan> spans) {
    static final LinkedText EMPTY = new LinkedText(List.of(), List.of());
}

/** Resolves recipe ids to renderable grids once (onShown), instead of hitting RecipeManager every frame. */
final class GuideRecipes {
    private GuideRecipes() {}

    record GridSpec(int cols, int rows, NonNullList<ItemStack> stacks) {}

    record ResolvedRecipe(GridSpec grid, ItemStack result) {}

    static Map<ResourceLocation, ResolvedRecipe> resolveAll(ClientLevel level, Collection<ResourceLocation> ids) {
        Map<ResourceLocation, ResolvedRecipe> out = new HashMap<>();
        if (level == null) return out;
        RecipeManager rm = level.getRecipeManager();
        for (ResourceLocation id : ids) {
            rm.byKey(id).ifPresent(r ->
                    out.put(id, new ResolvedRecipe(buildGrid(r), r.getResultItem(level.registryAccess()))));
        }
        return out;
    }

    // ---- wood-family cycling for popup recipes: registering e.g. "fermenting_barrel_oak" as a
    // representative lets any FlowNode.popupRecipes referencing it cycle through all wood types,
    // the same time-bucketed scheme Flowchart.SubIcon already uses for its item/blockstate icons. ----
    private static final Map<ResourceLocation, List<ResourceLocation>> FAMILIES = new HashMap<>();
    private static final int FAMILY_INTERVAL_SECONDS = 1;

    /** Registers `variants` (display order) as the cycling family for `representative`. */
    static void registerFamily(ResourceLocation representative, List<ResourceLocation> variants) {
        FAMILIES.put(representative, variants);
    }

    /** All ids resolveAll() needs for `id` - its full family if registered, else just itself. */
    private static List<ResourceLocation> familyVariants(ResourceLocation id) {
        return FAMILIES.getOrDefault(id, List.of(id));
    }

    /** The variant `id` currently displays as, based on wall-clock time. Non-family ids resolve to themselves. */
    static ResourceLocation currentVariant(ResourceLocation id) {
        List<ResourceLocation> variants = FAMILIES.get(id);
        if (variants == null || variants.size() <= 1) return id;
        long epochSec = System.currentTimeMillis() / 1000L;
        return variants.get((int) ((epochSec / FAMILY_INTERVAL_SECONDS) % variants.size()));
    }

    /** Like resolveAll(), but expands any family representative in `ids` to its full variant set first. */
    static Map<ResourceLocation, ResolvedRecipe> resolveAllWithFamilies(ClientLevel level, Collection<ResourceLocation> ids) {
        Set<ResourceLocation> expanded = new HashSet<>();
        for (ResourceLocation id : ids) expanded.addAll(familyVariants(id));
        return resolveAll(level, expanded);
    }

    private static GridSpec buildGrid(Recipe<?> r) {
        if (r instanceof ShapedRecipe shaped) {
            int w = shaped.getWidth(), h = shaped.getHeight();
            NonNullList<ItemStack> stacks = NonNullList.withSize(w * h, ItemStack.EMPTY);
            fill(stacks, shaped.getIngredients());
            return new GridSpec(w, h, stacks);
        } else if (r instanceof ShapelessRecipe shapeless) {
            int count = shapeless.getIngredients().size();
            int cols, rows;
            if (count <= 1) { cols = 1; rows = 1; }
            else if (count <= 4) { cols = 2; rows = 2; }
            else if (count <= 9) { cols = 3; rows = 3; }
            else { cols = 3; rows = (count + 2) / 3; }
            NonNullList<ItemStack> stacks = NonNullList.withSize(cols * rows, ItemStack.EMPTY);
            fill(stacks, shapeless.getIngredients());
            return new GridSpec(cols, rows, stacks);
        }
        return new GridSpec(0, 0, NonNullList.create());
    }

    private static void fill(NonNullList<ItemStack> stacks, List<Ingredient> ingredients) {
        for (int i = 0; i < ingredients.size() && i < stacks.size(); i++) {
            Ingredient ing = ingredients.get(i);
            if (ing == null || ing.isEmpty()) continue;
            ItemStack[] opts = ing.getItems();
            if (opts.length > 0) stacks.set(i, opts[0].copy());
        }
    }

    // Same slot graphic the vanilla crafting table GUI uses - crops straight out of its texture
    // so recipe grids look identical to the real crafting UI instead of a hand-drawn approximation.
    private static final ResourceLocation CRAFTING_TEX = new ResourceLocation("textures/gui/container/crafting_table.png");
    private static final int TEX_W = 256, TEX_H = 256;
    private static final int SLOT_U = 29, SLOT_V = 16;     // one 18x18 grid slot
    private static final int OUTPUT_SLOT_U = 119, OUTPUT_SLOT_V = 30; // the 18x18 result slot

    /** Draws the crafting grid at (startX,startY) using real slot art. Returns pixel height used. */
    static int renderGrid(GuiGraphics graphics, Font font, ItemRenderer itemRenderer, int startX, int startY, int slotSize, GridSpec spec) {
        // No gap between slots: vanilla's slot art tiles edge-to-edge. A 1px gap here would let
        // whatever's behind the grid show through as a seam between every cell.
        int padding = 0;

        // Batch all slot backgrounds first, then all items - avoids re-binding the slot texture
        // between every single item (itemRenderer swaps textures internally per item).
        for (int ry = 0; ry < spec.rows(); ry++) {
            for (int cx = 0; cx < spec.cols(); cx++) {
                int sx = startX + cx * (slotSize + padding);
                int sy = startY + ry * (slotSize + padding);
                graphics.blit(CRAFTING_TEX, sx, sy, SLOT_U, SLOT_V, slotSize, slotSize, TEX_W, TEX_H);
            }
        }

        for (int ry = 0; ry < spec.rows(); ry++) {
            for (int cx = 0; cx < spec.cols(); cx++) {
                int idx = ry * spec.cols() + cx;
                if (idx >= spec.stacks().size()) continue;
                ItemStack stack = spec.stacks().get(idx);
                if (stack.isEmpty()) continue;
                int sx = startX + cx * (slotSize + padding);
                int sy = startY + ry * (slotSize + padding);
                int iconX = sx + (slotSize - 16) / 2;
                int iconY = sy + (slotSize - 16) / 2;
                graphics.renderItem(stack, iconX, iconY);
                graphics.renderItemDecorations(font, stack, iconX, iconY, null);
            }
        }
        return spec.rows() * slotSize + Math.max(0, spec.rows() - 1) * padding;
    }

    private static final int OUTPUT_SLOT_SIZE = 26;

    /** Draws a single vanilla-styled output slot with the resulting item in it. */
    static void renderResultSlot(GuiGraphics graphics, Font font, ItemRenderer itemRenderer, int x, int y, ItemStack stack) {
        graphics.blit(CRAFTING_TEX, x, y, OUTPUT_SLOT_U, OUTPUT_SLOT_V, OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE, TEX_W, TEX_H);
        if (!stack.isEmpty()) {
            int iconX = x + (OUTPUT_SLOT_SIZE - 16) / 2, iconY = y + (OUTPUT_SLOT_SIZE - 16) / 2;
            graphics.renderItem(stack, iconX, iconY);
            graphics.renderItemDecorations(font, stack, iconX, iconY, null);
        }
    }
}

/** Generic column-fit table renderer. */
final class GuideTables {
    private GuideTables() {}

    private static final int PADDING = 2, CELL_HLINE = 1, GRID_COLOR = 0xFF652816 /*0xFFAAAAAA*/, TEXT_COLOR = 0x2b2b2b, MIN_COL_INNER_WIDTH = 12;

    /** Column count + pixel widths, fit to availWidth. Shared by measure() and render() so they can't drift apart. */
    private static int[] computeColumnWidths(Font font, int availWidth, List<List<String>> table, int cols) {
        int[] desiredInner = new int[cols];
        Arrays.fill(desiredInner, MIN_COL_INNER_WIDTH);
        for (List<String> row : table) {
            for (int c = 0; c < cols; c++) {
                String text = c < row.size() && row.get(c) != null ? row.get(c) : "";
                for (String line : text.split("\r?\n")) desiredInner[c] = Math.max(desiredInner[c], font.width(line));
            }
        }

        int[] desiredTotal = new int[cols];
        long sumDesired = 0;
        for (int c = 0; c < cols; c++) {
            desiredTotal[c] = desiredInner[c] + PADDING * 2;
            sumDesired += desiredTotal[c];
        }

        if (sumDesired <= availWidth) return desiredTotal;

        int[] colWidths = new int[cols];
        double scale = (double) availWidth / (double) sumDesired;
        for (int c = 0; c < cols; c++)
            colWidths[c] = Math.max(MIN_COL_INNER_WIDTH + PADDING * 2, (int) Math.floor(desiredTotal[c] * scale));
        return colWidths;
    }

    /** Computes the pixel height a table would take without drawing anything - used for page-height bookkeeping. */
    static int measure(Font font, int availWidth, List<List<String>> table) {
        if (table == null || table.isEmpty()) return 0;
        int cols = 0;
        for (List<String> row : table) cols = Math.max(cols, row.size());
        if (cols == 0) return 0;

        int[] colWidths = computeColumnWidths(font, availWidth, table, cols);
        int total = 0;
        for (List<String> row : table) {
            int rowHeight = 0;
            for (int c = 0; c < cols; c++) {
                String text = c < row.size() && row.get(c) != null ? row.get(c) : "";
                int inner = Math.max(4, colWidths[c] - PADDING * 2);
                List<FormattedCharSequence> wrapped = TextWrapCache.split(font, text, inner);
                rowHeight = Math.max(rowHeight, wrapped.size() * font.lineHeight + PADDING * 2);
            }
            total += rowHeight;
        }
        return total;
    }

    /** Renders a table fit to availWidth. Returns pixel height used. */
    static int render(GuiGraphics graphics, Font font, int x, int startY, int availWidth, List<List<String>> table) {
        if (table == null || table.isEmpty()) return 0;
        final int padding = PADDING, cellHLine = CELL_HLINE, gridColor = GRID_COLOR, textColor = TEXT_COLOR;

        int cols = 0;
        for (List<String> row : table) cols = Math.max(cols, row.size());
        if (cols == 0) return 0;

        int[] colWidths = computeColumnWidths(font, availWidth, table, cols);

        int tableWidth = 0;
        for (int w : colWidths) tableWidth += w;

        int y = startY;
        for (int r = 0; r < table.size(); r++) {
            List<String> row = table.get(r);
            int rowHeight = 0;
            List<List<FormattedCharSequence>> wrappedCells = new ArrayList<>(cols);
            for (int c = 0; c < cols; c++) {
                String text = c < row.size() && row.get(c) != null ? row.get(c) : "";
                int inner = Math.max(4, colWidths[c] - padding * 2);
                List<FormattedCharSequence> wrapped = TextWrapCache.split(font, text, inner);
                wrappedCells.add(wrapped);
                rowHeight = Math.max(rowHeight, wrapped.size() * font.lineHeight + padding * 2);
            }

            int xCol = x;
            for (int c = 0; c < cols; c++) {
                int w = colWidths[c];
                int textY = y + padding;
                for (FormattedCharSequence line : wrappedCells.get(c)) {
                    graphics.drawString(font, line, xCol + padding, textY, textColor, false);
                    textY += font.lineHeight;
                }
                if (c < cols - 1) graphics.fill(RenderType.guiOverlay(), xCol + w - cellHLine, y, xCol + w, y + rowHeight, gridColor);
                xCol += w;
            }

            if (r < table.size() - 1)
                graphics.fill(RenderType.guiOverlay(), x, y + rowHeight, x + tableWidth, y + rowHeight + cellHLine, gridColor);

            y += rowHeight;
        }
        return y - startY;
    }
}

/** Plain wrapped text page. */
class TextPage implements GuidePage {
    private final String title;
    private final String body;
    private LinkedText linked;

    TextPage(String title, String body) {
        this.title = title;
        this.body = body;
    }

    @Override public String rawTitle() { return title; }

    @Override
    public void onShown(PageContext ctx) {
        linked = GuideText.linked(ctx.font(), body, ctx.pageWidth());
    }

    @Override
    public void render(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        if (linked == null) onShown(ctx);
        GuideText.drawLinked(graphics, ctx.font(), linked, ctx.pageX(), ctx.pageY(), 0x2b2b2b,
                ctx.pageY() + ctx.pageHeight(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, PageContext ctx) {
        if (linked == null) return false;
        int target = GuideText.clickLinked(linked, ctx.font(), ctx.pageX(), ctx.pageY(), mouseX, mouseY);
        if (target < 0) return false;
        ctx.onNavigate().accept(target);
        return true;
    }

    @Override
    public int height(PageContext ctx) {
        if (linked == null) onShown(ctx);
        return GuideText.height(ctx.font(), linked.lines().size());
    }
}

/** Table of contents page with clickable entries. */
class TocPage implements GuidePage {
    record Entry(String title, int targetPage) {}

    private final String title;
    private final List<Entry> entries;
    private final IntConsumer onNavigate;
    private List<int[]> rects; // {y, height} per entry - x/width are constant (ctx.pageX()/pageWidth())
    private List<List<FormattedCharSequence>> lines;

    TocPage(String title, List<Entry> entries, IntConsumer onNavigate) {
        this.title = title;
        this.entries = entries;
        this.onNavigate = onNavigate;
    }

    @Override public String rawTitle() { return title; }

    @Override
    public void onShown(PageContext ctx) {
        rects = new ArrayList<>();
        lines = new ArrayList<>();
        int y = ctx.pageY();
        for (Entry e : entries) {
            List<FormattedCharSequence> wrapped = GuideText.wrap(ctx.font(), e.title(), ctx.pageWidth());
            int h = GuideText.height(ctx.font(), wrapped.size());
            lines.add(wrapped);
            rects.add(new int[]{y, h});
            y += h + 4;
        }
    }

    @Override
    public void render(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        if (rects == null) onShown(ctx);
        for (int i = 0; i < entries.size(); i++) {
            int[] r = rects.get(i);
            boolean hovered = mouseX >= ctx.pageX() && mouseX < ctx.pageX() + ctx.pageWidth()
                    && mouseY >= r[0] && mouseY < r[0] + r[1];
            if (hovered)
                graphics.fill(RenderType.guiOverlay(), ctx.pageX() - 2, r[0] - 1, ctx.pageX() + ctx.pageWidth() + 2,
                        r[0] + r[1] + 1, 0x5588CCFF /*0x88CCCCCC*/);
            GuideText.draw(graphics, ctx.font(), lines.get(i), ctx.pageX(), r[0], 0xFF000000);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, PageContext ctx) {
        if (rects == null) return false;
        for (int i = 0; i < entries.size(); i++) {
            int[] r = rects.get(i);
            if (mouseX >= ctx.pageX() && mouseX < ctx.pageX() + ctx.pageWidth() && mouseY >= r[0] && mouseY < r[0] + r[1]) {
                onNavigate.accept(entries.get(i).targetPage());
                return true;
            }
        }
        return false;
    }
}

/** Combines several GuidePages into one page, stacked top to bottom (e.g. text + recipe). Draws each part's own title, if it has one. */
class CompositePage implements GuidePage {
    private final String title;
    private final int gap;
    private final List<GuidePage> parts;

    CompositePage(String title, int gap, GuidePage... parts) {
        this.title = title;
        this.gap = gap;
        this.parts = List.of(parts);
    }

    @Override public String rawTitle() { return title; }

    /** Height a part's own title line takes, or 0 if it doesn't have one. Single line, like the screen's page title. */
    private static int titleHeight(GuidePage part, PageContext ctx) {
        return part.rawTitle().isEmpty() ? 0 : GuideText.lineAdvance(ctx.font());
    }

    @Override
    public void onShown(PageContext ctx) {
        int y = ctx.pageY();
        for (GuidePage p : parts) {
            y += titleHeight(p, ctx);
            PageContext sub = ctx.withY(y);
            p.onShown(sub);
            y += p.height(sub) + gap;
        }
    }

    @Override
    public void render(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        int y = ctx.pageY();
        for (GuidePage p : parts) {
            if (!p.rawTitle().isEmpty()) {
                y += GuideText.drawLine(graphics, ctx.font(), p.rawTitle(), ctx.pageX(), y, 0xFF000000);
            }
            PageContext sub = ctx.withY(y);
            p.render(graphics, sub, mouseX, mouseY);
            y += p.height(sub) + gap;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, PageContext ctx) {
        int y = ctx.pageY();
        for (GuidePage p : parts) {
            y += titleHeight(p, ctx);
            PageContext sub = ctx.withY(y);
            if (p.mouseClicked(mouseX, mouseY, button, sub)) return true;
            y += p.height(sub) + gap;
        }
        return false;
    }

    @Override
    public void renderPopup(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        int y = ctx.pageY();
        for (GuidePage p : parts) {
            y += titleHeight(p, ctx);
            PageContext sub = ctx.withY(y);
            p.renderPopup(graphics, sub, mouseX, mouseY);
            y += p.height(sub) + gap;
        }
    }

    @Override
    public int height(PageContext ctx) {
        int total = 0;
        for (int i = 0; i < parts.size(); i++) {
            GuidePage p = parts.get(i);
            int th = titleHeight(p, ctx);
            PageContext sub = ctx.withY(ctx.pageY() + total + th);
            total += th + p.height(sub);
            if (i < parts.size() - 1) total += gap;
        }
        return total;
    }
}

/**
 * One visual slot in a RecipeListPage. A single id renders statically; multiple ids cycle
 * over time (same scheme as Flowchart.SubIcon's item/blockstate cycling).
 */
record RecipeSlot(List<ResourceLocation> ids, int intervalSeconds) {

    static RecipeSlot of(ResourceLocation id) {
        return new RecipeSlot(List.of(id), 0);
    }

    static RecipeSlot cycling(int intervalSeconds, ResourceLocation... ids) {
        return new RecipeSlot(List.of(ids), intervalSeconds);
    }

    ResourceLocation current() {
        if (ids.size() == 1) return ids.get(0);
        long epochSec = System.currentTimeMillis() / 1000L;
        return ids.get((int) ((epochSec / Math.max(1, intervalSeconds)) % ids.size()));
    }
}

/** Page rendering one or more recipes with a result arrow, resolved once via GuideRecipes. */
class RecipeListPage implements GuidePage {
    private final List<RecipeSlot> slots;
    private Map<ResourceLocation, GuideRecipes.ResolvedRecipe> resolved = Map.of();
    // Max row count per slot across its cycling ids, fixed at onShown() so the page layout
    // (computed once by CompositePage) doesn't shift as the cycling variant changes over time.
    private int[] slotRowsMax;

    RecipeListPage(List<ResourceLocation> recipeIds) {
        List<RecipeSlot> s = new ArrayList<>(recipeIds.size());
        for (ResourceLocation id : recipeIds) s.add(RecipeSlot.of(id));
        this.slots = s;
    }

    RecipeListPage(RecipeSlot... slots) {
        this.slots = List.of(slots);
    }

    @Override
    public void onShown(PageContext ctx) {
        Set<ResourceLocation> allIds = new HashSet<>();
        for (RecipeSlot slot : slots) allIds.addAll(slot.ids());
        resolved = GuideRecipes.resolveAll(ctx.level(), allIds);

        slotRowsMax = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            int max = 0;
            for (ResourceLocation id : slots.get(i).ids()) {
                GuideRecipes.ResolvedRecipe r = resolved.get(id);
                if (r != null) max = Math.max(max, r.grid().rows());
            }
            slotRowsMax[i] = max;
        }
    }

    @Override
    public void render(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        if (slotRowsMax == null) onShown(ctx);
        int y = ctx.pageY();
        for (int i = 0; i < slots.size(); i++) {
            int reservedH = slotRowsMax[i] * 18 + 2;
            GuideRecipes.ResolvedRecipe r = resolved.get(slots.get(i).current());
            if (r == null || r.grid().cols() == 0) {
                y += reservedH;
                continue;
            }
            int gridH = GuideRecipes.renderGrid(graphics, ctx.font(), ctx.itemRenderer(), ctx.pageX(), y, 18, r.grid());
            int gridW = r.grid().cols() * 18;
            int gap = 4;
            int resultX = ctx.pageX() + gridW + gap;
            int arrowY = y + gridH / 2 - GuideGeometry.ARROW_HEIGHT / 2;
            GuideGeometry.drawArrowRight(graphics, resultX, arrowY);
            GuideRecipes.renderResultSlot(graphics, ctx.font(), ctx.itemRenderer(), resultX + GuideGeometry.ARROW_WIDTH + gap, y + gridH / 2 - 13,
                    r.result());
            y += reservedH; // advance by reserved height, not actual gridH, to stay stable across cycling
        }
    }

    @Override
    public int height(PageContext ctx) {
        if (slotRowsMax == null) onShown(ctx);
        int total = 0;
        for (int rows : slotRowsMax) total += rows * 18 + 2;
        return total;
    }
}

/** Optional lead-in text + a table + optional footnote, all at GuideText.SCALE. */
class TablePage implements GuidePage {
    private final String title;
    private final String bodyText;   // optional lead-in text above the table, "" if none
    private final List<List<String>> table;
    private final String footnote;   // optional text below the table, "" if none

    private List<FormattedCharSequence> wrappedBody;
    private int bodyHeight;
    private int tableHeight;    // real pixels (already multiplied back up by scale)
    private List<FormattedCharSequence> footnoteLines;
    private int footnoteHeight; // real pixels, includes the +3 gap before it; 0 if no footnote

    TablePage(String title, String bodyText, List<List<String>> table, String footnote) {
        this.title = title;
        this.bodyText = bodyText;
        this.table = table;
        this.footnote = footnote;
    }

    TablePage(String title, List<List<String>> table) {
        this(title, "", table, "");
    }

    @Override public String rawTitle() { return title; }

    @Override
    public void onShown(PageContext ctx) {
        Font font = ctx.font();
        float scale = GuideText.SCALE;

        if (!bodyText.isEmpty()) {
            wrappedBody = GuideText.wrap(font, bodyText, ctx.pageWidth());
            bodyHeight = GuideText.height(font, wrappedBody.size());
        } else {
            wrappedBody = List.of();
            bodyHeight = 0;
        }

        int scaledWidth = (int) (ctx.pageWidth() / scale);
        tableHeight = Math.round(GuideTables.measure(font, scaledWidth, table) * scale);

        if (!footnote.isEmpty()) {
            footnoteLines = TextWrapCache.split(font, footnote, scaledWidth);
            int measured = footnoteLines.size() * (font.lineHeight + 2) + 3; // +3 matches the gap used in render()
            footnoteHeight = Math.round(measured * scale);
        } else {
            footnoteLines = List.of();
            footnoteHeight = 0;
        }
    }

    @Override
    public void render(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        if (wrappedBody == null) onShown(ctx);
        Font font = ctx.font();
        float scale = GuideText.SCALE;

        int y = ctx.pageY();
        y += GuideText.draw(graphics, font, wrappedBody, ctx.pageX(), y, 0x2b2b2b) + 1;

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1f);
        int usedTable = GuideTables.render(graphics, font,
                (int) (ctx.pageX() / scale), (int) (y / scale), (int) (ctx.pageWidth() / scale), table);

        if (!footnoteLines.isEmpty()) {
            int fx = (int) (ctx.pageX() / scale);
            int fy = (int) (y / scale) + usedTable + 4;
            int fyStep = font.lineHeight + 2;
            for (FormattedCharSequence line : footnoteLines) {
                graphics.drawString(font, line, fx, fy, 0x2b2b2b, false);
                fy += fyStep;
            }
        }
        graphics.pose().popPose();
    }

    @Override
    public int height(PageContext ctx) {
        if (wrappedBody == null) onShown(ctx);
        return bodyHeight + tableHeight + footnoteHeight;
    }
}

/**
 * One background decoration on a page: an ItemStack, a BlockState, or a MobEffect icon,
 * offset from the page's center, drawn oversized and translucent.
 *
 * NOTE on alpha: item and effect icons respect `alpha` reliably (both are plain textured
 * quads driven by the shader's ColorModulator). Block-model icons do NOT reliably respect
 * alpha - most block render types bake vertex alpha to 1.0 and ignore ColorModulator, so a
 * "translucent" block background may render fully opaque depending on the block's render type.
 */
record BackgroundIcon(ItemStack item, BlockState blockState, MobEffect effect,
                      int offsetX, int offsetY, int size, float alpha) {

    static BackgroundIcon item(ItemStack item, int offsetX, int offsetY, int size, float alpha) {
        return new BackgroundIcon(item, null, null, offsetX, offsetY, size, alpha);
    }

    static BackgroundIcon block(BlockState state, int offsetX, int offsetY, int size, float alpha) {
        return new BackgroundIcon(ItemStack.EMPTY, state, null, offsetX, offsetY, size, alpha);
    }

    static BackgroundIcon effect(MobEffect effect, int offsetX, int offsetY, int size, float alpha) {
        return new BackgroundIcon(ItemStack.EMPTY, null, effect, offsetX, offsetY, size, alpha);
    }
}

/** Zero or more BackgroundIcons to draw behind a page's content. */
record PageBackground(List<BackgroundIcon> icons) {

    static PageBackground of(BackgroundIcon... icons) {
        return new PageBackground(List.of(icons));
    }
}

/**
 * Wraps any GuidePage, drawing a PageBackground behind it and forwarding everything else
 * unchanged. Lets any page opt into a background icon without touching its own class.
 */
final class BackgroundDecoratedPage implements GuidePage {
    private final GuidePage inner;
    private final PageBackground background;

    private BackgroundDecoratedPage(GuidePage inner, PageBackground background) {
        this.inner = inner;
        this.background = background;
    }

    /** Wraps `page` with `background`, or returns `page` unchanged if background is null/empty. */
    static GuidePage wrap(GuidePage page, PageBackground background) {
        if (background == null || background.icons().isEmpty()) return page;
        return new BackgroundDecoratedPage(page, background);
    }

    @Override public String rawTitle() { return inner.rawTitle(); }
    @Override public void onShown(PageContext ctx) { inner.onShown(ctx); }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button, PageContext ctx) {
        return inner.mouseClicked(mouseX, mouseY, button, ctx);
    }
    @Override public void renderPopup(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) { inner.renderPopup(graphics, ctx, mouseX, mouseY); }
    @Override public int height(PageContext ctx) { return inner.height(ctx); }

    @Override
    public void render(GuiGraphics graphics, PageContext ctx, int mouseX, int mouseY) {
        int centerX = ctx.pageX() + ctx.pageWidth() / 2;
        int centerY = ctx.pageY() + ctx.pageHeight() / 2;
        for (BackgroundIcon icon : background.icons()) {
            int cx = centerX + icon.offsetX(), cy = centerY + icon.offsetY();
            if (icon.effect() != null) {
                renderEffect(graphics, icon, cx, cy);
            } else if (icon.blockState() != null) {
                renderBlock(graphics.pose(), ctx, icon, cx, cy);
            } else if (!icon.item().isEmpty()) {
                renderItem(graphics, ctx, icon, cx, cy);
            }
        }
        inner.render(graphics, ctx, mouseX, mouseY);
    }

    // Effect icons come from the same 18x18 atlas the inventory HUD uses - no item needed.
    private static void renderEffect(GuiGraphics graphics, BackgroundIcon icon, int centerX, int centerY) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(icon.effect());
        float scale = icon.size() / 18f; // sprite is natively 18x18
        graphics.pose().pushPose();
        graphics.pose().translate(centerX - icon.size() / 2f, centerY - icon.size() / 2f, -500);
        graphics.pose().scale(scale, scale, 1f);
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, icon.alpha());
        graphics.blit(0, 0, 0, 18, 18, sprite);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        graphics.pose().popPose();
    }

    // Flat sprite blit, same technique as renderEffect - bypasses the item's own model render
    // type entirely, so translucency works consistently regardless of whether the item is a
    // plain Item (translucent/cutout render type) or a BlockItem on a solid block (which would
    // otherwise force-disable blending and ignore our alpha).
    private static void renderItem(GuiGraphics graphics, PageContext ctx, BackgroundIcon icon, int centerX, int centerY) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getItemRenderer().getModel(icon.item(), ctx.level(), null, 0);
        TextureAtlasSprite sprite = model.getParticleIcon();

        graphics.pose().pushPose();
        graphics.pose().translate(centerX - icon.size() / 2f, centerY - icon.size() / 2f, -500);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, icon.alpha());
        graphics.blit(0, 0, 0, icon.size(), icon.size(), sprite);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        graphics.pose().popPose();
    }

    private static void renderBlock(PoseStack pose, PageContext ctx, BackgroundIcon icon, int centerX, int centerY) {
        int size = icon.size();
        int textureId = BlockIconCache.get(ctx, icon.blockState(), size);
        if (textureId < 0) return; // reflection hook unavailable - skip rather than draw garbage

        int x = centerX - size / 2, y = centerY - size / 2;

        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, icon.alpha());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        Matrix4f mat = pose.last().pose();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(mat, x, y + size, 0).uv(0, 0).endVertex();
        buf.vertex(mat, x + size, y + size, 0).uv(1, 0).endVertex();
        buf.vertex(mat, x + size, y, 0).uv(1, 1).endVertex();
        buf.vertex(mat, x, y, 0).uv(0, 1).endVertex();
        tess.end();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }
}