package client;

import model.User;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ClientGUI extends JFrame {
    private ObjectOutputStream out;
    private String myUsername;
    private String pendingFilePath = "";

    private SidebarPanel sidebar;
    private ChatAreaPanel chatArea;
    private Map<String, List<ChatMessage>> histories = new HashMap<>();

    public ClientGUI(Socket socket, ObjectOutputStream out, ObjectInputStream in, String username, int peerPort) {
        this.out = out;
        this.myUsername = username;

        setTitle("IT World Messenger • @" + username);
        setIconImage(AppIcons.getAppLogoImage(64));
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        sidebar = new SidebarPanel(username, peerPort, this::onUserSelected);
        chatArea = new ChatAreaPanel(this::onSendMessage, this::onAttachFile, this::onClearChat);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, chatArea);
        splitPane.setDividerLocation(310);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        add(splitPane);

        // 1. Nhận File P2P: Lấy đúng Avatar của người gửi
        PeerService.startPeerServer(peerPort, new PeerService.FileCallback() {
            public void onFileSent(String receiver, String fname, String path) {
                SwingUtilities.invokeLater(() -> addFileMessage(receiver, myUsername, fname, path, true));
            }
            public void onFileReceived(String sender, String fname, String path) {
                // sender là tên thực tế của bạn bè (không dùng "System" nữa)
                SwingUtilities.invokeLater(() -> addFileMessage(sender, sender, fname, path, false));
            }
            public void onError(String target, String err) {
                SwingUtilities.invokeLater(() -> addTextMessage(target, "System", "[!] " + err, true));
            }
        });

        // 2. Nhận tin nhắn từ Server
        ClientNetworkService.startListening(in, new ClientNetworkService.NetworkCallback() {
            public void onTextMessage(String sender, String content) {
                SwingUtilities.invokeLater(() -> addTextMessage(sender, sender, content, false));
            }
            public void onUserListUpdated(List<User> users) {
                List<String> names = new ArrayList<>();
                for (User u : users) {
                    if (!u.getUsername().equals(myUsername)) names.add(u.getUsername());
                }
                SwingUtilities.invokeLater(() -> sidebar.setUsers(names));
            }
            public void onGetIpResponse(User target) {
                SwingUtilities.invokeLater(() -> addTextMessage(target.getUsername(), "System", "<i>Đang gửi file...</i>", true));

                PeerService.sendFile(target.getIpAddress(), target.getPeerPort(), myUsername, pendingFilePath, target.getUsername(), new PeerService.FileCallback() {
                    public void onFileSent(String receiver, String fname, String path) {
                        // myUsername là tên của chính bạn
                        SwingUtilities.invokeLater(() -> addFileMessage(receiver, myUsername, fname, path, true));
                    }
                    public void onFileReceived(String sender, String fname, String path) {}
                    public void onError(String targetUser, String err) {
                        SwingUtilities.invokeLater(() -> addTextMessage(targetUser, "System", "[!] " + err, true));
                    }
                });
            }
        });
    }

    private void onUserSelected(String user) {
        chatArea.selectChat(user);
        if (user != null) chatArea.displayHistory(histories.get(user));
    }

    private void onSendMessage(String text) {
        String receiver = sidebar.getSelectedUser();
        if (receiver == null) return;
        try {
            ClientNetworkService.sendText(out, myUsername, receiver, text);
            addTextMessage(receiver, myUsername, text, true);
        } catch (IOException e) {
            addTextMessage(receiver, "System", "[!] Lỗi kết nối máy chủ", true);
        }
    }

    private void onAttachFile() {
        String receiver = sidebar.getSelectedUser();
        if (receiver == null) return;
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pendingFilePath = fc.getSelectedFile().getAbsolutePath();
            try {
                ClientNetworkService.requestIp(out, myUsername, receiver);
            } catch (IOException e) {
                addTextMessage(receiver, "System", "[!] Không thể gửi yêu cầu truyền file", true);
            }
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

    public void addTextMessage(String targetUser, String sender, String message, boolean isMe) {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        boolean isSystem = sender.equalsIgnoreCase("System");
        ChatMessage item = new ChatMessage(sender, message, time, isMe, isSystem, false, null, null);

        histories.computeIfAbsent(targetUser, k -> new ArrayList<>()).add(item);
        sidebar.updateSnippet(targetUser, isSystem ? message.replaceAll("<[^>]*>", "") : message, time);

        if (targetUser.equals(sidebar.getSelectedUser())) {
            chatArea.appendBubble(item);
        }
    }

    public void addFileMessage(String targetUser, String sender, String fileName, String filePath, boolean isMe) {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        ChatMessage item = new ChatMessage(sender, fileName, time, isMe, false, true, fileName, filePath);

        histories.computeIfAbsent(targetUser, k -> new ArrayList<>()).add(item);
        sidebar.updateSnippet(targetUser, "[Tệp] " + fileName, time);

        if (targetUser.equals(sidebar.getSelectedUser())) {
            chatArea.appendBubble(item);
        }
    }
}