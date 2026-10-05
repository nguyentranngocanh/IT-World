package client;

import model.Message;
import model.MessageType;
import model.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1"; // Đổi thành IP của Server nếu chạy 2 máy khác nhau
    private static final int SERVER_PORT = 8000;

    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static String myUsername;
    private static String pendingFilePath = "";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Nhập tên đăng nhập của bạn: ");
        myUsername = scanner.nextLine();

        // Giả lập một Port ngẫu nhiên từ 9000-9999 để làm Port nhận file P2P
        int myPeerPort = 9000 + (int)(Math.random() * 1000);

        // [QUAN TRỌNG] Khởi động Server ngầm để sẵn sàng nhận file từ máy khác
        startPeerServer(myPeerPort);

        try {
            // 1. KẾT NỐI ĐẾN SERVER TRUNG TÂM
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("-> Đã kết nối đến Server thành công!");

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // 2. GỬI GÓI TIN ĐĂNG NHẬP
            User myUser = new User(myUsername, socket.getLocalAddress().getHostAddress(), myPeerPort);
            Message loginMsg = new Message(MessageType.LOGIN, myUsername, "Server");
            loginMsg.setPayload(myUser);
            out.writeObject(loginMsg);
            out.flush();

            // 3. MỞ LUỒNG (THREAD) LẮNG NGHE TIN NHẮN TỪ SERVER
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        Message receivedMsg = (Message) in.readObject();
                        handleReceivedMessage(receivedMsg);
                    }
                } catch (Exception e) {
                    System.out.println("Đã ngắt kết nối với Server.");
                }
            });
            readThread.start();

            // 4. LUỒNG CHÍNH: XỬ LÝ NHẬP PHÍM
            System.out.println("=====================================================");
            System.out.println("Cú pháp chat: @TênNgườiNhận Nội_dung_tin_nhắn");
            System.out.println("Cú pháp file: #FILE @TênNgườiNhận Đường_dẫn_file");
            System.out.println("Ví dụ gửi file: #FILE @Bob C:\\tailieu.pdf");
            System.out.println("=====================================================");

            while (true) {
                String input = scanner.nextLine();

                if (input.trim().isEmpty()) continue;

                // Cú pháp truyền file P2P
                if (input.startsWith("#FILE ")) {
                    String[] parts = input.split(" ", 3);
                    if (parts.length == 3 && parts[1].startsWith("@")) {
                        String receiver = parts[1].substring(1);
                        pendingFilePath = parts[2]; // Lưu tạm đường dẫn file chờ gửi

                        // Gửi yêu cầu xin IP lên Server
                        Message ipReqMsg = new Message(MessageType.GET_IP_REQUEST, myUsername, receiver);
                        out.writeObject(ipReqMsg);
                        out.flush();
                        System.out.println("[Hệ thống] Đang xin IP của " + receiver + " từ Server...");
                    } else {
                        System.out.println("[Hệ thống] Sai cú pháp! Mẫu: #FILE @Bob C:\\tailieu.pdf");
                    }
                    continue; // Bỏ qua logic text bên dưới để vòng lặp quay lại từ đầu
                }

                // Cú pháp chat Text thông thường
                if (input.startsWith("@")) {
                    int firstSpaceIndex = input.indexOf(" ");
                    if (firstSpaceIndex > 1) {
                        String receiver = input.substring(1, firstSpaceIndex);
                        String content = input.substring(firstSpaceIndex + 1);

                        Message chatMsg = new Message(MessageType.TEXT_MESSAGE, myUsername, receiver);
                        chatMsg.setContent(content);

                        out.writeObject(chatMsg);
                        out.flush();
                    } else {
                        System.out.println("[Hệ thống] Sai cú pháp! Vui lòng dùng: @TênNgườiNhận Nội_dung");
                    }
                } else {
                    System.out.println("[Hệ thống] Vui lòng thêm @TênNgườiNhận ở đầu câu.");
                }
            }

        } catch (IOException e) {
            System.out.println("Không thể kết nối đến Server! Vui lòng kiểm tra lại IP và Port.");
        }
    }

    // =================================================================================
    // CÁC HÀM XỬ LÝ LOGIC
    // =================================================================================

    @SuppressWarnings("unchecked")
    private static void handleReceivedMessage(Message msg) {
        switch (msg.getType()) {
            case TEXT_MESSAGE:
                System.out.println("\n[" + msg.getSender() + "] nói: " + msg.getContent());
                break;

            case USER_LIST_RESPONSE:
                List<User> onlineUsers = (List<User>) msg.getPayload();
                System.out.print("\n[Hệ thống] Đang online: ");
                for (User u : onlineUsers) {
                    System.out.print(u.getUsername() + "  ");
                }
                System.out.println();
                break;

            case GET_IP_RESPONSE:
                // Nhận được IP từ Server, tiến hành đâm thẳng vào máy kia (P2P)
                User targetUser = (User) msg.getPayload();
                System.out.println("[Hệ thống] Đã nhận IP của " + targetUser.getUsername() + " (" + targetUser.getIpAddress() + ":" + targetUser.getPeerPort() + ")");
                System.out.println("[P2P] Bắt đầu truyền file...");

                // Kích hoạt hàm truyền byte file
                sendFileP2P(targetUser.getIpAddress(), targetUser.getPeerPort(), pendingFilePath);
                break;

            default:
                break;
        }
    }

    // Mở một ServerSocket ẩn để luôn sẵn sàng nhận file từ Client khác
    private static void startPeerServer(int peerPort) {
        Thread p2pServerThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(peerPort)) {
                System.out.println("[P2P] Máy bạn đã sẵn sàng nhận file ở Port " + peerPort);

                while (true) {
                    Socket senderSocket = serverSocket.accept();
                    System.out.println("\n[P2P] Có kết nối truyền file đang đến!");
                    receiveFileP2P(senderSocket); // Gọi hàm nhận byte file
                }
            } catch (IOException e) {
                System.out.println("[P2P] Lỗi mở cổng P2P: " + e.getMessage());
            }
        });
        p2pServerThread.setDaemon(true);
        p2pServerThread.start();
    }

    // Thuật toán GỬI file bằng DataOutputStream (Chạy trên Thread riêng để không block Console)
    private static void sendFileP2P(String ip, int port, String filePath) {
        new Thread(() -> {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("[Lỗi] File không tồn tại: " + filePath);
                return;
            }

            try (Socket socket = new Socket(ip, port);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 FileInputStream fis = new FileInputStream(file)) {

                // 1. Gửi Tên file và Kích thước file trước (Header)
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());

                // 2. Cắt file thành các mảng byte nhỏ (4KB) để truyền đi
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, bytesRead);
                }

                System.out.println("[P2P] => Đã gửi file thành công!");

            } catch (IOException e) {
                System.out.println("[P2P] => Lỗi gửi file: " + e.getMessage());
            }
        }).start();
    }

    // Thuật toán NHẬN file bằng DataInputStream
    private static void receiveFileP2P(Socket socket) {
        new Thread(() -> {
            try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {

                // 1. Đọc Tên file và Kích thước file từ Header
                String fileName = dis.readUTF();
                long fileSize = dis.readLong();

                // Thêm tiền tố để tránh ghi đè file có sẵn
                String savePath = "nhan_duoc_" + fileName;

                // 2. Nhận các mảng byte và ráp thành file hoàn chỉnh
                try (FileOutputStream fos = new FileOutputStream(savePath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalRead = 0;

                    // Chỉ đọc đúng kích thước file để tránh kẹt luồng (blocking)
                    while (totalRead < fileSize && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) > 0) {
                        fos.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                    }
                }
                System.out.println("[P2P] <= Đã nhận file lưu tại: " + savePath);

            } catch (IOException e) {
                System.out.println("[P2P] <= Lỗi nhận file: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException e) {}
            }
        }).start();
    }
}