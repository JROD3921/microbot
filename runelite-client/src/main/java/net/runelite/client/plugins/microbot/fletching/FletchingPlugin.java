package net.runelite.client.plugins.microbot.fletching;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.NPCManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.widgets.WidgetID.LEVEL_UP_GROUP_ID;
import static net.runelite.client.plugins.natepainthelper.Info.*;

@PluginDescriptor(
        name = "Micro Fletcher",
        description = "Microbot fletching plugin",
        tags = {"fletching", "microbot", "skills"},
        enabledByDefault = false
)
@Slf4j
public class FletchingPlugin extends Plugin {
    @Inject
    private FletchingConfig config;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    Notifier notifier;

    @Provides
    FletchingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FletchingConfig.class);
    }
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private FletchingOverlay fletchingOverlay;

    @Inject
    FletchingScript fletchingScript;


    @Override
    protected void startUp() throws AWTException {
        Microbot.pauseAllScripts = false;
        Microbot.setClient(client);
        Microbot.setClientThread(clientThread);
        Microbot.setNotifier(notifier);
        Microbot.setMouse(new VirtualMouse());
        expstarted = Microbot.getClient().getSkillExperience(Skill.FLETCHING);
        startinglevel = Microbot.getClient().getRealSkillLevel(Skill.FLETCHING);
        timeBegan = System.currentTimeMillis();
        if (overlayManager != null) {
            overlayManager.add(fletchingOverlay);
        }
        fletchingScript.run(config);
    }

    protected void shutDown() {
        fletchingScript.shutdown();
        overlayManager.remove(fletchingOverlay);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        fletchingScript.onWidgetLoaded(event);
    }
}
