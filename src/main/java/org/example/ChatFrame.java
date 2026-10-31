package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ChatFrame extends JFrame {

    private final int myId;
    private final String myRole;
    private final String myName;
    private final int otherId;
    private final String otherRole;
    private final String otherName;

    private final MessageDAO messageDAO = new MessageDAO();

    private JTextArea chatArea;
    private JTextField inputField;

    private volatile boolean keepRefreshing = true;

    public ChatFrame(int myId, String myRole, String myName, int otherId, String otherRole, String otherName) {
        this.myId = myId;
        this.myRole = myRole;
        this.myName = myName;
        this.otherId = otherId;
        this.otherRole = otherRole;
        this.otherName = otherName;

        setTitle(LanguageManager.get("chat.title.prefix") + otherName);
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 250, 240));

        JLabel titleLabel = new JLabel(LanguageManager.get("chat.title.prefix") + otherName);
        titleLabel.setFont(new Font("Nirmala UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(56, 110, 54));
        titleLabel.setBorder(new EmptyBorder(10, 10, 5, 10));
        add(titleLabel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(new Color(240, 250, 240));
        bottomPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        JButton sendButton = new JButton(LanguageManager.get("chat.button.send"));
        sendButton.setFont(new Font("Nirmala UI", Font.PLAIN, 13));
        sendButton.setBackground(new Color(76, 140, 74));
        sendButton.setForeground(Color.WHITE);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                keepRefreshing = false;
            }
        });

        loadConversation();
        startAutoRefresh();

        setVisible(true);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        boolean success = messageDAO.sendMessage(myId, myRole, otherId, otherRole, text);
        if (success) {
            inputField.setText("");
            loadConversation();
        } else {
            JOptionPane.showMessageDialog(this, LanguageManager.get("chat.msg.sendfail"));
        }
    }

    private void loadConversation() {
        // Mark any messages the OTHER person sent to me as read, since I'm looking at them now.
        messageDAO.markConversationRead(myId, myRole, otherId, otherRole);

        List<Message> messages = messageDAO.getConversation(myId, myRole, otherId, otherRole);
        StringBuilder sb = new StringBuilder();

        for (Message m : messages) {
            String senderLabel = (m.getSenderId() == myId && m.getSenderRole().equals(myRole)) ? LanguageManager.get("chat.label.you") : otherName;
            String time = m.getSentAt() != null ? m.getSentAt().toString() : "";
            sb.append("[").append(time).append("] ").append(senderLabel).append(": ").append(m.getContent()).append("\n\n");
        }

        chatArea.setText(sb.toString());
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void startAutoRefresh() {
        Thread refreshThread = new Thread(() -> {
            while (keepRefreshing) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    break;
                }
                if (keepRefreshing) {
                    SwingUtilities.invokeLater(this::loadConversation);
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
}