package com.worldofwonder.view;

import com.worldofwonder.util.I18n;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.util.Random;

/**
 * Animated Wonder Wheel modal where players spin a 6-world wheel to unlock bonus coins
 * and double-XP challenges with smooth physics deceleration.
 */
public class WorldWheelModal extends JDialog {

    private static final Color[] WEDGE_COLORS = {
            new Color(0xd4a020), // Egypt (Gold)
            new Color(0x7b42f6), // Space (Purple)
            new Color(0x00a8e8), // Ocean (Cyan)
            new Color(0x27ae60), // Dinosaurs (Green)
            new Color(0xe74c3c), // Medieval (Red)
            new Color(0x16a085)  // Rainforest (Teal)
    };

    private static final String[] WORLD_KEYS = {
            "world_1_name",
            "world_2_name",
            "world_3_name",
            "world_4_name",
            "world_5_name",
            "world_6_name"
    };

    private final Dashboard dashboard;
    private final WheelCanvas wheelCanvas;
    private final JLabel resultLabel;
    private final JButton spinBtn;
    private Timer spinTimer;
    private double currentAngle = 0;
    private double spinSpeed = 0;
    private boolean isSpinning = false;
    private final Random random = new Random();

    public WorldWheelModal(JFrame parent, Dashboard dashboard) {
        super(parent, "Wonder Wheel - Spin & Win", true);
        this.dashboard = dashboard;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel(new BorderLayout(0, UITheme.GAP_SECTION));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        header.add(UITheme.vectorIcon(UITheme.VectorIcon.REFRESH, 36, UITheme.GOLD));
        header.add(Box.createVerticalStrut(6));

        String titleStr = I18n.get("wheel_title");
        JLabel title = new UITheme.GradientTextLabel(titleStr,
                UITheme.FONT_PAGE_TITLE + 2, new Color(0xffd700), new Color(0xff8800));
        title.setFont(UITheme.fontFor(titleStr, Font.BOLD, UITheme.FONT_PAGE_TITLE + 2));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));

        String subStr = I18n.get("wheel_sub");
        JLabel sub = UITheme.subtitle(subStr);
        sub.setFont(UITheme.fontFor(subStr, Font.PLAIN, UITheme.FONT_BODY));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(sub);
        content.add(header, BorderLayout.NORTH);

        // Center Wheel Canvas
        wheelCanvas = new WheelCanvas();
        wheelCanvas.setPreferredSize(new Dimension(300, 300));
        content.add(wheelCanvas, BorderLayout.CENTER);

        // South: Result + Action Buttons
        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));

        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(UITheme.fontFor("Bonus", Font.BOLD, 15));
        resultLabel.setForeground(UITheme.GOLD);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        south.add(resultLabel);
        south.add(Box.createVerticalStrut(10));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);

        spinBtn = UITheme.primaryButton(I18n.get("wheel_spin"));
        UIUtil.fixedSize(spinBtn, 160, UITheme.BTN_H);
        spinBtn.addActionListener(e -> startSpin());
        btnRow.add(spinBtn);

        JButton closeBtn = UITheme.ghostButton(I18n.get("btn_close"), UITheme.CORAL);
        UIUtil.fixedSize(closeBtn, 130, UITheme.BTN_H);
        closeBtn.addActionListener(e -> {
            if (spinTimer != null) spinTimer.stop();
            SoundUtil.playClick();
            dispose();
        });
        btnRow.add(closeBtn);

        south.add(btnRow);
        content.add(south, BorderLayout.SOUTH);

        // Outer Card
        JPanel card = UITheme.modalCard(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(0xd4a020), 2, true));
        card.setPreferredSize(new Dimension(460, 540));
        card.add(content, BorderLayout.CENTER);

        setContentPane(card);
        pack();
        setLocationRelativeTo(parent);
    }

    private void startSpin() {
        if (isSpinning) return;
        isSpinning = true;
        spinBtn.setEnabled(false);
        resultLabel.setText(" ");
        SoundUtil.playClick();

        spinSpeed = 22.0 + random.nextDouble() * 12.0;

        spinTimer = new Timer(20, e -> {
            currentAngle += spinSpeed;
            if (currentAngle >= 360) {
                currentAngle -= 360;
            }
            spinSpeed *= 0.982; // Deceleration friction

            wheelCanvas.repaint();

            if (spinSpeed < 0.25) {
                spinTimer.stop();
                isSpinning = false;
                spinBtn.setEnabled(true);
                onSpinFinished();
            }
        });
        spinTimer.start();
    }

    private void onSpinFinished() {
        // Pointer is at the top (270 degrees in standard polar coords)
        double normalized = (360 - (currentAngle % 360) + 270) % 360;
        int wedgeIndex = (int) (normalized / 60.0) % 6;
        if (wedgeIndex < 0) wedgeIndex += 6;

        String winningWorld = I18n.get(WORLD_KEYS[wedgeIndex]);
        int rewardCoins = 30;

        SoundUtil.playVictory();
        resultLabel.setText(I18n.get("wheel_result", winningWorld));
        resultLabel.setFont(UITheme.fontFor(resultLabel.getText(), Font.BOLD, 14));

        if (dashboard != null) {
            dashboard.addGamePoints(rewardCoins);
        }
    }

    private class WheelCanvas extends JPanel {
        WheelCanvas() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;
            int cy = h / 2;
            int radius = Math.min(w, h) / 2 - 20;

            // Draw Wheel Wedges
            for (int i = 0; i < 6; i++) {
                double startDeg = currentAngle + i * 60;
                g2.setColor(WEDGE_COLORS[i]);
                g2.fill(new Arc2D.Double(cx - radius, cy - radius, radius * 2, radius * 2, -startDeg, -60, Arc2D.PIE));

                g2.setColor(new Color(0, 0, 0, 60));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Arc2D.Double(cx - radius, cy - radius, radius * 2, radius * 2, -startDeg, -60, Arc2D.PIE));

                // Draw Text
                AffineTransform old = g2.getTransform();
                double textAngle = Math.toRadians(startDeg + 30);
                int tx = cx + (int) (Math.cos(textAngle) * (radius * 0.65));
                int ty = cy + (int) (Math.sin(textAngle) * (radius * 0.65));
                g2.translate(tx, ty);
                g2.rotate(textAngle + Math.PI / 2);

                String name = I18n.get(WORLD_KEYS[i]);
                if (name.length() > 10) name = name.substring(0, 8) + "..";
                g2.setFont(UITheme.fontFor(name, Font.BOLD, 11));
                g2.setColor(Color.WHITE);
                int tw = g2.getFontMetrics().stringWidth(name);
                g2.drawString(name, -tw / 2, 4);

                g2.setTransform(old);
            }

            // Outer Golden Rim
            g2.setColor(new Color(0xffd700));
            g2.setStroke(new BasicStroke(5f));
            g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

            // Center Hub
            g2.setColor(new Color(0x182438));
            g2.fillOval(cx - 26, cy - 26, 52, 52);
            g2.setColor(new Color(0xffd700));
            g2.setStroke(new BasicStroke(3f));
            g2.drawOval(cx - 26, cy - 26, 52, 52);

            // Center Star
            g2.setColor(UITheme.GOLD);
            g2.setFont(UITheme.displayFont(Font.BOLD, 18));
            g2.drawString("W", cx - 8, cy + 7);

            // Top Pointer (Arrow pointing down at wheel)
            Polygon pointer = new Polygon();
            pointer.addPoint(cx, cy - radius + 14);
            pointer.addPoint(cx - 12, cy - radius - 12);
            pointer.addPoint(cx + 12, cy - radius - 12);

            g2.setColor(new Color(0xff3366));
            g2.fillPolygon(pointer);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawPolygon(pointer);

            g2.dispose();
        }
    }
}
