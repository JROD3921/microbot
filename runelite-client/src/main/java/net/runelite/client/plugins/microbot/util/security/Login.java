package net.runelite.client.plugins.microbot.util.security;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.config.ProfileManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.globval.GlobalWidgetInfo;
import net.runelite.client.plugins.microbot.util.keyboard.VirtualKeyboard;
import net.runelite.client.plugins.microbot.util.menu.Rs2Menu;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.util.WorldUtil;

import java.awt.*;
import java.awt.event.KeyEvent;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class Login {

    private ConfigProfile getProfile() {
        try (ProfileManager.Lock lock = Microbot.getProfileManager().lock()) {
            return lock.getProfiles().stream().filter(x -> x.isActive()).findFirst().get();
        }

    }

    public Login() {
        VirtualKeyboard.keyPress(KeyEvent.VK_ENTER);
        sleep(300, 600);
        try {
            setWorld(360);
        }catch(Exception e) {
            System.out.println("Changing world failed");
        } finally {
            Microbot.getClient().setUsername(getProfile().getName());
            try {
                if (Encryption.decrypt(getProfile().getPassword()) != null && Encryption.decrypt(getProfile().getPassword()).length() > 0) {
                    Microbot.getClient().setPassword(Encryption.decrypt(getProfile().getPassword()));
                    sleep(300, 600);
                    VirtualKeyboard.keyPress(KeyEvent.VK_ENTER);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Login(String username, String password) {
        VirtualKeyboard.keyPress(KeyEvent.VK_ENTER);
        sleep(300, 600);
        try {
            setWorld(360);
        }catch(Exception e) {
            System.out.println("Changing world failed");
        } finally {
            Microbot.getClient().setUsername(username);
            try {
                Microbot.getClient().setPassword(Encryption.decrypt(password));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            sleep(300, 600);
            VirtualKeyboard.keyPress(KeyEvent.VK_ENTER);
        }
    }

    public Login(int world) {
        VirtualKeyboard.keyPress(KeyEvent.VK_ENTER);
        sleep(300, 600);
        try {
            setWorld(world);
        }catch(Exception e) {
            System.out.println("Changing world failed");
        } finally {
            Microbot.getClient().setUsername(getProfile().getName());
            try {
                Microbot.getClient().setPassword(Encryption.decrypt(getProfile().getPassword()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            sleep(300, 600);
            VirtualKeyboard.keyPress(KeyEvent.VK_ENTER);
        }
    }

    //TODO: this should be elsewhere
    private static final int INTERFACE_MAIN = 905;
    private static final int INTERFACE_MAIN_CHILD = 59;
    private static final int INTERFACE_MAIN_CHILD_COMPONENT_ID = 4;
    private static final int INTERFACE_LOGIN_SCREEN = 596;
    private static final int INTERFACE_USERNAME = 65;
    private static final int INTERFACE_USERNAME_WINDOW = 37;
    private static final int INTERFACE_PASSWORD = 71;
    private static final int INTERFACE_PASSWORD_WINDOW = 39;
    private static final int INTERFACE_BUTTON_LOGIN = 42;
    private static final int INTERFACE_TEXT_RETURN = 11;
    private static final int INTERFACE_BUTTON_BACK = 60;
    private static final int INTERFACE_WELCOME_SCREEN = 906;
    private static final int INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_1 = 160;
    private static final int INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_2 = 171;
    //private static final int INTERFACE_WELCOME_SCREEN_BUTTON_LOGOUT = 193;
    private static final int INTERFACE_WELCOME_SCREEN_TEXT_RETURN = 221;
    private static final int INTERFACE_WELCOME_SCREEN_BUTTON_BACK = 218;
    private static final int INTERFACE_WELCOME_SCREEN_HIGH_RISK_WORLD_TEXT = 86;
    private static final int INTERFACE_WELCOME_SCREEN_HIGH_RISK_WORLD_LOGIN_BUTTON = 93;
    private static final int INTERFACE_GRAPHICS_NOTICE = 976;
    private static final int INTERFACE_GRAPHICS_LEAVE_ALONE = 6;
    private static final int INDEX_LOGGED_OUT = 3;
    private static final int INDEX_LOBBY = 7;

    private int invalidCount, worldFullCount;

    public boolean activateCondition() {
        GameState idx = Microbot.getClient().getGameState();
        return ((Rs2Menu.getIndex("Play") == 0 || (idx == GameState.LOGIN_SCREEN || idx == GameState.LOGGING_IN)) && getProfile().getName() != null)
                || (idx == GameState.LOGGED_IN && Rs2Widget.getWidget(GlobalWidgetInfo.LOGIN_MOTW_TEXT.getPackedId(), 0) != null);
    }

    public void setWorld(int worldNumber) {
        net.runelite.http.api.worlds.World world = Microbot.getWorldService().getWorlds().findWorld(worldNumber);
        final net.runelite.api.World rsWorld = Microbot.getClient().createWorld();
        rsWorld.setActivity(world.getActivity());
        rsWorld.setAddress(world.getAddress());
        rsWorld.setId(world.getId());
        rsWorld.setPlayerCount(world.getPlayers());
        rsWorld.setLocation(world.getLocation());
        rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));
        Microbot.getClient().changeWorld(rsWorld);
    }

    private boolean switchingWorlds() {
        return Rs2Widget.getWidget(INTERFACE_WELCOME_SCREEN, INTERFACE_WELCOME_SCREEN_TEXT_RETURN) != null
                && Rs2Widget.getWidget(INTERFACE_WELCOME_SCREEN, INTERFACE_WELCOME_SCREEN_TEXT_RETURN)
                .getText().contains("just left another world");
    }

    // Clicks past all of the letters
    private boolean atLoginInterface(Widget i) {
        if (i == null) {
            return false;
        }
        Rectangle pos = i.getBounds();
        if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1) {
            return false;
        }
        int dy = (int) (pos.getHeight() - 4) / 2;
        int maxRandomX = (int) (pos.getMaxX() - pos.getCenterX());
        int midx = (int) (pos.getCenterX());
        int midy = (int) (pos.getMinY() + pos.getHeight() / 2);
        if (i.getIndex() == INTERFACE_PASSWORD_WINDOW) {
            Microbot.getMouse().click(minX(i), midy + random(-dy, dy));
        } else {
            Microbot.getMouse().click(midx + random(1, maxRandomX), midy + random(-dy, dy));
        }
        return true;
    }

    /*
     * Returns x int based on the letters in a Child Only the password text is
     * needed as the username text cannot reach past the middle of the interface
     */
    private int minX(Widget a) {
        int x = 0;
        Rectangle pos = a.getBounds();
        int dx = (int) (pos.getWidth() - 4) / 2;
        int midx = (int) (pos.getMinX() + pos.getWidth() / 2);
        if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1) {
            return 0;
        }
        Widget widget = Rs2Widget.getWidget(INTERFACE_LOGIN_SCREEN, 0);
        for (int i = 0; i < Rs2Widget.getWidget(GlobalWidgetInfo.TO_GROUP(widget.getId()), INTERFACE_PASSWORD).getText().length(); i++) {
            x += 11;
        }
        if (x > 44) {
            return (int) (pos.getMinX() + x + 15);
        } else {
            return midx + random(-dx, dx);
        }
    }

    private boolean atLoginScreen() {
        return !Rs2Widget.getWidget(596, 0).isHidden();
    }

    private boolean isUsernameFilled() {
        String username = getProfile().getName();
        Widget widget = Rs2Widget.getWidget(INTERFACE_LOGIN_SCREEN, 0);
        return Rs2Widget.getWidget(GlobalWidgetInfo.TO_GROUP(widget.getId()), INTERFACE_USERNAME).getText().toLowerCase().equalsIgnoreCase(username);
    }
}
