package nro.server.ui;

import nro.server.ServerNotify;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BroadcastPanel extends JPanel {

    private static final Color COL_GREEN  = new Color(40, 167, 69);
    private static final Color COL_ORANGE = new Color(200, 110, 0);

    private JTextArea txtMessage;
    private DefaultListModel<String> historyModel;
    private JLabel lblStatus;

    public BroadcastPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("THÔNG BÁO SERVER");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40, 40, 40));
        lblTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(lblTitle, BorderLayout.NORTH);

        // ---- Compose panel ----
        JPanel compose = new JPanel(new BorderLayout(0, 10));
        compose.setOpaque(false);
        compose.setBorder(ServerGuiUtils.createSectionBorder("Soạn thông báo"));

        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        typeRow.setOpaque(false);
        typeRow.add(new JLabel("Kiểu gửi:"));
        JComboBox<String> cmbType = new JComboBox<>(new String[]{
            "Chat VIP  — tất cả player đang online (chữ vàng)"
        });
        cmbType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeRow.add(cmbType);
        compose.add(typeRow, BorderLayout.NORTH);

        txtMessage = new JTextArea();
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMessage.setLineWrap(true);
        txtMessage.setWrapStyleWord(true);
        compose.add(new JScrollPane(txtMessage), BorderLayout.CENTER);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        JButton btnSend = ServerGuiUtils.createStyledButton("📢  GỬI TỚI TẤT CẢ PLAYER", COL_GREEN, Color.WHITE);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setPreferredSize(new Dimension(280, 44));
        btnSend.addActionListener(e -> sendBroadcast());

        JPanel btnRow = new JPanel(new BorderLayout(8, 0));
        btnRow.setOpaque(false);
        btnRow.add(lblStatus, BorderLayout.CENTER);
        btnRow.add(btnSend,   BorderLayout.EAST);
        compose.add(btnRow, BorderLayout.SOUTH);

        // ---- Info note ----
        JTextArea note = new JTextArea(
            "ℹ️  Chat VIP: tin nhắn màu vàng cuộn trên màn hình của tất cả player đang online.\n" +
            "Gửi xong không thể thu hồi — kiểm tra kỹ trước khi nhấn Gửi.");
        note.setEditable(false);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(new Color(80, 80, 80));
        note.setBackground(new Color(255, 252, 225));
        note.setBorder(new EmptyBorder(10, 12, 10, 12));
        note.setLineWrap(true);
        note.setWrapStyleWord(true);

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.add(compose, BorderLayout.CENTER);
        left.add(note,    BorderLayout.SOUTH);

        // ---- History panel ----
        historyModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Consolas", Font.PLAIN, 12));
        historyList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean sel, boolean foc) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, sel, foc);
                lbl.setBorder(new EmptyBorder(5, 8, 5, 8));
                if (!sel) lbl.setBackground(index % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                return lbl;
            }
        });

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setOpaque(false);
        right.setBorder(ServerGuiUtils.createSectionBorder("Lịch sử gửi (phiên này)"));
        JButton btnClear = ServerGuiUtils.createStyledButton("Xóa lịch sử", new Color(160, 50, 50), Color.WHITE);
        btnClear.addActionListener(e -> historyModel.clear());
        right.add(new JScrollPane(historyList), BorderLayout.CENTER);
        right.add(btnClear, BorderLayout.SOUTH);
        right.setPreferredSize(new Dimension(290, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.72);
        split.setDividerSize(6);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private void sendBroadcast() {
        String msg = txtMessage.getText().trim();
        if (msg.isEmpty()) { setStatus("⚠ Nội dung trống.", COL_ORANGE); return; }
        int ok = JOptionPane.showConfirmDialog(this,
            "Gửi thông báo sau tới TẤT CẢ player?\n\n\"" + msg + "\"",
            "Xác nhận gửi", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            ServerNotify.gI().notify(msg);
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            historyModel.add(0, "[" + time + "]  " +
                msg.replace("\n", "↵").substring(0, Math.min(msg.length(), 70)));
            txtMessage.setText("");
            setStatus("✓ Đã gửi lúc " + time, new Color(0, 140, 0));
        } catch (Exception ex) {
            setStatus("✗ Lỗi: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, "Không gửi được:\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setStatus(String text, Color color) {
        lblStatus.setText(text);
        lblStatus.setForeground(color);
    }
}
