import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.*;

public class SchoolEntrySystem extends JFrame {

    // ===== THEME =====
    private static final Color NAVY = new Color(18, 59, 109);
    private static final Color NAVY_DARK = new Color(12, 40, 75);
    private static final Color ACCENT = new Color(41, 128, 185);
    private static final Color BG = new Color(240, 244, 249);
    private static final Color TEXT = new Color(40, 50, 65);
    private static final Color MUTED = new Color(110, 120, 135);
    private static final Color BORDER = new Color(225, 231, 240);
    private static final Color SUCCESS = new Color(39, 155, 90);
    private static final Color ERROR = new Color(200, 60, 60);
    private static final Color GRAY_BTN = new Color(120, 130, 145);

    private static final String SANS = Font.SANS_SERIF;
    private static final Font BASE = new Font(SANS, Font.PLAIN, 14);
    private static final Font BOLD = new Font(SANS, Font.BOLD, 14);
    private static final Font H1 = new Font(SANS, Font.BOLD, 24);
    private static final Font H2 = new Font(SANS, Font.BOLD, 17);

    private static final DateTimeFormatter CLOCK_FMT =
            DateTimeFormatter.ofPattern("EEE, MMM dd yyyy  |  hh:mm:ss a");

    static {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    // ===== DATA =====
    private final ArrayList<Student> students = new ArrayList<>();
    private final ArrayList<EntryLog> logs = new ArrayList<>();

    // ===== UI COMPONENTS =====
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private JLabel studentCountLabel;
    private JLabel entryCountLabel;
    private JLabel todayCountLabel;
    private JLabel clockLabel;
    private JLabel entryStatus;
    private JLabel logCountLabel;

    private JTextField idField;
    private JTextField nameField;
    private JTextField courseField;
    private JComboBox<String> yearBox;
    private JTextField entryIdField;
    private JTextField searchField;

    private DefaultTableModel studentModel;
    private DefaultTableModel logModel;
    private DefaultTableModel recentModel;

    public SchoolEntrySystem() {
        setTitle("School Entry Monitoring System");
        setSize(1150, 700);
        setMinimumSize(new Dimension(950, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        studentModel = createModel("Student ID", "Full Name",
                "Course / Section", "Year Level");
        logModel = createModel("Student ID", "Full Name",
                "Course / Section", "Entry Date", "Entry Time");
        recentModel = createModel("Student ID", "Full Name",
                "Course / Section", "Time");

        contentPanel.add(createDashboard(), "Dashboard");
        contentPanel.add(createRegistration(), "Registration");
        contentPanel.add(createEntryPanel(), "School Entry");
        contentPanel.add(createLogsPanel(), "Entry Logs");

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showPage("Dashboard");
        updateDashboard();
    }

    // =====================================================
    // LAYOUT PIECES
    // =====================================================

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(16, 25, 16, 25));

        JLabel title = label("SCHOOL ENTRY MONITORING SYSTEM",
                new Font(SANS, Font.BOLD, 21), Color.WHITE);

        clockLabel = label(" ", BASE, new Color(190, 210, 235));
        Timer timer = new Timer(1000, e ->
                clockLabel.setText(LocalDateTime.now().format(CLOCK_FMT)));
        timer.setInitialDelay(0);
        timer.start();

        header.add(title, BorderLayout.WEST);
        header.add(clockLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(NAVY_DARK);
        side.setPreferredSize(new Dimension(210, 0));
        side.setBorder(new EmptyBorder(20, 0, 15, 0));

        for (String page : new String[]{
                "Dashboard", "Registration", "School Entry", "Entry Logs"}) {
            JButton b = new JButton(page);
            b.setFont(BOLD);
            b.setForeground(Color.WHITE);
            b.setBackground(NAVY_DARK);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(new EmptyBorder(14, 26, 14, 10));
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setOpaque(true);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> showPage(page));
            navButtons.put(page, b);
            side.add(b);
        }

        side.add(Box.createVerticalGlue());
        JLabel footer = label("  School Entry System v1.0",
                new Font(SANS, Font.PLAIN, 12), new Color(130, 155, 190));
        footer.setBorder(new EmptyBorder(0, 14, 0, 0));
        side.add(footer);
        return side;
    }

    private void showPage(String name) {
        cardLayout.show(contentPanel, name);
        for (Map.Entry<String, JButton> en : navButtons.entrySet()) {
            en.getValue().setBackground(
                    en.getKey().equals(name) ? ACCENT : NAVY_DARK);
        }
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    private JPanel createDashboard() {
        JPanel page = createPage();

        studentCountLabel = new JLabel("0");
        entryCountLabel = new JLabel("0");
        todayCountLabel = new JLabel("0");

        JPanel stats = new JPanel(new GridLayout(1, 3, 20, 0));
        stats.setOpaque(false);
        stats.add(createStatCard("Registered Students", studentCountLabel, ACCENT));
        stats.add(createStatCard("Total Entries", entryCountLabel, SUCCESS));
        stats.add(createStatCard("Entries Today", todayCountLabel,
                new Color(230, 126, 34)));

        JPanel top = new JPanel(new BorderLayout(0, 20));
        top.setOpaque(false);
        top.add(createPageTitle("Dashboard",
                        "Overview of registered students and campus entries"),
                BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);

        JPanel recent = createCard();
        recent.setLayout(new BorderLayout(0, 12));
        recent.add(label("Recent Entries", H2, NAVY), BorderLayout.NORTH);
        recent.add(createScroll(createTable(recentModel)), BorderLayout.CENTER);

        page.add(top, BorderLayout.NORTH);
        page.add(recent, BorderLayout.CENTER);
        return page;
    }

    private JPanel createStatCard(String title, JLabel value, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new MatteBorder(0, 6, 0, 0, color),
                new CompoundBorder(new LineBorder(BORDER),
                        new EmptyBorder(18, 20, 18, 20))));
        value.setFont(new Font(SANS, Font.BOLD, 38));
        value.setForeground(color);
        card.add(label(title, BOLD, MUTED), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    // =====================================================
    // REGISTRATION
    // =====================================================

    private JPanel createRegistration() {
        JPanel page = createPage();
        page.add(createPageTitle("Student Registration",
                "Add new students to the system"), BorderLayout.NORTH);

        // ----- form card -----
        JPanel form = createCard();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(340, 0));

        idField = new JTextField();
        nameField = new JTextField();
        courseField = new JTextField();
        yearBox = new JComboBox<>(new String[]{
                "1st Year", "2nd Year", "3rd Year", "4th Year"});
        yearBox.setFont(BASE);
        yearBox.setBackground(Color.WHITE);

        JLabel heading = label("New Student", H2, NAVY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(heading);
        form.add(Box.createVerticalStrut(16));

        addField(form, "Student ID", idField);
        addField(form, "Full Name", nameField);
        addField(form, "Course / Section", courseField);
        addField(form, "Year Level", yearBox);

        JButton registerBtn = new ModernButton("Register Student", ACCENT);
        JButton clearBtn = new ModernButton("Clear", GRAY_BTN);
        registerBtn.addActionListener(e -> registerStudent());
        clearBtn.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(registerBtn);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(clearBtn);
        form.add(buttons);
        form.add(Box.createVerticalGlue());

        // ----- table card -----
        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.add(label("Registered Students", H2, NAVY), BorderLayout.NORTH);
        tableCard.add(createScroll(createTable(studentModel)), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setOpaque(false);
        body.add(form, BorderLayout.WEST);
        body.add(tableCard, BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private void addField(JPanel form, String labelText, JComponent field) {
        JLabel l = label(labelText, new Font(SANS, Font.BOLD, 12), MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JTextField) {
            styleField((JTextField) field, 14);
        }
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(260, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(l);
        form.add(Box.createVerticalStrut(5));
        form.add(field);
        form.add(Box.createVerticalStrut(16));
    }

    private void registerStudent() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String course = courseField.getText().trim();
        String year = String.valueOf(yearBox.getSelectedItem());

        if (id.isEmpty() || name.isEmpty() || course.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please complete all fields.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (Student existing : students) {
            if (existing.getStudentId().equalsIgnoreCase(id)) {
                JOptionPane.showMessageDialog(this,
                        "Student ID already exists.",
                        "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Student student = new Student(id, name, course, year);
        students.add(student);

        studentModel.addRow(new Object[]{
                student.getStudentId(),
                student.getFullName(),
                student.getCourseSection(),
                student.getYearLevel()
        });

        updateDashboard();
        clearForm();

        JOptionPane.showMessageDialog(this,
                "Student registered successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        courseField.setText("");
        yearBox.setSelectedIndex(0);
        idField.requestFocusInWindow();
    }

    // =====================================================
    // SCHOOL ENTRY
    // =====================================================

    private JPanel createEntryPanel() {
        JPanel page = createPage();
        page.add(createPageTitle("School Entry",
                "Record a student's entry to the campus"), BorderLayout.NORTH);

        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(520, 330));

        JLabel heading = label("Enter Student ID", H2, NAVY);
        JLabel hint = label("Type or scan the ID, then press Enter.", BASE, MUTED);

        entryIdField = new JTextField();
        styleField(entryIdField, 20);
        entryIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JButton entryBtn = new ModernButton("Record Entry", ACCENT);
        entryBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        entryStatus = new JLabel(" ");
        entryStatus.setFont(BASE);
        entryStatus.setVerticalAlignment(SwingConstants.TOP);

        for (JComponent c : new JComponent[]{
                heading, hint, entryIdField, entryBtn, entryStatus}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        card.add(heading);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);
        card.add(Box.createVerticalStrut(20));
        card.add(entryIdField);
        card.add(Box.createVerticalStrut(14));
        card.add(entryBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(entryStatus);

        entryBtn.addActionListener(e -> recordEntry());
        entryIdField.addActionListener(e -> recordEntry());

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(card);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void recordEntry() {
        String id = entryIdField.getText().trim();

        if (id.isEmpty()) {
            showStatus("Please enter a student ID.", ERROR);
            return;
        }

        Student found = null;
        for (Student s : students) {
            if (s.getStudentId().equalsIgnoreCase(id)) {
                found = s;
                break;
            }
        }

        if (found == null) {
            showStatus("<b>Student not found.</b><br>Register the student first.",
                    ERROR);
            entryIdField.selectAll();
            return;
        }

        EntryLog log = new EntryLog(found);
        logs.add(log);

        recentModel.insertRow(0, new Object[]{
                found.getStudentId(),
                found.getFullName(),
                found.getCourseSection(),
                log.getEntryTime()
        });
        if (recentModel.getRowCount() > 8) {
            recentModel.removeRow(8);
        }

        filterLogs();
        updateDashboard();

        showStatus("<b>Entry recorded successfully</b><br>"
                + esc(found.getFullName()) + "  &middot;  "
                + esc(found.getCourseSection()) + "<br>"
                + log.getEntryDate() + "  " + log.getEntryTime(), SUCCESS);

        entryIdField.setText("");
        entryIdField.requestFocusInWindow();
    }

    private void showStatus(String html, Color color) {
        entryStatus.setForeground(color);
        entryStatus.setText("<html>" + html + "</html>");
    }

    // =====================================================
    // ENTRY LOGS
    // =====================================================

    private JPanel createLogsPanel() {
        JPanel page = createPage();
        page.add(createPageTitle("Entry Logs",
                "Search and review all recorded entries"), BorderLayout.NORTH);

        searchField = new JTextField();
        styleField(searchField, 14);
        JButton clearBtn = new ModernButton("Show All", GRAY_BTN);

        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.add(label("Search by ID or Name:", BOLD, TEXT), BorderLayout.WEST);
        bar.add(searchField, BorderLayout.CENTER);
        bar.add(clearBtn, BorderLayout.EAST);

        logCountLabel = label(" ", BASE, MUTED);

        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.add(bar, BorderLayout.NORTH);
        card.add(createScroll(createTable(logModel)), BorderLayout.CENTER);
        card.add(logCountLabel, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterLogs(); }
            public void removeUpdate(DocumentEvent e) { filterLogs(); }
            public void changedUpdate(DocumentEvent e) { filterLogs(); }
        });
        clearBtn.addActionListener(e -> searchField.setText(""));

        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private void filterLogs() {
        String keyword = searchField.getText().trim().toLowerCase();
        logModel.setRowCount(0);

        int shown = 0;
        for (EntryLog log : logs) {
            Student s = log.getStudent();
            if (keyword.isEmpty()
                    || s.getStudentId().toLowerCase().contains(keyword)
                    || s.getFullName().toLowerCase().contains(keyword)) {
                logModel.addRow(new Object[]{
                        s.getStudentId(),
                        s.getFullName(),
                        s.getCourseSection(),
                        log.getEntryDate(),
                        log.getEntryTime()
                });
                shown++;
            }
        }
        logCountLabel.setText("Showing " + shown + " of "
                + logs.size() + " entries");
    }

    private void updateDashboard() {
        studentCountLabel.setText(String.valueOf(students.size()));
        entryCountLabel.setText(String.valueOf(logs.size()));

        String today = LocalDate.now().toString();
        long todayCount = logs.stream()
                .filter(l -> l.getEntryDate().equals(today))
                .count();
        todayCountLabel.setText(String.valueOf(todayCount));
    }

    // =====================================================
    // STYLING HELPERS
    // =====================================================

    private JPanel createPage() {
        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(25, 30, 25, 30));
        return page;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(20, 24, 20, 24)));
        return card;
    }

    private JPanel createPageTitle(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(label(title, H1, NAVY));
        p.add(Box.createVerticalStrut(4));
        p.add(label(subtitle, BASE, MUTED));
        return p;
    }

    private JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private void styleField(JTextField f, int size) {
        f.setFont(new Font(SANS, Font.PLAIN, size));
        f.setForeground(TEXT);
        f.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 208, 220), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
    }

    private DefaultTableModel createModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(BASE);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(214, 230, 247));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);

        // zebra-striped rows with padding
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, selected, false, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setForeground(TEXT);
                if (!selected) {
                    setBackground(row % 2 == 0
                            ? Color.WHITE : new Color(246, 249, 253));
                }
                return this;
            }
        });

        // navy header
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, false, false, row, col);
                setBackground(NAVY);
                setForeground(Color.WHITE);
                setFont(BOLD);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
        return table;
    }

    private JScrollPane createScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    /** Escapes user text so it is safe inside an HTML label. */
    private String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // =====================================================
    // ROUNDED BUTTON
    // =====================================================

    private static class ModernButton extends JButton {
        private final Color base;

        ModernButton(String text, Color base) {
            super(text);
            this.base = base;
            setFont(BOLD);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(10, 22, 10, 22));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = getModel().isPressed() ? base.darker()
                    : getModel().isRollover() ? base.brighter() : base;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }

    }
}
