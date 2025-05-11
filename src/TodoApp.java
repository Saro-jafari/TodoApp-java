import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class TodoApp extends JFrame {
    private final String currentUser;
    private static final String TODOS_DIR = "todos";
    private java.util.List<TodoItem> items = new ArrayList<>();
    private JPanel listPanel;
    private JTextField taskField, dateField;
    private JButton actionButton;
    private int editIndex = -1;
    private boolean isEditing = false;

    public TodoApp(String username) {
        super("لیست کارهای " + username);
        this.currentUser = username;
        initLookAndFeel();
        initComponents();
        loadTasks();
        renderList();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("Label.font", new Font("Tahoma", Font.PLAIN, 18));
            UIManager.put("Button.font", new Font("Tahoma", Font.BOLD, 18));
            UIManager.put("TextField.font", new Font("Tahoma", Font.PLAIN, 18));
            UIManager.put("CheckBox.font", new Font("Tahoma", Font.PLAIN, 18));
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(new Color(240, 242, 245));
        applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(100, 120, 235), w, h, new Color(80, 100, 215));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        topBar.setPreferredSize(new Dimension(0, 100));

        JLabel titleLabel = new JLabel("خوش آمدید " + currentUser);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        topBar.add(titleLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("خروج");
        logoutButton.setFont(new Font("Tahoma", Font.BOLD, 18));
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> logout());
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoutPanel.setOpaque(false);
        logoutPanel.add(logoutButton);
        topBar.add(logoutPanel, BorderLayout.WEST);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 15, 0, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        taskField = new JTextField(30);
        dateField = new JTextField(12);
        
        taskField.setEnabled(true);
        dateField.setEnabled(true);
        taskField.setEditable(true);
        dateField.setEditable(true);
        taskField.setFocusable(true);
        dateField.setFocusable(true);

        styleInput(taskField);
        styleInput(dateField);
        dateField.setText(toPersianDate(new Date()));

        KeyAdapter preventDeleteListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isEditing && (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || 
                                e.getKeyCode() == KeyEvent.VK_DELETE)) {
                    e.consume();
                }
            }
        };
        
        taskField.addKeyListener(preventDeleteListener);
        dateField.addKeyListener(preventDeleteListener);

        actionButton = new JButton("افزودن کار جدید");
        actionButton.setPreferredSize(new Dimension(180, 55));
        actionButton.setBackground(new Color(66, 133, 244));
        actionButton.setForeground(Color.WHITE);
        actionButton.setBorder(new RoundedBorder(10));
        actionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel taskLabel = new JLabel("عنوان کار:");
        JLabel dateLabel = new JLabel("تاریخ:");
        taskLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        fieldsPanel.add(actionButton, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.15;
        fieldsPanel.add(dateLabel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.25;
        fieldsPanel.add(dateField, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.15;
        fieldsPanel.add(taskLabel, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0.35;
        fieldsPanel.add(taskField, gbc);

        inputPanel.add(fieldsPanel);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(240, 242, 245));
        listPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(240, 242, 245));

        JPanel contentPanel = new JPanel(new BorderLayout(0, 25));
        contentPanel.setBackground(new Color(240, 242, 245));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        contentPanel.add(inputPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        actionButton.addActionListener(e -> onAction());
        
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onAction();
            }
        };
        
        taskField.addKeyListener(enterListener);
        dateField.addKeyListener(enterListener);

        SwingUtilities.invokeLater(() -> taskField.requestFocusInWindow());
    }

    private void styleInput(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        field.setBackground(new Color(245, 247, 250));
        field.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        field.setHorizontalAlignment(JTextField.RIGHT);
        field.setFont(new Font("Tahoma", Font.PLAIN, 18));
    }

    private void logout() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            new LoginScreen(new LoginScreen.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    SwingUtilities.invokeLater(() -> new TodoApp(currentUser).setVisible(true));
                }
            }).setVisible(true);
        });
    }

    private void onAction() {
        String desc = taskField.getText().trim();
        String dateStr = dateField.getText().trim();
        
        if (desc.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لطفاً کار و تاریخ را وارد کنید.", "هشدار", JOptionPane.WARNING_MESSAGE);
            return;
        }

        isEditing = true;
        if (editIndex >= 0) {
            TodoItem item = items.get(editIndex);
            item.desc = desc;
            item.date = dateStr;
            editIndex = -1;
            actionButton.setText("افزودن کار جدید");
        } else {
            items.add(new TodoItem(desc, dateStr, false));
        }

        taskField.setText("");
        dateField.setText(toPersianDate(new Date()));
        isEditing = false;
        saveTasks();
        renderList();
        
        SwingUtilities.invokeLater(() -> taskField.requestFocusInWindow());
    }

    private JPanel createItemPanel(TodoItem item, int index) {
        JPanel panel = new JPanel(new BorderLayout(25, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JCheckBox check = new JCheckBox();
        check.setSelected(item.completed);
        check.setBackground(Color.WHITE);
        check.addItemListener(e -> {
            item.completed = check.isSelected();
            saveTasks();
            renderList();
        });

        JLabel text = new JLabel(item.completed ? 
            "<html><strike style='color: #999'>" + item.desc + "</strike></html>" : 
            item.desc);
        text.setFont(new Font("Tahoma", Font.PLAIN, 18));

        JLabel date = new JLabel(item.date);
        date.setForeground(new Color(100, 100, 100));
        date.setFont(new Font("Tahoma", Font.PLAIN, 16));

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(check);
        contentPanel.add(text);
        contentPanel.add(date);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton editBtn = createButton("ویرایش", new Color(66, 133, 244), e -> {
            editIndex = index;
            taskField.setText(item.desc);
            dateField.setText(item.date);
            actionButton.setText("ذخیره تغییرات");
            taskField.requestFocusInWindow();
        });

        JButton deleteBtn = createButton("حذف", new Color(234, 67, 53), e -> {
            items.remove(index);
            saveTasks();
            renderList();
        });

        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JButton createButton(String text, Color color, ActionListener listener) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 45));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(new RoundedBorder(8));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        return button;
    }

    private void renderList() {
        listPanel.removeAll();
        for (int i = 0; i < items.size(); i++) {
            listPanel.add(createItemPanel(items.get(i), i));
            listPanel.add(Box.createVerticalStrut(20));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void saveTasks() {
        File dir = new File(TODOS_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TODOS_DIR + "/" + currentUser + ".txt"))) {
            for (TodoItem t : items) {
                writer.write(t.date + "|" + t.desc + "|" + t.completed + "\n");
            }
        } catch (IOException ignored) {}
    }

    private void loadTasks() {
        File f = new File(TODOS_DIR + "/" + currentUser + ".txt");
        if (!f.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|", 3);
                if (p.length == 3) {
                    items.add(new TodoItem(p[1], p[0], Boolean.parseBoolean(p[2])));
                }
            }
        } catch (IOException ignored) {}
    }

    private String toPersianDate(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int gy = c.get(Calendar.YEAR);
        int gm = c.get(Calendar.MONTH) + 1;
        int gd = c.get(Calendar.DAY_OF_MONTH);
        int[] j = gregorianToJalali(gy, gm, gd);
        return String.format("%04d/%02d/%02d", j[0], j[1], j[2]);
    }

    private int[] gregorianToJalali(int gy, int gm, int gd) {
        int[] gdm = {0,31,(isLeap(gy)?29:28),31,30,31,30,31,31,30,31,30,31};
        int gy2 = (gm>2?gy+1:gy);
        long days = 355666 + (365L*gy) + ((gy2+3)/4) - ((gy2+99)/100) + ((gy2+399)/400);
        for(int i=0; i<gm; i++) days += gdm[i];
        days += gd;
        int jy = -1595 + 33*(int)(days/12053);
        days %= 12053;
        jy += 4*(int)(days/1461);
        days %= 1461;
        if(days > 365) {
            jy += (int)((days-1)/365);
            days = (days-1)%365;
        }
        int[] jdm = {0,31,31,31,31,31,31,30,30,30,30,30,29};
        int jm;
        for(jm=1; jm<=12 && days>=jdm[jm]; jm++) days -= jdm[jm];
        int jd = (int)days + 1;
        return new int[]{jy,jm,jd};
    }

    private boolean isLeap(int y) {
        return (y%4==0 && y%100!=0) || (y%400==0);
    }

    class TodoItem {
        String desc, date;
        boolean completed;
        
        TodoItem(String d, String dt, boolean c) {
            desc = d;
            date = dt;
            completed = c;
        }
    }
}
