package server;

import model.Comment;
import model.Post;
import model.UserProfile;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý kết nối và thao tác Cơ sở dữ liệu XAMPP MySQL (it_world)
 * Hỗ trợ tự động tạo Database, Bảng, Dữ liệu mẫu và cơ chế Fallback an toàn.
 */
public class DatabaseManager {
    private static final String DB_NAME = "it_world";
    private static boolean isConnected = false;

    // Cấu hình kết nối MySQL mặc định của XAMPP
    private static String dbUrl = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static String dbUser = "root";
    private static String dbPass = "";

    // Bộ nhớ đệm InMemory Fallback (sử dụng khi MySQL chưa được bật trên máy)
    private static final Map<String, UserProfile> memoryProfiles = new ConcurrentHashMap<>();
    private static final List<Post> memoryPosts = Collections.synchronizedList(new ArrayList<>());
    private static final Set<String> memoryLikes = Collections.synchronizedSet(new HashSet<>()); // "postId:username"
    private static final Set<String> memoryFollows = Collections.synchronizedSet(new HashSet<>()); // "follower:following"

    public static synchronized void initDatabase() {
        System.out.println("-> [DatabaseManager] Đang kiểm tra kết nối XAMPP MySQL...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("⚠️ Chưa tìm thấy MySQL JDBC Driver. Sẽ sử dụng bộ nhớ đệm Memory Fallback.");
            initMemoryFallback();
            return;
        }

        // Thử kết nối đến MySQL Server (không chỉ định database trước để tạo nếu chưa có)
        String[] possibleUrls = {
                "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "jdbc:mysql://localhost:3307/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "jdbc:mysql://127.0.0.1:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
        };
        String[] possiblePass = {"", "root", "123456"};

        Connection conn = null;
        for (String url : possibleUrls) {
            for (String pass : possiblePass) {
                try {
                    conn = DriverManager.getConnection(url, dbUser, pass);
                    if (conn != null) {
                        dbPass = pass;
                        int port = url.contains("3307") ? 3307 : 3306;
                        dbUrl = "jdbc:mysql://localhost:" + port + "/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
                        break;
                    }
                } catch (Exception ignored) {}
            }
            if (conn != null) break;
        }

        if (conn == null) {
            System.out.println("⚠️ Không thể kết nối tới XAMPP MySQL (Port 3306).");
            System.out.println("👉 Vui lòng mở XAMPP Control Panel và ấn [Start] ở mục MySQL.");
            System.out.println("⚡ Hệ thống tự động chuyển sang chế độ Memory Cache (Bộ nhớ tạm thời) để tiếp tục hoạt động!");
            initMemoryFallback();
            return;
        }

        try {
            Statement stmt = conn.createStatement();
            // 1. Tạo database nếu chưa có
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + DB_NAME + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.close();
            conn.close();

            // Kết nối trực tiếp vào database it_world
            Connection dbConn = getConnection();
            if (dbConn != null) {
                createTablesIfNotExist(dbConn);
                seedSampleData(dbConn);
                dbConn.close();
                isConnected = true;
                System.out.println("✅ [DatabaseManager] Kết nối XAMPP MySQL thành công (Database: " + DB_NAME + ")!");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Lỗi thiết lập cấu trúc database: " + e.getMessage());
            initMemoryFallback();
        }
    }

    private static Connection getConnection() {
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPass);
        } catch (Exception e) {
            return null;
        }
    }

    private static void createTablesIfNotExist(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        // 1. Users
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `users` (" +
                "`username` VARCHAR(50) NOT NULL PRIMARY KEY, " +
                "`password` VARCHAR(100) DEFAULT '123456', " +
                "`ip_address` VARCHAR(50) DEFAULT '127.0.0.1', " +
                "`peer_port` INT DEFAULT 9000, " +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 2. Profiles
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `user_profiles` (" +
                "`username` VARCHAR(50) NOT NULL PRIMARY KEY, " +
                "`full_name` VARCHAR(100) DEFAULT '', " +
                "`it_level` VARCHAR(50) DEFAULT 'Junior Developer', " +
                "`bio` TEXT, " +
                "`tech_stack` VARCHAR(255) DEFAULT 'Java, Spring Boot, MySQL', " +
                "`github_url` VARCHAR(255) DEFAULT 'https://github.com', " +
                "`avatar_color` INT DEFAULT 0, " +
                "`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (`username`) REFERENCES `users`(`username`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 3. Posts
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `posts` (" +
                "`id` VARCHAR(50) NOT NULL PRIMARY KEY, " +
                "`author` VARCHAR(50) NOT NULL, " +
                "`title` VARCHAR(255) NOT NULL, " +
                "`content` TEXT NOT NULL, " +
                "`code_snippet` TEXT, " +
                "`code_language` VARCHAR(50) DEFAULT 'Java', " +
                "`tags` VARCHAR(255) DEFAULT '#java', " +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (`author`) REFERENCES `users`(`username`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 4. Post Likes
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `post_likes` (" +
                "`post_id` VARCHAR(50) NOT NULL, " +
                "`username` VARCHAR(50) NOT NULL, " +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`post_id`, `username`), " +
                "FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`) ON DELETE CASCADE, " +
                "FOREIGN KEY (`username`) REFERENCES `users`(`username`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 5. Comments
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `comments` (" +
                "`id` VARCHAR(50) NOT NULL PRIMARY KEY, " +
                "`post_id` VARCHAR(50) NOT NULL, " +
                "`author` VARCHAR(50) NOT NULL, " +
                "`content` TEXT NOT NULL, " +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`) ON DELETE CASCADE, " +
                "FOREIGN KEY (`author`) REFERENCES `users`(`username`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 6. Follows
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `follows` (" +
                "`follower` VARCHAR(50) NOT NULL, " +
                "`following` VARCHAR(50) NOT NULL, " +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`follower`, `following`), " +
                "FOREIGN KEY (`follower`) REFERENCES `users`(`username`) ON DELETE CASCADE, " +
                "FOREIGN KEY (`following`) REFERENCES `users`(`username`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        stmt.close();
    }

    private static void seedSampleData(Connection conn) {
        try {
            Statement checkStmt = conn.createStatement();
            ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM `users`");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("-> Đang khởi tạo dữ liệu mẫu cho cộng đồng IT World...");
                Statement insertStmt = conn.createStatement();

                insertStmt.executeUpdate("INSERT IGNORE INTO `users` (`username`, `ip_address`, `peer_port`) VALUES " +
                        "('alex_dev', '127.0.0.1', 9001), " +
                        "('sarah_tech', '127.0.0.1', 9002), " +
                        "('david_cloud', '127.0.0.1', 9003)");

                insertStmt.executeUpdate("INSERT IGNORE INTO `user_profiles` (`username`, `full_name`, `it_level`, `bio`, `tech_stack`, `github_url`) VALUES " +
                        "('alex_dev', 'Alex Ferguson', 'Senior Backend Engineer', 'Đam mê High-Concurrency, Distributed Systems và Clean Architecture.', 'Java, Spring Boot, Redis, Kafka, Docker', 'https://github.com/alex-dev'), " +
                        "('sarah_tech', 'Sarah Connor', 'Solution Architect', '10 năm kinh nghiệm thiết kế hệ thống Microservices & Cloud-Native.', 'Java, Go, Kubernetes, AWS, PostgreSQL', 'https://github.com/sarah-tech'), " +
                        "('david_cloud', 'David Beckham', 'DevOps / Cloud Engineer', 'Chuyên gia CI/CD Pipeline, Terraform và bảo mật hạ tầng mạng.', 'Docker, K8s, Linux, Terraform, Python', 'https://github.com/david-cloud')");

                insertStmt.executeUpdate("INSERT IGNORE INTO `follows` (`follower`, `following`) VALUES " +
                        "('alex_dev', 'sarah_tech'), " +
                        "('david_cloud', 'sarah_tech'), " +
                        "('sarah_tech', 'alex_dev')");

                insertStmt.executeUpdate("INSERT IGNORE INTO `posts` (`id`, `author`, `title`, `content`, `code_snippet`, `code_language`, `tags`, `created_at`) VALUES " +
                        "('post_1', 'sarah_tech', 'Tối ưu Socket I/O trong Java với Virtual Threads (Java 21)', " +
                        "'Trước Java 21, mỗi kết nối Socket phải dùng 1 Platform Thread từ OS, dễ gây tràn bộ nhớ khi có hàng chục nghìn client. Giờ đây với Virtual Threads (Project Loom), việc xử lý hàng triệu connection trở nên cực kỳ nhẹ nhàng!', " +
                        "'// Tạo Executor chạy Virtual Thread cho mỗi Client kết nối\\nServerSocket serverSocket = new ServerSocket(8000);\\nExecutorService executor = Executors.newVirtualThreadPerTask();\\n\\nwhile (true) {\\n    Socket socket = serverSocket.accept();\\n    executor.submit(() -> handleClient(socket));\\n}', " +
                        "'Java', '#java #concurrency #socket #backend', NOW() - INTERVAL 2 HOUR), " +
                        "('post_2', 'alex_dev', 'Nguyên lý SOLID: Single Responsibility Principle trong Java Swing', " +
                        "'Một lỗi thường gặp khi code giao diện Java Swing là nhét tất cả code Socket, Vẽ giao diện, và Xử lý sự kiện vào chung 1 file JFrame. Hãy chia nhỏ thành View, Service và Model để code sạch và dễ bảo trì hơn rất nhiều!', " +
                        "'// Tách riêng Service mạng và UI View\\npublic class PeerService {\\n    public static void sendFile(String ip, int port, String path, FileCallback cb) {\\n        // Xử lý stream truyền file ở luồng ngầm\\n    }\\n}', " +
                        "'Java', '#architecture #clean-code #java', NOW() - INTERVAL 5 HOUR)");

                insertStmt.executeUpdate("INSERT IGNORE INTO `comments` (`id`, `post_id`, `author`, `content`, `created_at`) VALUES " +
                        "('cmt_1', 'post_1', 'alex_dev', 'Chia sẻ rất hay! Virtual Threads giúp giảm tải RAM đáng kinh ngạc.', NOW() - INTERVAL 1 HOUR), " +
                        "('cmt_2', 'post_1', 'david_cloud', 'Rất hữu ích cho các dự án Socket và Chat Server!', NOW() - INTERVAL 30 MINUTE)");

                insertStmt.close();
            }
            checkStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initMemoryFallback() {
        if (!memoryProfiles.isEmpty()) return;

        UserProfile u1 = new UserProfile("sarah_tech");
        u1.setFullName("Sarah Connor");
        u1.setItLevel("Solution Architect");
        u1.setBio("10 năm kinh nghiệm thiết kế hệ thống Microservices & Cloud-Native.");
        u1.setTechStack("Java, Go, Kubernetes, AWS, PostgreSQL");
        u1.setGithubUrl("https://github.com/sarah-tech");
        u1.setFollowersCount(2);
        u1.setFollowingCount(1);
        u1.setPostsCount(1);

        UserProfile u2 = new UserProfile("alex_dev");
        u2.setFullName("Alex Ferguson");
        u2.setItLevel("Senior Backend Engineer");
        u2.setBio("Đam mê High-Concurrency, Distributed Systems và Clean Architecture.");
        u2.setTechStack("Java, Spring Boot, Redis, Kafka, Docker");
        u2.setGithubUrl("https://github.com/alex-dev");
        u2.setFollowersCount(1);
        u2.setFollowingCount(1);
        u2.setPostsCount(1);

        UserProfile u3 = new UserProfile("david_cloud");
        u3.setFullName("David Beckham");
        u3.setItLevel("DevOps / Cloud Engineer");
        u3.setBio("Chuyên gia CI/CD Pipeline, Terraform và bảo mật hạ tầng mạng.");
        u3.setTechStack("Docker, K8s, Linux, Terraform, Python");
        u3.setGithubUrl("https://github.com/david-cloud");
        u3.setFollowersCount(0);
        u3.setFollowingCount(1);
        u3.setPostsCount(0);

        memoryProfiles.put(u1.getUsername(), u1);
        memoryProfiles.put(u2.getUsername(), u2);
        memoryProfiles.put(u3.getUsername(), u3);

        memoryFollows.add("alex_dev:sarah_tech");
        memoryFollows.add("david_cloud:sarah_tech");
        memoryFollows.add("sarah_tech:alex_dev");

        Post p1 = new Post("post_1", "sarah_tech", "Tối ưu Socket I/O trong Java với Virtual Threads (Java 21)",
                "Trước Java 21, mỗi kết nối Socket phải dùng 1 Platform Thread từ OS, dễ gây tràn bộ nhớ khi có hàng chục nghìn client. Giờ đây với Virtual Threads (Project Loom), việc xử lý hàng triệu connection trở nên cực kỳ nhẹ nhàng!",
                "// Tạo Executor chạy Virtual Thread cho mỗi Client kết nối\nServerSocket serverSocket = new ServerSocket(8000);\nExecutorService executor = Executors.newVirtualThreadPerTask();\n\nwhile (true) {\n    Socket socket = serverSocket.accept();\n    executor.submit(() -> handleClient(socket));\n}",
                "Java", "#java #concurrency #socket #backend");
        p1.setAuthorLevel("Solution Architect");
        p1.setLikesCount(2);
        p1.getComments().add(new Comment("cmt_1", "post_1", "alex_dev", "Chia sẻ rất hay! Virtual Threads giúp giảm tải RAM đáng kinh ngạc."));
        p1.getComments().add(new Comment("cmt_2", "post_1", "david_cloud", "Rất hữu ích cho các dự án Socket và Chat Server!"));

        Post p2 = new Post("post_2", "alex_dev", "Nguyên lý SOLID: Single Responsibility Principle trong Java Swing",
                "Một lỗi thường gặp khi code giao diện Java Swing là nhét tất cả code Socket, Vẽ giao diện, và Xử lý sự kiện vào chung 1 file JFrame. Hãy chia nhỏ thành View, Service và Model để code sạch và dễ bảo trì hơn rất nhiều!",
                "// Tách riêng Service mạng và UI View\npublic class PeerService {\n    public static void sendFile(String ip, int port, String path, FileCallback cb) {\n        // Xử lý stream truyền file ở luồng ngầm\n    }\n}",
                "Java", "#architecture #clean-code #java");
        p2.setAuthorLevel("Senior Backend Engineer");

        memoryPosts.add(p1);
        memoryPosts.add(p2);
    }

    // =========================================================
    // XÁC THỰC & ĐĂNG KÝ TÀI KHOẢN (AUTHENTICATION & REGISTRATION)
    // =========================================================

    public static synchronized String registerUser(String username, String password, String fullName, String itLevel, String techStack, String ip, int peerPort) {
        if (username == null || username.trim().isEmpty()) {
            return "Tên đăng nhập không được để trống!";
        }
        if (password == null || password.trim().isEmpty()) {
            return "Mật khẩu không được để trống!";
        }

        Connection conn = getConnection();
        if (conn != null) {
            try {
                PreparedStatement checkPs = conn.prepareStatement("SELECT `username` FROM `users` WHERE `username` = ?");
                checkPs.setString(1, username);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    rs.close();
                    checkPs.close();
                    conn.close();
                    return "Tên đăng nhập @" + username + " đã tồn tại! Vui lòng chọn tên khác.";
                }
                rs.close();
                checkPs.close();

                PreparedStatement ps = conn.prepareStatement("INSERT INTO `users` (`username`, `password`, `ip_address`, `peer_port`) VALUES (?, ?, ?, ?)");
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, ip != null ? ip : "127.0.0.1");
                ps.setInt(4, peerPort);
                ps.executeUpdate();
                ps.close();

                PreparedStatement psProfile = conn.prepareStatement("INSERT INTO `user_profiles` (`username`, `full_name`, `it_level`, `bio`, `tech_stack`, `github_url`) VALUES (?, ?, ?, ?, ?, ?)");
                psProfile.setString(1, username);
                psProfile.setString(2, (fullName != null && !fullName.trim().isEmpty()) ? fullName : username);
                psProfile.setString(3, (itLevel != null && !itLevel.trim().isEmpty()) ? itLevel : "Junior Developer");
                psProfile.setString(4, "Lập trình viên đam mê công nghệ tại IT World.");
                psProfile.setString(5, (techStack != null && !techStack.trim().isEmpty()) ? techStack : "Java, MySQL, Git");
                psProfile.setString(6, "https://github.com/" + username);
                psProfile.executeUpdate();
                psProfile.close();

                conn.close();
                return null;
            } catch (SQLException e) {
                e.printStackTrace();
                return "Lỗi CSDL: " + e.getMessage();
            }
        }

        if (memoryProfiles.containsKey(username)) {
            return "Tên đăng nhập @" + username + " đã tồn tại!";
        }
        UserProfile p = new UserProfile(username);
        if (fullName != null && !fullName.trim().isEmpty()) p.setFullName(fullName);
        if (itLevel != null && !itLevel.trim().isEmpty()) p.setItLevel(itLevel);
        if (techStack != null && !techStack.trim().isEmpty()) p.setTechStack(techStack);
        memoryProfiles.put(username, p);
        return null;
    }

    public static synchronized String authenticateUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "Vui lòng nhập tên đăng nhập!";
        }
        if (password == null) password = "";

        Connection conn = getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT `password` FROM `users` WHERE `username` = ?");
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    conn.close();
                    return "Tài khoản @" + username + " không tồn tại! Vui lòng chuyển sang tab Đăng ký.";
                }

                String dbPass = rs.getString("password");
                rs.close();
                ps.close();
                conn.close();

                if (dbPass == null || dbPass.isEmpty() || dbPass.equals(password)) {
                    return null;
                } else {
                    return "Mật khẩu không chính xác! Vui lòng thử lại.";
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Lỗi CSDL: " + e.getMessage();
            }
        }

        return null;
    }

    // =========================================================
    // CÁC THAO TÁC NGHIỆP VỤ (DATABASE & CACHE SYNC)
    // =========================================================

    public static synchronized void upsertUser(String username, String ip, int peerPort) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO `users` (`username`, `ip_address`, `peer_port`) " +
                        "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `ip_address` = VALUES(`ip_address`), `peer_port` = VALUES(`peer_port`)");
                ps.setString(1, username);
                ps.setString(2, ip);
                ps.setInt(3, peerPort);
                ps.executeUpdate();
                ps.close();

                // Tạo hồ sơ mặc định nếu chưa tồn tại
                PreparedStatement psProfile = conn.prepareStatement("INSERT IGNORE INTO `user_profiles` (`username`, `full_name`, `it_level`, `bio`, `tech_stack`, `github_url`) " +
                        "VALUES (?, ?, 'Junior Developer', 'Lập trình viên đam mê công nghệ tại IT World.', 'Java, MySQL, Git', ?)");
                psProfile.setString(1, username);
                psProfile.setString(2, username);
                psProfile.setString(3, "https://github.com/" + username);
                psProfile.executeUpdate();
                psProfile.close();

                conn.close();
                return;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Fallback
        memoryProfiles.computeIfAbsent(username, k -> new UserProfile(username));
    }

    public static synchronized UserProfile getUserProfile(String username, String viewerUsername) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM `user_profiles` WHERE `username` = ?");
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    UserProfile p = new UserProfile(username);
                    p.setFullName(rs.getString("full_name"));
                    p.setItLevel(rs.getString("it_level"));
                    p.setBio(rs.getString("bio"));
                    p.setTechStack(rs.getString("tech_stack"));
                    p.setGithubUrl(rs.getString("github_url"));

                    // Thống kê số liệu
                    p.setFollowersCount(queryCount(conn, "SELECT COUNT(*) FROM `follows` WHERE `following` = ?", username));
                    p.setFollowingCount(queryCount(conn, "SELECT COUNT(*) FROM `follows` WHERE `follower` = ?", username));
                    p.setPostsCount(queryCount(conn, "SELECT COUNT(*) FROM `posts` WHERE `author` = ?", username));
                    p.setFollowing(queryCount(conn, "SELECT COUNT(*) FROM `follows` WHERE `follower` = ? AND `following` = ?", viewerUsername, username) > 0);

                    rs.close();
                    ps.close();
                    conn.close();
                    return p;
                }
                rs.close();
                ps.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Fallback
        UserProfile p = memoryProfiles.getOrDefault(username, new UserProfile(username));
        int followers = 0;
        int following = 0;
        for (String f : memoryFollows) {
            String[] parts = f.split(":");
            if (parts.length == 2) {
                if (parts[1].equals(username)) followers++;
                if (parts[0].equals(username)) following++;
            }
        }
        p.setFollowersCount(followers);
        p.setFollowingCount(following);

        int postCount = 0;
        for (Post post : memoryPosts) {
            if (post.getAuthor().equals(username)) postCount++;
        }
        p.setPostsCount(postCount);
        p.setFollowing(memoryFollows.contains(viewerUsername + ":" + username));
        return p;
    }

    public static synchronized boolean updateUserProfile(UserProfile profile) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("UPDATE `user_profiles` SET " +
                        "`full_name` = ?, `it_level` = ?, `bio` = ?, `tech_stack` = ?, `github_url` = ? WHERE `username` = ?");
                ps.setString(1, profile.getFullName());
                ps.setString(2, profile.getItLevel());
                ps.setString(3, profile.getBio());
                ps.setString(4, profile.getTechStack());
                ps.setString(5, profile.getGithubUrl());
                ps.setString(6, profile.getUsername());
                int rows = ps.executeUpdate();
                ps.close();
                conn.close();
                return rows > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Fallback
        memoryProfiles.put(profile.getUsername(), profile);
        return true;
    }

    public static synchronized List<Post> getFeed(String viewerUsername, boolean followingOnly) {
        List<Post> result = new ArrayList<>();
        Connection conn = getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT p.*, up.it_level as author_level FROM `posts` p " +
                        "LEFT JOIN `user_profiles` up ON p.author = up.username ";
                if (followingOnly) {
                    sql += "JOIN `follows` f ON p.author = f.following AND f.follower = ? ";
                }
                sql += "ORDER BY p.created_at DESC";

                PreparedStatement ps = conn.prepareStatement(sql);
                if (followingOnly) {
                    ps.setString(1, viewerUsername);
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String postId = rs.getString("id");
                    Post p = new Post(
                            postId,
                            rs.getString("author"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("code_snippet"),
                            rs.getString("code_language"),
                            rs.getString("tags")
                    );
                    p.setAuthorLevel(rs.getString("author_level"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        p.setCreatedAt(new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy").format(ts));
                    }

                    // Lượt thích
                    p.setLikesCount(queryCount(conn, "SELECT COUNT(*) FROM `post_likes` WHERE `post_id` = ?", postId));
                    p.setLikedByMe(queryCount(conn, "SELECT COUNT(*) FROM `post_likes` WHERE `post_id` = ? AND `username` = ?", postId, viewerUsername) > 0);

                    // Bình luận
                    PreparedStatement psCmt = conn.prepareStatement("SELECT * FROM `comments` WHERE `post_id` = ? ORDER BY `created_at` ASC");
                    psCmt.setString(1, postId);
                    ResultSet rsCmt = psCmt.executeQuery();
                    while (rsCmt.next()) {
                        Comment c = new Comment(
                                rsCmt.getString("id"),
                                rsCmt.getString("post_id"),
                                rsCmt.getString("author"),
                                rsCmt.getString("content")
                        );
                        Timestamp cts = rsCmt.getTimestamp("created_at");
                        if (cts != null) {
                            c.setCreatedAt(new java.text.SimpleDateFormat("HH:mm dd/MM").format(cts));
                        }
                        p.getComments().add(c);
                    }
                    rsCmt.close();
                    psCmt.close();

                    result.add(p);
                }
                rs.close();
                ps.close();
                conn.close();
                return result;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Fallback
        for (Post p : memoryPosts) {
            if (followingOnly && !memoryFollows.contains(viewerUsername + ":" + p.getAuthor())) {
                continue;
            }
            p.setLikedByMe(memoryLikes.contains(p.getId() + ":" + viewerUsername));
            result.add(p);
        }
        return result;
    }

    public static synchronized boolean createPost(Post post) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO `posts` " +
                        "(`id`, `author`, `title`, `content`, `code_snippet`, `code_language`, `tags`, `created_at`) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)");
                ps.setString(1, post.getId());
                ps.setString(2, post.getAuthor());

                String title = (post.getTitle() != null && !post.getTitle().trim().isEmpty())
                        ? post.getTitle().trim()
                        : "Chia sẻ kỹ thuật " + (post.getCodeLanguage() != null ? post.getCodeLanguage() : "IT World");
                String content = (post.getContent() != null && !post.getContent().trim().isEmpty())
                        ? post.getContent().trim()
                        : "Xem đoạn mã chia sẻ đính kèm bên dưới.";

                ps.setString(3, title);
                ps.setString(4, content);
                ps.setString(5, post.getCodeSnippet());
                ps.setString(6, post.getCodeLanguage());
                ps.setString(7, post.getTags());
                int rows = ps.executeUpdate();
                ps.close();
                conn.close();
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("⚠️ Lỗi chèn bài viết vào MySQL: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Fallback
        memoryPosts.add(0, post);
        return true;
    }

    public static synchronized boolean toggleLikePost(String postId, String username) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                boolean currentlyLiked = queryCount(conn, "SELECT COUNT(*) FROM `post_likes` WHERE `post_id` = ? AND `username` = ?", postId, username) > 0;
                if (currentlyLiked) {
                    PreparedStatement del = conn.prepareStatement("DELETE FROM `post_likes` WHERE `post_id` = ? AND `username` = ?");
                    del.setString(1, postId);
                    del.setString(2, username);
                    del.executeUpdate();
                    del.close();
                    conn.close();
                    return false;
                } else {
                    PreparedStatement ins = conn.prepareStatement("INSERT INTO `post_likes` (`post_id`, `username`) VALUES (?, ?)");
                    ins.setString(1, postId);
                    ins.setString(2, username);
                    ins.executeUpdate();
                    ins.close();
                    conn.close();
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Fallback
        String key = postId + ":" + username;
        if (memoryLikes.contains(key)) {
            memoryLikes.remove(key);
            for (Post p : memoryPosts) {
                if (p.getId().equals(postId)) p.setLikesCount(Math.max(0, p.getLikesCount() - 1));
            }
            return false;
        } else {
            memoryLikes.add(key);
            for (Post p : memoryPosts) {
                if (p.getId().equals(postId)) p.setLikesCount(p.getLikesCount() + 1);
            }
            return true;
        }
    }

    public static synchronized boolean addComment(Comment comment) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO `comments` (`id`, `post_id`, `author`, `content`, `created_at`) " +
                        "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)");
                ps.setString(1, comment.getId());
                ps.setString(2, comment.getPostId());
                ps.setString(3, comment.getAuthor());
                ps.setString(4, comment.getContent());
                int rows = ps.executeUpdate();
                ps.close();
                conn.close();
                return rows > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Fallback
        for (Post p : memoryPosts) {
            if (p.getId().equals(comment.getPostId())) {
                p.getComments().add(comment);
                break;
            }
        }
        return true;
    }

    public static synchronized boolean toggleFollow(String follower, String following) {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                boolean currentlyFollowing = queryCount(conn, "SELECT COUNT(*) FROM `follows` WHERE `follower` = ? AND `following` = ?", follower, following) > 0;
                if (currentlyFollowing) {
                    PreparedStatement del = conn.prepareStatement("DELETE FROM `follows` WHERE `follower` = ? AND `following` = ?");
                    del.setString(1, follower);
                    del.setString(2, following);
                    del.executeUpdate();
                    del.close();
                    conn.close();
                    return false;
                } else {
                    PreparedStatement ins = conn.prepareStatement("INSERT INTO `follows` (`follower`, `following`) VALUES (?, ?)");
                    ins.setString(1, follower);
                    ins.setString(2, following);
                    ins.executeUpdate();
                    ins.close();
                    conn.close();
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Fallback
        String key = follower + ":" + following;
        if (memoryFollows.contains(key)) {
            memoryFollows.remove(key);
            return false;
        } else {
            memoryFollows.add(key);
            return true;
        }
    }

    public static synchronized List<UserProfile> getDevList(String viewerUsername) {
        List<UserProfile> list = new ArrayList<>();
        Connection conn = getConnection();
        if (conn != null) {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT `username` FROM `user_profiles` ORDER BY `full_name` ASC");
                List<String> usernames = new ArrayList<>();
                while (rs.next()) {
                    usernames.add(rs.getString("username"));
                }
                rs.close();
                stmt.close();
                conn.close();

                for (String u : usernames) {
                    list.add(getUserProfile(u, viewerUsername));
                }
                return list;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Fallback
        for (String u : memoryProfiles.keySet()) {
            list.add(getUserProfile(u, viewerUsername));
        }
        return list;
    }

    private static int queryCount(Connection conn, String sql, String... params) {
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
}
