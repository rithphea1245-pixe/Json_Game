package scratch;

import com.worldofwonder.controller.*;
import com.worldofwonder.model.*;

import java.util.List;

public class ModernFeaturesTest {

    public static void main(String[] args) {
        System.out.println("--- Starting Modern Features Sanity Test ---");

        GameRepository gameRepo = new GameRepository();
        UserRepository userRepo = new UserRepository();
        GameController gameCtrl = new GameController(gameRepo, userRepo);
        QuizController quizCtrl = new QuizController(gameRepo, gameCtrl);

        // 1. Leaderboard test
        List<User> topUsers = gameCtrl.getLeaderboard(5);
        assertNotNull("Leaderboard should not be null", topUsers);
        assertFalse("Leaderboard should not be empty", topUsers.isEmpty());
        for (int i = 0; i < topUsers.size() - 1; i++) {
            assertTrue("Leaderboard should be sorted descending",
                    topUsers.get(i).getTotalPoints() >= topUsers.get(i + 1).getTotalPoints());
        }
        System.out.println("✔ Test 1: Leaderboard sorting passed (Top: " + topUsers.get(0).getUsername() + ", " + topUsers.get(0).getTotalPoints() + " pts)");

        // 2. Rank calculation test
        User u1 = new User("tester1", "t1@test.com", "pass");
        u1.setTotalPoints(45);
        assertEquals("Rank for 45 pts should be Novice Explorer", "Novice Explorer", u1.getRankTitle());
        u1.setTotalPoints(120);
        assertEquals("Rank for 120 pts should be Bronze Adventurer", "Bronze Adventurer", u1.getRankTitle());
        u1.setTotalPoints(250);
        assertEquals("Rank for 250 pts should be Silver Scholar", "Silver Scholar", u1.getRankTitle());
        u1.setTotalPoints(450);
        assertEquals("Rank for 450 pts should be Gold Master", "Gold Master", u1.getRankTitle());
        u1.setTotalPoints(800);
        assertEquals("Rank for 800 pts should be Legendary Wonderer", "Legendary Wonderer", u1.getRankTitle());
        System.out.println("✔ Test 2: Rank calculation tier test passed");

        // 3. Daily bonus claim test
        User bob = userRepo.findByUsername("bob");
        assertNotNull("Bob should exist", bob);
        int initialPoints = bob.getTotalPoints();
        // Reset lastClaimDate for test
        bob.setLastClaimDate(null);
        int newTotal = gameCtrl.claimDailyBonus(bob.getId(), 50);
        assertEquals("Points after daily claim should be initial + 50", initialPoints + 50, newTotal);
        // Double claim should be rejected on same day
        int doubleClaim = gameCtrl.claimDailyBonus(bob.getId(), 50);
        assertEquals("Double claim on same day should return -2", -2, doubleClaim);
        System.out.println("✔ Test 3: Daily bonus claiming and anti-duplicate passed");

        // 4. Streak multiplier test
        int base = 10;
        assertEquals("Streak 0 should be base points", 10, quizCtrl.calculatePointsWithStreak(base, 0));
        assertEquals("Streak 2 should be base points", 10, quizCtrl.calculatePointsWithStreak(base, 2));
        assertEquals("Streak 3 should be 1.5x (15)", 15, quizCtrl.calculatePointsWithStreak(base, 3));
        assertEquals("Streak 4 should be 1.5x (15)", 15, quizCtrl.calculatePointsWithStreak(base, 4));
        assertEquals("Streak 5 should be 2.0x (20)", 20, quizCtrl.calculatePointsWithStreak(base, 5));
        assertEquals("Streak 8 should be 2.0x (20)", 20, quizCtrl.calculatePointsWithStreak(base, 8));
        System.out.println("✔ Test 4: Streak multiplier test passed");

        // 5. 50:50 Lifeline exclusion test
        Question q = new Question();
        q.setCorrectAnswer("B");
        int[] elim = quizCtrl.getFiftyFiftyExclusions(q);
        assertEquals("50:50 should return 2 exclusions", 2, elim.length);
        assertTrue("Exclusion 0 cannot be the correct answer (1)", elim[0] != 1);
        assertTrue("Exclusion 1 cannot be the correct answer (1)", elim[1] != 1);
        assertTrue("Exclusions must be distinct", elim[0] != elim[1]);
        System.out.println("✔ Test 5: 50:50 Lifeline exclusions test passed");

        System.out.println("=== ALL 5 MODERN FEATURES UNIT TESTS PASSED SUCCESSFULLY ===");
    }

    private static void assertEquals(String msg, Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(msg + " [Expected: " + expected + ", Actual: " + actual + "]");
    }

    private static void assertTrue(String msg, boolean condition) {
        if (!condition) throw new AssertionError(msg);
    }

    private static void assertFalse(String msg, boolean condition) {
        if (condition) throw new AssertionError(msg);
    }

    private static void assertNotNull(String msg, Object obj) {
        if (obj == null) throw new AssertionError(msg);
    }
}

