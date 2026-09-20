package com.worldofwonder.view;

import com.worldofwonder.model.*;
import com.worldofwonder.controller.*;

import com.worldofwonder.model.Level;
import com.worldofwonder.model.Question;
import com.worldofwonder.model.World;

import com.worldofwonder.util.I18n;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QuizGameScreen extends JPanel {

    private static final String PANEL_WORLDS = "worlds";
    private static final String PANEL_LEVELS = "levels";
    private static final String PANEL_QUIZ = "quiz";
    private static final String PANEL_COMPLETE = "complete";
    private static final char[] OPTION_LETTERS = {'A', 'B', 'C', 'D'};
    private static final int POINTS_PER_QUESTION = 10;

    private final Dashboard dashboard;
    private final SampleQuizData sample;

    private final CardLayout cards;
    private final JPanel content;

    private List<World> worlds = new ArrayList<>();
    private List<Level> levels = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();
    private int questionIndex;
    private int score;
    private int points;
    private int selectedAnswer = -1;
    private Level currentLevel;
    private boolean sampleMode;

    private JPanel worldsList;
    private JLabel statusLabel;
    private JLabel levelsTitle;
    private JPanel levelsList;
    private JLabel quizLevelName;
    private JLabel progressLabel;
    private UITheme.ProgressBar progressBar;
    private JLabel questionText;
    private JPanel optionsPanel;
    private JButton hintButton;
    private UITheme.HintBox hintLabel;
    private JLabel feedbackLabel;
    private JButton submitButton;
    private JButton nextButton;
    private JPanel completePanel;
    private UITheme.GradientTextLabel completeTitle;
    private JLabel completeText;
    private UITheme.Confetti confetti;

    // Real-Life Game Mode Features (Kahoot & Trivia Crack)
    private javax.swing.Timer questionTimer;
    private int secondsLeft = 15;
    private UITheme.ProgressBar timerBar;
    private JLabel timerText;
    private int streak = 0;
    private JLabel streakLabel;
    private JButton fiftyFiftyBtn;
    private boolean fiftyFiftyUsed = false;
    private JButton freezeBtn;
    private boolean freezeUsed = false;
    private JButton skipBtn;
    private boolean skipUsed = false;
    private JButton onlineTriviaBtn;
    private JLabel titleLbl;
    private JButton topBackBtn;
    private JLabel worldsTitle;
    private JLabel worldsSub;
    private JLabel levelsSub;
    private JButton levelsBackBtn;
    private JLabel completeSub;
    private JButton againBtn;
    private JButton levelsCompleteBtn;
    private JButton worldsCompleteBtn;
    private JButton exitBtn;
    private World currentWorld;
    private final Runnable langListener = this::refreshLanguage;

    public QuizGameScreen(Dashboard dashboard) {
        super(new BorderLayout());
        this.dashboard = dashboard;
        this.sample = new SampleQuizData();
        this.cards = new CardLayout();
        this.content = new JPanel(cards);
        content.setOpaque(false);
        setOpaque(false);

        content.add(buildWorldsPanel(), PANEL_WORLDS);
        content.add(buildLevelsPanel(), PANEL_LEVELS);
        content.add(buildQuizPanel(), PANEL_QUIZ);
        content.add(buildCompletePanel(), PANEL_COMPLETE);

        JPanel card = UITheme.card(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X, UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X));
        UIUtil.fixedSize(card, 960, 740);

        JButton back = UITheme.backButton(com.worldofwonder.util.I18n.get("back_to_games"), UITheme.CORAL);
        UIUtil.fixedSize(back, 180, UITheme.BTN_H);
        back.addActionListener(e -> {
            stopTimer();
            dashboard.showDashboard();
        });
        this.topBackBtn = back;
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 12, 2));
        topBar.add(back, BorderLayout.WEST);

        this.titleLbl = UITheme.title(com.worldofwonder.util.I18n.get("game_quiz_title"), 24);
        topBar.add(titleLbl, BorderLayout.CENTER);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        east.setOpaque(false);

        JButton soundBtn = UITheme.ghostButton(SoundUtil.isMuted() ? "SFX: OFF" : "SFX: ON", UITheme.TEAL);
        UIUtil.fixedSize(soundBtn, 95, UITheme.BTN_H_SM);
        soundBtn.setToolTipText("Toggle Sound Effects");
        soundBtn.addActionListener(e -> {
            SoundUtil.toggleMute();
            soundBtn.setText(SoundUtil.isMuted() ? "SFX: OFF" : "SFX: ON");
            if (!SoundUtil.isMuted()) {
                SoundUtil.playClick();
            }
        });
        east.add(soundBtn);
        topBar.add(east, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        JPanel root = UITheme.pageRoot(card);
        UITheme.autoScale(root, 1040, 830, 0.85, 1.5);
        add(root, BorderLayout.CENTER);

        cards.show(content, PANEL_WORLDS);
        loadWorlds();
        I18n.addLanguageListener(langListener);
    }

    /** Called when language changes - refreshes all dynamic labels in the quiz screen. */
    private void refreshLanguage() {
        if (titleLbl != null) {
            titleLbl.setText(com.worldofwonder.util.I18n.get("game_quiz_title"));
            titleLbl.setFont(UITheme.fontFor(titleLbl.getText(), Font.BOLD, 24));
        }
        if (topBackBtn != null) {
            topBackBtn.setText(com.worldofwonder.util.I18n.get("back_to_games"));
            topBackBtn.setFont(UITheme.fontFor(topBackBtn.getText(), Font.PLAIN, UITheme.FONT_BUTTON));
        }
        if (worldsTitle != null) {
            worldsTitle.setText(com.worldofwonder.util.I18n.get("quiz_choose_world"));
            worldsTitle.setFont(UITheme.fontFor(worldsTitle.getText(), Font.BOLD, UITheme.FONT_PAGE_TITLE));
        }
        if (worldsSub != null) {
            worldsSub.setText(com.worldofwonder.util.I18n.get("quiz_choose_world_sub"));
            worldsSub.setFont(UITheme.fontFor(worldsSub.getText(), Font.PLAIN, UITheme.FONT_BODY));
        }
        if (levelsTitle != null && currentWorld != null) {
            levelsTitle.setText(com.worldofwonder.util.I18n.get("quiz_levels_title", getWorldDisplayName(currentWorld)));
            levelsTitle.setFont(UITheme.fontFor(levelsTitle.getText(), Font.BOLD, UITheme.FONT_PAGE_TITLE));
        } else if (levelsTitle != null) {
            levelsTitle.setText(com.worldofwonder.util.I18n.get("quiz_select_level"));
            levelsTitle.setFont(UITheme.fontFor(levelsTitle.getText(), Font.BOLD, UITheme.FONT_PAGE_TITLE));
        }
        if (levelsSub != null) {
            levelsSub.setText(com.worldofwonder.util.I18n.get("quiz_choose_level_sub"));
            levelsSub.setFont(UITheme.fontFor(levelsSub.getText(), Font.PLAIN, UITheme.FONT_BODY));
        }
        if (levelsBackBtn != null) {
            levelsBackBtn.setText(com.worldofwonder.util.I18n.get("quiz_select_world"));
            levelsBackBtn.setFont(UITheme.fontFor(levelsBackBtn.getText(), Font.PLAIN, UITheme.FONT_BUTTON));
        }
        if (quizLevelName != null && currentLevel != null) {
            quizLevelName.setText(getLevelDisplayName(currentLevel));
            quizLevelName.setFont(UITheme.fontFor(quizLevelName.getText(), Font.BOLD, UITheme.FONT_SECTION));
        }
        if (hintButton != null) {
            hintButton.setText(com.worldofwonder.util.I18n.get("quiz_hint"));
            hintButton.setFont(UITheme.fontFor(hintButton.getText(), Font.BOLD, UITheme.FONT_BUTTON));
        }
        if (fiftyFiftyBtn != null) {
            fiftyFiftyBtn.setText(com.worldofwonder.util.I18n.get("quiz_lifeline"));
            fiftyFiftyBtn.setFont(UITheme.fontFor(fiftyFiftyBtn.getText(), Font.BOLD, UITheme.FONT_BUTTON));
        }
        if (submitButton != null) {
            submitButton.setText(com.worldofwonder.util.I18n.get("quiz_submit"));
            submitButton.setFont(UITheme.fontFor(submitButton.getText(), Font.BOLD, UITheme.FONT_BUTTON));
        }
        if (nextButton != null) {
            if (questions != null && questionIndex == questions.size() - 1) {
                nextButton.setText(com.worldofwonder.util.I18n.get("quiz_view_results"));
            } else {
                nextButton.setText(com.worldofwonder.util.I18n.get("quiz_next_question"));
            }
            nextButton.setFont(UITheme.fontFor(nextButton.getText(), Font.BOLD, UITheme.FONT_BUTTON));
        }
        if (exitBtn != null) {
            exitBtn.setText(com.worldofwonder.util.I18n.get("exit_to_games"));
            exitBtn.setFont(UITheme.fontFor(exitBtn.getText(), Font.BOLD, UITheme.FONT_BUTTON));
        }
        if (completeTitle != null) {
            completeTitle.setText(com.worldofwonder.util.I18n.get("quiz_victory_title"));
        }
        if (completeSub != null) {
            completeSub.setText(com.worldofwonder.util.I18n.get("tagline"));
            completeSub.setFont(UITheme.fontFor(completeSub.getText(), Font.PLAIN, UITheme.FONT_BODY));
        }
        if (againBtn != null) {
            againBtn.setText(com.worldofwonder.util.I18n.get("quiz_play_again"));
            againBtn.setFont(UITheme.fontFor(againBtn.getText(), Font.BOLD, UITheme.FONT_BUTTON));
        }
        if (levelsCompleteBtn != null) {
            levelsCompleteBtn.setText(com.worldofwonder.util.I18n.get("quiz_back_levels"));
            levelsCompleteBtn.setFont(UITheme.fontFor(levelsCompleteBtn.getText(), Font.BOLD, UITheme.FONT_BUTTON));
        }
        if (worldsCompleteBtn != null) {
            worldsCompleteBtn.setText(com.worldofwonder.util.I18n.get("quiz_another_world"));
            worldsCompleteBtn.setFont(UITheme.fontFor(worldsCompleteBtn.getText(), Font.PLAIN, UITheme.FONT_BUTTON));
        }
        if (timerText != null) {
            updateTimerDisplay();
        }
        if (streakLabel != null) {
            updateStreakDisplay();
        }
        renderWorlds();
        if (currentLevel != null && levels != null && !levels.isEmpty()) {
            renderLevels();
        }
        if (questions != null && !questions.isEmpty() && questionIndex < questions.size()) {
            renderQuestion();
        }
        revalidate();
        repaint();
    }

    private JPanel buildWorldsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        worldsTitle = UITheme.title(com.worldofwonder.util.I18n.get("quiz_choose_world"), UITheme.FONT_PAGE_TITLE);
        worldsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(worldsTitle);
        header.add(Box.createVerticalStrut(4));
        worldsSub = UITheme.subtitle(com.worldofwonder.util.I18n.get("quiz_choose_world_sub"));
        worldsSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(worldsSub);
        header.add(Box.createVerticalStrut(6));
        statusLabel = UITheme.sectionTitle(" ", UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(statusLabel);
        header.add(Box.createVerticalStrut(8));
        panel.add(header, BorderLayout.NORTH);

        worldsList = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 14));
        worldsList.setOpaque(false);
        panel.add(worldsList, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLevelsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        levelsTitle = UITheme.sectionTitle(com.worldofwonder.util.I18n.get("quiz_select_level"), UITheme.FONT_PAGE_TITLE);
        levelsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(levelsTitle);
        header.add(Box.createVerticalStrut(4));
        levelsSub = UITheme.subtitle(com.worldofwonder.util.I18n.get("quiz_choose_level_sub"));
        levelsSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(levelsSub);
        header.add(Box.createVerticalStrut(8));
        levelsBackBtn = UITheme.backButton(com.worldofwonder.util.I18n.get("quiz_select_world"), UITheme.TEXT_MUTED);
        UIUtil.fixedSize(levelsBackBtn, 220, UITheme.BTN_H_SM);
        levelsBackBtn.addActionListener(e -> showWorlds());
        levelsBackBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(levelsBackBtn);
        header.add(Box.createVerticalStrut(4));
        panel.add(header, BorderLayout.NORTH);

        levelsList = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 14));
        levelsList.setOpaque(false);
        panel.add(levelsList, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        quizLevelName = UITheme.sectionTitle("", UITheme.FONT_SECTION);
        quizLevelName.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(quizLevelName);
        progressLabel = new JLabel(" ", SwingConstants.CENTER);
        progressLabel.setFont(UITheme.bodyFont(Font.BOLD, UITheme.FONT_BODY));
        progressLabel.setForeground(UITheme.TEXT_MUTED);
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(Box.createVerticalStrut(4));
        header.add(progressLabel);
        progressBar = new UITheme.ProgressBar();
        progressBar.setPreferredSize(new Dimension(640, 14));
        progressBar.setMaximumSize(new Dimension(640, 14));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(Box.createVerticalStrut(6));
        header.add(progressBar);

        // Real-Life Timer & Streak Bar
        JPanel timerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        timerRow.setOpaque(false);

        timerText = new JLabel("[T] 15s");
        timerText.setFont(UITheme.displayFont(Font.BOLD, 15));
        timerText.setForeground(UITheme.GREEN);
        timerRow.add(timerText);

        streakLabel = new JLabel(I18n.get("quiz_streak", 0));
        streakLabel.setFont(UITheme.displayFont(Font.BOLD, 15));
        streakLabel.setForeground(new Color(0xff7744));
        timerRow.add(streakLabel);

        header.add(Box.createVerticalStrut(6));
        header.add(timerRow);

        timerBar = new UITheme.ProgressBar();
        timerBar.setPreferredSize(new Dimension(640, 8));
        timerBar.setMaximumSize(new Dimension(640, 8));
        timerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(Box.createVerticalStrut(4));
        header.add(timerBar);

        panel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        questionText = new JLabel(" ", SwingConstants.CENTER);
        questionText.setFont(UITheme.bodyFont(Font.BOLD, UITheme.FONT_CARD_TITLE));
        questionText.setForeground(UITheme.TEXT);
        questionText.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionText.setBorder(BorderFactory.createEmptyBorder(8, 30, 4, 30));
        center.add(questionText);
        center.add(Box.createVerticalStrut(14));
        optionsPanel = new UITheme.SlidePanel(new GridLayout(2, 2, 16, 16));
        optionsPanel.setOpaque(false);
        optionsPanel.setMaximumSize(new Dimension(780, 320));
        optionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(optionsPanel);
        panel.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));

        // Lifelines Row (Hint + 50:50 + Freeze Time + Skip + Online Trivia API)
        JPanel lifelineRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        lifelineRow.setOpaque(false);

        hintButton = UITheme.iconPillButton(UITheme.VectorIcon.LIGHTBULB, com.worldofwonder.util.I18n.get("quiz_hint"), UITheme.GOLD);
        UIUtil.fixedSize(hintButton, 130, UITheme.BTN_H_SM);
        hintButton.addActionListener(e -> showHint());
        lifelineRow.add(hintButton);

        fiftyFiftyBtn = UITheme.iconPillButton(UITheme.VectorIcon.SEARCH, com.worldofwonder.util.I18n.get("quiz_lifeline"), UITheme.TEAL);
        UIUtil.fixedSize(fiftyFiftyBtn, 130, UITheme.BTN_H_SM);
        fiftyFiftyBtn.addActionListener(e -> useFiftyFifty());
        lifelineRow.add(fiftyFiftyBtn);

        freezeBtn = UITheme.iconPillButton(UITheme.VectorIcon.REFRESH, com.worldofwonder.util.I18n.get("powerup_freeze"), UITheme.TEAL);
        UIUtil.fixedSize(freezeBtn, 135, UITheme.BTN_H_SM);
        freezeBtn.addActionListener(e -> freezeTime());
        lifelineRow.add(freezeBtn);

        skipBtn = UITheme.iconPillButton(UITheme.VectorIcon.ARROW_RIGHT, com.worldofwonder.util.I18n.get("powerup_skip"), UITheme.VIOLET);
        UIUtil.fixedSize(skipBtn, 105, UITheme.BTN_H_SM);
        skipBtn.addActionListener(e -> skipQuestion());
        lifelineRow.add(skipBtn);

        onlineTriviaBtn = UITheme.iconPillButton(UITheme.VectorIcon.GLOBE, "Online API", UITheme.GOLD);
        UIUtil.fixedSize(onlineTriviaBtn, 120, UITheme.BTN_H_SM);
        onlineTriviaBtn.setToolTipText("Load fresh live questions from Open Trivia DB");
        onlineTriviaBtn.addActionListener(e -> fetchOnlineQuestions());
        lifelineRow.add(onlineTriviaBtn);

        hintLabel = new UITheme.HintBox(" ");
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintLabel.setMaximumSize(new Dimension(760, 60));
        hintLabel.setVisible(false);
        feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
        feedbackLabel.setFont(UITheme.bodyFont(Font.BOLD, UITheme.FONT_BODY));
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);
        feedbackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttonRow.setOpaque(false);
        submitButton = UITheme.primaryButton(com.worldofwonder.util.I18n.get("quiz_submit"));
        UIUtil.fixedSize(submitButton, 210, UITheme.BTN_H);
        submitButton.addActionListener(e -> submitAnswer());
        nextButton = UITheme.secondaryButton(com.worldofwonder.util.I18n.get("quiz_next_question"));
        UIUtil.fixedSize(nextButton, 210, UITheme.BTN_H);
        nextButton.addActionListener(e -> nextQuestion());
        nextButton.setVisible(false);
        buttonRow.add(submitButton);
        buttonRow.add(nextButton);

        exitBtn = UITheme.iconPillButton(UITheme.VectorIcon.ARROW_LEFT, com.worldofwonder.util.I18n.get("exit_to_games"), UITheme.CORAL);
        UIUtil.fixedSize(exitBtn, 170, UITheme.BTN_H);
        exitBtn.setToolTipText("Return to the main game selection menu");
        exitBtn.addActionListener(e -> {
            stopTimer();
            dashboard.showDashboard();
        });
        buttonRow.add(exitBtn);
        south.add(Box.createVerticalStrut(6));
        south.add(lifelineRow);
        south.add(hintLabel);
        south.add(Box.createVerticalStrut(4));
        south.add(feedbackLabel);
        south.add(Box.createVerticalStrut(6));
        south.add(buttonRow);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCompletePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (confetti != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UITheme.quality(g2);
                    confetti.paint(g2);
                    g2.dispose();
                }
            }
        };
        panel.setOpaque(false);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        completeTitle = new UITheme.GradientTextLabel(com.worldofwonder.util.I18n.get("quiz_victory_title"), 40, UITheme.GOLD, UITheme.CORAL);
        completeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(completeTitle);
        completeSub = UITheme.subtitle(com.worldofwonder.util.I18n.get("tagline"));
        completeSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(completeSub);
        completeText = new JLabel(" ", SwingConstants.CENTER);
        completeText.setFont(UITheme.bodyFont(Font.BOLD, UITheme.FONT_CARD_TITLE));
        completeText.setForeground(UITheme.GOLD);
        completeText.setAlignmentX(Component.CENTER_ALIGNMENT);
        completeText.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        center.add(completeText);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.GAP_ELEMENT, 0));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(28, 0, 0, 0));
        againBtn = UITheme.primaryButton(com.worldofwonder.util.I18n.get("quiz_play_again"));
        UIUtil.fixedSize(againBtn, 210, UITheme.BTN_H);
        againBtn.addActionListener(e -> startQuiz(currentLevel));
        buttons.add(againBtn);
        levelsCompleteBtn = UITheme.secondaryButton(com.worldofwonder.util.I18n.get("quiz_back_levels"));
        UIUtil.fixedSize(levelsCompleteBtn, 210, UITheme.BTN_H);
        levelsCompleteBtn.addActionListener(e -> showLevels());
        buttons.add(levelsCompleteBtn);
        worldsCompleteBtn = UITheme.ghostButton(com.worldofwonder.util.I18n.get("quiz_another_world"), UITheme.TEXT_MUTED);
        UIUtil.fixedSize(worldsCompleteBtn, 210, UITheme.BTN_H);
        worldsCompleteBtn.addActionListener(e -> showWorlds());
        buttons.add(worldsCompleteBtn);
        center.add(buttons);
        panel.add(center, BorderLayout.CENTER);
        this.confetti = new UITheme.Confetti(panel);
        return panel;
    }


    private void showWorlds() {
        cards.show(content, PANEL_WORLDS);
        loadWorlds();
    }

    private void showLevels() {
        cards.show(content, PANEL_LEVELS);
        renderLevels();
    }

    private void showQuiz() {
        cards.show(content, PANEL_QUIZ);
        renderQuestion();
    }

    private void loadWorlds() {
        statusLabel.setText(" ");
        try {
            worlds = dashboard.getApp().getGameController().getAllWorlds();
            sampleMode = false;
        } catch (Exception e) {
            sampleMode = true;
            worlds = sample.getWorlds();
        }
        if (worlds == null || worlds.isEmpty()) {
            worlds = sample.getWorlds();
        }
        renderWorlds();
    }

    private String getWorldDisplayName(World world) {
        if (world == null) return "";
        String key = "world_" + world.getId() + "_name";
        String localized = com.worldofwonder.util.I18n.get(key);
        if (!localized.equals(key) && !localized.isEmpty()) {
            return localized;
        }
        return world.getName();
    }

    private String getWorldDisplayDesc(World world) {
        if (world == null) return "";
        String key = "world_" + world.getId() + "_desc";
        String localized = com.worldofwonder.util.I18n.get(key);
        if (!localized.equals(key) && !localized.isEmpty()) {
            return localized;
        }
        return world.getDescription();
    }

    private String getLevelDisplayName(Level level) {
        if (level == null) return "";
        String key = "level_" + level.getId() + "_name";
        String localized = com.worldofwonder.util.I18n.get(key);
        if (!localized.equals(key) && !localized.isEmpty()) {
            return localized;
        }
        return level.getName();
    }

    private void renderWorlds() {
        worldsList.removeAll();
        for (World world : worlds) {
            UITheme.TileButton tile = new UITheme.TileButton(getWorldDisplayName(world),
                    getWorldDisplayDesc(world), worldAccent(world), worldEmoji(world.getName()));
            tile.setDark(true);
            tile.setSubtitleColor(new Color(0xd0e8f5));
            UIUtil.fixedSize(tile, 280, 148);
            tile.addActionListener(e -> selectWorld(world));
            worldsList.add(tile);
        }
        worldsList.revalidate();
        worldsList.repaint();
        UITheme.recordBaseTree(worldsList);
        UITheme.rescale(worldsList);
    }

    private void selectWorld(World world) {
        this.currentWorld = world;
        levels = new ArrayList<>();
        try {
            levels = dashboard.getApp().getGameController().getLevelsForWorld(world.getId());
        } catch (Exception e) {
            levels = sample.getLevels(world.getId());
        }
        if (levels == null || levels.isEmpty()) {
            levels = sample.getLevels(world.getId());
        }
        levelsTitle.setText(com.worldofwonder.util.I18n.get("quiz_levels_title", getWorldDisplayName(world)));
        levelsTitle.setFont(UITheme.fontFor(levelsTitle.getText(), Font.BOLD, UITheme.FONT_PAGE_TITLE));
        showLevels();
    }

    private void renderLevels() {
        levelsList.removeAll();
        for (Level level : levels) {
            String subtitle = difficultyLabel(level.getDifficulty())
                    + "  -  " + com.worldofwonder.util.I18n.get("quiz_pts_reward", level.getPointReward());
            UITheme.TileButton tile = new UITheme.TileButton(getLevelDisplayName(level),
                    subtitle, difficultyAccent(level.getDifficulty()));
            tile.setDark(true);
            tile.setSubtitleColor(new Color(0xd0e8f5));
            UIUtil.fixedSize(tile, 280, 115);
            tile.addActionListener(e -> startQuiz(level));
            levelsList.add(tile);
        }
        levelsList.revalidate();
        levelsList.repaint();
        UITheme.recordBaseTree(levelsList);
        UITheme.rescale(levelsList);
    }

    private void startQuiz(Level level) {
        currentLevel = level;
        questionIndex = 0;
        score = 0;
        points = 0;
        selectedAnswer = -1;
        streak = 0;
        fiftyFiftyUsed = false;
        freezeUsed = false;
        skipUsed = false;
        quizLevelName.setText(getLevelDisplayName(level));
        quizLevelName.setFont(UITheme.fontFor(quizLevelName.getText(), Font.BOLD, UITheme.FONT_SECTION));
        cards.show(content, PANEL_QUIZ);
        try {
            questions = dashboard.getApp().getQuizController().getQuestionsForLevel(level.getId());
        } catch (Exception e) {
            questions = sample.getQuestions(level.getId());
        }
        if (questions == null || questions.isEmpty()) {
            questions = sample.getQuestions(level.getId());
        }
        if (fiftyFiftyBtn != null) fiftyFiftyBtn.setEnabled(true);
        if (freezeBtn != null) freezeBtn.setEnabled(true);
        if (skipBtn != null) skipBtn.setEnabled(true);
        updateStreakDisplay();
        renderQuestion();
    }

    private void renderQuestion() {
        if (questions == null || questions.isEmpty() || questionIndex >= questions.size()) {
            showLevelComplete();
            return;
        }
        Question question = questions.get(questionIndex);
        String qText = question.getLocalizedQuestionText();
        questionText.setText("<html><center>" + escapeHtml(qText) + "</center></html>");
        questionText.setFont(UITheme.fontFor(qText, Font.BOLD, UITheme.FONT_CARD_TITLE));
        progressLabel.setText(com.worldofwonder.util.I18n.get("quiz_question_counter", (questionIndex + 1), questions.size())
                + "  -  " + com.worldofwonder.util.I18n.get("quiz_score", points));
        progressLabel.setFont(UITheme.fontFor(progressLabel.getText(), Font.BOLD, UITheme.FONT_BODY));
        progressBar.setProgress(questionIndex / (double) questions.size());

        List<String> options = optionsOf(question);
        optionsPanel.removeAll();
        for (int i = 0; i < options.size(); i++) {
            final int idx = i;
            UITheme.OptionButton btn = new UITheme.OptionButton(letter(i), options.get(i));
            btn.reset();
            btn.playEnter(80 + i * 70);
            btn.addActionListener(e -> selectAnswer(idx));
            optionsPanel.add(btn);
        }
        selectedAnswer = -1;
        hintButton.setEnabled(true);
        hintLabel.setText(" ");
        hintLabel.setVisible(false);
        feedbackLabel.setText(" ");
        feedbackLabel.setForeground(UITheme.TEXT_MUTED);
        submitButton.setVisible(true);
        submitButton.setEnabled(true);
        nextButton.setVisible(false);
        if (fiftyFiftyBtn != null) fiftyFiftyBtn.setEnabled(!fiftyFiftyUsed);
        if (freezeBtn != null) freezeBtn.setEnabled(!freezeUsed);
        if (skipBtn != null) skipBtn.setEnabled(!skipUsed);
        optionsPanel.revalidate();
        optionsPanel.repaint();
        UITheme.recordBaseTree(optionsPanel);
        UITheme.rescale(optionsPanel);
        ((UITheme.SlidePanel) optionsPanel).play();

        startTimer();
    }

    private void startTimer() {
        stopTimer();
        secondsLeft = 15;
        updateTimerDisplay();
        questionTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft--;
            updateTimerDisplay();
            if (secondsLeft <= 3 && secondsLeft > 0) {
                SoundUtil.playClick();
            } else if (secondsLeft <= 0) {
                stopTimer();
                handleTimeout();
            }
        });
        questionTimer.start();
    }

    private void stopTimer() {
        if (questionTimer != null && questionTimer.isRunning()) {
            questionTimer.stop();
        }
    }

    private void updateTimerDisplay() {
        if (timerText != null) {
            timerText.setText("[T] " + secondsLeft + "s");
            if (secondsLeft <= 3) {
                timerText.setForeground(UITheme.CORAL);
            } else if (secondsLeft <= 7) {
                timerText.setForeground(UITheme.GOLD);
            } else {
                timerText.setForeground(UITheme.GREEN);
            }
        }
        if (timerBar != null) {
            timerBar.setProgress(secondsLeft / 15.0);
        }
    }

    private void updateStreakDisplay() {
        if (streakLabel != null) {
            if (streak >= 5) {
                streakLabel.setText("Super Streak: " + streak + " (2x Bonus)");
                streakLabel.setForeground(new Color(0xff3333));
            } else if (streak >= 3) {
                streakLabel.setText("Streak: " + streak + " (1.5x Bonus)");
                streakLabel.setForeground(new Color(0xff8800));
            } else if (streak > 0) {
                streakLabel.setText("Streak: " + streak);
                streakLabel.setForeground(UITheme.GOLD);
            } else {
                streakLabel.setText(I18n.get("quiz_streak", 0));
                streakLabel.setForeground(UITheme.TEXT_MUTED);
            }
        }
    }

    private void handleTimeout() {
        streak = 0;
        updateStreakDisplay();
        Question question = questions.get(questionIndex);
        int correctIdx = correctIndex(question);
        for (int i = 0; i < optionsPanel.getComponentCount(); i++) {
            UITheme.OptionButton ob = (UITheme.OptionButton) optionsPanel.getComponent(i);
            if (i == correctIdx) {
                ob.setCorrect();
            } else {
                ob.reset();
            }
        }
        submitButton.setVisible(false);
        hintButton.setEnabled(false);
        if (fiftyFiftyBtn != null) fiftyFiftyBtn.setEnabled(false);
        feedbackLabel.setForeground(UITheme.ERROR);
        feedbackLabel.setText(com.worldofwonder.util.I18n.get("quiz_time_up", letter(correctIdx)));
        SoundUtil.playError();
        if (questionIndex == questions.size() - 1) {
            nextButton.setText(com.worldofwonder.util.I18n.get("quiz_finish_level"));
        } else {
            nextButton.setText(com.worldofwonder.util.I18n.get("quiz_next_question"));
        }
        nextButton.setVisible(true);
    }

    private void useFiftyFifty() {
        if (fiftyFiftyUsed || questions == null || questionIndex >= questions.size()) {
            return;
        }
        fiftyFiftyUsed = true;
        if (fiftyFiftyBtn != null) {
            fiftyFiftyBtn.setEnabled(false);
        }
        Question q = questions.get(questionIndex);
        int[] exclusions = dashboard.getApp().getQuizController().getFiftyFiftyExclusions(q);
        for (int ex : exclusions) {
            if (ex >= 0 && ex < optionsPanel.getComponentCount()) {
                UITheme.OptionButton btn = (UITheme.OptionButton) optionsPanel.getComponent(ex);
                btn.setEnabled(false);
                btn.setText("--- [50:50] ---");
            }
        }
        SoundUtil.playHint();
        feedbackLabel.setForeground(UITheme.TEAL);
        feedbackLabel.setText(com.worldofwonder.util.I18n.get("quiz_lifeline_used"));
    }

    private void freezeTime() {
        if (freezeUsed || questions == null || questionIndex >= questions.size()) return;
        freezeUsed = true;
        if (freezeBtn != null) freezeBtn.setEnabled(false);
        secondsLeft += 15;
        updateTimerDisplay();
        SoundUtil.playHint();
        feedbackLabel.setForeground(UITheme.TEAL);
        feedbackLabel.setText(com.worldofwonder.util.I18n.get("powerup_used") + " (+15s)");
    }

    private void skipQuestion() {
        if (skipUsed || questions == null || questionIndex >= questions.size()) return;
        skipUsed = true;
        if (skipBtn != null) skipBtn.setEnabled(false);
        SoundUtil.playClick();
        feedbackLabel.setForeground(UITheme.GOLD);
        feedbackLabel.setText(com.worldofwonder.util.I18n.get("powerup_skip") + "!");
        nextQuestion();
    }

    private void fetchOnlineQuestions() {
        if (currentWorld == null) return;
        int category = com.worldofwonder.util.ApiService.getOpenTDBCategory(currentWorld.getId());
        feedbackLabel.setForeground(UITheme.TEAL);
        feedbackLabel.setText(com.worldofwonder.util.I18n.get("api_fetching"));

        com.worldofwonder.util.ApiService.fetchTrivia(category, "easy", 5, results -> {
            if (results != null && !results.isEmpty()) {
                List<Question> onlineList = new ArrayList<>();
                int qId = 1;
                for (Map<String, Object> map : results) {
                    String qText = (String) map.get("question");
                    String correct = (String) map.get("correct_answer");
                    List<String> opts = new ArrayList<>();
                    opts.add(correct);
                    if (map.containsKey("incorrect_answers")) {
                        for (Object o : (List<?>) map.get("incorrect_answers")) {
                            opts.add(String.valueOf(o));
                        }
                    }
                    Collections.shuffle(opts);
                    String optA = opts.size() > 0 ? opts.get(0) : "";
                    String optB = opts.size() > 1 ? opts.get(1) : "";
                    String optC = opts.size() > 2 ? opts.get(2) : "";
                    String optD = opts.size() > 3 ? opts.get(3) : "";
                    char correctLetter = 'A';
                    if (optB.equals(correct)) correctLetter = 'B';
                    else if (optC.equals(correct)) correctLetter = 'C';
                    else if (optD.equals(correct)) correctLetter = 'D';

                    onlineList.add(new Question(qId++, currentLevel != null ? currentLevel.getId() : 1,
                            qText, optA, optB, optC, optD, String.valueOf(correctLetter), "Think about world history & science!"));
                }
                questions = onlineList;
                questionIndex = 0;
                points = 0;
                score = 0;
                SoundUtil.playVictory();
                renderQuestion();
            }
        }, err -> {
            feedbackLabel.setForeground(UITheme.CORAL);
            feedbackLabel.setText(com.worldofwonder.util.I18n.get("api_error"));
        });
    }

    private void selectAnswer(int idx) {
        selectedAnswer = idx;
        for (int i = 0; i < optionsPanel.getComponentCount(); i++) {
            UITheme.OptionButton ob = (UITheme.OptionButton) optionsPanel.getComponent(i);
            if (i == idx) {
                ob.setSelected();
            } else {
                ob.reset();
            }
        }
    }

    private void submitAnswer() {
        if (selectedAnswer < 0) {
            feedbackLabel.setForeground(UITheme.ERROR);
            feedbackLabel.setText(com.worldofwonder.util.I18n.get("quiz_pick_answer"));
            SoundUtil.playError();
            return;
        }
        stopTimer();
        submitButton.setEnabled(false);
        hintButton.setEnabled(false);
        if (fiftyFiftyBtn != null) fiftyFiftyBtn.setEnabled(false);
        if (freezeBtn != null) freezeBtn.setEnabled(false);
        if (skipBtn != null) skipBtn.setEnabled(false);

        Question question = questions.get(questionIndex);
        boolean correct = selectedAnswer == correctIndex(question);
        int baseAward = dashboard.getApp().getQuizController().calculatePointsForQuestion(currentLevel, questions.size());
        if (correct) {
            streak++;
            int speedBonus = Math.max(0, secondsLeft);
            int awarded = dashboard.getApp().getQuizController().calculatePointsWithStreak(baseAward, streak) + speedBonus;
            points += awarded;
            score++;
            SoundUtil.playCorrect();
            int userId = dashboard.getUserId();
            if (userId > 0) {
                int newTotal = dashboard.getApp().getQuizController().awardQuizPoints(userId, awarded);
                dashboard.updateScore(newTotal);
            } else {
                dashboard.addGamePoints(awarded);
            }
            feedbackLabel.setForeground(UITheme.GREEN);
            String streakText = streak >= 5 ? " (2x)" : (streak >= 3 ? " (1.5x)" : "");
            String bonusNote = speedBonus > 0 ? " [+" + speedBonus + " speed bonus]" : "";
            feedbackLabel.setText(com.worldofwonder.util.I18n.get("quiz_correct_feedback", streakText + bonusNote, awarded));
        } else {
            streak = 0;
            SoundUtil.playError();
            int userId = dashboard.getUserId();
            if (userId > 0) {
                User u = dashboard.getApp().getGameController().getUser(userId);
                if (u != null) {
                    u.loseHeart();
                }
            }
            feedbackLabel.setForeground(UITheme.ERROR);
            feedbackLabel.setText(com.worldofwonder.util.I18n.get("quiz_wrong_feedback", letter(correctIndex(question))));
        }
        updateStreakDisplay();

        for (int i = 0; i < optionsPanel.getComponentCount(); i++) {
            UITheme.OptionButton ob = (UITheme.OptionButton) optionsPanel.getComponent(i);
            if (i == correctIndex(question)) {
                ob.setCorrect();
            } else if (i == selectedAnswer) {
                ob.setWrong();
            } else {
                ob.reset();
            }
        }

        progressBar.setProgress((questionIndex + 1) / (double) questions.size());

        if (questionIndex == questions.size() - 1) {
            nextButton.setText(com.worldofwonder.util.I18n.get("quiz_view_results"));
        } else {
            nextButton.setText(com.worldofwonder.util.I18n.get("quiz_next_question"));
        }
        nextButton.setVisible(true);
    }

    private void nextQuestion() {
        stopTimer();
        questionIndex++;
        if (questions == null || questionIndex >= questions.size()) {
            showLevelComplete();
        } else {
            renderQuestion();
        }
    }

    private void showHint() {
        Question question = questions.get(questionIndex);
        String hint = question.getLocalizedHint() == null || question.getLocalizedHint().isEmpty()
                ? "No hint available." : question.getLocalizedHint();
        hintLabel.setText("<html><div style='padding:2px 6px;'>" + escapeHtml(hint) + "</div></html>");
        hintLabel.setFont(UITheme.fontFor(hint, Font.PLAIN, UITheme.FONT_BODY));
        hintLabel.setVisible(true);
        hintButton.setEnabled(false);
        SoundUtil.playHint();
    }

    private void showLevelComplete() {
        stopTimer();
        SoundUtil.playVictory();
        completeTitle.setText(com.worldofwonder.util.I18n.get("quiz_victory_title"));
        completeText.setText("<html><center>" + com.worldofwonder.util.I18n.get("quiz_score_summary", score, (questions != null ? questions.size() : 0))
                + "<br><span style='color:#ffd700;font-size:16px;'>" + com.worldofwonder.util.I18n.get("quiz_total_earned", points) + "</span></center></html>");
        cards.show(content, PANEL_COMPLETE);
        confetti.launch();
    }

    private void syncCompletionToBackend() {
        // Points were already awarded per answer via QuizController
    }

    private static String letter(int i) {
        return String.valueOf(OPTION_LETTERS[i]);
    }

    private static List<String> optionsOf(Question q) {
        List<String> options = new ArrayList<>();
        options.add(q.getLocalizedOptionA() == null ? "" : q.getLocalizedOptionA());
        options.add(q.getLocalizedOptionB() == null ? "" : q.getLocalizedOptionB());
        options.add(q.getLocalizedOptionC() == null ? "" : q.getLocalizedOptionC());
        options.add(q.getLocalizedOptionD() == null ? "" : q.getLocalizedOptionD());
        return options;
    }

    private static int correctIndex(Question q) {
        if (q.getCorrectAnswer() == null || q.getCorrectAnswer().isEmpty()) {
            return 0;
        }
        char c = Character.toUpperCase(q.getCorrectAnswer().charAt(0));
        return Math.max(0, Math.min(3, c - 'A'));
    }

    private static Color worldAccent(World world) {
        switch (world.getId() % 3) {
            case 0:
                return UITheme.VIOLET;
            case 1:
                return UITheme.TEAL;
            default:
                return UITheme.CORAL;
        }
    }

    private static String worldEmoji(String name) {
        return "";
    }

    private static Color difficultyAccent(String difficulty) {
        if (difficulty == null) {
            return UITheme.TEAL;
        }
        switch (difficulty.toLowerCase()) {
            case "easy":
                return UITheme.GREEN;
            case "hard":
                return UITheme.CORAL;
            default:
                return UITheme.GOLD;
        }
    }

    private static String difficultyLabel(String difficulty) {
        if (difficulty == null || difficulty.isEmpty()) {
            return "Quiz";
        }
        String d = difficulty.toLowerCase();
        if (d.equals("easy")) {
            return com.worldofwonder.util.I18n.get("diff_easy");
        }
        if (d.equals("hard")) {
            return com.worldofwonder.util.I18n.get("diff_hard");
        }
        return com.worldofwonder.util.I18n.get("diff_medium");
    }


    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
