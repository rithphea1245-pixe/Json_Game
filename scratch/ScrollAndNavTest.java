package scratch;

import com.worldofwonder.view.*;
import javax.swing.*;
import java.awt.*;

public class ScrollAndNavTest {

    public static void main(String[] args) {
        System.out.println("--- Starting Scrolling & Navigation Sanity Test ---");

        // 1. Verify ScreenPage wraps in JScrollPane
        JPanel dummyCard = new JPanel();
        dummyCard.setPreferredSize(new Dimension(800, 600));
        JPanel page = UITheme.screenPage(dummyCard);
        assertNotNull("ScreenPage must not be null", page);

        Component[] children = page.getComponents();
        assertTrue("ScreenPage should have children", children.length > 0);
        assertTrue("ScreenPage child should be JScrollPane", children[0] instanceof JScrollPane);

        JScrollPane scroll = (JScrollPane) children[0];
        assertEquals("Unit increment should be 22", 22, scroll.getVerticalScrollBar().getUnitIncrement());
        assertTrue("Viewport view should be Scrollable", scroll.getViewport().getView() instanceof Scrollable);
        System.out.println("✔ Test 1: JScrollPane wrapping and smooth increment verified");

        // 2. CenteringPanel behavior
        UITheme.CenteringPanel cp = new UITheme.CenteringPanel(new GridBagLayout());
        cp.setPreferredSize(new Dimension(1000, 700));

        JViewport vp = new JViewport();
        vp.setView(cp);
        vp.setSize(1200, 900);
        assertTrue("Should track width when viewport is wider", cp.getScrollableTracksViewportWidth());
        assertTrue("Should track height when viewport is taller", cp.getScrollableTracksViewportHeight());

        vp.setSize(800, 500);
        assertFalse("Should not track width when viewport is narrower (enables scrollbar)", cp.getScrollableTracksViewportWidth());
        assertFalse("Should not track height when viewport is shorter (enables scrollbar)", cp.getScrollableTracksViewportHeight());
        System.out.println("✔ Test 2: CenteringPanel responsive viewport tracking verified");

        // 3. Font resolution
        Font display = UITheme.displayFont(Font.BOLD, 24);
        assertNotNull("Display font should be non-null", display);
        Font body = UITheme.bodyFont(Font.PLAIN, 15);
        assertNotNull("Body font should be non-null", body);
        System.out.println("✔ Test 3: Font resolution verified (Display: " + display.getFamily() + ", Body: " + body.getFamily() + ")");

        System.out.println("=== ALL SCROLL & NAVIGATION TESTS PASSED SUCCESSFULLY ===");
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

