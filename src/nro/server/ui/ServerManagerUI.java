package nro.server.ui;

import nro.server.ServerManager;
import nro.server.AutoSaveManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ServerManagerUI extends JFrame {

    // --- Class nội bộ quản lý Sidebar Item ---
    private static class NavItem {

        String name;
        Icon icon;
        String key;

        public NavItem(String name, String iconPath, String key) {
            this.name = name;
            this.key = key;
            this.icon = ServerGuiUtils.loadIcon(iconPath);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Instant serverStartTime;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JList<NavItem> sidebar;
    private final Map<String, Supplier<JPanel>> panelFactories = new HashMap<>();
    private final Set<String> loadedPanels = new HashSet<>();

    public static volatile boolean REQUEST_AUTO_RESTART = false;

    public ServerManagerUI() {
        super("Server Control Panel - Manager");

        // Setup giao diện FlatLaf cho hiện đại (nếu có thư viện)
        ServerGuiUtils.setupTheme();

        initUI();

        this.serverStartTime = Instant.now();

        // Hook tắt server an toàn
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (REQUEST_AUTO_RESTART) {
                triggerRestartProcess();
            }
        }));
    }

    // --- Logic Restart Server ---
    public void triggerRestartProcess() {
        int seconds = 5;
        System.out.println(">>> Restarting Server in " + seconds + "s...");

        try {
            String currentDir = System.getProperty("user.dir");
            String osName = System.getProperty("os.name").toLowerCase();

            ProcessBuilder pb;
            if (osName.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", "cmd", "/c", "timeout /t " + seconds + " /nobreak && run.bat");
            } else {
                pb = new ProcessBuilder("bash", "-c", "sleep " + seconds + "; ./run.sh &");
            }

            pb.directory(new File(currentDir));
            pb.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Khởi tạo Giao diện ---
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245)); // Màu nền tổng thể sáng nhẹ

        // Danh sách Menu
        NavItem[] menuItems = {
            new NavItem("Dashboard",       "/icon/dashboard.png", "Dashboard"),
            new NavItem("Account",         "/icon/Account.png",   "Account"),
            new NavItem("Players",         "/icon/user2.png",     "Players"),
            new NavItem("Shop Items",      "/icon/shop.png",      "ShopEditor"),
            new NavItem("Giftcode",        "/icon/gift.png",      "Giftcode"),
            new NavItem("Topup Reward",    "/icon/topup.png",     "TopupReward"),
            new NavItem("Events",          "/icon/calendar.png",  "Events"),
            new NavItem("Boss Config",     "/icon/monster.png",   "Boss Config"),
            new NavItem("Security",        "/icon/shield.png",    "Security"),
            new NavItem("Giao Dịch Log",   "/icon/trade-log.png", "TransactionLog"),
            new NavItem("Thông Báo",       "/icon/broadcast.png", "Broadcast"),
            new NavItem("Hướng Dẫn",       "/icon/guide.png",     "NotifyEditor"),
            new NavItem("Cấu Hình",        "/icon/config.png",    "ConfigEditor"),
            new NavItem("Lịch Hẹn",        "/icon/schedule.png",  "ScheduledActions"),
            new NavItem("Bản Đồ",          "/icon/map.png",       "MapData"),
            new NavItem("Vật Phẩm",        "/icon/item.png",      "ItemData"),
            new NavItem("Danh Hiệu",       "/icon/badge.png",     "BadgeData")
        };

        // Cấu hình Sidebar (JList)
        sidebar = new JList<>(menuItems);
        sidebar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sidebar.setSelectedIndex(0);
        sidebar.setFixedCellHeight(55); // Tăng chiều cao mỗi dòng
        sidebar.setBackground(new Color(255, 255, 255));
        sidebar.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Custom Renderer cho Sidebar đẹp hơn
        sidebar.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof NavItem) {
                    NavItem item = (NavItem) value;
                    lbl.setText(item.name);
                    if (item.icon != null) {
                        lbl.setIcon(item.icon);
                    }
                }

                lbl.setBorder(new EmptyBorder(0, 20, 0, 0)); // Padding trái
                lbl.setIconTextGap(15);
                lbl.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 14));

                if (isSelected) {
                    lbl.setBackground(new Color(230, 242, 255)); // Màu nền khi chọn (Xanh nhạt)
                    lbl.setForeground(new Color(0, 102, 204));   // Màu chữ khi chọn (Xanh đậm)
                    // Thêm vạch màu bên trái để đánh dấu
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0, 120, 215)),
                            new EmptyBorder(0, 16, 0, 0)
                    ));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(60, 60, 60));
                }
                return lbl;
            }
        });

        // Sidebar Container
        JScrollPane scrollSidebar = new JScrollPane(sidebar);
        scrollSidebar.setPreferredSize(new Dimension(260, getHeight())); // Rộng hơn chút
        scrollSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220))); // Viền phải nhẹ
        add(scrollSidebar, BorderLayout.WEST);

        // Content Panel (Chứa các màn hình chức năng)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); // Không viền thừa

        registerPanels();
        contentPanel.add(createStartupPanel(), "Startup");
        loadedPanels.add("Startup");
        cardLayout.show(contentPanel, "Startup");

        add(contentPanel, BorderLayout.CENTER);

        // Xử lý chuyển tab khi click sidebar
        sidebar.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                NavItem selected = sidebar.getSelectedValue();
                if (selected != null) {
                    showPanel(selected.key);
                }
            }
        });
        showDashboardAfterFirstPaint();

        // Cấu hình cửa sổ chính
        setSize(1300, 850);
        setMinimumSize(new Dimension(1150, 750));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Sự kiện đóng cửa sổ an toàn
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        ServerManagerUI.this,
                        "Bạn có chắc muốn dừng Server và thoát chương trình?",
                        "Xác nhận tắt Server",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        shutdownServer();
                    } catch (Throwable ex) {
                        System.err.println("Lỗi khi shutdown từ cửa sổ: " + ex.getMessage());
                        System.exit(0);
                    }
                }
            }
        });
    }

    private void registerPanels() {
        panelFactories.put("Dashboard", DashboardPanel::new);
        panelFactories.put("Account", AccountPanel::new);
        panelFactories.put("Players", PlayersPanel::new);
        panelFactories.put("ShopEditor", ShopEditorPanel::new);
        panelFactories.put("Giftcode", GiftcodePanel::new);
        panelFactories.put("TopupReward", TopupRewardPanel::new);
        panelFactories.put("Events", EventPanel::new);
        panelFactories.put("Boss Config", BossEditorPanel::new);
        panelFactories.put("Security", SecurityPanel::new);
        panelFactories.put("TransactionLog", TransactionLogPanel::new);
        panelFactories.put("Broadcast", BroadcastPanel::new);
        panelFactories.put("NotifyEditor", NotifyEditorPanel::new);
        panelFactories.put("ConfigEditor", ConfigEditorPanel::new);
        panelFactories.put("ScheduledActions", ScheduledActionsPanel::new);
        panelFactories.put("MapData", MapDataPanel::new);
        panelFactories.put("ItemData", ItemDataPanel::new);
        panelFactories.put("BadgeData", BadgeDataPanel::new);
    }

    private JPanel createStartupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Server Control Panel", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 28));
        label.setForeground(new Color(0, 102, 204));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void showDashboardAfterFirstPaint() {
        Timer timer = new Timer(150, e -> {
            NavItem selected = sidebar.getSelectedValue();
            if (selected != null && "Dashboard".equals(selected.key)) {
                showPanel("Dashboard");
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showPanel(String key) {
        if (!loadedPanels.contains(key)) {
            Supplier<JPanel> factory = panelFactories.get(key);
            if (factory == null) {
                return;
            }
            contentPanel.add(factory.get(), key);
            loadedPanels.add(key);
        }
        cardLayout.show(contentPanel, key);
    }

    public void startServerProcesses() {
        System.out.println(">> [ServerManagerUI] Starting Server Engine...");
        new Thread(() -> {
            services.top.TopAutoService.gI().activeAuto();
            ServerManager.gI().run();
            EventQueue.invokeLater(() -> setVisible(true));
        }, "Server-Engine-Starter").start();
    }

    private void shutdownServer() {
        System.out.println(">> Đang lưu dữ liệu và đóng kết nối...");
        try {
            Class<?> proxyManagerClass = Class.forName("firewall.ProxyManager");
            Object proxyManager = proxyManagerClass.getMethod("getInstance").invoke(null);
            if (proxyManager != null) {
                proxyManagerClass.getMethod("stopAll").invoke(proxyManager);
            }
        } catch (Throwable e) {
            System.err.println("Không đóng được ProxyManager: " + e.getMessage());
        }

        try {
            if (AutoSaveManager.getInstance() != null) {
                AutoSaveManager.getInstance().stopAutoSave();
            }
        } catch (Throwable e) {
            System.err.println("Không đóng được AutoSaveManager: " + e.getMessage());
        }

        System.out.println(">> Server shutting down... Bye!");
        System.exit(0);
    }

    public static void main(String[] args) {
        // Chạy trên luồng giao diện chuẩn Swing
        EventQueue.invokeLater(() -> {
            ServerManagerUI ui = new ServerManagerUI();
            ui.setVisible(true);
            ui.startServerProcesses();
        });
    }
}
