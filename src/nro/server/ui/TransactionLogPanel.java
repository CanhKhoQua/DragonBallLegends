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

public class TransactionLogPanel extends JPanel {

    private static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Color COL_PRIMARY = new Color(0, 120, 215);

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JTextArea txtDetail;

    // Full data cache (bag before/after) indexed by model row
    private final List<String[]> detailCache = new ArrayList<>();

    public TransactionLogPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        loadData("");
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("LỊCH SỬ GIAO DỊCH");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40, 40, 40));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tên player (để trống = tất cả)...");
        txtSearch.setFont(FONT_PLAIN);
        txtSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));

        JButton btnSearch = ServerGuiUtils.createStyledButton("Tìm", COL_PRIMARY, Color.WHITE);
        JButton btnAll    = ServerGuiUtils.createStyledButton("Tất cả", new Color(100, 100, 100), Color.WHITE);
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        btnAll.addActionListener(e -> { txtSearch.setText(""); loadData(""); });

        searchBar.add(new JLabel("Tìm player:"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnAll);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.setBorder(new EmptyBorder(0, 0, 8, 0));
        north.add(lblTitle, BorderLayout.NORTH);
        north.add(searchBar, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"Thời gian", "Player 1", "Player 2", "Đồ P1 giao", "Đồ P2 giao"}, 0) {
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
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                showDetail(table.getSelectedRow());
        });

        txtDetail = new JTextArea(7, 0);
        txtDetail.setEditable(false);
        txtDetail.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtDetail.setBorder(new EmptyBorder(6, 8, 6, 8));
        txtDetail.setText("← Click vào một hàng để xem balo trước/sau giao dịch.");
        JScrollPane scrollDetail = new JScrollPane(txtDetail);
        scrollDetail.setBorder(ServerGuiUtils.createSectionBorder("Chi tiết giao dịch"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), scrollDetail);
        split.setResizeWeight(0.62);
        split.setDividerSize(6);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private void loadData(String keyword) {
        new Thread(() -> {
            try (Connection con = DBConnecter.getConnectionServer()) {
                String sql;
                PreparedStatement ps;
                if (keyword.isEmpty()) {
                    sql = "SELECT player_1,player_2,item_player_1,item_player_2," +
                          "bag_1_before_tran,bag_2_before_tran,bag_1_after_tran,bag_2_after_tran,time_tran " +
                          "FROM history_transaction ORDER BY time_tran DESC LIMIT 300";
                    ps = con.prepareStatement(sql);
                } else {
                    sql = "SELECT player_1,player_2,item_player_1,item_player_2," +
                          "bag_1_before_tran,bag_2_before_tran,bag_1_after_tran,bag_2_after_tran,time_tran " +
                          "FROM history_transaction WHERE player_1 LIKE ? OR player_2 LIKE ? " +
                          "ORDER BY time_tran DESC LIMIT 300";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, "%" + keyword + "%");
                    ps.setString(2, "%" + keyword + "%");
                }
                List<Object[]> displayRows = new ArrayList<>();
                List<String[]> details     = new ArrayList<>();
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    displayRows.add(new Object[]{
                        nvl(rs.getTimestamp("time_tran") != null ? rs.getTimestamp("time_tran").toString() : ""),
                        nvl(rs.getString("player_1")),
                        nvl(rs.getString("player_2")),
                        trunc(rs.getString("item_player_1"), 55),
                        trunc(rs.getString("item_player_2"), 55)
                    });
                    details.add(new String[]{
                        nvl(rs.getString("item_player_1")),
                        nvl(rs.getString("item_player_2")),
                        nvl(rs.getString("bag_1_before_tran")),
                        nvl(rs.getString("bag_2_before_tran")),
                        nvl(rs.getString("bag_1_after_tran")),
                        nvl(rs.getString("bag_2_after_tran"))
                    });
                }
                rs.close(); ps.close();
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    detailCache.clear();
                    detailCache.addAll(details);
                    for (Object[] row : displayRows) model.addRow(row);
                    txtDetail.setText("← Click vào một hàng để xem balo trước/sau giao dịch.");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> txtDetail.setText("Lỗi tải dữ liệu: " + ex.getMessage()));
            }
        }).start();
    }

    private void showDetail(int viewRow) {
        if (detailCache.isEmpty() || viewRow >= detailCache.size()) return;
        String[] d  = detailCache.get(viewRow);
        String   p1 = (String) model.getValueAt(viewRow, 1);
        String   p2 = (String) model.getValueAt(viewRow, 2);
        StringBuilder sb = new StringBuilder();
        sb.append("▶ ").append(p1).append("\n");
        sb.append("  Đồ giao dịch : ").append(d[0]).append("\n");
        sb.append("  Balo TRƯỚC   : ").append(d[2]).append("\n");
        sb.append("  Balo SAU     : ").append(d[4]).append("\n\n");
        sb.append("▶ ").append(p2).append("\n");
        sb.append("  Đồ giao dịch : ").append(d[1]).append("\n");
        sb.append("  Balo TRƯỚC   : ").append(d[3]).append("\n");
        sb.append("  Balo SAU     : ").append(d[5]);
        txtDetail.setText(sb.toString());
        txtDetail.setCaretPosition(0);
    }

    private String trunc(String s, int len) {
        if (s == null) return "";
        return s.length() > len ? s.substring(0, len) + "…" : s;
    }
    private String nvl(String s) { return s != null ? s : ""; }
}
