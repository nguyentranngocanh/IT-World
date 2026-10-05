package client;

import model.Comment;
import model.Post;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Component hiển thị một bài viết công nghệ (Post Card)
 * Hỗ trợ: Avatar, Huy hiệu Trình độ IT, Đoạn mã lập trình (Code Snippet) có nút Copy, Thả tim và Bình luận.
 */
public class PostCardComponent extends JPanel {
    private final Post post;
    private final String currentUsername;
    private final Consumer<String> onLikeClicked;
    private final BiConsumer<String, String> onAddComment;
    private final Consumer<String> onAuthorClicked;

    private JButton btnLike;
    private JPanel commentsContainer;
    private boolean commentsVisible = false;

    public PostCardComponent(Post post, String currentUsername,
                             Consumer<String> onLikeClicked,
                             BiConsumer<String, String> onAddComment,
                             Consumer<String> onAuthorClicked) {
        this.post = post;
        this.currentUsername = currentUsername;
        this.onLikeClicked = onLikeClicked;
        this.onAddComment = onAddComment;
        this.onAuthorClicked = onAuthorClicked;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));
        setMaximumSize(new Dimension(860, Integer.MAX_VALUE));

        initUI();
    }

    private void initUI() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // 1. Header (Avatar, Author, IT Level, Timestamp)
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        AvatarPanel avatar = new AvatarPanel(post.getAuthor(), 42, false);
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onAuthorClicked != null) onAuthorClicked.accept(post.getAuthor());
            }
        });
        header.add(avatar, BorderLayout.WEST);

        JPanel authorMeta = new JPanel();
        authorMeta.setLayout(new BoxLayout(authorMeta, BoxLayout.Y_AXIS));
        authorMeta.setOpaque(false);

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nameRow.setOpaque(false);

        JLabel lblAuthor = new JLabel(post.getAuthor());
        lblAuthor.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAuthor.setForeground(new Color(15, 23, 42));
        lblAuthor.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAuthor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onAuthorClicked != null) onAuthorClicked.accept(post.getAuthor());
            }
        });
        nameRow.add(lblAuthor);

        // IT Level Badge
        JLabel lblLevel = new JLabel(post.getAuthorLevel()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255)); // Light Blue
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(191, 219, 254));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblLevel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLevel.setForeground(new Color(37, 99, 235));
        lblLevel.setBorder(new EmptyBorder(2, 8, 2, 8));
        nameRow.add(lblLevel);

        JLabel lblTime = new JLabel("• " + post.getCreatedAt());
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(new Color(100, 116, 139));
        nameRow.add(lblTime);

        authorMeta.add(nameRow);
        header.add(authorMeta, BorderLayout.CENTER);

        mainContent.add(header);
        mainContent.add(Box.createVerticalStrut(12));

        // 2. Tiêu đề bài viết
        if (post.getTitle() != null && !post.getTitle().isEmpty()) {
            JLabel lblTitle = new JLabel("<html><b>" + post.getTitle() + "</b></html>");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTitle.setForeground(new Color(15, 23, 42));
            lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainContent.add(lblTitle);
            mainContent.add(Box.createVerticalStrut(8));
        }

        // 3. Nội dung văn bản
        JTextArea txtContent = new JTextArea(post.getContent());
        txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtContent.setForeground(new Color(51, 65, 85));
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(false);
        txtContent.setOpaque(false);
        txtContent.setBorder(null);
        txtContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(txtContent);
        mainContent.add(Box.createVerticalStrut(12));

        // 4. Đoạn mã lập trình (Code Snippet Box)
        if (post.getCodeSnippet() != null && !post.getCodeSnippet().trim().isEmpty()) {
            JPanel codeBox = createCodeSnippetBox(post.getCodeSnippet(), post.getCodeLanguage());
            codeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainContent.add(codeBox);
            mainContent.add(Box.createVerticalStrut(12));
        }

        // 5. Thẻ Tag (#tags)
        if (!post.getTagList().isEmpty()) {
            JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            tagPanel.setOpaque(false);
            tagPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (String tag : post.getTagList()) {
                JLabel lblTag = new JLabel(tag);
                lblTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblTag.setForeground(new Color(79, 70, 229)); // Indigo
                lblTag.setBackground(new Color(238, 242, 255));
                lblTag.setOpaque(true);
                lblTag.setBorder(new EmptyBorder(3, 8, 3, 8));
                tagPanel.add(lblTag);
            }
            mainContent.add(tagPanel);
            mainContent.add(Box.createVerticalStrut(14));
        }

        // 6. Thanh tương tác (Thích, Bình luận)
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionBar.setOpaque(false);
        actionBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnLike = new JButton((post.isLikedByMe() ? "Đã thích (" : "Thích (") + post.getLikesCount() + ")") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                AppIcons.drawHeart(g2, 10, (getHeight() - 13) / 2, 13, post.isLikedByMe(),
                        post.isLikedByMe() ? new Color(225, 29, 72) : new Color(148, 163, 184));
                g2.dispose();
            }
        };
        btnLike.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLike.setForeground(post.isLikedByMe() ? new Color(225, 29, 72) : new Color(71, 85, 105));
        btnLike.setBackground(post.isLikedByMe() ? new Color(255, 241, 242) : new Color(241, 245, 249));
        btnLike.setBorder(new EmptyBorder(6, 28, 6, 14));
        btnLike.setFocusPainted(false);
        btnLike.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLike.addActionListener(e -> {
            if (onLikeClicked != null) onLikeClicked.accept(post.getId());
        });
        actionBar.add(btnLike);

        JButton btnCommentToggle = new JButton("Bình luận (" + post.getComments().size() + ")");
        btnCommentToggle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCommentToggle.setForeground(new Color(71, 85, 105));
        btnCommentToggle.setBackground(new Color(241, 245, 249));
        btnCommentToggle.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnCommentToggle.setFocusPainted(false);
        btnCommentToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCommentToggle.addActionListener(e -> toggleComments());
        actionBar.add(btnCommentToggle);

        mainContent.add(actionBar);

        // 7. Khu vực bình luận (Collapsible)
        commentsContainer = new JPanel();
        commentsContainer.setLayout(new BoxLayout(commentsContainer, BoxLayout.Y_AXIS));
        commentsContainer.setOpaque(false);
        commentsContainer.setBorder(new EmptyBorder(12, 0, 0, 0));
        commentsContainer.setVisible(false);

        buildCommentsUI();
        mainContent.add(commentsContainer);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createCodeSnippetBox(String code, String language) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(new Color(15, 23, 42)); // Slate-900 Dark Code Theme
        box.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));

        // Code Header Bar
        JPanel codeHeader = new JPanel(new BorderLayout());
        codeHeader.setBackground(new Color(30, 41, 59));
        codeHeader.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel lblLang = new JLabel("</> " + (language != null ? language.toUpperCase() : "CODE"));
        lblLang.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLang.setForeground(new Color(148, 163, 184));
        codeHeader.add(lblLang, BorderLayout.WEST);

        JButton btnCopy = new JButton("Sao chép");
        btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCopy.setForeground(new Color(203, 213, 225));
        btnCopy.setBackground(new Color(51, 65, 85));
        btnCopy.setBorder(new EmptyBorder(3, 8, 3, 8));
        btnCopy.setFocusPainted(false);
        btnCopy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCopy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
            btnCopy.setText("Đã sao chép!");
            Timer timer = new Timer(2000, evt -> btnCopy.setText("Sao chép"));
            timer.setRepeats(false);
            timer.start();
        });
        codeHeader.add(btnCopy, BorderLayout.EAST);

        box.add(codeHeader, BorderLayout.NORTH);

        // Code Text Area
        JTextArea txtCode = new JTextArea(code);
        txtCode.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtCode.setForeground(new Color(241, 245, 249));
        txtCode.setBackground(new Color(15, 23, 42));
        txtCode.setCaretColor(Color.WHITE);
        txtCode.setEditable(false);
        txtCode.setBorder(new EmptyBorder(10, 12, 10, 12));
        txtCode.setTabSize(4);
        box.add(txtCode, BorderLayout.CENTER);

        return box;
    }

    private void toggleComments() {
        commentsVisible = !commentsVisible;
        commentsContainer.setVisible(commentsVisible);
        revalidate();
        repaint();
    }

    private void buildCommentsUI() {
        commentsContainer.removeAll();

        // Danh sách các bình luận hiện tại
        for (Comment cmt : post.getComments()) {
            JPanel cmtRow = new JPanel(new BorderLayout(8, 0));
            cmtRow.setOpaque(false);
            cmtRow.setBorder(new EmptyBorder(6, 0, 6, 0));

            AvatarPanel cmtAvatar = new AvatarPanel(cmt.getAuthor(), 28, false);
            cmtRow.add(cmtAvatar, BorderLayout.WEST);

            JPanel cmtBubble = new JPanel();
            cmtBubble.setLayout(new BoxLayout(cmtBubble, BoxLayout.Y_AXIS));
            cmtBubble.setBackground(new Color(248, 250, 252));
            cmtBubble.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                    new EmptyBorder(6, 10, 6, 10)
            ));

            JPanel cmtHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            cmtHeader.setOpaque(false);

            JLabel lblCmtAuthor = new JLabel(cmt.getAuthor());
            lblCmtAuthor.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblCmtAuthor.setForeground(new Color(15, 23, 42));
            cmtHeader.add(lblCmtAuthor);

            JLabel lblCmtTime = new JLabel("• " + cmt.getCreatedAt());
            lblCmtTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblCmtTime.setForeground(new Color(148, 163, 184));
            cmtHeader.add(lblCmtTime);

            cmtBubble.add(cmtHeader);
            cmtBubble.add(Box.createVerticalStrut(3));

            JLabel lblCmtContent = new JLabel("<html>" + cmt.getContent() + "</html>");
            lblCmtContent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblCmtContent.setForeground(new Color(51, 65, 85));
            cmtBubble.add(lblCmtContent);

            cmtRow.add(cmtBubble, BorderLayout.CENTER);
            commentsContainer.add(cmtRow);
        }

        // Ô nhập bình luận mới
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.setBorder(new EmptyBorder(8, 0, 4, 0));

        JTextField txtInput = new JTextField();
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JButton btnSubmit = new JButton("Gửi");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBackground(new Color(37, 99, 235));
        btnSubmit.setBorder(new EmptyBorder(6, 16, 6, 16));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Runnable submitAction = () -> {
            String text = txtInput.getText().trim();
            if (!text.isEmpty()) {
                if (onAddComment != null) onAddComment.accept(post.getId(), text);
                txtInput.setText("");
            }
        };

        btnSubmit.addActionListener(e -> submitAction.run());
        txtInput.addActionListener(e -> submitAction.run());

        inputRow.add(txtInput, BorderLayout.CENTER);
        inputRow.add(btnSubmit, BorderLayout.EAST);
        commentsContainer.add(inputRow);

        commentsContainer.revalidate();
        commentsContainer.repaint();
    }

    public void updateLikeStatus(boolean isLiked, int newCount) {
        post.setLikedByMe(isLiked);
        post.setLikesCount(newCount);
        btnLike.setText((isLiked ? "Đã thích (" : "Thích (") + newCount + ")");
        btnLike.setForeground(isLiked ? new Color(225, 29, 72) : new Color(71, 85, 105));
        btnLike.setBackground(isLiked ? new Color(255, 241, 242) : new Color(241, 245, 249));
        btnLike.repaint();
    }
}
