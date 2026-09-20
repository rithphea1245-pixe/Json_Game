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

        String titleStr = com.worldofwonder.util.I18n.get("daily_modal_title");
        JLabel title = new UITheme.GradientTextLabel(titleStr,
                UITheme.FONT_PAGE_TITLE, new Color(0xffdf70), new Color(0xff9900));
        title.setFont(UITheme.fontFor(titleStr, Font.BOLD, UITheme.FONT_PAGE_TITLE));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(6));

        String subStr = canClaim ? com.worldofwonder.util.I18n.get("daily_ready") : com.worldofwonder.util.I18n.get("daily_come_back");
        JLabel sub = UITheme.subtitle(subStr);
        sub.setFont(UITheme.fontFor(subStr, Font.PLAIN, UITheme.FONT_BODY));
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
            JPanel claimedBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
            claimedBadge.setOpaque(false);
            claimedBadge.add(UITheme.vectorIcon(UITheme.VectorIcon.CHECK, 18, UITheme.GREEN));
            String claimedText = com.worldofwonder.util.I18n.get("daily_claimed_today");
            JLabel lbl = new JLabel(claimedText);
            lbl.setFont(UITheme.fontFor(claimedText, Font.BOLD, 12));
            lbl.setForeground(UITheme.GREEN);
            claimedBadge.add(lbl);
            claimedBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
            chestIcon = claimedBadge;
        }
        center.add(chestIcon);
        center.add(Box.createVerticalStrut(12));

        String coinsText = com.worldofwonder.util.I18n.get("daily_coins_amount", DAILY_REWARD);
        JLabel rewardAmount = new JLabel(coinsText, SwingConstants.CENTER);
        rewardAmount.setFont(UITheme.fontFor(coinsText, Font.BOLD, 26));
        rewardAmount.setForeground(UITheme.GOLD);
        rewardAmount.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(rewardAmount);

        String descStr = canClaim ? com.worldofwonder.util.I18n.get("daily_desc") : com.worldofwonder.util.I18n.get("daily_come_back");
        JLabel desc = new JLabel(descStr, SwingConstants.CENTER);
        desc.setFont(UITheme.fontFor(descStr, Font.PLAIN, 13));
        desc.setForeground(UITheme.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(desc);

        content.add(center, BorderLayout.CENTER);

        // Footer Action Button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        footer.setOpaque(false);

        if (canClaim) {
            JButton claimBtn = UITheme.iconPillButton(UITheme.VectorIcon.GIFT, com.worldofwonder.util.I18n.get("daily_claim_button", DAILY_REWARD), UITheme.GOLD);
            UIUtil.fixedSize(claimBtn, 220, UITheme.BTN_H);
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

        String closeText = canClaim ? com.worldofwonder.util.I18n.get("btn_later") : com.worldofwonder.util.I18n.get("btn_close");
        JButton closeBtn = UITheme.ghostButton(closeText, UITheme.CORAL);
        closeBtn.setFont(UITheme.fontFor(closeText, Font.PLAIN, 14));
        UIUtil.fixedSize(closeBtn, 140, UITheme.BTN_H);
        closeBtn.addActionListener(e -> {
            SoundUtil.playClick();
            dispose();
        });
        footer.add(closeBtn);

        content.add(footer, BorderLayout.SOUTH);

        // Card Container - Opaque modalCard prevents background text bleed-through
        JPanel card = UITheme.modalCard(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(0xd4a020), 2, true));
        card.setPreferredSize(new Dimension(460, 360));
        card.add(content, BorderLayout.CENTER);

        setContentPane(card);
        pack();
        setLocationRelativeTo(parent);
    }
}
