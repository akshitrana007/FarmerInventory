package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomerDashboardFrame extends JFrame {

    private final Customer currentCustomer;
    private DefaultTableModel tableModel;
    private JTable productTable;
    private List<Product> currentProducts;
    private JButton chatButton;

    private final ProductDAO productDAO = new ProductDAO();
    private final FarmerDAO farmerDAO = new FarmerDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    private volatile boolean keepMonitoring = true;

    public CustomerDashboardFrame(Customer customer) {
        this.currentCustomer = customer;

        setTitle(LanguageManager.get("custdash.title.prefix") + customer.getCustomerName());
        setSize(1100, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 250, 240));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(240, 250, 240));

        JLabel welcomeLabel = new JLabel(LanguageManager.get("custdash.welcome.prefix") + customer.getCustomerName() + "!");
        welcomeLabel.setFont(new Font("Nirmala UI", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(56, 110, 54));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(240, 250, 240));

        JTextField productField = new JTextField(12);
        JTextField areaField = new JTextField(12);
        JTextField farmerNameField = new JTextField(12);

        JButton searchButton = new JButton(LanguageManager.get("custdash.button.search"));
        searchButton.setBackground(new Color(76, 140, 74));
        searchButton.setForeground(Color.WHITE);

        JButton showAllButton = new JButton(LanguageManager.get("custdash.button.showall"));

        searchPanel.add(new JLabel(LanguageManager.get("custdash.label.product")));
        searchPanel.add(productField);
        searchPanel.add(new JLabel(LanguageManager.get("custdash.label.area")));
        searchPanel.add(areaField);
        searchPanel.add(new JLabel(LanguageManager.get("custdash.label.farmername")));
        searchPanel.add(farmerNameField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        topPanel.add(welcomeLabel);
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {
                LanguageManager.get("custdash.col.product"),
                LanguageManager.get("custdash.col.farmer"),
                LanguageManager.get("custdash.col.farmercontact"),
                LanguageManager.get("custdash.col.area"),
                LanguageManager.get("custdash.col.farmaddress"),
                LanguageManager.get("custdash.col.price"),
                LanguageManager.get("custdash.col.stock"),
                LanguageManager.get("custdash.col.unit"),
                LanguageManager.get("custdash.col.rating")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setRowHeight(24);
        productTable.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        productTable.getColumnModel().getColumn(8).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                c.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
                return c;
            }
        });
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(new Color(240, 250, 240));

        JButton orderButton = new JButton(LanguageManager.get("custdash.button.order"));
        orderButton.setFont(new Font("Nirmala UI", Font.BOLD, 14));
        orderButton.setBackground(new Color(76, 140, 74));
        orderButton.setForeground(Color.WHITE);

        JButton myOrdersButton = new JButton(LanguageManager.get("custdash.button.myorders"));
        myOrdersButton.setFont(new Font("Nirmala UI", Font.BOLD, 14));
        myOrdersButton.setBackground(new Color(76, 140, 74));
        myOrdersButton.setForeground(Color.WHITE);

        chatButton = new JButton(LanguageManager.get("custdash.button.chat"));
        chatButton.setFont(new Font("Nirmala UI", Font.BOLD, 14));
        chatButton.setBackground(new Color(76, 140, 74));
        chatButton.setForeground(Color.WHITE);

        JButton logoutButton = new JButton(LanguageManager.get("custdash.button.logout"));

        bottomPanel.add(orderButton);
        bottomPanel.add(myOrdersButton);
        bottomPanel.add(chatButton);
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadAllProducts();

        showAllButton.addActionListener(e -> {
            productField.setText("");
            areaField.setText("");
            farmerNameField.setText("");
            loadAllProducts();
        });

        searchButton.addActionListener(e -> {
            String productKeyword = productField.getText().trim();
            String areaKeyword = areaField.getText().trim();
            String farmerNameKeyword = farmerNameField.getText().trim();
            runCombinedSearch(productKeyword, areaKeyword, farmerNameKeyword);
        });

        orderButton.addActionListener(e -> sendOrderRequest());

        myOrdersButton.addActionListener(e -> new CustomerOrdersFrame(currentCustomer));

        chatButton.addActionListener(e -> openChat());

        logoutButton.addActionListener(e -> {
            keepMonitoring = false;
            dispose();
            new RoleSelectionFrame();
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                keepMonitoring = false;
            }
        });

        updateUnreadBadge();
        startMessageMonitoringThread();

        setVisible(true);
    }

    private void loadAllProducts() {
        runCombinedSearch("", "", "");
    }

    private void updateUnreadBadge() {
        int unreadMessages = messageDAO.getUnreadCount(currentCustomer.getCustomerId(), "CUSTOMER");
        if (unreadMessages > 0) {
            chatButton.setText(LanguageManager.get("custdash.button.chat") + " (" + unreadMessages + ")");
        } else {
            chatButton.setText(LanguageManager.get("custdash.button.chat"));
        }
    }

    private void startMessageMonitoringThread() {
        Thread monitorThread = new Thread(() -> {
            while (keepMonitoring) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
                if (keepMonitoring) {
                    SwingUtilities.invokeLater(this::updateUnreadBadge);
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void runCombinedSearch(String productKeyword, String areaKeyword, String farmerNameKeyword) {
        List<Farmer> allFarmers = farmerDAO.getAllFarmers();

        Set<Integer> matchingFarmerIds = new HashSet<>();

        boolean areaFilterUsed = !areaKeyword.isEmpty();
        boolean nameFilterUsed = !farmerNameKeyword.isEmpty();

        if (!areaFilterUsed && !nameFilterUsed) {
            for (Farmer f : allFarmers) {
                matchingFarmerIds.add(f.getFarmerId());
            }
        } else {
            for (Farmer f : allFarmers) {
                boolean areaMatches = !areaFilterUsed ||
                        (f.getFarmLocation() != null && f.getFarmLocation().toLowerCase().contains(areaKeyword.toLowerCase()));
                boolean nameMatches = !nameFilterUsed ||
                        (f.getFullName() != null && f.getFullName().toLowerCase().contains(farmerNameKeyword.toLowerCase()));

                if (areaMatches && nameMatches) {
                    matchingFarmerIds.add(f.getFarmerId());
                }
            }
        }

        List<Product> results = new ArrayList<>();
        boolean productFilterUsed = !productKeyword.isEmpty();

        for (Integer farmerId : matchingFarmerIds) {
            List<Product> farmerProducts = productDAO.getProductsByFarmer(farmerId);
            for (Product p : farmerProducts) {
                boolean productMatches = !productFilterUsed ||
                        (p.getProductName() != null && p.getProductName().toLowerCase().contains(productKeyword.toLowerCase()));
                if (productMatches) {
                    results.add(p);
                }
            }
        }

        displayProducts(results);
    }

    private void displayProducts(List<Product> products) {
        this.currentProducts = products;
        tableModel.setRowCount(0);

        List<Farmer> allFarmers = farmerDAO.getAllFarmers();

        for (Product p : products) {
            String farmerName = "Unknown";
            String farmerArea = "Unknown";
            String farmerContact = "N/A";
            String farmerAddress = LanguageManager.get("custdash.notspecified");

            for (Farmer f : allFarmers) {
                if (f.getFarmerId() == p.getFarmerId()) {
                    farmerName = f.getFullName();
                    farmerArea = f.getFarmLocation();
                    farmerContact = f.getContactNumber();
                    if (f.getFarmAddress() != null && !f.getFarmAddress().trim().isEmpty()) {
                        farmerAddress = f.getFarmAddress();
                    }
                    break;
                }
            }

            Double avgRating = feedbackDAO.getAverageRating(p.getProductId());
            String ratingDisplay;
            if (avgRating == null) {
                ratingDisplay = LanguageManager.get("custdash.notrated");
            } else {
                int roundedStars = (int) Math.round(avgRating);
                ratingDisplay = "★".repeat(Math.max(roundedStars, 0)) + " (" + String.format("%.1f", avgRating) + ")";
            }

            tableModel.addRow(new Object[]{
                    p.getProductName(),
                    farmerName,
                    farmerContact,
                    farmerArea,
                    farmerAddress,
                    p.getPricePerUnit(),
                    p.getStockQuantity(),
                    p.getUnit(),
                    ratingDisplay
            });
        }

        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.msg.noresults"));
        }
    }

    private void sendOrderRequest() {
        int selectedRow = productTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.msg.selectproduct"));
            return;
        }

        Product selectedProduct = currentProducts.get(selectedRow);

        String quantityStr = JOptionPane.showInputDialog(this,
                LanguageManager.get("custdash.msg.qtyprompt") + selectedProduct.getStockQuantity() + " " + selectedProduct.getUnit() + "):");

        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.msg.validnumber"));
            return;
        }

        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.msg.qtypositive"));
            return;
        }

        if (quantity > selectedProduct.getStockQuantity()) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.msg.notenoughstock"));
            return;
        }

        // Find farmer's stored farm address to show for reference while entering distance
        String farmerAddressDisplay = LanguageManager.get("custdash.notspecified");
        List<Farmer> allFarmers = farmerDAO.getAllFarmers();
        for (Farmer f : allFarmers) {
            if (f.getFarmerId() == selectedProduct.getFarmerId()) {
                if (f.getFarmAddress() != null && !f.getFarmAddress().trim().isEmpty()) {
                    farmerAddressDisplay = f.getFarmAddress();
                }
                break;
            }
        }

        Object[] modeOptions = {
                LanguageManager.get("custdash.delivery.pickup"),
                LanguageManager.get("custdash.delivery.delivery")
        };

        int modeChoice = JOptionPane.showOptionDialog(
                this,
                LanguageManager.get("custdash.delivery.chooseMode"),
                LanguageManager.get("custdash.delivery.title"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                modeOptions,
                modeOptions[0]
        );

        if (modeChoice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        boolean isDelivery = (modeChoice == 1);
        String deliveryMode = isDelivery ? "DELIVERY" : "PICKUP";
        String deliveryAddress = null;
        double distanceKm = 0;
        double deliveryCharge = 0;

        if (isDelivery) {
            JTextField addressField = new JTextField(20);
            JTextField distanceField = new JTextField(10);
            JLabel chargeLabel = new JLabel(LanguageManager.get("custdash.delivery.charge") + ": ₹0.0");
            JLabel farmerAddressLabel = new JLabel("<html><b>" + LanguageManager.get("custdash.delivery.farmerlocation") + ":</b> " + farmerAddressDisplay + "</html>");
            farmerAddressLabel.setPreferredSize(new Dimension(320, 40));

            distanceField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void recalc() {
                    try {
                        double km = Double.parseDouble(distanceField.getText().trim());
                        double charge = DeliveryPricingService.calculateDeliveryCharge(quantity, km);
                        chargeLabel.setText(LanguageManager.get("custdash.delivery.charge") + ": ₹" + String.format("%.2f", charge));
                    } catch (NumberFormatException ex) {
                        chargeLabel.setText(LanguageManager.get("custdash.delivery.charge") + ": ₹0.0");
                    }
                }

                public void insertUpdate(javax.swing.event.DocumentEvent e) { recalc(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { recalc(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { recalc(); }
            });

            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.add(farmerAddressLabel);
            panel.add(new JLabel(LanguageManager.get("custdash.delivery.enteraddress") + ":"));
            panel.add(addressField);
            panel.add(new JLabel(LanguageManager.get("custdash.delivery.enterdistance") + ":"));
            panel.add(distanceField);
            panel.add(chargeLabel);

            int result = JOptionPane.showConfirmDialog(
                    this, panel,
                    LanguageManager.get("custdash.delivery.title"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            deliveryAddress = addressField.getText().trim();

            if (deliveryAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.delivery.addressrequired"));
                return;
            }

            try {
                distanceKm = Double.parseDouble(distanceField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.delivery.invaliddistance"));
                return;
            }

            if (distanceKm <= 0) {
                JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.delivery.invaliddistance"));
                return;
            }

            deliveryCharge = DeliveryPricingService.calculateDeliveryCharge(quantity, distanceKm);
        }

        boolean success = orderDAO.createOrder(
                currentCustomer.getCustomerId(),
                selectedProduct.getFarmerId(),
                selectedProduct.getProductId(),
                quantity,
                deliveryMode,
                deliveryAddress,
                distanceKm,
                deliveryCharge
        );

        if (success) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.msg.ordersuccess"));
        } else {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.msg.orderfail"));
        }
    }

    private void openChat() {
        int selectedRow = productTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("custdash.msg.selectforchat"));
            return;
        }

        Product selectedProduct = currentProducts.get(selectedRow);
        List<Farmer> allFarmers = farmerDAO.getAllFarmers();
        String farmerName = "Farmer";

        for (Farmer f : allFarmers) {
            if (f.getFarmerId() == selectedProduct.getFarmerId()) {
                farmerName = f.getFullName();
                break;
            }
        }

        new ChatFrame(
                currentCustomer.getCustomerId(), "CUSTOMER", currentCustomer.getCustomerName(),
                selectedProduct.getFarmerId(), "FARMER", farmerName
        );
    }
}