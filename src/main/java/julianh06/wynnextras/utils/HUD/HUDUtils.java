package julianh06.wynnextras.utils.HUD;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;

public class HUDUtils {

    protected DrawContext drawContext;
    protected double scaleFactor;
    protected int xStart;
    protected int yStart;
    protected int screenWidth;
    protected int screenHeight;

    public HUDUtils(DrawContext drawContext, double scaleFactor, int xStart, int yStart) {
        this.drawContext = drawContext;
        this.scaleFactor = scaleFactor;
        this.xStart = xStart;
        this.yStart = yStart;
    }
    public HUDUtils() {
    }

    public void updateContext(DrawContext ctx, double scaleFactor, int xStart, int yStart) {
        this.drawContext = ctx;
        this.scaleFactor = scaleFactor;
        this.xStart = xStart;
        this.yStart = yStart;
    }


    public void computeScaleAndOffsets() {
        MinecraftClient client = MinecraftClient.getInstance();
        Window w = client.getWindow();
        if (w == null) return;

        this.scaleFactor = Math.max(1.0, w.getScaleFactor());
        this.screenWidth = w.getScaledWidth();
        this.screenHeight = w.getScaledHeight();

        this.xStart = 0;
        this.yStart = 0;
    }

    // logical UI width used when laying out elements; override if you use different logical area
    public int getLogicalWidth() {
        // default: scaled screen width in logical units (inverse of ui transform)
        return (int) Math.round(screenWidth * scaleFactor);
    }

    // logical UI width used when laying out elements; override if you use different logical area
    public    int getLogicalHeight() {
        // default: scaled screen width in logical units (inverse of ui transform)
        return (int) Math.round(screenHeight * scaleFactor);
    }

    // --- Getter / Setter ---
    public double getScaleFactor() { return scaleFactor; }
    public float getScaleFactorF() { return (float) scaleFactor; }
    public void setScaleFactor(double scaleFactor) { this.scaleFactor = scaleFactor; }
    public int getXStart() { return xStart; }
    public int getYStart() { return yStart; }
    public void setOffset(int xStart, int yStart) { this.xStart = xStart; this.yStart = yStart; }

    // --- Coordinate transforms (logical -> screen pixels) ---
    public float sx(float logicalX) { return xStart + (float)(logicalX / scaleFactor); }
    public float sy(float logicalY) { return yStart + (float)(logicalY / scaleFactor); }
    public int sw(float logicalW) { return Math.max(0, (int)Math.round(logicalW / scaleFactor)); }
    public int sh(float logicalH) { return Math.max(0, (int)Math.round(logicalH / scaleFactor)); }

    public void drawText(String text, float x, float y, CustomColor color, HorizontalAlignment hAlign, VerticalAlignment vAlign, TextShadow shadow, float textScale) {
        FontRenderer.getInstance().renderText(
                drawContext.getMatrices(),
                StyledText.fromComponent(Text.of(text)),
                sx(x),
                sy(y),
                color,
                hAlign,
                vAlign,
                shadow,
                (float)(textScale / scaleFactor)
        );
    }

    public void drawText(String text, float x, float y, CustomColor color, HorizontalAlignment hAlign, VerticalAlignment vAlign, float textScale) {
        drawText(text, x, y, color, hAlign, vAlign, TextShadow.NORMAL, textScale);
    }

    public void drawText(String text, float x, float y, CustomColor color, float textScale) {
        drawText(text, x, y, color, HorizontalAlignment.LEFT, VerticalAlignment.TOP, TextShadow.NORMAL, textScale);
    }

    public void drawText(String text, float x, float y, CustomColor color) {
        drawText(text, x, y, color, HorizontalAlignment.LEFT, VerticalAlignment.TOP, TextShadow.NORMAL, 3f);
    }

    public void drawText(String text, float x, float y) {
        drawText(text, x, y, CustomColor.fromHexString("FFFFFF"), HorizontalAlignment.LEFT, VerticalAlignment.TOP, TextShadow.NORMAL, 3f);
    }

    public void drawCenteredText(String text, float x, float y, CustomColor color, float textScale) {
        drawText(text, x, y, color, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE, TextShadow.NORMAL, textScale);
    }

    public void drawCenteredText(String text, float x, float y, CustomColor color) {
        drawText(text, x, y, color, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE, TextShadow.NORMAL, 3f);
    }

    public void drawCenteredText(String text, float x, float y) {
        drawText(text, x, y, CustomColor.fromHexString("FFFFFF"), HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE, TextShadow.NORMAL, 3f);
    }




}
