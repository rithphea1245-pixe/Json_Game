package com.worldofwonder.test;

import com.worldofwonder.controller.GameController;
import com.worldofwonder.model.User;
import com.worldofwonder.view.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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

        // 10. AuthController Registration & Login Validation Tests
        com.worldofwonder.controller.AuthController auth = new com.worldofwonder.controller.AuthController();

        // 10a. Invalid email format
        com.worldofwonder.controller.AuthController.AuthResult badEmail = auth.register("validuser", "notanemail", "pass123");
        if (badEmail.isSuccess()) {
            throw new AssertionError("Registration with invalid email should fail!");
        }
        System.out.println("[TEST 10a] Invalid email rejected correctly: " + badEmail.getMessage());

        // 10b. Short password
        com.worldofwonder.controller.AuthController.AuthResult shortPass = auth.register("validuser2", "user2@test.com", "12");
        if (shortPass.isSuccess()) {
            throw new AssertionError("Registration with short password (<4) should fail!");
        }
        System.out.println("[TEST 10b] Short password rejected correctly: " + shortPass.getMessage());

        // 10c. Successful register
        String authUser = "autotest_auth_" + System.currentTimeMillis();
        com.worldofwonder.controller.AuthController.AuthResult regOk = auth.register(authUser, authUser + "@wow.com", "mypass123");
        if (!regOk.isSuccess() || regOk.getUser() == null) {
            throw new AssertionError("Valid registration failed: " + regOk.getMessage());
        }
        System.out.println("[TEST 10c] Valid registration succeeded: " + regOk.getUser().getUsername());

        // 10d. Duplicate username rejection
        com.worldofwonder.controller.AuthController.AuthResult dupUser = auth.register(authUser, "different@wow.com", "mypass123");
        if (dupUser.isSuccess()) {
            throw new AssertionError("Duplicate username registration should fail!");
        }
        System.out.println("[TEST 10d] Duplicate username rejected correctly: " + dupUser.getMessage());

        // 10e. Valid login
        com.worldofwonder.controller.AuthController.AuthResult loginOk = auth.login(authUser, "mypass123");
        if (!loginOk.isSuccess()) {
            throw new AssertionError("Valid login failed: " + loginOk.getMessage());
        }
        System.out.println("[TEST 10e] Valid login succeeded: " + loginOk.getUser().getUsername());

        // 10f. Wrong password rejection
        com.worldofwonder.controller.AuthController.AuthResult badPass = auth.login(authUser, "wrongpassword");
        if (badPass.isSuccess()) {
            throw new AssertionError("Login with wrong password should fail!");
        }
        System.out.println("[TEST 10f] Wrong password rejected correctly: " + badPass.getMessage());

        // Clean up the registered test user
        auth.getUserRepository().deleteById(regOk.getUser().getId());
        System.out.println("[TEST 10g] Cleaned up auth test user.");

        // 11. Test I18n Localization & Dynamic Switcher
        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.EN);
        String enLogin = com.worldofwonder.util.I18n.get("tab_login");
        if (!"Login".equals(enLogin)) {
            throw new AssertionError("I18n English string mismatch: " + enLogin);
        }

        AtomicBoolean langListenerFired = new AtomicBoolean(false);
        Runnable langListener = () -> langListenerFired.set(true);
        com.worldofwonder.util.I18n.addLanguageListener(langListener);

        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.KM);
        String kmLogin = com.worldofwonder.util.I18n.get("tab_login");
        if (!"ចូលប្រើ".equals(kmLogin) || !langListenerFired.get()) {
            throw new AssertionError("I18n Khmer string or listener failed! Got: " + kmLogin);
        }
        System.out.println("[TEST 11] I18n Localization verified: EN='Login', KM='ចូលប្រើ', Listener=true");
        com.worldofwonder.util.I18n.removeLanguageListener(langListener);
        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.EN);

        // 12. Test Empty Submission Security Prevention
        com.worldofwonder.controller.AuthController.AuthResult emptyUserLogin = auth.login("", "pass123");
        com.worldofwonder.controller.AuthController.AuthResult emptyPassLogin = auth.login("admin", "   ");
        if (emptyUserLogin.isSuccess() || emptyPassLogin.isSuccess()) {
            throw new AssertionError("Empty login submission must be prevented!");
        }

        com.worldofwonder.controller.AuthController.AuthResult emptyReg = auth.register("   ", "", "  ");
        if (emptyReg.isSuccess()) {
            throw new AssertionError("Empty registration submission must be prevented!");
        }
        System.out.println("[TEST 12] Empty Form Submission Security verified: Rejected empty and whitespace inputs");

        // 13. Test SHA-256 Cryptographic Hashing and Legacy Migration
        String rawPass = "MySecretTestPass2026!";
        String hashed = com.worldofwonder.model.UserRepository.hashPassword(rawPass);
        if (hashed == null || hashed.length() != 64 || !hashed.matches("^[0-9a-fA-F]{64}$")) {
            throw new AssertionError("SHA-256 hash must be 64 hexadecimal characters! Got: " + hashed);
        }
        System.out.println("[TEST 13a] SHA-256 hash generation verified: " + hashed.substring(0, 16) + "...");

        // Verify transparent legacy password migration
        String legacyUser = "legacy_test_" + System.currentTimeMillis();
        User testLegacy = new User(0, legacyUser, legacyUser + "@test.com", "PlainOldPassword42", 20);
        com.worldofwonder.model.UserRepository repo = new com.worldofwonder.model.UserRepository();
        // Insert user directly with plaintext password
        repo.createUser(testLegacy);
        // Authenticate with plaintext password; repo should auto-upgrade to SHA-256 hash
        boolean verified = repo.verifyPassword(testLegacy, "PlainOldPassword42");
        if (!verified) {
            throw new AssertionError("Legacy plaintext verification failed!");
        }
        if (testLegacy.getPassword().equals("PlainOldPassword42") || testLegacy.getPassword().length() != 64) {
            throw new AssertionError("Legacy password was not migrated to SHA-256 hash! Got: " + testLegacy.getPassword());
        }
        System.out.println("[TEST 13b] Legacy Plaintext to SHA-256 Auto-Migration verified: Successfully migrated to hash");
        repo.deleteById(testLegacy.getId());

        // 14. Test Secret Password Eye Toggle Component
        UITheme.PillPasswordField passField = new UITheme.PillPasswordField("Password", UITheme.FieldIcon.PASSWORD);
        if (passField.getEchoChar() != '\u2022' || passField.isPasswordRevealed()) {
            throw new AssertionError("Password should be masked by default with bullet!");
        }
        passField.setPasswordRevealed(true);
        if (passField.getEchoChar() != (char) 0 || !passField.isPasswordRevealed()) {
            throw new AssertionError("Password should be visible when revealed is true!");
        }
        passField.setPasswordRevealed(false);
        if (passField.getEchoChar() != '\u2022' || passField.isPasswordRevealed()) {
            throw new AssertionError("Password should be re-masked when revealed is false!");
        }
        System.out.println("[TEST 14] Secret Password Eye Toggle state transitions verified: masked -> visible -> masked");

        // 15. Test Real-Life Vector Field Icons
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(200, 200, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        UITheme.drawFieldIcon(g2, UITheme.FieldIcon.USER, 10, 10, 24, java.awt.Color.WHITE);
        UITheme.drawFieldIcon(g2, UITheme.FieldIcon.EMAIL, 10, 40, 24, java.awt.Color.WHITE);
        UITheme.drawFieldIcon(g2, UITheme.FieldIcon.PASSWORD, 10, 70, 24, java.awt.Color.WHITE);
        System.out.println("[TEST 15] Real-Life Vector Field Icons rendered successfully");

        // 16. Test All VectorIcon Enums and Rendering
        for (UITheme.VectorIcon icon : UITheme.VectorIcon.values()) {
            UITheme.drawVectorIcon(g2, icon, 10, 10, 32, java.awt.Color.CYAN);
            javax.swing.JComponent comp = UITheme.vectorIcon(icon, 24, java.awt.Color.YELLOW);
            if (comp == null || comp.getPreferredSize().width != 24) {
                throw new AssertionError("VectorIcon component creation failed for: " + icon);
            }
        }
        g2.dispose();
        System.out.println("[TEST 16] All " + UITheme.VectorIcon.values().length + " VectorIcon enums verified and rendered cleanly");

        // 17. Test I18n Bilingual Switching (English <-> Khmer)
        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.EN);
        if (com.worldofwonder.util.I18n.isKhmer() || !com.worldofwonder.util.I18n.get("choose_game_title").equals("Choose your game")) {
            throw new AssertionError("I18n EN resolution failed! Got: " + com.worldofwonder.util.I18n.get("choose_game_title"));
        }
        if (!com.worldofwonder.util.I18n.get("back_to_games").equals("Back to Games")) {
            throw new AssertionError("I18n EN back_to_games failed!");
        }

        // Switch to Khmer
        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.KM);
        if (!com.worldofwonder.util.I18n.isKhmer() || !com.worldofwonder.util.I18n.get("choose_game_title").equals("ជ្រើសរើសហ្គេមរបស់អ្នក")) {
            throw new AssertionError("I18n KM resolution failed! Got: " + com.worldofwonder.util.I18n.get("choose_game_title"));
        }
        if (!com.worldofwonder.util.I18n.get("back_to_games").equals("ត្រឡប់ទៅហ្គេម")) {
            throw new AssertionError("I18n KM back_to_games failed!");
        }
        if (!com.worldofwonder.util.I18n.get("settings").equals("ការកំណត់")) {
            throw new AssertionError("I18n KM settings failed!");
        }
        if (!com.worldofwonder.util.I18n.get("wow_shuffle").equals("ច្របល់")) {
            throw new AssertionError("I18n KM wow_shuffle failed!");
        }
        if (!com.worldofwonder.util.I18n.get("cups_moves", 5).equals("ចំនួនលើក: 5")) {
            throw new AssertionError("I18n KM cups_moves parameter interpolation failed!");
        }

        // Verify font resolution returns Khmer font for Khmer text
        java.awt.Font khmerFont = UITheme.fontFor("ជ្រើសរើសហ្គេមរបស់អ្នក", java.awt.Font.BOLD, 16);
        if (!khmerFont.getFamily().equals(UITheme.KHMER_FAMILY)) {
            throw new AssertionError("UITheme.fontFor did not select KHMER_FAMILY! Got: " + khmerFont.getFamily());
        }

        // Reset to EN for default app state
        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.EN);
        System.out.println("[TEST 17] Full I18n Bilingual switching (English <-> ភាសាខ្មែរ) and font auto-resolution verified");

        // 18. Test Official Admin & Player Default Credentials and Localized Rank Titles
        com.worldofwonder.controller.AuthController.AuthResult adminLogin = auth.login("admin", "admin123");
        if (!adminLogin.isSuccess() || !adminLogin.getUser().isAdmin()) {
            throw new AssertionError("admin/admin123 login failed: " + adminLogin.getMessage());
        }
        System.out.println("[TEST 18a] Admin login with 'admin' / 'admin123' succeeded: " + adminLogin.getUser().getUsername());

        com.worldofwonder.controller.AuthController.AuthResult rithpheaLogin = auth.login("rithphea", "password123");
        if (!rithpheaLogin.isSuccess()) {
            throw new AssertionError("rithphea/password123 login failed: " + rithpheaLogin.getMessage());
        }
        System.out.println("[TEST 18b] Player login with 'rithphea' / 'password123' succeeded: " + rithpheaLogin.getUser().getUsername());

        // Test localized rank title
        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.KM);
        User rankedUser = new User(99, "ranktest", "r@test.com", "pass", 650);
        String kmRank = rankedUser.getRankTitle();
        if (!"អ្នកអច្ឆរិយៈរឿងព្រេងនិទាន".equals(kmRank)) {
            throw new AssertionError("Rank title in Khmer failed! Expected 'អ្នកអច្ឆរិយៈរឿងព្រេងនិទាន', got: " + kmRank);
        }
        System.out.println("[TEST 18c] Dynamic localized rank title verified in Khmer: " + kmRank);

        // 19. Test Khmer Play Now Button & GameModeCard Subtitle Rendering and Tube Colors
        String playKhmer = com.worldofwonder.util.I18n.get("play_now");
        if (!"លេងឥឡូវនេះ".equals(playKhmer)) {
            throw new AssertionError("play_now in Khmer failed! Expected 'លេងឥឡូវនេះ', got: " + playKhmer);
        }
        String tubeEmpty = com.worldofwonder.util.I18n.get("tube_empty");
        if (!"ទទេ".equals(tubeEmpty)) {
            throw new AssertionError("tube_empty in Khmer failed! Expected 'ទទេ', got: " + tubeEmpty);
        }

        // Verify GameModeCard paints cleanly with Khmer text and Play Now button
        UITheme.GameModeCard testCard = new UITheme.GameModeCard(UITheme.GameIcon.QUIZ,
                com.worldofwonder.util.I18n.get("game_quiz_title"),
                com.worldofwonder.util.I18n.get("game_quiz_sub"), UITheme.VIOLET);
        testCard.setSize(420, 240);
        java.awt.image.BufferedImage cardImg = new java.awt.image.BufferedImage(420, 240, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Card = cardImg.createGraphics();
        testCard.paint(g2Card);
        g2Card.dispose();

        // Count non-transparent pixels in bottom button area (y: 190..230, x: 100..320)
        int nonZeroPixels = 0;
        for (int y = 190; y < 230; y++) {
            for (int x = 100; x < 320; x++) {
                if ((cardImg.getRGB(x, y) & 0xFF000000) != 0) {
                    nonZeroPixels++;
                }
            }
        }
        if (nonZeroPixels < 500) {
            throw new AssertionError("GameModeCard Khmer Play Now button failed to render! Pixels: " + nonZeroPixels);
        }
        System.out.println("[TEST 19a] Khmer Play Now ('" + playKhmer + "') & GameModeCard rendered successfully. Pixels: " + nonZeroPixels);

        // Verify Cups Water Sort Screen Instantiation & Transparency
        MainUI dummyUI = new MainUI();
        Dashboard dummyDash = new Dashboard(dummyUI);
        CupsWaterSortGameScreen cupsScreen = new CupsWaterSortGameScreen(dummyDash);
        cupsScreen.setSize(1020, 700);
        java.awt.image.BufferedImage cupsImg = new java.awt.image.BufferedImage(1020, 700, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Cups = cupsImg.createGraphics();
        cupsScreen.paint(g2Cups);
        g2Cups.dispose();
        System.out.println("[TEST 19b] Cups Water Sort Screen rendered cleanly with localized badges and high contrast.");

        // [TEST 20] WordSearchGameScreen CardLayout & Single-Instance Hierarchy Verification
        WordSearchGameScreen wsScreen = new WordSearchGameScreen(dummyDash);
        wsScreen.setSize(1000, 700);

        int initialCompCount = countAllComponents(wsScreen);

        // Toggle language multiple times between EN and KM
        for (int i = 0; i < 6; i++) {
            com.worldofwonder.util.I18n.toggleLanguage();
        }

        int afterToggleCompCount = countAllComponents(wsScreen);
        if (afterToggleCompCount != initialCompCount) {
            throw new AssertionError("WordSearchGameScreen accumulated duplicate components on language change! Initial: " 
                    + initialCompCount + ", After: " + afterToggleCompCount);
        }

        // Verify rendering in Khmer does not crash and paints properly
        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.KM);
        java.awt.image.BufferedImage wsImg = new java.awt.image.BufferedImage(1000, 700, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Ws = wsImg.createGraphics();
        wsScreen.paint(g2Ws);
        g2Ws.dispose();
        dummyUI.dispose();

        // 21. Test SettingsModal Theme Cards & Font Non-Dropping in Khmer Mode
        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.KM);
        String midnightKm = com.worldofwonder.util.I18n.get("theme_midnight");
        String oceanKm = com.worldofwonder.util.I18n.get("theme_ocean");
        String activeKm = com.worldofwonder.util.I18n.get("theme_active");
        if (!"មីដណាយ ណេប៊ុយឡា (ងងឹត)".equals(midnightKm) || !"មហាសមុទ្រជ្រៅ (ងងឹត)".equals(oceanKm) || !"[រូបរាងកំពុងប្រើ]".equals(activeKm)) {
            throw new AssertionError("Settings theme localization failed! Got: " + midnightKm + ", " + oceanKm + ", " + activeKm);
        }
        // Test fontFor produces a font capable of rendering both Latin and Khmer
        java.awt.Font hybridFont = UITheme.fontFor("មីដណាយ (Dark)", java.awt.Font.BOLD, 15);
        if (!hybridFont.canDisplay('M') || !hybridFont.canDisplay('(')) {
            throw new AssertionError("hybridFont cannot display Latin letters or parentheses in Khmer mode!");
        }
        System.out.println("[TEST 21] SettingsModal Theme Cards & Hybrid Font Rendering verified in Khmer mode: " + midnightKm);

        // 22. Test QuizGameScreen Worlds, Levels, and Questions in Khmer Mode
        com.worldofwonder.view.QuizGameScreen quizScreen = new com.worldofwonder.view.QuizGameScreen(dummyDash);
        quizScreen.setSize(1040, 830);

        // Verify world 1-6 translations
        String w1Name = com.worldofwonder.util.I18n.get("world_1_name");
        String w1Desc = com.worldofwonder.util.I18n.get("world_1_desc");
        if (!"អេហ្ស៊ីបបុរាណ".equals(w1Name) || !w1Desc.contains("ស្តេចផារ៉ាអុង")) {
            throw new AssertionError("World 1 translation failed! Got: " + w1Name + ", " + w1Desc);
        }

        // Verify SampleQuizData bilingual questions
        com.worldofwonder.view.SampleQuizData sampleData = new com.worldofwonder.view.SampleQuizData();
        List<com.worldofwonder.model.Question> qList = sampleData.getQuestions(1);
        if (qList.isEmpty()) {
            throw new AssertionError("SampleQuizData level 1 has no questions!");
        }
        com.worldofwonder.model.Question q1 = qList.get(0);
        if (!q1.getLocalizedQuestionText().contains("ទន្លេណា") || !q1.getLocalizedOptionB().equals("នីល")) {
            throw new AssertionError("SampleQuizData Question 1 Khmer localization failed! Q: " + q1.getLocalizedQuestionText());
        }

        // Verify paint of QuizGameScreen in Khmer
        java.awt.image.BufferedImage quizImg = new java.awt.image.BufferedImage(1040, 830, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Quiz = quizImg.createGraphics();
        quizScreen.paint(g2Quiz);
        g2Quiz.dispose();

        System.out.println("[TEST 22] QuizGameScreen Worlds, Levels & Bilingual Questions verified in Khmer mode: " + w1Name);

        // [TEST 23] User Gamification Model
        com.worldofwonder.model.User gameUser = new com.worldofwonder.model.User(999, "wonder_gamer", "gamer@wonder.com", "pass123", 100);
        if (gameUser.getHearts() != 5 || !gameUser.hasHearts()) {
            throw new AssertionError("Initial hearts should be 5!");
        }
        gameUser.loseHeart();
        if (gameUser.getHearts() != 4) {
            throw new AssertionError("Hearts should be 4 after loseHeart! Got: " + gameUser.getHearts());
        }
        gameUser.restoreHearts();
        if (gameUser.getHearts() != 5) {
            throw new AssertionError("Hearts should restore to 5!");
        }
        gameUser.addHintCoins(50);
        if (gameUser.getHintCoins() != 50) {
            throw new AssertionError("Hint coins should be 50!");
        }
        boolean spent = gameUser.spendHintCoins(20);
        if (!spent || gameUser.getHintCoins() != 30) {
            throw new AssertionError("Spend hint coins failed! Remaining: " + gameUser.getHintCoins());
        }
        gameUser.updateStreak();
        if (gameUser.getStreakCount() != 1) {
            throw new AssertionError("Streak count should be 1 on first login!");
        }
        System.out.println("[TEST 23] User Gamification (Hearts, Streaks, Hint Coins) verified successfully.");

        // [TEST 24] MatchGameScreen
        com.worldofwonder.view.MatchGameScreen matchScreen = new com.worldofwonder.view.MatchGameScreen(dummyDash);
        matchScreen.setSize(1040, 800);
        java.awt.image.BufferedImage matchImg = new java.awt.image.BufferedImage(1040, 800, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Match = matchImg.createGraphics();
        matchScreen.paint(g2Match);
        g2Match.dispose();
        System.out.println("[TEST 24] MatchGameScreen (60-sec Match Master) instantiated and rendered successfully.");

        // [TEST 25] WorldWheelModal
        com.worldofwonder.view.WorldWheelModal wheelModal = new com.worldofwonder.view.WorldWheelModal(null, dummyDash);
        wheelModal.setSize(460, 540);
        java.awt.image.BufferedImage wheelImg = new java.awt.image.BufferedImage(460, 540, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Wheel = wheelImg.createGraphics();
        wheelModal.paint(g2Wheel);
        g2Wheel.dispose();
        wheelModal.dispose();
        System.out.println("[TEST 25] WorldWheelModal (6-world spinning wheel) instantiated and rendered successfully.");

        // [TEST 26] EncyclopediaModal
        com.worldofwonder.view.EncyclopediaModal encModal = new com.worldofwonder.view.EncyclopediaModal(null);
        encModal.setSize(740, 600);
        java.awt.image.BufferedImage encImg = new java.awt.image.BufferedImage(740, 600, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2Enc = encImg.createGraphics();
        encModal.paint(g2Enc);
        g2Enc.dispose();
        encModal.dispose();
        System.out.println("[TEST 26] EncyclopediaModal (Wikipedia Codex Cards) instantiated and rendered successfully.");

        // [TEST 27] ApiService Helpers & Category Mapping
        if (com.worldofwonder.util.ApiService.getOpenTDBCategory(1) != 23 ||
            com.worldofwonder.util.ApiService.getOpenTDBCategory(2) != 17 ||
            com.worldofwonder.util.ApiService.getOpenTDBCategory(3) != 27) {
            throw new AssertionError("OpenTDB category mappings invalid!");
        }
        System.out.println("[TEST 27] ApiService Category Mappings & Service Layer verified successfully.");

        com.worldofwonder.util.I18n.setLanguage(com.worldofwonder.util.I18n.Language.EN);
        System.out.println("=== ALL ADMIN CRUD, THEME, AUTH, I18N, GAMIFICATION & NEW GAME MODES TESTS PASSED SUCCESSFULLY! ===");
    }

    private static int countAllComponents(java.awt.Container c) {
        int count = 1;
        for (java.awt.Component comp : c.getComponents()) {
            if (comp instanceof java.awt.Container) {
                count += countAllComponents((java.awt.Container) comp);
            } else {
                count++;
            }
        }
        return count;
    }
}

