package julianh06.wynnextras.event.api;


import julianh06.wynnextras.annotations.WEModule;
import julianh06.wynnextras.event.*;
import julianh06.wynnextras.utils.MinecraftUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import org.lwjgl.glfw.GLFW;


@WEModule
public class ClientEvents {
    private static int totalTicks = 0;
    private static long lastKnownTime = -1;
    private static int accumulatedTicks = 0;
    public ClientEvents() {
        ClientTickEvents.START_WORLD_TICK.register(world -> {
            if (!MinecraftUtils.localPlayerExists()) return;
            if (!MinecraftUtils.localWorldExists()) return;

            totalTicks++;
            new TickEvent(totalTicks).post();
        });
        ClientTickEvents.END_CLIENT_TICK.register(world -> {
            if (!MinecraftUtils.localPlayerExists()) return;
            if (!MinecraftUtils.localWorldExists()) return;
            //Code to simulate Clientticks.
            //When the server is dying the code can fale
            long currentTime = MinecraftClient.getInstance().player.getWorld().getTime();  // server-synced world time

            if (lastKnownTime == -1) {
                lastKnownTime = currentTime;
                return;
            }

            long delta = currentTime - lastKnownTime;
            if (delta > 0) {
                accumulatedTicks += (int) delta;
                lastKnownTime = currentTime;
            }
            new ServerTickEvent(accumulatedTicks).post();

        });
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            new DisconnectEvent().post();
        }));

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            new WorldChangeEvent().post();
        });

        ClientReceiveMessageEvents.GAME.register((message, var2) -> {
            new ChatEvent(message).post();
        });
    }
}
