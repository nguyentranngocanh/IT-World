package model;

public enum MessageType {
    // 1. Phân hệ Định danh & Xác thực (Authentication & Registration)
    LOGIN,              // Yêu cầu đăng nhập kèm tài khoản và mật khẩu
    LOGIN_RESPONSE,     // Kết quả đăng nhập (Thành công / Sai mật khẩu / Đang online)
    REGISTER_REQUEST,   // Đăng ký tài khoản lập trình viên mới
    REGISTER_RESPONSE,  // Kết quả đăng ký tài khoản (Thành công / Trùng username)

    // 2. Phân hệ Chat & P2P truyền file sẵn có
    TEXT_MESSAGE,       // Gửi tin nhắn chat bình thường
    GET_USER_LIST,      // Client xin danh sách user đang online
    USER_LIST_RESPONSE, // Server trả về danh sách user
    GET_IP_REQUEST,     // Client A xin IP của Client B để chuẩn bị gửi file
    GET_IP_RESPONSE,    // Server trả về IP và Port của Client B
    FILE_TRANSFER,      // Gói tin chứa dữ liệu file truyền P2P

    // 3. Phân hệ Bảng tin công nghệ (Newsfeed & Posts)
    GET_FEED_REQUEST,       // Yêu cầu lấy danh sách bài viết
    GET_FEED_RESPONSE,      // Danh sách bài viết trả về
    CREATE_POST_REQUEST,    // Đăng bài viết công nghệ mới
    CREATE_POST_RESPONSE,   // Kết quả đăng bài
    LIKE_POST_REQUEST,      // Thả tim / Bỏ tim bài viết
    LIKE_POST_RESPONSE,     // Trả về kết quả like
    ADD_COMMENT_REQUEST,    // Thêm bình luận vào bài viết
    ADD_COMMENT_RESPONSE,   // Trả về kết quả bình luận

    // 4. Phân hệ Hồ sơ IT & Trình độ (Developer Profile)
    GET_PROFILE_REQUEST,    // Xem hồ sơ lập trình viên
    GET_PROFILE_RESPONSE,   // Trả về hồ sơ lập trình viên
    UPDATE_PROFILE_REQUEST, // Cập nhật hồ sơ & kỹ năng IT
    UPDATE_PROFILE_RESPONSE,// Kết quả cập nhật hồ sơ

    // 5. Phân hệ Cộng đồng & Theo dõi (Follow System)
    TOGGLE_FOLLOW_REQUEST,  // Theo dõi hoặc Hủy theo dõi bạn bè
    TOGGLE_FOLLOW_RESPONSE, // Kết quả cập nhật trạng thái theo dõi
    GET_DEV_LIST_REQUEST,   // Lấy danh sách cộng đồng lập trình viên
    GET_DEV_LIST_RESPONSE   // Danh sách lập trình viên trả về
}