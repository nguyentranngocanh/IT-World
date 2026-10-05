package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class MessageBubbleFactory {

    public static JPanel createBubble(ChatMessage msg, Component parent) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(3, 16, 3, 16));

        // 1. Tin nhắn hệ thống (Pill xám ở giữa)
        if (msg.isSystem() && !msg.isFile()) {
            JPanel pill = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(226, 232, 240, 210));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            JLabel lbl = new JLabel(msg.getText().replaceAll("<[^>]*>", ""));
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(UITheme.TEXT_MUTED);
            pill.add(lbl);

            JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
            center.setOpaque(false);
            center.add(pill);
            row.add(center, BorderLayout.CENTER);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            return row;
        }

        // 2. Thẻ gửi/nhận File (CẢ 2 PHÍA ĐỀU HIỆN ĐÚNG AVATAR TÀI KHOẢN)
        if (msg.isFile()) {
            JPanel fileCard = createFileCard(msg, parent);
            if (msg.isMe()) {
                // Bên bạn gửi đi: Thẻ file + Avatar của chính bạn bên phải
                JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                rightBox.setOpaque(false);
                rightBox.add(fileCard);
                rightBox.add(new AvatarPanel(msg.getSender(), 32, false));
                row.add(rightBox, BorderLayout.EAST);
            } else {
                // Bên bạn nhận về: Avatar của người gửi + Thẻ file bên trái
                JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
                leftBox.setOpaque(false);
                leftBox.add(new AvatarPanel(msg.getSender(), 32, false));
                leftBox.add(fileCard);
                row.add(leftBox, BorderLayout.WEST);
            }
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            return row;
        }

        // 3. Bong bóng tin nhắn văn bản thông thường
        JPanel bubble = new JPanel(new BorderLayout(8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 18;
                if (msg.isMe()) {
                    g2.setColor(UITheme.BUBBLE_ME);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                } else {
                    g2.setColor(UITheme.BUBBLE_OTHER);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                    g2.setColor(UITheme.BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel lblText = new JLabel();
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblText.setForeground(msg.isMe() ? Color.WHITE : UITheme.TEXT_MAIN);

        if (msg.getText().length() > 36 || msg.getText().contains("\n")) {
            String safeText = msg.getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>");
            lblText.setText("<html><body style='width: 320px;'>" + safeText + "</body></html>");
        } else {
            lblText.setText(msg.getText());
        }
        bubble.add(lblText, BorderLayout.CENTER);

        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        metaPanel.setOpaque(false);

        JLabel lblTime = new JLabel(msg.getTime());
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTime.setForeground(msg.isMe() ? new Color(219, 234, 254) : UITheme.TEXT_MUTED);
        metaPanel.add(lblTime);

        if (msg.isMe()) {
            JComponent checkMarks = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(191, 219, 254));
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(1, 6, 4, 9);
                    g2.drawLine(4, 9, 8, 3);
                    g2.drawLine(5, 6, 8, 9);
                    g2.drawLine(8, 9, 12, 3);
                    g2.dispose();
                }
            };
            checkMarks.setPreferredSize(new Dimension(14, 12));
            metaPanel.add(checkMarks);
        }
        bubble.add(metaPanel, BorderLayout.SOUTH);

        if (msg.isMe()) {
            JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightBox.setOpaque(false);
            rightBox.add(bubble);
            rightBox.add(new AvatarPanel(msg.getSender(), 32, false));
            row.add(rightBox, BorderLayout.EAST);
        } else {
            JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            leftBox.setOpaque(false);
            leftBox.add(new AvatarPanel(msg.getSender(), 32, false));
            leftBox.add(bubble);
            row.add(leftBox, BorderLayout.WEST);
        }

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private static JPanel createFileCard(ChatMessage msg, Component parent) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(msg.isMe() ? UITheme.PRIMARY : UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 12, 8, 12));
        card.setPreferredSize(new Dimension(260, 60));

        JComponent fileIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UITheme.PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(8, 6, 16, 22, 2, 2);
                g2.drawLine(12, 12, 20, 12);
                g2.drawLine(12, 16, 20, 16);
                g2.drawLine(12, 20, 17, 20);
                g2.dispose();
            }
        };
        fileIcon.setPreferredSize(new Dimension(36, 36));
        card.add(fileIcon, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);

        JLabel lblFileName = new JLabel(msg.getFileName() != null ? msg.getFileName() : "Tệp tin");
        lblFileName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFileName.setForeground(UITheme.TEXT_MAIN);

        String statusText = (msg.isMe() ? "Đã gửi P2P" : "Đã nhận") + " • " + msg.getTime() + " (Click mở)";
        JLabel lblStatus = new JLabel(statusText);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(UITheme.PRIMARY);

        infoPanel.add(lblFileName);
        infoPanel.add(lblStatus);
        card.add(infoPanel, BorderLayout.CENTER);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    File f = (msg.getFilePath() != null) ? new File(msg.getFilePath()) : new File(".");
                    Desktop.getDesktop().open(f.exists() && f.getParentFile() != null ? f.getParentFile() : new File("."));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parent, "Đường dẫn file: " + new File(".").getAbsolutePath());
                }
            }
        });
        return card;
    }
}