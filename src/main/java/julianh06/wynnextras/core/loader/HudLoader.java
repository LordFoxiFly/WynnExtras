package julianh06.wynnextras.core.loader;

import julianh06.wynnextras.event.api.WEEventBus;
import julianh06.wynnextras.features.ability.AbilityCooldownOverlay;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import org.reflections.Reflections;

import java.util.Set;

public class HudLoader implements WELoader {

    public HudLoader(){
        Reflections reflections = new Reflections("julianh06.wynnextras");

        Set<Class<? extends WEHudElement>> loaderClasses = reflections.getSubTypesOf(WEHudElement.class);

        for (Class<? extends WEHudElement> clazz : loaderClasses) {
            try {
                WEHudElement loader = clazz.getDeclaredConstructor().newInstance();
                HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(loader.getIdentifiedLayer(), loader.getLayerIdentifier(), loader::render));
                WEEventBus.registerEventListener(loader);
            } catch (Exception e) {
                System.err.println("Failed to load WELoader: " + clazz.getName());
                e.printStackTrace();
            }
        }
    }

}
