package com.worldofwonder.view;

import com.worldofwonder.model.*;
import com.worldofwonder.controller.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

public class WelcomeScreen extends JPanel {

    private static final int TAB_LOGIN = 0;
    private static final int TAB_REGISTER = 1;

    private final MainUI app;

    private final JTextField loginUser;
    private final JPasswordField loginPass;
    private final JLabel loginStatus;
    private final JButton loginButton;

    private final JTextField regUser;
    private final JTextField regEmail;
    private final JPasswordField regPass;
    private final JLabel regStatus;
    private final JButton registerButton;

    private final UITheme.SegmentTabs tabs;
    private final UITheme.FadeCards forms;
    private int activeTab = TAB_LOGIN;

    public WelcomeScreen(MainUI app) {
        super(new BorderLayout());
        this.app = app;
        setOpaque(false);

        loginUser = UITheme.pillField("Username", "@");
        loginPass = UITheme.pillPassword("Password", "●●●");
        loginUser.setFont(fieldFont());
        loginPass.setFont(fieldFont());
        loginStatus = statusLabel();
        loginButton = UITheme.glowButton("Start Exploring", UITheme.TEAL, UITheme.VIOLET);
        loginButton.setFont(buttonFont());

        regUser = UITheme.pillField("Username", "@");
        regEmail = UITheme.pillField("Email", "✉");
        regPass = UITheme.pillPassword("Password", "●●●");
        regUser.setFont(fieldFont());
        regEmail.setFont(fieldFont());
        regPass.setFont(fieldFont());
        regStatus = statusLabel();
        registerButton = UITheme.glowButton("Create Account", UITheme.VIOLET, UITheme.PINK);
        registerButton.setFont(buttonFont());

        tabs = (UITheme.SegmentTabs) UITheme.segmentTabs(
                new String[]{"Login", "Register"},
                index -> showTab(index == TAB_LOGIN ? TAB_LOGIN : TAB_REGISTER));
        tabs.setPreferredSize(new Dimension(360, 52));
        tabs.setMinimumSize(new Dimension(320, 52));
        tabs.setMaximumSize(new Dimension(360, 52));

        forms = (UITheme.FadeCards) UITheme.fadeCards(
                new JComponent[]{buildLoginForm(), buildRegisterForm()});

        forms.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel card = UITheme.glowCard(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));
        card.setPreferredSize(new Dimension(580, 620));
        card.setMinimumSize(new Dimension(360, 480));
        card.add(buildContent(), BorderLayout.CENTER);

        JPanel root = UITheme.screenPage(card);
        add(root, BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(Box.createVerticalGlue());

        JLabel badge = UITheme.badge("★ GLOBAL ADVENTURE ★", UITheme.GOLD);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(badge);
        content.add(Box.createVerticalStrut(14));

        JComponent logo = UITheme.logoRow(52, 28);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(logo);
        content.add(Box.createVerticalStrut(8));

        JLabel tagline = new UITheme.GradientTextLabel(
                "Travel the world. Answer the questions. Earn the stars.",
                UITheme.FONT_BODY, new Color(0x9fe7ff), new Color(0xffc93c));
        tagline.setFont(UITheme.displayFont(Font.BOLD, UITheme.FONT_BODY));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(tagline);
        content.add(Box.createVerticalStrut(UITheme.GAP_ELEMENT));

        tabs.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(tabs);
        content.add(Box.createVerticalStrut(UITheme.GAP_ELEMENT));

        forms.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(forms);
        content.add(Box.createVerticalStrut(UITheme.GAP_ELEMENT));

        content.add(UITheme.divider("or", UITheme.FONT_SMALL));
        content.add(Box.createVerticalStrut(UITheme.GAP_ELEMENT));

        JButton guest = UITheme.ghostButton("Play as Guest", UITheme.VIOLET);
        guest.setFont(buttonFont());
        UIUtil.fullWidth(guest, UITheme.BTN_H);
        guest.setAlignmentX(Component.CENTER_ALIGNMENT);
        guest.addActionListener(e -> enterDashboard("Guest", true, 0, null, 0));
        content.add(guest);
        content.add(Box.createVerticalStrut(UITheme.GAP_TIGHT));

        JLabel note = new JLabel("No account needed. Progress and points won't be saved.", SwingConstants.CENTER);
        note.setFont(UITheme.bodyFont(Font.PLAIN, UITheme.FONT_SMALL));
        note.setForeground(UITheme.TEXT_MUTED);
        note.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(note);

        content.add(Box.createVerticalGlue());

        return content;
    }

    private JPanel buildLoginForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(Box.createVerticalGlue());

        form.add(field(loginUser));
        form.add(Box.createVerticalStrut(UITheme.GAP_ELEMENT));
        form.add(field(loginPass));
        form.add(Box.createVerticalStrut(UITheme.GAP_TIGHT));

        JPanel forgotRow = new JPanel(new BorderLayout(0, 0));
        forgotRow.setOpaque(false);
        forgotRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        forgotRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

        JButton forgot = UITheme.linkButton("Forgot password?");
        forgot.addActionListener(e -> showPasswordHelp());
        forgotRow.add(forgot, BorderLayout.EAST);

        form.add(forgotRow);
        form.add(Box.createVerticalStrut(6));

        loginStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(loginStatus);
        form.add(Box.createVerticalStrut(14));

        UIUtil.fullWidth(loginButton, UITheme.BTN_H);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> doLogin());
        form.add(loginButton);

        form.add(Box.createVerticalGlue());

        loginUser.addActionListener(e -> doLogin());
        loginPass.addActionListener(e -> doLogin());
        return form;
    }

    private JPanel buildRegisterForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(Box.createVerticalGlue());

        form.add(field(regUser));
        form.add(Box.createVerticalStrut(UITheme.GAP_ELEMENT));
        form.add(field(regEmail));
        form.add(Box.createVerticalStrut(UITheme.GAP_ELEMENT));
        form.add(field(regPass));
        form.add(Box.createVerticalStrut(UITheme.GAP_TIGHT));

        JLabel hint = new JLabel("Create a profile to save your progress.", SwingConstants.CENTER);
        hint.setFont(UITheme.bodyFont(Font.PLAIN, UITheme.FONT_SMALL));
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(hint);
        form.add(Box.createVerticalStrut(8));

        regStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(regStatus);
        form.add(Box.createVerticalStrut(12));

        UIUtil.fullWidth(registerButton, UITheme.BTN_H);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> doRegister());
        form.add(registerButton);

        form.add(Box.createVerticalGlue());

        regUser.addActionListener(e -> doRegister());
        regEmail.addActionListener(e -> doRegister());
        regPass.addActionListener(e -> doRegister());
        return form;
    }

    private void showTab(int tab) {
        if (activeTab == tab) {
            return;
        }
        activeTab = tab;
        tabs.select(tab);
        forms.show(tab);
        if (tab == TAB_LOGIN) {
            loginUser.requestFocusInWindow();
        } else {
            regUser.requestFocusInWindow();
        }
    }

    private void showPasswordHelp() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Password Reset", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel bg = UITheme.gradientPanel(new GridBagLayout());
        JPanel card = UITheme.glowCard(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 34, 26, 34));
        UIUtil.fixedSize(card, 480, 330);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("\uD83D\uDD10", SwingConstants.CENTER);
        icon.setFont(UITheme.emojiFont(42));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(icon);
        inner.add(Box.createVerticalStrut(12));

        JLabel title = UITheme.title("Forgot your password?", UITheme.FONT_SECTION);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(title);
        inner.add(Box.createVerticalStrut(UITheme.GAP_ELEMENT));

        JLabel body = new JLabel(
                "<html><div style='text-align:center'>Password resets are handled by your teacher or "
                        + "administrator.<br>Ask them to reset your account and you can sign in "
                        + "with a fresh password right away.</div></html>",
                SwingConstants.CENTER);
        body.setFont(UITheme.bodyFont(Font.PLAIN, UITheme.FONT_BODY));
        body.setForeground(UITheme.TEXT_MUTED);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(body);
        inner.add(Box.createVerticalStrut(UITheme.GAP_SECTION));

        JButton ok = UITheme.glowButton("Got it", UITheme.TEAL, UITheme.VIOLET);
        ok.setFont(buttonFont());
        UIUtil.fullWidth(ok, UITheme.BTN_H);
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        ok.addActionListener(e -> dialog.dispose());
        inner.add(ok);

        card.add(inner, BorderLayout.CENTER);
        bg.add(card);
        dialog.setContentPane(bg);
        dialog.setSize(480, 340);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private JComponent field(JTextField field) {
        field.setPreferredSize(new Dimension(470, 66));
        field.setMinimumSize(new Dimension(200, 66));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        return field;
    }

    private JLabel statusLabel() {
        JLabel label = new JLabel(" ", SwingConstants.CENTER);
        label.setFont(UITheme.bodyFont(Font.PLAIN, UITheme.FONT_BODY));
        label.setForeground(UITheme.ERROR);
        return label;
    }

    private static Font fieldFont() {
        return UITheme.bodyFont(Font.PLAIN, 18);
    }

    private static Font buttonFont() {
        return UITheme.displayFont(Font.BOLD, UITheme.FONT_BUTTON);
    }

    private void doLogin() {
        String username = loginUser.getText().trim();
        String password = new String(loginPass.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            loginStatus.setText("Enter your username and password.");
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AuthController.AuthResult result = app.getAuthController().login(username, password);
        if (result.isSuccess()) {
            User user = result.getUser();
            JOptionPane.showMessageDialog(WelcomeScreen.this,
                    "Login successful! Welcome back, " + user.getUsername() + "!",
                    "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            enterDashboard(user.getUsername(), false, user.getId(), "session-token", user.getTotalPoints());
        } else {
            loginStatus.setText(result.getMessage());
            loginButton.setEnabled(true);
            JOptionPane.showMessageDialog(WelcomeScreen.this, result.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRegister() {
        String username = regUser.getText().trim();
        String email = regEmail.getText().trim();
        String password = new String(regPass.getPassword());
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            regStatus.setText("All fields are required.");
            JOptionPane.showMessageDialog(this, "All fields (username, email, and password) are required.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AuthController.AuthResult result = app.getAuthController().register(username, email, password);
        if (result.isSuccess()) {
            User user = result.getUser();
            JOptionPane.showMessageDialog(WelcomeScreen.this,
                    "Account created successfully! Welcome, " + user.getUsername() + "!",
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            enterDashboard(user.getUsername(), false, user.getId(), "session-token", user.getTotalPoints());
        } else {
            regStatus.setText(result.getMessage());
            registerButton.setEnabled(true);
            JOptionPane.showMessageDialog(WelcomeScreen.this, result.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setBusy(JButton button, JLabel status, String message) {
        button.setEnabled(false);
        status.setForeground(UITheme.TEXT_MUTED);
        status.setText(message);
    }

    private void enterDashboard(String username, boolean isGuest, int userId, String token, int totalPoints) {
        app.enterDashboard(username, isGuest, userId, token, totalPoints);
    }
}
