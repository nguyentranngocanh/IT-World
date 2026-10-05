package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Thanh điều hướng dọc (Navigation Rail) hiện đại 68px
 * Cho phép chuyển đổi linh hoạt giữa: Bảng tin (Feed), Nhắn tin (Chat), Cộng đồng (Network), Hồ sơ (Profile).
 */
public class NavigationRail extends JPanel {
    private final String currentUsername;
    private final Consumer<String> onTabSelected;
    private final List<RailButton> buttons = new ArrayList<>();
    private String selectedTab = "FEED";

    public NavigationRail(String username, Consumer<String> onTabSelected) {
        this.currentUsername = username;
        this.onTabSelected = onTabSelected;

        setPreferredSize(new Dimension(72, 0));
        setBackground(new Color(15, 23, 42)); // Slate-900 sang trọng
        setLayout(new BorderLayout());

        // Top: Logo Brand
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(16, 0, 16, 0));

        JComponent lblLogo = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppIcons.drawLogo(g2, 0, 0, getWidth());
                g2.dispose();
            }
        };
        lblLogo.setPreferredSize(new Dimension(42, 42));
        lblLogo.setMaximumSize(new Dimension(42, 42));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(lblLogo);

        JLabel lblBrand = new JLabel("IT World", SwingConstants.CENTER);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBrand.setForeground(new Color(148, 163, 184));
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(lblBrand);

        add(topPanel, BorderLayout.NORTH);

        // Center: Các nút điều hướng chính
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(12, 6, 0, 6));

        RailButton btnFeed = new RailButton("FEED", "Bảng tin");
        RailButton btnChat = new RailButton("CHAT", "Tin nhắn");
        RailButton btnNet = new RailButton("NETWORK", "Cộng đồng");
        RailButton btnProfile = new RailButton("PROFILE", "Hồ sơ");

        buttons.add(btnFeed);
        buttons.add(btnChat);
        buttons.add(btnNet);
        buttons.add(btnProfile);

        for (RailButton btn : buttons) {
            centerPanel.add(btn);
            centerPanel.add(Box.createVerticalStrut(10));
        }

        add(centerPanel, BorderLayout.CENTER);

        // Bottom: Avatar người dùng hiện tại
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        AvatarPanel userAvatar = new AvatarPanel(currentUsername, 38, true);
        userAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        userAvatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userAvatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectTab("PROFILE");
            }
        });
        bottomPanel.add(userAvatar);

        add(bottomPanel, BorderLayout.SOUTH);

        // Chọn mặc định FEED (không kích hoạt callback khi đang dựng giao diện)
        setSelectedTabOnly("FEED");
    }

    public void setSelectedTabOnly(String tabId) {
        this.selectedTab = tabId;
        for (RailButton btn : buttons) {
            btn.setSelected(btn.tabId.equals(tabId));
        }
    }

    public void selectTab(String tabId) {
        setSelectedTabOnly(tabId);
        if (onTabSelected != null) {
            onTabSelected.accept(tabId);
        }
    }

    public String getSelectedTab() {
        return selectedTab;
    }

    // =========================================================
    // Nút Tab tùy biến với hiệu ứng Hover & Active Pill Indicator
    // =========================================================
    class RailButton extends JPanel {
        final String tabId;
        private final JLabel lblText;
        private boolean isSelected = false;
        private boolean isHover = false;

        public RailButton(String tabId, String title) {
            this.tabId = tabId;
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(60, 56));
            setMaximumSize(new Dimension(60, 56));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);

            JComponent iconComp = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    Color c = isSelected ? Color.WHITE : (isHover ? Color.WHITE : new Color(148, 163, 184));
                    AppIcons.drawNavIcon(g2, tabId, getWidth() / 2, getHeight() / 2, c);
                    g2.dispose();
                }
            };
            iconComp.setPreferredSize(new Dimension(24, 24));
            iconComp.setMaximumSize(new Dimension(24, 24));
            iconComp.setAlignmentX(Component.CENTER_ALIGNMENT);

            lblText = new JLabel(title, SwingConstants.CENTER);
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblText.setAlignmentX(Component.CENTER_ALIGNMENT);

            content.add(Box.createVerticalGlue());
            content.add(iconComp);
            content.add(Box.createVerticalStrut(3));
            content.add(lblText);
            content.add(Box.createVerticalGlue());

            add(content, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHover = true;
                    updateColors();
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHover = false;
                    updateColors();
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectTab(tabId);
                }
            });

            updateColors();
        }

        public void setSelected(boolean sel) {
            this.isSelected = sel;
            updateColors();
            repaint();
        }

        private void updateColors() {
            if (isSelected) {
                lblText.setForeground(Color.WHITE);
            } else if (isHover) {
                lblText.setForeground(Color.WHITE);
            } else {
                lblText.setForeground(new Color(148, 163, 184));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isSelected) {
                g2.setColor(new Color(37, 99, 235));
                g2.fillRoundRect(4, 4, w - 8, h - 8, 12, 12);
            } else if (isHover) {
                g2.setColor(new Color(30, 41, 59));
                g2.fillRoundRect(4, 4, w - 8, h - 8, 12, 12);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
