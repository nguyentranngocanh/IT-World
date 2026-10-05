package client;

import model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class ClientNetworkService {
    public interface NetworkCallback {
        // Chat & P2P
        void onTextMessage(String sender, String content);
        void onUserListUpdated(List<User> users);
        void onGetIpResponse(User targetUser);

        // IT World Features
        default void onFeedReceived(List<Post> posts) {}
        default void onPostCreated(Post post) {}
        default void onLikeUpdated(String postId, boolean isLiked) {}
        default void onCommentAdded(Comment comment) {}
        default void onProfileReceived(UserProfile profile) {}
        default void onProfileUpdated(UserProfile profile) {}
        default void onFollowUpdated(String targetUser, boolean isFollowing) {}
        default void onDevListReceived(List<UserProfile> devs) {}
    }

    public static void startListening(ObjectInputStream in, NetworkCallback callback) {
        Thread listenThread = new Thread(() -> {
            try {
                while (true) {
                    Message msg = (Message) in.readObject();
                    if (msg == null) break;

                    switch (msg.getType()) {
                        case TEXT_MESSAGE:
                            callback.onTextMessage(msg.getSender(), msg.getContent());
                            break;
                        case USER_LIST_RESPONSE:
                            callback.onUserListUpdated((List<User>) msg.getPayload());
                            break;
                        case GET_IP_RESPONSE:
                            callback.onGetIpResponse((User) msg.getPayload());
                            break;
                        case GET_FEED_RESPONSE:
                            callback.onFeedReceived((List<Post>) msg.getPayload());
                            break;
                        case CREATE_POST_RESPONSE:
                            callback.onPostCreated((Post) msg.getPayload());
                            break;
                        case LIKE_POST_RESPONSE:
                            callback.onLikeUpdated(msg.getContent(), (Boolean) msg.getPayload());
                            break;
                        case ADD_COMMENT_RESPONSE:
                            callback.onCommentAdded((Comment) msg.getPayload());
                            break;
                        case GET_PROFILE_RESPONSE:
                            callback.onProfileReceived((UserProfile) msg.getPayload());
                            break;
                        case UPDATE_PROFILE_RESPONSE:
                            callback.onProfileUpdated((UserProfile) msg.getPayload());
                            break;
                        case TOGGLE_FOLLOW_RESPONSE:
                            callback.onFollowUpdated(msg.getContent(), (Boolean) msg.getPayload());
                            break;
                        case GET_DEV_LIST_RESPONSE:
                            callback.onDevListReceived((List<UserProfile>) msg.getPayload());
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Luồng nhận dữ liệu Client đã dừng: " + e.getMessage());
            }
        });
        listenThread.setDaemon(true);
        listenThread.start();
    }

    // --- CÁC HÀM GỬI YÊU CẦU LÊN SERVER ---

    public static void sendText(ObjectOutputStream out, String sender, String receiver, String text) throws IOException {
        Message msg = new Message(MessageType.TEXT_MESSAGE, sender, receiver);
        msg.setContent(text);
        sendMessage(out, msg);
    }

    public static void requestIp(ObjectOutputStream out, String sender, String receiver) throws IOException {
        Message msg = new Message(MessageType.GET_IP_REQUEST, sender, receiver);
        sendMessage(out, msg);
    }

    public static void requestFeed(ObjectOutputStream out, String sender, boolean followingOnly) throws IOException {
        Message msg = new Message(MessageType.GET_FEED_REQUEST, sender, "server");
        msg.setPayload(followingOnly);
        sendMessage(out, msg);
    }

    public static void createPost(ObjectOutputStream out, String sender, Post post) throws IOException {
        Message msg = new Message(MessageType.CREATE_POST_REQUEST, sender, "server");
        msg.setPayload(post);
        sendMessage(out, msg);
    }

    public static void toggleLike(ObjectOutputStream out, String sender, String postId) throws IOException {
        Message msg = new Message(MessageType.LIKE_POST_REQUEST, sender, "server");
        msg.setPayload(postId);
        sendMessage(out, msg);
    }

    public static void addComment(ObjectOutputStream out, String sender, Comment comment) throws IOException {
        Message msg = new Message(MessageType.ADD_COMMENT_REQUEST, sender, "server");
        msg.setPayload(comment);
        sendMessage(out, msg);
    }

    public static void requestProfile(ObjectOutputStream out, String sender, String targetUsername) throws IOException {
        Message msg = new Message(MessageType.GET_PROFILE_REQUEST, sender, "server");
        msg.setContent(targetUsername);
        sendMessage(out, msg);
    }

    public static void updateProfile(ObjectOutputStream out, String sender, UserProfile profile) throws IOException {
        Message msg = new Message(MessageType.UPDATE_PROFILE_REQUEST, sender, "server");
        msg.setPayload(profile);
        sendMessage(out, msg);
    }

    public static void toggleFollow(ObjectOutputStream out, String sender, String targetUsername) throws IOException {
        Message msg = new Message(MessageType.TOGGLE_FOLLOW_REQUEST, sender, "server");
        msg.setContent(targetUsername);
        sendMessage(out, msg);
    }

    public static void requestDevList(ObjectOutputStream out, String sender) throws IOException {
        Message msg = new Message(MessageType.GET_DEV_LIST_REQUEST, sender, "server");
        sendMessage(out, msg);
    }

    private static synchronized void sendMessage(ObjectOutputStream out, Message msg) throws IOException {
        if (out == null) return;
        synchronized (out) {
            out.reset();
            out.writeObject(msg);
            out.flush();
        }
    }
}