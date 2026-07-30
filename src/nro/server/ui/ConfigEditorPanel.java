package nro.server.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigEditorPanel extends JPanel {

    private static final String CONFIG_PATH = "data/config/config.properties";

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_HINT = new Font("Segoe UI", Font.PLAIN, 11);

    private static final Color BG = new Color(246, 248, 251);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 226, 235);
    private static final Color TEXT = new Color(34, 40, 49);
    private static final Color MUTED = new Color(100, 112, 128);
    private static final Color PRIMARY = new Color(0, 120, 215);
    private static final Color WARNING = new Color(180, 105, 0);

    private final Map<String, JTextField> fieldMap = new LinkedHashMap<>();
    private final Map<String, String> valueMap = new LinkedHashMap<>();
    private final List<String> rawLines = new ArrayList<>();
    private final Set<String> knownKeys = new LinkedHashSet<>();

    private JPanel contentPanel;
    private JPanel advancedPanel;
    private JCheckBox chkAdvanced;
    private JLabel lblStatus;
    private boolean dirty;

    public ConfigEditorPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 18, 16, 18));
        initUI();
        loadConfig();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout(12, 4));
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Server Config");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(new File(CONFIG_PATH).getAbsolutePath());
        subtitle.setFont(FONT_HINT);
        subtitle.setForeground(MUTED);

        titleBox.add(title);
        titleBox.add(Box.createRigidArea(new Dimension(0, 4)));
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.CENTER);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topActions.setOpaque(false);

        JButton reload = ServerGuiUtils.createStyledButton("Reload", new Color(92, 104, 120), Color.WHITE);
        reload.setPreferredSize(new Dimension(96, 34));
        reload.addActionListener(e -> reloadConfig());

        JButton save = ServerGuiUtils.createStyledButton("Save Config", PRIMARY, Color.WHITE);
        save.setPreferredSize(new Dimension(128, 34));
        save.addActionListener(e -> saveConfig());

        topActions.add(reload);
        topActions.add(save);
        header.add(topActions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 0, 0, 0));

        lblStatus = new JLabel("Ready");
        lblStatus.setFont(FONT_HINT);
        lblStatus.setForeground(MUTED);
        footer.add(lblStatus, BorderLayout.WEST);

        JLabel restartHint = new JLabel("Most server/game/network changes need restart.");
        restartHint.setFont(FONT_HINT);
        restartHint.setForeground(WARNING);
        footer.add(restartHint, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    private void reloadConfig() {
        fieldMap.clear();
        valueMap.clear();
        rawLines.clear();
        knownKeys.clear();
        dirty = false;
        contentPanel.removeAll();
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            contentPanel.add(messageCard("Config file not found", file.getAbsolutePath(), true));
            refresh();
            return;
        }

        try {
            rawLines.addAll(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
            parseConfig();
            buildForm();
            setStatus("Loaded " + valueMap.size() + " keys", false);
        } catch (Exception ex) {
            contentPanel.add(messageCard("Can not read config", ex.getMessage(), true));
        }
        refresh();
    }

    private void parseConfig() {
        for (String line : rawLines) {
            String trim = line.trim();
            if (trim.isEmpty() || trim.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            valueMap.put(key, value);
        }
    }

    private void buildForm() {
        contentPanel.add(summaryCard());
        contentPanel.add(space(10));

        addGroup("Run Controls", "Things you are most likely to tune while operating the game.",
                "Affects gameplay/load. Restart is recommended.",
                keys("server.name", "server.sv", "server.expserver", "server.maxplayer", "server.maxperip", "server.waitlogin", "server.debug"));

        addGroup("Network", "Public address and ports used by the game client.",
                "Changing ports/IP needs restart and client/server-list alignment.",
                networkKeys());

        addGroup("Database", "Connection and pool settings.",
                "Database host/user/password changes need restart.",
                keys("database.host", "database.port", "database.name", "database.user", "database.pass", "database.min", "database.max", "database.lifetime"));

        // auto.maintenance / .hour / .minute are intentionally not editable here.
        // Scheduled Actions' Daily Maintenance card is the single source of truth for
        // recurring maintenance; this boot-time key set duplicated it (two independent
        // schedulers that could both fire) and is left untouched on save.
        knownKeys.addAll(keys("auto.maintenance", "auto.maintenance.hour", "auto.maintenance.minute"));

        chkAdvanced = new JCheckBox("Show advanced / rarely changed keys");
        chkAdvanced.setOpaque(false);
        chkAdvanced.setFont(FONT_LABEL);
        chkAdvanced.setForeground(TEXT);
        chkAdvanced.addActionListener(e -> advancedPanel.setVisible(chkAdvanced.isSelected()));
        contentPanel.add(wrapLeft(chkAdvanced));

        advancedPanel = new JPanel();
        advancedPanel.setOpaque(false);
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
        contentPanel.add(advancedPanel);
        addAdvancedGroups();
        advancedPanel.setVisible(false);

        addUnknownKeys();
    }

    private JPanel summaryCard() {
        JPanel card = cardPanel(new BorderLayout(12, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 216, 234)),
                new EmptyBorder(12, 14, 12, 14)));

        JPanel stats = new JPanel(new GridLayout(1, 4, 10, 0));
        stats.setOpaque(false);
        stats.add(summaryItem("Server", getValue("server.name", "-")));
        stats.add(summaryItem("EXP", "x" + getValue("server.expserver", "1")));
        stats.add(summaryItem("Max Players", getValue("server.maxplayer", "-")));
        stats.add(summaryItem("Port", getValue("server.port_proxy", getValue("server.port_real", "-"))));

        card.add(stats, BorderLayout.CENTER);

        JLabel hint = new JLabel("Keep day-to-day controls visible; database driver, mode flags, and rarely used keys are in Advanced.");
        hint.setFont(FONT_HINT);
        hint.setForeground(MUTED);
        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    private JPanel summaryItem(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(FONT_HINT);
        l.setForeground(MUTED);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(TEXT);
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private void addGroup(String title, String subtitle, String note, List<String> keys) {
        List<String> existing = existingKeys(keys);
        if (existing.isEmpty()) {
            return;
        }

        JPanel card = cardPanel(new BorderLayout(10, 10));
        JPanel head = new JPanel(new BorderLayout(8, 2));
        head.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SECTION);
        titleLabel.setForeground(TEXT);
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_HINT);
        subLabel.setForeground(MUTED);
        head.add(titleLabel, BorderLayout.NORTH);
        head.add(subLabel, BorderLayout.CENTER);
        card.add(head, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        int row = 0;
        for (String key : existing) {
            addFieldRow(rows, key, row++);
            knownKeys.add(key);
        }
        card.add(rows, BorderLayout.CENTER);

        if (note != null && !note.isBlank()) {
            JLabel noteLabel = new JLabel(note);
            noteLabel.setFont(FONT_HINT);
            noteLabel.setForeground(WARNING);
            card.add(noteLabel, BorderLayout.SOUTH);
        }

        contentPanel.add(card);
        contentPanel.add(space(10));
    }

    private void addAdvancedGroups() {
        addEventScheduleGroups(advancedPanel);

        List<String> mode = keys("server.local", "server.test", "server.daoautoupdater", "database.driver");
        addGroupTo(advancedPanel, "Advanced Flags", "Mode flags and driver setup. Change only when you know the boot impact.", mode);

        List<String> rest = new ArrayList<>();
        for (String key : valueMap.keySet()) {
            if (!knownKeys.contains(key) && !isEventKey(key)) {
                rest.add(key);
            }
        }
        rest.removeAll(mode);
        addGroupTo(advancedPanel, "Other Keys", "Loaded from config but not part of the common control surface.", rest);
    }

    private void addEventScheduleGroups(JPanel parent) {
        Map<String, List<String>> grouped = eventKeysByGroup();
        if (grouped.isEmpty()) {
            return;
        }

        JPanel card = cardPanel(new BorderLayout(10, 10));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel titleLabel = new JLabel("Event Schedule");
        titleLabel.setFont(FONT_SECTION);
        titleLabel.setForeground(TEXT);
        JLabel subLabel = new JLabel("Each event keeps its own open, end, and reward time fields together.");
        subLabel.setFont(FONT_HINT);
        subLabel.setForeground(MUTED);
        head.add(titleLabel, BorderLayout.NORTH);
        head.add(subLabel, BorderLayout.CENTER);
        card.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            List<String> existing = existingKeys(entry.getValue());
            if (existing.isEmpty()) {
                continue;
            }
            body.add(eventSubGroup(eventGroupTitle(entry.getKey()), existing));
            body.add(space(8));
        }

        card.add(body, BorderLayout.CENTER);
        parent.add(card);
        parent.add(space(10));
    }

    private JPanel eventSubGroup(String title, List<String> keys) {
        JPanel group = new JPanel(new BorderLayout(8, 8));
        group.setOpaque(false);
        group.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(10, 0, 0, 0)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_LABEL);
        titleLabel.setForeground(TEXT);
        group.add(titleLabel, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        for (int i = 0; i < keys.size(); i++) {
            addCompactEventField(rows, keys.get(i), i);
            knownKeys.add(keys.get(i));
        }
        group.add(rows, BorderLayout.CENTER);
        return group;
    }

    private void addCompactEventField(JPanel rows, String key, int index) {
        int col = index % 4;
        int row = index / 4;

        JPanel cell = new JPanel(new BorderLayout(0, 3));
        cell.setOpaque(false);

        JLabel label = new JLabel(displayName(key));
        label.setFont(FONT_HINT);
        label.setForeground(MUTED);
        label.setToolTipText(key);

        JTextField field = new JTextField(valueMap.get(key), 8);
        field.setFont(FONT_TEXT);
        field.setPreferredSize(new Dimension(92, 28));
        field.setToolTipText(key + " - " + description(key));
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { markDirty(); }
            @Override public void removeUpdate(DocumentEvent e) { markDirty(); }
            @Override public void changedUpdate(DocumentEvent e) { markDirty(); }
        });

        fieldMap.put(key, field);
        cell.add(label, BorderLayout.NORTH);
        cell.add(field, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = col;
        gc.gridy = row;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets = new Insets(4, 0, 6, col == 3 ? 0 : 10);
        rows.add(cell, gc);
    }

    private void addUnknownKeys() {
        List<String> eventOrKnown = new ArrayList<>(knownKeys);
        eventOrKnown.addAll(eventKeys());
        for (String key : eventOrKnown) {
            knownKeys.add(key);
        }
    }

    private void addGroupTo(JPanel parent, String title, String subtitle, List<String> keys) {
        List<String> existing = existingKeys(keys);
        if (existing.isEmpty()) {
            return;
        }
        JPanel card = cardPanel(new BorderLayout(10, 10));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SECTION);
        titleLabel.setForeground(TEXT);
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_HINT);
        subLabel.setForeground(MUTED);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(titleLabel, BorderLayout.NORTH);
        head.add(subLabel, BorderLayout.CENTER);
        card.add(head, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        int row = 0;
        for (String key : existing) {
            addFieldRow(rows, key, row++);
            knownKeys.add(key);
        }
        card.add(rows, BorderLayout.CENTER);
        parent.add(card);
        parent.add(space(10));
    }

    private void addFieldRow(JPanel rows, String key, int row) {
        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0;
        gl.gridy = row;
        gl.anchor = GridBagConstraints.WEST;
        gl.insets = new Insets(5, 0, 5, 14);
        gl.weightx = 0;

        GridBagConstraints gf = new GridBagConstraints();
        gf.gridx = 1;
        gf.gridy = row;
        gf.fill = GridBagConstraints.HORIZONTAL;
        gf.insets = new Insets(5, 0, 5, 10);
        gf.weightx = 1.0;

        GridBagConstraints gh = new GridBagConstraints();
        gh.gridx = 2;
        gh.gridy = row;
        gh.anchor = GridBagConstraints.WEST;
        gh.insets = new Insets(5, 0, 5, 0);
        gh.weightx = 0;

        JLabel label = new JLabel(displayName(key));
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT);
        label.setToolTipText(key);

        JTextField field = isPasswordKey(key) ? new JPasswordField(valueMap.get(key), 24) : new JTextField(valueMap.get(key), 24);
        field.setFont(FONT_TEXT);
        field.setPreferredSize(new Dimension(260, 32));
        field.setToolTipText(key);
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { markDirty(); }
            @Override public void removeUpdate(DocumentEvent e) { markDirty(); }
            @Override public void changedUpdate(DocumentEvent e) { markDirty(); }
        });

        JLabel hint = new JLabel(description(key));
        hint.setFont(FONT_HINT);
        hint.setForeground(isDangerKey(key) ? WARNING : MUTED);

        fieldMap.put(key, field);
        rows.add(label, gl);
        rows.add(field, gf);
        rows.add(hint, gh);
    }

    private JPanel messageCard(String title, String message, boolean error) {
        JPanel p = cardPanel(new BorderLayout(0, 5));
        JLabel t = new JLabel(title);
        t.setFont(FONT_SECTION);
        t.setForeground(error ? new Color(190, 40, 40) : TEXT);
        JLabel m = new JLabel(message);
        m.setFont(FONT_TEXT);
        m.setForeground(MUTED);
        p.add(t, BorderLayout.NORTH);
        p.add(m, BorderLayout.CENTER);
        return p;
    }

    private JPanel cardPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)));
        return p;
    }

    private JPanel wrapLeft(JComponent component) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(component);
        return p;
    }

    private Component space(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    private List<String> keys(String... keys) {
        return new ArrayList<>(Arrays.asList(keys));
    }

    private List<String> networkKeys() {
        List<String> keys = keys("server.ip_host", "server.port_real", "server.port_proxy");
        for (int i = 1; i <= 10; i++) {
            keys.add("server.sv" + i);
        }
        return keys;
    }

    private List<String> eventKeys() {
        List<String> keys = new ArrayList<>();
        for (String key : valueMap.keySet()) {
            if (isEventKey(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private Map<String, List<String>> eventKeysByGroup() {
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (String key : eventKeys()) {
            grouped.computeIfAbsent(eventGroupKey(key), ignored -> new ArrayList<>()).add(key);
        }
        return grouped;
    }

    private boolean isEventKey(String key) {
        return key.startsWith("event.");
    }

    private String eventGroupKey(String key) {
        if (key.equals("event.year")) {
            return "global";
        }
        String[] parts = key.split("\\.");
        return parts.length >= 3 ? parts[1] : "other";
    }

    private String eventGroupTitle(String group) {
        return switch (group) {
            case "global" -> "Global Event Year";
            case "top" -> "Dua Top";
            case "sm" -> "Dua Top Suc Manh";
            case "nap" -> "Dua Top Nap";
            case "chucvip" -> "Chuc VIP";
            case "trangsucvip" -> "Trang Suc VIP";
            case "thangmuoi" -> "Su Kien Thang Muoi";
            default -> titleCase(group.replace('_', ' ').replace('-', ' '));
        };
    }

    private String titleCase(String text) {
        if (text == null || text.isBlank()) {
            return "Other Event";
        }
        StringBuilder out = new StringBuilder();
        for (String part : text.trim().split("\\s+")) {
            if (part.isBlank()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                out.append(part.substring(1));
            }
        }
        return out.toString();
    }

    private List<String> existingKeys(List<String> keys) {
        List<String> existing = new ArrayList<>();
        for (String key : keys) {
            if (valueMap.containsKey(key)) {
                existing.add(key);
            }
        }
        return existing;
    }

    private String getValue(String key, String fallback) {
        return valueMap.getOrDefault(key, fallback);
    }

    private boolean isPasswordKey(String key) {
        String lower = key.toLowerCase();
        return lower.contains("pass") || lower.contains("password") || lower.contains("secret") || lower.contains("token");
    }

    private boolean isDangerKey(String key) {
        return key.equals("server.test")
                || key.equals("server.local")
                || key.equals("server.daoautoupdater")
                || key.equals("database.driver")
                || key.startsWith("server.port");
    }

    private String displayName(String key) {
        if (key.startsWith("event.")) {
            return eventFieldName(key);
        }
        return switch (key) {
            case "server.name" -> "Server name";
            case "server.sv" -> "Server ID";
            case "server.expserver" -> "EXP rate";
            case "server.maxplayer" -> "Max players";
            case "server.maxperip" -> "Max accounts/IP";
            case "server.waitlogin" -> "Login wait";
            case "server.debug" -> "Debug log";
            case "server.ip_host" -> "Bind host";
            case "server.port_real" -> "Real port";
            case "server.port_proxy" -> "Proxy port";
            case "database.host" -> "DB host";
            case "database.port" -> "DB port";
            case "database.name" -> "DB name";
            case "database.user" -> "DB user";
            case "database.pass" -> "DB password";
            case "database.min" -> "DB pool min";
            case "database.max" -> "DB pool max";
            case "database.lifetime" -> "DB lifetime";
            case "auto.maintenance" -> "Auto maintenance";
            case "auto.maintenance.hour" -> "Maintenance hour";
            case "auto.maintenance.minute" -> "Maintenance minute";
            case "server.local" -> "Local mode";
            case "server.test" -> "Test mode";
            case "server.daoautoupdater" -> "DAO auto updater";
            case "database.driver" -> "DB driver";
            default -> key;
        };
    }

    private String eventFieldName(String key) {
        if (key.equals("event.year")) {
            return "Year";
        }
        String[] parts = key.split("\\.");
        String field = parts.length >= 3 ? parts[2] : key;
        return switch (field) {
            case "month_open" -> "Open month";
            case "date_open" -> "Open day";
            case "hour_open" -> "Open hour";
            case "minute_open" -> "Open minute";
            case "month_end" -> "End month";
            case "date_end" -> "End day";
            case "hour_end" -> "End hour";
            case "minute_end" -> "End minute";
            case "month_reward" -> "Reward month";
            case "date_reward" -> "Reward day";
            case "hour_reward" -> "Reward hour";
            case "minute_reward" -> "Reward minute";
            default -> field;
        };
    }

    private String description(String key) {
        if (key.startsWith("server.sv") && !key.equals("server.sv")) {
            return "Name:host:port:flags";
        }
        if (key.startsWith("event.")) {
            return eventDescription(key);
        }
        return switch (key) {
            case "server.name" -> "Shown in server list";
            case "server.sv" -> "Shard index";
            case "server.expserver" -> "Runtime EXP multiplier";
            case "server.maxplayer" -> "Connection capacity";
            case "server.maxperip" -> "Anti multi-account limit";
            case "server.waitlogin" -> "Seconds before login retry";
            case "server.debug" -> "Verbose logs";
            case "server.ip_host" -> "Usually 127.0.0.1 behind proxy";
            case "server.port_real", "server.port_proxy" -> "Restart required";
            case "database.host", "database.port", "database.name", "database.user", "database.pass" -> "Restart required";
            case "database.min", "database.max" -> "Pool size";
            case "database.lifetime" -> "Pool lifetime ms";
            case "auto.maintenance" -> "0 off, 1 on";
            case "auto.maintenance.hour" -> "0-23";
            case "auto.maintenance.minute" -> "0-59";
            case "server.local", "server.test", "server.daoautoupdater" -> "Advanced boot flag";
            case "database.driver" -> "Do not change casually";
            default -> "";
        };
    }

    private String eventDescription(String key) {
        if (key.equals("event.year")) {
            return "Applied to all event dates";
        }
        if (key.endsWith(".month_open")) return "Open month";
        if (key.endsWith(".date_open")) return "Open day";
        if (key.endsWith(".hour_open")) return "Open hour";
        if (key.endsWith(".minute_open")) return "Open minute";
        if (key.endsWith(".month_end")) return "End month";
        if (key.endsWith(".date_end")) return "End day";
        if (key.endsWith(".hour_end")) return "End hour";
        if (key.endsWith(".minute_end")) return "End minute";
        if (key.endsWith(".month_reward")) return "Reward month";
        if (key.endsWith(".date_reward")) return "Reward day";
        if (key.endsWith(".hour_reward")) return "Reward hour";
        if (key.endsWith(".minute_reward")) return "Reward minute";
        return "Event schedule";
    }

    private void markDirty() {
        if (!dirty) {
            dirty = true;
            setStatus("Unsaved changes", true);
        }
    }

    private void setStatus(String text, boolean warn) {
        if (lblStatus != null) {
            lblStatus.setText(text);
            lblStatus.setForeground(warn ? WARNING : MUTED);
        }
    }

    private void saveConfig() {
        if (rawLines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Config has not been loaded.");
            return;
        }

        List<String> output = new ArrayList<>();
        for (String line : rawLines) {
            String trim = line.trim();
            if (trim.isEmpty() || trim.startsWith("#")) {
                output.add(line);
                continue;
            }
            int eq = line.indexOf('=');
            if (eq < 0) {
                output.add(line);
                continue;
            }
            String key = line.substring(0, eq).trim();
            JTextField field = fieldMap.get(key);
            if (field == null) {
                output.add(line);
            } else {
                output.add(line.substring(0, eq) + "= " + field.getText().trim());
            }
        }

        File file = new File(CONFIG_PATH);
        try {
            Files.copy(file.toPath(), Paths.get(CONFIG_PATH + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            Files.write(file.toPath(), output, StandardCharsets.UTF_8);
            dirty = false;
            setStatus("Saved. Backup: config.properties.bak", false);
            JOptionPane.showMessageDialog(this,
                    "Saved successfully.\nBackup: config.properties.bak\n\nRestart server to apply boot-time changes.",
                    "Config saved", JOptionPane.INFORMATION_MESSAGE);
            reloadConfig();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refresh() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
