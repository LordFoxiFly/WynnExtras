package julianh06.wynnextras.utils.HUD;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import julianh06.wynnextras.core.loader.WEHudElement;
import julianh06.wynnextras.utils.UI.UIUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

public abstract class WEHud implements WEHudElement {

    protected DrawContext drawContext;
    protected double scaleFactor;
    protected int xStart;
    protected int yStart;
    protected UIUtils ui;
    protected int screenWidth;
    protected int screenHeight;
    protected  Identifier identifiedLayer;
    protected  Identifier layerIdentifier;

    @Override
    public Identifier getIdentifiedLayer() {
        return identifiedLayer;
    }

    @Override
    public Identifier getLayerIdentifier() {
        return layerIdentifier;
    }

    /**
     * Constructor for WEHud.
     * @param identifiedLayer the layer things should be drawn on. Example: IdentifierLayer.MISC
     * @param layerIdentifier the indentifier for the callback registration.
     */
    public WEHud(Identifier identifiedLayer, Identifier layerIdentifier) {
        this.identifiedLayer = identifiedLayer;
        this.layerIdentifier = layerIdentifier;
        this.screenWidth = 0;
        this.screenHeight = 0;
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        this.drawContext = context;
        computeScaleAndOffsets();
        if (ui == null) ui = new UIUtils(context, scaleFactor, xStart, yStart);
        else ui.updateContext(context, scaleFactor, xStart, yStart);
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

        if (ui != null) ui.updateContext(drawContext, scaleFactor, xStart, yStart);
    }

    protected int getLogicalWidth() {
        // default: scaled screen width in logical units (inverse of ui transform)
        return (int) Math.round(screenWidth * scaleFactor);
    }

    // logical UI width used when laying out elements; override if you use different logical area
    protected int getLogicalHeight() {
        // default: scaled screen width in logical units (inverse of ui transform)
        return (int) Math.round(screenHeight * scaleFactor);
    }

    protected void drawText(String text, float x, float y, CustomColor color,
                            HorizontalAlignment horizontalAlignment,
                            VerticalAlignment verticalAlignment,
                            TextShadow shadow, float textScale) {
        if (ui == null) return;
        ui.drawText(text, x, y, color, horizontalAlignment, verticalAlignment, shadow, textScale);
    }

    protected void drawText(String text, float x, float y, CustomColor color,
                            HorizontalAlignment horizontalAlignment,
                            VerticalAlignment verticalAlignment, float textScale) {
        drawText(text, x, y, color, horizontalAlignment, verticalAlignment, TextShadow.NORMAL, textScale);
    }

    protected void drawText(String text, float x, float y, CustomColor color, float textScale) {
        drawText(text, x, y, color, HorizontalAlignment.LEFT, VerticalAlignment.TOP, TextShadow.NORMAL, textScale);
    }

    protected void drawText(String text, float x, float y, CustomColor color) {
        drawText(text, x, y, color, 3f);
    }

    protected void drawText(String text, float x, float y) {
        drawText(text, x, y, CustomColor.fromHexString("FFFFFF"));
    }

    protected void drawCenteredText(String text, float x, float y, CustomColor color, float textScale) {
        drawText(text, x, y, color, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE, TextShadow.NORMAL, textScale);
    }

    protected void drawCenteredText(String text, float x, float y, CustomColor color) {
        drawCenteredText(text, x, y, color, 3f);
    }

    protected void drawCenteredText(String text, float x, float y) {
        drawCenteredText(text, x, y, CustomColor.fromHexString("FFFFFF"));
    }

    protected void drawImage(net.minecraft.util.Identifier texture, float x, float y, float width, float height) {
        if (ui == null) return;
        ui.drawImage(texture, x, y, width, height);
    }

}
