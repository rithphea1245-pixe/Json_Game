package com.worldofwonder.view;

import com.worldofwonder.model.*;
import com.worldofwonder.controller.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

public class MainUI extends JFrame {

    private static final String SCREEN_WELCOME = "welcome";
    private static final String SCREEN_DASHBOARD = "dashboard";
    private static final String SCREEN_QUIZ = "quiz";
    private static final String SCREEN_WORDSEARCH = "wordsearch";
    private static final String SCREEN_CUPS = "cups";
    private static final String SCREEN_WORDS = "words";
    private static final String SCREEN_MATCH = "match";

    private final CardLayout cards;
    private final JPanel cardsPanel;
    private final Dashboard dashboard;
    private final UITheme.FloatRoot background;

    private final AuthController authController;
    private final GameController gameController;
    private final QuizController quizController;

    public MainUI() {
        super("World of Wonder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(768, 580));

        this.authController = new AuthController();
        this.gameController = new GameController();
        this.quizController = new QuizController(new GameRepository(), gameController);

        this.cards = new CardLayout();
        this.cardsPanel = new JPanel(cards);
        this.cardsPanel.setOpaque(false);
        this.dashboard = new Dashboard(this);

        cardsPanel.add(new WelcomeScreen(this), SCREEN_WELCOME);
        cardsPanel.add(dashboard, SCREEN_DASHBOARD);
        cardsPanel.add(new QuizGameScreen(dashboard), SCREEN_QUIZ);
        cardsPanel.add(new WordSearchGameScreen(dashboard), SCREEN_WORDSEARCH);
        cardsPanel.add(new CupsWaterSortGameScreen(dashboard), SCREEN_CUPS);
        cardsPanel.add(new WordsOfWondersGameScreen(dashboard), SCREEN_WORDS);
        cardsPanel.add(new MatchGameScreen(dashboard), SCREEN_MATCH);

        this.background = UITheme.animatedRoot(new BorderLayout());
        background.add(cardsPanel, BorderLayout.CENTER);
        setContentPane(background);
        setSize(1920, 1080);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        showScreen(SCREEN_WELCOME);
        setVisible(true);
    }

    public void showScreen(String name) {
        cards.show(cardsPanel, name);
        if (background != null) {
            background.setTheme(name);
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    public void showDashboard() {
        showScreen(SCREEN_DASHBOARD);
    }

    public void showWelcome() {
        showScreen(SCREEN_WELCOME);
    }

    public void enterDashboard(String username, boolean isGuest, int userId, String token, int totalPoints) {
        dashboard.setUser(username, isGuest, userId, token, totalPoints);
        showScreen(SCREEN_DASHBOARD);
    }

    public AuthController getAuthController() {
        return authController;
    }

    public GameController getGameController() {
        return gameController;
    }

    public QuizController getQuizController() {
        return quizController;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainUI::new);
    }
}
