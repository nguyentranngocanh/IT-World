package client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerService {
    public interface FileCallback {
        void onFileSent(String receiver, String fileName, String filePath);
        void onFileReceived(String sender, String fileName, String filePath);
        void onError(String target, String errorMsg);
    }

    public static void startPeerServer(int port, FileCallback callback) {
        Thread t = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                while (true) {
                    Socket s = ss.accept();
                    receiveFile(s, callback);
                }
            } catch (IOException e) {}
        });
        t.setDaemon(true);
        t.start();
    }

    private static void receiveFile(Socket s, FileCallback callback) {
        new Thread(() -> {
            try (DataInputStream dis = new DataInputStream(s.getInputStream())) {
                String sender = dis.readUTF();
                String fname = dis.readUTF();
                long size = dis.readLong();
                File f = new File("NhanDuoc_" + fname);
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    byte[] buf = new byte[4096];
                    int read;
                    long total = 0;
                    while (total < size && (read = dis.read(buf, 0, (int) Math.min(buf.length, size - total))) > 0) {
                        fos.write(buf, 0, read);
                        total += read;
                    }
                    fos.flush();
                    if (callback != null) {
                        callback.onFileReceived(sender, fname, f.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onError("System", "Lỗi khi nhận file P2P");
                }
            }
        }).start();
    }

    public static void sendFile(String ip, int port, String myUsername, String path, String receiver, FileCallback callback) {
        new Thread(() -> {
            File f = new File(path);
            if (!f.exists()) {
                if (callback != null) callback.onError(receiver, "Tệp không tồn tại: " + path);
                return;
            }
            try (Socket s = new Socket(ip, port);
                 DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                 FileInputStream fis = new FileInputStream(f)) {
                dos.writeUTF(myUsername);
                dos.writeUTF(f.getName());
                dos.writeLong(f.length());
                byte[] buf = new byte[4096];
                int read;
                while ((read = fis.read(buf)) > 0) dos.write(buf, 0, read);
                dos.flush();
                if (callback != null) {
                    callback.onFileSent(receiver, f.getName(), f.getAbsolutePath());
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onError(receiver, "Không thể kết nối P2P tới " + receiver + " (" + ip + ":" + port + ")");
                }
            }
        }).start();
    }
}