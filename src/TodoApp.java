import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class TodoApp extends JFrame {
    private static final String FILE_NAME = "todo.txt";
    private java.util.List<TodoItem> items = new ArrayList<>();
    private JPanel listPanel;
    private JTextField taskField, dateField;
    private JButton actionButton;
    private int editIndex = -1;

    public TodoApp() {
        super("لیست کارها");
        initLookAndFeel();
        initComponents();
        loadTasks();
        renderList();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            Font base = new Font("Tahoma", Font.PLAIN, 16);
            UIManager.put("Label.font", base);
            UIManager.put("Button.font", new Font("Tahoma", Font.BOLD, 16));
            UIManager.put("TextField.font", base);
            UIManager.put("CheckBox.font", base);
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // Add logout button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        JButton logoutButton = new JButton("خروج");
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> logout());
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoutPanel.setBackground(Color.WHITE);
        logoutPanel.add(logoutButton);
        topBar.add(logoutPanel, BorderLayout.WEST);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTask = new JLabel("کار:");
        taskField = new JTextField();
        taskField.setColumns(20);
        styleInput(taskField);

        JLabel lblDate = new JLabel("تاریخ شمسی:");
        dateField = new JTextField(10);
        dateField.setText(toPersianDate(new Date()));
        styleInput(dateField);

        actionButton = new JButton("اضافه");
        actionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        KeyAdapter enterListener = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onAction();
            }
        };
        taskField.addKeyListener(enterListener);
        dateField.addKeyListener(enterListener);
        actionButton.addActionListener(e -> onAction());

        topPanel.add(lblTask);
        topPanel.add(taskField);
        topPanel.add(lblDate);
        topPanel.add(dateField);
        topPanel.add(actionButton);

        topBar.add(topPanel, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(new EmptyBorder(0, 15, 15, 25));
        add(scroll, BorderLayout.CENTER);
    }

    private void logout() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            new LoginScreen(new LoginScreen.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    SwingUtilities.invokeLater(() -> new TodoApp().setVisible(true));
                }
            }).setVisible(true);
        });
    }

    private void styleInput(JTextField field) {
        field.setBorder(new CompoundBorder(
                new RoundedBorder(8),
                new EmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(new Color(250, 250, 255));
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
            actionButton.setText("اضافه");
        } else {
            items.add(new TodoItem(desc, dateStr, false));
        }
        taskField.setText("");
        dateField.setText(toPersianDate(new Date()));
        saveTasks();
        renderList();
    }

    private void renderList() {
        listPanel.removeAll();
        for (int i = 0; i < items.size(); i++) {
            listPanel.add(createItemPanel(items.get(i), i));
            listPanel.add(Box.createVerticalStrut(8));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createItemPanel(TodoItem item, int index) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 255));
        panel.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 255), 2, true));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel content = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 18));
        content.setOpaque(false);
        content.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JCheckBox check = new JCheckBox();
        check.setSelected(item.completed);
        check.setPreferredSize(new Dimension(24, 24));
        check.addItemListener(e -> {
            item.completed = check.isSelected();
            saveTasks(); renderList();
        });

        JLabel text = new JLabel(item.desc);
        text.setFont(new Font("vazir", Font.PLAIN, 18));
        if (item.completed) text.setText("<html><strike>" + item.desc + "</strike></html>");
        text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        text.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                check.setSelected(!check.isSelected());
            }
        });

        JLabel dateLabel = new JLabel(item.date);
        dateLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        btnPanel.setOpaque(false);
        JButton editBtn = new JButton("ویرایش");
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editBtn.setPreferredSize(new Dimension(90, 40));
        editBtn.addActionListener(e -> {
            editIndex = index;
            taskField.setText(item.desc);
            dateField.setText(item.date);
            actionButton.setText("ذخیره");
        });
        JButton deleteBtn = new JButton("حذف");
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.setPreferredSize(new Dimension(90, 40));
        deleteBtn.addActionListener(e1 -> { items.remove(index); saveTasks(); renderList(); });

        content.add(check);
        content.add(text);
        content.add(dateLabel);
        panel.add(content, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.WEST);

        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        return panel;
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (TodoItem t : items) writer.write(t.date + "|" + t.desc + "|" + t.completed + "\n");
        } catch (IOException ignored) {}
    }

    private void loadTasks() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|", 3);
                if (p.length == 3) items.add(new TodoItem(p[1], p[0], Boolean.parseBoolean(p[2])));
            }
        } catch (IOException ignored) {}
    }

    private String toPersianDate(Date d) {
        Calendar c = Calendar.getInstance(); c.setTime(d);
        int gy = c.get(Calendar.YEAR), gm = c.get(Calendar.MONTH)+1, gd = c.get(Calendar.DAY_OF_MONTH);
        int[] j = gregorianToJalali(gy, gm, gd);
        return String.format("%04d/%02d/%02d", j[0], j[1], j[2]);
    }

    private int[] gregorianToJalali(int gy,int gm,int gd){
        int[] gdm={0,31,(isLeap(gy)?29:28),31,30,31,30,31,31,30,31,30,31};
        int gy2=(gm>2?gy+1:gy);
        long days=355666+(365L*gy)+((gy2+3)/4)-((gy2+99)/100)+((gy2+399)/400);
        for(int i=0;i<gm;i++) days+=gdm[i]; days+=gd;
        int jy=-1595+33*(int)(days/12053); days%=12053;
        jy+=4*(int)(days/1461); days%=1461;
        if(days>365){jy+=(int)((days-1)/365); days=(days-1)%365;}
        int[] jdm={0,31,31,31,31,31,31,30,30,30,30,30,29}; long d=days;int jm;
        for(jm=1;jm<=12&&d>=jdm[jm];jm++) d-=jdm[jm]; int jd=(int)d+1;
        return new int[]{jy,jm,jd};
    }

    private boolean isLeap(int y){return(y%4==0&&y%100!=0)||(y%400==0);}

    public static void main(String[] args){ 
        SwingUtilities.invokeLater(() -> {
            new LoginScreen(new LoginScreen.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    SwingUtilities.invokeLater(() -> new TodoApp().setVisible(true));
                }
            }).setVisible(true);
        });
    }

    class TodoItem { String desc, date; boolean completed; TodoItem(String d,String dt,boolean c){desc=d;date=dt;completed=c;} }
}