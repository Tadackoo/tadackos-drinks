package net.tadacko.tadackosdrinks.client.guide;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.tadacko.tadackosdrinks.TadackosDrinks;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuideBookScreen extends Screen {
    private static final ResourceLocation BOOK_TEX = new ResourceLocation(TadackosDrinks.MOD_ID, "textures/gui/book_gui.png");
    private static final ResourceLocation VANILLA_BOOK_TEX = new ResourceLocation("textures/gui/book.png");

    private final List<GuidePage> pages;
    private final boolean[] shown;
    private int spreadIndex = 0;

    private final int guiWidth = 282, guiHeight = 180;
    private final int pageWidth = 110, pageHeight = 140;
    private final int pageLeftOffset = 6, pageTopOffset = 15;

    private Button prevButton, nextButton, closeButton;

    public GuideBookScreen() {
        super(Component.translatable("tadackosdrinks.guide.title"));
        this.pages = GuideBookContent.buildPages(this::navigateTo);
        this.shown = new boolean[pages.size()];
    }

    private void navigateTo(int pageIndex) {
        if (pageIndex >= 0 && pageIndex < pages.size()) {
            spreadIndex = pageIndex / 2;
            updateButtons();
        }
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;
        final int btnW = 23, btnH = 13;
        final int leftU = 0, leftUHover = 23, leftV = 205;
        final int rightU = 0, rightUHover = 23, rightV = 192;

        prevButton = this.addRenderableWidget(new ImageButton(
                x + 24, y + guiHeight - 26, btnW, btnH, leftU, leftV, btnH, VANILLA_BOOK_TEX, 256, 256,
                b -> {
                    if (spreadIndex > 0) {
                        spreadIndex--;
                        updateButtons();
                    }
                }) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                boolean hovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
                graphics.blit(VANILLA_BOOK_TEX, getX(), getY(), hovered ? leftUHover : leftU, leftV, getWidth(), getHeight(), 256, 256);
            }
        });

        nextButton = this.addRenderableWidget(new ImageButton(
                x + guiWidth - 25 - btnW, y + guiHeight - 26, btnW, btnH, rightU, rightV, btnH, VANILLA_BOOK_TEX, 256, 256,
                b -> {
                    if ((spreadIndex + 1) * 2 < pages.size()) {
                        spreadIndex++;
                        updateButtons();
                    }
                }) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                boolean hovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
                graphics.blit(VANILLA_BOOK_TEX, getX(), getY(), hovered ? rightUHover : rightU, rightV, getWidth(), getHeight(), 256, 256);
            }
        });

        closeButton = this.addRenderableWidget(Button.builder(
                Component.translatable("tadackosdrinks.guide.close"),
                b -> onClose()).pos(x + guiWidth / 2 - 50, y + guiHeight + 5).width(100).build());

        updateButtons();
    }

    private void updateButtons() {
        if (prevButton != null) {
            boolean show = spreadIndex > 0;
            prevButton.active = show;
            prevButton.visible = show;
        }
        if (nextButton != null) {
            boolean show = (spreadIndex + 1) * 2 < pages.size();
            nextButton.active = show;
            nextButton.visible = show;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int[] pageOrigin(boolean left) {
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;
        int middleX = x + guiWidth / 2;
        int innerGutter = 8;
        int pageX = left ? middleX - innerGutter - pageLeftOffset - pageWidth : middleX + innerGutter + pageLeftOffset;
        return new int[]{pageX, y + pageTopOffset};
    }

    private PageContext contentContext(int[] origin) {
        int contentY = origin[1] + GuideText.lineAdvance(font) + 4;
        return PageContext.of(origin[0], contentY, pageWidth, pageHeight, this::navigateTo);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = false;
        for (boolean left : new boolean[]{true, false}) {
            int pageIndex = spreadIndex * 2 + (left ? 0 : 1);
            if (pageIndex < 0 || pageIndex >= pages.size()) continue;
            PageContext ctx = contentContext(pageOrigin(left));
            // Don't short-circuit: both pages must see every click so an unrelated click on one
            // page still closes an open popup on the other (each page's Flowchart owns its own
            // `selected` state, so only that page's own mouseClicked call can clear it).
            if (pages.get(pageIndex).mouseClicked(mouseX, mouseY, button, ctx)) handled = true;
        }
        if (handled) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;
        graphics.blit(BOOK_TEX, x, y, 0, 0, guiWidth, guiHeight, guiWidth, guiHeight);

        for (boolean left : new boolean[]{true, false}) {
            int pageIndex = spreadIndex * 2 + (left ? 0 : 1);
            if (pageIndex < 0 || pageIndex >= pages.size()) continue;
            GuidePage page = pages.get(pageIndex);
            int[] origin = pageOrigin(left);
            PageContext ctx = contentContext(origin);

            if (!shown[pageIndex]) {
                page.onShown(ctx);
                shown[pageIndex] = true;
            }

            if (!page.rawTitle().isEmpty()) GuideText.drawLine(graphics, font, page.rawTitle(), origin[0], origin[1], 0xFF000000);
            page.render(graphics, ctx, mouseX, mouseY);

            String pageNum = (pageIndex + 1) + "/" + pages.size();
            int boxStartX = left ? x + 16 : x + guiWidth / 2 + 8;
            int boxWidth = guiWidth / 2 - 24;
            int centerX = boxStartX + boxWidth / 2;
            int textWidth = Math.round(font.width(pageNum) * GuideText.SCALE);
            GuideText.drawLine(graphics, font, pageNum, centerX - textWidth / 2, y + guiHeight - 20, 0xFF8B8B8B);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);

        // popups drawn last so they overlay pages, tables and widgets
        for (boolean left : new boolean[]{true, false}) {
            int pageIndex = spreadIndex * 2 + (left ? 0 : 1);
            if (pageIndex < 0 || pageIndex >= pages.size()) continue;
            pages.get(pageIndex).renderPopup(graphics, contentContext(pageOrigin(left)), mouseX, mouseY);
        }
    }
}