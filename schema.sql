-- ========================================================
-- CƠ SỞ DỮ LIỆU "IT WORLD" - CỘNG ĐỒNG LẬP TRÌNH VIÊN
-- Dùng cho XAMPP MySQL (phpMyAdmin)
-- ========================================================

CREATE DATABASE IF NOT EXISTS `it_world` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `it_world`;

-- 1. Bảng tài khoản người dùng
CREATE TABLE IF NOT EXISTS `users` (
    `username` VARCHAR(50) NOT NULL PRIMARY KEY,
    `password` VARCHAR(100) DEFAULT '123456',
    `ip_address` VARCHAR(50) DEFAULT '127.0.0.1',
    `peer_port` INT DEFAULT 9000,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Bảng hồ sơ lập trình viên (Trình độ IT, Tech stack, GitHub)
CREATE TABLE IF NOT EXISTS `user_profiles` (
    `username` VARCHAR(50) NOT NULL PRIMARY KEY,
    `full_name` VARCHAR(100) DEFAULT '',
    `it_level` VARCHAR(50) DEFAULT 'Junior Developer',
    `bio` TEXT,
    `tech_stack` VARCHAR(255) DEFAULT 'Java, Spring Boot, MySQL',
    `github_url` VARCHAR(255) DEFAULT 'https://github.com',
    `avatar_color` INT DEFAULT 0,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`username`) REFERENCES `users`(`username`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Bảng bài viết công nghệ (Newsfeed & Code Snippets)
CREATE TABLE IF NOT EXISTS `posts` (
    `id` VARCHAR(50) NOT NULL PRIMARY KEY,
    `author` VARCHAR(50) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `code_snippet` TEXT,
    `code_language` VARCHAR(50) DEFAULT 'Java',
    `tags` VARCHAR(255) DEFAULT '#java',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`author`) REFERENCES `users`(`username`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Bảng lượt thích bài viết
CREATE TABLE IF NOT EXISTS `post_likes` (
    `post_id` VARCHAR(50) NOT NULL,
    `username` VARCHAR(50) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`post_id`, `username`),
    FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`username`) REFERENCES `users`(`username`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Bảng bình luận bài viết
CREATE TABLE IF NOT EXISTS `comments` (
    `id` VARCHAR(50) NOT NULL PRIMARY KEY,
    `post_id` VARCHAR(50) NOT NULL,
    `author` VARCHAR(50) NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`author`) REFERENCES `users`(`username`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Bảng theo dõi (Followers & Following)
CREATE TABLE IF NOT EXISTS `follows` (
    `follower` VARCHAR(50) NOT NULL,
    `following` VARCHAR(50) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`follower`, `following`),
    FOREIGN KEY (`follower`) REFERENCES `users`(`username`) ON DELETE CASCADE,
    FOREIGN KEY (`following`) REFERENCES `users`(`username`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========================================================
-- DỮ LIỆU MẪU ĐỂ TEST NGAY KHI VỪA IMPORT
-- ========================================================

-- Tạo các tài khoản dev mẫu
INSERT IGNORE INTO `users` (`username`, `ip_address`, `peer_port`) VALUES
('alex_dev', '127.0.0.1', 9001),
('sarah_tech', '127.0.0.1', 9002),
('david_cloud', '127.0.0.1', 9003);

-- Tạo hồ sơ năng lực IT mẫu
INSERT IGNORE INTO `user_profiles` (`username`, `full_name`, `it_level`, `bio`, `tech_stack`, `github_url`) VALUES
('alex_dev', 'Alex Ferguson', 'Senior Backend Engineer', 'Đam mê High-Concurrency, Distributed Systems và Clean Architecture.', 'Java, Spring Boot, Redis, Kafka, Docker', 'https://github.com/alex-dev'),
('sarah_tech', 'Sarah Connor', 'Solution Architect', '10 năm kinh nghiệm thiết kế hệ thống Microservices & Cloud-Native.', 'Java, Go, Kubernetes, AWS, PostgreSQL', 'https://github.com/sarah-tech'),
('david_cloud', 'David Beckham', 'DevOps / Cloud Engineer', 'Chuyên gia CI/CD Pipeline, Terraform và bảo mật hạ tầng mạng.', 'Docker, K8s, Linux, Terraform, Python', 'https://github.com/david-cloud');

-- Mối quan hệ follow mẫu
INSERT IGNORE INTO `follows` (`follower`, `following`) VALUES
('alex_dev', 'sarah_tech'),
('david_cloud', 'sarah_tech'),
('sarah_tech', 'alex_dev');

-- Bài viết công nghệ mẫu kèm Code Snippet
INSERT IGNORE INTO `posts` (`id`, `author`, `title`, `content`, `code_snippet`, `code_language`, `tags`, `created_at`) VALUES
('post_1', 'sarah_tech', 'Tối ưu Socket I/O trong Java với Virtual Threads (Java 21)', 
'Trước Java 21, mỗi kết nối Socket phải dùng 1 Platform Thread từ OS, dễ gây tràn bộ nhớ khi có hàng chục nghìn client. Giờ đây với Virtual Threads (Project Loom), việc xử lý hàng triệu connection trở nên cực kỳ nhẹ nhàng!', 
'// Tạo Executor chạy Virtual Thread cho mỗi Client kết nối\nServerSocket serverSocket = new ServerSocket(8000);\nExecutorService executor = Executors.newVirtualThreadPerTask();\n\nwhile (true) {\n    Socket socket = serverSocket.accept();\n    executor.submit(() -> handleClient(socket));\n}', 
'Java', '#java #concurrency #socket #backend', NOW() - INTERVAL 2 HOUR),

('post_2', 'alex_dev', 'Nguyên lý SOLID: Single Responsibility Principle trong Java Swing', 
'Một lỗi thường gặp khi code giao diện Java Swing là nhét tất cả code Socket, Vẽ giao diện, và Xử lý sự kiện vào chung 1 file JFrame. Hãy chia nhỏ thành View, Service và Model để code sạch và dễ bảo trì hơn rất nhiều!', 
'// Tách riêng Service mạng và UI View\npublic class PeerService {\n    public static void sendFile(String ip, int port, String path, FileCallback cb) {\n        // Xử lý stream truyền file ở luồng ngầm\n    }\n}', 
'Java', '#architecture #clean-code #java', NOW() - INTERVAL 5 HOUR);

-- Bình luận mẫu
INSERT IGNORE INTO `comments` (`id`, `post_id`, `author`, `content`, `created_at`) VALUES
('cmt_1', 'post_1', 'alex_dev', 'Chia sẻ rất hay! Virtual Threads giúp giảm tải RAM đáng kinh ngạc.', NOW() - INTERVAL 1 HOUR),
('cmt_2', 'post_1', 'david_cloud', 'Rất hữu ích cho các dự án Socket và Chat Server!', NOW() - INTERVAL 30 MINUTE);
