package com.worldofwonder.controller;

import com.worldofwonder.model.GameRepository;
import com.worldofwonder.model.Level;
import com.worldofwonder.model.Question;

import java.util.List;

/**
 * Controller managing quiz session questions, answers, and scoring calculations.
 */
public class QuizController {

    private final GameRepository gameRepository;
    private final GameController gameController;

    public QuizController(GameRepository gameRepository, GameController gameController) {
        this.gameRepository = gameRepository;
        this.gameController = gameController;
    }

    public List<Question> getQuestionsForLevel(int levelId) {
        return gameRepository.getQuestionsByLevelId(levelId);
    }

    public boolean validateAnswer(Question question, int selectedIndex) {
        if (question == null || selectedIndex < 0 || selectedIndex > 3) {
            return false;
        }
        char selectedChar = (char) ('A' + selectedIndex);
        String correct = question.getCorrectAnswer();
        return correct != null && correct.trim().equalsIgnoreCase(String.valueOf(selectedChar));
    }

    public int calculatePointsForQuestion(Level level, int totalQuestions) {
        if (level == null || totalQuestions <= 0) {
            return 10;
        }
        return Math.max(1, level.getPointReward() / totalQuestions);
    }

    public int awardQuizPoints(int userId, int pointsEarned) {
        return gameController.addPoints(userId, pointsEarned);
    }

    public int calculatePointsWithStreak(int basePoints, int currentStreak) {
        if (currentStreak >= 5) {
            return (int) Math.round(basePoints * 2.0); // 2x Flame Streak!
        } else if (currentStreak >= 3) {
            return (int) Math.round(basePoints * 1.5); // 1.5x Flame Streak!
        }
        return basePoints;
    }

    public int[] getFiftyFiftyExclusions(Question question) {
        if (question == null) return new int[0];
        String correct = question.getCorrectAnswer();
        if (correct == null || correct.trim().isEmpty()) return new int[0];
        char correctChar = Character.toUpperCase(correct.trim().charAt(0));
        int correctIndex = correctChar - 'A';

        java.util.List<Integer> wrongIndices = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != correctIndex) {
                wrongIndices.add(i);
            }
        }
        java.util.Collections.shuffle(wrongIndices);
        return new int[]{wrongIndices.get(0), wrongIndices.get(1)};
    }
}

