package fr.uga.miashs.dciss.chatservice.client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InstantMessengerGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JTextField usernameField;
    private JButton connectButton;

    public InstantMessengerGUI() {
        setTitle("Messagerie Instantanée");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel connectionPanel = new JPanel();
        connectionPanel.setLayout(new BorderLayout());

        JPanel connectInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        connectInputPanel.add(new JLabel("Pseudo:"));
        usernameField = new JTextField(15);
        connectInputPanel.add(usernameField);
        connectButton = new JButton("Se connecter");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
        connectInputPanel.add(connectButton);
        connectionPanel.add(connectInputPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Connexion", connectionPanel);

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Envoyer");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Chat", chatPanel);

        add(tabbedPane);
    }

    private void connect() {
        String username = usernameField.getText().trim();
        if (!username.isEmpty()) {
            // Ajoutez le code ici pour gérer la connexion au serveur avec le pseudo
            chatArea.append("Connecté en tant que: " + username + "\n");
            // Vous pouvez également changer d'onglet après la connexion réussie
            JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
            tabbedPane.setSelectedIndex(1); // Onglet de chat
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un pseudo valide.", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // Ajoutez le code ici pour envoyer le message au serveur ou au destinataire approprié
            chatArea.append("Moi: " + message + "\n");
            messageField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                InstantMessengerGUI gui = new InstantMessengerGUI();
                gui.setVisible(true);
            }
        });
    }
}
