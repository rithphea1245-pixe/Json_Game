package com.worldofwonder.view;

import com.worldofwonder.model.*;
import com.worldofwonder.controller.*;

import com.worldofwonder.model.Level;
import com.worldofwonder.model.Question;
import com.worldofwonder.model.World;

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
        completePanel = buildCompletePanel();
        content.add(completePanel, PANEL_COMPLETE);
        confetti = new UITheme.Confetti(completePanel);

        JPanel card = UITheme.card(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X, UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X));
        UIUtil.fixedSize(card, 960, 740);

        JButton back = UITheme.ghostButton("🏠 Back to Games", UITheme.CORAL);
        UIUtil.fixedSize(back, 180, UITheme.BTN_H);
        back.addActionListener(e -> {
            stopTimer();
            dashboard.showDashboard();
        });
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 12, 2));
        topBar.add(back, BorderLayout.WEST);

        JLabel titleLbl = UITheme.title("World of Wonder Quiz", 24);
        topBar.add(titleLbl, BorderLayout.CENTER);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        east.setOpaque(false);

        JButton soundBtn = UITheme.ghostButton(SoundUtil.isMuted() ? "\uD83D\uDD07" : "\uD83D\uDD0A", UITheme.TEAL);
        UIUtil.fixedSize(soundBtn, 56, UITheme.BTN_H_SM);
        soundBtn.setToolTipText("Toggle Sound Effects");
        soundBtn.addActionListener(e -> {
            SoundUtil.toggleMute();
            soundBtn.setText(SoundUtil.isMuted() ? "\uD83D\uDD07" : "\uD83D\uDD0A");
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
    }

    private JPanel buildWorldsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UITheme.title("Choose Your World", UITheme.FONT_PAGE_TITLE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        JLabel sub = UITheme.subtitle("Pick a world to begin your quiz adventure");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(sub);
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
        levelsTitle = UITheme.sectionTitle("Levels", UITheme.FONT_PAGE_TITLE);
        levelsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(levelsTitle);
        header.add(Box.createVerticalStrut(4));
        JLabel sub = UITheme.subtitle("Pick a level to start the quiz");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(sub);
        header.add(Box.createVerticalStrut(8));
        JButton back = UITheme.ghostButton("< Back to Worlds", UITheme.TEXT_MUTED);
        UIUtil.fixedSize(back, 200, UITheme.BTN_H_SM);
        back.addActionListener(e -> showWorlds());
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(back);
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

        timerText = new JLabel("⏱️ 15s");
        timerText.setFont(UITheme.displayFont(Font.BOLD, 15));
        timerText.setForeground(UITheme.GREEN);
        timerRow.add(timerText);

        streakLabel = new JLabel("🔥 Streak: 0");
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

        // Lifelines Row (Hint + 50:50)
        JPanel lifelineRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        lifelineRow.setOpaque(false);

        hintButton = UITheme.ghostButton("\uD83D\uDCA1 Show Hint", UITheme.GOLD);
        UIUtil.fixedSize(hintButton, 150, UITheme.BTN_H_SM);
        hintButton.addActionListener(e -> showHint());
        lifelineRow.add(hintButton);

        fiftyFiftyBtn = UITheme.ghostButton("\u2702\uFE0F 50:50 Lifeline", UITheme.TEAL);
        UIUtil.fixedSize(fiftyFiftyBtn, 160, UITheme.BTN_H_SM);
        fiftyFiftyBtn.addActionListener(e -> useFiftyFifty());
        lifelineRow.add(fiftyFiftyBtn);

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
        submitButton = UITheme.primaryButton("Submit Answer");
        UIUtil.fixedSize(submitButton, 210, UITheme.BTN_H);
        submitButton.addActionListener(e -> submitAnswer());
        nextButton = UITheme.secondaryButton("Next Question");
        UIUtil.fixedSize(nextButton, 210, UITheme.BTN_H);
        nextButton.addActionListener(e -> nextQuestion());
        nextButton.setVisible(false);
        buttonRow.add(submitButton);
        buttonRow.add(nextButton);

        JButton exitBtn = UITheme.ghostButton("🏠 Exit to Games", UITheme.CORAL);
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
        completeTitle = new UITheme.GradientTextLabel("Level Complete!", 40, UITheme.GOLD, UITheme.CORAL);
        completeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(completeTitle);
        JLabel sub = UITheme.subtitle("Great job, explorer!");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(sub);
        completeText = new JLabel(" ", SwingConstants.CENTER);
        completeText.setFont(UITheme.bodyFont(Font.BOLD, UITheme.FONT_CARD_TITLE));
        completeText.setForeground(UITheme.GOLD);
        completeText.setAlignmentX(Component.CENTER_ALIGNMENT);
        completeText.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        center.add(completeText);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.GAP_ELEMENT, 0));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(28, 0, 0, 0));
        JButton again = UITheme.primaryButton("Play Again");
        UIUtil.fixedSize(again, 210, UITheme.BTN_H);
        again.addActionListener(e -> startQuiz(currentLevel));
        buttons.add(again);
        JButton levelsBtn = UITheme.secondaryButton("Back to Levels");
        UIUtil.fixedSize(levelsBtn, 210, UITheme.BTN_H);
        levelsBtn.addActionListener(e -> showLevels());
        buttons.add(levelsBtn);
        JButton worldsBtn = UITheme.ghostButton("Choose Another World", UITheme.TEXT_MUTED);
        UIUtil.fixedSize(worldsBtn, 210, UITheme.BTN_H);
        worldsBtn.addActionListener(e -> showWorlds());
        buttons.add(worldsBtn);
        center.add(buttons);
        panel.add(center, BorderLayout.CENTER);
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

    private void renderWorlds() {
        worldsList.removeAll();
        for (World world : worlds) {
            UITheme.TileButton tile = new UITheme.TileButton(world.getName(),
                    world.getDescription(), worldAccent(world), worldEmoji(world.getName()));
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
        levels = new ArrayList<>();
        try {
            levels = dashboard.getApp().getGameController().getLevelsForWorld(world.getId());
        } catch (Exception e) {
            levels = sample.getLevels(world.getId());
        }
        if (levels == null || levels.isEmpty()) {
            levels = sample.getLevels(world.getId());
        }
        levelsTitle.setText(worldEmoji(world.getName()) + "  " + world.getName() + " Levels");
        showLevels();
    }

    private void renderLevels() {
        levelsList.removeAll();
        for (Level level : levels) {
            String subtitle = difficultyLabel(level.getDifficulty())
                    + "  -  " + level.getPointReward() + " points";
            UITheme.TileButton tile = new UITheme.TileButton(level.getName(),
                    subtitle, difficultyAccent(level.getDifficulty()), "\u2b50");
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
        quizLevelName.setText(level.getName());
        cards.show(content, PANEL_QUIZ);
        try {
            questions = dashboard.getApp().getQuizController().getQuestionsForLevel(level.getId());
        } catch (Exception e) {
            questions = sample.getQuestions(level.getId());
        }
        if (questions == null || questions.isEmpty()) {
            questions = sample.getQuestions(level.getId());
        }
        if (fiftyFiftyBtn != null) {
            fiftyFiftyBtn.setEnabled(true);
        }
        updateStreakDisplay();
        renderQuestion();
    }

    private void renderQuestion() {
        if (questions == null || questions.isEmpty() || questionIndex >= questions.size()) {
            showLevelComplete();
            return;
        }
        Question question = questions.get(questionIndex);
        questionText.setText("<html><center>" + escapeHtml(question.getQuestionText()) + "</center></html>");
        progressLabel.setText("Question " + (questionIndex + 1) + " of " + questions.size()
                + "  -  Level Points: " + points);
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
        if (fiftyFiftyBtn != null) {
            fiftyFiftyBtn.setEnabled(!fiftyFiftyUsed);
        }
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
            timerText.setText("⏱️ " + secondsLeft + "s");
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
                streakLabel.setText("\uD83D\uDD25 Super Streak: " + streak + " (2x \u26A1)");
                streakLabel.setForeground(new Color(0xff3333));
            } else if (streak >= 3) {
                streakLabel.setText("\uD83D\uDD25 Streak: " + streak + " (1.5x \u26A1)");
                streakLabel.setForeground(new Color(0xff8800));
            } else if (streak > 0) {
                streakLabel.setText("\uD83D\uDD25 Streak: " + streak);
                streakLabel.setForeground(UITheme.GOLD);
            } else {
                streakLabel.setText("Streak: 0");
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
        feedbackLabel.setText("⏰ Time's up! The correct answer was " + letter(correctIdx) + ".");
        SoundUtil.playError();
        if (questionIndex == questions.size() - 1) {
            nextButton.setText("Finish Level \u2714");
        } else {
            nextButton.setText("Next Question \u2192");
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
                btn.setText("--- [Eliminated] ---");
            }
        }
        SoundUtil.playHint();
        feedbackLabel.setForeground(UITheme.TEAL);
        feedbackLabel.setText("✂️ 50:50 Lifeline used! Two incorrect choices removed.");
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
            feedbackLabel.setText("Pick an answer first!");
            SoundUtil.playError();
            return;
        }
        stopTimer();
        submitButton.setEnabled(false);
        hintButton.setEnabled(false);
        if (fiftyFiftyBtn != null) fiftyFiftyBtn.setEnabled(false);

        Question question = questions.get(questionIndex);
        boolean correct = selectedAnswer == correctIndex(question);
        int baseAward = dashboard.getApp().getQuizController().calculatePointsForQuestion(currentLevel, questions.size());
        if (correct) {
            streak++;
            int awarded = dashboard.getApp().getQuizController().calculatePointsWithStreak(baseAward, streak);
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
            String streakText = streak >= 5 ? " \uD83D\uDD25 (2x Streak Bonus!)" : (streak >= 3 ? " \uD83D\uDD25 (1.5x Streak Bonus!)" : "");
            feedbackLabel.setText("Correct!" + streakText + " +" + awarded + " points");
        } else {
            streak = 0;
            SoundUtil.playError();
            feedbackLabel.setForeground(UITheme.ERROR);
            feedbackLabel.setText("Not quite. The answer is " + letter(correctIndex(question)) + ".");
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
            nextButton.setText("View Results \u2192");
        } else {
            nextButton.setText("Next Question \u2192");
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
        String hint = question.getHint() == null || question.getHint().isEmpty()
                ? "No hint available." : question.getHint();
        hintLabel.setText("<html><div style='padding:2px 6px;'>" + escapeHtml(hint) + "</div></html>");
        hintLabel.setVisible(true);
        hintButton.setEnabled(false);
        SoundUtil.playHint();
    }

    private void showLevelComplete() {
        stopTimer();
        SoundUtil.playVictory();
        double ratio = (questions == null || questions.isEmpty()) ? 0 : (double) score / questions.size();
        String stars = ratio >= 0.85 ? "\u2B50\u2B50\u2B50" : (ratio >= 0.5 ? "\u2B50\u2B50" : "\u2B50");
        completeTitle.setText("Level Complete! " + stars);
        completeText.setText("<html><center>You scored " + score + " of " + (questions != null ? questions.size() : 0)
                + " correct.<br><span style='color:#ffd700;font-size:16px;'>Total Earned: +" + points + " points!</span></center></html>");
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
        options.add(q.getOptionA() == null ? "" : q.getOptionA());
        options.add(q.getOptionB() == null ? "" : q.getOptionB());
        options.add(q.getOptionC() == null ? "" : q.getOptionC());
        options.add(q.getOptionD() == null ? "" : q.getOptionD());
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
        if (name == null) {
            return "\uD83C\uDF0D";
        }
        String n = name.toLowerCase();
        if (n.contains("space") || n.contains("galax") || n.contains("solar")) {
            return "\uD83D\uDE80";
        }
        if (n.contains("ocean") || n.contains("deep sea") || n.contains("reef") || n.contains("sea")) {
            return "\uD83C\uDF0A";
        }
        if (n.contains("egypt") || n.contains("nile")) {
            return "\uD83C\uDFDB";
        }
        if (n.contains("rainforest") || n.contains("jungle")) {
            return "\uD83C\uDF33";
        }
        if (n.contains("mountain")) {
            return "\u26F0";
        }
        return "\uD83C\uDF0D";
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
            return "Easy";
        }
        if (d.equals("hard")) {
            return "Hard";
        }
        return "Medium";
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
