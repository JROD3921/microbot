package net.runelite.client.plugins.microbot.deserttreasure2.bosses.leviathan;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GraphicID;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NpcID;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.example.ExampleScript;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "Micro Leviathan",
        description = "Microbot Leviathan plugin",
        tags = {"Leviathan", "microbot", "boss"},
        enabledByDefault = false
)
@Slf4j
public class LeviathanPlugin extends Plugin {

    int meleeProjectile = 2488;
    int mageProjectile = 2489;
    int rangeProjectile = 2487;

    @Inject
    private LeviathanConfig config;
    @Provides
    LeviathanConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LeviathanConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private LeviathanOverlay exampleOverlay;

    @Inject
    LeviathanScript LeviathanScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(exampleOverlay);
        }
        LeviathanScript.run(config);
    }

    protected void shutDown() {
        LeviathanScript.shutdown();
        overlayManager.remove(exampleOverlay);
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event)
    {
        System.out.println("Graphics object created");
        System.out.println(event.getGraphicsObject().getId());
    }
    @Subscribe
    public void onProjectileMoved(ProjectileMoved event)
    {
        final Projectile projectile = event.getProjectile();

        System.out.println(projectile.getId());

        if (event.getProjectile().getRemainingCycles() < 10) {
            if (projectile.getId() == meleeProjectile)
            {
                Rs2Prayer.turnOnMeleePrayer();
            } else if (projectile.getId() == mageProjectile) {
                Rs2Prayer.turnOnMagePrayer();
            } else if (projectile.getId() == rangeProjectile) {
                Rs2Prayer.turnOnRangedPrayer();
            }
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        System.out.println("npc spawned created");
        System.out.println(event.getNpc().getId());
        if (event.getNpc().getId() == NpcID.ABYSSAL_PATHFINDER) {
            System.out.println("pathfinder found!");
        }
    }
}
