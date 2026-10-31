package org.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class CustomerOrdersFrame extends JFrame {
    private final Customer currentCustomer;
    private final OrderDAO orderDAO = new OrderDAO();
    private final FarmerDAO farmerDAO = new FarmerDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private DefaultTableModel tableModel;
    private JTable orderTable;
    private List<Order> currentOrders;

    public CustomerOrdersFrame(Customer customer) {
        this.currentCustomer = customer;
        setTitle(LanguageManager.get("custorders.title.prefix") + customer.getCustomerName());
        setSize(1150, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 250, 240));

        JLabel titleLabel = new JLabel(LanguageManager.get("custorders.heading"));
        titleLabel.setFont(new Font("Nirmala UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(56, 110, 54));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                LanguageManager.get("custorders.col.product"),
                LanguageManager.get("custorders.col.farmer"),
                LanguageManager.get("custorders.col.qty"),
                LanguageManager.get("custorders.col.price"),
                LanguageManager.get("custorders.col.total"),
                LanguageManager.get("custorders.col.status"),
                LanguageManager.get("orders.col.deliverymode"),
                LanguageManager.get("orders.col.deliveryaddress"),
                LanguageManager.get("orders.col.distance"),
                LanguageManager.get("orders.col.deliverycharge"),
                LanguageManager.get("orders.col.deliverystatus"),
                LanguageManager.get("custorders.col.requestedat"),
                LanguageManager.get("custorders.col.respondedat")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(24);
        orderTable.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        orderTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(new Color(240, 250, 240));

        JButton rateButton = new JButton(LanguageManager.get("custorders.button.rate"));
        rateButton.setBackground(new Color(70, 130, 180));
        rateButton.setForeground(Color.WHITE);

        JButton refreshButton = new JButton(LanguageManager.get("custorders.button.refresh"));
        refreshButton.setBackground(new Color(76, 140, 74));
        refreshButton.setForeground(Color.WHITE);

        JButton closeButton = new JButton(LanguageManager.get("custorders.button.close"));

        bottomPanel.add(rateButton);
        bottomPanel.add(refreshButton);
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        rateButton.addActionListener(e -> handleRateOrder());
        refreshButton.addActionListener(e -> loadOrders());
        closeButton.addActionListener(e -> dispose());

        loadOrders();
        setVisible(true);
    }

    private void loadOrders() {
        tableModel.setRowCount(0);
        currentOrders = orderDAO.getOrdersForCustomer(currentCustomer.getCustomerId());
        List<Farmer> allFarmers = farmerDAO.getAllFarmers();

        for (Order o : currentOrders) {
            String farmerName = "Unknown";
            for (Farmer f : allFarmers) {
                if (f.getFarmerId() == o.getFarmerId()) {
                    farmerName = f.getFullName();
                    break;
                }
            }

            double total = o.getPricePerUnit() * o.getQuantity();
            String requestedAt = o.getRequestedAt() != null ? o.getRequestedAt().toString() : "";
            String respondedAt = o.getRespondedAt() != null ? o.getRespondedAt().toString() : LanguageManager.get("custorders.notyet");

            boolean isDelivery = "DELIVERY".equalsIgnoreCase(o.getDeliveryMode());

            String modeDisplay = isDelivery
                    ? LanguageManager.get("custdash.delivery.delivery")
                    : LanguageManager.get("custdash.delivery.pickup");

            String addressDisplay = isDelivery && o.getDeliveryAddress() != null
                    ? o.getDeliveryAddress() : LanguageManager.get("orders.na");

            String distanceDisplay = isDelivery
                    ? String.format("%.1f", o.getDistanceKm()) : LanguageManager.get("orders.na");

            String chargeDisplay = isDelivery
                    ? "₹" + String.format("%.2f", o.getDeliveryCharge()) : LanguageManager.get("orders.na");

            String deliveryStatusDisplay = deliveryStatusLabel(o.getDeliveryStatus());

            tableModel.addRow(new Object[]{
                    o.getProductName(),
                    farmerName,
                    o.getQuantity(),
                    o.getPricePerUnit(),
                    total,
                    o.getStatus(),
                    modeDisplay,
                    addressDisplay,
                    distanceDisplay,
                    chargeDisplay,
                    deliveryStatusDisplay,
                    requestedAt,
                    respondedAt
            });
        }

        if (currentOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custorders.msg.noorders"));
        }
    }

    private String deliveryStatusLabel(String rawStatus) {
        if (rawStatus == null) return LanguageManager.get("orders.deliverystatus.pending");
        switch (rawStatus) {
            case "OUT_FOR_DELIVERY": return LanguageManager.get("orders.deliverystatus.outfordelivery");
            case "READY_FOR_PICKUP": return LanguageManager.get("orders.deliverystatus.readyforpickup");
            case "COMPLETED": return LanguageManager.get("orders.deliverystatus.completed");
            default: return LanguageManager.get("orders.deliverystatus.pending");
        }
    }

    private void handleRateOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custorders.msg.selectorder"));
            return;
        }

        Order selectedOrder = currentOrders.get(selectedRow);

        if (!selectedOrder.getStatus().equalsIgnoreCase("ACCEPTED")) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custorders.msg.onlyaccepted"));
            return;
        }

        if (feedbackDAO.hasFeedback(selectedOrder.getOrderId())) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custorders.msg.alreadyrated"));
            return;
        }

        JComboBox<String> starDropdown = new JComboBox<>(new String[]{"★", "★★", "★★★", "★★★★", "★★★★★"});
        starDropdown.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        starDropdown.setSelectedIndex(4);

        JTextArea commentArea = new JTextArea(4, 20);
        JScrollPane commentScroll = new JScrollPane(commentArea);

        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridLayout(4, 1, 5, 5));
        panel.add(new JLabel(LanguageManager.get("custorders.dialog.rating")));
        panel.add(starDropdown);
        panel.add(new JLabel(LanguageManager.get("custorders.dialog.comment")));
        panel.add(commentScroll);

        int result = JOptionPane.showConfirmDialog(this, panel,
                LanguageManager.get("custorders.dialog.title"), JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            int rating = starDropdown.getSelectedIndex() + 1;
            String comment = commentArea.getText().trim();

            Feedback feedback = new Feedback();
            feedback.setOrderId(selectedOrder.getOrderId());
            feedback.setCustomerId(currentCustomer.getCustomerId());
            feedback.setFarmerId(selectedOrder.getFarmerId());
            feedback.setProductId(selectedOrder.getProductId());
            feedback.setRating(rating);
            feedback.setComment(comment);

            boolean success = feedbackDAO.addFeedback(feedback);

            if (success) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("custorders.msg.feedbacksuccess"));
            } else {
                JOptionPane.showMessageDialog(this, LanguageManager.get("custorders.msg.feedbackfail"));
            }
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";

            if (status.equalsIgnoreCase("ACCEPTED")) {
                c.setForeground(new Color(34, 120, 34));
            } else if (status.equalsIgnoreCase("REJECTED")) {
                c.setForeground(new Color(180, 40, 40));
            } else {
                c.setForeground(new Color(160, 120, 20));
            }

            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
}