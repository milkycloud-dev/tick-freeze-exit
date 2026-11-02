package com.notebuns.tickfreezeexit;

import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = TickFreezeExit.MOD_ID, dist = Dist.DEDICATED_SERVER)
public final class TickFreezeExit {
    public static final String MOD_ID = "tickfreezeexit";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final long FREEZE_MS = Long.getLong("tickfreezeexit.freezeSeconds", 300L) * 1000L;
    private static final long DEAD_MS = Long.getLong("tickfreezeexit.deadSeconds", 90L) * 1000L;
    private static final long CHECK_MS = 5_000L;
    private boolean started;

    public TickFreezeExit(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Tick Freeze Exit 1.0.0: exit code 1 after {} s without a tick or {} s after the server thread ends", FREEZE_MS / 1000, DEAD_MS / 1000);
    }

    @SubscribeEvent
    public void onStarted(ServerStartedEvent event) {
        if (started) {
            return;
        }
        started = true;
        MinecraftServer server = event.getServer();
        Thread main = server.getRunningThread();
        Thread watcher = new Thread(() -> watch(server, main), "Tick Freeze Exit");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static void watch(MinecraftServer server, Thread main) {
        long deadSince = 0L;
        int lastTick = server.getTickCount();
        long frozenSince = System.currentTimeMillis();
        boolean frozenWarned = false;
        while (true) {
            try {
                Thread.sleep(CHECK_MS);
            } catch (InterruptedException e) {
                return;
            }
            long now = System.currentTimeMillis();
            if (main.isAlive()) {
                deadSince = 0L;
                int tick = server.getTickCount();
                if (tick != lastTick) {
                    lastTick = tick;
                    frozenSince = now;
                    frozenWarned = false;
                    continue;
                }
                long frozen = now - frozenSince;
                if (!frozenWarned && frozen >= FREEZE_MS / 2) {
                    frozenWarned = true;
                    LOGGER.warn("Server thread is alive but tick {} has not advanced for {} s, exiting in {} s", tick, frozen / 1000, (FREEZE_MS - frozen) / 1000);
                }
                if (frozen >= FREEZE_MS) {
                    LOGGER.error("Server frozen on tick {} for {} s, halting the JVM with exit code 1 so the host restarts it", tick, frozen / 1000);
                    Runtime.getRuntime().halt(1);
                }
                continue;
            }
            if (deadSince == 0L) {
                deadSince = now;
                LOGGER.warn("Server thread has ended, waiting {} s for the JVM to exit", DEAD_MS / 1000);
            } else if (now - deadSince >= DEAD_MS) {
                LOGGER.error("Server stopped but the JVM is still alive after {} s, halting with exit code 1 so the host restarts it", (now - deadSince) / 1000);
                Runtime.getRuntime().halt(1);
            }
        }
    }
}
