package client;

import model.UserProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Hộp thoại chỉnh sửa thông tin cá nhân và trình độ IT
 */
public class EditProfileDialog extends JDialog {
    private final UserProfile profile;
    private final Consumer<UserProfile> onSaved;

    private JTextField txtFullName;
    private JComboBox<String> cbLevel;
    private JTextArea txtBio;
    private JTextField txtTechStack;
    private JTextField txtGithub;

    public EditProfileDialog(Frame parent, UserProfile profile, Consumer<UserProfile> onSaved) {
        super(parent, "Chỉnh sửa hồ sơ lập trình viên • IT World", true);
        setIconImage(AppIcons.getAppLogoImage(64));
        this.profile = profile;
        this.onSaved = onSaved;

        setSize(520, 580);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(248, 250, 252));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel lblTitle = new JLabel("Cập nhật hồ sơ IT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(new Color(15, 23, 42));
        header.add(lblTitle, BorderLayout.WEST);

        root.add(header, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Họ và tên
        form.add(createLabel("HỌ VÀ TÊN"));
        form.add(Box.createVerticalStrut(6));
        txtFullName = new JTextField(profile.getFullName());
        txtFullName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtFullName.setBorder(createInputBorder());
        txtFullName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(txtFullName);
        form.add(Box.createVerticalStrut(14));

        // 2. Trình độ IT
        form.add(createLabel("TRÌNH ĐỘ / VỊ TRÍ IT (LEVEL)"));
        form.add(Box.createVerticalStrut(6));
        String[] levels = {
                "Intern / Thực tập sinh",
                "Fresher Developer",
                "Junior Developer",
                "Middle Developer",
                "Senior Developer",
                "Tech Lead",
                "Solution Architect",
                "DevOps / Cloud Engineer",
                "Full-Stack Developer",
                "AI / Data Specialist"
        };
        cbLevel = new JComboBox<>(levels);
        cbLevel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbLevel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbLevel.setBackground(Color.WHITE);

        // Chọn item phù hợp với profile hiện tại
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equalsIgnoreCase(profile.getItLevel()) || levels[i].startsWith(profile.getItLevel())) {
                cbLevel.setSelectedIndex(i);
                break;
            }
        }
        form.add(cbLevel);
        form.add(Box.createVerticalStrut(14));

        // 3. Giới thiệu bản thân (Bio)
        form.add(createLabel("GIỚI THIỆU BẢN THÂN (BIO)"));
        form.add(Box.createVerticalStrut(6));
        txtBio = new JTextArea(profile.getBio(), 3, 20);
        txtBio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBio.setLineWrap(true);
        txtBio.setWrapStyleWord(true);
        txtBio.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane scrollBio = new JScrollPane(txtBio);
        scrollBio.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        scrollBio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        form.add(scrollBio);
        form.add(Box.createVerticalStrut(14));

        // 4. Kỹ năng & Công nghệ (Tech Stack)
        form.add(createLabel("KỸ NĂNG & CÔNG NGHỆ (PHÂN CÁCH BẰNG DẤU PHẨY)"));
        form.add(Box.createVerticalStrut(6));
        txtTechStack = new JTextField(profile.getTechStack());
        txtTechStack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTechStack.setBorder(createInputBorder());
        txtTechStack.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(txtTechStack);
        form.add(Box.createVerticalStrut(14));

        // 5. GitHub URL
        form.add(createLabel("ĐƯỜNG DẪN GITHUB (GITHUB PROFILE URL)"));
        form.add(Box.createVerticalStrut(6));
        txtGithub = new JTextField(profile.getGithubUrl());
        txtGithub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGithub.setBorder(createInputBorder());
        txtGithub.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(txtGithub);

        root.add(form, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setForeground(new Color(100, 116, 139));
        btnCancel.setBackground(new Color(241, 245, 249));
        btnCancel.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        footer.add(btnCancel);

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveProfile());
        footer.add(btnSave);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(100, 116, 139));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private javax.swing.border.Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 10, 6, 10)
        );
    }

    private void saveProfile() {
        profile.setFullName(txtFullName.getText().trim());
        profile.setItLevel((String) cbLevel.getSelectedItem());
        profile.setBio(txtBio.getText().trim());
        profile.setTechStack(txtTechStack.getText().trim());
        profile.setGithubUrl(txtGithub.getText().trim());

        if (onSaved != null) {
            onSaved.accept(profile);
        }
        dispose();
    }
}
