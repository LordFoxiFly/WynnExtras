package julianh06.wynnextras.event;

import julianh06.wynnextras.event.api.WEEvent;

public class ServerTickEvent extends WEEvent {
    public int ticks;

    public ServerTickEvent(int ticks) {
        this.ticks = ticks;
    }
}
