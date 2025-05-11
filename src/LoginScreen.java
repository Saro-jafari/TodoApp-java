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
        setSize(400, 300);
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

        JLabel usernameLabel = new JLabel("نام کاربری:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        styleInput(usernameField);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("رمز عبور:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        styleInput(passwordField);
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("ورود");
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);

        loginButton.addActionListener(e -> onLogin());
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onLogin();
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void styleInput(JTextField field) {
        field.setBorder(new CompoundBorder(
                new RoundedBorder(8),
                new EmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(new Color(250, 250, 255));
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لطفاً نام کاربری و رمز عبور را وارد کنید.", "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simple authentication (in a real app, this should be more secure)
        if (username.equals("admin") && password.equals("admin")) {
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