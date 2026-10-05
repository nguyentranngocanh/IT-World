package client;

import model.Post;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Giao diện Bảng tin công nghệ IT World
 * Quản lý danh sách bài viết, bộ lọc "Tất cả" / "Đang theo dõi", nút đăng bài và cập nhật thời gian thực.
 */
public class FeedPanel extends JPanel {
    private final String currentUsername;
    private final Consumer<Boolean> onRefreshFeed;
    private final Consumer<Post> onCreatePost;
    private final Consumer<String> onLikeClicked;
    private final BiConsumer<String, String> onAddComment;
    private final Consumer<String> onAuthorClicked;

    private JPanel postsListPanel;
    private JScrollPane scrollPane;
    private JButton btnFilterAll;
    private JButton btnFilterFollowing;
    private boolean isFollowingFilter = false;

    public FeedPanel(String currentUsername,
                     Consumer<Boolean> onRefreshFeed,
                     Consumer<Post> onCreatePost,
                     Consumer<String> onLikeClicked,
                     BiConsumer<String, String> onAddComment,
                     Consumer<String> onAuthorClicked) {
        this.currentUsername = currentUsername;
        this.onRefreshFeed = onRefreshFeed;
        this.onCreatePost = onCreatePost;
        this.onLikeClicked = onLikeClicked;
        this.onAddComment = onAddComment;
        this.onAuthorClicked = onAuthorClicked;

        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249)); // Slate-100

        initUI();
    }

    private void initUI() {
        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(14, 24, 14, 24)
        ));

        // Tiêu đề & Subtitle
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Bảng tin Công nghệ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Khám phá các chia sẻ kỹ thuật, kiến trúc và mã nguồn mới nhất");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblSub);
        topBar.add(titlePanel, BorderLayout.WEST);

        // Filter Buttons & Create Post Button
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        btnFilterAll = createFilterButton("Tất cả bài viết", true);
        btnFilterFollowing = createFilterButton("Đang theo dõi", false);

        btnFilterAll.addActionListener(e -> setFilter(false));
        btnFilterFollowing.addActionListener(e -> setFilter(true));

        actionsPanel.add(btnFilterAll);
        actionsPanel.add(btnFilterFollowing);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setForeground(new Color(71, 85, 105));
        btnRefresh.setBackground(new Color(241, 245, 249));
        btnRefresh.setBorder(new EmptyBorder(7, 12, 7, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> {
            if (onRefreshFeed != null) onRefreshFeed.accept(isFollowingFilter);
        });
        actionsPanel.add(btnRefresh);

        JButton btnCreate = new JButton("+ Đăng bài viết");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setBackground(new Color(37, 99, 235));
        btnCreate.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnCreate.setFocusPainted(false);
        btnCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreate.addActionListener(e -> openCreatePostDialog());
        actionsPanel.add(btnCreate);

        topBar.add(actionsPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Scrollable Posts List Area
        postsListPanel = new JPanel();
        postsListPanel.setLayout(new BoxLayout(postsListPanel, BoxLayout.Y_AXIS));
        postsListPanel.setOpaque(false);
        postsListPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        scrollPane = new JScrollPane(postsListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JButton createFilterButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(active ? new Color(37, 99, 235) : new Color(100, 116, 139));
        btn.setBackground(active ? new Color(239, 246, 255) : Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? new Color(191, 219, 254) : new Color(226, 232, 240), 1),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setFilter(boolean followingOnly) {
        this.isFollowingFilter = followingOnly;

        btnFilterAll.setFont(new Font("Segoe UI", !followingOnly ? Font.BOLD : Font.PLAIN, 12));
        btnFilterAll.setForeground(!followingOnly ? new Color(37, 99, 235) : new Color(100, 116, 139));
        btnFilterAll.setBackground(!followingOnly ? new Color(239, 246, 255) : Color.WHITE);

        btnFilterFollowing.setFont(new Font("Segoe UI", followingOnly ? Font.BOLD : Font.PLAIN, 12));
        btnFilterFollowing.setForeground(followingOnly ? new Color(37, 99, 235) : new Color(100, 116, 139));
        btnFilterFollowing.setBackground(followingOnly ? new Color(239, 246, 255) : Color.WHITE);

        if (onRefreshFeed != null) {
            onRefreshFeed.accept(isFollowingFilter);
        }
    }

    public void setPosts(List<Post> posts) {
        postsListPanel.removeAll();

        if (posts == null || posts.isEmpty()) {
            JPanel emptyState = new JPanel();
            emptyState.setLayout(new BoxLayout(emptyState, BoxLayout.Y_AXIS));
            emptyState.setOpaque(false);
            emptyState.setBorder(new EmptyBorder(60, 0, 0, 0));

            JComponent lblEmptyIcon = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    AppIcons.drawNavIcon(g2, "FEED", getWidth() / 2, getHeight() / 2, new Color(148, 163, 184));
                    g2.dispose();
                }
            };
            lblEmptyIcon.setPreferredSize(new Dimension(54, 54));
            lblEmptyIcon.setMaximumSize(new Dimension(54, 54));
            lblEmptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblEmpty = new JLabel("Chưa có bài viết nào phù hợp.");
            lblEmpty.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblEmpty.setForeground(new Color(100, 116, 139));
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblSub = new JLabel("Hãy trở thành người đầu tiên chia sẻ mã nguồn hoặc theo dõi thêm các lập trình viên khác!");
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblSub.setForeground(new Color(148, 163, 184));
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyState.add(lblEmptyIcon);
            emptyState.add(Box.createVerticalStrut(12));
            emptyState.add(lblEmpty);
            emptyState.add(Box.createVerticalStrut(6));
            emptyState.add(lblSub);

            postsListPanel.add(emptyState);
        } else {
            for (Post p : posts) {
                PostCardComponent card = new PostCardComponent(
                        p,
                        currentUsername,
                        onLikeClicked,
                        onAddComment,
                        onAuthorClicked
                );
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                postsListPanel.add(card);
                postsListPanel.add(Box.createVerticalStrut(16));
            }
        }

        postsListPanel.revalidate();
        postsListPanel.repaint();
        if (scrollPane != null) {
            SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
        }
    }

    public void resetFilterAndRefresh() {
        setFilter(false);
    }

    private void openCreatePostDialog() {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        CreatePostDialog dlg = new CreatePostDialog(topFrame, currentUsername, post -> {
            if (onCreatePost != null) {
                onCreatePost.accept(post);
            }
        });
        dlg.setVisible(true);
    }
}
