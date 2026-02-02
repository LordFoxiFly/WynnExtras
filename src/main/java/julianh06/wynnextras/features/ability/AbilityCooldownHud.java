package julianh06.wynnextras.features.ability;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.config.simpleconfig.SimpleConfig;
import julianh06.wynnextras.core.WELogger;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.event.ServerTickEvent;
import julianh06.wynnextras.event.TickEvent;
import julianh06.wynnextras.event.WorldChangeEvent;
import julianh06.wynnextras.utils.HUD.WEHud;
import julianh06.wynnextras.utils.UI.UIUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Unique;

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
    @Unique
    private static WynnExtrasConfig config;

    public static void register(){
        WynnExtras.LOGGER.info("Ability CooldownHUD registerd");
        if (config == null) config = SimpleConfig.getInstance(WynnExtrasConfig.class);
    }


    public AbilityCooldownHud() {
        super(IdentifiedLayer.MISC_OVERLAYS, Identifier.of("wynnextras", "ability-cooldown-hud"));
    }

//    Legacy code that did not calculate in the ServerTPS
//    @SubscribeEvent
//    public void onTick(TickEvent event) {
//        //This entire code could be a bit more optimized if done with StatusEffectChangedEvent from Wynntils.
//        //But when ability is on cooldown we update the icon every 1s anyway and the code that gets executed is not that heavy, that it would probaly make an impact if you are not playing on early 2000s Hardware.
//        if (!config.abilitycooldown) return;
//        if (event.ticks % 20 == 0) {
//
//            recalculateRenderCache();
//        }
//    }
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event){
        //This entire code could be a bit more optimized if done with StatusEffectChangedEvent from Wynntils.
        //But when ability is on cooldown we update the icon every 1s anyway and the code that gets executed is not that heavy, that it would probaly make an impact if you are not playing on early 2000s Hardware.
        if (!config.abilitycooldown) return;
        if (event.ticks % 20 == 0) {
            WynnExtras.LOGGER.info(event.ticks / 20  + ": CurrentServerTick secs");
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


                if (config.minAbilityTotalDuration >= effectCooldown.totalDuration) continue;

                renderCache.add(effectCooldown);
            }
            effectCooldown.updateTime();
            effectCooldown.setActive(true);
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
        //super.render(context, tickCounter);

        int startX = getLogicalWidth() / 2 - config.abilityHudX;
        int startY = getLogicalHeight() / 2 - config.abilityHudY;
        int currentx = 0;
        int textureScaled  = Math.round(24 * config.abilityHudScale);


        if (renderCache.isEmpty()){
            return;
        }

        this.drawContext = context;
        computeScaleAndOffsets();
        if (ui == null) ui = new UIUtils(context, scaleFactor, xStart, yStart);
        else ui.updateContext(context, scaleFactor, xStart, yStart);

        for(AbilityCooldown abilityCooldown : renderCache){
            int distanceX =Math.round(currentx * config.abilityHudScale);
            WynnExtras.LOGGER.info(distanceX + "Distance" + "Float: " + config.abilityHudScale);
            int cooldownDrawState =  textureScaled;
            context.drawTexture(RenderLayer::getGuiTextured, abilityCooldown.getIdentifier(), startX + distanceX, startY, 0 , 0 , textureScaled, textureScaled, textureScaled, textureScaled);

            //Draws the Cooldown animation
            if (abilityCooldown.getCurrentDuration() > 0){
                cooldownDrawState = Math.round((float) textureScaled / ((float) abilityCooldown.totalDuration / abilityCooldown.currentDuration));
                context.drawTexture(RenderLayer::getGuiTextured, COOLDOWN_IDENTIFIER, startX + distanceX, startY , textureScaled, 0, textureScaled, cooldownDrawState ,textureScaled, cooldownDrawState, textureScaled, cooldownDrawState);
            }

            //This line could be used for drawing the duration as a Text.
            //this.drawText(String.valueOf(abilityCooldown.currentDuration + 1), startX + 10 + currentx, startY + 8,CustomColor.fromHexString("FFFFFF") ,(float)scaleFactor);
            currentx += 25;
        }

    }

    /**
     * Returns the Identefiere of a StatusEffect
     * @param effect
     * @return
     */
    public static Identifier getAbilitiyCooldownIdentifier(StatusEffect effect){
        Pattern p = Pattern.compile("§7", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(effect.getName().getString());
        String result = m.replaceAll("");
        return  Identifier.of("wynnextras", "textures/general/hud/abilities/" +result.replaceAll(" ", "").toLowerCase() + ".png");
    }

    @Override
    protected void drawText(String text, float x, float y, CustomColor color, float textScale) {
        FontRenderer.getInstance().renderText(
                drawContext.getMatrices(),
                StyledText.fromComponent(Text.of(text)),
                x,
                y,
                color,
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL,
                (float)(textScale / scaleFactor),
                TextRenderer.TextLayerType.POLYGON_OFFSET);
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
