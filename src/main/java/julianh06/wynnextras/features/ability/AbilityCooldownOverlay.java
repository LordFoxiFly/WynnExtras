package julianh06.wynnextras.features.ability;

import com.wynntils.core.components.Models;
import com.wynntils.models.statuseffects.type.StatusEffect;
import julianh06.wynnextras.annotations.WEModule;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.event.TickEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.ai.brain.task.BreezeMovementUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WEModule
public class AbilityCooldownOverlay {
    public static final Identifier ABILITYCOOLDOWN_LAYER = Identifier.of("wynnextras", "hud-example-layer");

    private static final Identifier COOLDOWN_IDENTIFIER = Identifier.of("wynnextras",  "textures/general/hud/abilities/cooldown.png");
    private static List<Identifier> renderCache = new ArrayList<>();

    private static List<AbilityCooldown> renderCacheTwo = new ArrayList<>();

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    private int currentTick = 0;

    private static Identifier tempIdentifier = Identifier.of("wynnextras", "textures/general/hud/abilities/holytrumpets.png");


    public static void register(){
        WynnExtras.LOGGER.info("Ability CooldownOverlay registerd");

        ClientTickEvents.START_CLIENT_TICK.register((tick) -> {

        });
    }
    @SubscribeEvent
    public void onTick(TickEvent event) {
        //This entire code could be a bit more optimized if done with StatusEffectChangedEvent from Wynntils.
        //But when ability is on cooldown we update the icon every 1s anyway and the code that gets executed is not that heavy, that it would probaly make an impact if you are not playing on very old Hardware.


//        WynnExtras.LOGGER.info("Tick");
//        this.currentTick++;
//        if (this.currentTick == 20){
//            this.currentTick = 0;
//            WynnExtras.LOGGER.info("Current Event Tick: "+event.ticks);
//            recalculateRenderCache();
//        }

        if(event.ticks % 20 == 0){

            recalculateRenderCache();
        }
    }




    private void recalculateRenderCache() {
        List<StatusEffect> effects = Models.StatusEffect.getStatusEffects();
        //renderCache.clear();

        //Using this isActive for better display if it should lag
        renderCacheTwo.forEach(abilityCooldown -> abilityCooldown.setActive(false));
        if (effects.isEmpty()){
            //Sometimes happens that the renderCache doesnt get cleared after the effects are over.
            if (!renderCacheTwo.isEmpty()) renderCacheTwo.clear();
            return;
        }

        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

        //String effectsAsString = "";
        for (StatusEffect effect : effects){
            //effectsAsString +=  effect.getName().getString()  + "[" + effect.getDuration() +  "]"+ ",";
            Identifier effectIdentifier = getAbilitiyCooldownIdentifier(effect);

            //Checks if there is a png for the effect or if the prefix is the Cooldown prefix.
            if (resourceManager.getResource(effectIdentifier).isEmpty() || !effect.getPrefix().getString().equals("§8⬤")) continue;

            //If the Rendercache doesnt have the cooldown in it it will add it
            AbilityCooldown effectCooldown = nameToAbilityCooldown(effect.getName().getString());
            if (effectCooldown == null){
                effectCooldown = new AbilityCooldown(effect.getName().getString(), effect.getDuration(), effectIdentifier);
                renderCacheTwo.add(effectCooldown);
            }
            effectCooldown.updateTime();
            effectCooldown.setActive(true);
            //renderCache.add(effectIdentifier);


        }
        //Removes all nonActive
        //            abilityCooldown.currentDuration = effect.getDuration();
        //            WynnExtras.LOGGER.info("AbilityCD: "+ abilityCooldown.name + " | " + abilityCooldown.currentDuration);
        //            if (abilityCooldown.currentDuration <= 0) renderCacheTwo.remove(abilityCooldown);
        renderCacheTwo.removeIf(abilityCooldown -> !abilityCooldown.isActive);
        WynnExtras.LOGGER.info(renderCacheTwo.size() + "");
        //WynnExtras.LOGGER.info(effectsAsString);
    }


    //Could use a rename didnt know what I should call it tbh.
    private static boolean alreadyContainsName(String name){
        Set<String> names = renderCacheTwo.stream()
                .map(AbilityCooldown::getName)
                .collect(Collectors.toSet());

        return names.contains(name);
    }

    private static AbilityCooldown nameToAbilityCooldown(String name){
        var nameToPerson = renderCacheTwo.stream()
                .collect(Collectors.toMap(
                        AbilityCooldown::getName,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        return nameToPerson.get(name);
    }


    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (renderCacheTwo.isEmpty()){
            return;
        }
        int currentx = 0;
        for(AbilityCooldown abilityCooldown : renderCacheTwo ){
            int cooldownDrawState =  24;

            //context.drawTexture(RenderLayer::getGuiTextured, COOLDOWN_IDENTIFIER, 225+currentx, 290 , 24-cooldownDrawState, 24, 24,-cooldownDrawState ,24, 24, 24, 24);


            context.drawTexture(RenderLayer::getGuiTextured, abilityCooldown.getIdentifier(), 225 + currentx, 290, 0 , 0 , 24, 24, 24, 24);
            if (abilityCooldown.currentDuration > 0){
                cooldownDrawState = Math.round((float) 24 / ((float) abilityCooldown.totalDuration / abilityCooldown.currentDuration));
                context.drawTexture(RenderLayer::getGuiTextured, COOLDOWN_IDENTIFIER, 225 + currentx, 290 , 24, 0, 24, cooldownDrawState ,24, cooldownDrawState, 24, cooldownDrawState);
            }
            currentx += 25;
        }

    }

    //Returns the Identifier of a Statuseffect
    //§7Heavenly Trumpet
    public static Identifier getAbilitiyCooldownIdentifier(StatusEffect effect){
        Pattern p = Pattern.compile("§7", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(effect.getName().getString());
        String result = m.replaceAll("");
        WynnExtras.LOGGER.info("textures/general/hud/abilities/"+result.replaceAll(" ", "").toLowerCase() + ".png" + " : Prefix: " + effect.getPrefix().getString() + " : Duration: " + effect.getDuration() + " : DisplayeTime :" + effect.getDisplayedTime().getString());
        //if (!Identifier.isPathValid("textures/general/hud/abilities/" + result.replaceAll(" ", "").toLowerCase() + ".png")) return  null;
        return  Identifier.of("wynnextras", "textures/general/hud/abilities/" +result.replaceAll(" ", "").toLowerCase() + ".png");
    }

    private class AbilityCooldown{
        private final int totalDuration;
        private boolean isActive;
        private int currentDuration;
        private Identifier identifier;
        private String name;
        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }
        public int getTotalDuration() {
            return totalDuration;
        }

        public int getCurrentDuration() {
            return currentDuration;
        }

        public void setCurrentDuration(int currentDuration) {
            this.currentDuration = currentDuration;
        }

        public Identifier getIdentifier() {
            return identifier;
        }

        public void setIdentifier(Identifier identifier) {
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public AbilityCooldown(String name, int totalDuration, Identifier identifier){
            this.name = name;
            this.totalDuration = totalDuration;
            this.currentDuration = this.totalDuration;
            this.identifier = identifier;
        }


        //Sets the time down by one second
        public void  updateTime(){
            this.currentDuration--;
        }

        //Sets the time down by {amount} of seconds
        public void  updateTime(int amount){
            this.currentDuration -= amount;
        }

    }

}


