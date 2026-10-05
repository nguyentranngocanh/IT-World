package client;

import model.UserProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Giao diện Khám phá & Kết nối Mạng lưới Lập trình viên (Dev Network)
 * Hỗ trợ: Tìm kiếm theo Tên / Kỹ năng IT, Chế độ Theo dõi (Follow/Unfollow), Nhắn tin trực tiếp.
 */
public class DevNetworkPanel extends JPanel {
    private final String currentUsername;
    private final Runnable onRefresh;
    private final Consumer<String> onToggleFollow;
    private final Consumer<String> onDirectChat;
    private final Consumer<String> onViewProfile;

    private List<UserProfile> allDevs = new ArrayList<>();
    private JPanel gridPanel;
    private JTextField txtSearch;

    public DevNetworkPanel(String currentUsername,
                           Runnable onRefresh,
                           Consumer<String> onToggleFollow,
                           Consumer<String> onDirectChat,
                           Consumer<String> onViewProfile) {
        this.currentUsername = currentUsername;
        this.onRefresh = onRefresh;
        this.onToggleFollow = onToggleFollow;
        this.onDirectChat = onDirectChat;
        this.onViewProfile = onViewProfile;

        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));

        initUI();
    }

    private void initUI() {
        // Top Toolbar & Search Bar
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Cộng đồng Lập trình viên IT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Kết nối, học hỏi và trao đổi kinh nghiệm cùng các chuyên gia IT");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblSub);
        topBar.add(titlePanel, BorderLayout.WEST);

        // Search & Refresh Action
        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchActions.setOpaque(false);

        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setToolTipText("Tìm kiếm theo tên hoặc công nghệ (vd: Java, Senior, Docker)...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterDevs(); }
            public void removeUpdate(DocumentEvent e) { filterDevs(); }
            public void changedUpdate(DocumentEvent e) { filterDevs(); }
        });
        JLabel lblSearchText = new JLabel("Tìm:");
        lblSearchText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearchText.setForeground(new Color(100, 116, 139));
        searchActions.add(lblSearchText);
        searchActions.add(txtSearch);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setForeground(new Color(71, 85, 105));
        btnRefresh.setBackground(new Color(241, 245, 249));
        btnRefresh.setBorder(new EmptyBorder(7, 12, 7, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> {
            if (onRefresh != null) onRefresh.run();
        });
        searchActions.add(btnRefresh);

        topBar.add(searchActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Grid Cards Area
        gridPanel = new JPanel();
        gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void setDevList(List<UserProfile> devs) {
        this.allDevs = (devs != null) ? new ArrayList<>(devs) : new ArrayList<>();
        filterDevs();
    }

    private void filterDevs() {
        gridPanel.removeAll();
        String query = txtSearch.getText().trim().toLowerCase();

        List<UserProfile> filtered = new ArrayList<>();
        for (UserProfile p : allDevs) {
            if (p.getUsername().equalsIgnoreCase(currentUsername)) continue; // Không tự hiển thị bản thân trong danh sách follow

            boolean match = query.isEmpty()
                    || p.getUsername().toLowerCase().contains(query)
                    || p.getFullName().toLowerCase().contains(query)
                    || p.getItLevel().toLowerCase().contains(query)
                    || p.getTechStack().toLowerCase().contains(query);
            if (match) filtered.add(p);
        }

        if (filtered.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setOpaque(false);
            empty.setBorder(new EmptyBorder(60, 0, 0, 0));

            JLabel lblEmpty = new JLabel("Không tìm thấy lập trình viên phù hợp.");
            lblEmpty.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblEmpty.setForeground(new Color(100, 116, 139));
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(lblEmpty);

            gridPanel.add(empty);
        } else {
            for (UserProfile p : filtered) {
                JPanel card = createDevCard(p);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                gridPanel.add(card);
                gridPanel.add(Box.createVerticalStrut(14));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createDevCard(UserProfile p) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));
        card.setMaximumSize(new Dimension(860, 110));

        // Left Avatar
        AvatarPanel avatar = new AvatarPanel(p.getUsername(), 52, true);
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onViewProfile != null) onViewProfile.accept(p.getUsername());
            }
        });
        card.add(avatar, BorderLayout.WEST);

        // Center Details
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.setOpaque(false);

        JLabel lblName = new JLabel(p.getFullName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(new Color(15, 23, 42));
        lblName.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblName.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onViewProfile != null) onViewProfile.accept(p.getUsername());
            }
        });
        row1.add(lblName);

        JLabel lblTag = new JLabel("@" + p.getUsername());
        lblTag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTag.setForeground(new Color(100, 116, 139));
        row1.add(lblTag);

        JLabel lblLevel = new JLabel(" " + p.getItLevel() + " ");
        lblLevel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLevel.setForeground(new Color(37, 99, 235));
        lblLevel.setBackground(new Color(239, 246, 255));
        lblLevel.setOpaque(true);
        lblLevel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1),
                new EmptyBorder(2, 6, 2, 6)
        ));
        row1.add(lblLevel);

        center.add(row1);
        center.add(Box.createVerticalStrut(4));

        // Bio snippet
        String bio = p.getBio().isEmpty() ? "Lập trình viên tại IT World" : p.getBio();
        if (bio.length() > 80) bio = bio.substring(0, 77) + "...";
        JLabel lblBio = new JLabel(bio);
        lblBio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBio.setForeground(new Color(71, 85, 105));
        center.add(lblBio);
        center.add(Box.createVerticalStrut(6));

        // Tech stack preview
        JPanel techRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        techRow.setOpaque(false);
        for (String tech : p.getTechStackList()) {
            JLabel lblTech = new JLabel(tech);
            lblTech.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblTech.setForeground(new Color(100, 116, 139));
            lblTech.setBackground(new Color(241, 245, 249));
            lblTech.setOpaque(true);
            lblTech.setBorder(new EmptyBorder(2, 6, 2, 6));
            techRow.add(lblTech);
        }
        center.add(techRow);

        card.add(center, BorderLayout.CENTER);

        // Right Action Buttons (Follow / Chat)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        actions.setOpaque(false);

        JButton btnChat = new JButton("Nhắn tin");
        btnChat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChat.setForeground(new Color(71, 85, 105));
        btnChat.setBackground(new Color(241, 245, 249));
        btnChat.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnChat.setFocusPainted(false);
        btnChat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChat.addActionListener(e -> {
            if (onDirectChat != null) onDirectChat.accept(p.getUsername());
        });
        actions.add(btnChat);

        JButton btnFollow = new JButton(p.isFollowing() ? "Đang theo dõi" : "+ Theo dõi");
        btnFollow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFollow.setForeground(p.isFollowing() ? new Color(71, 85, 105) : Color.WHITE);
        btnFollow.setBackground(p.isFollowing() ? new Color(241, 245, 249) : new Color(37, 99, 235));
        btnFollow.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnFollow.setFocusPainted(false);
        btnFollow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFollow.addActionListener(e -> {
            if (onToggleFollow != null) onToggleFollow.accept(p.getUsername());
        });
        actions.add(btnFollow);

        card.add(actions, BorderLayout.EAST);
        return card;
    }
}
