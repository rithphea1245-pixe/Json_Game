package com.worldofwonder.view;

import com.worldofwonder.model.*;
import com.worldofwonder.controller.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class Dashboard extends JPanel {

    private final MainUI app;

    private String username = "Guest";
    private boolean isGuest = true;
    private int userId = 0;
    private String token = null;
    private int totalPoints = 0;

    private final JPanel content;
    private JPanel topbar;
    private JLabel scoreValue;

    public Dashboard(MainUI app) {
        super(new BorderLayout());
        this.app = app;
        setOpaque(false);

        this.content = new JPanel(new BorderLayout(0, UITheme.GAP_SECTION));
        content.setOpaque(false);
        this.topbar = buildTopbar();
        content.add(topbar, BorderLayout.NORTH);
        content.add(buildCenter(), BorderLayout.CENTER);

        JPanel card = UITheme.card(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X, UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X));
        card.setPreferredSize(new Dimension(1040, 710));
        card.setMinimumSize(new Dimension(500, 480));
        card.add(content, BorderLayout.CENTER);

        JPanel root = UITheme.pageRoot(card);
        add(root, BorderLayout.CENTER);
    }

    public void setUser(String username, boolean isGuest, int userId, String token, int totalPoints) {
        this.username = username;
        this.isGuest = isGuest;
        this.userId = userId;
        this.token = token;
        this.totalPoints = totalPoints;
        content.remove(topbar);
        topbar = buildTopbar();
        content.add(topbar, BorderLayout.NORTH);
        content.revalidate();
        content.repaint();
    }

    public int getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void updateScore(int newTotal) {
        this.totalPoints = newTotal;
        if (scoreValue != null) {
            scoreValue.setText(String.valueOf(newTotal));
        }
    }

    public void addGamePoints(int pointsEarned) {
        if (userId > 0 && pointsEarned > 0) {
            int newTotal = app.getGameController().addPoints(userId, pointsEarned);
            updateScore(newTotal);
        }
    }

    public MainUI getApp() {
        return app;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        javax.swing.JLabel title = new UITheme.GradientTextLabel("Choose your game",
                UITheme.FONT_PAGE_TITLE + 4, new java.awt.Color(0xe8f4ff), new java.awt.Color(0xc8b0ff));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(title);
        center.add(Box.createVerticalStrut(UITheme.GAP_TIGHT));

        JLabel subtitle = UITheme.subtitle("Pick a destination and start earning points");
        subtitle.setFont(UITheme.bodyFont(Font.PLAIN, UITheme.FONT_BODY + 1));
        subtitle.setForeground(new java.awt.Color(0xb8c8e8));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(subtitle);
        center.add(Box.createVerticalStrut(UITheme.GAP_SECTION));

        JPanel games = new JPanel(new GridLayout(2, 2, UITheme.GAP_SECTION, UITheme.GAP_SECTION));
        games.setOpaque(false);

        games.add(gameCard("quiz", UITheme.GameIcon.QUIZ, "World of Wonder Quiz",
                "Travel the world, answer questions, and earn points on every stop.", UITheme.VIOLET));
        games.add(gameCard("wordsearch", UITheme.GameIcon.WORDSEARCH, "Word Search Puzzles",
                "Hunt for hidden words in a letter grid and earn points on every find.", UITheme.PINK));
        games.add(gameCard("cups", UITheme.GameIcon.CUPS, "Cups - Water Sort",
                "Pour the colored water until every cup holds a single color.", UITheme.TEAL));
        games.add(gameCard("words", UITheme.GameIcon.WORDS, "Words of Wonders",
                "Connect letters, find hidden words, and complete crossword puzzles.", UITheme.GOLD));

        center.add(games);
        return center;
    }

    private JPanel buildTopbar() {
        JPanel bar = UITheme.roundedBar();

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(UITheme.avatar(isGuest ? "?" : username));

        JPanel userTextBox = new JPanel();
        userTextBox.setOpaque(false);
        userTextBox.setLayout(new BoxLayout(userTextBox, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Welcome, " + (isGuest ? "Guest" : username));
        welcomeLabel.setFont(UITheme.displayFont(Font.BOLD, UITheme.FONT_CARD_TITLE - 1));
        welcomeLabel.setForeground(UITheme.TEXT);
        userTextBox.add(welcomeLabel);

        String rank = "Novice Explorer";
        if (totalPoints >= 600) rank = "Legendary Wonderer";
        else if (totalPoints >= 300) rank = "Gold Master";
        else if (totalPoints >= 150) rank = "Silver Scholar";
        else if (totalPoints >= 50) rank = "Bronze Adventurer";

        JLabel rankLabel = new JLabel("Rank: " + rank);
        rankLabel.setFont(UITheme.bodyFont(Font.ITALIC, 11));
        rankLabel.setForeground(new java.awt.Color(0xb0c4de));
        userTextBox.add(rankLabel);

        left.add(userTextBox);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Settings Button
        JButton settingsBtn = UITheme.ghostButton("Settings", UITheme.TEAL);
        UIUtil.fixedSize(settingsBtn, 95, UITheme.BTN_H);
        settingsBtn.setToolTipText("Change visual theme, dark/light mode, and sound");
        settingsBtn.addActionListener(e -> {
            SoundUtil.playClick();
            new SettingsModal(app).setVisible(true);
        });
        right.add(settingsBtn);

        // Daily Bonus Chest Button
        JButton dailyBtn = UITheme.ghostButton("★ Daily Gift", UITheme.GOLD);
        UIUtil.fixedSize(dailyBtn, 110, UITheme.BTN_H);
        dailyBtn.setToolTipText("Open daily treasure chest for +50 coins");
        dailyBtn.addActionListener(e -> {
            SoundUtil.playClick();
            new DailyRewardModal(app, this, app.getGameController()).setVisible(true);
        });
        right.add(dailyBtn);

        // Hall of Fame / Leaderboard Button
        JButton leadBtn = UITheme.ghostButton("★ Hall of Fame", UITheme.VIOLET);
        UIUtil.fixedSize(leadBtn, 130, UITheme.BTN_H);
        leadBtn.setToolTipText("View global top players and podium");
        leadBtn.addActionListener(e -> {
            SoundUtil.playClick();
            new LeaderboardModal(app, app.getGameController()).setVisible(true);
        });
        right.add(leadBtn);

        // Admin Control Panel Button (Only visible if user has Admin role!)
        boolean isAdmin = !isGuest && (app.getGameController().isUserAdmin(userId) || app.getGameController().isUserAdmin(username));
        if (isAdmin) {
            JButton adminBtn = UITheme.primaryButton("Admin Panel");
            adminBtn.setBackground(UITheme.CORAL);
            UIUtil.fixedSize(adminBtn, 120, UITheme.BTN_H);
            adminBtn.setToolTipText("Manage users, adjust points, and toggle roles");
            adminBtn.addActionListener(e -> {
                SoundUtil.playClick();
                new AdminControlModal(app, this, app.getGameController(), username).setVisible(true);
            });
            right.add(adminBtn);
        }

        // Score display box
        JPanel scoreBox = new JPanel();
        scoreBox.setOpaque(false);
        scoreBox.setLayout(new BoxLayout(scoreBox, BoxLayout.Y_AXIS));

        JLabel scoreLabel = new JLabel("Total Points", SwingConstants.RIGHT);
        scoreLabel.setFont(UITheme.displayFont(Font.BOLD, UITheme.FONT_BADGE));
        scoreLabel.setForeground(UITheme.TEXT_MUTED);
        scoreLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        scoreBox.add(scoreLabel);

        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        scoreRow.setOpaque(false);
        scoreRow.add(UITheme.coin(24));

        JLabel scoreValueLocal = new JLabel(String.valueOf(totalPoints), SwingConstants.RIGHT);
        scoreValueLocal.setFont(UITheme.displayFont(Font.BOLD, UITheme.FONT_CARD_TITLE));
        scoreValueLocal.setForeground(UITheme.GOLD);
        scoreRow.add(scoreValueLocal);
        scoreRow.setAlignmentX(Component.RIGHT_ALIGNMENT);
        scoreBox.add(scoreRow);
        this.scoreValue = scoreValueLocal;

        right.add(scoreBox);

        JButton logout = UITheme.ghostButton("Logout", UITheme.CORAL);
        UIUtil.fixedSize(logout, 90, UITheme.BTN_H);
        logout.addActionListener(e -> logout());
        right.add(logout);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JButton gameCard(String cardName, UITheme.GameIcon icon, String label, String subtitle, java.awt.Color accent) {
        UITheme.GameModeCard card = new UITheme.GameModeCard(icon, label, subtitle, accent);
        if ("quiz".equals(cardName)) {
            card.setBand(new java.awt.Color(0x1a1050), new java.awt.Color(0x3a25a0));
        } else if ("wordsearch".equals(cardName)) {
            card.setBand(new java.awt.Color(0x4a1040), new java.awt.Color(0xd04080));
        } else if ("words".equals(cardName)) {
            card.setBand(new java.awt.Color(0x2a1a00), new java.awt.Color(0xd4a020));
        } else {
            card.setBand(new java.awt.Color(0x0a2848), new java.awt.Color(0x20d0c0));
        }
        UIUtil.flexSize(card, 420, 225, 260, Integer.MAX_VALUE);
        card.addActionListener(e -> app.showScreen(cardName));
        return card;
    }

    private void logout() {
        app.showWelcome();
    }

    public void showScreen(String cardName) {
        app.showScreen(cardName);
    }

    public void showDashboard() {
        app.showDashboard();
    }
}
