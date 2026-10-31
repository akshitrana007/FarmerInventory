package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class FarmerFeedbackFrame extends JFrame {

    private final Farmer currentFarmer;
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color PRIMARY_GREEN_DARK = new Color(60, 120, 50);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Color CARD_BG = Color.WHITE;

    public FarmerFeedbackFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle(LanguageManager.get("farmerfeedback.title.prefix") + farmer.getFullName());
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        // ---- Top banner ----
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(PRIMARY_GREEN);
        topBanner.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(LanguageManager.get("farmerfeedback.heading"));
        titleLabel.setFont(new Font("Nirmala UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topBanner.add(titleLabel, BorderLayout.WEST);

        Double avgRating = feedbackDAO.getAverageRatingForFarmer(farmer.getFarmerId());
        int ratingCount = feedbackDAO.getRatingCountForFarmer(farmer.getFarmerId());

        JLabel avgLabel = new JLabel(
                avgRating == null
                        ? LanguageManager.get("farmerfeedback.avg.norating")
                        : LanguageManager.get("farmerfeedback.avg.prefix") + String.format("%.1f", avgRating) + " (" + ratingCount + ")"
        );
        avgLabel.setFont(new Font("Nirmala UI", Font.BOLD, 14));
        avgLabel.setForeground(Color.WHITE);
        topBanner.add(avgLabel, BorderLayout.EAST);

        add(topBanner, BorderLayout.NORTH);

        // ---- Table ----
        String[] columns = {
                LanguageManager.get("farmerfeedback.col.customer"),
                LanguageManager.get("farmerfeedback.col.product"),
                LanguageManager.get("farmerfeedback.col.rating"),
                LanguageManager.get("farmerfeedback.col.comment"),
                LanguageManager.get("farmerfeedback.col.date")
        };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(210, 235, 200));
        table.setGridColor(new Color(235, 235, 235));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Nirmala UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_GREEN_DARK);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 32));

        List<FeedbackDetail> feedbackList = feedbackDAO.getFeedbackForFarmer(farmer.getFarmerId());
        for (FeedbackDetail f : feedbackList) {
            String stars = "★".repeat(f.getRating());
            tableModel.addRow(new Object[]{
                    f.getCustomerName(),
                    f.getProductName(),
                    stars,
                    f.getComment() == null ? "" : f.getComment(),
                    f.getCreatedAt() != null ? f.getCreatedAt().toString() : ""
            });
        }
        table.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                c.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BACKGROUND);
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        mainContent.add(scrollPane, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // ---- Bottom ----
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(BACKGROUND);
        JButton closeButton = new JButton(LanguageManager.get("farmerfeedback.button.close"));
        closeButton.setFont(new Font("Nirmala UI", Font.BOLD, 13));
        closeButton.setBackground(new Color(140, 140, 140));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        closeButton.addActionListener(e -> dispose());

        if (feedbackList.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("farmerfeedback.msg.nofeedback"));
        }

        setVisible(true);
    }
}