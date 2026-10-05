package model;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;    // Loại tin nhắn (bắt buộc)
    private String sender;       // Người gửi
    private String receiver;     // Người nhận (nếu để trống là gửi cho tất cả)

    private String content;      // Nội dung chat hoặc thông điệp phản hồi
    private String password;     // Mật khẩu xác thực đăng nhập / đăng ký
    private boolean success;     // Trạng thái thành công / thất bại của yêu cầu

    // 3 biến dưới đây dành riêng cho việc truyền file P2P
    private String fileName;
    private long fileSize;
    private byte[] fileData;

    // Biến phụ để chứa danh sách User, UserProfile, Post hoặc IP trả về từ Server
    private Object payload;

    // Constructor cơ bản
    public Message(MessageType type, String sender, String receiver) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
    }

    // --- Generate các hàm Getter và Setter ---
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}