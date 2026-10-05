package server;

import model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Máy chủ trung tâm IT World
 * Quản lý kết nối Socket, luồng Client, Chat thời gian thực, điều phối P2P và các dịch vụ cộng đồng IT.
 */
public class Server {
    private static final int PORT = 8000;

    // Quản lý danh sách Client đang kết nối: username -> ClientHandler
    private static final ConcurrentHashMap<String, ClientHandler> onlineClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("🚀  IT WORLD SERVER ĐANG KHỞI ĐỘNG TẠI PORT " + PORT);
        System.out.println("=================================================");

        // Khởi tạo Database XAMPP MySQL
        DatabaseManager.initDatabase();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("-> Máy chủ đã sẵn sàng lắng nghe kết nối từ các lập trình viên...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("-> [Kết nối mới] Client IP: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================
    // Lớp nội bộ xử lý riêng cho từng Client
    // =========================================================
    public static class ClientHandler extends Thread {
        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private User currentUser;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Khởi tạo Stream
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Message message = (Message) in.readObject();
                    if (message == null) break;

                    switch (message.getType()) {
                        // 1. Phân hệ Định danh & Xác thực (Authentication & Registration)
                        case REGISTER_REQUEST:
                            handleRegister(message);
                            break;

                        case LOGIN:
                            handleLogin(message);
                            break;

                        case TEXT_MESSAGE:
                            handleTextMessage(message);
                            break;

                        case GET_IP_REQUEST:
                            handleGetIpRequest(message);
                            break;

                        case GET_USER_LIST:
                            broadcastUserList();
                            break;

                        // 2. Phân hệ Bảng tin công nghệ (Newsfeed & Code Snippets)
                        case GET_FEED_REQUEST:
                            handleGetFeed(message);
                            break;

                        case CREATE_POST_REQUEST:
                            handleCreatePost(message);
                            break;

                        case LIKE_POST_REQUEST:
                            handleLikePost(message);
                            break;

                        case ADD_COMMENT_REQUEST:
                            handleAddComment(message);
                            break;

                        // 3. Phân hệ Hồ sơ lập trình viên (Developer Profile)
                        case GET_PROFILE_REQUEST:
                            handleGetProfile(message);
                            break;

                        case UPDATE_PROFILE_REQUEST:
                            handleUpdateProfile(message);
                            break;

                        // 4. Phân hệ Cộng đồng & Theo dõi (Follow System)
                        case TOGGLE_FOLLOW_REQUEST:
                            handleToggleFollow(message);
                            break;

                        case GET_DEV_LIST_REQUEST:
                            handleGetDevList();
                            break;

                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println("- Client ngắt kết nối: " + (currentUser != null ? currentUser.getUsername() : "Chưa đăng nhập"));
            } finally {
                if (currentUser != null) {
                    onlineClients.remove(currentUser.getUsername());
                    System.out.println("❌ User đã rời mạng: " + currentUser.getUsername());
                    broadcastUserList();
                }
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }

        public void sendMessage(Message msg) {
            try {
                synchronized (out) {
                    out.reset();
                    out.writeObject(msg);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Lỗi gửi tin tới " + (currentUser != null ? currentUser.getUsername() : "Client") + ": " + e.getMessage());
            }
        }

        // =========================================================
        // XỬ LÝ CHI TIẾT TỪNG LOẠI GÓI TIN
        // =========================================================

        private void handleRegister(Message message) {
            String username = message.getSender();
            String password = message.getPassword();
            UserProfile profile = (UserProfile) message.getPayload();
            String fullName = profile != null ? profile.getFullName() : username;
            String itLevel = profile != null ? profile.getItLevel() : "Junior Developer";
            String techStack = profile != null ? profile.getTechStack() : "Java, MySQL, Git";

            String error = DatabaseManager.registerUser(username, password, fullName, itLevel, techStack, "127.0.0.1", 9000);
            Message resp = new Message(MessageType.REGISTER_RESPONSE, "server", username);
            if (error == null) {
                resp.setSuccess(true);
                resp.setContent("Đăng ký thành công tài khoản @" + username + "! Hãy đăng nhập ngay.");
                System.out.println("✨ Đăng ký thành công: @" + username + " (" + itLevel + ")");
            } else {
                resp.setSuccess(false);
                resp.setContent(error);
                System.out.println("⚠️ Đăng ký thất bại cho @" + username + ": " + error);
            }
            sendMessage(resp);
        }

        private void handleLogin(Message message) {
            User user = (User) message.getPayload();
            String username = (user != null) ? user.getUsername() : message.getSender();
            String password = message.getPassword();

            // 1. Kiểm tra xác thực mật khẩu qua CSDL XAMPP MySQL
            String authError = DatabaseManager.authenticateUser(username, password);
            Message resp = new Message(MessageType.LOGIN_RESPONSE, "server", username);

            if (authError != null) {
                resp.setSuccess(false);
                resp.setContent(authError);
                sendMessage(resp);
                System.out.println("❌ Đăng nhập thất bại: @" + username + " - " + authError);
                return;
            }

            // 2. Xác thực thành công
            this.currentUser = user;
            onlineClients.put(currentUser.getUsername(), this);
            System.out.println("✅ Đăng nhập: @" + currentUser.getUsername() + " (IP: " + currentUser.getIpAddress() + " | Port P2P: " + currentUser.getPeerPort() + ")");

            DatabaseManager.upsertUser(currentUser.getUsername(), currentUser.getIpAddress(), currentUser.getPeerPort());

            UserProfile myProfile = DatabaseManager.getUserProfile(currentUser.getUsername(), currentUser.getUsername());
            resp.setSuccess(true);
            resp.setContent("Đăng nhập thành công!");
            resp.setPayload(myProfile);
            sendMessage(resp);

            // Thông báo danh sách online
            broadcastUserList();
        }

        private void handleTextMessage(Message message) {
            String receiverName = message.getReceiver();
            if (onlineClients.containsKey(receiverName)) {
                onlineClients.get(receiverName).sendMessage(message);
            } else {
                System.out.println("⚠️ Không tìm thấy người nhận chat: " + receiverName);
            }
        }

        private void handleGetIpRequest(Message message) {
            String targetUsername = message.getReceiver();
            if (onlineClients.containsKey(targetUsername)) {
                User targetUser = onlineClients.get(targetUsername).currentUser;
                Message response = new Message(MessageType.GET_IP_RESPONSE, "server", message.getSender());
                response.setPayload(targetUser);
                this.sendMessage(response);
                System.out.println("-> Cấp thông tin P2P của @" + targetUsername + " cho @" + message.getSender());
            }
        }

        private void broadcastUserList() {
            List<User> listUsers = new ArrayList<>();
            for (ClientHandler handler : onlineClients.values()) {
                if (handler.currentUser != null) {
                    listUsers.add(handler.currentUser);
                }
            }

            Message msg = new Message(MessageType.USER_LIST_RESPONSE, "server", "All");
            msg.setPayload(listUsers);

            for (ClientHandler handler : onlineClients.values()) {
                handler.sendMessage(msg);
            }
        }

        private void handleGetFeed(Message message) {
            boolean followingOnly = message.getPayload() instanceof Boolean && (Boolean) message.getPayload();
            String viewer = currentUser != null ? currentUser.getUsername() : message.getSender();
            List<Post> feed = DatabaseManager.getFeed(viewer, followingOnly);

            Message resp = new Message(MessageType.GET_FEED_RESPONSE, "server", viewer);
            resp.setPayload(feed);
            sendMessage(resp);
        }

        private void handleCreatePost(Message message) {
            try {
                Post post = (Post) message.getPayload();
                if (post != null) {
                    DatabaseManager.createPost(post);
                    System.out.println("📝 Bài viết mới từ @" + post.getAuthor() + ": " + post.getTitle());

                    // Trả lời cho người đăng
                    Message resp = new Message(MessageType.CREATE_POST_RESPONSE, "server", post.getAuthor());
                    resp.setPayload(post);
                    sendMessage(resp);

                    // Broadcast cập nhật Bảng tin cho tất cả user online
                    for (ClientHandler client : onlineClients.values()) {
                        if (client != null && client.currentUser != null) {
                            try {
                                List<Post> clientFeed = DatabaseManager.getFeed(client.currentUser.getUsername(), false);
                                Message feedMsg = new Message(MessageType.GET_FEED_RESPONSE, "server", client.currentUser.getUsername());
                                feedMsg.setPayload(clientFeed);
                                client.sendMessage(feedMsg);
                            } catch (Exception ex) {
                                System.err.println("Lỗi gửi feed tới " + client.currentUser.getUsername() + ": " + ex.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi xử lý CREATE_POST_REQUEST: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleLikePost(Message message) {
            String postId = (String) message.getPayload();
            String username = currentUser != null ? currentUser.getUsername() : message.getSender();
            boolean liked = DatabaseManager.toggleLikePost(postId, username);

            Message resp = new Message(MessageType.LIKE_POST_RESPONSE, "server", username);
            resp.setContent(postId);
            resp.setPayload(liked);
            sendMessage(resp);
        }

        private void handleAddComment(Message message) {
            try {
                Comment comment = (Comment) message.getPayload();
                if (comment != null) {
                    DatabaseManager.addComment(comment);

                    Message resp = new Message(MessageType.ADD_COMMENT_RESPONSE, "server", comment.getAuthor());
                    resp.setPayload(comment);
                    sendMessage(resp);

                    // Thông báo cập nhật feed cho mọi người
                    for (ClientHandler client : onlineClients.values()) {
                        if (client != null && client.currentUser != null) {
                            try {
                                List<Post> clientFeed = DatabaseManager.getFeed(client.currentUser.getUsername(), false);
                                Message feedMsg = new Message(MessageType.GET_FEED_RESPONSE, "server", client.currentUser.getUsername());
                                feedMsg.setPayload(clientFeed);
                                client.sendMessage(feedMsg);
                            } catch (Exception ex) {
                                System.err.println("Lỗi gửi comment feed: " + ex.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi xử lý ADD_COMMENT_REQUEST: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleGetProfile(Message message) {
            String targetUser = message.getContent();
            if (targetUser == null || targetUser.trim().isEmpty()) {
                targetUser = currentUser != null ? currentUser.getUsername() : message.getSender();
            }
            String viewer = currentUser != null ? currentUser.getUsername() : message.getSender();
            UserProfile profile = DatabaseManager.getUserProfile(targetUser, viewer);

            Message resp = new Message(MessageType.GET_PROFILE_RESPONSE, "server", viewer);
            resp.setPayload(profile);
            sendMessage(resp);
        }

        private void handleUpdateProfile(Message message) {
            UserProfile profile = (UserProfile) message.getPayload();
            if (profile != null) {
                DatabaseManager.updateUserProfile(profile);
                System.out.println("👤 Cập nhật hồ sơ thành công: @" + profile.getUsername());

                Message resp = new Message(MessageType.UPDATE_PROFILE_RESPONSE, "server", profile.getUsername());
                resp.setPayload(profile);
                sendMessage(resp);
            }
        }

        private void handleToggleFollow(Message message) {
            String targetUser = message.getContent();
            String follower = currentUser != null ? currentUser.getUsername() : message.getSender();
            boolean isNowFollowing = DatabaseManager.toggleFollow(follower, targetUser);

            Message resp = new Message(MessageType.TOGGLE_FOLLOW_RESPONSE, "server", follower);
            resp.setContent(targetUser);
            resp.setPayload(isNowFollowing);
            sendMessage(resp);
        }

        private void handleGetDevList() {
            String viewer = currentUser != null ? currentUser.getUsername() : "";
            List<UserProfile> devs = DatabaseManager.getDevList(viewer);

            Message resp = new Message(MessageType.GET_DEV_LIST_RESPONSE, "server", viewer);
            resp.setPayload(devs);
            sendMessage(resp);
        }
    }
}