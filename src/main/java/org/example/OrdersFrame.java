package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class OrdersFrame extends JFrame {

    private final Farmer currentFarmer;
    private DefaultTableModel tableModel;
    private JTable ordersTable;
    private List<Order> currentOrders;

    private final OrderDAO orderDAO = new OrderDAO();

    private static final Color PRIMARY_GREEN = new Color(76, 145, 65);
    private static final Color PRIMARY_GREEN_DARK = new Color(60, 120, 50);
    private static final Color BACKGROUND = new Color(245, 250, 240);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color ALERT_RED = new Color(200, 60, 60);
    private static final Color ROW_ALT = new Color(240, 248, 238);

    public OrdersFrame(Farmer farmer) {
        this.currentFarmer = farmer;

        setTitle("Order Requests - " + farmer.getFullName());
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND);

        // ---- Top banner (matches Dashboard/Inventory style) ----
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(PRIMARY_GREEN);
        topBanner.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titleLabel = new JLabel("Order Requests");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        topBanner.add(titleLabel, BorderLayout.WEST);

        add(topBanner, BorderLayout.NORTH);

        // ---- Center card containing table ----
        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(CARD_BG);
        centerCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Order ID", "Customer", "Customer Contact", "Product", "Quantity", "Price/Unit", "Requested At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                } else {
                    c.setBackground(new Color(200, 230, 195));
                }
                return c;
            }
        };
        ordersTable.setRowHeight(32);
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ordersTable.setShowGrid(false);
        ordersTable.setIntercellSpacing(new Dimension(0, 0));
        ordersTable.setSelectionBackground(new Color(200, 230, 195));
        ordersTable.setFillsViewportHeight(true);

        JTableHeader header = ordersTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_GREEN_DARK);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 36));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < ordersTable.getColumnCount(); i++) {
            ordersTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        add(centerCard, BorderLayout.CENTER);

        // ---- Bottom panel: buttons ----
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomPanel.setBackground(BACKGROUND);

        JButton acceptButton = createStyledButton("Accept Selected", PRIMARY_GREEN);
        JButton rejectButton = createStyledButton("Reject Selected", ALERT_RED);
        JButton refreshButton = createStyledButton("Refresh", new Color(120, 120, 120));

        bottomPanel.add(acceptButton);
        bottomPanel.add(rejectButton);
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadPendingOrders();

        acceptButton.addActionListener(e -> handleAccept());
        rejectButton.addActionListener(e -> handleReject());
        refreshButton.addActionListener(e -> loadPendingOrders());

        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadPendingOrders() {
        currentOrders = orderDAO.getPendingOrdersForFarmer(currentFarmer.getFarmerId());
        tableModel.setRowCount(0);

        for (Order o : currentOrders) {
            tableModel.addRow(new Object[]{
                    o.getOrderId(),
                    o.getCustomerName(),
                    o.getCustomerContact(),
                    o.getProductName(),
                    o.getQuantity(),
                    o.getPricePerUnit(),
                    o.getRequestedAt()
            });
        }

        if (currentOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending order requests right now.");
        }
    }

    private void handleAccept() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order first.");
            return;
        }

        Order selectedOrder = currentOrders.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Accept order for " + selectedOrder.getQuantity() + " " + selectedOrder.getProductName() +
                        " from " + selectedOrder.getCustomerName() + "?",
                "Confirm Accept", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = orderDAO.acceptOrder(selectedOrder.getOrderId());

        if (success) {
            JOptionPane.showMessageDialog(this, "Order accepted and sale recorded!");
        } else {
            JOptionPane.showMessageDialog(this, "Could not accept order (maybe not enough stock left).");
        }

        loadPendingOrders();
    }

    private void handleReject() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order first.");
            return;
        }

        Order selectedOrder = currentOrders.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Reject order for " + selectedOrder.getQuantity() + " " + selectedOrder.getProductName() +
                        " from " + selectedOrder.getCustomerName() + "?",
                "Confirm Reject", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = orderDAO.rejectOrder(selectedOrder.getOrderId());

        if (success) {
            JOptionPane.showMessageDialog(this, "Order rejected.");
        } else {
            JOptionPane.showMessageDialog(this, "Could not reject order.");
        }

        loadPendingOrders();
    }
}