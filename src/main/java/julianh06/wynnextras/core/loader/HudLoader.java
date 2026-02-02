package julianh06.wynnextras.core.loader;

import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.event.api.WEEventBus;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import org.reflections.Reflections;

import java.util.Set;

public class HudLoader implements WELoader {

    public HudLoader(){
        Reflections reflections = new Reflections("julianh06.wynnextras");

        Set<Class<? extends WEHudElement>> loaderClasses = reflections.getSubTypesOf(WEHudElement.class);
        //Set<Class<? extends WEHud>> loaderClassesTwo = reflections.getSubTypesOf(WEHud.class);

        for (Class<? extends WEHudElement> clazz : loaderClasses) {
            try {
                WEHudElement loader = clazz.getDeclaredConstructor().newInstance();
                HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(loader.getIdentifiedLayer(), loader.getLayerIdentifier(), loader::render));
                WEEventBus.registerEventListener(loader);
                WynnExtras.LOGGER.info("loaded Hud: " + clazz.getName());
            } catch (Exception e) {
                System.err.println("Failed to load WELoader: " + clazz.getName());
                e.printStackTrace();
            }
        }
//        for (Class<? extends WEHud> clazz : loaderClassesTwo) {
//            try {
//                WEHud loader = clazz.getDeclaredConstructor().newInstance();
//                HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(loader.getIdentifiedLayer(), loader.getLayerIdentifier(), loader::render));
//                WEEventBus.registerEventListener(loader);
//                WynnExtras.LOGGER.info("loaded Hud: " + clazz.getName());
//            } catch (Exception e) {
//                System.err.println("Failed to load WELoader: " + clazz.getName());
//                e.printStackTrace();
//            }
//        }
    }

}
