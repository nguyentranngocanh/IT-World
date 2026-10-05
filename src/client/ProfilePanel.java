package client;

import model.UserProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.function.Consumer;

/**
 * Giao diện Trang cá nhân Lập trình viên (Developer Profile)
 * Hiển thị: Avatar, Trình độ IT (Level), Kỹ năng công nghệ (Tech Stack), Bio, GitHub và Thống kê Follow.
 */
public class ProfilePanel extends JPanel {
    private final String loggedInUsername;
    private final Consumer<UserProfile> onUpdateProfile;
    private final Consumer<String> onToggleFollow;
    private final Consumer<String> onRefreshProfile;

    private UserProfile currentProfile;
    private JPanel contentPanel;

    public ProfilePanel(String loggedInUsername,
                        Consumer<UserProfile> onUpdateProfile,
                        Consumer<String> onToggleFollow,
                        Consumer<String> onRefreshProfile) {
        this.loggedInUsername = loggedInUsername;
        this.onUpdateProfile = onUpdateProfile;
        this.onToggleFollow = onToggleFollow;
        this.onRefreshProfile = onRefreshProfile;

        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));

        initUI();
    }

    private void initUI() {
        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JLabel lblTitle = new JLabel("Trang cá nhân Lập trình viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        topBar.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Tải lại");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setForeground(new Color(71, 85, 105));
        btnRefresh.setBackground(new Color(241, 245, 249));
        btnRefresh.setBorder(new EmptyBorder(7, 12, 7, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> {
            if (currentProfile != null && onRefreshProfile != null) {
                onRefreshProfile.accept(currentProfile.getUsername());
            }
        });
        topBar.add(btnRefresh, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Body Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void setProfile(UserProfile profile) {
        this.currentProfile = profile;
        contentPanel.removeAll();

        if (profile == null) {
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        boolean isMe = profile.getUsername().equalsIgnoreCase(loggedInUsername);

        // 1. Profile Hero Card
        JPanel heroCard = new JPanel(new BorderLayout(24, 0));
        heroCard.setBackground(Color.WHITE);
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));
        heroCard.setMaximumSize(new Dimension(860, 220));

        // Avatar
        AvatarPanel avatar = new AvatarPanel(profile.getUsername(), 78, true);
        heroCard.add(avatar, BorderLayout.WEST);

        // Center Details
        JPanel centerInfo = new JPanel();
        centerInfo.setLayout(new BoxLayout(centerInfo, BoxLayout.Y_AXIS));
        centerInfo.setOpaque(false);

        // Name + Level Badge Row
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        nameRow.setOpaque(false);

        JLabel lblName = new JLabel(profile.getFullName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setForeground(new Color(15, 23, 42));
        nameRow.add(lblName);

        JLabel lblUserTag = new JLabel("@" + profile.getUsername());
        lblUserTag.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUserTag.setForeground(new Color(100, 116, 139));
        nameRow.add(lblUserTag);

        JLabel lblLevel = new JLabel(" " + profile.getItLevel() + " ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(238, 242, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(199, 210, 254));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblLevel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLevel.setForeground(new Color(79, 70, 229));
        lblLevel.setBorder(new EmptyBorder(3, 8, 3, 8));
        nameRow.add(lblLevel);

        centerInfo.add(nameRow);
        centerInfo.add(Box.createVerticalStrut(8));

        // Bio
        JLabel lblBio = new JLabel("<html>" + (profile.getBio().isEmpty() ? "<i>Chưa có phần giới thiệu</i>" : profile.getBio()) + "</html>");
        lblBio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBio.setForeground(new Color(71, 85, 105));
        centerInfo.add(lblBio);
        centerInfo.add(Box.createVerticalStrut(12));

        // Stats Row
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        statsRow.setOpaque(false);
        statsRow.add(createStatItem(String.valueOf(profile.getPostsCount()), "Bài viết"));
        statsRow.add(createStatItem(String.valueOf(profile.getFollowersCount()), "Người theo dõi"));
        statsRow.add(createStatItem(String.valueOf(profile.getFollowingCount()), "Đang theo dõi"));
        centerInfo.add(statsRow);

        heroCard.add(centerInfo, BorderLayout.CENTER);

        // Right Actions (Edit or Follow)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setOpaque(false);

        if (isMe) {
            JButton btnEdit = new JButton("Chỉnh sửa hồ sơ");
            btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnEdit.setForeground(Color.WHITE);
            btnEdit.setBackground(new Color(37, 99, 235));
            btnEdit.setBorder(new EmptyBorder(8, 16, 8, 16));
            btnEdit.setFocusPainted(false);
            btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnEdit.addActionListener(e -> openEditDialog());
            actionPanel.add(btnEdit);
        } else {
            JButton btnFollow = new JButton(profile.isFollowing() ? "Đang theo dõi" : "+ Theo dõi");
            btnFollow.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnFollow.setForeground(profile.isFollowing() ? new Color(71, 85, 105) : Color.WHITE);
            btnFollow.setBackground(profile.isFollowing() ? new Color(241, 245, 249) : new Color(37, 99, 235));
            btnFollow.setBorder(new EmptyBorder(8, 18, 8, 18));
            btnFollow.setFocusPainted(false);
            btnFollow.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnFollow.addActionListener(e -> {
                if (onToggleFollow != null) onToggleFollow.accept(profile.getUsername());
            });
            actionPanel.add(btnFollow);
        }
        heroCard.add(actionPanel, BorderLayout.EAST);

        contentPanel.add(heroCard);
        contentPanel.add(Box.createVerticalStrut(18));

        // 2. Tech Stack & GitHub Section
        JPanel techCard = new JPanel();
        techCard.setLayout(new BoxLayout(techCard, BoxLayout.Y_AXIS));
        techCard.setBackground(Color.WHITE);
        techCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));
        techCard.setMaximumSize(new Dimension(860, 180));

        JLabel lblTechTitle = new JLabel("NĂNG LỰC CÔNG NGHỆ & TECH STACK");
        lblTechTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTechTitle.setForeground(new Color(100, 116, 139));
        techCard.add(lblTechTitle);
        techCard.add(Box.createVerticalStrut(10));

        JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        tagsPanel.setOpaque(false);

        for (String tech : profile.getTechStackList()) {
            JLabel lblTech = new JLabel(tech);
            lblTech.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTech.setForeground(new Color(30, 41, 59));
            lblTech.setBackground(new Color(241, 245, 249));
            lblTech.setOpaque(true);
            lblTech.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
            tagsPanel.add(lblTech);
        }
        techCard.add(tagsPanel);
        techCard.add(Box.createVerticalStrut(14));

        // GitHub Link
        if (profile.getGithubUrl() != null && !profile.getGithubUrl().isEmpty()) {
            JPanel gitRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            gitRow.setOpaque(false);

            JLabel lblGit = new JLabel("GitHub: " + profile.getGithubUrl());
            lblGit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblGit.setForeground(new Color(37, 99, 235));
            gitRow.add(lblGit);

            JButton btnCopyGit = new JButton("Sao chép link");
            btnCopyGit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnCopyGit.setForeground(new Color(100, 116, 139));
            btnCopyGit.setBackground(new Color(241, 245, 249));
            btnCopyGit.setBorder(new EmptyBorder(2, 8, 2, 8));
            btnCopyGit.setFocusPainted(false);
            btnCopyGit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCopyGit.addActionListener(e -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(profile.getGithubUrl()), null);
                btnCopyGit.setText("Đã chép!");
                Timer t = new Timer(2000, evt -> btnCopyGit.setText("Sao chép link"));
                t.setRepeats(false);
                t.start();
            });
            gitRow.add(btnCopyGit);

            techCard.add(gitRow);
        }

        contentPanel.add(techCard);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createStatItem(String count, String label) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lblCount = new JLabel(count);
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCount.setForeground(new Color(15, 23, 42));

        JLabel lblText = new JLabel(label);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblText.setForeground(new Color(100, 116, 139));

        p.add(lblCount);
        p.add(lblText);
        return p;
    }

    private void openEditDialog() {
        if (currentProfile == null) return;
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        EditProfileDialog dlg = new EditProfileDialog(topFrame, currentProfile, updated -> {
            if (onUpdateProfile != null) {
                onUpdateProfile.accept(updated);
            }
            setProfile(updated);
        });
        dlg.setVisible(true);
    }
}
