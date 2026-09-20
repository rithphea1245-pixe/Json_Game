package com.worldofwonder.view;

import com.worldofwonder.util.I18n;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fast-paced 60-second Match Master mini-game inspired by Quizlet Match.
 * Players match vocabulary words to their definitions against the clock.
 */
public class MatchGameScreen extends JPanel {

    private static final String VIEW_PLAY = "play";
    private static final String VIEW_OVER = "over";

    private static class WordPair {
        final String wordEn;
        final String wordKm;
        final String defEn;
        final String defKm;

        WordPair(String wordEn, String wordKm, String defEn, String defKm) {
            this.wordEn = wordEn;
            this.wordKm = wordKm;
            this.defEn = defEn;
            this.defKm = defKm;
        }

        String getWord() {
            return I18n.isKhmer() ? wordKm : wordEn;
        }

        String getDef() {
            return I18n.isKhmer() ? defKm : defEn;
        }
    }

    private static final List<WordPair> VOCAB_BANK = new ArrayList<>();
    static {
        VOCAB_BANK.add(new WordPair("Pyramid", "ពីរ៉ាមីត", "Ancient monumental structure with triangular sides", "សំណង់បុរាណដែលមានជ្រុងត្រីកោណ"));
        VOCAB_BANK.add(new WordPair("Pharaoh", "ផារ៉ាអុង", "Monarch of ancient Egypt", "ស្តេចនៃអេហ្ស៊ីបបុរាណ"));
        VOCAB_BANK.add(new WordPair("Orbit", "គន្លង", "Curved trajectory of an object around a star or planet", "គន្លងដែលវត្ថុវិលជុំវិញផ្កាយ"));
        VOCAB_BANK.add(new WordPair("Galaxy", "កាឡាក់ស៊ី", "Gravitationally bound system of stars and stellar dust", "ប្រព័ន្ធផ្កាយរាប់ពាន់លានក្នុងចក្រវាល"));
        VOCAB_BANK.add(new WordPair("Coral", "ផ្កាថ្ម", "Marine invertebrates that build vibrant reefs", "សត្វសមុទ្រដែលកកើតជាថ្មប៉ប្រះទឹក"));
        VOCAB_BANK.add(new WordPair("Fossil", "ហ្វូស៊ីល", "Preserved remains of ancient organisms", "សំណល់សត្វរុក្ខជាតិបុរាណរាប់លានឆ្នាំ"));
        VOCAB_BANK.add(new WordPair("Knight", "អ្នកក្លាហាន", "Noble armored warrior serving a sovereign", "អ្នកចម្បាំងពាក់អាវក្រោះសម័យកណ្តាល"));
        VOCAB_BANK.add(new WordPair("Jungle", "ព្រៃស្រោង", "Dense forest with rich tropical biodiversity", "ព្រៃក្រាស់ដែលសម្បូរដោយជីវិតសត្វព្រៃ"));
        VOCAB_BANK.add(new WordPair("Meteor", "ផ្កាយព្រះគ្រោះ", "Small body entering Earth's atmosphere producing light", "ដុំថ្មអាកាសដែលធ្លាក់មកផែនដី"));
        VOCAB_BANK.add(new WordPair("Glacier", "ផ្ទាំងទឹកកក", "Large persistent body of dense moving ice", "ផ្ទាំងទឹកកកដ៏ធំរំកិលយឺតៗ"));
    }

    private final Dashboard dashboard;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    private final JPanel header;
    private final JLabel timerLabel;
    private final JLabel scoreLabel;
    private final JLabel pairsLabel;
    private final UITheme.ProgressBar timerBar;

    private final JPanel wordsColumn;
    private final JPanel defsColumn;

    private final JPanel gameOverPanel;
    private JLabel overTitle;
    private JLabel overScoreLabel;
    private UITheme.Confetti confetti;

    private Timer gameTimer;
    private int secondsRemaining = 60;
    private int score = 0;
    private int pairsMatched = 0;
    private int currentRoundPairsCount = 0;

    private JButton selectedWordBtn = null;
    private JButton selectedDefBtn = null;
    private WordPair selectedWordPair = null;
    private WordPair selectedDefPair = null;

    private List<WordPair> activePairs = new ArrayList<>();
    private final Runnable langListener = this::refreshLanguage;

    public MatchGameScreen(Dashboard dashboard) {
        super(new BorderLayout());
        this.dashboard = dashboard;
        setOpaque(false);

        // Header
        header = buildHeader();

        // Play Panel
        JPanel playPanel = new JPanel(new BorderLayout(0, 12));
        playPanel.setOpaque(false);

        // Stats row
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        statsRow.setOpaque(false);

        timerLabel = new JLabel("[T] 60s");
        timerLabel.setFont(UITheme.displayFont(Font.BOLD, 16));
        timerLabel.setForeground(UITheme.GREEN);
        statsRow.add(timerLabel);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(UITheme.displayFont(Font.BOLD, 16));
        scoreLabel.setForeground(UITheme.GOLD);
        statsRow.add(scoreLabel);

        pairsLabel = new JLabel(I18n.get("match_pairs", 0, 5));
        pairsLabel.setFont(UITheme.displayFont(Font.BOLD, 16));
        pairsLabel.setForeground(UITheme.TEAL);
        statsRow.add(pairsLabel);

        JPanel topStats = new JPanel(new BorderLayout(0, 6));
        topStats.setOpaque(false);
        topStats.add(statsRow, BorderLayout.NORTH);

        timerBar = new UITheme.ProgressBar();
        timerBar.setPreferredSize(new Dimension(600, 8));
        timerBar.setMaximumSize(new Dimension(600, 8));
        topStats.add(timerBar, BorderLayout.SOUTH);

        playPanel.add(topStats, BorderLayout.NORTH);

        // Matching Grid: 2 columns
        JPanel matchGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        matchGrid.setOpaque(false);
        matchGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        wordsColumn = new JPanel(new GridLayout(5, 1, 0, 10));
        wordsColumn.setOpaque(false);
        matchGrid.add(wordsColumn);

        defsColumn = new JPanel(new GridLayout(5, 1, 0, 10));
        defsColumn.setOpaque(false);
        matchGrid.add(defsColumn);

        playPanel.add(matchGrid, BorderLayout.CENTER);

        // Game Over Panel
        gameOverPanel = buildGameOverPanel();

        content.setOpaque(false);
        content.add(playPanel, VIEW_PLAY);
        content.add(gameOverPanel, VIEW_OVER);

        JPanel card = UITheme.card(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X, UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X));
        UIUtil.fixedSize(card, 960, 720);
        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        JPanel root = UITheme.pageRoot(card);
        UITheme.autoScale(root, 1040, 800, 0.85, 1.5);
        add(root, BorderLayout.CENTER);

        cards.show(content, VIEW_PLAY);
        I18n.addLanguageListener(langListener);
        startNewGame();
    }

    private JPanel buildHeader() {
        JButton back = UITheme.backButton(I18n.get("back_to_games"), UITheme.CORAL);
        UIUtil.fixedSize(back, 180, UITheme.BTN_H);
        back.addActionListener(e -> {
            stopGameTimer();
            dashboard.showDashboard();
        });

        return UITheme.screenHeader(back, I18n.get("game_match_title"), 28);
    }

    private JPanel buildGameOverPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        confetti = new UITheme.Confetti(panel);

        panel.add(Box.createVerticalStrut(40));
        overTitle = new JLabel("TIME'S UP!", SwingConstants.CENTER);
        overTitle.setFont(UITheme.displayFont(Font.BOLD, 36));
        overTitle.setForeground(UITheme.GOLD);
        overTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(overTitle);

        panel.add(Box.createVerticalStrut(14));
        overScoreLabel = new JLabel("Pairs Matched: 0  |  Points: +0", SwingConstants.CENTER);
        overScoreLabel.setFont(UITheme.bodyFont(Font.BOLD, 20));
        overScoreLabel.setForeground(UITheme.TEXT);
        overScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(overScoreLabel);

        panel.add(Box.createVerticalStrut(30));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);

        JButton againBtn = UITheme.primaryButton(I18n.get("quiz_play_again"));
        UIUtil.fixedSize(againBtn, 180, UITheme.BTN_H);
        againBtn.addActionListener(e -> startNewGame());
        btnRow.add(againBtn);

        JButton dashBtn = UITheme.ghostButton(I18n.get("back_to_dashboard"), UITheme.CORAL);
        UIUtil.fixedSize(dashBtn, 200, UITheme.BTN_H);
        dashBtn.addActionListener(e -> dashboard.showDashboard());
        btnRow.add(dashBtn);

        panel.add(btnRow);
        return panel;
    }

    public void startNewGame() {
        stopGameTimer();
        secondsRemaining = 60;
        score = 0;
        pairsMatched = 0;
        timerLabel.setText("[T] 60s");
        scoreLabel.setText("Score: 0");
        timerBar.setProgress(1.0);

        cards.show(content, VIEW_PLAY);
        loadNextRound();

        gameTimer = new Timer(1000, e -> {
            secondsRemaining--;
            timerLabel.setText("[T] " + secondsRemaining + "s");
            timerBar.setProgress(secondsRemaining / 60.0);

            if (secondsRemaining <= 10) {
                timerLabel.setForeground(UITheme.CORAL);
                SoundUtil.playClick();
            }

            if (secondsRemaining <= 0) {
                endGame();
            }
        });
        gameTimer.start();
    }

    private void stopGameTimer() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    private void loadNextRound() {
        wordsColumn.removeAll();
        defsColumn.removeAll();
        selectedWordBtn = null;
        selectedDefBtn = null;
        selectedWordPair = null;
        selectedDefPair = null;

        List<WordPair> pool = new ArrayList<>(VOCAB_BANK);
        Collections.shuffle(pool);
        activePairs = pool.subList(0, Math.min(5, pool.size()));
        currentRoundPairsCount = activePairs.size();
        pairsLabel.setText(I18n.get("match_pairs", pairsMatched, (pairsMatched + currentRoundPairsCount)));

        // Shuffle words and definitions independently
        List<WordPair> shuffledWords = new ArrayList<>(activePairs);
        List<WordPair> shuffledDefs = new ArrayList<>(activePairs);
        Collections.shuffle(shuffledWords);
        Collections.shuffle(shuffledDefs);

        for (WordPair wp : shuffledWords) {
            JButton btn = createMatchButton(wp.getWord(), UITheme.TEAL);
            btn.addActionListener(e -> selectWord(btn, wp));
            wordsColumn.add(btn);
        }

        for (WordPair dp : shuffledDefs) {
            JButton btn = createMatchButton("<html><center style='width: 320px;'>" + dp.getDef() + "</center></html>", UITheme.VIOLET);
            btn.addActionListener(e -> selectDef(btn, dp));
            defsColumn.add(btn);
        }

        wordsColumn.revalidate();
        wordsColumn.repaint();
        defsColumn.revalidate();
        defsColumn.repaint();
    }

    private JButton createMatchButton(String text, Color accent) {
        JButton btn = UITheme.ghostButton(text, accent);
        btn.setFont(UITheme.fontFor(text, Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(380, 52));
        return btn;
    }

    private void selectWord(JButton btn, WordPair pair) {
        if (selectedWordBtn != null) {
            selectedWordBtn.setBackground(new Color(0x182438));
        }
        selectedWordBtn = btn;
        selectedWordPair = pair;
        btn.setBackground(new Color(0x008080));
        SoundUtil.playClick();
        checkMatch();
    }

    private void selectDef(JButton btn, WordPair pair) {
        if (selectedDefBtn != null) {
            selectedDefBtn.setBackground(new Color(0x182438));
        }
        selectedDefBtn = btn;
        selectedDefPair = pair;
        btn.setBackground(new Color(0x4a2580));
        SoundUtil.playClick();
        checkMatch();
    }

    private void checkMatch() {
        if (selectedWordPair == null || selectedDefPair == null) {
            return;
        }

        if (selectedWordPair == selectedDefPair) {
            // Correct Match!
            SoundUtil.playCorrect();
            score += 25;
            pairsMatched++;
            currentRoundPairsCount--;
            scoreLabel.setText("Score: " + score);
            pairsLabel.setText(I18n.get("match_pairs", pairsMatched, pairsMatched + currentRoundPairsCount));

            selectedWordBtn.setEnabled(false);
            selectedDefBtn.setEnabled(false);
            selectedWordBtn.setBackground(new Color(0x27ae60));
            selectedDefBtn.setBackground(new Color(0x27ae60));

            selectedWordBtn = null;
            selectedDefBtn = null;
            selectedWordPair = null;
            selectedDefPair = null;

            if (currentRoundPairsCount <= 0) {
                // Round Complete! Load next 5 pairs
                Timer roundTimer = new Timer(600, e -> loadNextRound());
                roundTimer.setRepeats(false);
                roundTimer.start();
            }
        } else {
            // Wrong Match
            SoundUtil.playError();
            final JButton wb = selectedWordBtn;
            final JButton db = selectedDefBtn;
            wb.setBackground(new Color(0xc0392b));
            db.setBackground(new Color(0xc0392b));

            Timer resetTimer = new Timer(400, e -> {
                wb.setBackground(new Color(0x182438));
                db.setBackground(new Color(0x182438));
            });
            resetTimer.setRepeats(false);
            resetTimer.start();

            selectedWordBtn = null;
            selectedDefBtn = null;
            selectedWordPair = null;
            selectedDefPair = null;
        }
    }

    private void endGame() {
        stopGameTimer();
        SoundUtil.playVictory();

        overScoreLabel.setText("Pairs Matched: " + pairsMatched + "  |  Points: +" + score);
        if (dashboard != null && score > 0) {
            dashboard.addGamePoints(score);
        }

        cards.show(content, VIEW_OVER);
        if (confetti != null) {
            confetti.launch();
        }
    }

    private void refreshLanguage() {
        if (header != null) {
            removeAll();
            JPanel card = UITheme.card(new BorderLayout());
            card.setBorder(BorderFactory.createEmptyBorder(UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X, UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X));
            UIUtil.fixedSize(card, 960, 720);
            card.add(buildHeader(), BorderLayout.NORTH);
            card.add(content, BorderLayout.CENTER);
            JPanel root = UITheme.pageRoot(card);
            UITheme.autoScale(root, 1040, 800, 0.85, 1.5);
            add(root, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }
}
