package com.worldofwonder.view;

import com.worldofwonder.controller.GameController;
import com.worldofwonder.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/**
 * Modern modal dialog displaying global Top 10 players with a podium for top 3
 * and ranking list loaded from data/users.json.
 */
public class LeaderboardModal extends JDialog {

    public LeaderboardModal(JFrame parent, GameController gameController) {
        super(parent, "Hall of Fame - Top Wonderers", true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel(new BorderLayout(0, UITheme.GAP_SECTION));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // Header Title
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JComponent trophyIcon = UITheme.vectorIcon(UITheme.VectorIcon.TROPHY, 36, UITheme.GOLD);
        trophyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(trophyIcon);
        header.add(Box.createVerticalStrut(6));

        String titleStr = com.worldofwonder.util.I18n.get("leaderboard_title");
        JLabel title = new UITheme.GradientTextLabel(titleStr,
                UITheme.FONT_PAGE_TITLE + 2, new Color(0xffd700), new Color(0xffaa00));
        title.setFont(UITheme.fontFor(titleStr, Font.BOLD, UITheme.FONT_PAGE_TITLE + 2));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));

        String subStr = com.worldofwonder.util.I18n.get("leaderboard_sub");
        JLabel sub = UITheme.subtitle(subStr);
        sub.setFont(UITheme.fontFor(subStr, Font.PLAIN, UITheme.FONT_BODY));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(sub);
        content.add(header, BorderLayout.NORTH);

        // Fetch Top 10
        List<User> topUsers = gameController.getLeaderboard(10);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Podium for Top 3 (if at least 1 player exists)
        if (!topUsers.isEmpty()) {
            JPanel podiumPanel = buildPodium(topUsers);
            centerPanel.add(podiumPanel);
            centerPanel.add(Box.createVerticalStrut(16));
        }

        // List for remaining ranks (ranks 4..10)
        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < topUsers.size(); i++) {
            User u = topUsers.get(i);
            int rank = i + 1;
            JPanel row = buildUserRow(rank, u);
            listPanel.add(row);
            listPanel.add(Box.createVerticalStrut(6));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(520, 260));
        centerPanel.add(scrollPane);

        content.add(centerPanel, BorderLayout.CENTER);

        // Footer with Close Button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        JButton closeBtn = UITheme.iconPillButton(UITheme.VectorIcon.CHECK, com.worldofwonder.util.I18n.get("btn_got_it"), UITheme.VIOLET);
        UIUtil.fixedSize(closeBtn, 220, UITheme.BTN_H);
        closeBtn.addActionListener(e -> {
            SoundUtil.playClick();
            dispose();
        });
        footer.add(closeBtn);
        content.add(footer, BorderLayout.SOUTH);

        // Card Container - Opaque modalCard prevents background text bleed-through
        JPanel card = UITheme.modalCard(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(0x5a4890), 2, true));
        card.setPreferredSize(new Dimension(580, 560));
        card.add(content, BorderLayout.CENTER);

        setContentPane(card);
        pack();
        setLocationRelativeTo(parent);
    }

    private JPanel buildPodium(List<User> topUsers) {
        JPanel podium = new JPanel(new GridLayout(1, 3, 12, 0));
        podium.setOpaque(false);

        User first = topUsers.size() > 0 ? topUsers.get(0) : null;
        User second = topUsers.size() > 1 ? topUsers.get(1) : null;
        User third = topUsers.size() > 2 ? topUsers.get(2) : null;

        // Order: 2nd place (Silver), 1st place (Gold, taller), 3rd place (Bronze)
        podium.add(buildPodiumStep(2, second, new Color(0xd0d8e8), "2nd"));
        podium.add(buildPodiumStep(1, first, new Color(0xffd700), "1st"));
        podium.add(buildPodiumStep(3, third, new Color(0xcd7f32), "3rd"));

        return podium;
    }

    private JPanel buildPodiumStep(int rank, User user, Color accent, String label) {
        JPanel step = new JPanel();
        step.setOpaque(false);
        step.setLayout(new BoxLayout(step, BoxLayout.Y_AXIS));

        if (user == null) {
            JLabel empty = new JLabel("-", SwingConstants.CENTER);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            step.add(empty);
            return step;
        }

        JLabel crown = new JLabel(rank == 1 ? "#1" : (rank == 2 ? "#2" : "#3"), SwingConstants.CENTER);
        crown.setFont(UITheme.displayFont(Font.BOLD, rank == 1 ? 18 : 15));
        crown.setForeground(accent);
        crown.setAlignmentX(Component.CENTER_ALIGNMENT);
        step.add(crown);

        JLabel name = new JLabel(user.getUsername(), SwingConstants.CENTER);
        name.setFont(UITheme.displayFont(Font.BOLD, rank == 1 ? 16 : 14));
        name.setForeground(accent);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        step.add(name);

        JLabel pts = new JLabel(user.getTotalPoints() + " " + com.worldofwonder.util.I18n.get("pts"), SwingConstants.CENTER);
        pts.setFont(UITheme.bodyFont(Font.BOLD, 12));
        pts.setForeground(UITheme.GOLD);
        pts.setAlignmentX(Component.CENTER_ALIGNMENT);
        step.add(pts);

        return step;
    }

    private JPanel buildUserRow(int rank, User user) {
        JPanel row = UITheme.roundedBar();
        row.setLayout(new BorderLayout(12, 0));
        row.setPreferredSize(new Dimension(480, 42));
        row.setMaximumSize(new Dimension(500, 42));
        row.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        // Left: Rank & Name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel rankLabel = new JLabel("#" + rank);
        rankLabel.setFont(UITheme.displayFont(Font.BOLD, 14));
        rankLabel.setForeground(rank == 1 ? UITheme.GOLD : (rank <= 3 ? UITheme.TEAL : UITheme.TEXT_MUTED));
        left.add(rankLabel);

        JLabel nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(UITheme.bodyFont(Font.BOLD, 14));
        nameLabel.setForeground(UITheme.TEXT);
        left.add(nameLabel);

        JLabel rankBadge = new JLabel("(" + user.getRankTitle() + ")");
        rankBadge.setFont(UITheme.bodyFont(Font.ITALIC, 11));
        rankBadge.setForeground(UITheme.TEXT_MUTED);
        left.add(rankBadge);

        row.add(left, BorderLayout.WEST);

        // Right: Total points
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(UITheme.coin(18));

        JLabel ptsLabel = new JLabel(String.valueOf(user.getTotalPoints()));
        ptsLabel.setFont(UITheme.displayFont(Font.BOLD, 14));
        ptsLabel.setForeground(UITheme.GOLD);
        right.add(ptsLabel);

        row.add(right, BorderLayout.EAST);
        return row;
    }
}

