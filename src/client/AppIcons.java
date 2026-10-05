package client;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * Hệ thống Icon & Logo Vector thuần Graphics2D cho IT World
 * KHÔNG dùng Emoji hay ký tự unicode đặc biệt nhằm chống 100% lỗi hiển thị ô vuông (tofu glyph) trên Windows Swing.
 * Tự động scale mượt mà theo kích thước màn hình và độ phân giải cao (HiDPI/Retina).
 */
public class AppIcons {

    /**
     * Tạo hình ảnh Logo ứng dụng IT World (dùng cho Taskbar, Title bar, Dialogs)
     */
    public static BufferedImage getAppLogoImage(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        drawLogo(g2, 0, 0, size);
        g2.dispose();
        return img;
    }

    /**
     * Vẽ Huy hiệu Logo IT World: Nền xanh gradient bo góc hiện đại + Biểu tượng code </ > màu trắng siêu nét
     */
    public static void drawLogo(Graphics2D g2, int x, int y, int size) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float s = size / 64.0f;
        int arc = Math.max(8, (int) (size * 0.32f));

        // 1. Nền Gradient Royal Blue sang Deep Indigo
        GradientPaint gp = new GradientPaint(
                x, y, new Color(59, 130, 246),
                x, y + size, new Color(29, 78, 216)
        );
        g.setPaint(gp);
        g.fillRoundRect(x, y, size, size, arc, arc);

        // 2. Viền phản quang tinh tế
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(Math.max(1.0f, 1.2f * s)));
        g.drawRoundRect(x + 1, y + 1, size - 2, size - 2, arc, arc);

        // 3. Biểu tượng Lập trình viên </ >
        g.setColor(Color.WHITE);
        float strokeW = Math.max(1.8f, 3.6f * s);
        g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        float cx = x + size / 2.0f;
        float cy = y + size / 2.0f;

        // Dấu mở <
        Path2D left = new Path2D.Float();
        left.moveTo(cx - 9.0f * s, cy - 8.5f * s);
        left.lineTo(cx - 17.5f * s, cy);
        left.lineTo(cx - 9.0f * s, cy + 8.5f * s);
        g.draw(left);

        // Dấu gạch chéo /
        g.drawLine(
                (int) (cx - 3.5f * s), (int) (cy + 11.5f * s),
                (int) (cx + 3.5f * s), (int) (cy - 11.5f * s)
        );

        // Dấu đóng >
        Path2D right = new Path2D.Float();
        right.moveTo(cx + 9.0f * s, cy - 8.5f * s);
        right.lineTo(cx + 17.5f * s, cy);
        right.lineTo(cx + 9.0f * s, cy + 8.5f * s);
        g.draw(right);

        g.dispose();
    }

    /**
     * Vẽ Icon cho thanh điều hướng dọc (Navigation Rail)
     */
    public static void drawNavIcon(Graphics2D g2, String tabId, int cx, int cy, Color color) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);

        if ("FEED".equalsIgnoreCase(tabId)) {
            // Icon Tờ báo / Bài viết kỹ thuật
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Khung ngoài
            g.drawRoundRect(cx - 8, cy - 9, 16, 18, 4, 4);
            // Các dòng chữ mô phỏng bài viết
            g.drawLine(cx - 4, cy - 4, cx + 4, cy - 4);
            g.drawLine(cx - 4, cy, cx + 4, cy);
            g.drawLine(cx - 4, cy + 4, cx + 1, cy + 4);
        } else if ("CHAT".equalsIgnoreCase(tabId)) {
            // Icon Bong bóng Chat
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D bubble = new Path2D.Float();
            bubble.moveTo(cx - 8, cy - 7);
            bubble.lineTo(cx + 8, cy - 7);
            bubble.quadTo(cx + 10, cy - 7, cx + 10, cy - 5);
            bubble.lineTo(cx + 10, cy + 2);
            bubble.quadTo(cx + 10, cy + 4, cx + 8, cy + 4);
            bubble.lineTo(cx - 2, cy + 4);
            bubble.lineTo(cx - 6, cy + 8);
            bubble.lineTo(cx - 6, cy + 4);
            bubble.lineTo(cx - 8, cy + 4);
            bubble.quadTo(cx - 10, cy + 4, cx - 10, cy + 2);
            bubble.lineTo(cx - 10, cy - 5);
            bubble.quadTo(cx - 10, cy - 7, cx - 8, cy - 7);
            bubble.closePath();
            g.draw(bubble);

            // Hai chấm tin nhắn bên trong
            g.fillOval(cx - 4, cy - 2, 2, 2);
            g.fillOval(cx + 2, cy - 2, 2, 2);
        } else if ("NETWORK".equalsIgnoreCase(tabId)) {
            // Icon Địa cầu / Mạng lưới lập trình viên
            g.setStroke(new BasicStroke(1.5f));
            // Vòng tròn địa cầu
            g.drawOval(cx - 9, cy - 9, 18, 18);
            // Đường xích đạo ngang
            g.drawLine(cx - 9, cy, cx + 9, cy);
            // Đường kinh tuyến dọc bo tròn
            g.drawOval(cx - 4, cy - 9, 8, 18);
        } else if ("PROFILE".equalsIgnoreCase(tabId)) {
            // Icon Người dùng / Trang cá nhân
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Đầu tròn
            g.drawOval(cx - 4, cy - 9, 8, 8);
            // Thân / Vai vòng cung
            Path2D shoulders = new Path2D.Float();
            shoulders.moveTo(cx - 8, cy + 8);
            shoulders.curveTo(cx - 8, cy + 2, cx + 8, cy + 2, cx + 8, cy + 8);
            g.draw(shoulders);
        }

        g.dispose();
    }

    /**
     * Vẽ Icon nhỏ trong các ô nhập liệu (ModernInput)
     */
    public static void drawInputIcon(Graphics2D g2, String type, int x, int y, Color color) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if ("user".equalsIgnoreCase(type)) {
            // Đầu tròn
            g.drawOval(x + 3, y - 6, 7, 7);
            // Vai
            Path2D shoulders = new Path2D.Float();
            shoulders.moveTo(x, y + 8);
            shoulders.curveTo(x, y + 3, x + 13, y + 3, x + 13, y + 8);
            g.draw(shoulders);
        } else if ("lock".equalsIgnoreCase(type)) {
            // Quai khóa
            g.drawArc(x + 3, y - 6, 8, 8, 0, 180);
            // Thân khóa
            g.drawRoundRect(x + 1, y - 1, 12, 10, 3, 3);
            // Lỗ khóa nhỏ
            g.fillOval(x + 6, y + 2, 2, 3);
        } else if ("name".equalsIgnoreCase(type) || "edit".equalsIgnoreCase(type)) {
            // Biểu tượng chiếc thẻ / bút viết
            g.drawRoundRect(x + 1, y - 6, 12, 15, 3, 3);
            g.drawLine(x + 4, y - 2, x + 10, y - 2);
            g.drawLine(x + 4, y + 2, x + 10, y + 2);
            g.drawLine(x + 4, y + 5, x + 7, y + 5);
        } else if ("search".equalsIgnoreCase(type)) {
            // Kính lúp
            g.drawOval(x + 1, y - 5, 8, 8);
            g.drawLine(x + 8, y + 2, x + 12, y + 6);
        }

        g.dispose();
    }

    /**
     * Vẽ Icon trái tim Vector (Like button)
     */
    public static void drawHeart(Graphics2D g2, int x, int y, int size, boolean filled, Color color) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);

        float s = size / 16.0f;
        Path2D heart = new Path2D.Float();
        heart.moveTo(x + 8.0f * s, y + 13.5f * s);
        heart.curveTo(x + 1.0f * s, y + 9.0f * s, x, y + 4.5f * s, x + 4.0f * s, y + 2.0f * s);
        heart.curveTo(x + 6.5f * s, y + 0.5f * s, x + 8.0f * s, y + 3.0f * s, x + 8.0f * s, y + 3.0f * s);
        heart.curveTo(x + 8.0f * s, y + 3.0f * s, x + 9.5f * s, y + 0.5f * s, x + 12.0f * s, y + 2.0f * s);
        heart.curveTo(x + 16.0f * s, y + 4.5f * s, x + 15.0f * s, y + 9.0f * s, x + 8.0f * s, y + 13.5f * s);
        heart.closePath();

        if (filled) {
            g.fill(heart);
        } else {
            g.setStroke(new BasicStroke(1.4f));
            g.draw(heart);
        }

        g.dispose();
    }
}
