package com.worldofwonder.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Settings and customization modal dialog.
 * Enables switching themes (Midnight, Deep Ocean, Royal Amethyst, Daylight Crystal),
 * toggling Dark/Light mode, and managing game sound effects.
 */
public class SettingsModal extends JDialog {

    private final JFrame parent;
    private final JButton sfxToggleBtn;
    private final JPanel themeCardsPanel;

    public SettingsModal(JFrame parent) {
        super(parent, "Settings & Customization", true);
        this.parent = parent;

        setSize(620, 540);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(new Color(13, 27, 42));
        content.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = UITheme.title("Game Settings & Appearance", 22);
        title.setForeground(UITheme.GOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Customize visual themes, switch dark/light mode, and toggle audio.");
        sub.setFont(UITheme.bodyFont(Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(sub);

        content.add(header, BorderLayout.NORTH);

        // Body with Theme Selection and Sound Options
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Theme Mode Section
        JLabel themeTitle = new JLabel("Visual Theme & Color Palette");
        themeTitle.setFont(UITheme.displayFont(Font.BOLD, 15));
        themeTitle.setForeground(UITheme.ICE);
        themeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(themeTitle);
        body.add(Box.createVerticalStrut(10));

        themeCardsPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        themeCardsPanel.setOpaque(false);
        themeCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rebuildThemeButtons();
        body.add(themeCardsPanel);
        body.add(Box.createVerticalStrut(22));

        // Sound Settings Section
        JLabel audioTitle = new JLabel("Audio & Sound Effects");
        audioTitle.setFont(UITheme.displayFont(Font.BOLD, 15));
        audioTitle.setForeground(UITheme.ICE);
        audioTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(audioTitle);
        body.add(Box.createVerticalStrut(10));

        JPanel audioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        audioRow.setOpaque(false);
        audioRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        sfxToggleBtn = UITheme.primaryButton(SoundUtil.isMuted() ? "SFX: Muted (Off)" : "SFX: Enabled (On)");
        sfxToggleBtn.setPreferredSize(new Dimension(170, UITheme.BTN_H_SM));
        sfxToggleBtn.addActionListener(e -> {
            SoundUtil.toggleMute();
            updateSfxButtonState();
            if (!SoundUtil.isMuted()) {
                SoundUtil.playClick();
            }
        });
        audioRow.add(sfxToggleBtn);

        JButton testSoundBtn = UITheme.ghostButton("Play Test Chime", UITheme.TEAL);
        testSoundBtn.setPreferredSize(new Dimension(150, UITheme.BTN_H_SM));
        testSoundBtn.addActionListener(e -> {
            if (!SoundUtil.isMuted()) {
                SoundUtil.playVictory();
            } else {
                JOptionPane.showMessageDialog(this, "Sound is currently muted! Enable SFX first.", "Sound Muted", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        audioRow.add(testSoundBtn);

        body.add(audioRow);
        content.add(body, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton closeBtn = UITheme.glowButton("Save & Close", UITheme.TEAL, UITheme.VIOLET);
        closeBtn.setPreferredSize(new Dimension(140, UITheme.BTN_H_SM));
        closeBtn.addActionListener(e -> {
            SoundUtil.playClick();
            dispose();
        });
        footer.add(closeBtn);

        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void updateSfxButtonState() {
        if (SoundUtil.isMuted()) {
            sfxToggleBtn.setText("SFX: Muted (Off)");
            sfxToggleBtn.setBackground(UITheme.CORAL_DARK);
        } else {
            sfxToggleBtn.setText("SFX: Enabled (On)");
            sfxToggleBtn.setBackground(UITheme.TEAL_DARK);
        }
        sfxToggleBtn.repaint();
    }

    private void rebuildThemeButtons() {
        themeCardsPanel.removeAll();
        UITheme.ThemePalette current = UITheme.getCurrentPalette();

        for (UITheme.ThemePalette palette : UITheme.ThemePalette.values()) {
            boolean isActive = (palette == current);
            JButton btn = createThemeCardButton(palette, isActive);
            themeCardsPanel.add(btn);
        }
        themeCardsPanel.revalidate();
        themeCardsPanel.repaint();
    }

    private JButton createThemeCardButton(UITheme.ThemePalette palette, boolean isActive) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.quality(g2);
                int w = getWidth();
                int h = getHeight();

                // Background gradient
                GradientPaint gp = new GradientPaint(0, 0, palette.gradient[0], w, h, palette.gradient[palette.gradient.length - 1]);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 16, 16));

                // Border
                if (isActive) {
                    g2.setColor(UITheme.GOLD);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 16, 16));
                } else {
                    g2.setColor(new Color(255, 255, 255, 45));
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 16, 16));
                }

                // Text
                g2.setFont(UITheme.displayFont(Font.BOLD, 13));
                g2.setColor(palette.isLight ? new Color(15, 30, 60) : Color.WHITE);
                g2.drawString(palette.displayName, 14, 28);

                // Subtitle / Active indicator
                g2.setFont(UITheme.bodyFont(Font.PLAIN, 11));
                if (isActive) {
                    g2.setColor(palette.isLight ? new Color(0, 110, 80) : UITheme.GOLD);
                    g2.drawString("[Active Theme]", 14, 48);
                } else {
                    g2.setColor(palette.isLight ? new Color(70, 90, 120) : UITheme.TEXT_MUTED);
                    g2.drawString(palette.isLight ? "Light Mode" : "Dark Mode", 14, 48);
                }

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(260, 68));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            SoundUtil.playClick();
            UITheme.setPalette(palette);
            rebuildThemeButtons();
            if (parent != null) {
                parent.repaint();
            }
        });

        return btn;
    }
}
