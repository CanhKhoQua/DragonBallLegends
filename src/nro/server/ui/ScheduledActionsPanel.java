package nro.server.ui;

import nro.server.ServerNotify;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduledActionsPanel extends JPanel {

    private static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Color COL_PRIMARY = new Color(0, 120, 215);
    private static final Color COL_RED     = new Color(200, 50, 50);
    private static final Color COL_GREEN   = new Color(40, 167, 69);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM");
    private static final String DAILY_MAINTENANCE_CONFIG = "scheduled_maintenance.properties";
    private static final int[] MAINTENANCE_WARNINGS = {10, 5, 1};

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final AtomicInteger idSeq = new AtomicInteger(1);
    private final List<TaskRow> tasks  = new ArrayList<>();
    private final List<ScheduledFuture<?>> dailyMaintenanceWarnings = new ArrayList<>();

    private DefaultTableModel model;
    private JTable table;
    private JComboBox<String> cmbAction;
    private JSpinner spnDelay;
    private JSpinner spnPeriod;
    private JTextArea txtContent;
    private JPanel repeatRow;
    private JCheckBox chkDailyMaintenance;
    private JCheckBox chkDailyAutoRestart;
    private JSpinner spnDailyHour;
    private JSpinner spnDailyMinute;
    private JLabel lblDailyStatus;
    private ScheduledFuture<?> dailyMaintenanceFuture;

    public ScheduledActionsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        loadDailyMaintenanceConfig();
        // Refresh countdown every second
        scheduler.scheduleAtFixedRate(
            () -> SwingUtilities.invokeLater(this::refreshCountdown), 1, 1, TimeUnit.SECONDS);
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("LỊCH HẸN TÁC VỤ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40, 40, 40));
        lblTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(lblTitle, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildFormPanel(), buildTablePanel());
        split.setResizeWeight(0.38);
        split.setDividerSize(6);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 10, 0));
        wrapper.setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(ServerGuiUtils.createSectionBorder("Thêm tác vụ mới"));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        // Hàng 0: loại tác vụ
        cmbAction = new JComboBox<>(new String[]{
            "Thông báo Chat VIP (1 lần)",
            "Thông báo định kỳ (lặp lại)"
        });
        cmbAction.setFont(FONT_PLAIN);
        addRow(fields, 0, bold("Loại tác vụ:"), cmbAction);

        // Hàng 1: delay
        spnDelay = new JSpinner(new SpinnerNumberModel(5, 1, 1440, 1));
        spnDelay.setFont(FONT_PLAIN);
        ((JSpinner.DefaultEditor) spnDelay.getEditor()).getTextField().setColumns(5);
        JPanel delayWrap = flow(spnDelay, new JLabel("phút nữa"));
        addRow(fields, 1, bold("Thực hiện sau:"), delayWrap);

        // Hàng 2: period (ẩn khi không dùng)
        spnPeriod = new JSpinner(new SpinnerNumberModel(30, 1, 1440, 1));
        spnPeriod.setFont(FONT_PLAIN);
        ((JSpinner.DefaultEditor) spnPeriod.getEditor()).getTextField().setColumns(5);
        JComboBox<String> cmbUnit = new JComboBox<>(new String[]{"phút", "giờ"});
        cmbUnit.setFont(FONT_PLAIN);
        repeatRow = flow(new JLabel("mỗi"), spnPeriod, cmbUnit);
        repeatRow.setVisible(false);
        addRow(fields, 2, bold("Lặp lại:"), repeatRow);

        // Hàng 3: nội dung
        txtContent = new JTextArea(3, 0);
        txtContent.setFont(FONT_PLAIN);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        addRow(fields, 3, bold("Nội dung:"), new JScrollPane(txtContent));

        cmbAction.addActionListener(e ->
            repeatRow.setVisible(cmbAction.getSelectedIndex() == 1));

        panel.add(fields, BorderLayout.CENTER);

        JButton btnAdd = ServerGuiUtils.createStyledButton("+ Thêm lịch hẹn", COL_PRIMARY, Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(200, 42));
        btnAdd.addActionListener(e -> addTask(cmbUnit));
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnWrap.setOpaque(false);
        btnWrap.add(btnAdd);
        panel.add(btnWrap, BorderLayout.SOUTH);

        wrapper.add(panel);
        wrapper.add(buildDailyMaintenancePanel());
        return wrapper;
    }

    private JPanel buildDailyMaintenancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(ServerGuiUtils.createSectionBorder("Bảo trì hằng ngày"));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        chkDailyMaintenance = new JCheckBox("Bật bảo trì tự động mỗi ngày");
        chkDailyMaintenance.setFont(FONT_BOLD);
        chkDailyMaintenance.setOpaque(false);
        addRow(fields, 0, bold("Trạng thái:"), chkDailyMaintenance);

        spnDailyHour = new JSpinner(new SpinnerNumberModel(5, 0, 23, 1));
        spnDailyMinute = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        spnDailyHour.setFont(FONT_PLAIN);
        spnDailyMinute.setFont(FONT_PLAIN);
        ((JSpinner.DefaultEditor) spnDailyHour.getEditor()).getTextField().setColumns(2);
        ((JSpinner.DefaultEditor) spnDailyMinute.getEditor()).getTextField().setColumns(2);
        addRow(fields, 1, bold("Giờ chạy:"), flow(spnDailyHour, new JLabel(":"), spnDailyMinute));

        chkDailyAutoRestart = new JCheckBox("Tự restart sau bảo trì");
        chkDailyAutoRestart.setFont(FONT_PLAIN);
        chkDailyAutoRestart.setOpaque(false);
        chkDailyAutoRestart.setSelected(true);
        addRow(fields, 2, bold("Restart:"), chkDailyAutoRestart);

        JLabel warnLabel = new JLabel("Cảnh báo tự động trước 10 phút, 5 phút, 1 phút");
        warnLabel.setFont(FONT_PLAIN);
        warnLabel.setForeground(new Color(90, 90, 90));
        addRow(fields, 3, bold("Nhắc trước:"), warnLabel);

        lblDailyStatus = new JLabel("Chưa bật lịch bảo trì hằng ngày.");
        lblDailyStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblDailyStatus.setForeground(new Color(90, 90, 90));
        addRow(fields, 4, bold("Lịch kế tiếp:"), lblDailyStatus);

        JButton btnSave = ServerGuiUtils.createStyledButton("Lưu lịch bảo trì", COL_GREEN, Color.WHITE);
        btnSave.setPreferredSize(new Dimension(180, 42));
        btnSave.addActionListener(e -> saveDailyMaintenanceConfig());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        buttonRow.setOpaque(false);
        buttonRow.add(btnSave);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttonRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(ServerGuiUtils.createSectionBorder("Tác vụ đang chờ"));

        model = new DefaultTableModel(
                new String[]{"ID", "Loại", "Nội dung", "Thực hiện lúc", "Còn lại", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(FONT_PLAIN);
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(36);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COL_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 36));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String s = String.valueOf(t.getModel().getValueAt(r, 5));
                    if ("Đã xong".equals(s))   setBackground(new Color(240, 255, 240));
                    else if ("Lỗi".equals(s))  setBackground(new Color(255, 240, 240));
                    else if ("Đã hủy".equals(s)) setBackground(new Color(248, 248, 248));
                    else                         setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        JButton btnCancel   = ServerGuiUtils.createStyledButton("Hủy đã chọn", COL_RED, Color.WHITE);
        JButton btnClear    = ServerGuiUtils.createStyledButton("Xóa đã xong/hủy", new Color(110, 110, 110), Color.WHITE);
        btnCancel.addActionListener(e -> cancelSelected());
        btnClear.addActionListener(e -> clearFinished());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnRow.setOpaque(false);
        btnRow.add(btnClear);
        btnRow.add(btnCancel);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    private void addTask(JComboBox<String> cmbUnit) {
        String content   = txtContent.getText().trim();
        int    actionIdx = cmbAction.getSelectedIndex();
        int    delay     = (int) spnDelay.getValue();
        boolean periodic = (actionIdx == 1);

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung thông báo.");
            return;
        }

        int   id    = idSeq.getAndIncrement();
        LocalDateTime runAt = LocalDateTime.now().plusMinutes(delay);
        String typeLabel    = (String) cmbAction.getSelectedItem();
        String preview      = content.isEmpty() ? "(bảo trì)" : content.substring(0, Math.min(content.length(), 50));

        String finalContent = content;
        Runnable action = () -> {
            try {
                ServerNotify.gI().notify(finalContent);
                if (periodic) {
                    updateNextRun(id, periodMinutes(cmbUnit));
                } else {
                    updateStatus(id, "Đã xong");
                }
            } catch (Exception ex) {
                updateStatus(id, "Lỗi");
            }
        };

        ScheduledFuture<?> future;
        if (periodic) {
            int period = (int) spnPeriod.getValue();
            long periodMin = periodMinutes(cmbUnit);
            future = scheduler.scheduleAtFixedRate(action, delay, periodMin, TimeUnit.MINUTES);
        } else {
            future = scheduler.schedule(action, delay, TimeUnit.MINUTES);
        }

        tasks.add(new TaskRow(id, future, runAt, periodic));
        model.addRow(new Object[]{
            id, typeLabel, preview, runAt.format(DT_FMT), formatSec(delay * 60L), "Đang chờ"
        });
        txtContent.setText("");
    }

    private void cancelSelected() {
        int vr = table.getSelectedRow();
        if (vr < 0) return;
        int id = (int) model.getValueAt(vr, 0);
        tasks.stream().filter(t -> t.id == id).findFirst().ifPresent(t -> {
            t.future.cancel(false);
            model.setValueAt("Đã hủy", vr, 5);
            model.setValueAt("—", vr, 4);
        });
    }

    private void clearFinished() {
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            String s = (String) model.getValueAt(i, 5);
            if ("Đã xong".equals(s) || "Đã hủy".equals(s) || "Lỗi".equals(s)) {
                int id = (int) model.getValueAt(i, 0);
                tasks.removeIf(t -> t.id == id);
                model.removeRow(i);
            }
        }
    }

    private void updateStatus(int id, String status) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < model.getRowCount(); i++) {
                if ((int) model.getValueAt(i, 0) == id) {
                    model.setValueAt(status, i, 5);
                    if (!"Đang chờ".equals(status)) model.setValueAt("—", i, 4);
                    break;
                }
            }
        });
    }

    private long periodMinutes(JComboBox<String> cmbUnit) {
        int period = (int) spnPeriod.getValue();
        return "giờ".equals(cmbUnit.getSelectedItem()) ? period * 60L : period;
    }

    private void updateNextRun(int id, long periodMin) {
        SwingUtilities.invokeLater(() -> {
            for (TaskRow task : tasks) {
                if (task.id == id) {
                    task.runAt = LocalDateTime.now().plusMinutes(periodMin);
                    break;
                }
            }
            for (int i = 0; i < model.getRowCount(); i++) {
                if ((int) model.getValueAt(i, 0) == id) {
                    TaskRow row = tasks.stream().filter(t -> t.id == id).findFirst().orElse(null);
                    if (row != null) {
                        model.setValueAt(row.runAt.format(DT_FMT), i, 3);
                        model.setValueAt("Đang chờ", i, 5);
                    }
                    break;
                }
            }
        });
    }

    private void loadDailyMaintenanceConfig() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(DAILY_MAINTENANCE_CONFIG)) {
            props.load(in);
        } catch (Exception ignored) {
            setDailyStatus("Chưa bật lịch bảo trì hằng ngày.", new Color(90, 90, 90));
            return;
        }

        boolean enabled = Boolean.parseBoolean(props.getProperty("enabled", "false"));
        int hour = parseBoundedInt(props.getProperty("hour"), 5, 0, 23);
        int minute = parseBoundedInt(props.getProperty("minute"), 0, 0, 59);
        boolean autoRestart = Boolean.parseBoolean(props.getProperty("autoRestart", "true"));

        chkDailyMaintenance.setSelected(enabled);
        spnDailyHour.setValue(hour);
        spnDailyMinute.setValue(minute);
        chkDailyAutoRestart.setSelected(autoRestart);

        if (enabled) {
            scheduleNextDailyMaintenance();
        } else {
            setDailyStatus("Lịch bảo trì hằng ngày đang tắt.", new Color(90, 90, 90));
        }
    }

    private void saveDailyMaintenanceConfig() {
        Properties props = new Properties();
        props.setProperty("enabled", String.valueOf(chkDailyMaintenance.isSelected()));
        props.setProperty("hour", String.valueOf((int) spnDailyHour.getValue()));
        props.setProperty("minute", String.valueOf((int) spnDailyMinute.getValue()));
        props.setProperty("autoRestart", String.valueOf(chkDailyAutoRestart.isSelected()));

        try (FileOutputStream out = new FileOutputStream(DAILY_MAINTENANCE_CONFIG)) {
            props.store(out, "Daily maintenance schedule for Server Control Panel");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được lịch bảo trì: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (chkDailyMaintenance.isSelected()) {
            scheduleNextDailyMaintenance();
        } else {
            cancelDailyMaintenanceSchedule();
            setDailyStatus("Lịch bảo trì hằng ngày đang tắt.", new Color(90, 90, 90));
        }
    }

    private void scheduleNextDailyMaintenance() {
        cancelDailyMaintenanceSchedule();

        int hour = (int) spnDailyHour.getValue();
        int minute = (int) spnDailyMinute.getValue();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.with(LocalTime.of(hour, minute, 0));
        if (!target.isAfter(now)) {
            target = target.plusDays(1);
        }

        long delaySec = java.time.Duration.between(now, target).getSeconds();
        dailyMaintenanceFuture = scheduler.schedule(this::runDailyMaintenance, delaySec, TimeUnit.SECONDS);
        scheduleMaintenanceWarnings(now, target);

        setDailyStatus("Đã bật: " + target.format(DT_FMT)
                + " | AutoRestart: " + chkDailyAutoRestart.isSelected(), COL_GREEN);
    }

    private void scheduleMaintenanceWarnings(LocalDateTime now, LocalDateTime target) {
        for (int minute : MAINTENANCE_WARNINGS) {
            LocalDateTime warnAt = target.minusMinutes(minute);
            long delaySec = java.time.Duration.between(now, warnAt).getSeconds();
            if (delaySec <= 0) {
                continue;
            }
            dailyMaintenanceWarnings.add(scheduler.schedule(() -> sendMaintenanceWarning(minute),
                    delaySec, TimeUnit.SECONDS));
        }
    }

    private void sendMaintenanceWarning(int minute) {
        try {
            ServerNotify.gI().notify("Server sẽ bảo trì sau " + minute + " phút. Vui lòng thoát game an toàn.");
        } catch (Exception ignored) {
        }
    }

    private void runDailyMaintenance() {
        try {
            ServerManagerUI.REQUEST_AUTO_RESTART = chkDailyAutoRestart.isSelected();
            nro.server.Maintenance.gI().start(1);
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                    "Lỗi chạy bảo trì hằng ngày: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
        } finally {
            if (chkDailyMaintenance.isSelected()) {
                SwingUtilities.invokeLater(this::scheduleNextDailyMaintenance);
            }
        }
    }

    private void cancelDailyMaintenanceSchedule() {
        if (dailyMaintenanceFuture != null) {
            dailyMaintenanceFuture.cancel(false);
            dailyMaintenanceFuture = null;
        }
        for (ScheduledFuture<?> future : dailyMaintenanceWarnings) {
            future.cancel(false);
        }
        dailyMaintenanceWarnings.clear();
    }

    private int parseBoundedInt(String value, int fallback, int min, int max) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(min, Math.min(max, parsed));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void setDailyStatus(String text, Color color) {
        if (lblDailyStatus == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            lblDailyStatus.setText(text);
            lblDailyStatus.setForeground(color);
        });
    }

    private void refreshCountdown() {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (!"Đang chờ".equals(model.getValueAt(i, 5))) continue;
            int id = (int) model.getValueAt(i, 0);
            final int row = i;
            tasks.stream().filter(t -> t.id == id).findFirst().ifPresent(t -> {
                if (t.future.isCancelled() || t.future.isDone()) return;
                long sec = java.time.Duration.between(now, t.runAt).getSeconds();
                model.setValueAt(sec > 0 ? formatSec(sec) : "Sắp xong...", row, 4);
            });
        }
    }

    private String formatSec(long s) {
        if (s <= 0) return "0s";
        long h = s / 3600, m = (s % 3600) / 60, r = s % 60;
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + r + "s";
        return r + "s";
    }

    // ---- UI helpers ----
    private JLabel bold(String text) {
        JLabel l = new JLabel(text); l.setFont(FONT_BOLD); return l;
    }

    private JPanel flow(Component... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        for (Component c : comps) p.add(c);
        return p;
    }

    private void addRow(JPanel grid, int row, JLabel label, Component field) {
        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0; gl.gridy = row; gl.anchor = GridBagConstraints.NORTHWEST;
        gl.insets = new Insets(6, 8, 6, 12); gl.weightx = 0;

        GridBagConstraints gf = new GridBagConstraints();
        gf.gridx = 1; gf.gridy = row; gf.fill = GridBagConstraints.HORIZONTAL;
        gf.insets = new Insets(4, 0, 4, 8); gf.weightx = 1.0;

        grid.add(label, gl);
        grid.add(field, gf);
    }

    private static class TaskRow {
        int id;
        ScheduledFuture<?> future;
        LocalDateTime runAt;
        boolean periodic;
        TaskRow(int id, ScheduledFuture<?> future, LocalDateTime runAt, boolean periodic) {
            this.id = id; this.future = future; this.runAt = runAt; this.periodic = periodic;
        }
    }
}
