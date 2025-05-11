import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private LoginCallback callback;

    public LoginScreen(LoginCallback callback) {
        super("ورود به برنامه");
        this.callback = callback;
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("ورود به سیستم");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Username panel
        JPanel usernamePanel = new JPanel(new BorderLayout(10, 0));
        usernamePanel.setBackground(Color.WHITE);
        JLabel usernameLabel = new JLabel("نام کاربری:");
        usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        usernameField = new JTextField(15);
        styleInput(usernameField);
        usernamePanel.add(usernameLabel, BorderLayout.WEST);
        usernamePanel.add(usernameField, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 30, 5, 30);
        mainPanel.add(usernamePanel, gbc);

        // Password panel
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 0));
        passwordPanel.setBackground(Color.WHITE);
        JLabel passwordLabel = new JLabel("رمز عبور:");
        passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordField = new JPasswordField(15);
        styleInput(passwordField);
        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 30, 20, 30);
        mainPanel.add(passwordPanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton loginButton = new JButton("ورود");
        styleButton(loginButton);
        loginButton.addActionListener(e -> onLogin());

        JButton registerButton = new JButton("ثبت نام");
        styleButton(registerButton);
        registerButton.addActionListener(e -> {
            dispose();
            new RegistrationScreen().setVisible(true);
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Add enter key listener
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onLogin();
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);
    }

    private void styleInput(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 40));
        field.setBorder(new CompoundBorder(
            new RoundedBorder(8),
            new EmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(new Color(250, 250, 255));
        field.setHorizontalAlignment(JTextField.LEFT);
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(100, 35));
        button.setBackground(new Color(66, 133, 244));
        button.setForeground(Color.WHITE);
        button.setBorder(new RoundedBorder(8));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لطفاً نام کاربری و رمز عبور را وارد کنید.", "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (UserManager.getInstance().validateUser(username, password)) {
            dispose();
            callback.onLoginSuccess();
        } else {
            JOptionPane.showMessageDialog(this, "نام کاربری یا رمز عبور اشتباه است.", "خطا", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    public interface LoginCallback {
        void onLoginSuccess();
    }
}
