package nro.server.ui;

import jdbc.DBConnecter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel xem lịch sử đăng nhập từ bảng login_log.
 *
 * Để log tự động, thêm vào flow login của server:
 *   DBConnecter.executeUpdate(
 *       "INSERT INTO login_log (username, ip, status) VALUES (?, ?, ?)",
 *       username, clientIp, success ? 1 : 0);
 */
public class LoginHistoryPanel extends JPanel {

    private static final String CREATE_SQL =
        "CREATE TABLE IF NOT EXISTS login_log (\n" +
        "  id         BIGINT       AUTO_INCREMENT PRIMARY KEY,\n" +
        "  username   VARCHAR(50)  NOT NULL,\n" +
        "  ip         VARCHAR(45),\n" +
        "  status     TINYINT(1)   DEFAULT 1 COMMENT '1=thành công, 0=thất bại',\n" +
        "  login_time DATETIME     DEFAULT CURRENT_TIMESTAMP,\n" +
        "  INDEX idx_u (username),\n" +
        "  INDEX idx_t (login_time)\n" +
        ")";

    private static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Color COL_PRIMARY = new Color(0, 120, 215);
    private static final Color COL_RED     = new Color(200, 50, 50);

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private CardLayout card;
    private JPanel mainCards;

    public LoginHistoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("LỊCH SỬ ĐĂNG NHẬP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40, 40, 40));
        lblTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(lblTitle, BorderLayout.NORTH);

        card = new CardLayout();
        mainCards = new JPanel(card);
        mainCards.setBackground(Color.WHITE);
        mainCards.add(buildSetupPanel(), "setup");
        mainCards.add(buildQueryPanel(), "query");
        add(mainCards, BorderLayout.CENTER);

        checkTableAndSwitch();
    }

    // ---- Setup panel (khi bảng chưa tồn tại) ----
    private JPanel buildSetupPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(Color.WHITE);

        JTextArea info = new JTextArea(
            "⚠  Bảng login_log chưa tồn tại trong database.\n\n" +
            "Nhấn nút bên dưới để tạo bảng tự động.\n\n" +
            "Sau đó, thêm vào flow đăng nhập của server:\n\n" +
            "    DBConnecter.executeUpdate(\n" +
            "        \"INSERT INTO login_log (username, ip, status) VALUES (?, ?, ?)\",\n" +
            "        username, clientIp, success ? 1 : 0);\n\n" +
            "SQL sẽ chạy:\n\n" + CREATE_SQL);
        info.setEditable(false);
        info.setFont(new Font("Consolas", Font.PLAIN, 12));
        info.setBackground(new Color(255, 253, 230));
        info.setBorder(new EmptyBorder(14, 16, 14, 16));
        info.setLineWrap(true);
        info.setWrapStyleWord(true);

        JButton btnCreate = ServerGuiUtils.createStyledButton("Tạo bảng login_log", COL_PRIMARY, Color.WHITE);
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreate.setPreferredSize(new Dimension(220, 44));
        btnCreate.addActionListener(e -> createTableAndSwitch());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrap.setOpaque(false);
        btnWrap.add(btnCreate);

        p.add(new JScrollPane(info), BorderLayout.CENTER);
        p.add(btnWrap, BorderLayout.SOUTH);
        return p;
    }

    // ---- Query panel (khi bảng tồn tại) ----
    private JPanel buildQueryPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        txtSearch = new JTextField(22);
        txtSearch.setPreferredSize(new Dimension(240, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tên tài khoản hoặc IP...");
        txtSearch.setFont(FONT_PLAIN);
        txtSearch.addActionListener(e -> loadData(txtSearch.getText().trim(), false));

        JButton btnSearch   = ServerGuiUtils.createStyledButton("Tìm", COL_PRIMARY, Color.WHITE);
        JButton btnAll      = ServerGuiUtils.createStyledButton("Tất cả", new Color(100, 100, 100), Color.WHITE);
        JButton btnOnlyFail = ServerGuiUtils.createStyledButton("Chỉ thất bại", COL_RED, Color.WHITE);
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim(), false));
        btnAll.addActionListener(e -> { txtSearch.setText(""); loadData("", false); });
        btnOnlyFail.addActionListener(e -> loadData("", true));

        searchBar.add(new JLabel("Tìm:"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnAll);
        searchBar.add(btnOnlyFail);

        model = new DefaultTableModel(
                new String[]{"Thời gian", "Tài khoản", "IP", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(FONT_PLAIN);
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
                    boolean fail = "Thất bại".equals(t.getModel().getValueAt(r, 3));
                    setBackground(fail ? new Color(255, 235, 235)
                                       : (r % 2 == 0 ? Color.WHITE : new Color(248, 252, 248)));
                    setForeground(fail ? COL_RED : Color.BLACK);
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        p.add(searchBar, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void checkTableAndSwitch() {
        new Thread(() -> {
            try (Connection con = DBConnecter.getConnectionServer()) {
                ResultSet rs = con.getMetaData().getTables(null, null, "login_log", new String[]{"TABLE"});
                boolean exists = rs.next();
                rs.close();
                SwingUtilities.invokeLater(() -> {
                    card.show(mainCards, exists ? "query" : "setup");
                    if (exists) loadData("", false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> card.show(mainCards, "setup"));
            }
        }).start();
    }

    private void createTableAndSwitch() {
        new Thread(() -> {
            try (Connection con = DBConnecter.getConnectionServer();
                 Statement st = con.createStatement()) {
                st.execute(CREATE_SQL);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Đã tạo bảng login_log thành công!\nBây giờ hãy thêm logging vào flow đăng nhập của server.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    card.show(mainCards, "query");
                    loadData("", false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "Lỗi tạo bảng: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void loadData(String keyword, boolean onlyFail) {
        new Thread(() -> {
            try (Connection con = DBConnecter.getConnectionServer()) {
                StringBuilder sql = new StringBuilder(
                    "SELECT login_time, username, ip, status FROM login_log WHERE 1=1");
                if (!keyword.isEmpty()) sql.append(" AND (username LIKE ? OR ip LIKE ?)");
                if (onlyFail) sql.append(" AND status = 0");
                sql.append(" ORDER BY login_time DESC LIMIT 500");

                PreparedStatement ps = con.prepareStatement(sql.toString());
                int idx = 1;
                if (!keyword.isEmpty()) {
                    ps.setString(idx++, "%" + keyword + "%");
                    ps.setString(idx,   "%" + keyword + "%");
                }
                List<Object[]> rows = new ArrayList<>();
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getTimestamp("login_time") != null ? rs.getTimestamp("login_time").toString() : "",
                        rs.getString("username"),
                        rs.getString("ip"),
                        rs.getInt("status") == 1 ? "Thành công" : "Thất bại"
                    });
                }
                rs.close(); ps.close();
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (Object[] row : rows) model.addRow(row);
                });
            } catch (Exception ex) {
                System.err.println("[LoginHistoryPanel] " + ex.getMessage());
            }
        }).start();
    }
}
