package nro.server.ui;

import models.Template.MapTemplate;
import nro.server.Manager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class MapDataPanel extends JPanel {

    private static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Color COL_PRIMARY = new Color(0, 120, 215);

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JLabel lblCount;

    private boolean loaded = false;

    public MapDataPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing() && !loaded) {
                loaded = true;
                loadData();
            }
        });
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("DỮ LIỆU BẢN ĐỒ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40, 40, 40));

        lblCount = new JLabel();
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCount.setForeground(new Color(100, 100, 100));

        txtSearch = new JTextField(22);
        txtSearch.setPreferredSize(new Dimension(250, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm ID hoặc tên bản đồ...");
        txtSearch.setFont(FONT_PLAIN);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });

        JButton btnReload = ServerGuiUtils.createStyledButton("↺ Tải lại", new Color(100, 100, 100), Color.WHITE);
        btnReload.addActionListener(e -> { model.setRowCount(0); loadData(); });

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Tìm:"));
        searchBar.add(txtSearch);
        searchBar.add(btnReload);
        searchBar.add(lblCount);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.setBorder(new EmptyBorder(0, 0, 8, 0));
        north.add(lblTitle,   BorderLayout.NORTH);
        north.add(searchBar,  BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "Tên bản đồ", "Loại", "Hành tinh", "Số vùng", "Max/vùng", "Mob", "NPC"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 1 ? String.class : Integer.class;
            }
        };
        sorter = new TableRowSorter<>(model);

        table = new JTable(model);
        table.setRowSorter(sorter);
        table.setFont(FONT_PLAIN);
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COL_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 36));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadData() {
        MapTemplate[] templates = Manager.MAP_TEMPLATES;
        if (templates == null) {
            lblCount.setText("Server chưa khởi động xong — nhấn Tải lại.");
            return;
        }
        new Thread(() -> {
            List<Object[]> rows = new ArrayList<>();
            for (MapTemplate m : templates) {
                if (m == null) continue;
                rows.add(new Object[]{
                    m.id,
                    m.name != null ? m.name : "",
                    (int) m.type,
                    (int) m.planetId,
                    (int) m.zones,
                    (int) m.maxPlayerPerZone,
                    m.mobTemp != null ? m.mobTemp.length : 0,
                    m.npcId   != null ? m.npcId.length   : 0
                });
            }
            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                for (Object[] r : rows) model.addRow(r);
                lblCount.setText("  Tổng: " + rows.size() + " bản đồ");
            });
        }).start();
    }

    private void filter() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) { sorter.setRowFilter(null); return; }
        try {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw, 0, 1));
        } catch (PatternSyntaxException ignored) {}
    }
}
