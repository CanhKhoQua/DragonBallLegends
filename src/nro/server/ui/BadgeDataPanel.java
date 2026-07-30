package nro.server.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jdbc.DBConnecter;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BadgeDataPanel extends JPanel {

    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Color COL_PRIMARY = new Color(0, 120, 215);
    private static final Color COL_GREEN = new Color(40, 167, 69);
    private static final String EFFECT_DIR = "data/effect/";
    private static final String EFFDATA_DIR = "data/effdata/";
    private static final String ICON_DIR = "data/icon/";
    private static final int PREVIEW_HEAD_PART = 391;
    private static final int PREVIEW_BODY_PART = 392;
    private static final int PREVIEW_LEG_PART = 393;
    private static final int PART_HEAD = 0;
    private static final int PART_LEG = 1;
    private static final int PART_BODY = 2;
    private static final int CI_FRAME = 0;
    private static final int CI_DX = 1;
    private static final int CI_DY = 2;
    private static final int[][][] PREVIEW_CHAR_INFO = {
        {{0, -13, 34}, {1, -8, 10}, {1, -9, 16}}
    };

    private final DefaultTableModel badgeModel = new DefaultTableModel(
            new String[]{"ID", "Effect", "Item", "Tên", "Icon", "Data", "Ảnh x2"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
        @Override public Class<?> getColumnClass(int col) {
            return col == 3 || col == 5 || col == 6 ? String.class : Integer.class;
        }
    };
    private final DefaultTableModel spriteModel = new DefaultTableModel(
            new String[]{"Sprite", "X", "Y", "W", "H"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return true; }
        @Override public Class<?> getColumnClass(int col) { return Integer.class; }
    };
    private final DefaultTableModel partModel = new DefaultTableModel(
            new String[]{"Frame", "Part", "X", "Y", "Sprite"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return col >= 2; }
        @Override public Class<?> getColumnClass(int col) { return Integer.class; }
    };

    private JTable badgeTable;
    private JTable spriteTable;
    private JTable partTable;
    private TableRowSorter<DefaultTableModel> badgeSorter;
    private JTextField txtSearch;
    private JLabel lblInfo;
    private JLabel lblFileInfo;
    private JComboBox<String> cbZoom;
    private JSpinner spnFrame;
    private JSpinner spnMoveX;
    private JSpinner spnMoveY;
    private JSpinner spnBottom;
    private JSpinner spnScalePercent;
    private EffectPreviewPanel previewPanel;

    private EffectData currentEffect;
    private int currentEffectId = -1;
    private boolean loadingTables;
    private boolean loaded;
    private final Map<Integer, List<CharacterPartFrame>> previewPartFrames = new HashMap<>();
    private final Map<String, BufferedImage> previewPartImageCache = new HashMap<>();

    public BadgeDataPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initUI();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing() && !loaded) {
                loaded = true;
                loadBadges();
            }
        });
    }

    private void initUI() {
        JLabel title = new JLabel("DỮ LIỆU DANH HIỆU");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(40, 40, 40));

        txtSearch = new JTextField(16);
        txtSearch.setPreferredSize(new Dimension(180, 32));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm ID, effect hoặc tên...");
        txtSearch.setFont(FONT_PLAIN);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterBadges(); }
            @Override public void removeUpdate(DocumentEvent e) { filterBadges(); }
            @Override public void changedUpdate(DocumentEvent e) { filterBadges(); }
        });

        JButton btnReload = ServerGuiUtils.createStyledButton("Tải lại", new Color(100, 100, 100), Color.WHITE);
        btnReload.addActionListener(e -> loadBadges());

        JButton btnSaveTop = ServerGuiUtils.createStyledButton("Lưu", COL_GREEN, Color.WHITE);
        btnSaveTop.addActionListener(e -> saveCurrentEffect());

        lblInfo = new JLabel(" ");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(new Color(100, 100, 100));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setBackground(Color.WHITE);
        searchBar.add(new JLabel("Tìm:"));
        searchBar.add(txtSearch);
        searchBar.add(btnReload);
        searchBar.add(btnSaveTop);
        searchBar.add(lblInfo);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setBackground(Color.WHITE);
        north.add(title, BorderLayout.NORTH);
        north.add(searchBar, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        badgeTable = createTable(badgeModel);
        badgeSorter = new TableRowSorter<>(badgeModel);
        badgeTable.setRowSorter(badgeSorter);
        badgeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        badgeTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        badgeTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        badgeTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        badgeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedBadge();
            }
        });

        JScrollPane badgeScroll = new JScrollPane(badgeTable);
        badgeScroll.setPreferredSize(new Dimension(300, 0));
        badgeScroll.setMinimumSize(new Dimension(260, 0));

        JPanel editor = buildEditorPanel();
        add(badgeScroll, BorderLayout.WEST);
        add(editor, BorderLayout.CENTER);
    }

    private JPanel buildEditorPanel() {
        spriteTable = createTable(spriteModel);
        partTable = createTable(partModel);
        spriteTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        partTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        spriteModel.addTableModelListener(e -> {
            if (!loadingTables && e.getType() != TableModelEvent.DELETE) {
                refreshPreview();
            }
        });
        partModel.addTableModelListener(e -> {
            if (!loadingTables && e.getType() != TableModelEvent.DELETE) {
                refreshPreview();
            }
        });
        partTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = partTable.getSelectedRow();
                if (row >= 0) {
                    int modelRow = partTable.convertRowIndexToModel(row);
                    spnFrame.setValue(toInt(partModel.getValueAt(modelRow, 0), 0));
                    refreshPreview();
                }
            }
        });

        cbZoom = new JComboBox<>(new String[]{"x1", "x2", "x3", "x4"});
        cbZoom.setSelectedItem("x2");
        cbZoom.addActionListener(e -> refreshPreview());

        spnFrame = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        spnFrame.addChangeListener(e -> refreshPreview());

        spnMoveX = new JSpinner(new SpinnerNumberModel(0, -999, 999, 1));
        spnMoveY = new JSpinner(new SpinnerNumberModel(0, -999, 999, 1));
        spnBottom = new JSpinner(new SpinnerNumberModel(1, -999, 999, 1));
        spnScalePercent = new JSpinner(new SpinnerNumberModel(110, 50, 200, 5));
        setSpinnerWidth(spnFrame, 56);
        setSpinnerWidth(spnMoveX, 56);
        setSpinnerWidth(spnMoveY, 56);
        setSpinnerWidth(spnBottom, 56);
        setSpinnerWidth(spnScalePercent, 64);

        JButton btnMove = ServerGuiUtils.createStyledButton("Dời X/Y", COL_PRIMARY, Color.WHITE);
        btnMove.addActionListener(e -> moveAllParts((Integer) spnMoveX.getValue(), (Integer) spnMoveY.getValue()));

        JButton btnUp = ServerGuiUtils.createStyledButton("Lên 10", new Color(100, 100, 100), Color.WHITE);
        btnUp.addActionListener(e -> moveAllParts(0, -10));

        JButton btnDown = ServerGuiUtils.createStyledButton("Xuống 10", new Color(100, 100, 100), Color.WHITE);
        btnDown.addActionListener(e -> moveAllParts(0, 10));

        JButton btnBottom = ServerGuiUtils.createStyledButton("Đặt đáy", COL_PRIMARY, Color.WHITE);
        btnBottom.addActionListener(e -> setBottomForAllParts((Integer) spnBottom.getValue()));

        JButton btnSave = ServerGuiUtils.createStyledButton("Lưu", COL_GREEN, Color.WHITE);
        btnSave.addActionListener(e -> saveCurrentEffect());

        JButton btnReload = ServerGuiUtils.createStyledButton("Reload", new Color(100, 100, 100), Color.WHITE);
        btnReload.addActionListener(e -> {
            if (currentEffectId >= 0) {
                loadEffect(currentEffectId);
            }
        });

        JButton btnScale = ServerGuiUtils.createStyledButton("Scale lưu", new Color(111, 66, 193), Color.WHITE);
        btnScale.addActionListener(e -> scaleCurrentEffect((Integer) spnScalePercent.getValue()));

        lblFileInfo = new JLabel("Chọn một danh hiệu để sửa.");
        lblFileInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblFileInfo.setForeground(new Color(90, 90, 90));

        JPanel topControls = new JPanel(new GridBagLayout());
        topControls.setBackground(Color.WHITE);
        GridBagConstraints controlGbc = new GridBagConstraints();
        controlGbc.gridx = 0;
        controlGbc.weightx = 1;
        controlGbc.anchor = GridBagConstraints.WEST;
        controlGbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel viewControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        viewControls.setBackground(Color.WHITE);
        viewControls.add(new JLabel("Zoom:"));
        viewControls.add(cbZoom);
        viewControls.add(new JLabel("Frame:"));
        viewControls.add(spnFrame);
        viewControls.add(new JLabel("Move X:"));
        viewControls.add(spnMoveX);
        viewControls.add(new JLabel("Y:"));
        viewControls.add(spnMoveY);
        viewControls.add(btnMove);

        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bottomControls.setBackground(Color.WHITE);
        bottomControls.add(btnUp);
        bottomControls.add(btnDown);
        bottomControls.add(new JLabel("Đáy:"));
        bottomControls.add(spnBottom);
        bottomControls.add(btnBottom);

        controlGbc.gridy = 0;
        topControls.add(viewControls, controlGbc);
        controlGbc.gridy = 1;
        topControls.add(bottomControls, controlGbc);

        JPanel sideActions = new JPanel(new GridBagLayout());
        sideActions.setBackground(Color.WHITE);
        sideActions.setBorder(ServerGuiUtils.createSectionBorder("Thao tác"));
        sideActions.setPreferredSize(new Dimension(150, 0));
        sideActions.setMinimumSize(new Dimension(140, 0));
        GridBagConstraints sideGbc = new GridBagConstraints();
        sideGbc.gridx = 0;
        sideGbc.weightx = 1;
        sideGbc.fill = GridBagConstraints.HORIZONTAL;
        sideGbc.insets = new java.awt.Insets(0, 6, 8, 6);
        sideGbc.gridy = 0;
        sideActions.add(btnSave, sideGbc);
        sideGbc.gridy++;
        sideActions.add(btnReload, sideGbc);
        sideGbc.gridy++;
        sideActions.add(new JLabel("Scale %:"), sideGbc);
        sideGbc.gridy++;
        sideActions.add(spnScalePercent, sideGbc);
        sideGbc.gridy++;
        sideActions.add(btnScale, sideGbc);
        sideGbc.gridy++;
        sideGbc.weighty = 1;
        JPanel sideFiller = new JPanel();
        sideFiller.setBackground(Color.WHITE);
        sideActions.add(sideFiller, sideGbc);

        JPanel tables = new JPanel(new GridLayout(1, 2, 10, 0));
        tables.setBackground(Color.WHITE);
        tables.setPreferredSize(new Dimension(0, 190));
        tables.setMinimumSize(new Dimension(0, 120));
        JPanel spritePanel = new JPanel(new BorderLayout(0, 6));
        spritePanel.setBackground(Color.WHITE);
        spritePanel.setBorder(ServerGuiUtils.createSectionBorder("Sprite rect trong DataEffect"));
        spritePanel.add(new JScrollPane(spriteTable), BorderLayout.CENTER);
        JPanel partPanel = new JPanel(new BorderLayout(0, 6));
        partPanel.setBackground(Color.WHITE);
        partPanel.setBorder(ServerGuiUtils.createSectionBorder("Frame part / offset"));
        partPanel.add(new JScrollPane(partTable), BorderLayout.CENTER);
        tables.add(spritePanel);
        tables.add(partPanel);

        previewPanel = new EffectPreviewPanel();
        previewPanel.setPreferredSize(new Dimension(520, 420));
        previewPanel.setMinimumSize(new Dimension(320, 280));
        JPanel previewWrap = new JPanel(new BorderLayout(0, 6));
        previewWrap.setBackground(Color.WHITE);
        previewWrap.setBorder(ServerGuiUtils.createSectionBorder("Preview frame"));
        previewWrap.add(previewPanel, BorderLayout.CENTER);
        previewWrap.add(lblFileInfo, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(Color.WHITE);
        center.add(topControls, BorderLayout.NORTH);
        center.add(previewWrap, BorderLayout.CENTER);
        center.add(tables, BorderLayout.SOUTH);

        JPanel editorBody = new JPanel(new BorderLayout(10, 0));
        editorBody.setBackground(Color.WHITE);
        editorBody.add(center, BorderLayout.CENTER);
        editorBody.add(sideActions, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(editorBody, BorderLayout.CENTER);
        return wrapper;
    }

    private void addHelp(JPanel panel, int row, String text) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel label = new JLabel(text);
        label.setFont(FONT_PLAIN);
        panel.add(label, gbc);
    }

    private void setSpinnerWidth(JSpinner spinner, int width) {
        Dimension size = new Dimension(width, 30);
        spinner.setPreferredSize(size);
        spinner.setMinimumSize(size);
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONT_PLAIN);
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COL_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, selected, focus, row, col);
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });
        return table;
    }

    private void loadBadges() {
        new Thread(() -> {
            List<Object[]> rows = new ArrayList<>();
            try (Connection conn = DBConnecter.getConnectionServer();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT b.id, b.idEffect, b.idItem, b.NAME, i.icon_id "
                                 + "FROM data_badges b LEFT JOIN item_template i ON b.idItem = i.id "
                                 + "ORDER BY b.idEffect ASC")) {
                while (rs.next()) {
                    int effectId = rs.getInt("idEffect");
                    rows.add(new Object[]{
                            rs.getInt("id"),
                            effectId,
                            rs.getInt("idItem"),
                            rs.getString("NAME"),
                            rs.getObject("icon_id") != null ? rs.getInt("icon_id") : -1,
                            new File(EFFDATA_DIR + "DataEffect_" + effectId).exists() ? "OK" : "Thiếu",
                            new File(EFFECT_DIR + "x2/ImgEffect_" + effectId + ".png").exists() ? "OK" : "Thiếu"
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Không tải được data_badges: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
            }
            SwingUtilities.invokeLater(() -> {
                badgeModel.setRowCount(0);
                for (Object[] row : rows) {
                    badgeModel.addRow(row);
                }
                lblInfo.setText("Tổng: " + rows.size() + " danh hiệu");
            });
        }).start();
    }

    private void filterBadges() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) {
            badgeSorter.setRowFilter(null);
            return;
        }
        badgeSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(kw), 0, 1, 3));
    }

    private void loadSelectedBadge() {
        int row = badgeTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = badgeTable.convertRowIndexToModel(row);
        int effectId = toInt(badgeModel.getValueAt(modelRow, 1), -1);
        loadEffect(effectId);
    }

    private void loadEffect(int effectId) {
        try {
            currentEffectId = effectId;
            currentEffect = EffectData.read(new File(EFFDATA_DIR + "DataEffect_" + effectId));
            loadingTables = true;
            spriteModel.setRowCount(0);
            partModel.setRowCount(0);
            for (SpriteRect rect : currentEffect.sprites) {
                spriteModel.addRow(new Object[]{rect.id, rect.x, rect.y, rect.w, rect.h});
            }
            for (int frame = 0; frame < currentEffect.frames.size(); frame++) {
                List<FramePart> parts = currentEffect.frames.get(frame);
                for (int part = 0; part < parts.size(); part++) {
                    FramePart fp = parts.get(part);
                    partModel.addRow(new Object[]{frame, part, fp.x, fp.y, fp.spriteId});
                }
            }
            loadingTables = false;
            ((SpinnerNumberModel) spnFrame.getModel()).setMaximum(Math.max(0, currentEffect.frames.size() - 1));
            spnFrame.setValue(0);
            lblFileInfo.setText("Effect " + effectId + " | sprites=" + currentEffect.sprites.size()
                    + " | frames=" + currentEffect.frames.size()
                    + " | trailing=" + currentEffect.trailing.length + " bytes");
            refreshPreview();
        } catch (Exception ex) {
            currentEffect = null;
            currentEffectId = -1;
            spriteModel.setRowCount(0);
            partModel.setRowCount(0);
            previewPanel.setData(null, null, "x2", 2, 0);
            lblFileInfo.setText("Không đọc được DataEffect_" + effectId + ": " + ex.getMessage());
            JOptionPane.showMessageDialog(this, lblFileInfo.getText(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            loadingTables = false;
        }
    }

    private void refreshPreview() {
        if (currentEffectId < 0) {
            return;
        }
        stopEditingTables();
        try {
            EffectData edited = buildEffectFromTables();
            String zoomText = String.valueOf(cbZoom.getSelectedItem());
            int zoom = Integer.parseInt(zoomText.substring(1));
            File imgFile = new File(EFFECT_DIR + zoomText + "/ImgEffect_" + currentEffectId + ".png");
            BufferedImage image = imgFile.exists() ? ImageIO.read(imgFile) : null;
            int frame = (Integer) spnFrame.getValue();
            previewPanel.setData(edited, image, zoomText, zoom, frame);
            if (image == null) {
                lblFileInfo.setText("Thiếu ảnh: " + imgFile.getPath());
            }
        } catch (Exception ex) {
            lblFileInfo.setText("Preview lỗi: " + ex.getMessage());
        }
    }

    private void moveAllParts(int dx, int dy) {
        stopEditingTables();
        loadingTables = true;
        try {
            for (int row = 0; row < partModel.getRowCount(); row++) {
                partModel.setValueAt(toInt(partModel.getValueAt(row, 2), 0) + dx, row, 2);
                partModel.setValueAt(toInt(partModel.getValueAt(row, 3), 0) + dy, row, 3);
            }
        } finally {
            loadingTables = false;
        }
        refreshPreview();
    }

    private void setBottomForAllParts(int bottom) {
        stopEditingTables();
        Map<Integer, Integer> heights = new HashMap<>();
        for (int row = 0; row < spriteModel.getRowCount(); row++) {
            heights.put(toInt(spriteModel.getValueAt(row, 0), 0), toInt(spriteModel.getValueAt(row, 4), 0));
        }
        for (int row = 0; row < partModel.getRowCount(); row++) {
            int spriteId = toInt(partModel.getValueAt(row, 4), 0);
            Integer h = heights.get(spriteId);
            if (h != null) {
                partModel.setValueAt(bottom - h, row, 3);
            }
        }
        refreshPreview();
    }

    private void scaleCurrentEffect(int percent) {
        if (currentEffectId < 0 || currentEffect == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn danh hiệu.");
            return;
        }
        if (percent == 100) {
            JOptionPane.showMessageDialog(this, "Scale 100% không thay đổi gì.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Phóng/thu effect " + currentEffectId + " lên " + percent + "%?\n"
                        + "Tool sẽ backup rồi ghi lại DataEffect và ảnh ImgEffect x1-x4.",
                "Xác nhận scale danh hiệu", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        stopEditingTables();
        try {
            double factor = percent / 100.0;
            EffectData scaled = scaleEffectData(buildEffectFromTables(), factor);
            String ts = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            scaleEffectImages(currentEffectId, factor, ts);

            File file = new File(EFFDATA_DIR + "DataEffect_" + currentEffectId);
            if (file.exists()) {
                Files.copy(file.toPath(), new File(file.getPath() + ".bak_" + ts).toPath());
            }
            scaled.write(file);
            currentEffect = scaled;
            loadEffectToTables(scaled);
            lblFileInfo.setText("Đã scale effect " + currentEffectId + " lên " + percent + "%. Tắt mở lại client để test cache.");
            JOptionPane.showMessageDialog(this, "Đã scale và lưu effect " + currentEffectId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Scale lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private EffectData scaleEffectData(EffectData source, double factor) {
        EffectData scaled = new EffectData();
        scaled.unknown = source.unknown;
        scaled.trailing = source.trailing;
        for (SpriteRect rect : source.sprites) {
            SpriteRect copy = new SpriteRect();
            copy.id = rect.id;
            copy.x = scaleInt(rect.x, factor);
            copy.y = scaleInt(rect.y, factor);
            copy.w = Math.max(1, scaleInt(rect.w, factor));
            copy.h = Math.max(1, scaleInt(rect.h, factor));
            if (copy.x > 255 || copy.y > 255 || copy.w > 255 || copy.h > 255) {
                throw new IllegalArgumentException("Sprite rect vượt giới hạn 255 sau khi scale. Hãy giảm % scale.");
            }
            scaled.sprites.add(copy);
        }
        for (List<FramePart> parts : source.frames) {
            List<FramePart> frame = new ArrayList<>();
            for (FramePart part : parts) {
                FramePart copy = new FramePart();
                copy.x = scaleInt(part.x, factor);
                copy.y = scaleInt(part.y, factor);
                copy.spriteId = part.spriteId;
                frame.add(copy);
            }
            scaled.frames.add(frame);
        }
        return scaled;
    }

    private int scaleInt(int value, double factor) {
        return (int) Math.round(value * factor);
    }

    private void scaleEffectImages(int effectId, double factor, String ts) throws Exception {
        for (String zoomText : new String[]{"x1", "x2", "x3", "x4"}) {
            File file = new File(EFFECT_DIR + zoomText + "/ImgEffect_" + effectId + ".png");
            if (!file.exists()) {
                continue;
            }
            BufferedImage source = ImageIO.read(file);
            if (source == null) {
                continue;
            }
            BufferedImage scaled = scaleImage(source, factor);
            Files.copy(file.toPath(), new File(file.getPath() + ".bak_" + ts).toPath());
            ImageIO.write(scaled, "png", file);
        }
    }

    private BufferedImage scaleImage(BufferedImage source, double factor) {
        int width = Math.max(1, scaleInt(source.getWidth(), factor));
        int height = Math.max(1, scaleInt(source.getHeight(), factor));
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(source, 0, 0, width, height, null);
        g2.dispose();
        return scaled;
    }

    private void saveCurrentEffect() {
        if (currentEffectId < 0 || currentEffect == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn danh hiệu.");
            return;
        }
        stopEditingTables();
        try {
            EffectData edited = buildEffectFromTables();
            File file = new File(EFFDATA_DIR + "DataEffect_" + currentEffectId);
            if (file.exists()) {
                String ts = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                Files.copy(file.toPath(), new File(file.getPath() + ".bak_" + ts).toPath());
            }
            edited.write(file);
            currentEffect = edited;
            lblFileInfo.setText("Đã lưu DataEffect_" + currentEffectId + ". Tắt mở lại client để test cache.");
            JOptionPane.showMessageDialog(this, "Đã lưu DataEffect_" + currentEffectId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lưu lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEffectToTables(EffectData effectData) {
        loadingTables = true;
        try {
            spriteModel.setRowCount(0);
            partModel.setRowCount(0);
            for (SpriteRect rect : effectData.sprites) {
                spriteModel.addRow(new Object[]{rect.id, rect.x, rect.y, rect.w, rect.h});
            }
            for (int frame = 0; frame < effectData.frames.size(); frame++) {
                List<FramePart> parts = effectData.frames.get(frame);
                for (int part = 0; part < parts.size(); part++) {
                    FramePart fp = parts.get(part);
                    partModel.addRow(new Object[]{frame, part, fp.x, fp.y, fp.spriteId});
                }
            }
            ((SpinnerNumberModel) spnFrame.getModel()).setMaximum(Math.max(0, effectData.frames.size() - 1));
            spnFrame.setValue(Math.min((Integer) spnFrame.getValue(), Math.max(0, effectData.frames.size() - 1)));
        } finally {
            loadingTables = false;
        }
        refreshPreview();
    }

    private void stopEditingTables() {
        if (spriteTable.isEditing()) {
            spriteTable.getCellEditor().stopCellEditing();
        }
        if (partTable.isEditing()) {
            partTable.getCellEditor().stopCellEditing();
        }
    }

    private EffectData buildEffectFromTables() {
        EffectData data = new EffectData();
        data.unknown = currentEffect != null ? currentEffect.unknown : 0;
        data.trailing = currentEffect != null ? currentEffect.trailing : new byte[0];

        for (int row = 0; row < spriteModel.getRowCount(); row++) {
            SpriteRect rect = new SpriteRect();
            rect.id = toInt(spriteModel.getValueAt(row, 0), 0);
            rect.x = toInt(spriteModel.getValueAt(row, 1), 0);
            rect.y = toInt(spriteModel.getValueAt(row, 2), 0);
            rect.w = toInt(spriteModel.getValueAt(row, 3), 0);
            rect.h = toInt(spriteModel.getValueAt(row, 4), 0);
            data.sprites.add(rect);
        }

        int maxFrame = -1;
        for (int row = 0; row < partModel.getRowCount(); row++) {
            maxFrame = Math.max(maxFrame, toInt(partModel.getValueAt(row, 0), 0));
        }
        for (int i = 0; i <= maxFrame; i++) {
            data.frames.add(new ArrayList<>());
        }
        for (int row = 0; row < partModel.getRowCount(); row++) {
            int frame = toInt(partModel.getValueAt(row, 0), 0);
            FramePart part = new FramePart();
            part.x = toInt(partModel.getValueAt(row, 2), 0);
            part.y = toInt(partModel.getValueAt(row, 3), 0);
            part.spriteId = toInt(partModel.getValueAt(row, 4), 0);
            data.frames.get(frame).add(part);
        }
        return data;
    }

    private int toInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void ensurePreviewCharacterPartsLoaded() {
        if (!previewPartFrames.isEmpty()) {
            return;
        }
        try (Connection conn = DBConnecter.getConnectionServer();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, data FROM part WHERE id IN (391,392,393)")) {
            while (rs.next()) {
                int partId = rs.getInt("id");
                JsonArray arr = new JsonParser().parse(rs.getString("data")).getAsJsonArray();
                List<CharacterPartFrame> frames = new ArrayList<>();
                for (int i = 0; i < arr.size(); i++) {
                    try {
                        if (arr.get(i).isJsonArray()) {
                            JsonArray frame = arr.get(i).getAsJsonArray();
                            frames.add(new CharacterPartFrame(frame.get(0).getAsInt(), frame.get(1).getAsInt(), frame.get(2).getAsInt()));
                        } else if (arr.get(i).isJsonObject()) {
                            JsonObject frame = arr.get(i).getAsJsonObject();
                            frames.add(new CharacterPartFrame(frame.get("id").getAsInt(), frame.get("dx").getAsInt(), frame.get("dy").getAsInt()));
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (!frames.isEmpty()) {
                    previewPartFrames.put(partId, frames);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private BufferedImage loadPreviewPartImage(int iconId, String zoomText) {
        String key = zoomText + "/" + iconId;
        if (previewPartImageCache.containsKey(key)) {
            return previewPartImageCache.get(key);
        }
        try {
            File file = new File(ICON_DIR + zoomText + "/" + iconId + ".png");
            BufferedImage image = file.exists() ? ImageIO.read(file) : null;
            previewPartImageCache.put(key, image);
            return image;
        } catch (Exception ignored) {
            previewPartImageCache.put(key, null);
            return null;
        }
    }

    private List<CharacterDrawPart> buildPreviewCharacterParts(int baseX, int baseY, int zoom, String zoomText) {
        ensurePreviewCharacterPartsLoaded();
        List<CharacterDrawPart> drawParts = new ArrayList<>();
        addPreviewCharacterPart(drawParts, PREVIEW_LEG_PART, PREVIEW_CHAR_INFO[0][PART_LEG], baseX, baseY, zoom, zoomText);
        addPreviewCharacterPart(drawParts, PREVIEW_BODY_PART, PREVIEW_CHAR_INFO[0][PART_BODY], baseX, baseY, zoom, zoomText);
        addPreviewCharacterPart(drawParts, PREVIEW_HEAD_PART, PREVIEW_CHAR_INFO[0][PART_HEAD], baseX, baseY, zoom, zoomText);
        return drawParts;
    }

    private void addPreviewCharacterPart(List<CharacterDrawPart> drawParts, int partId, int[] clientInfo,
            int baseX, int baseY, int zoom, String zoomText) {
        List<CharacterPartFrame> frames = previewPartFrames.get(partId);
        if (frames == null || frames.isEmpty()) {
            return;
        }
        CharacterPartFrame frame = frames.get(Math.floorMod(clientInfo[CI_FRAME], frames.size()));
        BufferedImage image = loadPreviewPartImage(frame.iconId, zoomText);
        if (image == null) {
            return;
        }
        int x = baseX + (clientInfo[CI_DX] + frame.dx) * zoom;
        int y = baseY - clientInfo[CI_DY] * zoom + frame.dy * zoom;
        drawParts.add(new CharacterDrawPart(image, x, y));
    }

    private Rectangle previewCharacterBounds(List<CharacterDrawPart> drawParts) {
        Rectangle bounds = null;
        for (CharacterDrawPart part : drawParts) {
            Rectangle rect = new Rectangle(part.x, part.y, part.image.getWidth(), part.image.getHeight());
            bounds = bounds == null ? rect : bounds.union(rect);
        }
        return bounds;
    }

    private static int readS16(byte hi, byte lo) {
        int value = ((hi & 0xFF) << 8) | (lo & 0xFF);
        return value >= 32768 ? value - 65536 : value;
    }

    private static void writeS16(ByteArrayOutputStream out, int value) {
        if (value < 0) {
            value = 65536 + value;
        }
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private static void writeU8(ByteArrayOutputStream out, int value, String field) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(field + " phải nằm trong 0..255: " + value);
        }
        out.write(value & 0xFF);
    }

    private static class EffectData {
        int unknown;
        byte[] trailing = new byte[0];
        final List<SpriteRect> sprites = new ArrayList<>();
        final List<List<FramePart>> frames = new ArrayList<>();

        static EffectData read(File file) throws Exception {
            byte[] bytes = Files.readAllBytes(file.toPath());
            if (bytes.length < 3) {
                throw new IllegalArgumentException("File quá ngắn");
            }
            EffectData data = new EffectData();
            int idx = 0;
            int spriteCount = bytes[idx++] & 0xFF;
            for (int i = 0; i < spriteCount; i++) {
                if (idx + 4 >= bytes.length) {
                    throw new IllegalArgumentException("Thiếu sprite rect tại index " + i);
                }
                SpriteRect rect = new SpriteRect();
                rect.id = bytes[idx++] & 0xFF;
                rect.x = bytes[idx++] & 0xFF;
                rect.y = bytes[idx++] & 0xFF;
                rect.w = bytes[idx++] & 0xFF;
                rect.h = bytes[idx++] & 0xFF;
                data.sprites.add(rect);
            }
            data.unknown = bytes[idx++] & 0xFF;
            int frameCount = bytes[idx++] & 0xFF;
            for (int frame = 0; frame < frameCount; frame++) {
                if (idx >= bytes.length) {
                    throw new IllegalArgumentException("Thiếu frame " + frame);
                }
                int partCount = bytes[idx++] & 0xFF;
                List<FramePart> parts = new ArrayList<>();
                for (int part = 0; part < partCount; part++) {
                    if (idx + 4 >= bytes.length) {
                        throw new IllegalArgumentException("Thiếu part " + part + " của frame " + frame);
                    }
                    FramePart fp = new FramePart();
                    fp.x = readS16(bytes[idx++], bytes[idx++]);
                    fp.y = readS16(bytes[idx++], bytes[idx++]);
                    fp.spriteId = bytes[idx++] & 0xFF;
                    parts.add(fp);
                }
                data.frames.add(parts);
            }
            data.trailing = new byte[bytes.length - idx];
            System.arraycopy(bytes, idx, data.trailing, 0, data.trailing.length);
            return data;
        }

        void write(File file) throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeU8(out, sprites.size(), "Số sprite");
            for (SpriteRect rect : sprites) {
                writeU8(out, rect.id, "Sprite id");
                writeU8(out, rect.x, "Sprite x");
                writeU8(out, rect.y, "Sprite y");
                writeU8(out, rect.w, "Sprite w");
                writeU8(out, rect.h, "Sprite h");
            }
            writeU8(out, unknown, "Unknown");
            writeU8(out, frames.size(), "Số frame");
            for (List<FramePart> parts : frames) {
                writeU8(out, parts.size(), "Số part/frame");
                for (FramePart part : parts) {
                    writeS16(out, part.x);
                    writeS16(out, part.y);
                    writeU8(out, part.spriteId, "Part sprite id");
                }
            }
            out.write(trailing);
            Files.write(file.toPath(), out.toByteArray());
        }

        Map<Integer, SpriteRect> spriteMap() {
            Map<Integer, SpriteRect> map = new HashMap<>();
            for (SpriteRect rect : sprites) {
                map.put(rect.id, rect);
            }
            return map;
        }
    }

    private static class SpriteRect {
        int id;
        int x;
        int y;
        int w;
        int h;
    }

    private static class FramePart {
        int x;
        int y;
        int spriteId;
    }

    private static class CharacterPartFrame {
        final int iconId;
        final int dx;
        final int dy;

        CharacterPartFrame(int iconId, int dx, int dy) {
            this.iconId = iconId;
            this.dx = dx;
            this.dy = dy;
        }
    }

    private static class CharacterDrawPart {
        final BufferedImage image;
        final int x;
        final int y;

        CharacterDrawPart(BufferedImage image, int x, int y) {
            this.image = image;
            this.x = x;
            this.y = y;
        }
    }

    private class EffectPreviewPanel extends JPanel {
        private EffectData effect;
        private BufferedImage image;
        private String zoomText = "x1";
        private int zoom = 1;
        private int frameIndex;
        private int dragLastX;
        private int dragLastY;
        private int dragCarryX;
        private int dragCarryY;

        EffectPreviewPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            MouseAdapter dragHandler = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    dragLastX = e.getX();
                    dragLastY = e.getY();
                    dragCarryX = 0;
                    dragCarryY = 0;
                }

                @Override public void mouseDragged(MouseEvent e) {
                    dragCarryX += e.getX() - dragLastX;
                    dragCarryY += e.getY() - dragLastY;
                    dragLastX = e.getX();
                    dragLastY = e.getY();

                    int moveX = dragCarryX / zoom;
                    int moveY = dragCarryY / zoom;
                    if (moveX == 0 && moveY == 0) {
                        return;
                    }
                    dragCarryX -= moveX * zoom;
                    dragCarryY -= moveY * zoom;
                    BadgeDataPanel.this.moveAllParts(moveX, moveY);
                }
            };
            addMouseListener(dragHandler);
            addMouseMotionListener(dragHandler);
        }

        void setData(EffectData effect, BufferedImage image, String zoomText, int zoom, int frameIndex) {
            this.effect = effect;
            this.image = image;
            this.zoomText = zoomText;
            this.zoom = Math.max(1, zoom);
            this.frameIndex = Math.max(0, frameIndex);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int originX = getWidth() / 2;
            int originY = previewOriginY();

            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(0, originY, getWidth(), originY);
            g2.drawLine(originX, 0, originX, getHeight());
            g2.setColor(new Color(180, 0, 0));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(0, originY + zoom, getWidth(), originY + zoom);
            drawGameCharacter(g2, originX, originY + zoom);

            if (effect == null || image == null || frameIndex >= effect.frames.size()) {
                g2.setColor(Color.GRAY);
                g2.drawString("Chưa có preview", 12, 22);
                g2.dispose();
                return;
            }

            Map<Integer, SpriteRect> sprites = effect.spriteMap();
            for (FramePart part : effect.frames.get(frameIndex)) {
                SpriteRect rect = sprites.get(part.spriteId);
                if (rect == null) {
                    continue;
                }
                int sx = rect.x * zoom;
                int sy = rect.y * zoom;
                int sw = rect.w * zoom;
                int sh = rect.h * zoom;
                int dx = originX + part.x * zoom;
                int dy = originY + part.y * zoom;
                if (sx < 0 || sy < 0 || sx >= image.getWidth() || sy >= image.getHeight()) {
                    g2.setColor(Color.RED);
                    g2.drawRect(dx, dy, Math.max(1, sw), Math.max(1, sh));
                    continue;
                }
                int clippedSw = Math.min(sw, image.getWidth() - sx);
                int clippedSh = Math.min(sh, image.getHeight() - sy);
                g2.drawImage(image, dx, dy, dx + clippedSw, dy + clippedSh, sx, sy, sx + clippedSw, sy + clippedSh, null);
                g2.setColor(new Color(0, 120, 215, 120));
                g2.drawRect(dx, dy, Math.max(1, sw), Math.max(1, sh));
            }

            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Frame " + frameIndex + " | zoom x" + zoom + " | đỏ = đỉnh đầu nhân vật", 10, 18);
            g2.dispose();
        }

        private int previewOriginY() {
            int base = Math.max(160, getHeight() - 180);
            int max = Math.max(120, getHeight() - 90);
            int minFrameY = minFrameOffsetY();
            if (minFrameY < 0) {
                base = Math.max(base, 28 - minFrameY * zoom);
            }
            return Math.min(base, max);
        }

        private int minFrameOffsetY() {
            if (effect == null || frameIndex >= effect.frames.size()) {
                return 0;
            }
            int min = 0;
            for (FramePart part : effect.frames.get(frameIndex)) {
                min = Math.min(min, part.y);
            }
            return min;
        }

        private void drawGameCharacter(Graphics2D g2, int centerX, int headTopY) {
            List<CharacterDrawPart> initialParts = buildPreviewCharacterParts(0, 0, zoom, zoomText);
            Rectangle headBounds = previewCharacterHeadBounds(initialParts);
            if (headBounds == null) {
                g2.setColor(Color.GRAY);
                g2.drawString("Không load được part preview 391/392/393", 12, getHeight() - 12);
                return;
            }
            int baseX = centerX - (headBounds.x + headBounds.width / 2);
            int baseY = headTopY - headBounds.y;
            List<CharacterDrawPart> drawParts = buildPreviewCharacterParts(baseX, baseY, zoom, zoomText);
            Rectangle alignedBounds = previewCharacterBounds(drawParts);
            if (alignedBounds != null) {
                g2.setColor(new Color(0, 0, 0, 35));
                g2.fillOval(centerX - 18 * zoom, alignedBounds.y + alignedBounds.height - 4 * zoom,
                        36 * zoom, Math.max(3, 5 * zoom));
            }
            for (CharacterDrawPart part : drawParts) {
                g2.drawImage(part.image, part.x, part.y, null);
            }
        }

        private Rectangle previewCharacterHeadBounds(List<CharacterDrawPart> drawParts) {
            if (drawParts == null || drawParts.isEmpty()) {
                return null;
            }
            CharacterDrawPart head = drawParts.get(drawParts.size() - 1);
            return new Rectangle(head.x, head.y, head.image.getWidth(), head.image.getHeight());
        }

    }
}
