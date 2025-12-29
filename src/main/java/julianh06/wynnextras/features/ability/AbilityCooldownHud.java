package julianh06.wynnextras.features.ability;

import com.wynntils.core.components.Models;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.colors.CustomColor;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.event.TickEvent;
import julianh06.wynnextras.utils.HUD.HUDUtils;
import julianh06.wynnextras.utils.HUD.WEHud;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AbilityCooldownHud extends WEHud {
    //public static final AbilityCooldownHud INSTANCE = new AbilityCooldownHud();
    public static final Identifier ABILITYCOOLDOWN_LAYER = Identifier.of("wynnextras", "ability-cooldown-layer");
    public static final Identifier IDENTIFIED_LAYER = IdentifiedLayer.MISC_OVERLAYS;
    private static final Identifier COOLDOWN_IDENTIFIER = Identifier.of("wynnextras",  "textures/general/hud/abilities/cooldown.png");

    private static List<AbilityCooldown> renderCache = new ArrayList<>();
    private int currentTick = 0;

    private static Identifier tempIdentifier = Identifier.of("wynnextras", "textures/general/hud/abilities/holytrumpets.png");

    public static void register(){
        WynnExtras.LOGGER.info("Ability CooldownHUD registerd");
        //hudUtils = new HUDUtils();

        ClientTickEvents.START_CLIENT_TICK.register((tick) -> {

        });
    }


    public AbilityCooldownHud() {
        super(IdentifiedLayer.MISC_OVERLAYS, Identifier.of("wynnextras", "ability-cooldown-hud"));
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        //This entire code could be a bit more optimized if done with StatusEffectChangedEvent from Wynntils.
        //But when ability is on cooldown we update the icon every 1s anyway and the code that gets executed is not that heavy, that it would probaly make an impact if you are not playing on early 2000s Hardware.

        if(event.ticks % 20 == 0){
            recalculateRenderCache();
        }
    }

    private void recalculateRenderCache() {

        List<StatusEffect> effects = Models.StatusEffect.getStatusEffects();

        //Using this isActive for better display if it should lag
        renderCache.forEach(abilityCooldown -> abilityCooldown.setActive(false));
        if (effects.isEmpty()){
            //Sometimes happens that the renderCache doesnt get cleared after the effects are over.
            renderCache.clear();
            return;
        }

        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

        //String effectsAsString = "";
        for (StatusEffect effect : effects){
            //effectsAsString +=  effect.getName().getString()  + "[" + effect.getDuration() +  "]"+ ",";
            Identifier effectIdentifier = getAbilitiyCooldownIdentifier(effect);

            //As an Example +Lightweight effect would throw a error if we dont check if its a Valid path first.
            if (!Identifier.isPathValid(effectIdentifier.getPath())) continue;

            //Checks if there is a png for the effect or if the prefix is the Cooldown prefix.
            if (resourceManager.getResource(effectIdentifier).isEmpty() || !effect.getPrefix().getString().equals("§8⬤")) continue;

            //If the Rendercache doesnt have the cooldown in it it will add it
            AbilityCooldown effectCooldown = nameToAbilityCooldown(effect.getName().getString());
            if (effectCooldown == null){
                effectCooldown = new AbilityCooldown(effect.getName().getString(), effect.getDuration(), effectIdentifier);
                renderCache.add(effectCooldown);
            }
            effectCooldown.updateTime();
            effectCooldown.setActive(true);
            //renderCache.add(effectIdentifier);


        }
        renderCache.removeIf(abilityCooldown -> !abilityCooldown.isActive());
    }

    private static AbilityCooldown nameToAbilityCooldown(String name){
        var nameToPerson = renderCache.stream()
                .collect(Collectors.toMap(
                        AbilityCooldown::getName,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        return nameToPerson.get(name);
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        super.render(context, tickCounter);
        int startX = getLogicalWidth() / 2 - 735;
        int startY = getLogicalHeight() / 2 -  250;



        //WynnExtras.LOGGER.info("Hud width:" + hudUtils.getLogicalWidth() / 2 + " Hud height: " + hudUtils.getLogicalHeight() / 2);
        if (renderCache.isEmpty()){
            return;
        }
        int currentx = 0;
        for(AbilityCooldown abilityCooldown : renderCache){
            int cooldownDrawState =  24;
            //225, 290
            context.drawTexture(RenderLayer::getGuiTextured, abilityCooldown.getIdentifier(), startX + currentx, startY, 0 , 0 , 24, 24, 24, 24);
            if (abilityCooldown.getCurrentDuration() > 0){
                float test1 =  (float) abilityCooldown.getTotalDuration() /abilityCooldown.getCurrentDuration();
                float test2 = 24 / test1;

                cooldownDrawState = Math.round((float) 24 / ((float) abilityCooldown.totalDuration / abilityCooldown.currentDuration));
                //WynnExtras.LOGGER.info("Test1: " + test1 +  " Test2: " +test2 + "Final: " + Math.round(test2));
                context.drawTexture(RenderLayer::getGuiTextured, COOLDOWN_IDENTIFIER, startX + currentx, startY , 24, 0, 24, cooldownDrawState ,24, cooldownDrawState, 24, cooldownDrawState);
            }
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(String.valueOf(abilityCooldown.currentDuration + 1)), startX + 10 + currentx,startY + 8, CustomColor.fromHexString("FFFFFF").asInt(), false);
            currentx += 25;
        }

    }
    public static Identifier getAbilitiyCooldownIdentifier(StatusEffect effect){
        Pattern p = Pattern.compile("§7", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(effect.getName().getString());
        String result = m.replaceAll("");
        //WynnExtras.LOGGER.info("textures/general/hud/abilities/"+result.replaceAll(" ", "").toLowerCase() + ".png" + " : Prefix: " + effect.getPrefix().getString() + " : Duration: " + effect.getDuration() + " : DisplayeTime :" + effect.getDisplayedTime().getString());
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
