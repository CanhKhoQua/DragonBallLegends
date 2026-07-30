package nro.server.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class ServerGuiUtils {

    public static void setupTheme() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.width", 10);
        } catch (Exception e) {
            System.err.println("Failed to initialize LaF: " + e.getMessage());
        }
    }

    public static TitledBorder createSectionBorder(String title) {
        return BorderFactory.createTitledBorder(
            new LineBorder(new Color(220, 220, 220)), title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY
        );
    }

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel createStyledLabel(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        return l;
    }

    public static Icon loadIcon(String path) {
        try {
            URL url = ServerGuiUtils.class.getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            } 
        } catch (Exception e) {
        }
        return createFallbackIcon(path);
    }

    private static Icon createFallbackIcon(String path) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);

                String p = path.toLowerCase();

                if (p.contains("dashboard")) {
                    g2.setColor(new Color(0, 120, 215)); 
                    g2.fill(new Rectangle2D.Double(2, 2, 7, 7));
                    g2.fill(new Rectangle2D.Double(11, 2, 7, 7));
                    g2.fill(new Rectangle2D.Double(2, 11, 7, 7));
                    g2.fill(new Rectangle2D.Double(11, 11, 7, 7));
                } 
                // --- ĐÃ CHỈNH SỬA: Tách riêng User/Player ---
                else if (p.contains("user") || p.contains("player")) {
                    g2.setColor(new Color(0, 153, 51)); // Màu xanh lá
                    g2.fill(new Ellipse2D.Double(5, 2, 10, 10)); // Đầu
                    g2.fill(new Arc2D.Double(2, 12, 16, 8, 0, 180, Arc2D.CHORD)); // Thân
                }
                // --- ĐÃ CHỈNH SỬA: Icon mới cho Account (Dạng thẻ ID) ---
                else if (p.contains("account")) {
                    g2.setColor(new Color(65, 105, 225)); // Màu xanh hoàng gia
                    // Khung thẻ
                    g2.fill(new RoundRectangle2D.Double(3, 5, 14, 10, 2, 2));
                    // Ảnh đại diện nhỏ
                    g2.setColor(new Color(173, 216, 230)); // Xanh nhạt
                    g2.fill(new Ellipse2D.Double(5, 7, 5, 5));
                    // Dòng chữ giả
                    g2.setColor(Color.WHITE);
                    g2.fill(new Rectangle2D.Double(11, 8, 4, 2));
                    g2.fill(new Rectangle2D.Double(11, 11, 3, 2));
                }
                else if (p.contains("shop")) { 
                    g2.setColor(new Color(255, 87, 34)); 
                    Path2D roof = new Path2D.Double();
                    roof.moveTo(2, 4);  
                    roof.lineTo(18, 4); 
                    roof.lineTo(20, 9); 
                    roof.lineTo(0, 9);  
                    roof.closePath();
                    g2.fill(roof);
                    g2.setColor(new Color(255, 255, 255, 100)); 
                    g2.fillRect(5, 4, 2, 5);
                    g2.fillRect(10, 4, 2, 5);
                    g2.fillRect(15, 4, 2, 5);
                    g2.setColor(new Color(240, 230, 140)); 
                    g2.fillRect(3, 9, 14, 9);
                    g2.setColor(new Color(101, 67, 33));
                    g2.fillRect(8, 11, 4, 7);
                    g2.setColor(new Color(135, 206, 250));
                    g2.fillRect(4, 11, 3, 4);
                    g2.fillRect(13, 11, 3, 4);
                }
                else if (p.contains("gift")) {
                    g2.setColor(new Color(255, 69, 58)); 
                    g2.fill(new Rectangle2D.Double(3, 6, 14, 12));
                    g2.setColor(new Color(200, 30, 30)); 
                    g2.fill(new Rectangle2D.Double(2, 4, 16, 4));
                    g2.setColor(new Color(255, 215, 0)); 
                    g2.fill(new Rectangle2D.Double(8, 4, 4, 14));
                    g2.fill(new Ellipse2D.Double(6, 1, 4, 4)); 
                    g2.fill(new Ellipse2D.Double(10, 1, 4, 4)); 
                }
                // --- ICON TOPUP REWARD ---
                else if (p.contains("topup") || p.contains("reward")) {
                    // Thân thẻ
                    g2.setColor(new Color(102, 102, 255)); // Màu xanh tím
                    g2.fill(new RoundRectangle2D.Double(2, 5, 16, 10, 3, 3));
                    // Dải băng từ hoặc chip
                    g2.setColor(new Color(255, 215, 0)); // Màu vàng chip
                    g2.fill(new Rectangle2D.Double(4, 8, 4, 3));
                    // Các vạch giả số thẻ
                    g2.setColor(new Color(255, 255, 255, 150));
                    g2.fill(new Rectangle2D.Double(4, 12, 12, 1));
                }
                else if (p.contains("shield") || p.contains("security")) { 
                    g2.setColor(new Color(220, 53, 69)); 
                    Path2D pPath = new Path2D.Double();
                    pPath.moveTo(10, 1);
                    pPath.lineTo(18, 4);
                    pPath.lineTo(18, 10);
                    pPath.curveTo(18, 16, 10, 19, 10, 19);
                    pPath.curveTo(10, 19, 2, 16, 2, 10);
                    pPath.lineTo(2, 4);
                    pPath.closePath();
                    g2.fill(pPath);
                }
                else if (p.contains("firewall")) {
                    g2.setColor(new Color(255, 69, 0));
                    g2.fill(new Rectangle2D.Double(2, 4, 16, 12));
                    g2.setColor(Color.WHITE);
                    g2.fill(new Rectangle2D.Double(4, 6, 2, 2));
                    g2.fill(new Rectangle2D.Double(8, 6, 2, 2));
                    g2.fill(new Rectangle2D.Double(12, 6, 2, 2));
                }
                else if (p.contains("calendar") || p.contains("event")) {
                    g2.setColor(new Color(102, 51, 153));
                    g2.fill(new Rectangle2D.Double(3, 4, 14, 13));
                    g2.setColor(Color.WHITE);
                    g2.fillRect(3, 4, 14, 4);
                }
                else if (p.contains("monster") || p.contains("boss")) {
                    g2.setColor(Color.DARK_GRAY); 
                    g2.fill(new Ellipse2D.Double(2, 2, 16, 16));
                    g2.setColor(Color.RED);
                    g2.fill(new Ellipse2D.Double(6, 7, 3, 3));
                    g2.fill(new Ellipse2D.Double(11, 7, 3, 3));
                }
                else if (p.contains("trade")) {
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(new Color(0, 150, 136));
                    g2.drawLine(4, 7, 15, 7);
                    g2.drawLine(12, 4, 15, 7);
                    g2.drawLine(12, 10, 15, 7);
                    g2.setColor(new Color(255, 152, 0));
                    g2.drawLine(16, 13, 5, 13);
                    g2.drawLine(8, 10, 5, 13);
                    g2.drawLine(8, 16, 5, 13);
                }
                else if (p.contains("broadcast")) {
                    g2.setColor(new Color(23, 162, 184));
                    Path2D horn = new Path2D.Double();
                    horn.moveTo(3, 9);
                    horn.lineTo(12, 5);
                    horn.lineTo(12, 15);
                    horn.lineTo(3, 11);
                    horn.closePath();
                    g2.fill(horn);
                    g2.fill(new Rectangle2D.Double(3, 8, 3, 4));
                    g2.setStroke(new BasicStroke(1.6f));
                    g2.draw(new Arc2D.Double(11, 6, 7, 8, -45, 90, Arc2D.OPEN));
                    g2.draw(new Arc2D.Double(10, 3, 10, 14, -45, 90, Arc2D.OPEN));
                }
                else if (p.contains("guide")) {
                    g2.setColor(new Color(111, 66, 193));
                    g2.fill(new RoundRectangle2D.Double(4, 2, 12, 16, 2, 2));
                    g2.setColor(Color.WHITE);
                    g2.fill(new Rectangle2D.Double(7, 6, 7, 1.5));
                    g2.fill(new Rectangle2D.Double(7, 10, 6, 1.5));
                    g2.fill(new Rectangle2D.Double(7, 14, 4, 1.5));
                }
                else if (p.contains("config")) {
                    g2.setColor(new Color(96, 110, 125));
                    g2.fill(new Ellipse2D.Double(5, 5, 10, 10));
                    g2.setColor(Color.WHITE);
                    g2.fill(new Ellipse2D.Double(8, 8, 4, 4));
                    g2.setColor(new Color(96, 110, 125));
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI * 2 * i / 8.0;
                        int cx = 10 + (int) Math.round(Math.cos(a) * 7);
                        int cy = 10 + (int) Math.round(Math.sin(a) * 7);
                        g2.fill(new Rectangle2D.Double(cx - 1, cy - 1, 2, 2));
                    }
                }
                else if (p.contains("schedule")) {
                    g2.setColor(new Color(255, 193, 7));
                    g2.fill(new Ellipse2D.Double(3, 3, 14, 14));
                    g2.setColor(new Color(90, 70, 20));
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(10, 10, 10, 6);
                    g2.drawLine(10, 10, 14, 12);
                }
                else if (p.contains("map")) {
                    g2.setColor(new Color(76, 175, 80));
                    Path2D pin = new Path2D.Double();
                    pin.moveTo(10, 18);
                    pin.curveTo(5, 12, 4, 9, 4, 7);
                    pin.curveTo(4, 3, 7, 1, 10, 1);
                    pin.curveTo(13, 1, 16, 3, 16, 7);
                    pin.curveTo(16, 9, 15, 12, 10, 18);
                    pin.closePath();
                    g2.fill(pin);
                    g2.setColor(Color.WHITE);
                    g2.fill(new Ellipse2D.Double(7, 5, 6, 6));
                }
                else if (p.contains("item")) {
                    g2.setColor(new Color(121, 85, 72));
                    g2.fill(new RoundRectangle2D.Double(3, 6, 14, 11, 2, 2));
                    g2.setColor(new Color(255, 215, 0));
                    g2.fill(new Rectangle2D.Double(8, 6, 4, 11));
                    g2.fill(new Rectangle2D.Double(3, 10, 14, 3));
                    g2.setColor(new Color(93, 64, 55));
                    g2.draw(new RoundRectangle2D.Double(3, 6, 14, 11, 2, 2));
                }
                else if (p.contains("badge")) {
                    g2.setColor(new Color(255, 152, 0));
                    g2.fill(new Ellipse2D.Double(4, 2, 12, 12));
                    g2.setColor(new Color(255, 215, 0));
                    Path2D star = new Path2D.Double();
                    star.moveTo(10, 4);
                    star.lineTo(11.5, 8);
                    star.lineTo(15.5, 8);
                    star.lineTo(12.3, 10.4);
                    star.lineTo(13.5, 14);
                    star.lineTo(10, 11.8);
                    star.lineTo(6.5, 14);
                    star.lineTo(7.7, 10.4);
                    star.lineTo(4.5, 8);
                    star.lineTo(8.5, 8);
                    star.closePath();
                    g2.fill(star);
                    g2.setColor(new Color(220, 53, 69));
                    g2.fill(new Rectangle2D.Double(6, 13, 3, 6));
                    g2.fill(new Rectangle2D.Double(11, 13, 3, 6));
                }
                else {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fill(new Ellipse2D.Double(4, 4, 12, 12));
                }

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 20; }

            @Override
            public int getIconHeight() { return 20; }
        };
    }
}
