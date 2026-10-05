package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private List<String> allUsers = new ArrayList<>();
    private JList<String> userList;
    private JTextField txtSearch;
    private Map<String, String> lastMessages = new HashMap<>();
    private Map<String, String> lastMessageTimes = new HashMap<>();

    public SidebarPanel(String myUsername, int myPeerPort, Consumer<String> onUserSelected) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_SIDEBAR);
        setPreferredSize(new Dimension(310, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        // 1. Profile User
        JPanel profile = new JPanel(new BorderLayout(12, 0));
        profile.setBackground(UITheme.BG_SIDEBAR);
        profile.setBorder(new EmptyBorder(16, 16, 14, 16));
        profile.add(new AvatarPanel(myUsername, 44, true), BorderLayout.WEST);

        JPanel myInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        myInfo.setOpaque(false);
        JLabel lblName = new JLabel(myUsername);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel lblPort = new JLabel("● Online • Cổng: " + myPeerPort);
        lblPort.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPort.setForeground(UITheme.ONLINE);
        myInfo.add(lblName);
        myInfo.add(lblPort);
        profile.add(myInfo, BorderLayout.CENTER);

        // 2. Search Field
        JPanel searchBox = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        searchBox.setOpaque(false);
        searchBox.setBorder(new EmptyBorder(6, 12, 6, 12));
        JComponent searchIcon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppIcons.drawInputIcon(g2, "search", 2, getHeight() / 2, new Color(148, 163, 184));
                g2.dispose();
            }
        };
        searchIcon.setPreferredSize(new Dimension(18, 18));
        searchBox.add(searchIcon, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setOpaque(false);
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchBox.add(txtSearch, BorderLayout.CENTER);

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(UITheme.BG_SIDEBAR);
        searchWrapper.setBorder(new EmptyBorder(0, 16, 12, 16));
        searchWrapper.add(searchBox);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(profile, BorderLayout.NORTH);
        top.add(searchWrapper, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // 3. User List
        userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setBackground(UITheme.BG_SIDEBAR);
        userList.setCellRenderer(new ModernUserCellRenderer(lastMessages, lastMessageTimes));
        userList.setFixedCellHeight(68);

        JScrollPane scroll = new JScrollPane(userList);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        add(scroll, BorderLayout.CENTER);

        // Events
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onUserSelected.accept(userList.getSelectedValue());
            }
        });
    }

    public void setUsers(List<String> users) {
        this.allUsers = new ArrayList<>(users);
        filter();
    }

    public void updateSnippet(String user, String snippet, String time) {
        lastMessages.put(user, snippet);
        lastMessageTimes.put(user, time);
        userList.repaint();
    }

    public void clearUserHistory(String user) {
        lastMessages.remove(user);
        lastMessageTimes.remove(user);
        userList.repaint();
    }

    private void filter() {
        String q = txtSearch.getText().trim().toLowerCase();
        String curr = userList.getSelectedValue();
        listModel.clear();
        for (String u : allUsers) {
            if (q.isEmpty() || u.toLowerCase().contains(q)) listModel.addElement(u);
        }
        if (curr != null && listModel.contains(curr)) userList.setSelectedValue(curr, true);
    }

    public void selectUser(String username) {
        if (username != null) {
            if (!allUsers.contains(username)) {
                allUsers.add(username);
                filter();
            }
            userList.setSelectedValue(username, true);
        }
    }

    public String getSelectedUser() { return userList.getSelectedValue(); }
}