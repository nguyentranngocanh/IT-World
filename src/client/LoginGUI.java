package client;

import model.Message;
import model.MessageType;
import model.User;
import model.UserProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.prefs.Preferences;

/**
 * Cửa sổ Đăng nhập & Đăng ký tài khoản IT World
 * Hỗ trợ:
 * 1. Chuyển đổi linh hoạt giữa [Đăng nhập] và [Đăng ký tài khoản]
 * 2. Tự động ghi nhớ địa chỉ IP máy chủ (không cần nhập lại mỗi lần)
 * 3. Bảo mật xác thực qua XAMPP MySQL
 */
public class LoginGUI extends JFrame {
    private static final int SERVER_PORT = 8000;
    private static final String PREF_SERVER_IP = "ITWORLD_SERVER_IP";

    // Trạng thái hiện tại: true = Đăng nhập, false = Đăng ký
    private boolean isLoginMode = true;
    private String serverIp;

    // Design System
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);       // #2563EB
    private static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216); // #1D4ED8
    private static final Color COLOR_BG = new Color(248, 250, 252);          // #F8FAFC
    private static final Color COLOR_BORDER = new Color(226, 232, 240);      // #E2E8F0
    private static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);      // #0F172A
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // #64748B

    // Các thành phần UI Đăng nhập
    private ModernInput txtLoginUser;
    private ModernPasswordInput txtLoginPass;
    private ModernButton btnLoginSubmit;

    // Các thành phần UI Đăng ký
    private ModernInput txtRegUser;
    private ModernPasswordInput txtRegPass;
    private ModernPasswordInput txtRegConfirmPass;
    private ModernInput txtRegFullName;
    private JComboBox<String> cbRegLevel;
    private ModernButton btnRegSubmit;

    // Tabs & Thông báo
    private JButton tabLogin;
    private JButton tabRegister;
    private JPanel formContainer;
    private CardLayout formCardLayout;
    private AlertBanner alertBanner;
    private JLabel lblServerConfig;

    public LoginGUI() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Đọc địa chỉ IP đã lưu từ lần trước (Mặc định 127.0.0.1)
        Preferences prefs = Preferences.userNodeForPackage(LoginGUI.class);
        serverIp = prefs.get(PREF_SERVER_IP, "127.0.0.1");

        setTitle("IT World • Nền tảng Cộng đồng Lập trình viên");
        setIconImage(AppIcons.getAppLogoImage(64));
        setSize(440, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_BG);

        initUI();
    }

    private void initUI() {
        JPanel mainCard = new JPanel();
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBackground(Color.WHITE);
        mainCard.setBorder(new EmptyBorder(28, 32, 28, 32));

        // 1. Logo Brand Badge Vector
        JComponent lblLogo = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppIcons.drawLogo(g2, 0, 0, getWidth());
                g2.dispose();
            }
        };
        lblLogo.setPreferredSize(new Dimension(56, 56));
        lblLogo.setMaximumSize(new Dimension(56, 56));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Tiêu đề
        JLabel lblTitle = new JLabel("IT World", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Cộng đồng Chia sẻ Mã nguồn & Lập trình viên", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(COLOR_TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Tab Switcher ([Đăng nhập] | [Đăng ký])
        JPanel tabSwitcher = new JPanel(new GridLayout(1, 2, 8, 0));
        tabSwitcher.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tabSwitcher.setOpaque(false);

        tabLogin = createTabButton("Đăng nhập", true);
        tabRegister = createTabButton("Đăng ký", false);

        tabLogin.addActionListener(e -> switchMode(true));
        tabRegister.addActionListener(e -> switchMode(false));

        tabSwitcher.add(tabLogin);
        tabSwitcher.add(tabRegister);

        // 4. Thẻ cảnh báo lỗi / thành công
        alertBanner = new AlertBanner();
        alertBanner.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 5. Card Container chứa 2 Form (Đăng nhập & Đăng ký)
        formCardLayout = new CardLayout();
        formContainer = new JPanel(formCardLayout);
        formContainer.setOpaque(false);

        formContainer.add(createLoginForm(), "LOGIN");
        formContainer.add(createRegisterForm(), "REGISTER");

        // 6. Cấu hình IP thông minh (Tự động nhớ, bấm vào đổi)
        JPanel serverConfigPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        serverConfigPanel.setOpaque(false);

        lblServerConfig = new JLabel("● Máy chủ: " + serverIp + " (Bấm để đổi IP)");
        lblServerConfig.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblServerConfig.setForeground(COLOR_PRIMARY);
        lblServerConfig.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblServerConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                promptChangeServerIp();
            }
        });
        serverConfigPanel.add(lblServerConfig);

        // Ráp nối bố cục
        mainCard.add(lblLogo);
        mainCard.add(Box.createVerticalStrut(8));
        mainCard.add(lblTitle);
        mainCard.add(Box.createVerticalStrut(4));
        mainCard.add(lblSub);
        mainCard.add(Box.createVerticalStrut(18));

        mainCard.add(tabSwitcher);
        mainCard.add(Box.createVerticalStrut(14));

        mainCard.add(alertBanner);
        mainCard.add(Box.createVerticalStrut(6));

        mainCard.add(formContainer);
        mainCard.add(Box.createVerticalStrut(14));

        mainCard.add(serverConfigPanel);

        add(mainCard);
    }

    private JPanel createLoginForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        p.add(createFieldHeader("TÊN ĐĂNG NHẬP"));
        p.add(Box.createVerticalStrut(6));
        txtLoginUser = new ModernInput("user", "Nhập tên tài khoản của bạn", "");
        p.add(txtLoginUser);
        p.add(Box.createVerticalStrut(14));

        p.add(createFieldHeader("MẬT KHẨU"));
        p.add(Box.createVerticalStrut(6));
        txtLoginPass = new ModernPasswordInput("lock", "Nhập mật khẩu");
        p.add(txtLoginPass);
        p.add(Box.createVerticalStrut(24));

        btnLoginSubmit = new ModernButton("Đăng nhập vào IT World");
        btnLoginSubmit.addActionListener(e -> processLogin());
        p.add(btnLoginSubmit);

        txtLoginUser.addActionListener(e -> txtLoginPass.requestFocus());
        txtLoginPass.addActionListener(e -> processLogin());

        return p;
    }

    private JPanel createRegisterForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        p.add(createFieldHeader("TÊN TÀI KHOẢN (DUY NHẤT)"));
        p.add(Box.createVerticalStrut(4));
        txtRegUser = new ModernInput("user", "Tên đăng nhập (vd: alex_dev)", "");
        p.add(txtRegUser);
        p.add(Box.createVerticalStrut(10));

        p.add(createFieldHeader("HỌ VÀ TÊN HIỂN THỊ"));
        p.add(Box.createVerticalStrut(4));
        txtRegFullName = new ModernInput("name", "Họ và tên của bạn", "");
        p.add(txtRegFullName);
        p.add(Box.createVerticalStrut(10));

        p.add(createFieldHeader("TRÌNH ĐỘ / VỊ TRÍ IT"));
        p.add(Box.createVerticalStrut(4));
        String[] levels = {
                "Sinh viên IT / Thực tập sinh",
                "Fresher Developer",
                "Junior Developer",
                "Middle Developer",
                "Senior Developer",
                "Tech Lead",
                "Solution Architect",
                "DevOps / Cloud Engineer"
        };
        cbRegLevel = new JComboBox<>(levels);
        cbRegLevel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRegLevel.setBackground(Color.WHITE);
        cbRegLevel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        p.add(cbRegLevel);
        p.add(Box.createVerticalStrut(10));

        p.add(createFieldHeader("MẬT KHẨU"));
        p.add(Box.createVerticalStrut(4));
        txtRegPass = new ModernPasswordInput("lock", "Mật khẩu bảo mật");
        p.add(txtRegPass);
        p.add(Box.createVerticalStrut(10));

        p.add(createFieldHeader("XÁC NHẬN MẬT KHẨU"));
        p.add(Box.createVerticalStrut(4));
        txtRegConfirmPass = new ModernPasswordInput("lock", "Nhập lại mật khẩu");
        p.add(txtRegConfirmPass);
        p.add(Box.createVerticalStrut(18));

        btnRegSubmit = new ModernButton("Tạo tài khoản ngay");
        btnRegSubmit.addActionListener(e -> processRegister());
        p.add(btnRegSubmit);

        return p;
    }

    private JButton createTabButton(String title, boolean active) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        setTabStyle(btn, active);
        return btn;
    }

    private void setTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setForeground(COLOR_PRIMARY);
            btn.setBackground(new Color(239, 246, 255));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));
        } else {
            btn.setForeground(COLOR_TEXT_MUTED);
            btn.setBackground(new Color(248, 250, 252));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));
        }
    }

    private void switchMode(boolean login) {
        this.isLoginMode = login;
        alertBanner.hideAlert();
        setTabStyle(tabLogin, login);
        setTabStyle(tabRegister, !login);
        formCardLayout.show(formContainer, login ? "LOGIN" : "REGISTER");
    }

    private void promptChangeServerIp() {
        String newIp = JOptionPane.showInputDialog(this,
                "Nhập địa chỉ IP của máy chủ Server:\n(Để nguyên 127.0.0.1 nếu chạy cùng máy, hoặc nhập IP mạng LAN của máy bạn bè)",
                serverIp);
        if (newIp != null && !newIp.trim().isEmpty()) {
            serverIp = newIp.trim();
            Preferences.userNodeForPackage(LoginGUI.class).put(PREF_SERVER_IP, serverIp);
            lblServerConfig.setText("● Máy chủ: " + serverIp + " (Bấm để đổi IP)");
        }
    }

    // =========================================================
    // XỬ LÝ ĐĂNG NHẬP & ĐĂNG KÝ QUA SOCKET & MYSQL
    // =========================================================

    private void processLogin() {
        String username = txtLoginUser.getText().trim();
        String password = new String(txtLoginPass.getPassword()).trim();

        if (username.isEmpty()) {
            alertBanner.showAlert("Vui lòng nhập tên đăng nhập!", true);
            txtLoginUser.requestFocus();
            return;
        }

        alertBanner.hideAlert();
        btnLoginSubmit.setLoading(true);

        new Thread(() -> {
            try {
                int myPeerPort = 9000 + (int) (Math.random() * 1000);
                Socket socket = new Socket(serverIp, SERVER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                String myIp = findActualLanIp(socket);

                // Gửi gói tin đăng nhập
                User myUser = new User(username, myIp, myPeerPort);
                Message loginMsg = new Message(MessageType.LOGIN, username, "Server");
                loginMsg.setPassword(password);
                loginMsg.setPayload(myUser);
                out.writeObject(loginMsg);
                out.flush();

                // Đọc phản hồi xác thực từ Server
                Message resp = (Message) in.readObject();
                if (resp != null && resp.getType() == MessageType.LOGIN_RESPONSE) {
                    if (resp.isSuccess()) {
                        // Thành công: mở cửa sổ IT World
                        SwingUtilities.invokeLater(() -> {
                            try {
                                new ITWorldMainFrame(socket, out, in, username, myPeerPort).setVisible(true);
                                this.dispose();
                            } catch (Throwable t) {
                                t.printStackTrace();
                                btnLoginSubmit.setLoading(false);
                                alertBanner.showAlert("Lỗi khởi chạy giao diện: " + t.getMessage(), true);
                            }
                        });
                    } else {
                        // Thất bại: báo lỗi sai pass hoặc chưa đăng ký
                        try { socket.close(); } catch (Exception ignored) {}
                        SwingUtilities.invokeLater(() -> {
                            btnLoginSubmit.setLoading(false);
                            alertBanner.showAlert(resp.getContent(), true);
                        });
                    }
                } else {
                    try { socket.close(); } catch (Exception ignored) {}
                    SwingUtilities.invokeLater(() -> {
                        btnLoginSubmit.setLoading(false);
                        alertBanner.showAlert("Phản hồi không xác định từ máy chủ!", true);
                    });
                }

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    btnLoginSubmit.setLoading(false);
                    alertBanner.showAlert("Không thể kết nối đến máy chủ: " + serverIp + " (Hãy chắc chắn Server.java đã bật!)", true);
                });
            }
        }).start();
    }

    private void processRegister() {
        String username = txtRegUser.getText().trim();
        String fullName = txtRegFullName.getText().trim();
        String itLevel = (String) cbRegLevel.getSelectedItem();
        String password = new String(txtRegPass.getPassword()).trim();
        String confirmPass = new String(txtRegConfirmPass.getPassword()).trim();

        if (username.isEmpty()) {
            alertBanner.showAlert("Vui lòng nhập tên tài khoản!", true);
            txtRegUser.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            alertBanner.showAlert("Vui lòng nhập mật khẩu!", true);
            txtRegPass.requestFocus();
            return;
        }
        if (!password.equals(confirmPass)) {
            alertBanner.showAlert("Mật khẩu xác nhận không khớp!", true);
            txtRegConfirmPass.requestFocus();
            return;
        }

        alertBanner.hideAlert();
        btnRegSubmit.setLoading(true);

        new Thread(() -> {
            try {
                Socket socket = new Socket(serverIp, SERVER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // Gửi gói tin đăng ký
                UserProfile profile = new UserProfile(username);
                profile.setFullName(fullName.isEmpty() ? username : fullName);
                profile.setItLevel(itLevel);
                profile.setTechStack("Java, MySQL, Git");

                Message regMsg = new Message(MessageType.REGISTER_REQUEST, username, "Server");
                regMsg.setPassword(password);
                regMsg.setPayload(profile);
                out.writeObject(regMsg);
                out.flush();

                // Đọc phản hồi đăng ký từ Server
                Message resp = (Message) in.readObject();
                socket.close();

                SwingUtilities.invokeLater(() -> {
                    btnRegSubmit.setLoading(false);
                    if (resp.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "Chúc mừng! Bạn đã đăng ký tài khoản @" + username + " thành công.\nHãy đăng nhập để trải nghiệm IT World!",
                                "Đăng ký thành công", JOptionPane.INFORMATION_MESSAGE);
                        // Tự động điền username sang tab đăng nhập
                        txtLoginUser.setText(username);
                        txtLoginPass.setText(password);
                        switchMode(true);
                    } else {
                        alertBanner.showAlert(resp.getContent(), true);
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    btnRegSubmit.setLoading(false);
                    alertBanner.showAlert("Không thể kết nối đến máy chủ: " + serverIp, true);
                });
            }
        }).start();
    }

    private String findActualLanIp(Socket socket) {
        String myIp = socket.getLocalAddress().getHostAddress();
        if (myIp.equals("127.0.0.1") || myIp.startsWith("127.")) {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    if (iface.isLoopback() || !iface.isUp()) continue;
                    Enumeration<InetAddress> addrs = iface.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress addr = addrs.nextElement();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                            return addr.getHostAddress();
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return myIp;
    }

    private JLabel createFieldHeader(String title) {
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(COLOR_TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // =========================================================
    // CÁC COMPONENT CUSTOM GIAO DIỆN
    // =========================================================

    static class ModernInput extends JTextField {
        private final String icon;
        private final String placeholder;

        public ModernInput(String icon, String placeholder, String initialText) {
            super(initialText);
            this.icon = icon;
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(COLOR_TEXT_MAIN);
            setBorder(new EmptyBorder(8, 36, 8, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setPreferredSize(new Dimension(0, 38));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(isFocusOwner() ? Color.WHITE : new Color(248, 250, 252));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g2.setColor(isFocusOwner() ? COLOR_PRIMARY : COLOR_BORDER);
            g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1.0f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);

            if (icon != null) {
                Color c = isFocusOwner() ? COLOR_PRIMARY : new Color(148, 163, 184);
                AppIcons.drawInputIcon(g2, icon, 12, getHeight() / 2, c);
            }

            super.paintComponent(g2);

            if (getText().isEmpty() && !isFocusOwner() && placeholder != null) {
                g2.setFont(getFont());
                g2.setColor(new Color(148, 163, 184));
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, getInsets().left, y);
            }

            g2.dispose();
        }
    }

    static class ModernPasswordInput extends JPasswordField {
        private final String icon;
        private final String placeholder;

        public ModernPasswordInput(String icon, String placeholder) {
            this.icon = icon;
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(COLOR_TEXT_MAIN);
            setBorder(new EmptyBorder(8, 36, 8, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setPreferredSize(new Dimension(0, 38));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(isFocusOwner() ? Color.WHITE : new Color(248, 250, 252));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            g2.setColor(isFocusOwner() ? COLOR_PRIMARY : COLOR_BORDER);
            g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1.0f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);

            if (icon != null) {
                Color c = isFocusOwner() ? COLOR_PRIMARY : new Color(148, 163, 184);
                AppIcons.drawInputIcon(g2, icon, 12, getHeight() / 2, c);
            }

            super.paintComponent(g2);

            if (getPassword().length == 0 && !isFocusOwner() && placeholder != null) {
                g2.setFont(getFont());
                g2.setColor(new Color(148, 163, 184));
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, getInsets().left, y);
            }

            g2.dispose();
        }
    }

    static class ModernButton extends JButton {
        private boolean loading = false;

        public ModernButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
            setEnabled(!loading);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (!isEnabled()) {
                g2.setColor(new Color(148, 163, 184));
                g2.fillRoundRect(0, 0, w, h, 10, 10);
            } else {
                g2.setColor(getModel().isRollover() ? COLOR_PRIMARY_HOVER : COLOR_PRIMARY);
                g2.fillRoundRect(0, 0, w, h, 10, 10);
            }

            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String txt = loading ? "Đang xử lý kết nối..." : getText();
            int tx = (w - fm.stringWidth(txt)) / 2;
            int ty = ((h - 2) - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(txt, tx, ty);

            g2.dispose();
        }
    }

    static class AlertBanner extends JPanel {
        private final JLabel lblMsg;
        private boolean isError = true;

        public AlertBanner() {
            setLayout(new BorderLayout());
            setBackground(new Color(254, 242, 242));
            setBorder(new EmptyBorder(6, 12, 6, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setVisible(false);

            lblMsg = new JLabel("Cảnh báo", SwingConstants.CENTER);
            lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblMsg.setForeground(new Color(220, 38, 38));
            add(lblMsg, BorderLayout.CENTER);
        }

        public void showAlert(String message, boolean error) {
            this.isError = error;
            setBackground(error ? new Color(254, 242, 242) : new Color(240, 253, 244));
            lblMsg.setForeground(error ? new Color(220, 38, 38) : new Color(22, 163, 74));
            lblMsg.setText(message);
            setVisible(true);
            revalidate();
            repaint();
        }

        public void hideAlert() {
            setVisible(false);
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(isError ? new Color(254, 202, 202) : new Color(187, 247, 208));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}