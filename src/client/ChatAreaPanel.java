package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ChatAreaPanel extends JPanel {
    private CardLayout cardLayout = new CardLayout();
    private JPanel chatMessagesPanel;
    private JScrollPane chatScroll;
    private JTextField messageField;
    private SendButton btnSend;
    private JLabel lblChatTitle, lblChatStatus;
    private AvatarPanel headerAvatar;

    public ChatAreaPanel(Consumer<String> onSend, Runnable onAttach, Runnable onClear) {
        setLayout(cardLayout);
        setBackground(UITheme.BG_CHAT);

        add(createEmptyState(), "EMPTY");
        add(createActiveChat(onSend, onAttach, onClear), "CHAT");
        cardLayout.show(this, "EMPTY");
    }

    private JPanel createEmptyState() {
        JPanel empty = new JPanel(new GridBagLayout());
        empty.setBackground(UITheme.BG_CHAT);
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JComponent logo = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppIcons.drawLogo(g2, 0, 0, getWidth());
                g2.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(80, 80));
        logo.setMaximumSize(new Dimension(80, 80));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("IT World Messenger");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Chọn một lập trình viên từ danh sách bên trái để bắt đầu trò chuyện & gửi file");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        box.add(logo);
        box.add(Box.createVerticalStrut(14));
        box.add(title);
        box.add(Box.createVerticalStrut(6));
        box.add(sub);
        empty.add(box);
        return empty;
    }

    private JPanel createActiveChat(Consumer<String> onSend, Runnable onAttach, Runnable onClear) {
        JPanel active = new JPanel(new BorderLayout());
        active.setBackground(UITheme.BG_CHAT);

        // 1. Header Cuộc trò chuyện
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        headerAvatar = new AvatarPanel("", 42, true);
        left.add(headerAvatar, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        lblChatTitle = new JLabel("Người dùng");
        lblChatTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblChatStatus = new JLabel("● Đang hoạt động");
        lblChatStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblChatStatus.setForeground(UITheme.ONLINE);
        info.add(lblChatTitle);
        info.add(lblChatStatus);
        left.add(info, BorderLayout.CENTER);

        // Nút hành động trên Header
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        IconButton btnFile = new IconButton("clip", "Gửi tệp tin");
        btnFile.addActionListener(e -> onAttach.run());
        IconButton btnClear = new IconButton("trash", "Xoá cuộc trò chuyện");
        btnClear.addActionListener(e -> onClear.run());
        actions.add(btnFile);
        actions.add(btnClear);

        header.add(left, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        active.add(header, BorderLayout.NORTH);

        // 2. Khu vực tin nhắn
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(UITheme.BG_CHAT);
        chatMessagesPanel.setBorder(new EmptyBorder(16, 0, 16, 0));

        chatScroll = new JScrollPane(chatMessagesPanel);
        chatScroll.setBorder(null);
        chatScroll.setBackground(UITheme.BG_CHAT);
        chatScroll.getViewport().setBackground(UITheme.BG_CHAT);
        chatScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        chatScroll.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        active.add(chatScroll, BorderLayout.CENTER);

        // 3. Thanh nhập liệu (Được đặt trong Dock nổi bo góc màu trắng sang trọng)
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                new EmptyBorder(12, 20, 14, 20)
        ));

        JPanel pill = new JPanel(new BorderLayout(8, 0)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền xám nhạt nhẹ nhàng
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                // Viền mỏng
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.setBorder(new EmptyBorder(3, 6, 3, 6));

        // Nút gửi file hình tròn vector
        IconButton clip = new IconButton("clip", "Đính kèm tệp");
        clip.addActionListener(e -> onAttach.run());

        messageField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString("Nhập tin nhắn... (Nhấn Enter để gửi)", getInsets().left, y);
                    g2.dispose();
                }
            }
        };
        messageField.setOpaque(false);
        messageField.setBorder(new EmptyBorder(6, 10, 6, 10));
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnSend = new SendButton();
        btnSend.addActionListener(e -> send(onSend));
        messageField.addActionListener(e -> send(onSend));

        messageField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { btnSend.setHasText(!messageField.getText().trim().isEmpty()); }
            public void removeUpdate(DocumentEvent e) { btnSend.setHasText(!messageField.getText().trim().isEmpty()); }
            public void changedUpdate(DocumentEvent e) { btnSend.setHasText(!messageField.getText().trim().isEmpty()); }
        });

        pill.add(clip, BorderLayout.WEST);
        pill.add(messageField, BorderLayout.CENTER);
        pill.add(btnSend, BorderLayout.EAST);
        bottom.add(pill, BorderLayout.CENTER);
        active.add(bottom, BorderLayout.SOUTH);

        return active;
    }

    private void send(Consumer<String> onSend) {
        String txt = messageField.getText().trim();
        if (!txt.isEmpty()) {
            onSend.accept(txt);
            messageField.setText("");
        }
    }

    public void selectChat(String username) {
        if (username == null) {
            cardLayout.show(this, "EMPTY");
        } else {
            headerAvatar.setUserName(username);
            lblChatTitle.setText(username);
            cardLayout.show(this, "CHAT");
            messageField.requestFocus();
        }
    }

    public void displayHistory(List<ChatMessage> history) {
        chatMessagesPanel.removeAll();
        if (history != null) {
            for (ChatMessage msg : history) {
                chatMessagesPanel.add(MessageBubbleFactory.createBubble(msg, this));
                chatMessagesPanel.add(Box.createVerticalStrut(10));
            }
        }
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
        scrollToBottom();
    }

    public void appendBubble(ChatMessage msg) {
        chatMessagesPanel.add(MessageBubbleFactory.createBubble(msg, this));
        chatMessagesPanel.add(Box.createVerticalStrut(10));
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
        scrollToBottom();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum()));
    }
}