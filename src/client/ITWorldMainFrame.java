package client;

import model.Comment;
import model.Post;
import model.User;
import model.UserProfile;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Cửa sổ trung tâm ứng dụng IT World
 * Tích hợp toàn diện:
 * 1. Bảng tin chia sẻ mã nguồn (Feed)
 * 2. Nhắn tin thời gian thực & Truyền file P2P (Chat & File Transfer)
 * 3. Mạng lưới kết nối & Theo dõi Lập trình viên (Dev Network)
 * 4. Trang cá nhân & Năng lực IT (Profile)
 */
public class ITWorldMainFrame extends JFrame {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final String myUsername;
    private final int myPeerPort;
    private String pendingFilePath = "";

    // Giao diện
    private NavigationRail navRail;
    private CardLayout cardLayout;
    private JPanel cardsPanel;

    // Các phân hệ
    private FeedPanel feedPanel;
    private DevNetworkPanel devNetworkPanel;
    private ProfilePanel profilePanel;

    // Phân hệ Chat & P2P kế thừa từ Lab 4
    private SidebarPanel sidebar;
    private ChatAreaPanel chatArea;
    private final Map<String, List<ChatMessage>> histories = new HashMap<>();

    public ITWorldMainFrame(Socket socket, ObjectOutputStream out, ObjectInputStream in, String username, int peerPort) {
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.myUsername = username;
        this.myPeerPort = peerPort;

        setTitle("IT World • Cộng đồng Lập trình viên (@" + username + ")");
        setIconImage(AppIcons.getAppLogoImage(64));
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        initP2PFileService();
        initNetworkService();

        // Tải dữ liệu ban đầu
        loadInitialData();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());

        // 1. Card Layout Panel (Trung tâm) - Khởi tạo trước để sẵn sàng đón nhận sự kiện chuyển Tab
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // Tab 1: FEED
        feedPanel = new FeedPanel(
                myUsername,
                this::refreshFeed,
                this::createPost,
                this::toggleLikePost,
                this::addComment,
                this::viewUserProfile
        );
        cardsPanel.add(feedPanel, "FEED");

        // Tab 2: CHAT & P2P (Kế thừa trọn vẹn từ Lab 4)
        JPanel chatPanel = createChatPanel();
        cardsPanel.add(chatPanel, "CHAT");

        // Tab 3: DEV NETWORK
        devNetworkPanel = new DevNetworkPanel(
                myUsername,
                this::refreshDevList,
                this::toggleFollowUser,
                this::startDirectChatWith,
                this::viewUserProfile
        );
        cardsPanel.add(devNetworkPanel, "NETWORK");

        // Tab 4: PROFILE
        profilePanel = new ProfilePanel(
                myUsername,
                this::updateMyProfile,
                this::toggleFollowUser,
                this::viewUserProfile
        );
        cardsPanel.add(profilePanel, "PROFILE");

        // 2. Navigation Rail (Bên trái) - Khởi tạo sau khi CardLayout đã sẵn sàng
        navRail = new NavigationRail(myUsername, this::switchTab);
        root.add(navRail, BorderLayout.WEST);
        root.add(cardsPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createChatPanel() {
        sidebar = new SidebarPanel(myUsername, myPeerPort, this::onUserSelected);
        chatArea = new ChatAreaPanel(this::onSendMessage, this::onAttachFile, this::onClearChat);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, chatArea);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);

        JPanel p = new JPanel(new BorderLayout());
        p.add(splitPane, BorderLayout.CENTER);
        return p;
    }

    private void switchTab(String tabId) {
        if (cardLayout != null && cardsPanel != null) {
            cardLayout.show(cardsPanel, tabId);
        }
        if ("FEED".equals(tabId)) {
            refreshFeed(false);
        } else if ("NETWORK".equals(tabId)) {
            refreshDevList();
        } else if ("PROFILE".equals(tabId)) {
            viewUserProfile(myUsername);
        }
    }

    // =========================================================
    // DỊCH VỤ MẠNG & P2P FILE TRANSFER
    // =========================================================

    private void initP2PFileService() {
        PeerService.startPeerServer(myPeerPort, new PeerService.FileCallback() {
            public void onFileSent(String receiver, String fname, String path) {
                SwingUtilities.invokeLater(() -> addFileMessage(receiver, myUsername, fname, path, true));
            }

            public void onFileReceived(String sender, String fname, String path) {
                SwingUtilities.invokeLater(() -> addFileMessage(sender, sender, fname, path, false));
            }

            public void onError(String target, String err) {
                SwingUtilities.invokeLater(() -> addTextMessage(target, "System", "[!] " + err, true));
            }
        });
    }

    private void initNetworkService() {
        ClientNetworkService.startListening(in, new ClientNetworkService.NetworkCallback() {
            @Override
            public void onTextMessage(String sender, String content) {
                SwingUtilities.invokeLater(() -> addTextMessage(sender, sender, content, false));
            }

            @Override
            public void onUserListUpdated(List<User> users) {
                List<String> names = new ArrayList<>();
                for (User u : users) {
                    if (!u.getUsername().equals(myUsername)) names.add(u.getUsername());
                }
                SwingUtilities.invokeLater(() -> sidebar.setUsers(names));
            }

            @Override
            public void onGetIpResponse(User target) {
                SwingUtilities.invokeLater(() -> addTextMessage(target.getUsername(), "System", "<i>Đang gửi file...</i>", true));

                PeerService.sendFile(target.getIpAddress(), target.getPeerPort(), myUsername, pendingFilePath, target.getUsername(), new PeerService.FileCallback() {
                    public void onFileSent(String receiver, String fname, String path) {
                        SwingUtilities.invokeLater(() -> addFileMessage(receiver, myUsername, fname, path, true));
                    }
                    public void onFileReceived(String sender, String fname, String path) {}
                    public void onError(String targetUser, String err) {
                        SwingUtilities.invokeLater(() -> addTextMessage(targetUser, "System", "[!] " + err, true));
                    }
                });
            }

            @Override
            public void onFeedReceived(List<Post> posts) {
                SwingUtilities.invokeLater(() -> feedPanel.setPosts(posts));
            }

            @Override
            public void onPostCreated(Post post) {
                SwingUtilities.invokeLater(() -> {
                    feedPanel.resetFilterAndRefresh();
                    JOptionPane.showMessageDialog(ITWorldMainFrame.this,
                            "Bài viết của bạn đã được đăng tải thành công lên Bảng tin IT World!",
                            "Đăng bài thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            }

            @Override
            public void onLikeUpdated(String postId, boolean isLiked) {
                SwingUtilities.invokeLater(() -> refreshFeed(false));
            }

            @Override
            public void onCommentAdded(Comment comment) {
                SwingUtilities.invokeLater(() -> refreshFeed(false));
            }

            @Override
            public void onProfileReceived(UserProfile profile) {
                SwingUtilities.invokeLater(() -> profilePanel.setProfile(profile));
            }

            @Override
            public void onProfileUpdated(UserProfile profile) {
                SwingUtilities.invokeLater(() -> {
                    profilePanel.setProfile(profile);
                    JOptionPane.showMessageDialog(ITWorldMainFrame.this, "Hồ sơ của bạn đã được cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                });
            }

            @Override
            public void onFollowUpdated(String targetUser, boolean isFollowing) {
                SwingUtilities.invokeLater(() -> {
                    refreshDevList();
                    viewUserProfile(targetUser);
                });
            }

            @Override
            public void onDevListReceived(List<UserProfile> devs) {
                SwingUtilities.invokeLater(() -> devNetworkPanel.setDevList(devs));
            }
        });
    }

    private void loadInitialData() {
        try {
            ClientNetworkService.requestFeed(out, myUsername, false);
            ClientNetworkService.requestProfile(out, myUsername, myUsername);
            ClientNetworkService.requestDevList(out, myUsername);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // CÁC THAO TÁC NGHIỆP VỤ BẢNG TIN, HỒ SƠ & FOLLOW
    // =========================================================

    private void refreshFeed(boolean followingOnly) {
        try {
            ClientNetworkService.requestFeed(out, myUsername, followingOnly);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPost(Post post) {
        try {
            switchTab("FEED");
            ClientNetworkService.createPost(out, myUsername, post);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể gửi bài viết lên máy chủ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleLikePost(String postId) {
        try {
            ClientNetworkService.toggleLike(out, myUsername, postId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addComment(String postId, String content) {
        String cmtId = "cmt_" + UUID.randomUUID().toString().substring(0, 8);
        Comment cmt = new Comment(cmtId, postId, myUsername, content);
        try {
            ClientNetworkService.addComment(out, myUsername, cmt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshDevList() {
        try {
            ClientNetworkService.requestDevList(out, myUsername);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toggleFollowUser(String targetUser) {
        try {
            ClientNetworkService.toggleFollow(out, myUsername, targetUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void viewUserProfile(String username) {
        try {
            ClientNetworkService.requestProfile(out, myUsername, username);
            navRail.selectTab("PROFILE");
            cardLayout.show(cardsPanel, "PROFILE");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMyProfile(UserProfile profile) {
        try {
            ClientNetworkService.updateProfile(out, myUsername, profile);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật hồ sơ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startDirectChatWith(String username) {
        navRail.selectTab("CHAT");
        cardLayout.show(cardsPanel, "CHAT");
        sidebar.selectUser(username);
        onUserSelected(username);
    }

    // =========================================================
    // CÁC THAO TÁC PHÂN HỆ CHAT (KẾ THỪA LAB 4)
    // =========================================================

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
