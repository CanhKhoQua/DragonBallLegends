package nro.server.ui;

import jdbc.DBConnecter;
import nro.server.Manager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class NotifyEditorPanel extends JPanel {

    private static final Color COL_PRIMARY = new Color(0, 120, 215);
    private static final Color COL_GREEN = new Color(40, 167, 69);
    private static final Color COL_DANGER = new Color(220, 53, 69);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtId;
    private JTextField txtName;
    private JTextArea txtText;
    private JLabel lblStatus;

    public NotifyEditorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        loadData();
    }

    private void initUI() {
        JPanel north = new JPanel(new BorderLayout(10, 8));
        north.setOpaque(false);

        JLabel title = new JLabel("THÔNG BÁO / HƯỚNG DẪN TRONG GAME");
        title.setFont(FONT_TITLE);
        title.setForeground(new Color(40, 40, 40));
        north.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnReload = ServerGuiUtils.createStyledButton("Tải lại", new Color(100, 100, 100), Color.WHITE);
        JButton btnNew = ServerGuiUtils.createStyledButton("Tạo mới", COL_PRIMARY, Color.WHITE);
        JButton btnSave = ServerGuiUtils.createStyledButton("Lưu", COL_GREEN, Color.WHITE);
        JButton btnDelete = ServerGuiUtils.createStyledButton("Xoá", COL_DANGER, Color.WHITE);
        btnReload.addActionListener(e -> loadData());
        btnNew.addActionListener(e -> clearEditor());
        btnSave.addActionListener(e -> saveNotify());
        btnDelete.addActionListener(e -> deleteNotify());
        actions.add(btnReload);
        actions.add(btnNew);
        actions.add(btnSave);
        actions.add(btnDelete);
        north.add(actions, BorderLayout.EAST);
        add(north, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Tiêu đề", "Nội dung"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setFont(FONT_PLAIN);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.getSelectionModel().addListSelectionListener(this::onSelectRow);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COL_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 36));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(520);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JPanel editor = buildEditor();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), editor);
        split.setResizeWeight(0.48);
        split.setDividerSize(7);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(90, 100, 115));
        add(lblStatus, BorderLayout.SOUTH);
    }

    private JPanel buildEditor() {
        JPanel editor = new JPanel(new BorderLayout(0, 10));
        editor.setOpaque(false);
        editor.setBorder(ServerGuiUtils.createSectionBorder("Nội dung hiển thị trong menu Thông báo"));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 0, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setFont(FONT_PLAIN);
        txtName = new JTextField();
        txtName.setFont(FONT_PLAIN);

        addRow(fields, gbc, 0, "ID", txtId);
        addRow(fields, gbc, 1, "Tiêu đề", txtName);
        editor.add(fields, BorderLayout.NORTH);

        txtText = new JTextArea();
        txtText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtText.setLineWrap(true);
        txtText.setWrapStyleWord(true);
        JScrollPane scrollText = new JScrollPane(txtText);
        scrollText.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(220, 226, 235))));
        editor.add(scrollText, BorderLayout.CENTER);

        JTextArea hint = new JTextArea(
                "Dòng này nằm trong table notify. Người chơi mở menu Thông báo sẽ đọc được.\n"
                        + "Sau khi lưu, panel sẽ reload cache Manager.NOTIFY ngay, không cần restart server.");
        hint.setEditable(false);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setForeground(new Color(80, 80, 80));
        hint.setBackground(new Color(248, 250, 253));
        hint.setBorder(new EmptyBorder(10, 12, 10, 12));
        editor.add(hint, BorderLayout.SOUTH);
        return editor;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        input.setPreferredSize(new Dimension(0, 34));
        panel.add(input, gbc);
    }

    private void loadData() {
        new Thread(() -> {
            try (Connection con = DBConnecter.getConnectionServer();
                 PreparedStatement ps = con.prepareStatement("SELECT id, name, text FROM notify ORDER BY id DESC");
                 ResultSet rs = ps.executeQuery()) {
                DefaultTableModel next = new DefaultTableModel(new String[]{"ID", "Tiêu đề", "Nội dung"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                while (rs.next()) {
                    String text = rs.getString("text");
                    String preview = text == null ? "" : text.replace("\r", "").replace("\n", " ");
                    next.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            preview.substring(0, Math.min(preview.length(), 120))
                    });
                }
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (int i = 0; i < next.getRowCount(); i++) {
                        model.addRow(new Object[]{next.getValueAt(i, 0), next.getValueAt(i, 1), next.getValueAt(i, 2)});
                    }
                    clearEditor();
                    setStatus("Đã tải " + model.getRowCount() + " thông báo.", false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> setStatus("Lỗi tải thông báo: " + ex.getMessage(), true));
            }
        }, "NotifyEditorPanel-Load").start();
    }

    private void onSelectRow(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) {
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        loadDetail(id);
    }

    private void loadDetail(int id) {
        new Thread(() -> {
            try (Connection con = DBConnecter.getConnectionServer();
                 PreparedStatement ps = con.prepareStatement("SELECT id, name, text FROM notify WHERE id = ? LIMIT 1")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int rowId = rs.getInt("id");
                        String name = rs.getString("name");
                        String text = rs.getString("text");
                        SwingUtilities.invokeLater(() -> {
                            txtId.setText(String.valueOf(rowId));
                            txtName.setText(name == null ? "" : name);
                            txtText.setText(text == null ? "" : text);
                            txtText.setCaretPosition(0);
                        });
                    }
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> setStatus("Lỗi tải chi tiết: " + ex.getMessage(), true));
            }
        }, "NotifyEditorPanel-Detail").start();
    }

    private void saveNotify() {
        String name = txtName.getText().trim();
        String text = txtText.getText().trim();
        if (name.isEmpty() || text.isEmpty()) {
            setStatus("Cần nhập đủ tiêu đề và nội dung.", true);
            return;
        }
        Integer id = txtId.getText().trim().isEmpty() ? null : Integer.parseInt(txtId.getText().trim());
        new Thread(() -> {
            try (Connection con = DBConnecter.getConnectionServer()) {
                if (id == null) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO notify(name, text) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, name);
                        ps.setString(2, text);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                int generatedId = keys.getInt(1);
                                SwingUtilities.invokeLater(() -> txtId.setText(String.valueOf(generatedId)));
                            }
                        }
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE notify SET name = ?, text = ? WHERE id = ?")) {
                        ps.setString(1, name);
                        ps.setString(2, text);
                        ps.setInt(3, id);
                        ps.executeUpdate();
                    }
                }
                Manager.reloadNotify();
                SwingUtilities.invokeLater(() -> {
                    loadData();
                    setStatus("Đã lưu và reload cache thông báo.", false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> setStatus("Lỗi lưu: " + ex.getMessage(), true));
            }
        }, "NotifyEditorPanel-Save").start();
    }

    private void deleteNotify() {
        if (txtId.getText().trim().isEmpty()) {
            setStatus("Chưa chọn thông báo để xoá.", true);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Xoá thông báo này khỏi menu Thông báo trong game?",
                "Xác nhận xoá", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        int id = Integer.parseInt(txtId.getText().trim());
        new Thread(() -> {
            try (Connection con = DBConnecter.getConnectionServer();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM notify WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                Manager.reloadNotify();
                SwingUtilities.invokeLater(() -> {
                    loadData();
                    setStatus("Đã xoá và reload cache thông báo.", false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> setStatus("Lỗi xoá: " + ex.getMessage(), true));
            }
        }, "NotifyEditorPanel-Delete").start();
    }

    private void clearEditor() {
        txtId.setText("");
        txtName.setText("");
        txtText.setText("");
        if (table.getSelectedRow() >= 0) {
            table.clearSelection();
        }
    }

    private void setStatus(String text, boolean error) {
        lblStatus.setText(text);
        lblStatus.setForeground(error ? COL_DANGER : new Color(0, 130, 0));
    }
}
