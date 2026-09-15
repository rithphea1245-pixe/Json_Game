package com.worldofwonder.view;

import com.worldofwonder.controller.GameController;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Modern modal dialog for claiming daily rewards with chest celebration.
 */
public class DailyRewardModal extends JDialog {

    private static final int DAILY_REWARD = 50;

    public DailyRewardModal(JFrame parent, Dashboard dashboard, GameController gameController) {
        super(parent, "Daily Wonder Reward", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        int userId = dashboard.getUserId();
        boolean canClaim = gameController.canClaimDailyBonus(userId);

        JPanel content = new JPanel(new BorderLayout(0, UITheme.GAP_SECTION));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        // Header Title
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new UITheme.GradientTextLabel("Daily Treasure Chest",
                UITheme.FONT_PAGE_TITLE, new Color(0xffdf70), new Color(0xff9900));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(6));

        JLabel sub = UITheme.subtitle(canClaim ? "Your daily gift is ready to open!" : "You have already claimed today's treasure!");
        sub.setFont(UITheme.bodyFont(Font.PLAIN, UITheme.FONT_BODY));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(sub);
        content.add(header, BorderLayout.NORTH);

        // Center: Big Chest & Reward Info
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JComponent chestIcon;
        if (canClaim) {
            chestIcon = UITheme.coin(64);
            chestIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        } else {
            JLabel claimedBadge = UITheme.badge("✓ CLAIMED TODAY", UITheme.GREEN);
            claimedBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
            chestIcon = claimedBadge;
        }
        center.add(chestIcon);
        center.add(Box.createVerticalStrut(12));

        JLabel rewardAmount = new JLabel("+" + DAILY_REWARD + " Coins", SwingConstants.CENTER);
        rewardAmount.setFont(UITheme.displayFont(Font.BOLD, 26));
        rewardAmount.setForeground(UITheme.GOLD);
        rewardAmount.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(rewardAmount);

        JLabel desc = new JLabel(canClaim ? "Log in every day to collect free coins!" : "Come back tomorrow for your next reward!", SwingConstants.CENTER);
        desc.setFont(UITheme.bodyFont(Font.PLAIN, 13));
        desc.setForeground(UITheme.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(desc);

        content.add(center, BorderLayout.CENTER);

        // Footer Action Button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        footer.setOpaque(false);

        if (canClaim) {
            JButton claimBtn = UITheme.accentButton("Claim Now! (+50)", UITheme.GOLD);
            UIUtil.fixedSize(claimBtn, 200, UITheme.BTN_H);
            claimBtn.addActionListener(e -> {
                SoundUtil.playVictory();
                int newTotal = gameController.claimDailyBonus(userId, DAILY_REWARD);
                if (newTotal > 0) {
                    dashboard.updateScore(newTotal);
                } else if (userId <= 0) {
                    dashboard.addGamePoints(DAILY_REWARD);
                }
                dispose();
            });
            footer.add(claimBtn);
        }

        JButton closeBtn = UITheme.ghostButton(canClaim ? "Later" : "Close", UITheme.CORAL);
        UIUtil.fixedSize(closeBtn, 140, UITheme.BTN_H);
        closeBtn.addActionListener(e -> {
            SoundUtil.playClick();
            dispose();
        });
        footer.add(closeBtn);

        content.add(footer, BorderLayout.SOUTH);

        // Card Container
        JPanel card = UITheme.card(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(0xd4a020), 2, true));
        card.setPreferredSize(new Dimension(460, 360));
        card.add(content, BorderLayout.CENTER);

        setContentPane(card);
        pack();
        setLocationRelativeTo(parent);
    }
}
