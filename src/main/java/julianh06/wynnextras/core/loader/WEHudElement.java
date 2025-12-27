package julianh06.wynnextras.core.loader;

import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public interface WEHudElement {

    /**
     * In 1.21.4 drawing to the Hud needs a Identifier that it can be registered to the HudLayerRegistrationCallback
     * Example: Identifier.of("wynnextras", "ability-cooldown-layer");
     * @return Identifier of the Hud drawing Layer.
     */
    Identifier getLayerIdentifier();


    /**
     * The Layer the things should be drawn on.
     * Example: IdentifiedLayer.MISC_OVERLAYS for normal HUD
     * @return
     */
    Identifier getIdentifiedLayer();

    void render(DrawContext context, RenderTickCounter tickCounter);


}
