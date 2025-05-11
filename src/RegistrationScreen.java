import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class RegistrationScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField, confirmPasswordField;

    public RegistrationScreen() {
        super("ثبت نام کاربر جدید");
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("ثبت نام کاربر جدید");
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
        gbc.insets = new Insets(5, 30, 5, 30);
        mainPanel.add(passwordPanel, gbc);

        // Confirm Password panel
        JPanel confirmPanel = new JPanel(new BorderLayout(10, 0));
        confirmPanel.setBackground(Color.WHITE);
        JLabel confirmLabel = new JLabel("تکرار رمز عبور:");
        confirmLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        confirmPasswordField = new JPasswordField(15);
        styleInput(confirmPasswordField);
        confirmPanel.add(confirmLabel, BorderLayout.WEST);
        confirmPanel.add(confirmPasswordField, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 30, 20, 30);
        mainPanel.add(confirmPanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton registerButton = new JButton("ثبت نام");
        styleButton(registerButton);
        registerButton.addActionListener(e -> onRegister());

        JButton cancelButton = new JButton("انصراف");
        styleButton(cancelButton);
        cancelButton.addActionListener(e -> {
            dispose();
            new LoginScreen(new LoginScreen.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    String currentUser = UserManager.getInstance().getCurrentUser();
                    new TodoApp(currentUser).setVisible(true);
                }
            }).setVisible(true);
        });

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Add enter key listener
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onRegister();
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);
        confirmPasswordField.addKeyListener(enterListener);
    }

    private void styleInput(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 35));
        field.setBorder(new CompoundBorder(
            new RoundedBorder(8),
            new EmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(new Color(250, 250, 255));
        field.setHorizontalAlignment(JTextField.RIGHT);
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(100, 35));
        button.setBackground(new Color(66, 133, 244));
        button.setForeground(Color.WHITE);
        button.setBorder(new RoundedBorder(8));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void onRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لطفاً تمام فیلدها را پر کنید.", "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "رمز عبور و تکرار آن مطابقت ندارند.", "خطا", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            confirmPasswordField.setText("");
            return;
        }

        if (UserManager.getInstance().registerUser(username, password)) {
            JOptionPane.showMessageDialog(this, "ثبت نام با موفقیت انجام شد.", "موفقیت", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginScreen(new LoginScreen.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    String currentUser = UserManager.getInstance().getCurrentUser();
                    new TodoApp(currentUser).setVisible(true);
                }
            }).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "این نام کاربری قبلاً ثبت شده است.", "خطا", JOptionPane.ERROR_MESSAGE);
            usernameField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
        }
    }
}