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
            
            // Load Vazir font
            InputStream is = TodoApp.class.getResourceAsStream("/fonts/Vazir.ttf");
            if (is != null) {
                Font vazirFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(vazirFont);
                
                Font baseFont = vazirFont.deriveFont(Font.PLAIN, 16);
                Font boldFont = vazirFont.deriveFont(Font.BOLD, 16);
                
                UIManager.put("Label.font", baseFont);
                UIManager.put("Button.font", boldFont);
                UIManager.put("TextField.font", baseFont);
                UIManager.put("CheckBox.font", baseFont);
            }
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 242, 245));
        applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // Top bar with gradient
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
        topBar.setPreferredSize(new Dimension(0, 70));

        // Logout button with custom styling
        JButton logoutButton = new JButton("خروج");
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> logout());
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoutPanel.setOpaque(false);
        logoutPanel.add(logoutButton);
        topBar.add(logoutPanel, BorderLayout.WEST);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        fieldsPanel.setBackground(Color.WHITE);

        taskField = new JTextField(20);
        dateField = new JTextField(10);
        styleInput(taskField);
        styleInput(dateField);
        dateField.setText(toPersianDate(new Date()));

        actionButton = new JButton("افزودن کار جدید");
        actionButton.setBackground(new Color(66, 133, 244));
        actionButton.setForeground(Color.WHITE);
        actionButton.setBorder(new RoundedBorder(8));
        actionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        fieldsPanel.add(new JLabel("کار:"));
        fieldsPanel.add(taskField);
        fieldsPanel.add(new JLabel("تاریخ:"));
        fieldsPanel.add(dateField);
        fieldsPanel.add(actionButton);

        inputPanel.add(fieldsPanel);

        // List panel
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(240, 242, 245));
        listPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(240, 242, 245));

        // Add components
        add(topBar, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Add action listeners
        actionButton.addActionListener(e -> onAction());
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onAction();
            }
        };
        taskField.addKeyListener(enterListener);
        dateField.addKeyListener(enterListener);
    }

    private void styleInput(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(new Color(245, 247, 250));
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
        saveTasks();
        renderList();
    }

    private JPanel createItemPanel(TodoItem item, int index) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(12),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

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
        text.setFont(new Font("Vazir", Font.PLAIN, 16));

        JLabel date = new JLabel(item.date);
        date.setForeground(new Color(100, 100, 100));

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(check);
        contentPanel.add(text);
        contentPanel.add(date);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton editBtn = createButton("ویرایش", new Color(66, 133, 244), e -> {
            editIndex = index;
            taskField.setText(item.desc);
            dateField.setText(item.date);
            actionButton.setText("ذخیره تغییرات");
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
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(new RoundedBorder(6));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        return button;
    }

    private void renderList() {
        listPanel.removeAll();
        for (int i = 0; i < items.size(); i++) {
            listPanel.add(createItemPanel(items.get(i), i));
            listPanel.add(Box.createVerticalStrut(10));
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