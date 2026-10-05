package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Map;

public class UITheme {
    // Bảng màu hiện đại chuẩn Messenger / Telegram
    public static final Color PRIMARY = new Color(37, 99, 235);       // #2563EB
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216); // #1D4ED8
    public static final Color PRIMARY_LIGHT = new Color(239, 246, 255);// #EFF6FF
    public static final Color BG_SIDEBAR = new Color(255, 255, 255);
    public static final Color BG_CHAT = new Color(241, 245, 249);     // #F1F5F9 (Độ tương phản cao)
    public static final Color BORDER = new Color(226, 232, 240);       // #E2E8F0
    public static final Color TEXT_MAIN = new Color(15, 23, 42);       // #0F172A
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // #64748B
    public static final Color ONLINE = new Color(34, 197, 94);        // #22C55E
    public static final Color BUBBLE_ME = new Color(37, 99, 235);
    public static final Color BUBBLE_OTHER = new Color(255, 255, 255);

    private static final Color[] AVATAR_PALETTE = {
            new Color(239, 68, 68), new Color(249, 115, 22), new Color(245, 158, 11),
            new Color(16, 185, 129), new Color(6, 182, 212), new Color(59, 130, 246),
            new Color(99, 102, 241), new Color(168, 85, 247), new Color(236, 72, 153)
    };

    public static Color getAvatarColor(String name) {
        if (name == null || name.isEmpty()) return PRIMARY;
        return AVATAR_PALETTE[Math.abs(name.hashCode()) % AVATAR_PALETTE.length];
    }

    // SỬA LỖI: Lọc bỏ hoàn toàn dấu ngoặc đơn để không còn bị "A("
    public static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String clean = name.replaceAll("[\\(\\)\\[\\]\\{\\}]", "").trim();
        String[] parts = clean.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            String first = parts[0].substring(0, 1);
            String last = parts[parts.length - 1].substring(0, 1);
            return (first + last).toUpperCase();
        }
    }
}

// ================= CÁC COMPONENT GIAO DIỆN NÂNG CẤP VECTOR =================

class AvatarPanel extends JPanel {
    private String userName;
    private int size;
    private boolean showOnlineDot;

    public AvatarPanel(String userName, int size, boolean showOnlineDot) {
        this.userName = userName;
        this.size = size;
        this.showOnlineDot = showOnlineDot;
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        setOpaque(false);
    }

    public void setUserName(String name) {
        this.userName = name;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Nền tròn
        g2.setColor(UITheme.getAvatarColor(userName));
        g2.fillOval(0, 0, size, size);

        // Chữ viết tắt chuẩn (không dấu ngoặc)
        String initials = UITheme.getInitials(userName);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, size * 2 / 5)));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (size - fm.stringWidth(initials)) / 2;
        int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(initials, tx, ty);

        // Chấm xanh online có viền trắng bọc quanh
        if (showOnlineDot) {
            int dotSize = Math.max(9, size / 4);
            int dotX = size - dotSize - 1;
            int dotY = size - dotSize - 1;
            g2.setColor(Color.WHITE);
            g2.fillOval(dotX - 2, dotY - 2, dotSize + 4, dotSize + 4);
            g2.setColor(UITheme.ONLINE);
            g2.fillOval(dotX, dotY, dotSize, dotSize);
        }
        g2.dispose();
    }
}

/**
 * Nút Icon hình tròn hoàn hảo, vẽ Vector kẹp ghim & thùng rác siêu nét
 */
class IconButton extends JButton {
    private String type; // "clip" hoặc "trash"

    public IconButton(String type, String tooltip) {
        this.type = type;
        setToolTipText(tooltip);
        setPreferredSize(new Dimension(36, 36));
        setMinimumSize(new Dimension(36, 36));
        setMaximumSize(new Dimension(36, 36));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Khung tròn nổi bật
        if (getModel().isPressed()) {
            g2.setColor(new Color(226, 232, 240));
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(239, 246, 255));
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.fillOval(2, 2, w - 4, h - 4);

        // 2. Viền tròn
        g2.setColor(getModel().isRollover() ? UITheme.PRIMARY : new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(2, 2, w - 5, h - 5);

        // 3. Vẽ Icon Vector sắc nét (Không dùng Emoji)
        Color iconColor = getModel().isRollover() ? UITheme.PRIMARY : new Color(100, 116, 139);
        g2.setColor(iconColor);

        int cx = w / 2;
        int cy = h / 2;

        if (type.contains("clip")) {
            // Vẽ Kẹp ghim Vector thanh lịch
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D clip = new Path2D.Double();
            clip.moveTo(cx - 3, cy + 4);
            clip.lineTo(cx - 3, cy - 3);
            clip.quadTo(cx - 3, cy - 7, cx, cy - 7);
            clip.quadTo(cx + 4, cy - 7, cx + 4, cy - 3);
            clip.lineTo(cx + 4, cy + 5);
            clip.quadTo(cx + 4, cy + 8, cx + 1, cy + 8);
            clip.quadTo(cx - 1, cy + 8, cx - 1, cy + 5);
            clip.lineTo(cx - 1, cy - 1);
            g2.draw(clip);
        } else {
            // Vẽ Thùng rác Vector
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(cx - 6, cy - 5, cx + 6, cy - 5); // Nắp
            g2.drawLine(cx - 2, cy - 7, cx + 2, cy - 7); // Quai
            g2.drawRoundRect(cx - 5, cy - 4, 10, 12, 3, 3); // Thân thùng
            g2.drawLine(cx - 2, cy - 2, cx - 2, cy + 5);
            g2.drawLine(cx + 2, cy - 2, cx + 2, cy + 5);
        }

        g2.dispose();
    }
}

/**
 * Nút Gửi tròn Vector màu xanh Royal Blue
 */
class SendButton extends JButton {
    private boolean hasText = false;

    public SendButton() {
        setPreferredSize(new Dimension(38, 38));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setHasText(boolean hasText) {
        this.hasText = hasText;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Nền nút tròn
        if (hasText) {
            g2.setColor(getModel().isRollover() ? UITheme.PRIMARY_HOVER : UITheme.PRIMARY);
        } else {
            g2.setColor(new Color(226, 232, 240));
        }
        g2.fillOval(2, 2, w - 4, h - 4);

        // Mũi tên vector
        g2.setColor(hasText ? Color.WHITE : new Color(148, 163, 184));
        int cx = w / 2;
        int cy = h / 2;
        Polygon arrow = new Polygon();
        arrow.addPoint(cx - 5, cy - 8);
        arrow.addPoint(cx + 8, cy);
        arrow.addPoint(cx - 5, cy + 8);
        arrow.addPoint(cx - 2, cy);
        g2.fillPolygon(arrow);

        g2.dispose();
    }
}

class ModernScrollBarUI extends BasicScrollBarUI {
    @Override
    protected JButton createDecreaseButton(int orientation) { return createZero(); }
    @Override
    protected JButton createIncreaseButton(int orientation) { return createZero(); }
    private JButton createZero() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        return b;
    }
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {}
    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isThumbRollover() ? new Color(148, 163, 184) : new Color(203, 213, 225));
        g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 2, 5, thumbBounds.height - 4, 5, 5);
        g2.dispose();
    }
}

class ModernUserCellRenderer extends JPanel implements ListCellRenderer<String> {
    private AvatarPanel avatarPanel;
    private JLabel lblName, lblTime, lblSnippet;
    private boolean isSelected;
    private Map<String, String> lastMessages;
    private Map<String, String> lastMessageTimes;

    public ModernUserCellRenderer(Map<String, String> lastMessages, Map<String, String> lastMessageTimes) {
        this.lastMessages = lastMessages;
        this.lastMessageTimes = lastMessageTimes;

        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(10, 16, 10, 16));
        setOpaque(true);

        avatarPanel = new AvatarPanel("", 44, true);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        textPanel.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        lblName = new JLabel();
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        topRow.add(lblName, BorderLayout.WEST);
        topRow.add(lblTime, BorderLayout.EAST);

        lblSnippet = new JLabel();
        lblSnippet.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        textPanel.add(topRow);
        textPanel.add(lblSnippet);

        add(avatarPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        this.isSelected = isSelected;
        lblName.setText(value);
        avatarPanel.setUserName(value);

        String snippet = lastMessages != null ? lastMessages.getOrDefault(value, "Nhấn để bắt đầu trò chuyện...") : "";
        if (snippet.length() > 28) snippet = snippet.substring(0, 25) + "...";
        lblSnippet.setText(snippet);

        String time = lastMessageTimes != null ? lastMessageTimes.getOrDefault(value, "") : "";
        lblTime.setText(time);

        if (isSelected) {
            setBackground(UITheme.PRIMARY_LIGHT);
            lblName.setForeground(UITheme.PRIMARY);
            lblTime.setForeground(UITheme.PRIMARY);
            lblSnippet.setForeground(new Color(71, 85, 105));
        } else {
            setBackground(Color.WHITE);
            lblName.setForeground(UITheme.TEXT_MAIN);
            lblTime.setForeground(UITheme.TEXT_MUTED);
            lblSnippet.setForeground(UITheme.TEXT_MUTED);
        }
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(UITheme.PRIMARY);
            g2.fillRect(0, 0, 4, getHeight());
            g2.dispose();
        }
    }
}