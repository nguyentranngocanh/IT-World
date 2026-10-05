package client;

import model.Post;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Hộp thoại đăng bài viết công nghệ mới
 * Cho phép nhập Tiêu đề, Nội dung, Chọn ngôn ngữ lập trình, Nhập Code Snippet và Gắn Tags.
 */
public class CreatePostDialog extends JDialog {
    private final String authorUsername;
    private final Consumer<Post> onCreated;

    private JTextField txtTitle;
    private JTextArea txtContent;
    private JComboBox<String> cbLanguage;
    private JTextArea txtCodeSnippet;
    private JTextField txtTags;

    public CreatePostDialog(Frame parent, String authorUsername, Consumer<Post> onCreated) {
        super(parent, "Đăng bài viết công nghệ • IT World", true);
        setIconImage(AppIcons.getAppLogoImage(64));
        this.authorUsername = authorUsername;
        this.onCreated = onCreated;

        setSize(620, 680);
        setLocationRelativeTo(parent);
        setResizable(true);

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

        JLabel lblTitle = new JLabel("Chia sẻ bài viết & Mã nguồn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        header.add(lblTitle, BorderLayout.WEST);

        root.add(header, BorderLayout.NORTH);

        // Body Form (Scrollable)
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Tiêu đề bài viết
        form.add(createLabel("TIÊU ĐỀ BÀI VIẾT"));
        form.add(Box.createVerticalStrut(6));
        txtTitle = new JTextField();
        txtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTitle.setBorder(createInputBorder());
        txtTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(txtTitle);
        form.add(Box.createVerticalStrut(14));

        // 2. Nội dung bài viết
        form.add(createLabel("NỘI DUNG CHIA SẺ"));
        form.add(Box.createVerticalStrut(6));
        txtContent = new JTextArea(4, 30);
        txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane scrollContent = new JScrollPane(txtContent);
        scrollContent.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        scrollContent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        form.add(scrollContent);
        form.add(Box.createVerticalStrut(14));

        // 3. Ngôn ngữ lập trình
        form.add(createLabel("NGÔN NGỮ LẬP TRÌNH (TÙY CHỌN)"));
        form.add(Box.createVerticalStrut(6));
        String[] languages = {"Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "SQL", "Go", "Kotlin", "HTML/CSS", "Khác"};
        cbLanguage = new JComboBox<>(languages);
        cbLanguage.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbLanguage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbLanguage.setBackground(Color.WHITE);
        form.add(cbLanguage);
        form.add(Box.createVerticalStrut(14));

        // 4. Đoạn mã lập trình (Code Snippet)
        form.add(createLabel("ĐOẠN MÃ LẬP TRÌNH (CODE SNIPPET)"));
        form.add(Box.createVerticalStrut(6));
        txtCodeSnippet = new JTextArea(6, 30);
        txtCodeSnippet.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtCodeSnippet.setBackground(new Color(15, 23, 42)); // Slate-900 Dark Code Theme
        txtCodeSnippet.setForeground(new Color(241, 245, 249));
        txtCodeSnippet.setCaretColor(Color.WHITE);
        txtCodeSnippet.setTabSize(4);
        txtCodeSnippet.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane scrollCode = new JScrollPane(txtCodeSnippet);
        scrollCode.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        scrollCode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        form.add(scrollCode);
        form.add(Box.createVerticalStrut(14));

        // 5. Thẻ Tag
        form.add(createLabel("THẺ PHÂN LOẠI (TAGS - CÁCH NHAU BẰNG DẤU PHẨY HOẶC KHOẢNG TRẮNG)"));
        form.add(Box.createVerticalStrut(6));
        txtTags = new JTextField("#java, #backend, #itworld");
        txtTags.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTags.setBorder(createInputBorder());
        txtTags.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(txtTags);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        root.add(formScroll, BorderLayout.CENTER);

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

        JButton btnSubmit = new JButton("Đăng bài viết");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBackground(new Color(37, 99, 235));
        btnSubmit.setBorder(new EmptyBorder(8, 22, 8, 22));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.addActionListener(e -> submitPost());
        footer.add(btnSubmit);

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

    private void submitPost() {
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();
        String code = txtCodeSnippet.getText().trim();
        String lang = (String) cbLanguage.getSelectedItem();
        String tags = txtTags.getText().trim();

        if (title.isEmpty() && content.isEmpty() && code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tiêu đề hoặc nội dung bài viết!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (title.isEmpty()) {
            if (!code.isEmpty()) {
                title = "Chia sẻ đoạn mã " + (lang != null ? lang : "kỹ thuật");
            } else if (!content.isEmpty()) {
                title = content.length() > 50 ? content.substring(0, 47) + "..." : content;
            } else {
                title = "Chia sẻ kỹ thuật từ @" + authorUsername;
            }
        }

        if (content.isEmpty()) {
            if (!code.isEmpty()) {
                content = "Xem chi tiết đoạn mã nguồn " + (lang != null ? lang : "") + " đính kèm bên dưới.";
            } else {
                content = title;
            }
        }

        String postId = "post_" + UUID.randomUUID().toString().substring(0, 8);
        Post p = new Post(postId, authorUsername, title, content, code.isEmpty() ? null : code, lang, tags);

        if (onCreated != null) {
            onCreated.accept(p);
        }
        dispose();
    }
}
