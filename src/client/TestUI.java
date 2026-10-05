package client;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class TestUI extends JFrame {
    private SidebarPanel sidebar;
    private ChatAreaPanel chatArea;
    private Map<String, List<ChatMessage>> histories = new HashMap<>();

    public TestUI() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        setTitle("Cute Messenger • [Chế độ xem trước giao diện]");
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Khởi tạo Sidebar và Khung Chat
        sidebar = new SidebarPanel("Alice (Bạn)", 8881, this::onUserSelected);
        chatArea = new ChatAreaPanel(this::onSendMessage, this::onAttachFile, this::onClearChat);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, chatArea);
        splitPane.setDividerLocation(310);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        add(splitPane);

        // Nạp dữ liệu mẫu để kiểm tra toàn bộ giao diện
        loadMockData();
    }

    private void loadMockData() {
        // 1. Danh sách bạn bè mẫu
        List<String> users = Arrays.asList("Bob (IT Support)", "Charlie Puth", "Diana Prince", "David Beckham");
        sidebar.setUsers(users);

        // 2. Tin nhắn mẫu với Bob
        addMessage("Bob (IT Support)", "Bob (IT Support)", "Chào Alice! Giao diện mới này trông xịn quá!", false);
        addMessage("Bob (IT Support)", "Alice", "Cảm ơn Bob! Toàn bộ giao diện đã được thiết kế lại theo chuẩn hiện đại rồi đấy.", true);
        addMessage("Bob (IT Support)", "Bob (IT Support)", "Thử tính năng ngắt dòng khi tin nhắn dài xem nào: Đây là một đoạn tin nhắn dài để kiểm tra xem bong bóng chat có tự động co giãn và xuống dòng đẹp mắt không nhé!", false);
        addMessage("Bob (IT Support)", "System", "📎 <b>Đã gửi file:</b> BaoCaoTuan.pdf", true);
        addMessage("Bob (IT Support)", "System", "📎 <b>Nhận được file:</b> AnhThietKe.png", false);

        // 3. Tin nhắn mẫu với Charlie
        addMessage("Charlie Puth", "Charlie Puth", "Hôm nay có họp dự án không bạn ơi?", false);

        // Mặc định chọn Bob để mở khung chat ngay
        sidebar.setUsers(users);
        onUserSelected("Bob (IT Support)");
    }

    private void onUserSelected(String user) {
        chatArea.selectChat(user);
        if (user != null) {
            chatArea.displayHistory(histories.get(user));
        }
    }

    private void onSendMessage(String text) {
        String receiver = sidebar.getSelectedUser();
        if (receiver == null) return;

        // Hiện tin nhắn của bạn
        addMessage(receiver, "Alice", text, true);

        // Giả lập đối phương tự động trả lời sau 1 giây
        Timer botReply = new Timer(1000, e -> {
            addMessage(receiver, receiver, "Tôi đã nhận được: \"" + text + "\"", false);
        });
        botReply.setRepeats(false);
        botReply.start();
    }

    private void onAttachFile() {
        String receiver = sidebar.getSelectedUser();
        if (receiver == null) return;

        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fname = fc.getSelectedFile().getName();
            addMessage(receiver, "System", "📎 <b>Đã gửi file:</b> " + fname, true);
        }
    }

    private void onClearChat() {
        String receiver = sidebar.getSelectedUser();
        if (receiver == null) return;
        if (JOptionPane.showConfirmDialog(this, "Xoá cuộc trò chuyện với " + receiver + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            histories.remove(receiver);
            sidebar.clearUserHistory(receiver);
            chatArea.displayHistory(null);
        }
    }

    private void addMessage(String targetUser, String sender, String message, boolean isMe) {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        boolean isSystem = sender.equalsIgnoreCase("System");
        boolean isFile = message.contains("📎") || message.toLowerCase().contains("file:");
        String fileName = isFile ? message.replaceAll("<[^>]*>", "").replace("📎", "").replace("Đã gửi file:", "").replace("Nhận được file:", "").trim() : null;

        ChatMessage item = new ChatMessage(sender, message, time, isMe, isSystem, isFile, fileName, null);
        histories.computeIfAbsent(targetUser, k -> new ArrayList<>()).add(item);

        sidebar.updateSnippet(targetUser, isFile ? "📎 " + fileName : (isSystem ? message.replaceAll("<[^>]*>", "") : message), time);
        if (targetUser.equals(sidebar.getSelectedUser())) {
            chatArea.appendBubble(item);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TestUI().setVisible(true));
    }
}