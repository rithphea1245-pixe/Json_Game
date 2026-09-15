package com.worldofwonder.test;

import com.worldofwonder.controller.GameController;
import com.worldofwonder.model.User;
import com.worldofwonder.view.UITheme;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdminCrudTest {

    public static void main(String[] args) {
        System.out.println("=== Starting Admin CRUD & Theme Automation Test ===");
        GameController controller = new GameController();

        // 1. Read all users
        List<User> initialUsers = controller.getAllUsers();
        System.out.println("[TEST 1] Retrieved existing users count: " + initialUsers.size());
        assert initialUsers != null : "User list should not be null";

        // 2. Create a test user
        String testUsername = "autotest_" + System.currentTimeMillis();
        User testUser = new User(0, testUsername, testUsername + "@example.com", "SecretPass123!", 50);
        testUser.setAdmin(false);
        boolean created = controller.createUser(testUser);
        System.out.println("[TEST 2] Created user '" + testUsername + "': " + created);
        if (!created || testUser.getId() <= 0) {
            throw new AssertionError("User creation failed or ID not generated!");
        }

        // 3. Verify user exists and verify role
        int createdId = testUser.getId();
        boolean isAdminInitial = controller.isUserAdmin(createdId);
        System.out.println("[TEST 3] User initial isAdmin: " + isAdminInitial + " (expected false)");
        if (isAdminInitial) {
            throw new AssertionError("Newly created user should NOT be admin!");
        }

        // 4. Update user: promote to ADMIN and add 500 points
        testUser.setAdmin(true);
        testUser.setTotalPoints(550);
        boolean updated = controller.updateUser(testUser);
        System.out.println("[TEST 4] Updated user to ADMIN with 550 points: " + updated);
        if (!updated) {
            throw new AssertionError("User update failed!");
        }

        // 5. Verify role is now ADMIN
        boolean isAdminNow = controller.isUserAdmin(createdId);
        System.out.println("[TEST 5] User updated isAdmin: " + isAdminNow + " (expected true)");
        if (!isAdminNow) {
            throw new AssertionError("User should now be admin!");
        }

        // 6. Delete test user
        boolean deleted = controller.deleteUser(createdId);
        System.out.println("[TEST 6] Deleted test user: " + deleted);
        if (!deleted) {
            throw new AssertionError("User deletion failed!");
        }

        // 7. Verify user no longer exists
        boolean isAdminAfterDelete = controller.isUserAdmin(createdId);
        System.out.println("[TEST 7] User isAdmin after deletion: " + isAdminAfterDelete + " (expected false)");
        if (isAdminAfterDelete) {
            throw new AssertionError("Deleted user should not exist or be admin!");
        }

        // 8. Test ThemePalette and listeners
        AtomicBoolean themeListenerFired = new AtomicBoolean(false);
        Runnable listener = () -> themeListenerFired.set(true);
        UITheme.addThemeListener(listener);

        UITheme.setPalette(UITheme.ThemePalette.OCEAN);
        System.out.println("[TEST 8] Changed theme to OCEAN. Current: " + UITheme.getCurrentPalette().displayName);
        if (UITheme.getCurrentPalette() != UITheme.ThemePalette.OCEAN || !themeListenerFired.get()) {
            throw new AssertionError("Theme listener did not fire on palette change!");
        }

        // Reset theme to MIDNIGHT
        UITheme.setPalette(UITheme.ThemePalette.MIDNIGHT);
        UITheme.removeThemeListener(listener);

        // 9. Coin component vector star verification
        javax.swing.JComponent coinComp = UITheme.coin(24);
        if (coinComp.getPreferredSize().width != 24 || coinComp.getPreferredSize().height != 24) {
            throw new AssertionError("Coin dimension mismatch!");
        }
        System.out.println("[TEST 9] Coin component verified: " + coinComp.getPreferredSize());

        System.out.println("=== ALL ADMIN CRUD & THEME TESTS PASSED SUCCESSFULLY! ===");
    }
}
