package nro.server.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import jdbc.DBConnecter;
import models.Template.ItemTemplate;
import nro.server.Manager;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ItemDataPanel extends JPanel {

    private static final String ICON_FOLDER = "data/icon/";
    private static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Color COL_PRIMARY = new Color(0, 120, 215);
    private static final Color COL_GREEN   = new Color(40, 167, 69);

    private static final String[] PLANET_NAMES = {"Trái đất", "Namếc", "Xayda"};
    private static final Map<Integer, String> KNOWN_ITEM_TYPE_NAMES = new HashMap<>();
    static {
        KNOWN_ITEM_TYPE_NAMES.put(0, "Áo");
        KNOWN_ITEM_TYPE_NAMES.put(1, "Quần");
        KNOWN_ITEM_TYPE_NAMES.put(2, "Găng");
        KNOWN_ITEM_TYPE_NAMES.put(3, "Giày");
        KNOWN_ITEM_TYPE_NAMES.put(4, "Rada");
        KNOWN_ITEM_TYPE_NAMES.put(5, "Cải trang/Tóc");
        KNOWN_ITEM_TYPE_NAMES.put(6, "Đậu thần");
        KNOWN_ITEM_TYPE_NAMES.put(11, "Đồ đeo lưng");
        KNOWN_ITEM_TYPE_NAMES.put(12, "Ngọc rồng");
        KNOWN_ITEM_TYPE_NAMES.put(21, "Pet");
        KNOWN_ITEM_TYPE_NAMES.put(23, "Thú cưỡi");
        KNOWN_ITEM_TYPE_NAMES.put(24, "Thú cưỡi");
        KNOWN_ITEM_TYPE_NAMES.put(27, "Vật phẩm");
        KNOWN_ITEM_TYPE_NAMES.put(29, "Capsule/Bánh");
        KNOWN_ITEM_TYPE_NAMES.put(32, "Giáp tập");
    }
    private static final String[] GENDER_FILTERS = {
        "- Tất cả Hệ -", "0 - Trái Đất", "1 - Namếc", "2 - Xayda", "3 - Chung/Tất cả"
    };
    private static final String[] CLIENT_CF_LABELS = {
        "0 dung yen 1", "1 dung yen 2", "2 chay 1", "3 chay 2", "4 chay 3", "5 chay 4", "6 chay 5",
        "7 roi/bay", "8 dap dat", "9 bay ngang 1", "10 bay ngang 2", "11 bay ngang 3", "12 bay dung",
        "13 dam 1", "14 dam 2", "15 bi danh 1", "16 bi danh 2", "17 nga 1", "18 nga 2", "19 chuong 1",
        "20 chuong 2", "21 chuong 3", "22 dung/cho", "23 trung don", "24 skill start", "25 move nhanh",
        "26 fly dam 1", "27 fly dam 2", "28 fly bi danh 1", "29 fly bi danh 2", "30 fly start", "31 fly chuong",
        "32 reset"
    };
    private static final int PART_HEAD = 0;
    private static final int PART_LEG = 1;
    private static final int PART_BODY = 2;
    private static final int CI_FRAME = 0;
    private static final int CI_DX = 1;
    private static final int CI_DY = 2;
    private static final int[][][] CLIENT_CHAR_INFO = {
        {{0, -13, 34}, {1, -8, 10}, {1, -9, 16}},
        {{0, -13, 35}, {1, -8, 10}, {1, -9, 17}},
        {{1, -10, 33}, {2, -10, 11}, {2, -8, 16}},
        {{1, -10, 32}, {3, -12, 10}, {3, -11, 15}},
        {{1, -10, 34}, {4, -8, 11}, {4, -7, 17}},
        {{1, -10, 34}, {5, -12, 11}, {5, -9, 17}},
        {{1, -10, 33}, {6, -10, 10}, {6, -8, 16}},
        {{0, -9, 36}, {7, -5, 17}, {7, -11, 25}},
        {{0, -7, 35}, {0, -18, 22}, {7, -10, 25}},
        {{1, -11, 35}, {10, -3, 25}, {12, -10, 26}},
        {{1, -11, 37}, {11, -3, 25}, {12, -11, 27}},
        {{0, -14, 34}, {12, -8, 21}, {9, -7, 31}},
        {{0, -12, 35}, {8, -5, 14}, {8, -15, 29}},
        {{1, -9, 34}, {9, -12, 9}, {10, -7, 19}},
        {{1, -13, 34}, {9, -12, 9}, {11, -10, 19}},
        {{1, -8, 32}, {9, -12, 9}, {2, -6, 15}},
        {{1, -8, 32}, {9, -12, 9}, {13, -12, 16}},
        {{0, -10, 31}, {9, -12, 9}, {7, -13, 20}},
        {{0, -11, 32}, {9, -12, 9}, {8, -15, 26}},
        {{0, -9, 33}, {9, -12, 9}, {14, -8, 18}},
        {{0, -11, 33}, {9, -12, 9}, {15, -6, 19}},
        {{0, -16, 31}, {9, -12, 9}, {9, -8, 28}},
        {{0, -14, 34}, {1, -8, 10}, {8, -16, 28}},
        {{0, -8, 36}, {7, -5, 17}, {0, -5, 25}},
        {{0, -9, 31}, {9, -12, 9}, {0, -6, 20}},
        {{2, -9, 36}, {13, -5, 17}, {16, -11, 25}},
        {{1, -9, 34}, {8, -5, 13}, {10, -7, 19}},
        {{1, -13, 34}, {8, -5, 13}, {11, -10, 19}},
        {{1, -8, 32}, {8, -5, 13}, {2, -6, 15}},
        {{1, -8, 32}, {8, -5, 13}, {13, -12, 16}},
        {{0, -9, 33}, {8, -5, 13}, {14, -8, 18}},
        {{0, -11, 33}, {8, -5, 13}, {15, -6, 19}},
        {{0, -16, 32}, {8, -5, 13}, {9, -8, 29}}
    };

    // Col indices
    private static final int COL_ID = 0, COL_ICON = 1, COL_NAME = 2, COL_TYPE = 3,
                             COL_GENDER = 4, COL_LEVEL = 5, COL_DESC = 6;

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JComboBox<String> cmbType;
    private JComboBox<String> cmbGender;
    private JLabel lblCount;

    private final Map<Integer, ImageIcon> iconCache = new HashMap<>();
    private final Map<Integer, Boolean>   noIcon    = new HashMap<>();
    private final Map<Integer, Integer> partIconMap = new HashMap<>();
    private final Map<Integer, List<PartFrame>> partFrameMap = new HashMap<>();

    private boolean loaded = false;

    public ItemDataPanel() {
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

    // -----------------------------------------------------------------------
    private void initUI() {
        JLabel lblTitle = new JLabel("DỮ LIỆU VẬT PHẨM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40, 40, 40));

        lblCount = new JLabel();
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCount.setForeground(new Color(100, 100, 100));

        // Search
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(230, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm ID hoặc tên...");
        txtSearch.setFont(FONT_PLAIN);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filter(); }
            @Override public void removeUpdate(DocumentEvent e)  { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });

        // Type/Gender filters follow the item picker used by ShopEditorPanel.
        cmbType = new JComboBox<>(buildItemTypeFilterOptions());
        cmbType.setFont(FONT_PLAIN);
        cmbType.setPreferredSize(new Dimension(170, 36));
        cmbType.addActionListener(e -> filter());

        cmbGender = new JComboBox<>(GENDER_FILTERS);
        cmbGender.setFont(FONT_PLAIN);
        cmbGender.setPreferredSize(new Dimension(160, 36));
        cmbGender.addActionListener(e -> filter());

        JButton btnReload = ServerGuiUtils.createStyledButton("↺", new Color(100, 100, 100), Color.WHITE);
        btnReload.setPreferredSize(new Dimension(40, 36));
        btnReload.setToolTipText("Tải lại dữ liệu");
        btnReload.addActionListener(e -> { iconCache.clear(); noIcon.clear(); model.setRowCount(0); loadData(); });

        JButton btnAdd = ServerGuiUtils.createStyledButton("+ Thêm item", COL_GREEN, Color.WHITE);
        btnAdd.setFont(FONT_BOLD);
        btnAdd.addActionListener(e -> openItemDialog(null));
        JButton btnWorkshop = ServerGuiUtils.createStyledButton("Costume Workshop", new Color(23, 162, 184), Color.WHITE);
        btnWorkshop.setFont(FONT_BOLD);
        btnWorkshop.addActionListener(e -> showCostumeWorkshopDialog(this));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Tìm:"));
        searchBar.add(txtSearch);
        searchBar.add(new JLabel("Loại:"));
        searchBar.add(cmbType);
        searchBar.add(new JLabel("Hệ:"));
        searchBar.add(cmbGender);
        searchBar.add(btnReload);
        searchBar.add(lblCount);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(lblTitle, BorderLayout.WEST);
        JPanel titleActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        titleActions.setOpaque(false);
        titleActions.add(btnWorkshop);
        titleActions.add(btnAdd);
        titleRow.add(titleActions, BorderLayout.EAST);

        north.add(titleRow,  BorderLayout.NORTH);
        north.add(searchBar, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(
                new String[]{"ID", "Icon", "Tên vật phẩm", "Loại", "Hành tinh", "Level", "Mô tả"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == COL_ID || c == COL_TYPE || c == COL_GENDER || c == COL_LEVEL)
                    return Integer.class;
                return String.class;
            }
        };
        sorter = new TableRowSorter<>(model);

        table = new JTable(model);
        table.setRowSorter(sorter);
        table.setFont(FONT_PLAIN);
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(COL_ID).setPreferredWidth(42);
        table.getColumnModel().getColumn(COL_ID).setMaxWidth(56);
        table.getColumnModel().getColumn(COL_ICON).setPreferredWidth(44);
        table.getColumnModel().getColumn(COL_ICON).setMaxWidth(48);
        table.getColumnModel().getColumn(COL_NAME).setPreferredWidth(200);
        table.getColumnModel().getColumn(COL_TYPE).setPreferredWidth(70);
        table.getColumnModel().getColumn(COL_GENDER).setPreferredWidth(70);
        table.getColumnModel().getColumn(COL_DESC).setPreferredWidth(200);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COL_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 36));

        // Zebra + icon renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                setBorder(new EmptyBorder(0, 5, 0, 5));
                if (c == COL_TYPE && v instanceof Integer) {
                    setText(formatType((Integer) v));
                } else if (c == COL_GENDER && v instanceof Integer) {
                    setText(formatGender((Integer) v));
                }
                setHorizontalAlignment(c == COL_ID || c == COL_ICON ? CENTER : LEFT);
                return this;
            }
        });
        table.getColumnModel().getColumn(COL_ICON).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel();
                lbl.setHorizontalAlignment(JLabel.CENTER);
                if (v instanceof Integer) {
                    lbl.setIcon(ItemDataPanel.this.getIcon((Integer) v, 24));
                }
                lbl.setBackground(sel ? t.getSelectionBackground()
                                      : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 253)));
                lbl.setOpaque(true);
                return lbl;
            }
        });

        // Double-click → edit
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                if (e.getButton() == MouseEvent.BUTTON3) {
                    // Right-click → copy ID
                    table.setRowSelectionInterval(row, row);
                    int modelRow = table.convertRowIndexToModel(row);
                    int itemId = (int) model.getValueAt(modelRow, COL_ID);
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem miCopy = new JMenuItem("Copy ID: " + itemId);
                    miCopy.addActionListener(ev -> Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(String.valueOf(itemId)), null));
                    JMenuItem miEdit = new JMenuItem("Sửa item này...");
                    miEdit.addActionListener(ev -> openItemDialog(modelRow));
                    popup.add(miCopy);
                    popup.addSeparator();
                    popup.add(miEdit);
                    popup.show(table, e.getX(), e.getY());
                } else if (e.getClickCount() == 2) {
                    openItemDialog(table.convertRowIndexToModel(row));
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // -----------------------------------------------------------------------
    private void loadData() {
        List<ItemTemplate> templates = Manager.ITEM_TEMPLATES;
        if (templates == null || templates.isEmpty()) {
            lblCount.setText("Server chưa khởi động — nhấn ↺");
            return;
        }
        new Thread(() -> {
            List<Object[]> rows = new ArrayList<>();
            for (ItemTemplate item : templates) {
                if (item == null) continue;
                int g = item.gender & 0xFF;
                rows.add(new Object[]{
                    (int) item.id,
                    (int) item.iconID,
                    item.name != null ? item.name : "",
                    (int) item.type,
                    g,
                    (int) item.level,
                    item.description != null ? item.description : ""
                });
            }
            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                for (Object[] r : rows) model.addRow(r);
                lblCount.setText("  " + rows.size() + " vật phẩm");
            });
        }).start();
    }

    // -----------------------------------------------------------------------
    private void filter() {
        String kw      = txtSearch.getText().trim();
        String typeStr = (String) cmbType.getSelectedItem();
        String genderStr = (String) cmbGender.getSelectedItem();
        boolean allTypes = typeStr == null || typeStr.startsWith("-");
        boolean allGenders = genderStr == null || genderStr.startsWith("-");
        Integer typeFilter = null;
        Integer genderFilter = null;
        if (!allTypes) {
            try { typeFilter = Integer.parseInt(typeStr.split(" - ")[0]); }
            catch (NumberFormatException ignored) {}
        }
        if (!allGenders) {
            try { genderFilter = Integer.parseInt(genderStr.split(" - ")[0]); }
            catch (NumberFormatException ignored) {}
        }
        final Integer tf = typeFilter;
        final Integer gf = genderFilter;

        if (kw.isEmpty() && allTypes && allGenders) { sorter.setRowFilter(null); return; }

        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        if (!kw.isEmpty()) {
            try {
                RowFilter<DefaultTableModel, Integer> idFilter =
                        RowFilter.regexFilter("(?i)" + Pattern.quote(kw), COL_ID);
                RowFilter<DefaultTableModel, Integer> nameFilter =
                        RowFilter.regexFilter("(?i)" + Pattern.quote(kw), COL_NAME);
                filters.add(RowFilter.orFilter(Arrays.asList(idFilter, nameFilter)));
            }
            catch (PatternSyntaxException ignored) {}
        }
        if (tf != null) {
            final int typeVal = tf;
            filters.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                    Object v = e.getValue(COL_TYPE);
                    return v instanceof Integer && (int) v == typeVal;
                }
            });
        }
        if (gf != null) {
            final int genderVal = gf;
            filters.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                    Object v = e.getValue(COL_GENDER);
                    return v instanceof Integer && (int) v == genderVal;
                }
            });
        }
        sorter.setRowFilter(filters.size() == 1 ? filters.get(0) : RowFilter.andFilter(filters));
    }

    private String formatType(int type) {
        String name = KNOWN_ITEM_TYPE_NAMES.get(type);
        return name != null ? (type + " - " + name) : String.valueOf(type);
    }

    // Built from every distinct type actually present in the loaded item templates,
    // instead of a short hard-coded list, so the filter always covers every type in the DB.
    private String[] buildItemTypeFilterOptions() {
        TreeSet<Integer> types = new TreeSet<>();
        List<ItemTemplate> templates = Manager.ITEM_TEMPLATES;
        if (templates != null) {
            for (ItemTemplate item : templates) {
                if (item != null) types.add((int) item.type);
            }
        }
        List<String> options = new ArrayList<>();
        options.add("- Tất cả Loại -");
        for (int type : types) options.add(formatType(type));
        return options.toArray(new String[0]);
    }

    private String formatGender(int gender) {
        for (String item : GENDER_FILTERS) {
            if (item.startsWith(gender + " - ")) {
                return item;
            }
        }
        return String.valueOf(gender);
    }

    // -----------------------------------------------------------------------
    private ImageIcon getIcon(int iconId, int size) {
        if (iconId <= 0) return null;
        if (iconCache.containsKey(iconId)) return iconCache.get(iconId);
        if (noIcon.containsKey(iconId))    return null;
        try {
            for (String zoom : new String[]{"x4", "x3", "x2", "x1"}) {
                File f = new File(ICON_FOLDER + zoom + "/" + iconId + ".png");
                if (f.exists()) {
                    BufferedImage img = ImageIO.read(f);
                    Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(scaled);
                    iconCache.put(iconId, icon);
                    return icon;
                }
            }
        } catch (Exception ignored) {}
        noIcon.put(iconId, true);
        return null;
    }

    private ImageIcon getPartIcon(int partId, int size) {
        if (partFrameMap.isEmpty()) {
            loadPartIconMap();
        }
        Integer iconId = partIconMap.get(partId);
        return iconId != null ? getIcon(iconId, size) : null;
    }

    private void loadPartIconMap() {
        partIconMap.clear();
        partFrameMap.clear();
        try (Connection conn = DBConnecter.getConnectionServer();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, data FROM part")) {
            while (rs.next()) {
                try {
                    int partId = rs.getInt("id");
                    JsonArray arr = new JsonParser().parse(rs.getString("data")).getAsJsonArray();
                    List<PartFrame> frames = new ArrayList<>();
                    for (int i = 0; i < arr.size(); i++) {
                        if (arr.get(i).isJsonArray()) {
                            JsonArray frame = arr.get(i).getAsJsonArray();
                            frames.add(new PartFrame(frame.get(0).getAsInt(), frame.get(1).getAsInt(), frame.get(2).getAsInt()));
                        } else if (arr.get(i).isJsonObject()) {
                            com.google.gson.JsonObject frame = arr.get(i).getAsJsonObject();
                            frames.add(new PartFrame(frame.get("id").getAsInt(), frame.get("dx").getAsInt(), frame.get("dy").getAsInt()));
                        }
                    }
                    if (arr.size() > 0) {
                        partIconMap.put(partId, frames.get(0).iconId);
                        partFrameMap.put(partId, frames);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private ImageIcon renderCostumePreview(int headPart, int bodyPart, int legPart, int frameIndex, int width, int height) {
        if (partFrameMap.isEmpty()) {
            loadPartIconMap();
        }
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int baseX = width / 2;
        int baseY = height / 2 + 30;
        drawPartFrame(g, legPart, frameIndex, baseX, baseY);
        drawPartFrame(g, bodyPart, frameIndex, baseX, baseY);
        drawPartFrame(g, headPart, frameIndex, baseX, baseY);
        g.dispose();
        return new ImageIcon(canvas);
    }

    private void drawPartFrame(Graphics2D g, int partId, int frameIndex, int baseX, int baseY) {
        List<PartFrame> frames = partFrameMap.get(partId);
        if (frames == null || frames.isEmpty()) {
            return;
        }
        PartFrame frame = frames.get(Math.floorMod(frameIndex, frames.size()));
        ImageIcon icon = getIcon(frame.iconId, 48);
        if (icon == null) {
            return;
        }
        Image image = icon.getImage();
        int x = baseX + frame.dx - icon.getIconWidth() / 2;
        int y = baseY + frame.dy - icon.getIconHeight() / 2;
        g.drawImage(image, x, y, null);
    }

    // -----------------------------------------------------------------------
    private void openItemDialog(Integer modelRow) {
        boolean isEdit = (modelRow != null);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Sửa vật phẩm" : "Thêm vật phẩm mới", true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setSize(680, 640);
        dlg.setMinimumSize(new Dimension(620, 560));
        dlg.setLocationRelativeTo(this);
        Point location = dlg.getLocation();
        dlg.setLocation(Math.max(0, location.x - 120), location.y);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(15, 15, 10, 15));

        // Fields
        JTextField fId    = field(""); fId.setEnabled(!isEdit);
        JTextField fName  = field("");
        JTextField fDesc  = field("");
        JTextField fType  = field("0");
        JTextField fGender= field("0");
        JTextField fLevel = field("0");
        JTextField fIconId= field("0");
        JTextField fPart  = field("0");
        JTextField fPower = field("0");
        JTextField fGold  = field("0");
        JTextField fGem   = field("0");
        JTextField fHead  = field("0");
        JTextField fBody  = field("0");
        JTextField fLeg   = field("0");
        JCheckBox  cbUpUp = new JCheckBox("is_up_to_up");
        cbUpUp.setOpaque(false);
        JLabel lblIconPreview = previewLabel();
        JLabel lblPartPreview = previewLabel();

        if (isEdit) {
            // Prefill from Manager.ITEM_TEMPLATES
            ItemTemplate src = Manager.ITEM_TEMPLATES.stream()
                .filter(t -> t != null && (int) t.id == (int) model.getValueAt(modelRow, COL_ID))
                .findFirst().orElse(null);
            if (src != null) {
                fId.setText(String.valueOf(src.id));
                fName.setText(src.name != null ? src.name : "");
                fDesc.setText(src.description != null ? src.description : "");
                fType.setText(String.valueOf(src.type));
                fGender.setText(String.valueOf(src.gender));
                fLevel.setText(String.valueOf(src.level));
                fIconId.setText(String.valueOf(src.iconID));
                fPart.setText(String.valueOf(src.part));
                fPower.setText(String.valueOf(src.strRequire));
                fGold.setText(String.valueOf(src.gold));
                fGem.setText(String.valueOf(src.gem));
                fHead.setText(String.valueOf(src.head));
                fBody.setText(String.valueOf(src.body));
                fLeg.setText(String.valueOf(src.leg));
                cbUpUp.setSelected(src.isUpToUp);
            }
        }

        String[][] rows = {
            {"ID",       null}, {"Tên",     null}, {"Mô tả",   null},
            {"Loại (type)", null}, {"Hành tinh (0=Đất,1=Namếc,2=Xayda)", null}, {"Level",   null},
            {"Icon ID",  null}, {"Part",    null}, {"Power req", null},
            {"Vàng",     null}, {"Đá quý",  null},
            {"Head",     null}, {"Body",    null}, {"Leg",     null}
        };
        JTextField[] fields = {fId, fName, fDesc, fType, fGender, fLevel,
                               fIconId, fPart, fPower, fGold, fGem, fHead, fBody, fLeg};

        JButton btnFindIcon = new JButton("Tìm");
        btnFindIcon.addActionListener(e -> showIconSearchDialog(dlg, iconId -> {
            fIconId.setText(String.valueOf(iconId));
            updateIconPreview(fIconId, lblIconPreview);
        }));

        JButton btnFindPart = new JButton("Tìm");
        btnFindPart.addActionListener(e -> showPartSearchDialog(dlg, partId -> {
            fPart.setText(String.valueOf(partId));
            updatePartPreview(fPart, lblPartPreview);
        }));

        for (int i = 0; i < rows.length; i++) {
            GridBagConstraints gl = new GridBagConstraints();
            gl.gridx = 0; gl.gridy = i; gl.anchor = GridBagConstraints.WEST;
            gl.insets = new Insets(4, 4, 4, 10); gl.weightx = 0;
            GridBagConstraints gf = new GridBagConstraints();
            gf.gridx = 1; gf.gridy = i; gf.fill = GridBagConstraints.HORIZONTAL;
            gf.insets = new Insets(4, 0, 4, 4); gf.weightx = 1.0;
            JLabel lbl = new JLabel(rows[i][0] + ":");
            lbl.setFont(FONT_BOLD);
            form.add(lbl, gl);

            Component fieldComponent = fields[i];
            if (fields[i] == fIconId) {
                fieldComponent = pickerRow(fields[i], btnFindIcon, lblIconPreview);
            } else if (fields[i] == fPart) {
                fieldComponent = pickerRow(fields[i], btnFindPart, lblPartPreview);
            }
            form.add(fieldComponent, gf);
        }

        bindPreview(fIconId, () -> updateIconPreview(fIconId, lblIconPreview));
        bindPreview(fPart, () -> updatePartPreview(fPart, lblPartPreview));
        updateIconPreview(fIconId, lblIconPreview);
        updatePartPreview(fPart, lblPartPreview);
        // is_up_to_up checkbox
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 1; gc.gridy = rows.length;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 0, 4, 4);
        form.add(cbUpUp, gc);

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(null);
        scrollForm.getVerticalScrollBar().setUnitIncrement(16);
        dlg.add(scrollForm, BorderLayout.CENTER);

        // Buttons
        JButton btnSave   = ServerGuiUtils.createStyledButton(isEdit ? "Lưu thay đổi" : "Thêm vào game", COL_PRIMARY, Color.WHITE);
        JButton btnCancel = ServerGuiUtils.createStyledButton("Hủy", new Color(120, 120, 120), Color.WHITE);
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            try {
                short id      = Short.parseShort(fId.getText().trim());
                String name   = fName.getText().trim();
                String desc   = fDesc.getText().trim();
                byte type     = Byte.parseByte(fType.getText().trim());
                byte gender   = Byte.parseByte(fGender.getText().trim());
                byte level    = Byte.parseByte(fLevel.getText().trim());
                short iconId  = Short.parseShort(fIconId.getText().trim());
                short part    = Short.parseShort(fPart.getText().trim());
                int power     = Integer.parseInt(fPower.getText().trim());
                int gold      = Integer.parseInt(fGold.getText().trim());
                int gem       = Integer.parseInt(fGem.getText().trim());
                int head      = Integer.parseInt(fHead.getText().trim());
                int body      = Integer.parseInt(fBody.getText().trim());
                int leg       = Integer.parseInt(fLeg.getText().trim());
                boolean upup  = cbUpUp.isSelected();

                if (name.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Tên không được trống."); return; }

                saveItem(isEdit, id, name, desc, type, gender, level, iconId, part, power,
                         gold, gem, head, body, leg, upup, dlg);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Giá trị không hợp lệ: " + ex.getMessage());
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void saveItem(boolean isEdit, short id, String name, String desc,
                          byte type, byte gender, byte level, short iconId, short part,
                          int power, int gold, int gem, int head, int body, int leg,
                          boolean upup, JDialog dlg) {
        new Thread(() -> {
            try (Connection con = DBConnecter.getConnectionServer()) {
                if (isEdit) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE item_template SET name=?,description=?,type=?,gender=?,level=?," +
                            "icon_id=?,part=?,is_up_to_up=?,power_require=?,gold=?,gem=?," +
                            "head=?,body=?,leg=? WHERE id=?")) {
                        ps.setString(1, name); ps.setString(2, desc);
                        ps.setByte(3, type);   ps.setByte(4, gender);  ps.setByte(5, level);
                        ps.setShort(6, iconId); ps.setShort(7, part);
                        ps.setBoolean(8, upup); ps.setInt(9, power);
                        ps.setInt(10, gold);    ps.setInt(11, gem);
                        ps.setInt(12, head);    ps.setInt(13, body);   ps.setInt(14, leg);
                        ps.setShort(15, id);
                        ps.executeUpdate();
                    }
                    // Update in memory
                    Manager.ITEM_TEMPLATES.stream()
                        .filter(t -> t != null && t.id == id).findFirst().ifPresent(t -> {
                            t.name = name; t.description = desc; t.type = type;
                            t.gender = gender; t.level = level; t.iconID = iconId;
                            t.part = part; t.isUpToUp = upup; t.strRequire = power;
                            t.gold = gold; t.gem = gem; t.head = head; t.body = body; t.leg = leg;
                        });
                } else {
                    // Check ID duplicate
                    boolean exists = Manager.ITEM_TEMPLATES.stream()
                        .anyMatch(t -> t != null && t.id == id);
                    if (exists) {
                        SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(dlg, "ID " + id + " đã tồn tại."));
                        return;
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO item_template (id,name,description,type,gender,level," +
                            "icon_id,part,is_up_to_up,power_require,gold,gem,head,body,leg) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                        ps.setShort(1, id);     ps.setString(2, name);  ps.setString(3, desc);
                        ps.setByte(4, type);    ps.setByte(5, gender);  ps.setByte(6, level);
                        ps.setShort(7, iconId); ps.setShort(8, part);
                        ps.setBoolean(9, upup); ps.setInt(10, power);
                        ps.setInt(11, gold);    ps.setInt(12, gem);
                        ps.setInt(13, head);    ps.setInt(14, body);    ps.setInt(15, leg);
                        ps.executeUpdate();
                    }
                    // Add to memory immediately
                    ItemTemplate newItem = new ItemTemplate();
                    newItem.id = id; newItem.name = name; newItem.description = desc;
                    newItem.type = type; newItem.gender = gender; newItem.level = level;
                    newItem.iconID = iconId; newItem.part = part; newItem.isUpToUp = upup;
                    newItem.strRequire = power; newItem.gold = gold; newItem.gem = gem;
                    newItem.head = head; newItem.body = body; newItem.leg = leg;
                    Manager.ITEM_TEMPLATES.add(newItem);
                }

                iconCache.remove((int) iconId);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dlg,
                        isEdit ? "Đã cập nhật item " + id + " thành công!"
                               : "Đã thêm item " + id + " vào game!\nPlayer mới kết nối sẽ thấy ngay.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    model.setRowCount(0);
                    loadData();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private JLabel previewLabel() {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(34, 34));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        return label;
    }

    private JPanel flow(Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }

    private JPanel pickerRow(JTextField field, JButton button, JLabel preview) {
        field.setPreferredSize(new Dimension(120, 32));
        button.setPreferredSize(new Dimension(64, 32));
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        panel.add(field);
        panel.add(button);
        panel.add(preview);
        return panel;
    }

    private void bindPreview(JTextField field, Runnable update) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { update.run(); }
            @Override public void removeUpdate(DocumentEvent e) { update.run(); }
            @Override public void changedUpdate(DocumentEvent e) { update.run(); }
        });
    }

    private void updateIconPreview(JTextField field, JLabel preview) {
        try {
            preview.setIcon(getIcon(Integer.parseInt(field.getText().trim()), 30));
        } catch (Exception ignored) {
            preview.setIcon(null);
        }
    }

    private void updatePartPreview(JTextField field, JLabel preview) {
        try {
            preview.setIcon(getPartIcon(Integer.parseInt(field.getText().trim()), 30));
        } catch (Exception ignored) {
            preview.setIcon(null);
        }
    }

    private void updateCostumePreview(JTextField headField, JTextField bodyField, JTextField legField,
            JSpinner frameSpinner, JLabel preview) {
        try {
            int head = Integer.parseInt(headField.getText().trim());
            int body = Integer.parseInt(bodyField.getText().trim());
            int leg = Integer.parseInt(legField.getText().trim());
            int frame = (int) frameSpinner.getValue();
            preview.setIcon(renderCostumePreview(head, body, leg, frame, 150, 170));
        } catch (Exception ignored) {
            preview.setIcon(null);
        }
    }

    private void showCostumeBuilderDialog(JDialog parent, JTextField fType, JTextField fGender,
            JTextField fIconId, JTextField fPart, JTextField fHead, JTextField fBody, JTextField fLeg,
            JLabel mainIconPreview, JLabel mainPartPreview) {
        JDialog dialog = new JDialog(parent, "Costume Builder", true);
        dialog.setSize(620, 430);
        dialog.setMinimumSize(new Dimension(560, 380));
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(14, 14, 8, 14));

        JTextField bIcon = field(fIconId.getText().trim().isEmpty() ? "0" : fIconId.getText().trim());
        JTextField bHead = field(fHead.getText().trim().isEmpty() ? "0" : fHead.getText().trim());
        JTextField bBody = field(fBody.getText().trim().isEmpty() ? "0" : fBody.getText().trim());
        JTextField bLeg = field(fLeg.getText().trim().isEmpty() ? "0" : fLeg.getText().trim());
        JTextField bGender = field(fGender.getText().trim().isEmpty() ? "3" : fGender.getText().trim());
        JSpinner spnFrame = new JSpinner(new SpinnerNumberModel(0, 0, 15, 1));

        JLabel pIcon = previewLabel();
        JLabel pHead = previewLabel();
        JLabel pBody = previewLabel();
        JLabel pLeg = previewLabel();
        JLabel pCostume = new JLabel();
        pCostume.setPreferredSize(new Dimension(150, 170));
        pCostume.setHorizontalAlignment(JLabel.CENTER);
        pCostume.setVerticalAlignment(JLabel.CENTER);
        pCostume.setBorder(BorderFactory.createTitledBorder("Preview pose"));

        JButton findIcon = new JButton("Tìm");
        JButton findHead = new JButton("Tìm");
        JButton findBody = new JButton("Tìm");
        JButton findLeg = new JButton("Tìm");

        findIcon.addActionListener(e -> showIconSearchDialog(dialog, id -> {
            bIcon.setText(String.valueOf(id));
            updateIconPreview(bIcon, pIcon);
        }));
        findHead.addActionListener(e -> showPartSearchDialog(dialog, id -> {
            bHead.setText(String.valueOf(id));
            updatePartPreview(bHead, pHead);
        }));
        findBody.addActionListener(e -> showPartSearchDialog(dialog, id -> {
            bBody.setText(String.valueOf(id));
            updatePartPreview(bBody, pBody);
        }));
        findLeg.addActionListener(e -> showPartSearchDialog(dialog, id -> {
            bLeg.setText(String.valueOf(id));
            updatePartPreview(bLeg, pLeg);
        }));

        addBuilderRow(form, 0, "Icon item:", pickerRow(bIcon, findIcon, pIcon));
        addBuilderRow(form, 1, "Head part:", pickerRow(bHead, findHead, pHead));
        addBuilderRow(form, 2, "Body part:", pickerRow(bBody, findBody, pBody));
        addBuilderRow(form, 3, "Leg part:", pickerRow(bLeg, findLeg, pLeg));
        addBuilderRow(form, 4, "Gender:", bGender);
        addBuilderRow(form, 5, "Frame:", spnFrame);

        JLabel hint = new JLabel("Áp dụng sẽ set type=5, icon_id, part=head, head/body/leg và gender.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(new Color(90, 90, 90));
        GridBagConstraints hintGbc = new GridBagConstraints();
        hintGbc.gridx = 0;
        hintGbc.gridy = 6;
        hintGbc.gridwidth = 2;
        hintGbc.fill = GridBagConstraints.HORIZONTAL;
        hintGbc.insets = new Insets(10, 4, 4, 4);
        form.add(hint, hintGbc);

        Runnable updateCostume = () -> updateCostumePreview(bHead, bBody, bLeg, spnFrame, pCostume);
        bindPreview(bIcon, () -> updateIconPreview(bIcon, pIcon));
        bindPreview(bHead, () -> {
            updatePartPreview(bHead, pHead);
            updateCostume.run();
        });
        bindPreview(bBody, () -> {
            updatePartPreview(bBody, pBody);
            updateCostume.run();
        });
        bindPreview(bLeg, () -> {
            updatePartPreview(bLeg, pLeg);
            updateCostume.run();
        });
        spnFrame.addChangeListener(e -> updateCostume.run());
        updateIconPreview(bIcon, pIcon);
        updatePartPreview(bHead, pHead);
        updatePartPreview(bBody, pBody);
        updatePartPreview(bLeg, pLeg);
        updateCostume.run();

        JButton apply = ServerGuiUtils.createStyledButton("Áp dụng vào item", COL_PRIMARY, Color.WHITE);
        JButton workshop = ServerGuiUtils.createStyledButton("Workshop từ thư mục", new Color(23, 162, 184), Color.WHITE);
        JButton cancel = ServerGuiUtils.createStyledButton("Đóng", new Color(120, 120, 120), Color.WHITE);
        cancel.addActionListener(e -> dialog.dispose());
        workshop.addActionListener(e -> showCostumeWorkshopDialog(dialog));
        apply.addActionListener(e -> {
            try {
                int icon = Integer.parseInt(bIcon.getText().trim());
                int head = Integer.parseInt(bHead.getText().trim());
                int body = Integer.parseInt(bBody.getText().trim());
                int leg = Integer.parseInt(bLeg.getText().trim());
                int gender = Integer.parseInt(bGender.getText().trim());

                fType.setText("5");
                fGender.setText(String.valueOf(gender));
                fIconId.setText(String.valueOf(icon));
                fPart.setText(String.valueOf(head));
                fHead.setText(String.valueOf(head));
                fBody.setText(String.valueOf(body));
                fLeg.setText(String.valueOf(leg));
                updateIconPreview(fIconId, mainIconPreview);
                updatePartPreview(fPart, mainPartPreview);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "ID không hợp lệ: " + ex.getMessage());
            }
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonRow.setBackground(Color.WHITE);
        buttonRow.add(workshop);
        buttonRow.add(cancel);
        buttonRow.add(apply);

        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setBackground(Color.WHITE);
        content.add(form, BorderLayout.CENTER);
        content.add(pCostume, BorderLayout.EAST);
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttonRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addBuilderRow(JPanel form, int row, String labelText, Component field) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(5, 4, 5, 12);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1.0;
        fieldGbc.insets = new Insets(5, 0, 5, 4);

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_BOLD);
        form.add(label, labelGbc);
        form.add(field, fieldGbc);
    }

    private void showCostumeWorkshopDialog(Component parent) {
        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        showCostumeWorkshopDialog(owner instanceof JDialog ? (JDialog) owner : null);
    }

    private void showCostumeWorkshopDialog(JDialog parent) {
        JDialog dialog = new JDialog(parent, "Costume Workshop - ghép res ngoài", true);
        dialog.setSize(1280, 860);
        dialog.setMinimumSize(new Dimension(1180, 780));
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(10, 10));

        File[] folderRef = new File[1];
        int[] duplicatedPartIds = new int[]{-1, -1, -1}; // head, body, leg
        Map<String, ImageIcon> externalIconCache = new HashMap<>();

        DefaultTableModel frameModel = new DefaultTableModel(new String[]{"Part", "Part Frame", "Preview", "File PNG", "dx", "dy", "Layer", "CF"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column >= 3; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 || columnIndex == 4 || columnIndex == 5 || columnIndex == 6 || columnIndex == 7 ? Integer.class : String.class;
            }
        };
        addWorkshopFrameRows(frameModel, 0);

        JTable frameTable = new JTable(frameModel);
        frameTable.setRowHeight(42);
        frameTable.setFont(FONT_PLAIN);
        TableRowSorter<DefaultTableModel> frameSorter = new TableRowSorter<>(frameModel);
        frameTable.setRowSorter(frameSorter);
        filterWorkshopRowsForCf(frameSorter, 0);
        frameTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        frameTable.getColumnModel().getColumn(1).setPreferredWidth(55);
        frameTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        frameTable.getColumnModel().getColumn(3).setPreferredWidth(320);
        frameTable.getColumnModel().getColumn(4).setPreferredWidth(55);
        frameTable.getColumnModel().getColumn(5).setPreferredWidth(55);
        frameTable.getColumnModel().getColumn(6).setPreferredWidth(60);
        frameTable.getColumnModel().getColumn(7).setPreferredWidth(55);
        frameTable.getColumnModel().getColumn(2).setCellRenderer(createWorkshopPreviewCellRenderer(folderRef));
        frameTable.getColumnModel().getColumn(3).setCellRenderer(createWorkshopFileCellRenderer(folderRef));

        WorkshopPreviewPanel preview = new WorkshopPreviewPanel();
        preview.setPreferredSize(new Dimension(520, 470));
        preview.setBorder(BorderFactory.createTitledBorder("Preview frame"));
        WorkshopPreviewPanel partFramePreview = new WorkshopPreviewPanel();
        partFramePreview.setFitImage(true);
        partFramePreview.setPreferredSize(new Dimension(180, 140));
        partFramePreview.setBorder(BorderFactory.createTitledBorder("Part frame PNG"));
        JLabel lblPartFramePreview = new JLabel("Chon row de xem PNG");
        lblPartFramePreview.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JSpinner spnFrame = new JSpinner(new SpinnerNumberModel(0, 0, CLIENT_CHAR_INFO.length - 1, 1));
        JLabel lblCfInfo = new JLabel(clientCfLabel(0));
        lblCfInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JComboBox<String> cmbActivePart = new JComboBox<>(new String[]{"head", "body", "leg"});
        JLabel lblActivePartFrame = new JLabel("Part Frame: 0");
        lblActivePartFrame.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lblImageStatus = new JLabel("PNG: -");
        lblImageStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JComboBox<String> cmbPreviewHead = new JComboBox<>(new String[]{"head"});
        JComboBox<String> cmbSourceZoom = new JComboBox<>(new String[]{"x4", "x3", "x2", "x1"});
        JSpinner spnHeadScale = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 5.0, 0.05));
        JSpinner spnBodyScale = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 5.0, 0.05));
        JSpinner spnLegScale = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 5.0, 0.05));
        JSpinner spnHeadX = new JSpinner(new SpinnerNumberModel(0, -300, 300, 1));
        JSpinner spnHeadY = new JSpinner(new SpinnerNumberModel(0, -300, 300, 1));
        JSpinner spnBodyX = new JSpinner(new SpinnerNumberModel(0, -300, 300, 1));
        JSpinner spnBodyY = new JSpinner(new SpinnerNumberModel(0, -300, 300, 1));
        JSpinner spnLegX = new JSpinner(new SpinnerNumberModel(0, -300, 300, 1));
        JSpinner spnLegY = new JSpinner(new SpinnerNumberModel(0, -300, 300, 1));
        Runnable refreshPreview = () -> {
            int cf = (int) spnFrame.getValue();
            String previewHead = String.valueOf(cmbPreviewHead.getSelectedItem());
            int previewWidth = Math.max(480, preview.getWidth());
            int previewHeight = Math.max(300, preview.getHeight());
            preview.setImage(renderWorkshopPreviewImage(frameModel, folderRef[0],
                    externalIconCache, cf, previewHead, previewWidth, previewHeight,
                    workshopSourceImageScale(String.valueOf(cmbSourceZoom.getSelectedItem())),
                    workshopCoordinateScale(String.valueOf(cmbSourceZoom.getSelectedItem())),
                    workshopTransform(spnHeadScale, spnHeadX, spnHeadY),
                    workshopTransform(spnBodyScale, spnBodyX, spnBodyY),
                    workshopTransform(spnLegScale, spnLegX, spnLegY)));
            lblImageStatus.setText(workshopImageStatus(frameModel, folderRef[0], cf, previewHead));
            partFramePreview.setImage(renderSelectedWorkshopPartFrameImage(frameTable, frameModel,
                    folderRef[0], externalIconCache, 160, 150));
            lblPartFramePreview.setText(selectedWorkshopPartFrameLabel(frameTable, frameModel));
            frameTable.repaint();
        };
        preview.setDragHandler((dx, dy) -> moveSelectedWorkshopPart(frameTable, frameModel, dx, dy));

        frameModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 3) {
                int row = e.getFirstRow();
                if (row >= 0 && row < frameModel.getRowCount()) {
                    String fileName = String.valueOf(frameModel.getValueAt(row, 3));
                    if (!fileName.equals(String.valueOf(frameModel.getValueAt(row, 2)))) {
                        frameModel.setValueAt(fileName, row, 2);
                    }
                }
            }
            refreshPreview.run();
        });
        frameTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectPreviewHeadForWorkshopRow(frameTable, frameModel, cmbPreviewHead, cmbActivePart);
                lblActivePartFrame.setText(activePartFrameLabel((int) spnFrame.getValue(), String.valueOf(cmbActivePart.getSelectedItem())));
                refreshPreview.run();
            }
        });
        spnFrame.addChangeListener(e -> {
            lblCfInfo.setText(clientCfLabel((int) spnFrame.getValue()));
            String activePart = String.valueOf(cmbActivePart.getSelectedItem());
            ensureWorkshopRowsForClientCf(frameModel, (int) spnFrame.getValue());
            filterWorkshopRowsForCf(frameSorter, (int) spnFrame.getValue());
            lblActivePartFrame.setText(activePartFrameLabel((int) spnFrame.getValue(), activePart));
            selectWorkshopRowForClientCf(frameTable, frameModel, (int) spnFrame.getValue(), activePart);
            refreshPreview.run();
        });
        for (JSpinner spinner : new JSpinner[]{spnHeadScale, spnBodyScale, spnLegScale,
            spnHeadX, spnHeadY, spnBodyX, spnBodyY, spnLegX, spnLegY}) {
            spinner.addChangeListener(e -> refreshPreview.run());
        }
        cmbActivePart.addActionListener(e -> {
            String activePart = String.valueOf(cmbActivePart.getSelectedItem());
            lblActivePartFrame.setText(activePartFrameLabel((int) spnFrame.getValue(), activePart));
            selectWorkshopRowForClientCf(frameTable, frameModel, (int) spnFrame.getValue(), activePart);
            refreshPreview.run();
        });
        cmbPreviewHead.addActionListener(e -> refreshPreview.run());
        cmbSourceZoom.setSelectedItem("x4");

        JButton btnFolder = ServerGuiUtils.createStyledButton("Chọn thư mục res", COL_PRIMARY, Color.WHITE);
        JLabel lblFolder = new JLabel("Chưa chọn thư mục");
        lblFolder.setFont(FONT_PLAIN);
        btnFolder.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                if (frameTable.isEditing()) {
                    frameTable.getCellEditor().stopCellEditing();
                }
                folderRef[0] = chooser.getSelectedFile();
                lblFolder.setText(folderRef[0].getAbsolutePath());
                cmbSourceZoom.setSelectedItem("x4");
                externalIconCache.clear();
                frameTable.getColumnModel().getColumn(3).setCellEditor(
                        createWorkshopFileCellEditor(folderRef[0]));
                frameTable.repaint();
                refreshPreview.run();
            }
        });

        JButton btnDeleteFrame = ServerGuiUtils.createStyledButton("Xóa frame", new Color(200, 50, 50), Color.WHITE);
        btnDeleteFrame.addActionListener(e -> {
            deleteSelectedWorkshopRows(frameTable, frameModel);
            refreshPreview.run();
        });
        JButton btnLayerDown = ServerGuiUtils.createStyledButton("Layer -", new Color(100, 100, 100), Color.WHITE);
        btnLayerDown.addActionListener(e -> adjustSelectedWorkshopLayer(frameTable, frameModel, -1));
        JButton btnLayerUp = ServerGuiUtils.createStyledButton("Layer +", new Color(100, 100, 100), Color.WHITE);
        btnLayerUp.addActionListener(e -> adjustSelectedWorkshopLayer(frameTable, frameModel, 1));
        JButton btnPickPng = ServerGuiUtils.createStyledButton("Chon PNG", COL_PRIMARY, Color.WHITE);
        btnPickPng.addActionListener(e -> {
            chooseWorkshopPngForSelectedRow(dialog, frameTable, frameModel, folderRef[0], externalIconCache, false);
            selectPreviewHeadForWorkshopRow(frameTable, frameModel, cmbPreviewHead, cmbActivePart);
            refreshPreview.run();
        });
        JButton btnAutoFillPng = ServerGuiUtils.createStyledButton("Fill tu PNG dau", COL_PRIMARY, Color.WHITE);
        btnAutoFillPng.addActionListener(e -> {
            chooseWorkshopPngForSelectedRow(dialog, frameTable, frameModel, folderRef[0], externalIconCache, true);
            selectPreviewHeadForWorkshopRow(frameTable, frameModel, cmbPreviewHead, cmbActivePart);
            refreshPreview.run();
        });
        JButton btnMoveGroupTop = ServerGuiUtils.createStyledButton("Dua len dau", new Color(100, 100, 100), Color.WHITE);
        btnMoveGroupTop.addActionListener(e -> {
            moveSelectedWorkshopGroupToTop(frameTable, frameModel, frameSorter);
            selectPreviewHeadForWorkshopRow(frameTable, frameModel, cmbPreviewHead, cmbActivePart);
            refreshPreview.run();
        });

        JButton btnExport = ServerGuiUtils.createStyledButton("Copy config", new Color(100, 100, 100), Color.WHITE);
        btnExport.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(exportWorkshopConfig(frameModel, folderRef[0])), null);
            JOptionPane.showMessageDialog(dialog, "Đã copy config ghép costume vào clipboard.");
        });
        JButton btnSaveConfig = ServerGuiUtils.createStyledButton("Lưu config", new Color(100, 100, 100), Color.WHITE);
        btnSaveConfig.addActionListener(e -> saveWorkshopConfig(dialog, frameModel, folderRef[0]));
        JButton btnLoadConfig = ServerGuiUtils.createStyledButton("Load config", new Color(100, 100, 100), Color.WHITE);
        btnLoadConfig.addActionListener(e -> {
            File loadedFolder = loadWorkshopConfig(dialog, frameModel);
            if (loadedFolder != null) {
                folderRef[0] = loadedFolder;
                lblFolder.setText(loadedFolder.getAbsolutePath());
                cmbSourceZoom.setSelectedItem("x4");
                externalIconCache.clear();
                frameTable.getColumnModel().getColumn(3).setCellEditor(
                        createWorkshopFileCellEditor(loadedFolder));
                frameTable.repaint();
                refreshWorkshopHeadSelector(cmbPreviewHead, frameModel);
                filterWorkshopRowsForCf(frameSorter, (int) spnFrame.getValue());
                refreshPreview.run();
            }
        });
        JButton btnDuplicateCostume = ServerGuiUtils.createStyledButton("Duplicate costume", new Color(102, 51, 153), Color.WHITE);
        btnDuplicateCostume.addActionListener(e -> showDuplicateCostumeDialog(dialog, frameModel, folderRef,
                lblFolder, frameTable, frameSorter, spnFrame, cmbSourceZoom, externalIconCache, refreshPreview, duplicatedPartIds, cmbPreviewHead));
        JButton btnExportZooms = ServerGuiUtils.createStyledButton("Export x1-x3", new Color(0, 120, 120), Color.WHITE);
        btnExportZooms.addActionListener(e -> exportWorkshopZoomImages(dialog, frameModel, folderRef[0]));
        JButton btnExportPart = ServerGuiUtils.createStyledButton("Export part.sql", new Color(0, 120, 120), Color.WHITE);
        btnExportPart.addActionListener(e -> exportWorkshopPartSql(dialog, frameModel, duplicatedPartIds));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBorder(new EmptyBorder(10, 10, 0, 10));
        top.setBackground(Color.WHITE);
        JPanel fileButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fileButtons.setOpaque(false);
        fileButtons.add(btnFolder);
        fileButtons.add(btnDuplicateCostume);
        fileButtons.add(btnExportZooms);
        fileButtons.add(btnExportPart);
        fileButtons.add(btnExport);
        fileButtons.add(btnSaveConfig);
        fileButtons.add(btnLoadConfig);
        JPanel editButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        editButtons.setOpaque(false);
        editButtons.add(new JLabel("CF:"));
        editButtons.add(spnFrame);
        editButtons.add(lblCfInfo);
        editButtons.add(new JLabel("Part:"));
        editButtons.add(cmbActivePart);
        editButtons.add(lblActivePartFrame);
        editButtons.add(lblImageStatus);
        editButtons.add(new JLabel("Preview head:"));
        editButtons.add(cmbPreviewHead);
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionButtons.setOpaque(false);
        actionButtons.add(btnDeleteFrame);
        actionButtons.add(btnPickPng);
        actionButtons.add(btnAutoFillPng);
        actionButtons.add(btnMoveGroupTop);
        actionButtons.add(btnLayerDown);
        actionButtons.add(btnLayerUp);
        JPanel editRows = new JPanel();
        editRows.setOpaque(false);
        editRows.setLayout(new BoxLayout(editRows, BoxLayout.Y_AXIS));
        editRows.add(editButtons);
        editRows.add(Box.createVerticalStrut(6));
        editRows.add(actionButtons);
        JPanel controls = new JPanel(new BorderLayout(0, 8));
        controls.setOpaque(false);
        controls.add(fileButtons, BorderLayout.NORTH);
        controls.add(editRows, BorderLayout.CENTER);
        JPanel folderRow = new JPanel(new BorderLayout(8, 0));
        folderRow.setOpaque(false);
        folderRow.setBorder(new EmptyBorder(8, 0, 0, 0));
        folderRow.add(lblFolder, BorderLayout.CENTER);
        top.add(controls, BorderLayout.NORTH);
        top.add(folderRow, BorderLayout.CENTER);

        JPanel transformPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        transformPanel.setBorder(BorderFactory.createTitledBorder("Scale / Move part"));
        transformPanel.setBackground(Color.WHITE);
        transformPanel.add(transformRow("Head", spnHeadScale, spnHeadX, spnHeadY));
        transformPanel.add(transformRow("Body", spnBodyScale, spnBodyX, spnBodyY));
        transformPanel.add(transformRow("Leg", spnLegScale, spnLegX, spnLegY));

        JTextArea note = new JTextArea(
                "Mỗi frame có 3 dòng head/body/leg. Chọn file PNG và chỉnh dx/dy đến khi khớp.\n"
                + "Bản này dùng để canh visual từ thư mục res ngoài trước khi map sang icon id/part data.");
        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setBackground(new Color(250, 250, 250));
        note.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(Color.WHITE);
        right.add(preview, BorderLayout.CENTER);
        JPanel rightBottom = new JPanel();
        rightBottom.setLayout(new BoxLayout(rightBottom, BoxLayout.Y_AXIS));
        rightBottom.setBackground(Color.WHITE);
        JPanel partPreviewPanel = new JPanel(new BorderLayout(0, 4));
        partPreviewPanel.setBackground(Color.WHITE);
        partPreviewPanel.add(partFramePreview, BorderLayout.CENTER);
        partPreviewPanel.add(lblPartFramePreview, BorderLayout.SOUTH);
        rightBottom.add(partPreviewPanel);
        rightBottom.add(Box.createVerticalStrut(12));
        rightBottom.add(transformPanel);
        rightBottom.add(Box.createVerticalStrut(8));
        rightBottom.add(note);
        right.add(rightBottom, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(frameTable), right);
        split.setResizeWeight(0.58);
        split.setDividerSize(6);
        split.setBorder(new EmptyBorder(10, 10, 10, 10));

        dialog.add(top, BorderLayout.NORTH);
        dialog.add(split, BorderLayout.CENTER);
        refreshPreview.run();
        dialog.setVisible(true);
    }

    private void addWorkshopFrameRows(DefaultTableModel model, int frame) {
        int cf = Math.floorMod(frame, CLIENT_CHAR_INFO.length);
        model.addRow(new Object[]{"head", CLIENT_CHAR_INFO[cf][PART_HEAD][CI_FRAME], "", "", 0, 0, 2, cf});
        model.addRow(new Object[]{"body", CLIENT_CHAR_INFO[cf][PART_BODY][CI_FRAME], "", "", 0, 0, 1, cf});
        model.addRow(new Object[]{"leg", CLIENT_CHAR_INFO[cf][PART_LEG][CI_FRAME], "", "", 0, 0, 0, cf});
    }

    private void ensureWorkshopRowsForClientCf(DefaultTableModel model, int frame) {
        int cf = Math.floorMod(frame, CLIENT_CHAR_INFO.length);
        ensureWorkshopRow(model, "head", CLIENT_CHAR_INFO[cf][PART_HEAD][CI_FRAME], 2, cf);
        ensureWorkshopRow(model, "body", CLIENT_CHAR_INFO[cf][PART_BODY][CI_FRAME], 1, cf);
        ensureWorkshopRow(model, "leg", CLIENT_CHAR_INFO[cf][PART_LEG][CI_FRAME], 0, cf);
    }

    private void ensureWorkshopRow(DefaultTableModel model, String part, int partFrame, int layer, int cf) {
        if (!hasWorkshopCfRow(model, part, cf)) {
            model.addRow(new Object[]{part, partFrame, "", "", 0, 0, layer, cf});
        }
    }

    private void showDuplicateCostumeDialog(JDialog parent, DefaultTableModel frameModel, File[] folderRef,
            JLabel lblFolder, JTable frameTable, TableRowSorter<DefaultTableModel> frameSorter, JSpinner spnFrame,
            JComboBox<String> cmbSourceZoom, Map<String, ImageIcon> imageCache, Runnable refreshPreview,
            int[] duplicatedPartIds, JComboBox<String> cmbPreviewHead) {
        JDialog dialog = new JDialog(parent, "Duplicate costume có sẵn", true);
        dialog.setSize(820, 560);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(8, 8));

        JTextField txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Tìm ID hoặc tên cải trang"));

        DefaultTableModel costumeModel = new DefaultTableModel(new String[]{"ID", "Icon", "Tên", "Head", "Body", "Leg"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Integer.class : Object.class;
            }
        };

        for (ItemTemplate item : Manager.ITEM_TEMPLATES) {
            if (item != null && item.type == 5) {
                costumeModel.addRow(new Object[]{(int) item.id, (int) item.iconID,
                    item.name != null ? item.name : "", item.head, item.body, item.leg});
            }
        }

        JTable table = new JTable(costumeModel);
        table.setRowHeight(34);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                label.setHorizontalAlignment(JLabel.CENTER);
                if (value instanceof Integer) {
                    label.setIcon(ItemDataPanel.this.getIcon((Integer) value, 28));
                }
                label.setOpaque(true);
                label.setBackground(isSelected ? t.getSelectionBackground() : t.getBackground());
                return label;
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(costumeModel);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                    return;
                }
                RowFilter<DefaultTableModel, Integer> idFilter =
                        RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0);
                RowFilter<DefaultTableModel, Integer> nameFilter =
                        RowFilter.regexFilter("(?i)" + Pattern.quote(text), 2);
                sorter.setRowFilter(RowFilter.orFilter(Arrays.asList(idFilter, nameFilter)));
            }
        });

        Runnable select = () -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int modelRow = table.convertRowIndexToModel(row);
            int head = parseCellInt(costumeModel.getValueAt(modelRow, 3));
            int body = parseCellInt(costumeModel.getValueAt(modelRow, 4));
            int leg = parseCellInt(costumeModel.getValueAt(modelRow, 5));
            duplicatedPartIds[0] = head;
            duplicatedPartIds[1] = body;
            duplicatedPartIds[2] = leg;
            File iconFolder = new File(ICON_FOLDER + "x4");
            folderRef[0] = iconFolder;
            lblFolder.setText(iconFolder.getAbsolutePath());
            cmbSourceZoom.setSelectedItem("x4");
            imageCache.clear();
            frameTable.getColumnModel().getColumn(3).setCellEditor(
                    createWorkshopFileCellEditor(iconFolder));
            loadCostumePartsIntoWorkshop(frameModel, head, body, leg);
            refreshWorkshopHeadSelector(cmbPreviewHead, frameModel);
            filterWorkshopRowsForCf(frameSorter, (int) spnFrame.getValue());
            refreshPreview.run();
            dialog.dispose();
        };

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) select.run();
            }
        });

        JButton btnSelect = ServerGuiUtils.createStyledButton("Duplicate", COL_PRIMARY, Color.WHITE);
        btnSelect.addActionListener(e -> select.run());
        dialog.add(txtSearch, BorderLayout.NORTH);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(btnSelect, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void loadCostumePartsIntoWorkshop(DefaultTableModel model, int head, int body, int leg) {
        if (partFrameMap.isEmpty()) {
            loadPartIconMap();
        }
        model.setRowCount(0);
        List<Integer> headIds = resolveHeadPartIds(head);
        for (int cf = 0; cf < CLIENT_CHAR_INFO.length; cf++) {
            addPartFrameForClientCf(model, "head", headIds.get(0), cf, 2);
            addPartFrameForClientCf(model, "body", body, cf, 1);
            addPartFrameForClientCf(model, "leg", leg, cf, 0);
        }
        for (int i = 1; i < headIds.size(); i++) {
            addHeadRowsForSelector(model, "head:" + headIds.get(i), headIds.get(i));
        }
        sortWorkshopRows(model);
    }

    private void sortWorkshopRows(DefaultTableModel model) {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object[] row = new Object[model.getColumnCount()];
            for (int col = 0; col < model.getColumnCount(); col++) {
                row[col] = model.getValueAt(i, col);
            }
            rows.add(row);
        }
        rows.sort((a, b) -> {
            int partCompare = Integer.compare(workshopPartSortOrder(String.valueOf(a[0])),
                    workshopPartSortOrder(String.valueOf(b[0])));
            if (partCompare != 0) return partCompare;
            int nameCompare = String.valueOf(a[0]).compareTo(String.valueOf(b[0]));
            if (nameCompare != 0) return nameCompare;
            return Integer.compare(parseCellInt(a[1]), parseCellInt(b[1]));
        });
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    private int workshopPartSortOrder(String part) {
        if (part != null && part.startsWith("head")) return 0;
        if ("body".equals(part)) return 1;
        if ("leg".equals(part)) return 2;
        return 3;
    }

    private void addPartFrameForClientCf(DefaultTableModel model, String partName, int partId, int cf, int layer) {
        List<PartFrame> frames = partFrameMap.get(partId);
        if (frames == null || frames.isEmpty()) {
            return;
        }
        int partFrame = clientPartFrameIndex(cf, partName);
        if (hasWorkshopCfRow(model, partName, cf)) {
            return;
        }
        if (partFrame < 0 || partFrame >= frames.size()) {
            return;
        }
        PartFrame frame = frames.get(partFrame);
        if (isWorkshopPlaceholderIcon(frame.iconId)) {
            return;
        }
        String fileName = frame.iconId + ".png";
        model.addRow(new Object[]{partName, partFrame, fileName, fileName, frame.dx, frame.dy, layer, cf});
    }

    private void addHeadRowsForSelector(DefaultTableModel model, String partName, int partId) {
        List<PartFrame> frames = partFrameMap.get(partId);
        if (frames == null || frames.isEmpty()) {
            return;
        }
        for (int i = 0; i < Math.min(3, frames.size()); i++) {
            PartFrame frame = frames.get(i);
            if (!isWorkshopPlaceholderIcon(frame.iconId)) {
                String fileName = frame.iconId + ".png";
                model.addRow(new Object[]{partName, i, fileName, fileName, frame.dx, frame.dy, 2, -1});
            }
        }
    }

    private boolean hasWorkshopFrameRow(DefaultTableModel model, String partName, int partFrame) {
        for (int row = 0; row < model.getRowCount(); row++) {
            if (partName.equals(String.valueOf(model.getValueAt(row, 0)))
                    && parseCellInt(model.getValueAt(row, 1)) == partFrame) {
                return true;
            }
        }
        return false;
    }

    private boolean hasWorkshopCfRow(DefaultTableModel model, String partName, int cf) {
        for (int row = 0; row < model.getRowCount(); row++) {
            if (partName.equals(String.valueOf(model.getValueAt(row, 0)))
                    && workshopRowCf(model, row) == cf) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> resolveHeadPartIds(int head) {
        for (models.Template.ArrHead2Frames arr : Manager.ARR_HEAD_2_FRAMES) {
            if (arr.frames.contains(head)) {
                return new ArrayList<>(arr.frames);
            }
        }
        return new ArrayList<>(Arrays.asList(head));
    }

    private void addPartFramesToWorkshop(DefaultTableModel model, String partName, int partId, int layer) {
        List<PartFrame> frames = partFrameMap.get(partId);
        if (frames == null || frames.isEmpty()) {
            return;
        }
        for (int i = 0; i < frames.size(); i++) {
            PartFrame frame = frames.get(i);
            if (isWorkshopPlaceholderIcon(frame.iconId)) {
                continue;
            }
            String fileName = frame.iconId + ".png";
            model.addRow(new Object[]{partName, i, fileName, fileName, frame.dx, frame.dy, layer, -1});
        }
    }

    private File chooseIconFolderForParts(int head, int body, int leg) {
        if (partFrameMap.isEmpty()) {
            loadPartIconMap();
        }
        for (String zoom : new String[]{"x2", "x1", "x3", "x4"}) {
            File folder = new File(ICON_FOLDER + zoom);
            if (hasAnyPartIcon(folder, head) || hasAnyPartIcon(folder, body) || hasAnyPartIcon(folder, leg)) {
                return folder;
            }
        }
        return new File(ICON_FOLDER + "x2");
    }

    private boolean hasAnyPartIcon(File folder, int partId) {
        List<PartFrame> frames = partFrameMap.get(partId);
        if (frames == null) {
            return false;
        }
        for (PartFrame frame : frames) {
            if (new File(folder, frame.iconId + ".png").exists()) {
                return true;
            }
        }
        return false;
    }

    private String[] listPngNames(File folder) {
        File[] files = folder != null ? folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png")) : null;
        if (files == null || files.length == 0) return new String[]{""};
        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        String[] names = new String[files.length + 1];
        names[0] = "";
        for (int i = 0; i < files.length; i++) names[i + 1] = files[i].getName();
        return names;
    }

    private TableCellEditor createWorkshopFileCellEditor(File folder) {
        JComboBox<String> combo = new JComboBox<>(listPngNames(folder));
        combo.setEditable(true);
        return new DefaultCellEditor(combo);
    }

    private DefaultTableCellRenderer createWorkshopFileCellRenderer(File[] folderRef) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String name = String.valueOf(value).trim();
                boolean missing = !name.isBlank() && resolveWorkshopImageFile(folderRef[0], name) == null;
                if (!isSelected) {
                    component.setForeground(missing ? new Color(180, 30, 30) : table.getForeground());
                    component.setBackground(missing ? new Color(255, 238, 238) : table.getBackground());
                }
                setToolTipText(missing ? "Khong tim thay PNG trong folder da chon hoac data/icon/x4" : null);
                return component;
            }
        };
    }

    private DefaultTableCellRenderer createWorkshopPreviewCellRenderer(File[] folderRef) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setOpaque(true);
                label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                String name = String.valueOf(value).trim();
                if (!name.isBlank()) {
                    ImageIcon icon = loadWorkshopImage(folderRef[0], new HashMap<>(), name);
                    if (icon != null) {
                        label.setIcon(scaleIcon(icon, 34));
                    }
                }
                return label;
            }
        };
    }

    private void syncWorkshopPngName(DefaultTableModel model, int row, String name) {
        model.setValueAt(name, row, 2);
        model.setValueAt(name, row, 3);
    }

    private ImageIcon scaleIcon(ImageIcon icon, int maxSize) {
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        if (width <= 0 || height <= 0) {
            return icon;
        }
        double scale = Math.min(maxSize / (double) width, maxSize / (double) height);
        scale = Math.min(1.0, Math.max(0.1, scale));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        return new ImageIcon(icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH));
    }

    private void deleteSelectedWorkshopRows(JTable table, DefaultTableModel model) {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        int[] modelRows = new int[rows.length];
        for (int i = 0; i < rows.length; i++) {
            modelRows[i] = table.convertRowIndexToModel(rows[i]);
        }
        Arrays.sort(modelRows);
        for (int i = modelRows.length - 1; i >= 0; i--) {
            model.removeRow(modelRows[i]);
        }
    }

    private JPanel transformRow(String label, JSpinner scale, JSpinner moveX, JSpinner moveY) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        panel.add(new JLabel(label));
        panel.add(new JLabel("Scale"));
        panel.add(scale);
        panel.add(new JLabel("X"));
        panel.add(moveX);
        panel.add(new JLabel("Y"));
        panel.add(moveY);
        return panel;
    }

    private WorkshopTransform workshopTransform(JSpinner scale, JSpinner moveX, JSpinner moveY) {
        return new WorkshopTransform(((Number) scale.getValue()).doubleValue(),
                ((Number) moveX.getValue()).intValue(),
                ((Number) moveY.getValue()).intValue());
    }

    private double workshopSourceImageScale(String zoom) {
        return switch (zoom) {
            case "x1" -> 1.0;
            case "x2" -> 1.0;
            case "x3" -> 1.0;
            case "x4" -> 1.0;
            default -> 1.0;
        };
    }

    private double workshopCoordinateScale(String zoom) {
        return switch (zoom) {
            case "x1" -> 1.0;
            case "x2" -> 2.0;
            case "x3" -> 3.0;
            case "x4" -> 4.0;
            default -> 4.0;
        };
    }

    private BufferedImage renderWorkshopPreviewImage(DefaultTableModel model, File folder, Map<String, ImageIcon> imageCache,
            int frame, String previewHead, int width, int height, double sourceImageScale, double coordinateScale, WorkshopTransform headTransform,
            WorkshopTransform bodyTransform, WorkshopTransform legTransform) {
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int baseX = width / 2;
        int baseY = height - 70;
        drawWorkshopFrameLayers(g, model, folder, imageCache, frame, previewHead, baseX, baseY,
                sourceImageScale, coordinateScale, headTransform, bodyTransform, legTransform);
        g.dispose();
        return canvas;
    }

    private BufferedImage renderSelectedWorkshopPartFrameImage(JTable table, DefaultTableModel model,
            File folder, Map<String, ImageIcon> imageCache, int width, int height) {
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(new Color(250, 250, 250));
        g.fillRect(0, 0, width, height);
        int row = table.getSelectedRow();
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            String fileName = String.valueOf(model.getValueAt(modelRow, 3));
            ImageIcon icon = loadWorkshopImage(folder, imageCache, fileName);
            if (icon != null) {
                double scale = Math.min((width - 24) / (double) icon.getIconWidth(),
                        (height - 30) / (double) icon.getIconHeight());
                scale = Math.max(0.1, scale);
                int drawWidth = Math.max(1, (int) Math.round(icon.getIconWidth() * scale));
                int drawHeight = Math.max(1, (int) Math.round(icon.getIconHeight() * scale));
                int drawX = (width - drawWidth) / 2;
                int drawY = (height - drawHeight) / 2;
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(icon.getImage(), drawX, drawY, drawWidth, drawHeight, null);
            }
        }
        g.dispose();
        return canvas;
    }

    private Rectangle workshopFrameBounds(DefaultTableModel model, File folder, Map<String, ImageIcon> imageCache,
            int frame, String previewHead, double sourceImageScale, double coordinateScale,
            WorkshopTransform headTransform, WorkshopTransform bodyTransform, WorkshopTransform legTransform) {
        Rectangle bounds = null;
        int baseX = 0;
        int baseY = 0;
        for (int row = 0; row < model.getRowCount(); row++) {
            String part = String.valueOf(model.getValueAt(row, 0));
            if (part.startsWith("head") && !part.equals(previewHead)) {
                continue;
            }
            if (!workshopRowMatchesFrame(model, row, frame)) {
                continue;
            }
            String fileName = String.valueOf(model.getValueAt(row, 3));
            ImageIcon icon = loadWorkshopImage(folder, imageCache, fileName);
            if (icon == null) {
                continue;
            }
            WorkshopTransform transform = workshopTransformForPart(part, headTransform, bodyTransform, legTransform);
            Rectangle rowBounds = workshopRowBounds(model, row, frame, baseX, baseY,
                    sourceImageScale, coordinateScale, transform, icon);
            bounds = bounds == null ? rowBounds : bounds.union(rowBounds);
        }
        return bounds;
    }

    private Rectangle workshopRowBounds(DefaultTableModel model, int row, int cf, int baseX, int baseY,
            double sourceImageScale, double coordinateScale, WorkshopTransform transform, ImageIcon icon) {
        String part = String.valueOf(model.getValueAt(row, 0));
        int[] offset = clientPartOffset(cf, part);
        int dx = parseCellInt(model.getValueAt(row, 4));
        int dy = parseCellInt(model.getValueAt(row, 5));
        double finalScale = sourceImageScale * transform.scale;
        int drawWidth = Math.max(1, (int) Math.round(icon.getIconWidth() * finalScale));
        int drawHeight = Math.max(1, (int) Math.round(icon.getIconHeight() * finalScale));
        int drawX = baseX + (int) Math.round((offset[0] + dx + transform.moveX) * coordinateScale);
        int drawY = baseY - (int) Math.round(offset[1] * coordinateScale)
                + (int) Math.round((dy + transform.moveY) * coordinateScale);
        return new Rectangle(drawX, drawY, drawWidth, drawHeight);
    }

    private String selectedWorkshopPartFrameLabel(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return "Chon row de xem PNG";
        }
        int modelRow = table.convertRowIndexToModel(row);
        String part = String.valueOf(model.getValueAt(modelRow, 0));
        int frame = parseCellInt(model.getValueAt(modelRow, 1));
        int cf = workshopRowCf(model, modelRow);
        String file = String.valueOf(model.getValueAt(modelRow, 3)).trim();
        return part + " | PF " + frame + " | CF " + cf + (file.isBlank() ? "" : " | " + file);
    }

    private void moveSelectedWorkshopPart(JTable table, DefaultTableModel model, int dx, int dy) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        model.setValueAt(parseCellInt(model.getValueAt(modelRow, 4)) + dx, modelRow, 4);
        model.setValueAt(parseCellInt(model.getValueAt(modelRow, 5)) + dy, modelRow, 5);
    }

    private void selectPreviewHeadForWorkshopRow(JTable table, DefaultTableModel model,
            JComboBox<String> cmbPreviewHead, JComboBox<String> cmbActivePart) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String part = String.valueOf(model.getValueAt(modelRow, 0));
        if (part.startsWith("head")) {
            cmbPreviewHead.setSelectedItem(part);
            if ("head".equals(part)) {
                cmbActivePart.setSelectedItem("head");
            }
        } else if ("body".equals(part) || "leg".equals(part)) {
            cmbActivePart.setSelectedItem(part);
        }
    }

    private void selectWorkshopRowForClientCf(JTable table, DefaultTableModel model, int cf,
            String activePart) {
        String part = activePart;
        if (part == null || part.isBlank()) {
            return;
        }
        int targetPartFrame = clientPartFrameIndex(cf, part);
        for (int modelRow = 0; modelRow < model.getRowCount(); modelRow++) {
            if (part.equals(String.valueOf(model.getValueAt(modelRow, 0)))
                    && workshopRowCf(model, modelRow) == Math.floorMod(cf, CLIENT_CHAR_INFO.length)) {
                int viewRow = table.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    table.setRowSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                }
                return;
            }
        }
    }

    private String activePartFrameLabel(int cf, String part) {
        return "Part Frame: " + clientPartFrameIndex(cf, part);
    }

    private int workshopRowCf(DefaultTableModel model, int row) {
        if (model.getColumnCount() <= 6) {
            return -1;
        }
        return parseCellInt(model.getValueAt(row, 7));
    }

    private boolean workshopRowMatchesFrame(DefaultTableModel model, int row, int frame) {
        int cf = Math.floorMod(frame, CLIENT_CHAR_INFO.length);
        int rowCf = workshopRowCf(model, row);
        if (rowCf >= 0) {
            return rowCf == cf;
        }
        String part = String.valueOf(model.getValueAt(row, 0));
        return part.startsWith("head:")
                && parseCellInt(model.getValueAt(row, 1)) == clientPartFrameIndex(frame, part);
    }

    private void filterWorkshopRowsForCf(TableRowSorter<DefaultTableModel> sorter, int frame) {
        int cf = Math.floorMod(frame, CLIENT_CHAR_INFO.length);
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                int rowCf = parseCellInt(entry.getValue(7));
                String part = String.valueOf(entry.getValue(0));
                return rowCf == cf || rowCf < 0 || part.startsWith("head:");
            }
        });
    }

    private void chooseWorkshopPngForSelectedRow(Component parent, JTable table, DefaultTableModel model,
            File folder, Map<String, ImageIcon> imageCache, boolean autoFill) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(parent, "Chon 1 row part truoc.");
            return;
        }
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        JFileChooser chooser = new JFileChooser(folder != null ? folder : new File("."));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG images", "png"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selected = chooser.getSelectedFile();
        int modelRow = table.convertRowIndexToModel(row);
        if (autoFill) {
            autoFillWorkshopPngNames(parent, table, model, modelRow, selected.getName());
        } else {
            syncWorkshopPngName(model, modelRow, selected.getName());
        }
        imageCache.clear();
    }

    private void autoFillWorkshopPngNames(Component parent, JTable table, DefaultTableModel model,
            int startModelRow, String firstName) {
        NumberedPng numbered = parseNumberedPngName(firstName);
        if (numbered == null) {
            JOptionPane.showMessageDialog(parent, "Ten PNG phai co so, vi du 21340.png hoac Small21340.png.");
            return;
        }
        int startViewRow = table.convertRowIndexToView(startModelRow);
        if (startViewRow < 0) {
            startViewRow = 0;
        }
        int iconId = numbered.number;
        for (int viewRow = startViewRow; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            syncWorkshopPngName(model, modelRow, numbered.prefix + iconId + ".png");
            iconId++;
        }
    }

    private boolean isWorkshopPlaceholderIcon(int iconId) {
        return iconId == 0 || iconId == 2954 || iconId == 2955;
    }

    private void moveSelectedWorkshopGroupToTop(JTable table, DefaultTableModel model,
            TableRowSorter<DefaultTableModel> sorter) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        sorter.setSortKeys(null);
        int modelRow = table.convertRowIndexToModel(row);
        String group = String.valueOf(model.getValueAt(modelRow, 0));
        List<Object[]> moved = new ArrayList<>();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            if (group.equals(String.valueOf(model.getValueAt(i, 0)))) {
                Object[] rowData = new Object[model.getColumnCount()];
                for (int col = 0; col < model.getColumnCount(); col++) {
                    rowData[col] = model.getValueAt(i, col);
                }
                moved.add(0, rowData);
                model.removeRow(i);
            }
        }
        for (int i = 0; i < moved.size(); i++) {
            model.insertRow(i, moved.get(i));
        }
        if (!moved.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private NumberedPng parseNumberedPngName(String name) {
        if (name == null || !name.toLowerCase().endsWith(".png")) {
            return null;
        }
        String base = name.substring(0, name.length() - 4);
        String prefix = "";
        if (base.regionMatches(true, 0, "Small", 0, 5)) {
            prefix = base.substring(0, 5);
            base = base.substring(5);
        }
        try {
            return new NumberedPng(prefix, Integer.parseInt(base));
        } catch (Exception ignored) {
            return null;
        }
    }

    private void adjustSelectedWorkshopLayer(JTable table, DefaultTableModel model, int delta) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int layer = parseCellInt(model.getValueAt(modelRow, 6));
        model.setValueAt(layer + delta, modelRow, 6);
    }

    private void exportWorkshopZoomImages(Component parent, DefaultTableModel model, File sourceFolder) {
        if (sourceFolder == null || !sourceFolder.isDirectory()) {
            JOptionPane.showMessageDialog(parent, "Chon thu muc x4 hoac thu muc res nguon truoc khi export.");
            return;
        }
        JFileChooser chooser = new JFileChooser(new File(ICON_FOLDER).getAbsoluteFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Chon thu muc icon goc chua x1/x2/x3");
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File root = chooser.getSelectedFile();
        int exported = 0;
        try {
            TreeSet<String> names = workshopPngNames(model);
            for (String name : names) {
                File source = new File(sourceFolder, name);
                if (!source.isFile()) {
                    continue;
                }
                BufferedImage image = ImageIO.read(source);
                if (image == null) {
                    continue;
                }
                exported += writeScaledWorkshopImage(image, new File(root, "x1/" + name), 0.25);
                exported += writeScaledWorkshopImage(image, new File(root, "x2/" + name), 0.50);
                exported += writeScaledWorkshopImage(image, new File(root, "x3/" + name), 0.75);
            }
            JOptionPane.showMessageDialog(parent, "Da export " + exported + " file PNG sang x1/x2/x3.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Khong export duoc PNG: " + ex.getMessage());
        }
    }

    private TreeSet<String> workshopPngNames(DefaultTableModel model) {
        TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = String.valueOf(model.getValueAt(i, 3)).trim();
            if (!name.isBlank() && name.toLowerCase().endsWith(".png")) {
                names.add(name);
            }
        }
        return names;
    }

    private int writeScaledWorkshopImage(BufferedImage source, File target, double scale) throws Exception {
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        File parent = target.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        ImageIO.write(scaled, "png", target);
        return 1;
    }

    private void exportWorkshopPartSql(Component parent, DefaultTableModel model, int[] duplicatedPartIds) {
        String conflicts = workshopPartFrameConflicts(model);
        if (!conflicts.isBlank()) {
            int answer = JOptionPane.showConfirmDialog(parent,
                    "Co nhieu CF trung Part Frame nhung PNG/dx/dy khac nhau:\n\n"
                    + conflicts
                    + "\nExport se lay dong nam sau cung trong bang cho moi Part Frame. Tiep tuc?",
                    "Conflict Part Frame",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
        }
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        JSpinner spnHead = new JSpinner(new SpinnerNumberModel(Math.max(0, duplicatedPartIds[0]), 0, 999999, 1));
        JSpinner spnBody = new JSpinner(new SpinnerNumberModel(Math.max(0, duplicatedPartIds[1]), 0, 999999, 1));
        JSpinner spnLeg = new JSpinner(new SpinnerNumberModel(Math.max(0, duplicatedPartIds[2]), 0, 999999, 1));
        JLabel headGroup = new JLabel(String.join(", ", workshopPartNames(model, "head")));
        form.add(new JLabel("Head part ID"));
        form.add(spnHead);
        form.add(new JLabel("Body part ID"));
        form.add(spnBody);
        form.add(new JLabel("Leg part ID"));
        form.add(spnLeg);
        form.add(new JLabel("Head group"));
        form.add(headGroup);
        if (JOptionPane.showConfirmDialog(parent, form, "Export part.sql", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("part.sql"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            String sql = buildWorkshopPartSql(model,
                    ((Number) spnHead.getValue()).intValue(),
                    ((Number) spnBody.getValue()).intValue(),
                    ((Number) spnLeg.getValue()).intValue());
            java.nio.file.Files.writeString(chooser.getSelectedFile().toPath(), sql);
            JOptionPane.showMessageDialog(parent, "Da export part.sql.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Khong export duoc part.sql: " + ex.getMessage());
        }
    }

    private String buildWorkshopPartSql(DefaultTableModel model, int headId, int bodyId, int legId) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- Generated by Costume Workshop\n");
        appendWorkshopHeadPartSql(sb, model, headId);
        appendWorkshopPartSql(sb, model, bodyId, 1, "body");
        appendWorkshopPartSql(sb, model, legId, 2, "leg");
        return sb.toString();
    }

    private String workshopPartFrameConflicts(DefaultTableModel model) {
        Map<String, String> firstValue = new HashMap<>();
        Map<String, String> firstCf = new HashMap<>();
        List<String> conflicts = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            String part = String.valueOf(model.getValueAt(row, 0));
            int partFrame = parseCellInt(model.getValueAt(row, 1));
            String file = String.valueOf(model.getValueAt(row, 3)).trim();
            if (file.isBlank() || part.startsWith("head:")) {
                continue;
            }
            String key = part + "#" + partFrame;
            String value = file + "|" + parseCellInt(model.getValueAt(row, 4)) + "|" + parseCellInt(model.getValueAt(row, 5));
            String cf = String.valueOf(workshopRowCf(model, row));
            if (!firstValue.containsKey(key)) {
                firstValue.put(key, value);
                firstCf.put(key, cf);
            } else if (!firstValue.get(key).equals(value)) {
                conflicts.add(part + " frame " + partFrame + " CF " + firstCf.get(key) + " vs CF " + cf);
                if (conflicts.size() >= 8) {
                    conflicts.add("...");
                    break;
                }
            }
        }
        return String.join("\n", conflicts);
    }

    private void appendWorkshopHeadPartSql(StringBuilder sb, DefaultTableModel model, int headId) {
        List<String> headParts = workshopPartNames(model, "head");
        if (headParts.isEmpty()) {
            appendWorkshopPartSql(sb, model, headId, 0, "head");
            return;
        }
        for (String partName : headParts) {
            int id = headId;
            if (partName.startsWith("head:")) {
                id = parseCellInt(partName.substring(5));
            }
            appendWorkshopPartSql(sb, model, id, 0, partName);
        }
    }

    private List<String> workshopPartNames(DefaultTableModel model, String prefix) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String part = String.valueOf(model.getValueAt(i, 0));
            if (part.equals(prefix) || part.startsWith(prefix + ":")) {
                if (!names.contains(part)) {
                    names.add(part);
                }
            }
        }
        return names;
    }

    private void refreshWorkshopHeadSelector(JComboBox<String> combo, DefaultTableModel model) {
        Object selected = combo.getSelectedItem();
        List<String> heads = workshopPartNames(model, "head");
        combo.removeAllItems();
        if (heads.isEmpty()) {
            combo.addItem("head");
            return;
        }
        for (String head : heads) {
            combo.addItem(head);
        }
        if (selected != null && heads.contains(String.valueOf(selected))) {
            combo.setSelectedItem(selected);
        } else {
            combo.setSelectedIndex(0);
        }
    }

    private void appendWorkshopPartSql(StringBuilder sb, DefaultTableModel model, int partId, int type, String partName) {
        String data = buildWorkshopPartData(model, partName);
        sb.append("INSERT INTO part (id, TYPE, DATA) VALUES (")
                .append(partId).append(",")
                .append(type).append(",'")
                .append(data.replace("'", "''"))
                .append("') ON DUPLICATE KEY UPDATE TYPE=VALUES(TYPE), DATA=VALUES(DATA);\n");
    }

    private String buildWorkshopPartData(DefaultTableModel model, String partName) {
        Map<Integer, Object[]> rows = new HashMap<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (!partName.equals(String.valueOf(model.getValueAt(i, 0)))) {
                continue;
            }
            String fileName = String.valueOf(model.getValueAt(i, 3)).trim();
            int iconId = parseIconIdFromPng(fileName);
            if (iconId < 0) {
                continue;
            }
            rows.put(parseCellInt(model.getValueAt(i, 1)), new Object[]{iconId,
                parseCellInt(model.getValueAt(i, 4)), parseCellInt(model.getValueAt(i, 5))});
        }
        int frameCount = workshopPartFrameCount(partName);
        StringBuilder data = new StringBuilder("[");
        for (int frame = 0; frame < frameCount; frame++) {
            Object[] row = rows.get(frame);
            int iconId = row != null ? (Integer) row[0] : 2955;
            int dx = row != null ? (Integer) row[1] : 0;
            int dy = row != null ? (Integer) row[2] : 0;
            data.append("[")
                    .append(iconId).append(",")
                    .append(dx).append(",")
                    .append(dy).append("]");
            if (frame < frameCount - 1) {
                data.append(",");
            }
        }
        data.append("]");
        return data.toString();
    }

    private int workshopPartFrameCount(String partName) {
        if (partName != null && partName.startsWith("head")) {
            return 3;
        }
        if ("body".equals(partName)) {
            return 17;
        }
        if ("leg".equals(partName)) {
            return 14;
        }
        return 0;
    }

    private int parseIconIdFromPng(String fileName) {
        if (fileName == null || !fileName.toLowerCase().endsWith(".png")) {
            return -1;
        }
        try {
            String id = fileName.substring(0, fileName.length() - 4).trim();
            if (id.regionMatches(true, 0, "Small", 0, 5)) {
                id = id.substring(5);
            }
            return Integer.parseInt(id);
        } catch (Exception ignored) {
            return -1;
        }
    }

    private void saveWorkshopConfig(Component parent, DefaultTableModel model, File folder) {
        JFileChooser chooser = new JFileChooser(folder != null ? folder : new File("."));
        chooser.setSelectedFile(new File("costume-workshop.json"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            java.nio.file.Files.writeString(chooser.getSelectedFile().toPath(), exportWorkshopConfig(model, folder));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Không lưu được config: " + ex.getMessage());
        }
    }

    private File loadWorkshopConfig(Component parent, DefaultTableModel model) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        try {
            String raw = java.nio.file.Files.readString(chooser.getSelectedFile().toPath());
            com.google.gson.JsonObject root = new JsonParser().parse(raw).getAsJsonObject();
            File folder = root.has("folder") && !root.get("folder").getAsString().isBlank()
                    ? new File(root.get("folder").getAsString()) : null;
            model.setRowCount(0);
            JsonArray frames = root.getAsJsonArray("frames");
            for (int i = 0; i < frames.size(); i++) {
                com.google.gson.JsonObject obj = frames.get(i).getAsJsonObject();
                model.addRow(new Object[]{
                    obj.get("part").getAsString(),
                    obj.get("frame").getAsInt(),
                    obj.get("file").getAsString(),
                    obj.get("file").getAsString(),
                    obj.get("dx").getAsInt(),
                    obj.get("dy").getAsInt(),
                    obj.has("layer") ? obj.get("layer").getAsInt() : defaultLayerForPart(obj.get("part").getAsString()),
                    obj.has("cf") ? obj.get("cf").getAsInt() : -1
                });
            }
            return folder;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Không load được config: " + ex.getMessage());
            return null;
        }
    }

    private void drawWorkshopFrameLayers(Graphics2D g, DefaultTableModel model, File folder,
            Map<String, ImageIcon> imageCache, int frame, String previewHead, int baseX, int baseY,
            double sourceImageScale, double coordinateScale, WorkshopTransform headTransform, WorkshopTransform bodyTransform, WorkshopTransform legTransform) {
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String part = String.valueOf(model.getValueAt(i, 0));
            if (part.startsWith("head") && !part.equals(previewHead)) {
                continue;
            }
            if (workshopRowMatchesFrame(model, i, frame)) {
                rows.add(i);
            }
        }
        rows.sort((a, b) -> Integer.compare(parseCellInt(model.getValueAt(a, 6)), parseCellInt(model.getValueAt(b, 6))));
        for (int row : rows) {
            String part = String.valueOf(model.getValueAt(row, 0));
            WorkshopTransform transform = workshopTransformForPart(part, headTransform, bodyTransform, legTransform);
            drawWorkshopRow(g, model, folder, imageCache, row, frame, baseX, baseY, sourceImageScale, coordinateScale, transform);
        }
    }

    private String workshopImageStatus(DefaultTableModel model, File folder, int frame, String previewHead) {
        int current = 0;
        int currentMissing = 0;
        int total = 0;
        int totalMissing = 0;
        for (int row = 0; row < model.getRowCount(); row++) {
            String name = String.valueOf(model.getValueAt(row, 3)).trim();
            if (name.isBlank()) {
                continue;
            }
            total++;
            boolean missing = resolveWorkshopImageFile(folder, name) == null;
            if (missing) {
                totalMissing++;
            }
            String part = String.valueOf(model.getValueAt(row, 0));
            boolean visibleHead = !part.startsWith("head") || part.equals(previewHead);
            boolean currentFrame = workshopRowMatchesFrame(model, row, frame);
            if (visibleHead && currentFrame) {
                current++;
                if (missing) {
                    currentMissing++;
                }
            }
        }
        return "PNG frame: " + (current - currentMissing) + "/" + current
                + " | thieu: " + totalMissing + "/" + total;
    }

    private WorkshopTransform workshopTransformForPart(String part, WorkshopTransform headTransform,
            WorkshopTransform bodyTransform, WorkshopTransform legTransform) {
        if (part != null && part.startsWith("head")) {
            return headTransform;
        }
        return switch (part) {
            case "body" -> bodyTransform;
            case "leg" -> legTransform;
            default -> new WorkshopTransform(1.0, 0, 0);
        };
    }

    private void drawWorkshopRow(Graphics2D g, DefaultTableModel model, File folder,
            Map<String, ImageIcon> imageCache, int row, int cf, int baseX, int baseY,
            double sourceImageScale, double coordinateScale, WorkshopTransform transform) {
        String fileName = String.valueOf(model.getValueAt(row, 3));
        if (fileName == null || fileName.isBlank()) return;
        ImageIcon icon = loadWorkshopImage(folder, imageCache, fileName);
        if (icon == null) return;
        String part = String.valueOf(model.getValueAt(row, 0));
        Rectangle bounds = workshopRowBounds(model, row, cf, baseX, baseY, sourceImageScale, coordinateScale, transform, icon);
        g.drawImage(icon.getImage(),
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height,
                null);
    }

    private String clientCfLabel(int cf) {
        int index = Math.floorMod(cf, CLIENT_CHAR_INFO.length);
        int head = CLIENT_CHAR_INFO[index][PART_HEAD][CI_FRAME];
        int leg = CLIENT_CHAR_INFO[index][PART_LEG][CI_FRAME];
        int body = CLIENT_CHAR_INFO[index][PART_BODY][CI_FRAME];
        String label = index < CLIENT_CF_LABELS.length ? CLIENT_CF_LABELS[index] : "cf " + index;
        return label + "  | H" + head + " B" + body + " L" + leg;
    }

    private int clientPartFrameIndex(int cf, String part) {
        int index = Math.floorMod(cf, CLIENT_CHAR_INFO.length);
        return CLIENT_CHAR_INFO[index][clientPartIndex(part)][CI_FRAME];
    }

    private int[] clientPartOffset(int cf, String part) {
        int index = Math.floorMod(cf, CLIENT_CHAR_INFO.length);
        int[] info = CLIENT_CHAR_INFO[index][clientPartIndex(part)];
        return new int[]{info[CI_DX], info[CI_DY]};
    }

    private int clientPartIndex(String part) {
        if (part != null && part.startsWith("head")) {
            return PART_HEAD;
        }
        return switch (part) {
            case "head" -> PART_HEAD;
            case "body" -> PART_BODY;
            case "leg" -> PART_LEG;
            default -> PART_BODY;
        };
    }

    private ImageIcon loadWorkshopImage(File folder, Map<String, ImageIcon> imageCache, String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        File imageFile = resolveWorkshopImageFile(folder, fileName);
        if (imageFile == null) return null;
        String key = imageFile.getAbsolutePath();
        if (imageCache.containsKey(key)) return imageCache.get(key);
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) return null;
            ImageIcon icon = new ImageIcon(image);
            imageCache.put(key, icon);
            return icon;
        } catch (Exception ignored) {
            return null;
        }
    }

    private File resolveWorkshopImageFile(File folder, String fileName) {
        File image = resolveWorkshopImageFileInFolder(folder, fileName);
        if (image != null) return image;
        File x4Folder = new File(ICON_FOLDER + "x4");
        if (!x4Folder.equals(folder)) {
            image = resolveWorkshopImageFileInFolder(x4Folder, fileName);
            if (image != null) return image;
        }
        return null;
    }

    private File resolveWorkshopImageFileInFolder(File folder, String fileName) {
        if (folder == null) return null;
        File direct = new File(folder, fileName);
        if (direct.isFile()) return direct;
        String normalized = normalizeWorkshopPngName(fileName);
        File normalizedFile = new File(folder, normalized);
        if (normalizedFile.isFile()) return normalizedFile;
        File smallFile = new File(folder, "Small" + normalized);
        if (smallFile.isFile()) return smallFile;
        if (normalized.startsWith("Small")) {
            File numericFile = new File(folder, normalized.substring(5));
            if (numericFile.isFile()) return numericFile;
        }
        return null;
    }

    private String normalizeWorkshopPngName(String fileName) {
        String name = fileName != null ? fileName.trim() : "";
        if (name.toLowerCase().endsWith(".png")) {
            return name;
        }
        return name + ".png";
    }

    private int parseCellInt(Object value) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String exportWorkshopConfig(DefaultTableModel model, File folder) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"folder\": \"").append(folder != null ? folder.getAbsolutePath().replace("\\", "\\\\") : "").append("\",\n");
        sb.append("  \"frames\": [\n");
        for (int i = 0; i < model.getRowCount(); i++) {
            sb.append("    {")
                    .append("\"part\":\"").append(model.getValueAt(i, 0)).append("\",")
                    .append("\"frame\":").append(model.getValueAt(i, 1)).append(",")
                    .append("\"file\":\"").append(String.valueOf(model.getValueAt(i, 3)).replace("\"", "\\\"")).append("\",")
                    .append("\"dx\":").append(parseCellInt(model.getValueAt(i, 4))).append(",")
                    .append("\"dy\":").append(parseCellInt(model.getValueAt(i, 5))).append(",")
                    .append("\"layer\":").append(parseCellInt(model.getValueAt(i, 6))).append(",")
                    .append("\"cf\":").append(workshopRowCf(model, i)).append("}");
            if (i < model.getRowCount() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }

    private int defaultLayerForPart(String part) {
        return switch (part) {
            case "leg" -> 0;
            case "body" -> 1;
            case "head" -> 2;
            default -> 3;
        };
    }

    private void showIconSearchDialog(JDialog parent, IdCallback callback) {
        JDialog dialog = new JDialog(parent, "Tìm Icon ID", true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(8, 8));

        JTextField txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Nhập Icon ID để lọc"));

        DefaultTableModel searchModel = new DefaultTableModel(new String[]{"Icon ID", "Preview"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : Integer.class;
            }
        };

        for (int iconId : getAvailableIconIds()) {
            searchModel.addRow(new Object[]{iconId, iconId});
        }

        JTable resultTable = new JTable(searchModel);
        resultTable.setRowHeight(34);
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        resultTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                label.setHorizontalAlignment(JLabel.CENTER);
                if (value instanceof Integer) {
                    label.setIcon(ItemDataPanel.this.getIcon((Integer) value, 28));
                }
                label.setOpaque(true);
                label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return label;
            }
        });

        TableRowSorter<DefaultTableModel> searchSorter = new TableRowSorter<>(searchModel);
        resultTable.setRowSorter(searchSorter);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText().trim();
                searchSorter.setRowFilter(text.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0));
            }
        });

        Runnable select = () -> {
            int row = resultTable.getSelectedRow();
            if (row < 0) return;
            int modelRow = resultTable.convertRowIndexToModel(row);
            callback.onSelect((Integer) searchModel.getValueAt(modelRow, 0));
            dialog.dispose();
        };
        resultTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) select.run();
            }
        });

        JButton btnSelect = ServerGuiUtils.createStyledButton("Chọn Icon", COL_PRIMARY, Color.WHITE);
        btnSelect.addActionListener(e -> select.run());
        dialog.add(txtSearch, BorderLayout.NORTH);
        dialog.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        dialog.add(btnSelect, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showPartSearchDialog(JDialog parent, IdCallback callback) {
        if (partIconMap.isEmpty()) {
            loadPartIconMap();
        }

        JDialog dialog = new JDialog(parent, "Tìm Part", true);
        dialog.setSize(560, 600);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(8, 8));

        JTextField txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Nhập Part ID hoặc Icon ID để lọc"));

        DefaultTableModel searchModel = new DefaultTableModel(new String[]{"Part ID", "Icon ID", "Preview"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return Integer.class; }
        };

        partIconMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> searchModel.addRow(new Object[]{entry.getKey(), entry.getValue(), entry.getValue()}));

        JTable resultTable = new JTable(searchModel);
        resultTable.setRowHeight(34);
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        resultTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                label.setHorizontalAlignment(JLabel.CENTER);
                if (value instanceof Integer) {
                    label.setIcon(ItemDataPanel.this.getIcon((Integer) value, 28));
                }
                label.setOpaque(true);
                label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return label;
            }
        });

        TableRowSorter<DefaultTableModel> searchSorter = new TableRowSorter<>(searchModel);
        resultTable.setRowSorter(searchSorter);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    searchSorter.setRowFilter(null);
                    return;
                }
                RowFilter<DefaultTableModel, Integer> partFilter =
                        RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0);
                RowFilter<DefaultTableModel, Integer> iconFilter =
                        RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1);
                searchSorter.setRowFilter(RowFilter.orFilter(Arrays.asList(partFilter, iconFilter)));
            }
        });

        Runnable select = () -> {
            int row = resultTable.getSelectedRow();
            if (row < 0) return;
            int modelRow = resultTable.convertRowIndexToModel(row);
            callback.onSelect((Integer) searchModel.getValueAt(modelRow, 0));
            dialog.dispose();
        };
        resultTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) select.run();
            }
        });

        JButton btnSelect = ServerGuiUtils.createStyledButton("Chọn Part", COL_PRIMARY, Color.WHITE);
        btnSelect.addActionListener(e -> select.run());
        dialog.add(txtSearch, BorderLayout.NORTH);
        dialog.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        dialog.add(btnSelect, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private TreeSet<Integer> getAvailableIconIds() {
        TreeSet<Integer> ids = new TreeSet<>();
        for (String zoom : new String[]{"x2", "x1", "x3", "x4"}) {
            File dir = new File(ICON_FOLDER + zoom);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
            if (files == null) {
                continue;
            }
            for (File file : files) {
                String name = file.getName();
                try {
                    ids.add(Integer.parseInt(name.substring(0, name.length() - 4)));
                } catch (Exception ignored) {
                }
            }
            if (!ids.isEmpty()) {
                break;
            }
        }
        return ids;
    }

    private interface IdCallback {
        void onSelect(int id);
    }

    private static class PartFrame {
        final int iconId;
        final int dx;
        final int dy;

        PartFrame(int iconId, int dx, int dy) {
            this.iconId = iconId;
            this.dx = dx;
            this.dy = dy;
        }
    }

    private static class WorkshopTransform {
        final double scale;
        final int moveX;
        final int moveY;

        WorkshopTransform(double scale, int moveX, int moveY) {
            this.scale = scale;
            this.moveX = moveX;
            this.moveY = moveY;
        }
    }

    private static class NumberedPng {
        final String prefix;
        final int number;

        NumberedPng(String prefix, int number) {
            this.prefix = prefix;
            this.number = number;
        }
    }

    private static class WorkshopPreviewPanel extends JPanel {
        private BufferedImage image;
        private double zoom = 1.0;
        private boolean fitImage;
        private Point lastDragPoint;
        private DragHandler dragHandler;

        WorkshopPreviewPanel() {
            setBackground(Color.WHITE);
            addMouseWheelListener(e -> {
                double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
                zoom = Math.max(0.25, Math.min(5.0, zoom * factor));
                repaint();
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    lastDragPoint = e.getPoint();
                }
                @Override public void mouseReleased(MouseEvent e) {
                    lastDragPoint = null;
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseDragged(MouseEvent e) {
                    if (lastDragPoint == null || dragHandler == null) {
                        return;
                    }
                    int dx = (int) Math.round((e.getX() - lastDragPoint.x) / (zoom * 4.0));
                    int dy = (int) Math.round((e.getY() - lastDragPoint.y) / (zoom * 4.0));
                    if (dx != 0 || dy != 0) {
                        dragHandler.dragged(dx, dy);
                        lastDragPoint = e.getPoint();
                    }
                }
            });
        }

        void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        void setFitImage(boolean fitImage) {
            this.fitImage = fitImage;
            repaint();
        }

        void setDragHandler(DragHandler dragHandler) {
            this.dragHandler = dragHandler;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            double drawZoom = zoom;
            if (fitImage) {
                drawZoom = Math.min((getWidth() - 12) / (double) image.getWidth(),
                        (getHeight() - 12) / (double) image.getHeight());
                drawZoom = Math.max(0.1, drawZoom);
            }
            int drawWidth = (int) Math.round(image.getWidth() * drawZoom);
            int drawHeight = (int) Math.round(image.getHeight() * drawZoom);
            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;
            g2.drawImage(image, x, y, drawWidth, drawHeight, null);
            g2.dispose();
        }
    }

    private interface DragHandler {
        void dragged(int dx, int dy);
    }

    private JTextField field(String val) {
        JTextField f = new JTextField(val);
        f.setFont(FONT_PLAIN);
        f.setPreferredSize(new Dimension(180, 32));
        return f;
    }
}
