package com.worldofwonder.model;

public class Question {

    private int id;
    private int levelId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String hint;

    public Question() {
    }

    public Question(int levelId, String questionText, String optionA, String optionB,
                    String optionC, String optionD, String correctAnswer, String hint) {
        this.levelId = levelId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.hint = hint;
    }

    public Question(int id, int levelId, String questionText, String optionA, String optionB,
                    String optionC, String optionD, String correctAnswer, String hint) {
        this.id = id;
        this.levelId = levelId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.hint = hint;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    private String questionTextKm;
    private String optionAKm;
    private String optionBKm;
    private String optionCKm;
    private String optionDKm;
    private String hintKm;

    public String getQuestionTextKm() {
        return questionTextKm;
    }

    public void setQuestionTextKm(String questionTextKm) {
        this.questionTextKm = questionTextKm;
    }

    public String getOptionAKm() {
        return optionAKm;
    }

    public void setOptionAKm(String optionAKm) {
        this.optionAKm = optionAKm;
    }

    public String getOptionBKm() {
        return optionBKm;
    }

    public void setOptionBKm(String optionBKm) {
        this.optionBKm = optionBKm;
    }

    public String getOptionCKm() {
        return optionCKm;
    }

    public void setOptionCKm(String optionCKm) {
        this.optionCKm = optionCKm;
    }

    public String getOptionDKm() {
        return optionDKm;
    }

    public void setOptionDKm(String optionDKm) {
        this.optionDKm = optionDKm;
    }

    public String getHintKm() {
        return hintKm;
    }

    public void setHintKm(String hintKm) {
        this.hintKm = hintKm;
    }

    public String getLocalizedQuestionText() {
        if (com.worldofwonder.util.I18n.isKhmer()) {
            if (questionTextKm != null && !questionTextKm.isEmpty()) return questionTextKm;
            String key = "q_" + id + "_text";
            String loc = com.worldofwonder.util.I18n.get(key);
            if (loc != null && !loc.equals(key)) return loc;
        }
        return questionText;
    }

    public String getLocalizedOptionA() {
        if (com.worldofwonder.util.I18n.isKhmer()) {
            if (optionAKm != null && !optionAKm.isEmpty()) return optionAKm;
            String key = "q_" + id + "_a";
            String loc = com.worldofwonder.util.I18n.get(key);
            if (loc != null && !loc.equals(key)) return loc;
        }
        return optionA;
    }

    public String getLocalizedOptionB() {
        if (com.worldofwonder.util.I18n.isKhmer()) {
            if (optionBKm != null && !optionBKm.isEmpty()) return optionBKm;
            String key = "q_" + id + "_b";
            String loc = com.worldofwonder.util.I18n.get(key);
            if (loc != null && !loc.equals(key)) return loc;
        }
        return optionB;
    }

    public String getLocalizedOptionC() {
        if (com.worldofwonder.util.I18n.isKhmer()) {
            if (optionCKm != null && !optionCKm.isEmpty()) return optionCKm;
            String key = "q_" + id + "_c";
            String loc = com.worldofwonder.util.I18n.get(key);
            if (loc != null && !loc.equals(key)) return loc;
        }
        return optionC;
    }

    public String getLocalizedOptionD() {
        if (com.worldofwonder.util.I18n.isKhmer()) {
            if (optionDKm != null && !optionDKm.isEmpty()) return optionDKm;
            String key = "q_" + id + "_d";
            String loc = com.worldofwonder.util.I18n.get(key);
            if (loc != null && !loc.equals(key)) return loc;
        }
        return optionD;
    }

    public String getLocalizedHint() {
        if (com.worldofwonder.util.I18n.isKhmer()) {
            if (hintKm != null && !hintKm.isEmpty()) return hintKm;
            String key = "q_" + id + "_hint";
            String loc = com.worldofwonder.util.I18n.get(key);
            if (loc != null && !loc.equals(key)) return loc;
        }
        return hint;
    }
}
