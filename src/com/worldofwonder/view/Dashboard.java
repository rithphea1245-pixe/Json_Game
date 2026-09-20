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
    private JPanel centerPanel;
    private JLabel scoreValue;
    private final Runnable langListener;

    public Dashboard(MainUI app) {
        super(new BorderLayout());
        this.app = app;
        setOpaque(false);

        this.content = new JPanel(new BorderLayout(0, UITheme.GAP_SECTION));
        content.setOpaque(false);
        this.topbar = buildTopbar();
        this.centerPanel = buildCenter();
        content.add(topbar, BorderLayout.NORTH);
        content.add(centerPanel, BorderLayout.CENTER);

        JPanel card = UITheme.card(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X, UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X));
        card.setPreferredSize(new Dimension(1160, 720));
        card.setMinimumSize(new Dimension(700, 480));
        card.add(content, BorderLayout.CENTER);

        JPanel root = UITheme.pageRoot(card);
        add(root, BorderLayout.CENTER);

        this.langListener = this::onLanguageChanged;
        com.worldofwonder.util.I18n.addLanguageListener(langListener);
    }

    private void onLanguageChanged() {
        content.removeAll();
        topbar = buildTopbar();
        centerPanel = buildCenter();
        content.add(topbar, BorderLayout.NORTH);
        content.add(centerPanel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
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

        String titleText = com.worldofwonder.util.I18n.get("choose_game_title");
        javax.swing.JLabel title = new UITheme.GradientTextLabel(titleText,
                UITheme.FONT_PAGE_TITLE + 4, new java.awt.Color(0xe8f4ff), new java.awt.Color(0xc8b0ff));
        title.setFont(UITheme.fontFor(titleText, Font.BOLD, UITheme.FONT_PAGE_TITLE + 4));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(title);
        center.add(Box.createVerticalStrut(UITheme.GAP_TIGHT));

        String subText = com.worldofwonder.util.I18n.get("choose_game_sub");
        JLabel subtitle = UITheme.subtitle(subText);
        subtitle.setFont(UITheme.fontFor(subText, Font.PLAIN, UITheme.FONT_BODY + 1));
        subtitle.setForeground(new java.awt.Color(0xb8c8e8));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(subtitle);
        center.add(Box.createVerticalStrut(UITheme.GAP_SECTION));

        JPanel games = new JPanel(new GridLayout(2, 2, UITheme.GAP_SECTION, UITheme.GAP_SECTION));
        games.setOpaque(false);

        games.add(gameCard("quiz", UITheme.GameIcon.QUIZ,
                com.worldofwonder.util.I18n.get("game_quiz_title"),
                com.worldofwonder.util.I18n.get("game_quiz_sub"), UITheme.VIOLET));
        games.add(gameCard("wordsearch", UITheme.GameIcon.WORDSEARCH,
                com.worldofwonder.util.I18n.get("game_wordsearch_title"),
                com.worldofwonder.util.I18n.get("game_wordsearch_sub"), UITheme.PINK));
        games.add(gameCard("cups", UITheme.GameIcon.CUPS,
                com.worldofwonder.util.I18n.get("game_cups_title"),
                com.worldofwonder.util.I18n.get("game_cups_sub"), UITheme.TEAL));
        games.add(gameCard("words", UITheme.GameIcon.WORDS,
                com.worldofwonder.util.I18n.get("game_words_title"),
                com.worldofwonder.util.I18n.get("game_words_sub"), UITheme.GOLD));

        center.add(games);
        return center;
    }

    private JPanel buildTopbar() {
        JPanel bar = UITheme.roundedBar();

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(UITheme.avatar(isGuest ? "?" : username));

        JPanel userTextBox = new JPanel();
        userTextBox.setOpaque(false);
        userTextBox.setLayout(new BoxLayout(userTextBox, BoxLayout.Y_AXIS));

        String welcomeText = com.worldofwonder.util.I18n.get("welcome_user", isGuest ? "Guest" : username);
        JLabel welcomeLabel = new JLabel(welcomeText);
        welcomeLabel.setFont(UITheme.fontFor(welcomeText, Font.BOLD, UITheme.FONT_CARD_TITLE - 2));
        welcomeLabel.setForeground(UITheme.TEXT);
        userTextBox.add(welcomeLabel);

        String rankName;
        if (totalPoints >= 600) rankName = com.worldofwonder.util.I18n.get("rank_legendary");
        else if (totalPoints >= 300) rankName = com.worldofwonder.util.I18n.get("rank_gold");
        else if (totalPoints >= 150) rankName = com.worldofwonder.util.I18n.get("rank_silver");
        else if (totalPoints >= 50) rankName = com.worldofwonder.util.I18n.get("rank_bronze");
        else rankName = com.worldofwonder.util.I18n.get("rank_novice");

        String rankText = com.worldofwonder.util.I18n.get("rank_prefix", rankName);
        JLabel rankLabel = new JLabel(rankText);
        rankLabel.setFont(UITheme.fontFor(rankText, Font.PLAIN, 11));
        rankLabel.setForeground(new java.awt.Color(0xb0c4de));
        userTextBox.add(rankLabel);

        left.add(userTextBox);

        // Language toggle button cleanly grouped right beside user profile
        String langLabel = com.worldofwonder.util.I18n.isKhmer() ? "ភាសាខ្មែរ" : "English";
        JButton langBtn = UITheme.iconPillButton(UITheme.VectorIcon.GLOBE, langLabel, UITheme.GOLD);
        UIUtil.fixedSize(langBtn, 105, UITheme.BTN_H);
        langBtn.setFont(UITheme.fontFor(langLabel, Font.BOLD, 12));
        langBtn.setToolTipText(com.worldofwonder.util.I18n.get("tip_language"));
        langBtn.addActionListener(e -> {
            SoundUtil.playClick();
            com.worldofwonder.util.I18n.toggleLanguage();
        });
        left.add(Box.createHorizontalStrut(6));
        left.add(langBtn);

        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        // Settings Button with Gear Icon
        JButton settingsBtn = UITheme.iconPillButton(UITheme.VectorIcon.GEAR, com.worldofwonder.util.I18n.get("settings"), UITheme.TEAL);
        settingsBtn.setFont(UITheme.fontFor(settingsBtn.getText(), Font.BOLD, 12));
        UIUtil.fixedSize(settingsBtn, 95, UITheme.BTN_H);
        settingsBtn.setToolTipText("Change visual theme, dark/light mode, and sound");
        settingsBtn.addActionListener(e -> {
            SoundUtil.playClick();
            new SettingsModal(app).setVisible(true);
        });
        right.add(settingsBtn);

        // Daily Bonus Chest Button with Gift Icon
        JButton dailyBtn = UITheme.iconPillButton(UITheme.VectorIcon.GIFT, com.worldofwonder.util.I18n.get("daily_gift"), UITheme.GOLD);
        dailyBtn.setFont(UITheme.fontFor(dailyBtn.getText(), Font.BOLD, 12));
        UIUtil.fixedSize(dailyBtn, 100, UITheme.BTN_H);
        dailyBtn.setToolTipText("Open daily treasure chest for +50 coins");
        dailyBtn.addActionListener(e -> {
            SoundUtil.playClick();
            new DailyRewardModal(app, this, app.getGameController()).setVisible(true);
        });
        right.add(dailyBtn);

        // Hall of Fame / Leaderboard Button with Trophy Icon
        JButton leadBtn = UITheme.iconPillButton(UITheme.VectorIcon.TROPHY, com.worldofwonder.util.I18n.get("hall_of_fame"), UITheme.VIOLET);
        leadBtn.setFont(UITheme.fontFor(leadBtn.getText(), Font.BOLD, 12));
        UIUtil.fixedSize(leadBtn, 110, UITheme.BTN_H);
        leadBtn.setToolTipText("View global top players and podium");
        leadBtn.addActionListener(e -> {
            SoundUtil.playClick();
            new LeaderboardModal(app, app.getGameController()).setVisible(true);
        });
        right.add(leadBtn);

        // Admin Control Panel Button with Shield Icon
        boolean isAdmin = !isGuest && (app.getGameController().isUserAdmin(userId) || app.getGameController().isUserAdmin(username));
        if (isAdmin) {
            JButton adminBtn = UITheme.iconPillButton(UITheme.VectorIcon.SHIELD, com.worldofwonder.util.I18n.get("admin_panel"), UITheme.CORAL);
            adminBtn.setFont(UITheme.fontFor(adminBtn.getText(), Font.BOLD, 12));
            UIUtil.fixedSize(adminBtn, 110, UITheme.BTN_H);
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

        String pointsLabelText = com.worldofwonder.util.I18n.get("total_points");
        JLabel scoreLabel = new JLabel(pointsLabelText, SwingConstants.RIGHT);
        scoreLabel.setFont(UITheme.fontFor(pointsLabelText, Font.BOLD, UITheme.FONT_BADGE));
        scoreLabel.setForeground(UITheme.TEXT_MUTED);
        scoreLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        scoreBox.add(scoreLabel);

        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        scoreRow.setOpaque(false);
        scoreRow.add(UITheme.coin(22));

        JLabel scoreValueLocal = new JLabel(String.valueOf(totalPoints), SwingConstants.RIGHT);
        scoreValueLocal.setFont(UITheme.displayFont(Font.BOLD, UITheme.FONT_CARD_TITLE - 2));
        scoreValueLocal.setForeground(UITheme.GOLD);
        scoreRow.add(scoreValueLocal);
        scoreRow.setAlignmentX(Component.RIGHT_ALIGNMENT);
        scoreBox.add(scoreRow);
        this.scoreValue = scoreValueLocal;

        right.add(scoreBox);

        // Logout button with Logout vector icon
        JButton logout = UITheme.iconPillButton(UITheme.VectorIcon.LOGOUT, com.worldofwonder.util.I18n.get("logout"), UITheme.CORAL);
        logout.setFont(UITheme.fontFor(logout.getText(), Font.BOLD, 12));
        UIUtil.fixedSize(logout, 88, UITheme.BTN_H);
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
