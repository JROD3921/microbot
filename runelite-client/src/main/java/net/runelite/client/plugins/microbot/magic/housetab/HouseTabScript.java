package net.runelite.client.plugins.microbot.magic.housetab;

import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.magic.housetab.enums.HOUSETABS_CONFIG;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.VirtualKeyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HouseTabScript extends Script {
    public static String version = "1.0";
    private final int RIMMINGTON_PORTAL_OBJECT = 15478;
    private final int HOUSE_PORTAL_OBJECT = 4525;

    private final int HOUSE_ADVERTISEMENT_OBJECT = 29091;

    private final int HOUSE_ADVERTISEMENT_NAME_PARENT_INTERFACE = 3407881;

    private final int HOUSE_TABLET_INTERFACE = 5177359;

    private final HOUSETABS_CONFIG houseTabConfig;
    private final String[] playerHouses;

    private final ScheduledExecutorService scheduledExecutorService;


    private boolean hasSoftClay() {
        return Inventory.hasItem(1761);
    }

    private boolean hasSoftClayNoted() {
        return Inventory.hasItem(1762);
    }

    private boolean hasLawRune() {
        return Inventory.hasItem(ItemID.LAW_RUNE);
    }

    public HouseTabScript(HOUSETABS_CONFIG houseTabConfig, String[] playerHouses) {
        this.houseTabConfig = houseTabConfig;
        this.playerHouses = playerHouses;
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    private void lookForHouseAdvertisementObject() {
        Widget houseAdvertisementPanel = Microbot.getClient().getWidget(HOUSE_ADVERTISEMENT_NAME_PARENT_INTERFACE);
        if (!hasSoftClay() || houseAdvertisementPanel != null || Rs2GameObject.findObjectById(HOUSE_PORTAL_OBJECT) != null)
            return;


        boolean success = Rs2GameObject
                .interact(HOUSE_ADVERTISEMENT_OBJECT, "View");


        if (success) {
            sleepUntilOnClientThread(() -> Microbot.getClient().getWidget(HOUSE_ADVERTISEMENT_NAME_PARENT_INTERFACE) != null);
        }
    }

    private void lookForPlayerHouse() {
        Widget houseAdvertisementNameWidget = Microbot.getClient().getWidget(HOUSE_ADVERTISEMENT_NAME_PARENT_INTERFACE);
        if (houseAdvertisementNameWidget == null || houseAdvertisementNameWidget.getChildren() == null) return;
        if (!hasSoftClay())
            return;
        if (Rs2GameObject.findObjectById(HOUSE_PORTAL_OBJECT) != null)
            return;

        int enterHouseButtonHeight = 21;
        int houseIndexToJoin = 0;

        for (int i = 0; i < houseAdvertisementNameWidget.getChildren().length; i++) {
            Widget child = houseAdvertisementNameWidget.getChild(i);
            if (child == null) continue;
            if (Arrays.stream(this.playerHouses).anyMatch(x -> child.getText().equalsIgnoreCase(x))) {
                houseIndexToJoin = i;
                break;
            }
        }

        Widget mainWindow = Microbot.getClient().getWidget(3407879);
        if (mainWindow == null) return;
        int HOUSE_ADVERTISEMENT_ENTER_HOUSE_PARENT_INTERFACE = 3407891;
        Widget houseAdvertisementEnterHouseWidget = Microbot.getClient().getWidget(HOUSE_ADVERTISEMENT_ENTER_HOUSE_PARENT_INTERFACE);
        if (houseAdvertisementEnterHouseWidget == null) return;
        Widget enterHouseButton = houseAdvertisementEnterHouseWidget.getChild(houseIndexToJoin);
        int buttonRelativeY = houseAdvertisementEnterHouseWidget.getChild(houseIndexToJoin).getRelativeY() + enterHouseButtonHeight;
        if (buttonRelativeY > (mainWindow.getScrollY() + mainWindow.getHeight())) {
            keepExecuteUntil(() -> {
                int x = (int) mainWindow.getBounds().getCenterX() + Random.random(-50, 50);
                int y = (int) mainWindow.getBounds().getCenterY() + Random.random(-50, 50);
                Microbot.getMouse().scrollDown(new Point(x, y));
            }, () -> buttonRelativeY <= (mainWindow.getScrollY() + mainWindow.getHeight()), 500);
        } else {
            Microbot.getMouse()
                    .click(enterHouseButton.getCanvasLocation());
            sleepUntilOnClientThread(() -> Rs2GameObject.findObjectById(HOUSE_PORTAL_OBJECT) != null);
            sleep(2000, 3000);
        }
    }

    public void lookForLectern() {
        if (!hasSoftClay() || Rs2GameObject.findObjectById(HOUSE_ADVERTISEMENT_OBJECT) != null || Microbot.isGainingExp)
            return;

        Widget houseTabInterface = Microbot.getClient().getWidget(HOUSE_TABLET_INTERFACE);
        if (houseTabInterface != null || Rs2GameObject.findObjectById(HOUSE_PORTAL_OBJECT) == null) return;

        boolean success = Rs2GameObject.interact(new int[]{13647, 37349}, "Study");
        if (success) {
            sleepUntilOnClientThread(() -> Microbot.getClient().getWidget(HOUSE_TABLET_INTERFACE) != null);
        }
    }

    public void createHouseTablet() {
        Widget houseTabInterface = Microbot.getClient().getWidget(HOUSE_TABLET_INTERFACE);
        if (houseTabInterface == null) return;
        if (!hasSoftClay() || Rs2GameObject.findObjectById(HOUSE_PORTAL_OBJECT) == null)
            return;

        while (Microbot.getClient().getWidget(HOUSE_TABLET_INTERFACE) != null) {
            Microbot.getMouse()
                    .click(houseTabInterface.getCanvasLocation());
            sleep(1000, 2000);
        }

        sleepUntilOnClientThread(() -> !hasSoftClay()
                || Microbot.getClient().getWidget(HOUSE_TABLET_INTERFACE) != null
                || !Microbot.isGainingExp, 55000);
    }

    public void leaveHouse() {
        if (hasSoftClay() || Rs2GameObject.findObjectById(HOUSE_PORTAL_OBJECT) == null)
            return;

        boolean success = Rs2GameObject.interact(HOUSE_PORTAL_OBJECT, "Enter");
        if (success)
            sleepUntil(() -> Rs2GameObject.findObjectById(HOUSE_PORTAL_OBJECT) == null);
    }

    public void unnoteClay() {
        if (hasSoftClay() || Rs2GameObject.findObjectById(HOUSE_ADVERTISEMENT_OBJECT) == null)
            return;
        if (Microbot.getClient().getWidget(14352385) == null) {
            do {
                Microbot.getClientThread().invoke(() -> {
                    Inventory.useItemSafe("Soft clay");
                });
                sleep(300, 380);
            } while (!Rs2Npc.interact("Phials", "Use"));
        }

        sleep(2500, 5000);
        if (Microbot.getClient().getWidget(14352385) != null) {
            VirtualKeyboard.keyPress('3');
            sleep(300, 380);
        }
    }

    long currentInventoryCount = 0;

    public boolean run(HouseTabConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) return;
                if (!hasSoftClayNoted() || !hasLawRune()) {
                    shutdown();
                    return;
                }
                if (Microbot.isGainingExp) return;

                toggleRunEnergy(true);
                if (Microbot.getClient().getEnergy() < 3000 && !Rs2Widget.hasWidget("house teleport") && Rs2GameObject.findObject(new int[] {ObjectID.FROZEN_ORNATE_POOL_OF_REJUVENATION, ObjectID.POOL_OF_REJUVENATION}) != null) {
                    Rs2GameObject.interact(new int[] {ObjectID.FROZEN_ORNATE_POOL_OF_REJUVENATION, ObjectID.POOL_OF_REJUVENATION}, "drink");
                    return;
                }

                if (config.HouseConfig() == HOUSETABS_CONFIG.HOUSE_ADVERTISEMENT) {
                    lookForHouseAdvertisementObject();
                    lookForPlayerHouse();
                    lookForLectern();
                    createHouseTablet();
                    leaveHouse();
                    unnoteClay();
                } else if (config.HouseConfig() == HOUSETABS_CONFIG.FRIENDS_HOUSE) {
                    boolean isInHouse = Rs2GameObject.findObject(new int[] {ObjectID.LECTERN_37349}) != null;
                    if (isInHouse) {
                        if (hasSoftClay()) {
                            if (!Rs2Widget.hasWidget("house teleport"))
                                Rs2GameObject.interact("lectern");
                            sleepUntil(() -> Rs2Widget.hasWidget("house teleport"));
                            if (!Rs2Widget.hasWidget("house teleport")) return;
                            Rs2Widget.clickWidget("house teleport");
                            sleep(4000);
                            sleepUntil(() -> !hasSoftClay() || !Microbot.isGainingExp, 60000);
                        } else {
                            Rs2GameObject.interact(ObjectID.PORTAL_4525, "enter");
                            sleepUntil(() -> Rs2GameObject.findObjectById(ObjectID.LECTERN_37349) == null);
                        }

                    } else {
                        if (!Inventory.isInventoryFull()) {
                            Inventory.useItemSafe("soft clay");
                            if (Rs2Npc.interact("phials", "use"))
                                sleepUntil(() -> Rs2Widget.hasWidget("select an option"));
                            Rs2Widget.clickWidget("exchange all");
                        } else {
                            if (Rs2GameObject.interact(ObjectID.PORTAL_15478, "Friend's house")) {
                                sleepUntil(() -> Rs2Widget.hasWidget("Enter name"));
                                if (Rs2Widget.hasWidget("last name")) {
                                    Rs2Widget.clickWidget(config.housePlayerName());
                                } else {
                                    if (Rs2Widget.hasWidget("Enter name")) {
                                        VirtualKeyboard.typeString(config.housePlayerName());
                                        VirtualKeyboard.enter();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }
}