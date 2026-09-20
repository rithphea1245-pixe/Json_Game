package com.worldofwonder.view;

import com.worldofwonder.model.*;
import com.worldofwonder.controller.*;

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
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CupsWaterSortGameScreen extends JPanel {

    private static final String VIEW_DIFFICULTY = "difficulty";
    private static final String VIEW_GAME = "game";
    private static final String VIEW_COMPLETE = "complete";

    private static final int TUBE_W = 56;
    private static final int TUBE_GAP = 14;
    private static final int SEG_H = 30;
    private static final int BASE_H = 16;
    private static final int WALL = 5;
    private static final int TOP_PAD = 44;
    private static final int SIDE_PAD = 20;
    private static final int BOTTOM_PAD = 20;
    private static final int EMPTY_TUBES = 2;

    private static final Color[] WATER_COLORS = {
            new Color(0xE74C3C),
            new Color(0x2ECC71),
            new Color(0x3498DB),
            new Color(0xF1C40F),
            new Color(0x9B59B6),
            new Color(0xE67E22),
            new Color(0x1ABC9C),
            new Color(0xE84393),
    };

    private static final String[] WATER_NAMES = {
            "Ruby", "Emerald", "Ocean", "Gold", "Amethyst", "Tangerine", "Teal", "Orchid",
    };

    private static String colorName(Color c) {
        if (c == null) return com.worldofwonder.util.I18n.get("tube_empty");
        for (int i = 0; i < WATER_COLORS.length; i++) {
            if (WATER_COLORS[i].getRGB() == c.getRGB()) {
                switch (i) {
                    case 0: return com.worldofwonder.util.I18n.get("tube_ruby");
                    case 1: return com.worldofwonder.util.I18n.get("tube_emerald");
                    case 2: return com.worldofwonder.util.I18n.get("tube_ocean");
                    case 3: return com.worldofwonder.util.I18n.get("tube_gold");
                    case 4: return com.worldofwonder.util.I18n.get("tube_amethyst");
                    case 5: return com.worldofwonder.util.I18n.get("tube_tangerine");
                    case 6: return com.worldofwonder.util.I18n.get("tube_teal");
                    case 7: return com.worldofwonder.util.I18n.get("tube_orchid");
                }
            }
        }
        return "Water";
    }

    private final Dashboard dashboard;
    private final GameBoard board;
    private final JLabel difficultyLabel;
    private final JLabel movesLabel;
    private final JLabel completeText;
    private JPanel completePanel;
    private UITheme.Confetti confetti;

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    private int capacity;
    private int colorCount;
    private final List<Tube> tubes = new ArrayList<>();
    private final List<List<Color>> initialTubes = new ArrayList<>();
    private Tube selected;
    private int moves;
    private boolean won;
    private boolean pouring;
    private PourAnim pourAnim;
    private int extraTubesUsed = 0;
    private JButton extraTubeBtn;
    private JButton restartBtn;
    private JButton changeDiffBtn;
    private JButton newGameBtn;
    private JButton exitBtn;
    private final Runnable langListener = this::refreshLanguage;

    public CupsWaterSortGameScreen(Dashboard dashboard) {
        super(new BorderLayout());
        this.dashboard = dashboard;
        setOpaque(false);
        this.capacity = 4;
        this.colorCount = 4;

        this.board = new GameBoard();
        this.difficultyLabel = UITheme.badge("", UITheme.TEAL);
        this.movesLabel = UITheme.badge("Moves: 0", UITheme.GOLD);
        this.completeText = new JLabel("", SwingConstants.CENTER);
        completeText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, UITheme.FONT_CARD_TITLE));
        completeText.setForeground(UITheme.TEXT_MUTED);

        this.completePanel = buildCompletePanel();
        this.confetti = new UITheme.Confetti(completePanel);

        content.setOpaque(false);
        content.add(buildDifficultyPanel(), VIEW_DIFFICULTY);
        content.add(buildGamePanel(), VIEW_GAME);
        content.add(completePanel, VIEW_COMPLETE);

        this.viewCard = UITheme.card(new BorderLayout());
        viewCard.setBorder(BorderFactory.createEmptyBorder(UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X, UITheme.PAD_CARD_Y, UITheme.PAD_CARD_X));
        UIUtil.fixedSize(viewCard, 1020, 700);
        this.headerPanel = buildHeader();
        viewCard.add(headerPanel, BorderLayout.NORTH);
        viewCard.add(content, BorderLayout.CENTER);

        JPanel root = UITheme.screenPage(viewCard);

        UITheme.autoScale(root, 1100, 790, 0.85, 1.5);

        add(root, BorderLayout.CENTER);

        cards.show(content, VIEW_DIFFICULTY);
        newGame();
        I18n.addLanguageListener(langListener);
    }

    private final JPanel viewCard;
    private JPanel headerPanel;

    /** Called when language changes - refreshes labels in cups water sort screen. */
    private void refreshLanguage() {
        if (headerPanel != null && viewCard != null) {
            viewCard.remove(headerPanel);
            headerPanel = buildHeader();
            viewCard.add(headerPanel, BorderLayout.NORTH);
        }
        if (movesLabel != null) {
            movesLabel.setText(I18n.get("cups_moves", moves));
        }
        if (difficultyLabel != null) {
            String diffStr = colorCount <= 4 ? com.worldofwonder.util.I18n.get("diff_easy")
                           : colorCount <= 6 ? com.worldofwonder.util.I18n.get("diff_medium")
                           : com.worldofwonder.util.I18n.get("diff_hard");
            difficultyLabel.setText(com.worldofwonder.util.I18n.get("choose_difficulty") + ": " + diffStr);
        }
        if (restartBtn != null) {
            restartBtn.setText(com.worldofwonder.util.I18n.get("cups_restart"));
            restartBtn.setFont(UITheme.fontFor(restartBtn.getText(), Font.BOLD, 13));
        }
        if (extraTubeBtn != null) {
            extraTubeBtn.setText(com.worldofwonder.util.I18n.get("cups_extra_tube"));
            extraTubeBtn.setFont(UITheme.fontFor(extraTubeBtn.getText(), Font.BOLD, 13));
        }
        if (changeDiffBtn != null) {
            changeDiffBtn.setText(com.worldofwonder.util.I18n.get("cups_change_diff"));
            changeDiffBtn.setFont(UITheme.fontFor(changeDiffBtn.getText(), Font.BOLD, 13));
        }
        if (newGameBtn != null) {
            newGameBtn.setText(com.worldofwonder.util.I18n.get("cups_new_game"));
            newGameBtn.setFont(UITheme.fontFor(newGameBtn.getText(), Font.BOLD, 13));
        }
        if (exitBtn != null) {
            exitBtn.setText(com.worldofwonder.util.I18n.get("exit_to_games"));
            exitBtn.setFont(UITheme.fontFor(exitBtn.getText(), Font.BOLD, 13));
        }
        if (board != null) {
            board.repaint();
        }
        revalidate();
        repaint();
    }


    private JPanel buildHeader() {
        JButton back = UITheme.backButton(com.worldofwonder.util.I18n.get("back_to_games"), UITheme.CORAL);
        UIUtil.fixedSize(back, 190, UITheme.BTN_H);
        back.addActionListener(e -> dashboard.showDashboard());
        return UITheme.screenHeader(back, com.worldofwonder.util.I18n.get("game_cups_title"), 30);
    }

    private JPanel buildDifficultyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        panel.add(UITheme.sectionTitle(com.worldofwonder.util.I18n.get("choose_difficulty"), 26), BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 20));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(28, 34, 0, 34));
        buttons.add(difficultyTile(com.worldofwonder.util.I18n.get("diff_easy"), com.worldofwonder.util.I18n.get("diff_easy_desc"), UITheme.GREEN, 4));
        buttons.add(difficultyTile(com.worldofwonder.util.I18n.get("diff_medium"), com.worldofwonder.util.I18n.get("diff_medium_desc"), UITheme.GOLD, 6));
        buttons.add(difficultyTile(com.worldofwonder.util.I18n.get("diff_hard"), com.worldofwonder.util.I18n.get("diff_hard_desc"), UITheme.CORAL, 8));

        JPanel wrap = UIUtil.centered(buttons);
        wrap.setOpaque(false);
        panel.add(wrap, BorderLayout.CENTER);
        return panel;
    }

    private JButton difficultyTile(String name, String description, Color accent, int colors) {
        UITheme.TileButton button = new UITheme.TileButton(name, description, accent);
        button.setDark(true);
        button.setSubtitleColor(new Color(0xd0e8f5));
        UIUtil.fixedSize(button, 580, 100);
        button.addActionListener(e -> startGame(colors));
        return button;
    }

    private JPanel buildGamePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 6));
        meta.setOpaque(false);
        meta.add(difficultyLabel);
        meta.add(movesLabel);
        panel.add(meta, BorderLayout.NORTH);

        JPanel boardCenter = new JPanel(new GridBagLayout());
        boardCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        boardCenter.add(board, gbc);
        panel.add(boardCenter, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        restartBtn = UITheme.iconPillButton(UITheme.VectorIcon.REFRESH, com.worldofwonder.util.I18n.get("cups_restart"), UITheme.TEAL);
        UIUtil.fixedSize(restartBtn, 130, 46);
        restartBtn.addActionListener(e -> restartCurrentGame());
        actions.add(restartBtn);

        extraTubeBtn = UITheme.iconPillButton(UITheme.VectorIcon.PLUS, com.worldofwonder.util.I18n.get("cups_extra_tube"), UITheme.GREEN);
        UIUtil.fixedSize(extraTubeBtn, 160, 46);
        extraTubeBtn.setToolTipText("Add an extra empty tube to solve tricky puzzles");
        extraTubeBtn.addActionListener(e -> addExtraTube());
        actions.add(extraTubeBtn);

        changeDiffBtn = UITheme.iconPillButton(UITheme.VectorIcon.GEAR, com.worldofwonder.util.I18n.get("cups_change_diff"), UITheme.VIOLET);
        UIUtil.fixedSize(changeDiffBtn, 160, 46);
        changeDiffBtn.addActionListener(e -> showDifficulty());
        actions.add(changeDiffBtn);

        newGameBtn = UITheme.primaryButton(com.worldofwonder.util.I18n.get("cups_new_game"));
        UIUtil.fixedSize(newGameBtn, 130, 46);
        newGameBtn.addActionListener(e -> newGame());
        actions.add(newGameBtn);

        exitBtn = UITheme.iconPillButton(UITheme.VectorIcon.ARROW_LEFT, com.worldofwonder.util.I18n.get("exit_to_games"), UITheme.CORAL);
        UIUtil.fixedSize(exitBtn, 150, 46);
        exitBtn.setToolTipText("Return to the main game selection menu");
        exitBtn.addActionListener(e -> dashboard.showDashboard());
        actions.add(exitBtn);

        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCompletePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (confetti != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UITheme.paintQuality(g2);
                    confetti.paint(g2);
                    g2.dispose();
                }
            }
        };
        panel.setOpaque(false);

        UITheme.GradientTextLabel title =
                new UITheme.GradientTextLabel(com.worldofwonder.util.I18n.get("cups_solved_title"), 34, UITheme.GOLD, UITheme.TEAL);
        panel.add(title, BorderLayout.NORTH);
        panel.add(completeText, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 14));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(20, 34, 0, 34));

        JButton again = UITheme.primaryButton(com.worldofwonder.util.I18n.get("quiz_play_again"));
        UIUtil.fixedSize(again, 340, 54);
        again.addActionListener(e -> startGame(colorCount));
        buttons.add(again);

        JButton change = UITheme.ghostButton(com.worldofwonder.util.I18n.get("cups_change_diff"), UITheme.TEXT_MUTED);
        UIUtil.fixedSize(change, 340, 54);
        change.addActionListener(e -> showDifficulty());
        buttons.add(change);

        JButton dashboardBtn = UITheme.iconPillButton(UITheme.VectorIcon.ARROW_LEFT, com.worldofwonder.util.I18n.get("back_to_dashboard"), UITheme.TEXT_MUTED);
        UIUtil.fixedSize(dashboardBtn, 340, 54);
        dashboardBtn.addActionListener(e -> dashboard.showDashboard());
        buttons.add(dashboardBtn);

        JPanel wrap = UIUtil.centered(buttons);
        wrap.setOpaque(false);
        panel.add(wrap, BorderLayout.SOUTH);
        return panel;
    }

    private void startGame(int colors) {
        colorCount = colors;
        capacity = 4;
        won = false;
        selected = null;
        moves = 0;
        String diffStr = colors <= 4 ? com.worldofwonder.util.I18n.get("diff_easy")
                       : colors <= 6 ? com.worldofwonder.util.I18n.get("diff_medium")
                       : com.worldofwonder.util.I18n.get("diff_hard");
        difficultyLabel.setText(com.worldofwonder.util.I18n.get("choose_difficulty") + ": " + diffStr);
        newGame();
        cards.show(content, VIEW_GAME);
    }


    private void showDifficulty() {
        cards.show(content, VIEW_DIFFICULTY);
    }

    private void addExtraTube() {
        if (won || pouring || extraTubesUsed >= 1) {
            return;
        }
        extraTubesUsed++;
        if (extraTubeBtn != null) {
            extraTubeBtn.setEnabled(false);
        }
        tubes.add(new Tube(capacity));
        SoundUtil.playHint();
        board.revalidate();
        board.repaint();
    }

    private void restartCurrentGame() {
        if (initialTubes.isEmpty()) {
            newGame();
            return;
        }
        won = false;
        selected = null;
        moves = 0;
        pouring = false;
        extraTubesUsed = 0;
        if (extraTubeBtn != null) {
            extraTubeBtn.setEnabled(true);
        }
        if (pourAnim != null) {
            pourAnim.timer.stop();
            pourAnim = null;
        }
        board.cancelShake();
        updateMoveLabel();

        tubes.clear();
        for (List<Color> initList : initialTubes) {
            Tube t = new Tube(capacity);
            t.segments.addAll(initList);
            tubes.add(t);
        }
        board.revalidate();
        board.repaint();
    }

    private void newGame() {
        won = false;
        selected = null;
        moves = 0;
        pouring = false;
        extraTubesUsed = 0;
        if (extraTubeBtn != null) {
            extraTubeBtn.setEnabled(true);
        }
        if (pourAnim != null) {
            pourAnim.timer.stop();
            pourAnim = null;
        }
        board.cancelShake();
        updateMoveLabel();

        tubes.clear();
        while (true) {
            tubes.clear();
            List<Color> pool = new ArrayList<>();
            for (int c = 0; c < colorCount; c++) {
                for (int k = 0; k < capacity; k++) {
                    pool.add(WATER_COLORS[c]);
                }
            }
            Collections.shuffle(pool);

            int index = 0;
            for (int i = 0; i < colorCount; i++) {
                Tube tube = new Tube(capacity);
                for (int k = 0; k < capacity; k++) {
                    tube.segments.add(pool.get(index++));
                }
                tubes.add(tube);
            }
            for (int i = 0; i < EMPTY_TUBES; i++) {
                tubes.add(new Tube(capacity));
            }

            if (!startsSolved()) {
                break;
            }
        }

        initialTubes.clear();
        for (Tube t : tubes) {
            initialTubes.add(new ArrayList<>(t.segments));
        }

        board.revalidate();
        board.repaint();
    }

    private boolean startsSolved() {
        for (Tube tube : tubes) {
            if (tube.segments.isEmpty()) {
                continue;
            }
            Color first = tube.segments.get(0);
            boolean mono = true;
            for (Color segment : tube.segments) {
                if (!segment.equals(first)) {
                    mono = false;
                    break;
                }
            }
            if (mono) {
                return true;
            }
        }
        return false;
    }

    private void updateMoveLabel() {
        movesLabel.setText(com.worldofwonder.util.I18n.get("cups_moves", moves));
    }

    private void onTubeClick(Tube clicked) {
        if (won || pouring) {
            return;
        }
        if (selected == null) {
            if (!clicked.isEmpty()) {
                selected = clicked;
                SoundUtil.playLetterSelect(0);
                board.repaint();
            }
            return;
        }
        if (clicked == selected) {
            selected = null;
            board.repaint();
            return;
        }
        int count = planned(selected, clicked);
        if (count <= 0) {
            SoundUtil.playError();
            board.shake(clicked);
            board.repaint();
            return;
        }
        Tube from = selected;
        selected = null;
        startPour(from, clicked, count);
    }

    private int planned(Tube from, Tube to) {
        if (from.isEmpty() || to.isFull()) {
            return 0;
        }
        if (!to.isEmpty() && !to.topColor().equals(from.topColor())) {
            return 0;
        }
        Color color = from.topColor();
        int count = 0;
        for (int i = from.segments.size() - 1; i >= 0; i--) {
            if (to.segments.size() + count >= to.capacity || !color.equals(from.segments.get(i))) {
                break;
            }
            count++;
        }
        return count;
    }

    private void startPour(Tube from, Tube to, int count) {
        pouring = true;
        SoundUtil.playPour();
        pourAnim = new PourAnim(from, to, from.topColor(), count);
    }

    private class PourAnim {
        final Tube from;
        final Tube to;
        final Color color;
        final int count;
        final javax.swing.Timer timer;
        float t;
        boolean committed;

        PourAnim(Tube from, Tube to, Color color, int count) {
            this.from = from;
            this.to = to;
            this.color = color;
            this.count = count;
            timer = new javax.swing.Timer(16, e -> tick());
            timer.start();
        }

        void tick() {
            t += 1f / 32f;
            if (t >= 0.5f && !committed) {
                committed = true;
                for (int i = 0; i < count; i++) {
                    to.segments.add(from.segments.remove(from.segments.size() - 1));
                }
                moves++;
                updateMoveLabel();
                if (isSolved()) {
                    won = true;
                    SoundUtil.playVictory();
                    int earnedPoints = Math.max(15, colorCount * 20 - moves * 2);
                    String rating = (moves <= colorCount * 5 && extraTubesUsed == 0) ? "FLAWLESS (3/3)" : (moves <= colorCount * 8 ? "GREAT JOB (2/3)" : "SOLVED (1/3)");
                    completeText.setText("<html><center><span style='font-size:20px;letter-spacing:1px;color:#ffd700;font-weight:bold;'>" + rating + "</span><br><br>You sorted all the colors in " + moves + " moves!<br>"
                            + (extraTubesUsed > 0 ? "<span style='color:#a0b0d0;font-size:12px;'><i>(+1 Extra Tube Booster used)</i></span><br>" : "")
                            + "<span style='color:#ffd700;font-size:18px;'>+" + earnedPoints + " Points Earned!</span></center></html>");
                    completeText.setForeground(UITheme.GOLD);
                    SwingUtilities.invokeLater(() -> {
                        cards.show(content, VIEW_COMPLETE);
                        confetti.launch();
                    });
                    syncCompletionToBackend(earnedPoints);
                }
            }
            if (t >= 1f) {
                timer.stop();
                pouring = false;
            }
            board.repaint();
        }

        void paint(Graphics2D g2) {
            int fi = tubes.indexOf(from);
            int ti = tubes.indexOf(to);
            if (fi < 0 || ti < 0) {
                return;
            }
            Rectangle fr = getTubeRect(fi);
            Rectangle tr = getTubeRect(ti);
            float fx = fr.x + fr.width / 2f;
            float fy = fr.y;
            float tx = tr.x + tr.width / 2f;
            float ty = tr.y;
            float lift = 2.6f * SEG_H;

            float px;
            float py;
            float alpha = 1f;
            if (t < 0.35f) {
                float k = smooth(t / 0.35f);
                px = fx;
                py = fy - lift * k;
            } else if (t < 0.65f) {
                float k = (t - 0.35f) / 0.30f;
                px = fx + (tx - fx) * k;
                py = fy - lift;
            } else {
                float k = smooth((t - 0.65f) / 0.35f);
                px = tx;
                py = (fy - lift) + lift * k;
                alpha = Math.max(0f, 1f - (t - 0.5f) * 2f);
            }

            int bw = TUBE_W - 2 * WALL;
            int bh = SEG_H * Math.min(count, 3);
            int bx = Math.round(px - bw / 2f);
            int by = Math.round(py - bh);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.setColor(color);
            g2.fillRoundRect(bx, by, bw, bh, 12, 12);
            g2.setColor(new Color(255, 255, 255, 110));
            g2.setStroke(new java.awt.BasicStroke(1.4f));
            g2.drawRoundRect(bx, by, bw, bh, 12, 12);
            g2.setComposite(AlphaComposite.SrcOver.derive(1f));
        }

        private float smooth(float k) {
            return k * k * (3f - 2f * k);
        }
    }

    private void syncCompletionToBackend(int earnedPoints) {
        dashboard.addGamePoints(earnedPoints);
    }

    private boolean isSolved() {
        for (Tube tube : tubes) {
            if (tube.segments.isEmpty()) {
                continue;
            }
            if (tube.segments.size() != capacity) {
                return false;
            }
            Color first = tube.segments.get(0);
            for (Color segment : tube.segments) {
                if (!segment.equals(first)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Rectangle getTubeRect(int index) {
        int x = SIDE_PAD + index * (TUBE_W + TUBE_GAP);
        int y = TOP_PAD;
        return new Rectangle(x, y, TUBE_W, capacity * SEG_H + BASE_H);
    }

    private static class Tube {
        final int capacity;
        final List<Color> segments = new ArrayList<>();

        Tube(int capacity) {
            this.capacity = capacity;
        }

        boolean isEmpty() {
            return segments.isEmpty();
        }

        boolean isFull() {
            return segments.size() >= capacity;
        }

        Color topColor() {
            return segments.isEmpty() ? null : segments.get(segments.size() - 1);
        }
    }

    private class GameBoard extends JPanel {

        private Tube hoverTube;
        private Tube shakeTube;
        private float shakeT = -1f;
        private javax.swing.Timer shakeTimer;

        GameBoard() {
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Tube clicked = tubeAt(e.getPoint());
                    if (clicked != null) {
                        onTubeClick(clicked);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (hoverTube != null) {
                        hoverTube = null;
                        repaint();
                    }
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Tube tube = tubeAt(e.getPoint());
                    if (tube != hoverTube) {
                        hoverTube = tube;
                        repaint();
                    }
                }
            });
        }

        void cancelShake() {
            shakeTube = null;
            shakeT = -1f;
            if (shakeTimer != null) {
                shakeTimer.stop();
                shakeTimer = null;
            }
        }

        void shake(Tube tube) {
            if (tube == null) {
                return;
            }
            shakeTube = tube;
            shakeT = 0f;
            if (shakeTimer != null) {
                shakeTimer.stop();
            }
            shakeTimer = new javax.swing.Timer(16, e -> {
                shakeT += 1f / 24f;
                if (shakeT >= 1f) {
                    shakeT = -1f;
                    shakeTube = null;
                    shakeTimer.stop();
                }
                repaint();
            });
            shakeTimer.start();
        }

        @Override
        public Dimension getPreferredSize() {
            int width = SIDE_PAD * 2 + tubes.size() * TUBE_W + (tubes.size() - 1) * TUBE_GAP;
            int height = TOP_PAD + capacity * SEG_H + BASE_H + BOTTOM_PAD;
            return new Dimension(width, height);
        }

        private Tube tubeAt(Point p) {
            Dimension natural = getPreferredSize();
            double sx = natural.width > 0 ? getWidth() / (double) natural.width : 1.0;
            double sy = natural.height > 0 ? getHeight() / (double) natural.height : 1.0;
            int x = sx > 0 ? (int) Math.round(p.x / sx) : p.x;
            int y = sy > 0 ? (int) Math.round(p.y / sy) : p.y;
            for (int i = 0; i < tubes.size(); i++) {
                if (getTubeRect(i).contains(x, y)) {
                    return tubes.get(i);
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.paintQuality(g2);

            Dimension natural = getPreferredSize();
            double sx = natural.width > 0 ? getWidth() / (double) natural.width : 1.0;
            double sy = natural.height > 0 ? getHeight() / (double) natural.height : 1.0;
            if (sx > 0 && sy > 0 && (sx != 1.0 || sy != 1.0)) {
                g2.scale(sx, sy);
            }

            for (int i = 0; i < tubes.size(); i++) {
                Tube tube = tubes.get(i);
                int dx = (shakeTube == tube && shakeT >= 0f)
                        ? (int) Math.round(Math.sin(shakeT * Math.PI * 3) * 5 * (1 - shakeT)) : 0;
                if (dx != 0) {
                    g2.translate(dx, 0);
                }
                drawTube(g2, tube, getTubeRect(i), tube == selected, tube == hoverTube,
                        shakeTube == tube && shakeT >= 0f);
                if (dx != 0) {
                    g2.translate(-dx, 0);
                }
            }

            if (selected != null && !selected.isEmpty()) {
                drawTopBlob(g2, selected);
            }

            if (pourAnim != null && pouring) {
                pourAnim.paint(g2);
            }

            g2.dispose();
        }

        private void drawTube(Graphics2D g2, Tube tube, Rectangle r, boolean isSelected,
                              boolean isHovered, boolean shaking) {
            int bottom = r.y + capacity * SEG_H;
            int tubeH = capacity * SEG_H + BASE_H;

            g2.setColor(new Color(11, 26, 44, 175));
            g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, tubeH, 18, 18));
            g2.setColor(new Color(255, 255, 255, 40));
            g2.setStroke(new java.awt.BasicStroke(1.3f));
            g2.draw(new RoundRectangle2D.Float(r.x + 1.5f, r.y + 1.5f, r.width - 3f, tubeH - 3f, 17, 17));

            for (int j = 0; j < tube.segments.size(); j++) {
                int y = bottom - (j + 1) * SEG_H;
                Color color = tube.segments.get(j);
                g2.setColor(color);
                g2.fillRoundRect(r.x + WALL, y + 2, r.width - 2 * WALL, SEG_H - 3, 12, 12);
                g2.setColor(new Color(255, 255, 255, 48));
                g2.fillRoundRect(r.x + WALL + 3, y + 3, r.width - 2 * WALL - 6,
                        Math.max(4, (SEG_H - 3) / 3), 6, 6);
                g2.setColor(new Color(0, 0, 0, 26));
                g2.fillRoundRect(r.x + WALL, y + SEG_H - 6, r.width - 2 * WALL, 4, 4, 4);
            }

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            String countText = tube.segments.size() + "/" + capacity;
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(countText) + 16;
            int by = r.y - 26;
            g2.setColor(new Color(11, 26, 44, 230));
            g2.fillRoundRect(r.x + (r.width - bw) / 2, by, bw, 19, 9, 9);
            g2.setColor(UITheme.TEXT);
            g2.drawString(countText, r.x + (r.width - fm.stringWidth(countText)) / 2, by + 14);

            String name = tube.isEmpty() ? com.worldofwonder.util.I18n.get("tube_empty") : colorName(tube.topColor());
            Font nameFont = UITheme.fontFor(name, Font.BOLD, 12);
            g2.setFont(nameFont);
            fm = g2.getFontMetrics();
            int nw = fm.stringWidth(name);
            int nx = r.x + (r.width - nw) / 2;
            int ny = bottom + BASE_H + 6 + fm.getAscent();

            // Protective dark glass capsule badge to guarantee 100% contrast on any background
            g2.setColor(new Color(11, 26, 44, 210));
            g2.fillRoundRect(nx - 7, ny - fm.getAscent() - 2, nw + 14, fm.getHeight() + 4, 8, 8);
            g2.setColor(new Color(255, 255, 255, 45));
            g2.drawRoundRect(nx - 7, ny - fm.getAscent() - 2, nw + 14, fm.getHeight() + 4, 8, 8);

            g2.setColor(new Color(0xf1, 0xf5, 0xf9));
            g2.drawString(name, nx, ny);

            if (isSelected) {
                g2.setColor(new Color(32, 211, 194, 60));
                g2.setStroke(new java.awt.BasicStroke(8f));
                g2.draw(new RoundRectangle2D.Float(r.x - 6, r.y - 6, r.width + 12, tubeH + 12, 22, 22));
                g2.setColor(UITheme.TEAL);
                g2.setStroke(new java.awt.BasicStroke(4.5f));
                g2.draw(new RoundRectangle2D.Float(r.x - 3, r.y - 3, r.width + 6, tubeH + 6, 20, 20));
            } else if (shaking) {
                g2.setColor(new Color(255, 93, 93, 90));
                g2.setStroke(new java.awt.BasicStroke(5f));
                g2.draw(new RoundRectangle2D.Float(r.x - 4, r.y - 4, r.width + 8, tubeH + 8, 20, 20));
                g2.setColor(UITheme.ERROR);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(r.x - 2, r.y - 2, r.width + 4, tubeH + 4, 19, 19));
            } else if (isHovered) {
                g2.setColor(new Color(32, 211, 194, 55));
                g2.setStroke(new java.awt.BasicStroke(5f));
                g2.draw(new RoundRectangle2D.Float(r.x - 4, r.y - 4, r.width + 8, tubeH + 8, 20, 20));
                g2.setColor(new Color(32, 211, 194, 160));
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(r.x - 2, r.y - 2, r.width + 4, tubeH + 4, 19, 19));
            }
        }

        private void drawTopBlob(Graphics2D g2, Tube tube) {
            int index = tubes.indexOf(tube);
            Rectangle r = getTubeRect(index);
            Color color = tube.topColor();

            int blobW = TUBE_W - 2 * WALL;
            int blobH = SEG_H;
            int x = r.x + WALL;
            int y = r.y - blobH - 10;

            g2.setColor(color);
            g2.fillRoundRect(x, y, blobW, blobH, 12, 12);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new java.awt.BasicStroke(1.6f));
            g2.drawRoundRect(x, y, blobW, blobH, 12, 12);
        }
    }
}
